#include <windows.h>
#include <jni.h>

JNIEXPORT void JNICALL
Java_ie_bops_hacktvgui_ConsoleCtrlJNI_sendCtrlC(JNIEnv* env, jclass cls, jlong pid)
{
    static LPTHREAD_START_ROUTINE ctrlRoutine = NULL;

    // Uses the technique employed by windows-kill to invoke the Windows
    // internal CtrlRoutine() in the target process.
    // https://github.com/ElyDotDev/windows-kill/blob/master/windows-kill-library/README.md
    if (ctrlRoutine == NULL) {
        HMODULE kernel32 = GetModuleHandleA("kernel32.dll");
        if (kernel32 == NULL) {
            return;
        }

        ctrlRoutine = (LPTHREAD_START_ROUTINE)GetProcAddress(kernel32, "CtrlRoutine");

        if (ctrlRoutine == NULL) {
            return;
        }
    }

    // Open process with the access required for CreateRemoteThread().
    HANDLE process = OpenProcess(
        PROCESS_CREATE_THREAD |
        PROCESS_QUERY_INFORMATION |
        PROCESS_VM_OPERATION |
        PROCESS_VM_WRITE |
        PROCESS_VM_READ,
        FALSE,
        (DWORD)pid
    );

    if (process == NULL) {
        return;
    }

    // Pass CTRL_C_EVENT as the argument to CtrlRoutine().
    HANDLE thread = CreateRemoteThread(
        process,
        NULL,
        0,
        ctrlRoutine,
        (LPVOID)CTRL_C_EVENT,
        0,
        NULL
    );

    if (thread != NULL) {
        WaitForSingleObject(thread, INFINITE);
        CloseHandle(thread);
    }

    CloseHandle(process);
}
