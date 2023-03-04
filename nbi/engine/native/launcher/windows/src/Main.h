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

#ifndef _Main_H
#define	_Main_H

#include <wchar.h>
#include "Errors.h"


#ifdef	__cplusplus
extern "C" {
#endif

void addProgressPosition(LauncherProperties *props,DWORD add);
void setProgressRange(LauncherProperties *props, int64t * size);
void setErrorDetailString(LauncherProperties *props,const WCHAR * message);
void setErrorTitleString(LauncherProperties *props,const WCHAR * message);
void setButtonString(LauncherProperties *props,const WCHAR * message);
void setProgressTitleString(LauncherProperties *props,const WCHAR * message);
void setMainWindowTitle(LauncherProperties * props, const WCHAR * message);

void showLauncherWindows(LauncherProperties *props);
void closeLauncherWindows(LauncherProperties *props);
void hideLauncherWindows(LauncherProperties *props);

void hide(LauncherProperties *props,HWND hwnd);
void show(LauncherProperties *props,HWND hwnd);

DWORD isTerminated(LauncherProperties * props);

void showErrorW(LauncherProperties *props, const char * error, const DWORD varArgsNumber, ...);

void showMessageW(LauncherProperties *props,const WCHAR* message, const DWORD number, ...);
//void showMessageA(LauncherProperties *props,const char * message, const DWORD varArgsNumber, ...);

#ifdef	__cplusplus
}
#endif

#endif	/* _Main_H */

