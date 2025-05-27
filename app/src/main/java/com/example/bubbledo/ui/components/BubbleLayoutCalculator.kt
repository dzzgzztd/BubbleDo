package com.example.bubbledo.ui.components

import androidx.compose.ui.geometry.Offset
import com.example.bubbledo.model.Task
import kotlin.math.*

object BubbleLayoutCalculator {
    private const val MIN_SPACING = 1f
    private const val BASE_RADIUS = 20f
    private const val RADIUS_MULTIPLIER = 8f
    private const val MAX_RADIUS_RATIO = 0.2f
    private const val CELL_SIZE = 100f

    private class GridIndex {
        private val grid = mutableMapOf<Pair<Int, Int>, MutableList<Pair<Offset, Float>>>()

        fun insert(position: Offset, radius: Float) {
            val coveredCells = coveredGridCells(position, radius)
            for (cell in coveredCells) {
                grid.getOrPut(cell) { mutableListOf() }.add(position to radius)
            }
        }

        fun nearby(position: Offset, radius: Float): List<Pair<Offset, Float>> {
            val cells = coveredGridCells(position, radius)
            val result = mutableListOf<Pair<Offset, Float>>()
            for (cell in cells) {
                grid[cell]?.let { result.addAll(it) }
            }
            return result
        }

        private fun coveredGridCells(pos: Offset, radius: Float): Set<Pair<Int, Int>> {
            val left = floor((pos.x - radius) / CELL_SIZE).toInt()
            val right = floor((pos.x + radius) / CELL_SIZE).toInt()
            val top = floor((pos.y - radius) / CELL_SIZE).toInt()
            val bottom = floor((pos.y + radius) / CELL_SIZE).toInt()

            val result = mutableSetOf<Pair<Int, Int>>()
            for (gx in left..right) {
                for (gy in top..bottom) {
                    result.add(gx to gy)
                }
            }
            return result
        }
    }

    fun calculateBubblePositions(
        tasks: List<Task>,
        screenWidth: Float,
    ): MutableMap<String, Pair<Offset, Float>> {
        val sortedTasks = tasks.sortedWith(
            compareByDescending<Task> { it.urgency }.thenByDescending { it.importance }
        )

        val positions = mutableMapOf<String, Pair<Offset, Float>>()
        val maxRadius = screenWidth * MAX_RADIUS_RATIO
        val gridIndex = GridIndex()

        var y = maxRadius
        var x = MIN_SPACING

        for (task in sortedTasks) {
            val radius = calculateRadius(task.importance, maxRadius)

            x += radius
            if (x + radius > screenWidth - MIN_SPACING) {
                y += 2 * maxRadius + MIN_SPACING
                x = radius + MIN_SPACING
            }

            var pos = Offset(x, y)
            pos = riseBubble(pos, radius, gridIndex)

            x += radius + MIN_SPACING

            if (!hasCollisions(pos, radius, gridIndex) &&
                isWithinBounds(pos, radius, screenWidth)
            ) {
                gridIndex.insert(pos, radius)
                positions[task.id] = pos to radius
            }
        }

        return positions
    }

    private fun riseBubble(
        startPos: Offset,
        radius: Float,
        gridIndex: GridIndex
    ): Offset {
        var y = startPos.y
        while (y - radius > MIN_SPACING) {
            val candidate = Offset(startPos.x, y - 1f)
            if (
                hasCollisions(candidate, radius, gridIndex) ||
                candidate.y - radius <= MIN_SPACING
            ) break
            y -= 1f
        }
        return Offset(startPos.x, y + MIN_SPACING)
    }

    private fun hasCollisions(
        position: Offset,
        radius: Float,
        gridIndex: GridIndex
    ): Boolean {
        return gridIndex.nearby(position, radius).any { (otherPos, otherRadius) ->
            val dx = position.x - otherPos.x
            val dy = position.y - otherPos.y
            val distanceSq = dx * dx + dy * dy
            val minDist = radius + otherRadius + MIN_SPACING * 0.5f
            distanceSq < minDist * minDist
        }
    }

    private fun isWithinBounds(
        position: Offset,
        radius: Float,
        screenWidth: Float,
    ): Boolean {
        return position.x - radius >= MIN_SPACING &&
                position.y - radius >= MIN_SPACING &&
                position.x + radius <= screenWidth - MIN_SPACING
    }

    private fun calculateRadius(importance: Int, maxRadius: Float): Float {
        return (BASE_RADIUS + importance * RADIUS_MULTIPLIER).coerceAtMost(maxRadius)
    }
}