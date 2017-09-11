/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

#include <windows.h>
#include <wchar.h>



const DWORD SLEEP_DELAY   = 200;
const DWORD MAX_ATTEPTS   = 15;
const DWORD THREAD_FINISHED = 100;
const DWORD INITIAL_DELAY = 2000; // 2 seconds is seems to be enough to finish java process
const WCHAR * LINE_SEPARATOR = L"\r\n";
const WCHAR * UNC_PREFIX     = L"\\\\?\\";
const WCHAR * UNC_STD_PREFIX = L"\\\\";
const DWORD UNC_PREFIX_LENGTH = 4;

#ifdef _MSC_VER
#define ZERO(x,y) SecureZeroMemory((x),(y));
#else
#define ZERO(x,y) ZeroMemory((x),(y));
#endif

/*
 * typedef UINT  (WINAPI * WAIT_PROC)(HANDLE, DWORD);
 * typedef BOOL  (WINAPI * CLOSE_PROC)(HANDLE);
 * typedef BOOL  (WINAPI * DELETE_PROC)(LPCWSTR);
 * typedef VOID  (WINAPI * EXIT_PROC)(DWORD);
 * typedef VOID  (WINAPI * SLEEP_PROC)(DWORD);
 *
 *
 * typedef struct {
 * WAIT_PROC	waitObject;
 * CLOSE_PROC	closeHandle;
 * DELETE_PROC	deleteFile;
 * EXIT_PROC	exitProcess;
 * SLEEP_PROC  sleep;
 *
 * HANDLE		hProcess;
 * WCHAR		szFileName[MAX_PATH];
 *
 * } INJECT;
 */
WCHAR * search( const WCHAR * wcs1, const WCHAR * wcs2) {
    WCHAR *cp = (WCHAR *) wcs1;
    WCHAR *s1, *s2;
    
    if ( !*wcs2) {
        return (WCHAR *)wcs1;
    }
    
    while (*cp) {
        s1 = cp;
        s2 = (WCHAR *) wcs2;
        
        while ( *s1 && *s2 && !(*s1-*s2) ) {
            s1++, s2++;
        }
        if (!*s2) {
            return(cp);
        }
        cp++;
    }
    return(NULL);
}

/*
 * DWORD WINAPI RemoteThread(INJECT *remote) {
 * DWORD count = 0 ;
 *
 * remote->waitObject(remote->hProcess, INFINITE);
 * remote->closeHandle(remote->hProcess);
 * while(!remote->deleteFile(remote->szFileName) && (count++) < MAX_ATTEPTS) {
 * remote->sleep(SLEEP_DELAY);
 * }
 * remote->exitProcess(0);
 * return 0;
 * }
 *
 * HANDLE GetRemoteProcess() {
 * STARTUPINFO si;
 *
 * PROCESS_INFORMATION pi;
 * ZERO( &si, sizeof(si) );
 * ZERO( &pi, sizeof(pi) );
 * si.cb = sizeof(si);
 * if(CreateProcess(0, "explorer.exe", 0, 0, FALSE, CREATE_SUSPENDED|CREATE_NO_WINDOW|IDLE_PRIORITY_CLASS, 0, 0, &si, &pi)) {
 * CloseHandle(pi.hThread);
 * return pi.hProcess;
 * }
 * else {
 * return 0;
 * }
 * }
 *
 * BOOL removeItself() {
 *
 * INJECT local, *remote;
 * BYTE   *code;
 * HMODULE hKernel32;
 * HANDLE  hRemoteProcess;
 * HANDLE  hCurProc;
 *
 * DWORD	dwThreadId;
 * HANDLE	hThread = 0;
 * DWORD sizeOfCode = 200;
 *
 * hRemoteProcess = GetRemoteProcess();
 *
 * if(hRemoteProcess == 0) {
 * return FALSE;
 * }
 *
 * code = VirtualAllocEx(hRemoteProcess, 0, sizeof(INJECT) + sizeOfCode, MEM_RESERVE|MEM_COMMIT, PAGE_EXECUTE_READWRITE);
 *
 * if(code == 0) {
 * CloseHandle(hRemoteProcess);
 * return FALSE;
 * }
 *
 * hKernel32 = GetModuleHandleW(L"kernel32.dll");
 * remote = (INJECT *)(code + sizeOfCode);
 *
 * local.waitObject      = (WAIT_PROC)  GetProcAddress(hKernel32, "WaitForSingleObject");
 * local.closeHandle	  = (CLOSE_PROC) GetProcAddress(hKernel32, "CloseHandle");
 * local.exitProcess	  = (EXIT_PROC)  GetProcAddress(hKernel32, "ExitProcess");
 * local.deleteFile      = (DELETE_PROC)GetProcAddress(hKernel32, "DeleteFileW");
 * local.sleep           = (SLEEP_PROC) GetProcAddress(hKernel32, "Sleep");
 *
 * // duplicate our own process handle for remote process to wait on
 * hCurProc = GetCurrentProcess();
 *
 * DuplicateHandle(hCurProc, hCurProc, hRemoteProcess, &local.hProcess, 0, FALSE, DUPLICATE_SAME_ACCESS);
 *
 * // find name of current executable
 *
 * GetModuleFileNameW(NULL, local.szFileName, MAX_PATH);
 *
 * // write in code to execute, and the remote structure
 * WriteProcessMemory(hRemoteProcess, (LPVOID) code,   RemoteThread, sizeOfCode, 0);
 * WriteProcessMemory(hRemoteProcess, (LPVOID) remote, &local, sizeof(local), 0);
 *
 * // execute the code in remote process
 * hThread = CreateRemoteThread(hRemoteProcess, 0, 0, (LPTHREAD_START_ROUTINE) code, remote, 0, &dwThreadId);
 *
 * if(hThread != 0) {
 * CloseHandle(hThread);
 * }
 * return TRUE;
 * }
 */

