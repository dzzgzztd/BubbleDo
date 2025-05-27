package com.example.bubbledo

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.bubbledo.ui.screens.HomeScreen
import com.example.bubbledo.ui.theme.BubbleDoTheme
import com.example.bubbledo.viewmodel.TaskViewModel
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.Scope
import android.util.Log
import com.example.bubbledo.notifications.NotificationHelper

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("MainActivity", "Google SignIn result received, launching handler")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        } else {
            Log.d("MainActivity", "Google Sign-In failed or cancelled, resultCode: ${result.resultCode}")
            viewModel.setSyncEnabled(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(applicationContext)

        viewModel = TaskViewModel(application)

        val driveAppDataScope = Scope("https://www.googleapis.com/auth/drive.appdata")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(driveAppDataScope)
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            BubbleDoTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    HomeScreen(viewModel = viewModel, onRequestGoogleSignIn = { startGoogleSignIn() })
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        Log.d("MainActivity", "handleSignInResult started")
        try {
            val account = completedTask.getResult(Exception::class.java)
            Log.d("MainActivity", "GoogleSignInAccount obtained: $account")
            if (account != null) {
                viewModel.onGoogleAccountSignedIn(account)
            } else {
                viewModel.setSyncEnabled(false)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Exception in handleSignInResult", e)
            viewModel.setSyncEnabled(false)
        }
    }
}
