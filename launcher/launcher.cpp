#include <windows.h>
#include <shellapi.h>
#include <filesystem>
#include <string>
#include <vector>

namespace fs = std::filesystem;

constexpr wchar_t APP_NAME[] = L"hacktv-gui launcher";

static std::wstring GetWindowsErrorMessage(DWORD errorCode);
static std::wstring QuoteCommandLineArgument(const std::wstring& argument);

int WINAPI wWinMain(
    _In_ HINSTANCE,
    _In_opt_ HINSTANCE,
    _In_ PWSTR,
    _In_ int)
{
    wchar_t modulePath[MAX_PATH];

    DWORD length = GetModuleFileNameW(
        nullptr,
        modulePath,
        MAX_PATH
    );

    if (length == 0 || length == MAX_PATH)
    {
        MessageBoxW(
            nullptr,
            L"Unable to determine the application directory.",
            APP_NAME,
            MB_OK | MB_ICONERROR
        );

        return 1;
    }

    const fs::path appDir = fs::path(modulePath).parent_path();
    const fs::path binDir = appDir / L"bin";

    int argc = 0;

    LPWSTR* argv = CommandLineToArgvW(
        GetCommandLineW(),
        &argc
    );

    if (argv == nullptr)
    {
        const DWORD errorCode = GetLastError();

        const std::wstring message =
            L"Unable to process the application command line.\n\n" +
            GetWindowsErrorMessage(errorCode);

        MessageBoxW(
            nullptr,
            message.c_str(),
            APP_NAME,
            MB_OK | MB_ICONERROR
        );

        return 1;
    }

    std::wstring javaExeName = L"javaw.exe";
    int firstArgument = 1;

    if (argc > 1 && _wcsicmp(argv[1], L"/console") == 0)
    {
        javaExeName = L"java.exe";
        firstArgument = 2;
    }

    const fs::path javaExe =
        appDir / L"jre" / L"bin" / javaExeName;

    const fs::path jarFile =
        binDir / L"hacktv-gui.jar";

    if (!fs::is_regular_file(javaExe))
    {
        const std::wstring message =
            L"Unable to find the bundled Java Runtime Environment.\n\n"
            L"Expected to find:\n" +
            javaExe.wstring();

        MessageBoxW(
            nullptr,
            message.c_str(),
            APP_NAME,
            MB_OK | MB_ICONWARNING
        );

        LocalFree(argv);
        return 1;
    }

    if (!fs::is_regular_file(jarFile))
    {
        const std::wstring message =
            L"Unable to find the hacktv-gui application.\n\n"
            L"Expected to find:\n" +
            jarFile.wstring();

        MessageBoxW(
            nullptr,
            message.c_str(),
            APP_NAME,
            MB_OK | MB_ICONWARNING
        );

        LocalFree(argv);
        return 1;
    }

    std::wstring commandLine =
        L"\"" + javaExe.wstring() +
        L"\" --enable-native-access=ALL-UNNAMED"
        L" -jar \"" + jarFile.wstring() + L"\"";

    for (int i = firstArgument; i < argc; ++i)
    {
        commandLine += L" ";
        commandLine += QuoteCommandLineArgument(argv[i]);
    }

    LocalFree(argv);

    std::vector<wchar_t> commandLineBuffer(
        commandLine.begin(),
        commandLine.end()
    );

    commandLineBuffer.push_back(L'\0');

    STARTUPINFOW startupInfo{};
    startupInfo.cb = sizeof(startupInfo);

    PROCESS_INFORMATION processInfo{};

    if (!CreateProcessW(
        nullptr,
        commandLineBuffer.data(),
        nullptr,
        nullptr,
        FALSE,
        0,
        nullptr,
        binDir.c_str(),
        &startupInfo,
        &processInfo))
    {
        const DWORD errorCode = GetLastError();

        const std::wstring message =
            L"An error occurred while launching Java.\n\n" +
            GetWindowsErrorMessage(errorCode);

        MessageBoxW(
            nullptr,
            message.c_str(),
            APP_NAME,
            MB_OK | MB_ICONERROR
        );

        return 1;
    }

    CloseHandle(processInfo.hThread);
    CloseHandle(processInfo.hProcess);

    return 0;
}

static std::wstring GetWindowsErrorMessage(DWORD errorCode)
{
    LPWSTR buffer = nullptr;

    const DWORD length = FormatMessageW(
        FORMAT_MESSAGE_ALLOCATE_BUFFER |
        FORMAT_MESSAGE_FROM_SYSTEM |
        FORMAT_MESSAGE_IGNORE_INSERTS,
        nullptr,
        errorCode,
        0,
        reinterpret_cast<LPWSTR>(&buffer),
        0,
        nullptr
    );

    if (length == 0)
    {
        return L"Unknown Windows error (" +
            std::to_wstring(errorCode) +
            L").";
    }

    std::wstring message(buffer, length);

    LocalFree(buffer);

    while (!message.empty() &&
        (message.back() == L'\r' || message.back() == L'\n'))
    {
        message.pop_back();
    }

    return message;
}

static std::wstring QuoteCommandLineArgument(
    const std::wstring& argument)
{
    if (argument.find_first_of(L" \t\"") == std::wstring::npos)
    {
        return argument;
    }

    std::wstring result = L"\"";
    unsigned int backslashes = 0;

    for (wchar_t character : argument)
    {
        if (character == L'\\')
        {
            ++backslashes;
        }
        else if (character == L'"')
        {
            result.append(backslashes * 2 + 1, L'\\');
            result += L'"';
            backslashes = 0;
        }
        else
        {
            result.append(backslashes, L'\\');
            result += character;
            backslashes = 0;
        }
    }

    result.append(backslashes * 2, L'\\');
    result += L'"';

    return result;
}