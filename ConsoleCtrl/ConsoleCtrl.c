#include <windows.h>
#include <jni.h>

JNIEXPORT void JNICALL
Java_ie_bops_hacktvgui_ConsoleCtrlJNI_sendCtrlC(JNIEnv* env, jclass cls, jlong pid)
{
    // Detach from our current console
    FreeConsole();

    // Attach to the target process's console
    if (!AttachConsole((DWORD)pid))
        return;

    // Prevent this process from receiving the CTRL+C itself
    SetConsoleCtrlHandler(NULL, TRUE);

    // Send CTRL+C to all processes attached to the console
    GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);

    // Give Windows a moment to deliver the signal
    Sleep(100);

    // Restore normal CTRL+C handling
    SetConsoleCtrlHandler(NULL, FALSE);

    // Detach again
    FreeConsole();
}