typedef struct _list {
    WCHAR * item;
    struct _list * next;
} LIST;


WCHAR * toWCHAR(char * charBuffer, DWORD size) {
    DWORD i=0;
    WCHAR * buffer;
    BOOL hasBOM        = (*charBuffer == '\xFF' && *(charBuffer+1) == '\xFE');
    BOOL hasReverseBOM = (*charBuffer == '\xFE' && *(charBuffer+1) == '\xFF');
    
    char * realStringPtr = charBuffer;
    if (hasBOM || hasReverseBOM) {
        size-= 2;
        realStringPtr+= 2;
        if(hasReverseBOM) {
            char c;
            for (i = 0 ; i < size/2 ; i++) {
                c = charBuffer [2 * i] ;
                charBuffer [2 * i] = charBuffer [2 * i + 1] ;
                charBuffer [2 * i + 1] = c;
            }
        }
    }
    
    buffer = (WCHAR*) LocalAlloc(LPTR, sizeof(WCHAR) * (size/2+1));
    ZERO(buffer, sizeof(WCHAR) * (size/2+1));
    for(i=0;i<size/2;i++) {
        realStringPtr[2*i] = (realStringPtr[2*i]) & 0xFF;
        realStringPtr[2*i+1] = (realStringPtr[2*i+1])& 0xFF;
        buffer [i] = ((unsigned char)realStringPtr[2*i]) + (((unsigned char)realStringPtr[2*i+1]) << 8);
    }
    
    return buffer;
}

DWORD getLinesNumber(WCHAR *str) {
    DWORD result = 0;
    WCHAR *ptr = str;
    WCHAR *ptr2 = str;
    DWORD sepLength = lstrlenW(LINE_SEPARATOR);
    if(ptr!=NULL) {
        while((ptr2 = search(ptr, LINE_SEPARATOR))!=NULL) {
            ptr = ptr2 + sepLength;
            result++;
            
            if(ptr==NULL)  break;
        }
        if(ptr!=NULL && lstrlenW(ptr) > 0) {
            result ++;
        }
    }
    return result;
}

void getLines(WCHAR *str, WCHAR *** list, DWORD * number) {
    WCHAR *ptr = str;
    WCHAR *ptr2 = NULL;
    DWORD length = 0;
    DWORD sepLength = lstrlenW(LINE_SEPARATOR);
    DWORD counter = 0;
    *number = getLinesNumber(str);
    *list = (WCHAR**) LocalAlloc(LPTR, sizeof(WCHAR*) * (*number));
    
    if(ptr!=NULL) {
        while(counter < (*number)) {
            DWORD i = 0 ;
            if((ptr2 = search(ptr, LINE_SEPARATOR))!=NULL) {
                ptr2 = search(ptr, LINE_SEPARATOR) + sepLength;
                length = lstrlenW(ptr) - lstrlenW(ptr2) - sepLength;
                (*list) [counter ] = (WCHAR*) LocalAlloc(LPTR, sizeof(WCHAR*)*(length+1));
                ZERO((*list) [counter ], sizeof(WCHAR*)*(length+1));
                for(i=0;i<length;i++) {
                    (*list) [counter ][i]=ptr[i];
                }
                ptr = ptr2;
            } else if((length = lstrlenW(ptr)) > 0) {
                (*list)[counter ] = (WCHAR*) LocalAlloc(LPTR, sizeof(WCHAR*)*(length+1));
                ZERO((*list) [counter ], sizeof(WCHAR*)*(length+1));
                for(i=0;i<length;i++) {
                    (*list) [counter ][i]=ptr[i];
                }
                ptr = NULL;
            }
            counter++;
            if(ptr==NULL)  break;
        }
    }
}


