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

#include <jni.h>
#include <windows.h>
#include <winreg.h>
#include <winnt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>

#include "../../.common/src/CommonUtils.h"

#ifndef _WindowsUtils_H
#define _WindowsUtils_H

#ifdef __cplusplus
extern "C" {
#endif

HKEY getHKEY(jint jSection);

int queryValue(int mode, HKEY section, const unsigned short * key, const unsigned short * name, DWORD* type, DWORD* size, byte** value, int expand);

int setValue(int mode, HKEY section, const unsigned short * key, const unsigned short * name, DWORD type, const byte* data, int size, int expand);

LONG getMode(jint jmode);
#ifdef __cplusplus
}
#endif
#endif // _WindowsUtils_H
