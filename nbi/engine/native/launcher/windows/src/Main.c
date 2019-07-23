/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <commctrl.h>
#include "Launcher.h"
#include "FileUtils.h"
#include "StringUtils.h"
#include "SystemUtils.h"
#include "ExtractUtils.h"
#include "Main.h"

#define HAVE_WCHAR_H 1

char mainClassName[80] = "Main NBI Window Class";
char mainTitle [80] = "NBI Launcher";
char showMessageTitle [] = "\n<ShowMessage>\n";
HWND hwndPB = NULL;
HWND hwndMain = NULL;
HWND hwndErrorDetail = NULL;
HWND hwndErrorTitle = NULL;
HWND hwndButton = NULL;
HWND hwndProgressTitle = NULL;

HINSTANCE globalInstance = NULL;
int64t * totalProgressSize = NULL;
int64t * currentProgressSize = NULL;
long steps = 1000;
long lastCheckedStep = 0;
int iCmdShowGlobal = 0;

HANDLE initializationSuccess = NULL;
HANDLE initializationFailed = NULL;
HANDLE closingWindowsRequired = NULL;
HANDLE closingWindowsConfirmed = NULL;
HANDLE buttonPressed = NULL;

#define BTN_EXIT 254

LRESULT CALLBACK WndProc(HWND hwnd, UINT umsg, WPARAM wParam, LPARAM lParam) {
    switch (umsg) {
        case WM_CLOSE:
            SetEvent(closingWindowsRequired);
            DestroyWindow(hwndPB);
            DestroyWindow(hwndProgressTitle);
            DestroyWindow(hwndErrorDetail);
            DestroyWindow(hwndErrorTitle);
            DestroyWindow(hwndButton);
            DestroyWindow(hwnd);
            return 0;
            
        case WM_DESTROY:
            UnregisterClass(mainClassName, globalInstance);
            PostQuitMessage(0);
            return 0;
        case WM_COMMAND:
            if(LOWORD(wParam)==BTN_EXIT) {
                SetEvent(buttonPressed);
                return 0;
            }
    }
    return DefWindowProc(hwnd, umsg, wParam, lParam);
}

void initMainWindow(LauncherProperties * props, HINSTANCE hInstance) {
    if(!isSilent(props)) {
        int systemWidth = GetSystemMetrics(SM_CXSCREEN);
        int systemHeight = GetSystemMetrics(SM_CYSCREEN);
        
        int w = 460;
        int h = 200;
        int x = (systemWidth - w)/2;
        int y = (systemHeight - h)/2;

        InitCommonControls();

        hwndMain = CreateWindow(mainClassName, mainTitle,
                //WS_OVERLAPPED | WS_EX_TOOLWINDOW,
                WS_CLIPSIBLINGS | WS_CLIPCHILDREN | WS_BORDER | WS_DLGFRAME | WS_SYSMENU | WS_MINIMIZEBOX /* | WS_THICKFRAME | WS_MAXIMIZEBOX*/
                ,
                x, y, w, h, NULL, NULL, hInstance, NULL);        
    }
}

void initErrorTitleWindow(LauncherProperties *props, HINSTANCE hInstance) {
    if(!isSilent(props)) {
        RECT rcClient;
        int cyVScroll;
        cyVScroll = GetSystemMetrics(SM_CYVSCROLL);
        GetClientRect(hwndMain, &rcClient);
        hwndErrorTitle = CreateWindowExW(0,  WC_STATICW,  WC_STATICW, WS_CHILD,
                rcClient.left + 10,  15, rcClient.right - 20, 20, hwndMain, NULL, hInstance, NULL);
        if (hwndErrorTitle)  {
            HFONT hFont = (HFONT) GetStockObject(DEFAULT_GUI_FONT);
            LOGFONT lfTitle;
            HFONT titleFont;
            
            GetObject(hFont, sizeof(lfTitle), &lfTitle);
            lfTitle.lfWeight = FW_ULTRABOLD;//OLD;
            lfTitle.lfHeight = lfTitle.lfHeight  + 2 ;
            lfTitle.lfWidth  = 0;
            titleFont = CreateFontIndirect(&lfTitle);
            SendMessage(hwndErrorTitle, WM_SETFONT, (WPARAM) titleFont , FALSE);
            //DeleteObject(titleFont );
            setErrorTitleString(props, NULL);
        }
    }
}

