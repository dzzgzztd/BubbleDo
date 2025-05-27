package com.example.bubbledo.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.bubbledo.cloud.DriveApi
import com.example.bubbledo.cloud.DriveServiceFactory
import com.example.bubbledo.model.Task
import com.example.bubbledo.notifications.ReminderWorker
import com.example.bubbledo.repository.TaskRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import com.google.api.client.http.InputStreamContent


class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("SyncPreferences", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    private val AUTH_CODE_KEY = "server_auth_code"

    private fun scheduleReminders(task: Task) {
        if (task.deadline == null || !task.reminderEnabled) return

        val workManager = WorkManager.getInstance(getApplication())
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val timesToNotify = listOf(
            task.deadline - 3 * dayInMillis, // за 3 дня
            task.deadline                     // в день дедлайна
        )

        Log.d("TaskViewModel", "Reminder set at ${task.deadline} and ${task.deadline - 3 * dayInMillis}")

        for (time in timesToNotify) {
            val delay = time - now
            if (delay > 0) {
                val data = workDataOf(
                    "TASK_ID" to task.id,
                    "TASK_TITLE" to task.title
                )

                val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .addTag("reminder_${task.id}")
                    .setInputData(data)
                    .build()

                workManager.enqueueUniqueWork(
                    "reminder_${task.id}_$time",
                    ExistingWorkPolicy.REPLACE,
                    reminderRequest
                )
            }
        }
    }

    private fun cancelReminders(taskId: String) {
        val workManager = WorkManager.getInstance(getApplication())
        val prefix = "reminder_${taskId}_"
        workManager.cancelAllWorkByTag(prefix)
    }

    private val taskRepository = TaskRepository(application.applicationContext)

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _isSyncEnabled = MutableStateFlow(false)
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled

    private var googleAccount: GoogleSignInAccount? = null
    private var driveApi: DriveApi? = null

    private val BACKUP_FILENAME = "tasks_backup.json"
    private val gson = Gson()

    init {
        _isSyncEnabled.value = sharedPreferences.getBoolean("isSyncEnabled", false)
        viewModelScope.launch {
            taskRepository.getAllTasks().collect {
                _tasks.value = it
                if (_isSyncEnabled.value) {
                    val savedAuthCode = sharedPreferences.getString(AUTH_CODE_KEY, null)
                    if (savedAuthCode != null) {
                        driveApi = DriveServiceFactory.create(savedAuthCode)
                        syncTaskToCloud()
                    } else {
                        setSyncEnabled(false)
                    }
                }
            }
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        _isSyncEnabled.value = enabled
        editor.putBoolean("isSyncEnabled", enabled).apply()
        if (!enabled) {
            googleAccount = null
            driveApi = null
        }
    }

    fun onGoogleAccountSignedIn(account: GoogleSignInAccount) {
        googleAccount = account
        val serverAuthCode = account.serverAuthCode ?: run {
            setSyncEnabled(false)
            return
        }
        editor.putString(AUTH_CODE_KEY, serverAuthCode).apply()
        driveApi = DriveServiceFactory.create(serverAuthCode)

        viewModelScope.launch {
            try {
                Log.d("TaskViewModel", "Начинаем загрузку задач из облака")
                fetchTasksFromCloud()?.let { cloudTasks ->
                    Log.d("TaskViewModel", "Загружено ${cloudTasks.size} задач из облака")
                    cloudTasks.forEach { taskRepository.insertOrUpdateTask(it) }
                }
                _isSyncEnabled.value = true
                Log.d("TaskViewModel", "Синхронизация включена")

                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Синхронизация успешно запущена!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Ошибка при синхронизации", e)
                setSyncEnabled(false)
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskRepository.insertOrUpdateTask(task)
            if (_isSyncEnabled.value) {
                syncTaskToCloud()
            }
        }
        scheduleReminders(task)
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.insertOrUpdateTask(task)
            if (_isSyncEnabled.value) {
                syncTaskToCloud()
            }
            scheduleReminders(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            if (_isSyncEnabled.value) {
                deleteTaskFromCloud(taskId)
            }
        }
        cancelReminders(taskId)
    }

    private suspend fun fetchTasksFromCloud(): List<Task>? = withContext(Dispatchers.IO) {
        try {
            val service = driveApi ?: return@withContext null
            var fileId = findBackupFileId(service)
            if (fileId == null) {
                uploadNewBackupFile(service, gson.toJson(emptyList<Task>()))
                fileId = findBackupFileId(service) ?: return@withContext null
            }

            val response = service.downloadFile(fileId)
            if (!response.isSuccessful) return@withContext null
            val json = response.body()?.string() ?: return@withContext null

            val listType = object : TypeToken<List<Task>>() {}.type
            return@withContext gson.fromJson<List<Task>>(json, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun syncTasksToCloud(tasks: List<Task>) = withContext(Dispatchers.IO) {
        try {
            val service = driveApi ?: return@withContext null
            val json = gson.toJson(tasks)
            val fileId = findBackupFileId(service)
            if (fileId == null) {
                uploadNewBackupFile(service, json)
            } else {
                updateBackupFile(service, fileId, json)
            }
            Log.d("Sync", "Данные синхронизированы")
        } catch (e: Exception) {
            Log.e("Sync", "Ошибка синхронизации", e)
        }
    }

    private suspend fun syncTaskToCloud() {
        syncTasksToCloud(_tasks.value)
    }

    private suspend fun deleteTaskFromCloud(taskId: String) {
        val filtered = _tasks.value.filter { it.id != taskId }
        syncTasksToCloud(filtered)
    }

    private suspend fun findBackupFileId(service: DriveApi): String? {
        val result = service.listFiles(
            spaces = "appDataFolder",
            query = "name = '$BACKUP_FILENAME'"
        )
        return result.files.firstOrNull()?.id
    }

    private suspend fun uploadNewBackupFile(service: DriveApi, content: String) {
        val metadata = DriveServiceFactory.createMetadataJson(BACKUP_FILENAME)
        val filePart = DriveServiceFactory.createFilePart(content.toByteArray(), BACKUP_FILENAME)
        service.uploadFile(metadata, filePart)
    }

    private suspend fun updateBackupFile(service: DriveApi, fileId: String, content: String) {
        val requestBody = DriveServiceFactory.createJsonRequestBody(content)
        service.updateFile(fileId, requestBody)
    }


}
