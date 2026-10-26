package com.yoyo.battery.dashboard.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {

    val Bolt: ImageVector by lazy {
        ImageVector.Builder(
            name = "Bolt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(11f, 21f)
                lineTo(12f, 13f)
                lineTo(7f, 13f)
                lineTo(13f, 3f)
                lineTo(12f, 11f)
                lineTo(17f, 11f)
                close()
            }
        }.build()
    }

    val ElectricBolt: ImageVector by lazy {
        ImageVector.Builder(
            name = "ElectricBolt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15f, 2f)
                lineTo(8.5f, 12f)
                lineTo(13f, 12f)
                lineTo(9f, 22f)
                lineTo(15.5f, 12f)
                lineTo(11f, 12f)
                close()
            }
        }.build()
    }

    val Power: ImageVector by lazy {
        ImageVector.Builder(
            name = "Power",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 9f)
                verticalLineTo(4f)
                horizontalLineTo(14f)
                verticalLineTo(9f)
                horizontalLineTo(10f)
                verticalLineTo(4f)
                horizontalLineTo(8f)
                verticalLineTo(9f)
                curveTo(6.9f, 9f, 6f, 9.9f, 6f, 11f)
                verticalLineTo(14.5f)
                lineTo(9.5f, 18f)
                verticalLineTo(20f)
                horizontalLineTo(14.5f)
                verticalLineTo(18f)
                lineTo(18f, 14.5f)
                verticalLineTo(11f)
                curveTo(18f, 9.9f, 17.1f, 9f, 16f, 9f)
                close()
            }
        }.build()
    }

    val Favorite: ImageVector by lazy {
        ImageVector.Builder(
            name = "Favorite",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 21.35f)
                lineTo(10.55f, 20.03f)
                curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                lineTo(12f, 21.35f)
                close()
            }
        }.build()
    }

    val Shield: ImageVector by lazy {
        ImageVector.Builder(
            name = "Shield",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 1f)
                lineTo(3f, 5f)
                verticalLineTo(11f)
                curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
                curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
                verticalLineTo(5f)
                lineTo(12f, 1f)
                close()
            }
        }.build()
    }

    val Speed: ImageVector by lazy {
        ImageVector.Builder(
            name = "Speed",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.38f, 8.57f)
                lineTo(22.55f, 7.12f)
                lineTo(21.13f, 5.7f)
                lineTo(18.96f, 7.15f)
                curveTo(17.06f, 5.81f, 14.7f, 5f, 12f, 5f)
                curveTo(5.93f, 5f, 1f, 9.93f, 1f, 16f)
                horizontalLineTo(3f)
                curveTo(3f, 11.03f, 7.03f, 7f, 12f, 7f)
                curveTo(14.28f, 7f, 16.32f, 7.82f, 17.9f, 9.18f)
                lineTo(15.22f, 11.86f)
                lineTo(16.64f, 13.28f)
                lineTo(20.38f, 8.57f)
                close()
                moveTo(12f, 13f)
                curveTo(10.9f, 13f, 10f, 13.9f, 10f, 15f)
                curveTo(10f, 16.1f, 10.9f, 17f, 12f, 17f)
                curveTo(13.1f, 17f, 14f, 16.1f, 14f, 15f)
                curveTo(14f, 13.9f, 13.1f, 13f, 12f, 13f)
                close()
            }
        }.build()
    }

    val Thermostat: ImageVector by lazy {
        ImageVector.Builder(
            name = "Thermostat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15f, 13f)
                verticalLineTo(5f)
                curveTo(15f, 3.34f, 13.66f, 2f, 12f, 2f)
                curveTo(10.34f, 2f, 9f, 3.34f, 9f, 5f)
                verticalLineTo(13f)
                curveTo(7.14f, 14.38f, 6.57f, 16.92f, 7.64f, 19.06f)
                curveTo(8.71f, 21.21f, 11.12f, 22.38f, 13.5f, 21.82f)
                curveTo(15.88f, 21.26f, 17.57f, 19.13f, 17.57f, 16.69f)
                curveTo(17.57f, 15.17f, 16.58f, 13.82f, 15f, 13f)
                close()
                moveTo(12f, 4f)
                curveTo(12.55f, 4f, 13f, 4.45f, 13f, 5f)
                verticalLineTo(8f)
                horizontalLineTo(11f)
                verticalLineTo(5f)
                curveTo(11f, 4.45f, 11.45f, 4f, 12f, 4f)
                close()
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.14f, 12.94f)
                curveTo(19.18f, 12.63f, 19.2f, 12.32f, 19.2f, 12f)
                curveTo(19.2f, 11.68f, 19.18f, 11.37f, 19.14f, 11.06f)
                lineTo(21.16f, 9.48f)
                curveTo(21.34f, 9.34f, 21.39f, 9.08f, 21.28f, 8.87f)
                lineTo(19.37f, 5.56f)
                curveTo(19.25f, 5.35f, 19f, 5.27f, 18.79f, 5.35f)
                lineTo(16.41f, 6.31f)
                curveTo(15.91f, 5.93f, 15.37f, 5.61f, 14.79f, 5.37f)
                lineTo(14.43f, 2.84f)
                curveTo(14.4f, 2.61f, 14.2f, 2.45f, 13.97f, 2.45f)
                horizontalLineTo(10.15f)
                curveTo(9.92f, 2.45f, 9.72f, 2.61f, 9.69f, 2.84f)
                lineTo(9.33f, 5.37f)
                curveTo(8.75f, 5.61f, 8.21f, 5.93f, 7.71f, 6.31f)
                lineTo(5.33f, 5.35f)
                curveTo(5.12f, 5.27f, 4.87f, 5.35f, 4.75f, 5.56f)
                lineTo(2.84f, 8.87f)
                curveTo(2.73f, 9.08f, 2.78f, 9.34f, 2.96f, 9.48f)
                lineTo(4.98f, 11.06f)
                curveTo(4.94f, 11.37f, 4.92f, 11.68f, 4.92f, 12f)
                curveTo(4.92f, 12.32f, 4.94f, 12.63f, 4.98f, 12.94f)
                lineTo(2.96f, 14.52f)
                curveTo(2.78f, 14.66f, 2.73f, 14.92f, 2.84f, 15.13f)
                lineTo(4.75f, 18.44f)
                curveTo(4.87f, 18.65f, 5.12f, 18.73f, 5.33f, 18.65f)
                lineTo(7.71f, 17.69f)
                curveTo(8.21f, 18.07f, 8.75f, 18.39f, 9.33f, 18.63f)
                lineTo(9.69f, 21.16f)
                curveTo(9.72f, 21.39f, 9.92f, 21.55f, 10.15f, 21.55f)
                horizontalLineTo(13.97f)
                curveTo(14.2f, 21.55f, 14.4f, 21.39f, 14.43f, 21.16f)
                lineTo(14.79f, 18.63f)
                curveTo(15.37f, 18.39f, 15.91f, 18.07f, 16.41f, 17.69f)
                lineTo(18.79f, 18.65f)
                curveTo(19f, 18.73f, 19.25f, 18.65f, 19.37f, 18.44f)
                lineTo(21.28f, 15.13f)
                curveTo(21.39f, 14.92f, 21.34f, 14.66f, 21.16f, 14.52f)
                lineTo(19.14f, 12.94f)
                close()
                moveTo(12f, 15.5f)
                curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f)
                curveTo(8.5f, 10.07f, 10.07f, 8.5f, 12f, 8.5f)
                curveTo(13.93f, 8.5f, 15.5f, 10.07f, 15.5f, 12f)
                curveTo(15.5f, 13.93f, 13.93f, 15.5f, 12f, 15.5f)
                close()
            }
        }.build()
    }

    val Bedtime: ImageVector by lazy {
        ImageVector.Builder(
            name = "Bedtime",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.3f, 2f)
                curveTo(6.78f, 2.06f, 2.3f, 6.55f, 2.35f, 12.07f)
                curveTo(2.4f, 17.58f, 6.96f, 22f, 12.47f, 21.95f)
                curveTo(15.93f, 21.92f, 18.99f, 20.12f, 20.73f, 17.38f)
                curveTo(13.43f, 18.06f, 8.01f, 11.75f, 9.68f, 4.67f)
                curveTo(10.51f, 3.49f, 11.39f, 2.58f, 12.3f, 2f)
                close()
            }
        }.build()
    }

    val Refresh: ImageVector by lazy {
        ImageVector.Builder(
            name = "Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.65f, 6.35f)
                curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f)
                curveTo(7.58f, 4f, 4.01f, 7.58f, 4.01f, 12f)
                curveTo(4.01f, 16.42f, 7.58f, 20f, 12f, 20f)
                curveTo(15.73f, 20f, 18.84f, 17.45f, 19.73f, 14f)
                horizontalLineTo(17.65f)
                curveTo(16.83f, 16.33f, 14.61f, 18f, 12f, 18f)
                curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
                curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
                curveTo(13.66f, 6f, 15.14f, 6.69f, 16.22f, 7.78f)
                lineTo(13f, 11f)
                horizontalLineTo(20f)
                verticalLineTo(4f)
                lineTo(17.65f, 6.35f)
                close()
            }
        }.build()
    }

    val PhoneAndroid: ImageVector by lazy {
        ImageVector.Builder(
            name = "PhoneAndroid",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 1f)
                horizontalLineTo(8f)
                curveTo(6.9f, 1f, 6f, 1.9f, 6f, 3f)
                verticalLineTo(21f)
                curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
                horizontalLineTo(16f)
                curveTo(17.1f, 23f, 18f, 22.1f, 18f, 21f)
                verticalLineTo(3f)
                curveTo(18f, 1.9f, 17.1f, 1f, 16f, 1f)
                close()
                moveTo(16f, 18f)
                horizontalLineTo(8f)
                verticalLineTo(4f)
                horizontalLineTo(16f)
                verticalLineTo(18f)
                close()
            }
        }.build()
    }

    val Cpu: ImageVector by lazy {
        ImageVector.Builder(
            name = "Cpu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9f, 2f)
                horizontalLineTo(7f)
                verticalLineTo(4f)
                horizontalLineTo(5f)
                curveTo(3.9f, 4f, 3f, 4.9f, 3f, 6f)
                verticalLineTo(8f)
                horizontalLineTo(1f)
                verticalLineTo(10f)
                horizontalLineTo(3f)
                verticalLineTo(14f)
                horizontalLineTo(1f)
                verticalLineTo(16f)
                horizontalLineTo(3f)
                verticalLineTo(18f)
                curveTo(3f, 19.1f, 3.9f, 20f, 5f, 20f)
                horizontalLineTo(7f)
                verticalLineTo(22f)
                horizontalLineTo(9f)
                verticalLineTo(20f)
                horizontalLineTo(13f)
                verticalLineTo(22f)
                horizontalLineTo(15f)
                verticalLineTo(20f)
                horizontalLineTo(17f)
                curveTo(18.1f, 20f, 19f, 19.1f, 19f, 18f)
                verticalLineTo(16f)
                horizontalLineTo(21f)
                verticalLineTo(14f)
                horizontalLineTo(19f)
                verticalLineTo(10f)
                horizontalLineTo(21f)
                verticalLineTo(8f)
                horizontalLineTo(19f)
                verticalLineTo(6f)
                curveTo(19f, 4.9f, 18.1f, 4f, 17f, 4f)
                horizontalLineTo(15f)
                verticalLineTo(2f)
                horizontalLineTo(13f)
                verticalLineTo(4f)
                horizontalLineTo(9f)
                verticalLineTo(2f)
                close()
                moveTo(17f, 18f)
                horizontalLineTo(5f)
                verticalLineTo(6f)
                horizontalLineTo(17f)
                verticalLineTo(18f)
                close()
                moveTo(7f, 8f)
                horizontalLineTo(15f)
                verticalLineTo(16f)
                horizontalLineTo(7f)
                verticalLineTo(8f)
                close()
            }
        }.build()
    }

    val Camera: ImageVector by lazy {
        ImageVector.Builder(
            name = "Camera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 8.8f)
                curveTo(10.23f, 8.8f, 8.8f, 10.23f, 8.8f, 12f)
                curveTo(8.8f, 13.77f, 10.23f, 15.2f, 12f, 15.2f)
                curveTo(13.77f, 15.2f, 15.2f, 13.77f, 15.2f, 12f)
                curveTo(15.2f, 10.23f, 13.77f, 8.8f, 12f, 8.8f)
                close()
                moveTo(9f, 2f)
                lineTo(7.17f, 4f)
                horizontalLineTo(4f)
                curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
                verticalLineTo(18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                horizontalLineTo(20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                verticalLineTo(6f)
                curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
                horizontalLineTo(16.83f)
                lineTo(15f, 2f)
                horizontalLineTo(9f)
                close()
                moveTo(12f, 17f)
                curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
                curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
                curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
                close()
            }
        }.build()
    }

    val Display: ImageVector by lazy {
        ImageVector.Builder(
            name = "Display",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 3f)
                horizontalLineTo(4f)
                curveTo(2.9f, 3f, 2f, 3.9f, 2f, 5f)
                verticalLineTo(15f)
                curveTo(2f, 16.1f, 2.9f, 17f, 4f, 17f)
                horizontalLineTo(10f)
                lineTo(8f, 21f)
                verticalLineTo(22f)
                horizontalLineTo(16f)
                verticalLineTo(21f)
                lineTo(14f, 17f)
                horizontalLineTo(20f)
                curveTo(21.1f, 17f, 22f, 16.1f, 22f, 15f)
                verticalLineTo(5f)
                curveTo(22f, 3.9f, 21.1f, 3f, 20f, 3f)
                close()
                moveTo(20f, 15f)
                horizontalLineTo(4f)
                verticalLineTo(5f)
                horizontalLineTo(20f)
                verticalLineTo(15f)
                close()
            }
        }.build()
    }

    val Memory: ImageVector by lazy {
        ImageVector.Builder(
            name = "Memory",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15f, 9f)
                horizontalLineTo(9f)
                verticalLineTo(15f)
                horizontalLineTo(15f)
                verticalLineTo(9f)
                close()
                moveTo(13f, 13f)
                horizontalLineTo(11f)
                verticalLineTo(11f)
                horizontalLineTo(13f)
                verticalLineTo(13f)
                close()
                moveTo(21f, 11f)
                verticalLineTo(9f)
                horizontalLineTo(19f)
                verticalLineTo(7f)
                curveTo(19f, 5.9f, 18.1f, 5f, 17f, 5f)
                horizontalLineTo(15f)
                verticalLineTo(3f)
                horizontalLineTo(13f)
                verticalLineTo(5f)
                horizontalLineTo(11f)
                verticalLineTo(3f)
                horizontalLineTo(9f)
                verticalLineTo(5f)
                horizontalLineTo(7f)
                curveTo(5.9f, 5f, 5f, 5.9f, 5f, 7f)
                verticalLineTo(9f)
                horizontalLineTo(3f)
                verticalLineTo(11f)
                horizontalLineTo(5f)
                verticalLineTo(13f)
                horizontalLineTo(3f)
                verticalLineTo(15f)
                horizontalLineTo(5f)
                verticalLineTo(17f)
                curveTo(5f, 18.1f, 5.9f, 19f, 7f, 19f)
                horizontalLineTo(9f)
                verticalLineTo(21f)
                horizontalLineTo(11f)
                verticalLineTo(19f)
                horizontalLineTo(13f)
                verticalLineTo(21f)
                horizontalLineTo(15f)
                verticalLineTo(19f)
                horizontalLineTo(17f)
                curveTo(18.1f, 19f, 19f, 18.1f, 19f, 17f)
                verticalLineTo(15f)
                horizontalLineTo(21f)
                verticalLineTo(13f)
                horizontalLineTo(19f)
                verticalLineTo(11f)
                horizontalLineTo(21f)
                close()
                moveTo(17f, 17f)
                horizontalLineTo(7f)
                verticalLineTo(7f)
                horizontalLineTo(17f)
                verticalLineTo(17f)
                close()
            }
        }.build()
    }

    val History: ImageVector by lazy {
        ImageVector.Builder(
            name = "History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13f, 3f)
                curveTo(8.03f, 3f, 4f, 7.03f, 4f, 12f)
                horizontalLineTo(1f)
                lineTo(4.89f, 15.89f)
                lineTo(4.96f, 16.03f)
                lineTo(9f, 12f)
                horizontalLineTo(6f)
                curveTo(6f, 8.13f, 9.13f, 5f, 13f, 5f)
                curveTo(16.87f, 5f, 20f, 8.13f, 20f, 12f)
                curveTo(20f, 15.87f, 16.87f, 19f, 13f, 19f)
                curveTo(11.07f, 19f, 9.32f, 18.21f, 8.06f, 16.94f)
                lineTo(6.64f, 18.36f)
                curveTo(8.27f, 19.99f, 10.51f, 21f, 13f, 21f)
                curveTo(17.97f, 21f, 22f, 16.97f, 22f, 12f)
                curveTo(22f, 7.03f, 17.97f, 3f, 13f, 3f)
                close()
                moveTo(12f, 8f)
                verticalLineTo(13f)
                lineTo(16.28f, 15.54f)
                lineTo(17f, 14.33f)
                lineTo(13.5f, 12.25f)
                verticalLineTo(8f)
                horizontalLineTo(12f)
                close()
            }
        }.build()
    }

    val Analytics: ImageVector by lazy {
        ImageVector.Builder(
            name = "Analytics",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 3f)
                horizontalLineTo(5f)
                curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(5f)
                curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
                close()
                moveTo(9f, 17f)
                horizontalLineTo(7f)
                verticalLineTo(10f)
                horizontalLineTo(9f)
                verticalLineTo(17f)
                close()
                moveTo(13f, 17f)
                horizontalLineTo(11f)
                verticalLineTo(7f)
                horizontalLineTo(13f)
                verticalLineTo(17f)
                close()
                moveTo(17f, 17f)
                horizontalLineTo(15f)
                verticalLineTo(13f)
                horizontalLineTo(17f)
                verticalLineTo(17f)
                close()
            }
        }.build()
    }

    val Info: ImageVector by lazy {
        ImageVector.Builder(
            name = "Info",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(13f, 17f)
                horizontalLineTo(11f)
                verticalLineTo(11f)
                horizontalLineTo(13f)
                verticalLineTo(17f)
                close()
                moveTo(13f, 9f)
                horizontalLineTo(11f)
                verticalLineTo(7f)
                horizontalLineTo(13f)
                verticalLineTo(9f)
                close()
            }
        }.build()
    }

    val BatteryFull: ImageVector by lazy {
        ImageVector.Builder(
            name = "BatteryFull",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.67f, 4f)
                horizontalLineTo(14f)
                verticalLineTo(2f)
                horizontalLineTo(10f)
                verticalLineTo(4f)
                horizontalLineTo(8.33f)
                curveTo(7.6f, 4f, 7f, 4.6f, 7f, 5.33f)
                verticalLineTo(20.67f)
                curveTo(7f, 21.4f, 7.6f, 22f, 8.33f, 22f)
                horizontalLineTo(15.67f)
                curveTo(16.4f, 22f, 17f, 21.4f, 17f, 20.67f)
                verticalLineTo(5.33f)
                curveTo(17f, 4.6f, 16.4f, 4f, 15.67f, 4f)
                close()
            }
        }.build()
    }

    val GitHub: ImageVector by lazy {
        ImageVector.Builder(
            name = "GitHub",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.477f, 2f, 2f, 6.484f, 2f, 12.017f)
                curveTo(2f, 16.446f, 4.87f, 20.2f, 8.84f, 21.527f)
                curveTo(9.34f, 21.62f, 9.52f, 21.31f, 9.52f, 21.045f)
                curveTo(9.52f, 20.81f, 9.51f, 20.02f, 9.51f, 19.21f)
                curveTo(6.73f, 19.82f, 6.14f, 18.06f, 6.14f, 18.06f)
                curveTo(5.68f, 16.9f, 5.03f, 16.59f, 5.03f, 16.59f)
                curveTo(4.12f, 15.97f, 5.1f, 15.98f, 5.1f, 15.98f)
                curveTo(6.1f, 16.05f, 6.63f, 17.01f, 6.63f, 17.01f)
                curveTo(7.52f, 18.54f, 8.97f, 18.1f, 9.54f, 17.84f)
                curveTo(9.63f, 17.19f, 9.89f, 16.74f, 10.17f, 16.49f)
                curveTo(7.95f, 16.24f, 5.62f, 15.38f, 5.62f, 11.55f)
                curveTo(5.62f, 10.46f, 6.01f, 9.57f, 6.65f, 8.87f)
                curveTo(6.55f, 8.62f, 6.2f, 7.6f, 6.75f, 6.23f)
                curveTo(6.75f, 6.23f, 7.59f, 5.96f, 9.5f, 7.25f)
                curveTo(10.3f, 7.03f, 11.15f, 6.92f, 12f, 6.92f)
                curveTo(12.85f, 6.92f, 13.7f, 7.03f, 14.5f, 7.25f)
                curveTo(16.41f, 5.96f, 17.25f, 6.23f, 17.25f, 6.23f)
                curveTo(17.8f, 7.6f, 17.45f, 8.62f, 17.35f, 8.87f)
                curveTo(17.99f, 9.57f, 18.38f, 10.46f, 18.38f, 11.55f)
                curveTo(18.38f, 15.39f, 16.04f, 16.24f, 13.81f, 16.49f)
                curveTo(14.17f, 16.8f, 14.49f, 17.41f, 14.49f, 18.35f)
                curveTo(14.49f, 19.7f, 14.48f, 20.78f, 14.48f, 21.05f)
                curveTo(14.48f, 21.31f, 14.66f, 21.63f, 15.17f, 21.53f)
                curveTo(19.14f, 20.2f, 22f, 16.45f, 22f, 12.02f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
            }
        }.build()
    }

    val Telegram: ImageVector by lazy {
        ImageVector.Builder(
            name = "Telegram",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(17.64f, 8.59f)
                lineTo(15.71f, 17.68f)
                curveTo(15.57f, 18.32f, 15.19f, 18.47f, 14.65f, 18.17f)
                lineTo(11.71f, 16f)
                lineTo(10.29f, 17.36f)
                curveTo(10.13f, 17.52f, 10f, 17.65f, 9.69f, 17.65f)
                lineTo(9.9f, 14.67f)
                lineTo(15.33f, 9.76f)
                curveTo(15.57f, 9.55f, 15.28f, 9.43f, 14.96f, 9.64f)
                lineTo(8.24f, 13.87f)
                lineTo(5.35f, 12.97f)
                curveTo(4.72f, 12.77f, 4.71f, 12.34f, 5.48f, 12.04f)
                lineTo(16.77f, 7.69f)
                curveTo(17.29f, 7.5f, 17.75f, 7.82f, 17.64f, 8.59f)
                close()
            }
        }.build()
    }

    val FDroid: ImageVector by lazy {
        ImageVector.Builder(
            name = "FDroid",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.5f, 4.5f)
                lineTo(4.5f, 2.5f)
                lineTo(3.0f, 4.0f)
                lineTo(5.1f, 6.1f)
                curveTo(3.8f, 7.5f, 3.0f, 9.4f, 3.0f, 11.5f)
                horizontalLineTo(21.0f)
                curveTo(21.0f, 9.4f, 20.2f, 7.5f, 18.9f, 6.1f)
                lineTo(21.0f, 4.0f)
                lineTo(19.5f, 2.5f)
                lineTo(17.5f, 4.5f)
                curveTo(15.9f, 3.5f, 14.0f, 3.0f, 12.0f, 3.0f)
                curveTo(10.0f, 3.0f, 8.1f, 3.5f, 6.5f, 4.5f)
                close()
                moveTo(7.5f, 8.5f)
                curveTo(8.05f, 8.5f, 8.5f, 8.05f, 8.5f, 7.5f)
                curveTo(8.5f, 6.95f, 8.05f, 6.5f, 7.5f, 6.5f)
                curveTo(6.95f, 6.5f, 6.5f, 6.95f, 6.5f, 7.5f)
                curveTo(6.5f, 8.05f, 6.95f, 8.5f, 7.5f, 8.5f)
                close()
                moveTo(16.5f, 8.5f)
                curveTo(17.05f, 8.5f, 17.5f, 8.05f, 17.5f, 7.5f)
                curveTo(17.5f, 6.95f, 17.05f, 6.5f, 16.5f, 6.5f)
                curveTo(15.95f, 6.5f, 15.5f, 6.95f, 15.5f, 7.5f)
                curveTo(15.5f, 8.05f, 15.95f, 8.5f, 16.5f, 8.5f)
                close()
                moveTo(3.0f, 13.0f)
                verticalLineTo(19.0f)
                curveTo(3.0f, 20.1f, 3.9f, 21.0f, 5.0f, 21.0f)
                horizontalLineTo(19.0f)
                curveTo(20.1f, 21.0f, 21.0f, 20.1f, 21.0f, 19.0f)
                verticalLineTo(13.0f)
                horizontalLineTo(3.0f)
                close()
            }
        }.build()
    }

    val OpenInNew: ImageVector by lazy {
        ImageVector.Builder(
            name = "OpenInNew",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 19f)
                horizontalLineTo(5f)
                verticalLineTo(5f)
                horizontalLineTo(12f)
                verticalLineTo(3f)
                horizontalLineTo(5f)
                curveTo(3.89f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(12f)
                horizontalLineTo(19f)
                verticalLineTo(19f)
                close()
                moveTo(14f, 3f)
                verticalLineTo(5f)
                horizontalLineTo(17.59f)
                lineTo(7.76f, 14.83f)
                lineTo(9.17f, 16.24f)
                lineTo(19f, 6.41f)
                verticalLineTo(10f)
                horizontalLineTo(21f)
                verticalLineTo(3f)
                horizontalLineTo(14f)
                close()
            }
        }.build()
    }
}