void initErrorDetailWindow(LauncherProperties *props, HINSTANCE hInstance) {
    if(!isSilent(props)) {
        RECT rcClient;
        int cyVScroll;
        cyVScroll = GetSystemMetrics(SM_CYVSCROLL);
        GetClientRect(hwndMain, &rcClient);
        hwndErrorDetail = CreateWindowExW(0,  WC_STATICW,  WC_STATICW, WS_CHILD  ,
                rcClient.left + 10,  40, rcClient.right - 20, 80,
                hwndMain, NULL, hInstance, NULL);
        if (hwndErrorDetail)  {
            LOGFONT lfDetail;
            HFONT detailFont;
            HFONT hFont = (HFONT) GetStockObject(DEFAULT_GUI_FONT);
            GetObject(hFont, sizeof(lfDetail), &lfDetail);
            lfDetail.lfHeight = lfDetail.lfHeight + 2;
            lfDetail.lfWidth  = 0;
            detailFont = CreateFontIndirect(&lfDetail);
            SendMessage(hwndErrorDetail, WM_SETFONT, (WPARAM) detailFont, FALSE);
            //DeleteObject(detailFont);
            setErrorDetailString(props, NULL);
        }
    }
}

void initProgressTitleWindow(LauncherProperties *props, HINSTANCE hInstance) {
    if(!isSilent(props)) {
        RECT rcClient;
        int cyVScroll;
        int height = 20;
        cyVScroll = GetSystemMetrics(SM_CYVSCROLL);
        GetClientRect(hwndMain, &rcClient);
        hwndProgressTitle = CreateWindowExW(0,  WC_STATICW,  WC_STATICW, WS_CHILD | WS_VISIBLE ,
                rcClient.left + 10, (rcClient.bottom - cyVScroll)/2 - height, rcClient.right - 20, height,
                hwndMain, NULL, hInstance, NULL);
        if (hwndProgressTitle)  {
            LOGFONT lfTitle;
            HFONT progressTitleFont;
            HFONT hFont = (HFONT) GetStockObject(DEFAULT_GUI_FONT);
            GetObject(hFont, sizeof(lfTitle), &lfTitle);
            lfTitle.lfHeight = lfTitle.lfHeight + 2;
            lfTitle.lfWidth  = 0;
            progressTitleFont = CreateFontIndirect(&lfTitle);
            SendMessage(hwndProgressTitle, WM_SETFONT, (WPARAM) progressTitleFont, FALSE);
            //DeleteObject(detailFont);
            setProgressTitleString(props, NULL);
        }
    }
}

void initProgressWindow(LauncherProperties * props, HINSTANCE hInstance) {
    if(!isSilent(props)) {
        RECT rcClient;
        int cyVScroll;
        cyVScroll = GetSystemMetrics(SM_CYVSCROLL);
        GetClientRect(hwndMain, &rcClient);
        hwndPB = CreateWindowExW(0, PROGRESS_CLASSW, NULL, WS_CHILD | WS_VISIBLE | PBS_SMOOTH,
                rcClient.left + 10,  (rcClient.bottom - cyVScroll)/2 , rcClient.right - 20, cyVScroll,
                hwndMain, NULL, hInstance, NULL);
        totalProgressSize->Low = 100;
        totalProgressSize->High = 0;
    }
}

void initExitButton(LauncherProperties * props, HINSTANCE hInstance) {
    if(!isSilent(props)) {
        RECT rcClient;
        //int cyVScroll    = GetSystemMetrics(SM_CYVSCROLL);
        int buttonWidth  = 180;
        int buttonHeight = 25;
        
        GetClientRect(hwndMain, &rcClient);
        hwndButton = CreateWindowExW(0, WC_BUTTONW, NULL,
                WS_CHILD  | BS_DEFPUSHBUTTON | WS_TABSTOP | BS_PUSHBUTTON  ,
                rcClient.right - 20 - buttonWidth, rcClient.bottom - 10 - buttonHeight, buttonWidth, buttonHeight,
                hwndMain, (HMENU)BTN_EXIT, hInstance, 0);
        if (hwndButton)  {
            LOGFONT lfButton;
            HFONT buttonFont;
            HFONT hFont = (HFONT) GetStockObject(DEFAULT_GUI_FONT);
            GetObject(hFont, sizeof(lfButton), &lfButton);
            lfButton.lfHeight = lfButton.lfHeight + 2;
            lfButton.lfWidth  = 0;
            buttonFont = CreateFontIndirect(&lfButton);
            SendMessage(hwndButton, WM_SETFONT, (WPARAM) buttonFont, FALSE);
            SetFocus(hwndButton);
            //DeleteObject(detailFont);
            setButtonString(props, NULL);
            UpdateWindow(hwndButton);
        }
    }
}