void readStringList(HANDLE fileHandle, WCHAR *** list, DWORD *number) {
    DWORD size = GetFileSize(fileHandle, NULL); // hope it much less than 2GB
    DWORD read = 0;
    char * charBuffer = (char*) LocalAlloc(LPTR, sizeof(char) * (size + 2));
    ZERO(charBuffer, sizeof(char) * (size + 2));
    
    if(ReadFile(fileHandle, charBuffer, size, &read, 0) && read >=2) {
        WCHAR * buffer = toWCHAR(charBuffer, size + 2);
        getLines(buffer, list, number);
        LocalFree(buffer);
    }
    LocalFree(charBuffer);
}

void deleteFile(WCHAR * filePath) {
    DWORD count = 0 ;
    WIN32_FILE_ATTRIBUTE_DATA attrs;
    DWORD filePathLength = lstrlenW(filePath);
    DWORD prefixLength = (filePath == search(filePath, UNC_STD_PREFIX)) ? 0 : UNC_PREFIX_LENGTH;
    DWORD length = filePathLength + prefixLength + 1;
    WCHAR * file = (WCHAR*) LocalAlloc(LPTR, sizeof(WCHAR) * length);
    DWORD i=0;
    for(i=0;i<prefixLength;i++) {
        file[i]=UNC_PREFIX[i];
    }
    for(i=0;i<filePathLength;i++) {
        file[i+prefixLength] = filePath[i];
    }

    if(GetFileAttributesExW(file, GetFileExInfoStandard, &attrs)) {
        if(attrs.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
            while((!RemoveDirectoryW(file) || GetFileAttributesExW(file, GetFileExInfoStandard, &attrs)) &&
                ((count++) < MAX_ATTEPTS))
                Sleep(SLEEP_DELAY);
        else
            while((!DeleteFileW(file) || GetFileAttributesExW(file, GetFileExInfoStandard, &attrs)) &&
                ((count++) < MAX_ATTEPTS))
                Sleep(SLEEP_DELAY);
    }
    LocalFree(file);
}

DWORD WINAPI deleteFileThread(void * ptr) {
    WCHAR * file = (WCHAR*) ptr;
    deleteFile(file);
    return THREAD_FINISHED;
}

void getFreeIndexForNextThread(HANDLE * list, DWORD max, DWORD * counter) {
    DWORD code = 0;
    DWORD maxReached = 0;
    
    while(1) {
        if((*counter)==max) {
            maxReached = 1;
            *counter = 0;
        }
        code = 0;
        if(list[*counter]==INVALID_HANDLE_VALUE) {
            break;
        } else if(GetExitCodeThread(list[*counter], &code)!=0 && code==THREAD_FINISHED) {
            break;
        } else {
            *counter = (*counter) + 1;
            if((*counter)==max && maxReached == 1) {
                *counter = WaitForMultipleObjects(max, list, FALSE, INFINITE) - WAIT_OBJECT_0;
            }
        }
    }
}

#define BUFSIZE 512
void removeItselfUsingCmd() {
    char * currentFile = LocalAlloc(LPTR, sizeof(char) * BUFSIZE);    
    if (GetModuleFileNameA(0, currentFile, MAX_PATH)) {
        char * tempFile = LocalAlloc(LPTR, sizeof(char) * BUFSIZE);    
        HANDLE hTempFile;
        int index = 0;
        int i = 0;
        char cleanerSuffix [] = ".bat";
        for( i = 0; i < (lstrlenA(currentFile) - lstrlenA(cleanerSuffix)); i++) {
            tempFile[index++] = currentFile[i];
        }
        for(i=0;i<lstrlenA(cleanerSuffix);i++) {
            tempFile[index++] = cleanerSuffix[i];
        }
        hTempFile = CreateFileA(tempFile, GENERIC_READ | GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);        
        if (hTempFile != INVALID_HANDLE_VALUE) {
            char * command = LocalAlloc(LPTR, sizeof(char) * (lstrlenA(tempFile) + lstrlenA(currentFile) + 6));
            DWORD bytesNumber = 0 ;
            STARTUPINFO si;
            PROCESS_INFORMATION pi;   
            
            char * strings [4] = {
                ":Repeat\n",
                "del %1\n",
                "if exist %1 goto Repeat\n",
                "del %0\n",                
            };
            for(i=0;i<4;i++) {
                WriteFile(hTempFile, strings[i], lstrlenA(strings[i]), &bytesNumber, NULL);
            }
            
            CloseHandle(hTempFile);
            
            ZERO( &si, sizeof(si) );
            si.cb = sizeof(si);
            ZERO( &pi, sizeof(pi) );
            index=0;
            command [index++]= '"';
            for(i=0;i<lstrlenA(tempFile);i++) {
                command [index++] = tempFile[i];
            }
            command[index++]= '"';
            command[index++]= ' ';
            command[index++]= '"';
            for(i=0;i<lstrlenA(currentFile);i++) {
                command [index++] = currentFile[i];
            }
            command[index++]= '"';
            command[index++]= 0;

            CreateProcess(0, command, 0, 0, FALSE, CREATE_NO_WINDOW | IDLE_PRIORITY_CLASS, 0, 0, &si, &pi);
            LocalFree(command);
            CloseHandle( pi.hProcess );
            CloseHandle( pi.hThread );            
        }
        LocalFree(tempFile);
    }
    LocalFree(currentFile);
    
}

void changeCurrentDirectory() {
    WCHAR * currentFile = LocalAlloc(LPTR, sizeof(WCHAR) * MAX_PATH);    
    if (GetModuleFileNameW(0, currentFile, MAX_PATH)) {
        WCHAR * ptr = currentFile;
        DWORD i=0;
        DWORD len=0;
        WCHAR * parent;
        while(search(ptr, L"\\")!=NULL) {
            ptr = search(ptr, L"\\") + 1;
        }
        len = lstrlenW(currentFile) - lstrlenW(ptr) - 1;
        parent = LocalAlloc(LPTR, sizeof(WCHAR) * (len + 1));
        for(i=0;i<len;i++) {
            parent[i] = currentFile[i];
        }
        parent[len] = 0;
        SetCurrentDirectoryW(parent);
        LocalFree(parent);
    }
    LocalFree(currentFile);
}

// should be less or equals to MAXIMUM_WAIT_OBJECTS
#define MAXIMUM_THREADS MAXIMUM_WAIT_OBJECTS

int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hi, PSTR pszCmdLine, int nCmdShow) {
//int main(void) {
    int argumentsNumber = 0;
    DWORD i=0;
    DWORD  threadCounter = 0;
    DWORD dwThread;
    WCHAR ** commandLine = CommandLineToArgvW(GetCommandLineW(), &argumentsNumber);
    HANDLE * runningThreads = (HANDLE *) LocalAlloc(LPTR, sizeof(HANDLE) * MAXIMUM_THREADS);
    
    for(i=0;i<MAXIMUM_THREADS;i++) {
        runningThreads[i] = INVALID_HANDLE_VALUE;
    }
    changeCurrentDirectory();
    
    if(argumentsNumber==2) {
        WCHAR * filename = commandLine[1];
        HANDLE fileList = CreateFileW(filename, GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, FILE_FLAG_DELETE_ON_CLOSE, 0);
        if(fileList!=0) {
            WCHAR ** files = NULL;
            DWORD number = 0;
            DWORD allThreadsUsed=0;
            readStringList(fileList, &files, &number);
            CloseHandle(fileList);
            
            if(files!=NULL) {
                Sleep(INITIAL_DELAY);
                for(i=0;i<number;i++) {
                    WCHAR * file = files[i];
                    if(file!=NULL) {
                        if(lstrlenW(file)>0)  {
                            getFreeIndexForNextThread(runningThreads, MAXIMUM_THREADS, &threadCounter);
                            runningThreads [threadCounter] = CreateThread( NULL, 0, &deleteFileThread, (LPVOID) file, 0, &dwThread );
                            threadCounter++;
                            if(threadCounter==MAXIMUM_THREADS) allThreadsUsed = 1;
                        }
                    }
                }
                
                WaitForMultipleObjects(allThreadsUsed ? MAXIMUM_THREADS : threadCounter,
                        runningThreads, TRUE, INFINITE);
                
                for(i=0;i<number;i++) {
                    if(files[i]!=NULL) LocalFree(files[i]);
                }
                
                LocalFree(files);
            }
        }
    }
    LocalFree(commandLine);
    LocalFree(runningThreads);
    //removeItself();
    removeItselfUsingCmd();
    return 0;
}
