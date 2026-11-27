package io.github.rachitp.swatter

import androidx.test.platform.app.InstrumentationRegistry

/** Runs a shell command as the shell user (like adb shell) and waits for it to finish. */
fun shell(command: String): String {
    val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
    return android.os.ParcelFileDescriptor.AutoCloseInputStream(pfd).bufferedReader().use { it.readText() }
}