void showErrorW(LauncherProperties * props, const char * error, const DWORD varArgsNumber, ...) {
    
    WCHAR * errorTitle = NULL;
    WCHAR * errorMessage = NULL;
    DWORD  totalLength = 0;
    DWORD counter=0;
    WCHAR * result = NULL;
    va_list ap;
    
    getI18nPropertyTitleDetail(props, error, & errorTitle, &errorMessage);
    totalLength=getLengthW(errorMessage);
    
    va_start(ap, varArgsNumber);
    
    while((counter++)<varArgsNumber) {
        WCHAR * arg = va_arg( ap, WCHAR * );
        totalLength+=getLengthW(arg);
    }
    va_end(ap);
    result = newpWCHAR(totalLength + 1);
    va_start(ap, varArgsNumber);
    if(errorMessage!=NULL) {
        wvsprintfW(result, errorMessage, ap);
    }
    va_end(ap);
    
    if(!isSilent(props)) {
        HANDLE * events = (HANDLE *) LocalAlloc(LPTR, sizeof(HANDLE)*2);
        
        hide(props, hwndProgressTitle);
        hide(props, hwndPB);
        setErrorDetailString(props, result);
        setErrorTitleString(props, errorTitle);
        setButtonString(props, getI18nProperty(props, EXIT_BUTTON_PROP));
        show(props, hwndErrorDetail);
        show(props, hwndErrorTitle);
        show(props, hwndButton);
        
        events[0] = buttonPressed;
        events[1] = closingWindowsRequired;
        WaitForMultipleObjects(2, events, FALSE, INFINITE);
        FREE(events);
    }
    FREE(result);
    FREE(errorTitle);
    FREE(errorMessage);
}

BOOL InitInstance(LauncherProperties * props, HINSTANCE hInstance, int iCmdShow, HWND * MainWindowHandle) {
    if(isSilent(props)) return TRUE;
    iCmdShowGlobal = iCmdShow;
    
    initMainWindow(props, hInstance);
    if(!hwndMain) return FALSE;
    
    initErrorTitleWindow(props, hInstance);
    if(!hwndErrorTitle) return FALSE;
    
    initErrorDetailWindow(props, hInstance);
    if(!hwndErrorDetail) return FALSE;
    
    initProgressWindow(props, hInstance);
    if (! hwndPB)  return FALSE;
    
    initProgressTitleWindow(props, hInstance);
    if (! hwndPB)  return FALSE;
    
    initExitButton(props, hInstance);
    if (! hwndButton)  return FALSE;
    
    UpdateWindow(hwndMain);
    * MainWindowHandle = hwndMain;
    return TRUE;
}

void messageLoop(LauncherProperties * props){
    if(!isSilent(props)) {
        MSG message;
        while(GetMessage(&message, NULL, 0, 0) > 0) {
            if(!IsDialogMessage(hwndMain, & message)) {
                TranslateMessage(&message);
                DispatchMessage(&message);
            }
        }
    }
}

