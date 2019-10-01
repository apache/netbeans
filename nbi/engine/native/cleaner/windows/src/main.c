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
#include <wchar.h>


/*
 * cleaner.exe
 * 
 * Deletes a list of files/folders.
 * 
 * The command line syntax is:
 * 
 *    arg1:   File name containing a list of files/folders to delete.
 * 
 * Requirements for arg1:
 *  - The arg1 file name MUST be fully qualified. (or be in the same directory
 *       as the cleaner.exe executable)
 *  - The file MUST use Windows line ending (CRLF)
 *  - The file MUST be encoded in UTF-16. The NBI Engine will produce
 *    this file in Java charset "UNICODE" which effectively means
 *    UCS-2 BigEndian BOM. Such file will work just fine.
 *  - Each line in the file is expected to contain a fully qualified file
 *    or folder name. The entry can be 
 *        - a local file/folder name, e.g. "C:\foo\bar", up to a maximum of 32767 
 *          chars and thus not subject to the original Windows limitation 
 *          of 260 chars for a path name.
 *        - a UNC, e.g. "\\servername\sharename\foo\bar", subject to a
 *          restriction of 260 chars.
 *  - The list MUST be ordered so that the files in a folder are listed before 
 *    the folder itself. (it is not possible to delete a non-empty folder)
 * 
 * Method of working:
 * 
 * 1. After launch the content of command line arg1 is read into memory 
 *    as one big string.
 * 2. The string is chopped into a list by separating at the LINE_SEPARATOR.
 * 3. Sleep for 2 seconds to allow the launching process (the JVM) to exit.
 * 4. Loop over the list of files/folder to delete. Each file-delete operation 
 *    is spawned into a thread of its own up to a maximum of 64 threads. 
 *    Therefore the delete operations happens in parallel rather than in sequence. 
 *    If 64 threads have been spawned then wait for a thread to exit before
 *    spawning a new one. (therefore never more than 64 threads)
 *    For each file delete operation do the following:
 *       - Check to see if the file exists (by getting its attributes)
 *       - If file: delete file, if directory: delete directory (these are two 
 *            different calls in the Win32 API).
 *       - Attempt to delete each file/dir up to 15 times sleeping for 200 ms 
 *            between each attempt.
 * 5. Wait for all file-delete threads to exit.
 * 6. Delete self, i.e. the "cleaner.exe" executable.
 * 7. End
 *
 * The arg1 file is not deleted. However it can be part of the list itself 
 * if need be.
 * 
 * Author:  Dmitry Lipin, 2007
 * 
 *
 *  
 * Changes after transition to Apache:
 * 
 *   14-SEP-2019 Lars Bruun-Hansen (lbruun@apache.org) :   
 *         Function comment headers added. 
 *         Main comment header added.
 *  
 */


// Retry functionality: 
//   SLEEP_DELAY : millis between each attempt at a file delete
//   MAX_ATTEMPTS : how many times to attempt to delete a file
const DWORD SLEEP_DELAY   = 200;
const DWORD MAX_ATTEMPTS   = 15;
const DWORD THREAD_FINISHED = 100;

// Number of milliseconds to sleep at launch of the application.
const DWORD INITIAL_DELAY = 2000; // 2 seconds seems to be enough to finish java process

const WCHAR * LINE_SEPARATOR = L"\r\n";
const WCHAR * UNC_PREFIX     = L"\\\\?\\"; // Prefix for extended-length path in Win32 API
const WCHAR * UNC_STD_PREFIX = L"\\\\";  // Prefix for UNC paths, for example: \\servername\share\foo\bar
const DWORD UNC_PREFIX_LENGTH = 4;

#ifdef _MSC_VER
#define ZERO(x,y) SecureZeroMemory((x),(y));
#else
#define ZERO(x,y) ZeroMemory((x),(y));
#endif


/*
 * Search for the first occurrence of wcs2 within wcs1.
 * Returns a pointer to the first occurrence if found.
 * If not found, NULL is returned.
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

/*
 * Gets the number of lines in the input. 
 * (lines are expected to be separated by LINE_SEPARATOR) 
 */
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

/*
 * Produces a string array, 'list', from 'str' by splitting the string
 * at each occurrence of LINE_SEPARATOR.
 * 
 * [IN] str:      the input
 * [OUT] list:    string array
 * [OUT] number:  number of elements in 'list' 
 *
 */
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

/*
 *  Read file into memory. 
 */
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
    BOOL canDelete = TRUE;
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

    // Implementation note:
    // GetFileAttributesExW() is used not only to get file attributes
    // but also as a way to check if the file/dir (still) exist.

    if(GetFileAttributesExW(file, GetFileExInfoStandard, &attrs)) {
      if (attrs.dwFileAttributes & FILE_ATTRIBUTE_READONLY) { // if read-only attrib is set
            if (SetFileAttributesW(file, FILE_ATTRIBUTE_NORMAL) == 0) { // remove read-only attrib
                // The read-only attrib could not be deleted. No point in continuing.
                canDelete = FALSE;
            }
        }
        if (canDelete) {
            if (attrs.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
                while ((!RemoveDirectoryW(file) || GetFileAttributesExW(file, GetFileExInfoStandard, &attrs)) &&
                        ((count++) < MAX_ATTEMPTS)) {
                    Sleep(SLEEP_DELAY);
                }
            } else {
                while ((!DeleteFileW(file) || GetFileAttributesExW(file, GetFileExInfoStandard, &attrs)) &&
                        ((count++) < MAX_ATTEMPTS)) {
                    Sleep(SLEEP_DELAY);
                }
            }
        }
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

/*
 * Deletes the the current executable. This is done by spawning a small
 * .bat file which does the job. The .bat file even deletes itself when 
 * finished.
 */
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

/*
 * Changes directory to the directory where the currently executing
 * executable is located.
 */ 
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
