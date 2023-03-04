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

#ifndef _ProcessUtils_H
#define	_ProcessUtils_H

#include <windows.h>
#include <stdio.h>
#include "Errors.h"
#include "Types.h"
#include "ExtractUtils.h"
#include "FileUtils.h"



#ifdef	__cplusplus
extern "C" {
#endif
    
    #define STREAM_BUF_LENGTH 1024
    
    extern const DWORD DEFAULT_PROCESS_TIMEOUT;
    
    char * readHandle(HANDLE hRead);
    
    void executeCommand(LauncherProperties * props, WCHAR * command, WCHAR * dir, DWORD timeLimitMillis, HANDLE hWriteOutput, HANDLE hWriteError, DWORD priority);
    
#ifdef	__cplusplus
}
#endif

#endif	/* _ProcessUtils_H */