BOOL InitApplication(LauncherProperties * props, HINSTANCE hInstance) {
    if(isSilent(props)) {
        return TRUE;
    }
    else {
        
        WNDCLASSEX wndclass;
        wndclass.style = CS_HREDRAW | CS_VREDRAW;
        wndclass.lpfnWndProc = (WNDPROC)WndProc;
        wndclass.cbSize     = sizeof(WNDCLASSEX);
        wndclass.cbClsExtra = 0;
        wndclass.cbWndExtra = 0;
        wndclass.hIcon = LoadIcon(hInstance, MAKEINTRESOURCE(100));
        wndclass.hIconSm = (HICON)LoadImage(GetModuleHandle(NULL),
                MAKEINTRESOURCE(100),
                IMAGE_ICON, 16, 16, LR_DEFAULTCOLOR);
        wndclass.hInstance = hInstance;
        wndclass.hCursor = LoadCursor( 0, IDC_ARROW );
        wndclass.hbrBackground = (HBRUSH)(COLOR_BTNFACE + 1);
        wndclass.lpszMenuName = NULL;
        wndclass.lpszClassName = mainClassName;
        return RegisterClassEx(&wndclass);
    }
}


DWORD isTerminated(LauncherProperties * props) {
    if(props->status == ERROR_USER_TERMINATED) {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... already terminated", 1);
        return 1;
    }
    if (WAIT_OBJECT_0 == WaitForSingleObject(closingWindowsRequired, 0)) {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... terminate signal!", 1);
        props->status = ERROR_USER_TERMINATED;
        return 1;
    }
    return 0;
}

void addProgressPosition(LauncherProperties * props, DWORD add) {
    if(isSilent(props)) return;
    if ( add > 0 ) {
        int64t *mult1 = NULL;
        int64t *mult2 = NULL;
        int64t *currMult = NULL;
        int comp1 = 0;
        int comp2 = 0;
        int goout = 0;

        plus(currentProgressSize, add);
        do {
            multiply(mult1 = newint64_t(totalProgressSize->Low, totalProgressSize->High), lastCheckedStep);
            multiply(mult2 = newint64_t(totalProgressSize->Low, totalProgressSize->High), lastCheckedStep + 1);
            multiply(currMult = newint64_t(currentProgressSize->Low, currentProgressSize->High), steps);
            comp1 = compareInt64t(currMult, mult1);

            if (comp1 == 0) {
                goout = 1;
            } else {
                comp2 = compareInt64t(currMult, mult2);
                if (comp2 == 0) {
                    lastCheckedStep++;
                    goout = 1;
                } else if (comp1 > 0 && comp2 < 0) {
                    goout = 1;
                } else {
                    lastCheckedStep++;
                }
            }
            FREE(mult1);
            FREE(mult2);
            FREE(currMult);
            if(lastCheckedStep >= steps) {
                lastCheckedStep = steps;
                goout = 1;
            }
        } while (!goout);
        SendMessage(hwndPB, PBM_SETPOS, lastCheckedStep, 0);
    }
}

void setProgressRange(LauncherProperties * props, int64t * range) {
    if(isSilent(props)) return;
    totalProgressSize->Low = range->Low;
    totalProgressSize->High = range->High;
    currentProgressSize->Low = 0;
    currentProgressSize->High = 0;
    lastCheckedStep = 0;
    SendMessage(hwndPB, PBM_SETRANGE, 0, MAKELPARAM(0, steps));
    SendMessage(hwndPB, PBM_SETSTEP, 1, 0);
}

void hide(LauncherProperties * props, HWND hwnd) {
    if(!isSilent(props) && hwndMain != NULL && hwnd!=NULL ) {
        ShowWindow(hwnd, SW_HIDE);
        UpdateWindow(hwnd);
    }
}
void show(LauncherProperties * props, HWND hwnd) {
    if(!isSilent(props) && hwndMain != NULL && hwnd!=NULL ) {
        ShowWindow(hwnd, iCmdShowGlobal);
        UpdateWindow(hwnd);
    }
}

void setProgressTitleString(LauncherProperties * props, const WCHAR * message) {
    if(isSilent(props)) return;
    SetWindowTextW(hwndProgressTitle, message);
    UpdateWindow(hwndProgressTitle);
    UpdateWindow(hwndMain);
}

void setErrorTitleString(LauncherProperties * props, const WCHAR * message) {
    if(isSilent(props)) return;
    SetWindowTextW(hwndErrorTitle, message);
    UpdateWindow(hwndErrorTitle);
    UpdateWindow(hwndMain);
}

void setErrorDetailString(LauncherProperties * props, const WCHAR * message) {
    if(isSilent(props)) return;
    SetWindowTextW(hwndErrorDetail, message);
    UpdateWindow(hwndErrorDetail);
    UpdateWindow(hwndMain);
}

