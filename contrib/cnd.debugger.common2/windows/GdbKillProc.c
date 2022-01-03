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

int main(int argc, char **argv)
{
    unsigned pid = (unsigned) strtol(argv[3], NULL, 0);
    if (pid == 0) {
	return 1;
    }
    HANDLE proc = OpenProcess(PROCESS_ALL_ACCESS, FALSE, (DWORD) pid);
    if (proc == NULL) {
	return 2;
    }
    if (!DebugBreakProcess(proc)) {
	return 3;
    }
    CloseHandle(proc);
    return 0;
}

