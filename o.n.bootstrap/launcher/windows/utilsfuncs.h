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
 /*
 * Author: Tomas Holy
 */

#ifndef _UTILSFUNCS_H
#define	_UTILSFUNCS_H

#include <windows.h>
#include <string>

bool isWow64();
bool disableFolderVirtualization(HANDLE hProcess);
bool getStringFromRegistry(HKEY rootKey, const char *keyName, const char *valueName, std::string &value);
bool getStringFromRegistryEx(HKEY rootKey, const char *keyName, const char *valueName, std::string &value,bool read64bit);
bool getStringFromRegistry64bit(HKEY rootKey, const char *keyName, const char *valueName, std::string &value);
bool getDwordFromRegistry(HKEY rootKey, const char *keyName, const char *valueName, DWORD &value);
bool dirExists(const char *path);
bool fileExists(const char *path);
bool normalizePath(char *path, int len);
bool createPath(const char *path);
char * getCurrentModulePath(char *path, int pathLen);
char * skipWhitespaces(char *str);
char * trimWhitespaces(char *str);
void logMsg(const char *format, ...);
void logErr(bool appendSysError, bool showMsgBox, const char *format, ...);
bool checkLoggingArg(int argc, char *argv[], bool delFile);
bool setupProcess(int &argc, char *argv[], DWORD &parentProcID, const char *attachMsg = 0);
bool printToConsole(const char *msg);
bool getParentProcessID(DWORD &id);
bool isConsoleAttached();
int convertAnsiToUtf8(const char *ansi, char *utf8, int utf8Len);

#endif	/* _UTILSFUNCS_H */