void setButtonString(LauncherProperties * props, const WCHAR * message) {
    if(isSilent(props)) return;
    SetWindowTextW(hwndButton, message);
    UpdateWindow(hwndButton);
    UpdateWindow(hwndMain);
}

void setMainWindowTitle(LauncherProperties * props, const WCHAR * message) {
    if(isSilent(props)) return;
    SetWindowTextW(hwndMain, message);
    UpdateWindow(hwndMain);
}

void closeLauncherWindows(LauncherProperties * props) {
    if(isSilent(props)) return;
    SendMessage(hwndMain, WM_CLOSE, 0, 0);
}


void hideLauncherWindows(LauncherProperties * props) {
    if(isSilent(props)) return;
    if(hwndMain != NULL) {
        ShowWindow(hwndMain, HIDE_WINDOW);
        UpdateWindow(hwndMain);
    }
}


void showLauncherWindows(LauncherProperties * props) {
    if(isSilent(props)) return;
    ShowWindow(hwndMain, iCmdShowGlobal);
    SetForegroundWindow(hwndMain);
    UpdateWindow(hwndMain);
}

void showMessageW(LauncherProperties * props, const WCHAR* message, const DWORD varArgsNumber, ...) {
    DWORD totalLength = getLengthW(message);
    va_list ap;
    DWORD counter=0;
    WCHAR * result = NULL;
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, showMessageTitle, 0);
    
    va_start(ap, varArgsNumber);
    
    while((counter++)<varArgsNumber) {
        WCHAR * arg = va_arg( ap, WCHAR * );
        totalLength+=getLengthW(arg);
    }
    va_end(ap);
    
    result = newpWCHAR(totalLength + 1);
    va_start(ap, varArgsNumber);
    wvsprintfW(result, message, ap);
    va_end(ap);
    writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, result, 1);
    if(!isSilent(props)) {
        hideLauncherWindows(props);
        MessageBoxW(hwndMain, result, getI18nProperty(props, MSG_MESSAGEBOX_TITLE), MB_OK);
    }
}

/*
void showMessageA(LauncherProperties * props, const char* message, const DWORD varArgsNumber, ...) {
    DWORD totalLength = getLengthA(message);
    va_list ap;
    DWORD counter=0;
    char * result = NULL;
    
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, showMessageTitle, 0);
    va_start(ap, varArgsNumber);
    while((counter++)<varArgsNumber) {
        char * arg = va_arg( ap, char * );
        totalLength+=getLengthA(arg);
    }
    va_end(ap);
    
    result = newpChar(totalLength + 1);
    va_start(ap, varArgsNumber);
    vsprintf(result, message, ap);
    va_end(ap);
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, result, 1);
    
    if(!isSilent(props)) {
        char * prop = toChar(getI18nProperty(props, MSG_MESSAGEBOX_TITLE));
        MessageBoxA(hwndMain, result, prop, MB_OK);
        FREE(prop);
    }
    
}
*/
/*
 * WCHAR* GetStringFromStringTable( UINT uStringID ) {
 * WCHAR   *pwchMem, *pwchCur;
 * UINT      idRsrcBlk = uStringID / 16 + 1;
 * int       strIndex  = uStringID % 16;
 * HINSTANCE hModule = NULL;
 * HRSRC     hResource = NULL;
 * int i=0;
 * LANGID lang = LANGIDFROMLCID(GetUserDefaultLCID());
 *
 * hResource = FindResourceExW( GetModuleHandleW(NULL), (LPWSTR)RT_STRING,
 * MAKEINTRESOURCEW(idRsrcBlk), lang);
 *
 * if( hResource != NULL ) {
 * pwchMem = (WCHAR *)LoadResource( hModule, hResource );
 *
 * if( pwchMem != NULL ) {
 * pwchCur = pwchMem;
 * for(i = 0; i<16; i++ ) {
 * if( *pwchCur ) {
 * int cchString = *pwchCur;  // String size in characters.
 * pwchCur++;
 * if( i == strIndex ) {
 * WCHAR * tmp = appendStringNW(NULL, 0, pwchCur, cchString);
 * return tmp;
 * }
 * pwchCur += cchString;
 * }
 * else
 * pwchCur++;
 * }
 * }
 * }
 * return NULL;
 *
 * }*/




