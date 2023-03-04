/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#ifndef _FileUtils_H
#define	_FileUtils_H

#include <windows.h>
#include "Errors.h"
#include "Types.h"

#ifdef	__cplusplus
extern "C" {
#endif
    
#define OUTPUT_LEVEL_DEBUG 0
#define OUTPUT_LEVEL_NORMAL 1
    
    
    extern const WCHAR * FILE_SEP;
    extern const long CRC32_TABLE[256];
    void update_crc32(DWORD * crc32, char * buf, DWORD size);
    int64t * getFreeSpace(WCHAR *path);
    int64t * getFileSize(WCHAR * path);
    void checkFreeSpace(LauncherProperties * props, WCHAR * tmpDir, int64t * size);
    WCHAR * getParentDirectory(WCHAR * dir);
    void createDirectory(LauncherProperties * props, WCHAR * directory);
    void createTempDirectory(LauncherProperties * props, WCHAR * argTempDir, DWORD createRndSubDir);
    void deleteDirectory(LauncherProperties * props,WCHAR * dir);
    WCHAR * getExePath();
    WCHAR * getExeName();
    WCHAR * getExeDirectory();
    
    WCHAR * getSystemTemporaryDirectory();    
    DWORD isDirectory(WCHAR *path);
    WCHAR * getCurrentDirectory();
    WCHAR * getCurrentUserHome();
        
    
    void writeMessageW(LauncherProperties * props, DWORD level,DWORD isErr,  const WCHAR * message, DWORD needEndOfLine);
    void writeMessageA(LauncherProperties * props,DWORD level, DWORD isErr,  const char  * message, DWORD needEndOfLine);
    void writeErrorA(LauncherProperties * props,DWORD level,   DWORD isErr,  const char  * message, const WCHAR * param, DWORD errorCode);
    void writeDWORD(LauncherProperties * props,DWORD level,    DWORD isErr,  const char  * message, DWORD value, DWORD needEndOfLine);
    void writeint64t(LauncherProperties * props,DWORD level,   DWORD isErr,  const char  * message, int64t * value, DWORD needEndOfLine);
    
    void flushHandle(HANDLE hd);
    DWORD fileExists(WCHAR * path);
    
    #ifdef	__cplusplus
}
#endif

#endif	/* _FileUtils_H */
