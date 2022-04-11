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

#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x0501
#endif

#include <Windows.h>
#include <stddef.h>
#include <stdlib.h>

/**
 * Invoked by ExecutorCND.java with the following arguments:
 * 0 - Path to GdbKillProc.exe
 * 1 - "-s"
 * 2 - "INT"
 * 3 - gdb process PID.
 */
int main(int argc, char **argv)
{
    if (argc != 4) {
        /* Not enough arguments */
        return 1;
    }
    unsigned pid = (unsigned) strtol(argv[3], NULL, 0);
    if (pid == 0) {
	    return 1;
    }
    /* https://docs.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-openprocess */
    HANDLE proc = OpenProcess(PROCESS_ALL_ACCESS, FALSE, (DWORD) pid);
    if (proc == NULL) {
	    return 2;
    }
    /* https://docs.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-debugbreakprocess */
    if (!DebugBreakProcess(proc)) {
	    return 3;
    }
    /* https://docs.microsoft.com/en-us/windows/win32/api/handleapi/nf-handleapi-closehandle */
    CloseHandle(proc);
    return 0;
}