DWORD WINAPI launcherThread(void * ptr) {
    HANDLE * events = (HANDLE *) LocalAlloc(LPTR, sizeof(HANDLE)*2);
    DWORD result;
    events[0] = initializationSuccess;
    events[1] = initializationFailed;
    result = WaitForMultipleObjects(2, events, FALSE, INFINITE);
    
    FREE(events);
    
    if (result == WAIT_OBJECT_0) {
        LauncherProperties * props = (LauncherProperties*) ptr;
        processLauncher(props);
        SetEvent(closingWindowsConfirmed);
        closeLauncherWindows(props);
    }
    return 0;
}


void createLauncherThread(LauncherProperties *props) {
    DWORD threadId;
    if(CreateThread( NULL, 0, &launcherThread, (LPVOID) props, 0, &threadId )==NULL) {
        SetEvent(closingWindowsConfirmed);
    }
}

DWORD createGui(LauncherProperties* props, HINSTANCE hInstance, HINSTANCE hi, int nCmdShow) {
    if (!InitApplication(props, hInstance)) {
        SetEvent(initializationFailed);
        return 0;
    } else if (!InitInstance(props, hInstance, nCmdShow, & hwndMain)) {
        SetEvent(initializationFailed);
        return 0;
    } else {
        SetEvent(initializationSuccess);
    }
    return 1;
}


DWORD createEvents() {
    initializationSuccess = CreateEventW(NULL, TRUE, FALSE, NULL);
    if(initializationSuccess==NULL) {
        return 0;
    }
    initializationFailed = CreateEventW(NULL, TRUE, FALSE, NULL);
    if(initializationFailed==NULL) {
        return 0;
    }
    buttonPressed = CreateEventW(NULL, TRUE, FALSE, NULL);
    if(buttonPressed ==NULL) {
        return 0;
    }
    closingWindowsRequired = CreateEventW(NULL, TRUE, FALSE, NULL);
    if(closingWindowsRequired ==NULL) {
        return 0;
    }
    closingWindowsConfirmed = CreateEventW(NULL, TRUE, FALSE, NULL);
    if(closingWindowsConfirmed ==NULL) {
        return 0;
    }
    
    return 1;
}

int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hi, LPSTR lpCmdLine, int nCmdShow) {
    DWORD exitCode = 1;
    DWORD status = ERROR_OK;
    
    totalProgressSize = newint64_t(0,0);
    currentProgressSize = newint64_t(0,0);
    globalInstance = hInstance;
    UNREFERENCED_PARAMETER(lpCmdLine);
    initWow64();
    if(is9x()) {
        MessageBoxA(0, "Windows 9X platform is not supported", "Message", MB_OK);
        status = EXIT_CODE_SYSTEM_ERROR;
    } else {
        if(!createEvents()) {
            status = EXIT_CODE_EVENTS_INITIALIZATION_ERROR;
        } else {
            LauncherProperties * props = createLauncherProperties();
            createLauncherThread(props);
            if(!createGui(props, hInstance, hi, nCmdShow)) {
                status = EXIT_CODE_GUI_INITIALIZATION_ERROR;	        
            } else {	        
                messageLoop(props);
                WaitForSingleObject(closingWindowsConfirmed, INFINITE);
            }
            
            status = props->status;
            exitCode = props->exitCode;
            printStatus(props);
            freeLauncherProperties(&props);
        }
    }
    FREE(currentProgressSize);
    FREE(totalProgressSize);
    return (status==ERROR_OK) ? exitCode : status;
}

int WINAPI MyMain(void) {
    int exitCode;
    STARTUPINFO StartupInfo;
    StartupInfo.dwFlags = 0;
    GetStartupInfo( &StartupInfo );
    exitCode = WinMain(GetModuleHandle(NULL), NULL, NULL,  StartupInfo.dwFlags & STARTF_USESHOWWINDOW  ? StartupInfo.wShowWindow : SW_SHOWDEFAULT);
    ExitProcess(exitCode);
    return exitCode;
}
