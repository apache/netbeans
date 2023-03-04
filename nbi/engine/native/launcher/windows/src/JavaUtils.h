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

#ifndef _JavaUtils_H
#define	_JavaUtils_H

#include <windows.h>
#include "Launcher.h"
#include "Types.h"
#include "Errors.h"

#ifdef	__cplusplus
extern "C" {
#endif
    
// java.version
// java.vm.version
// java.vendor
// os.name
// os.arch
#define TEST_JAVA_PARAMETERS 5    
#define MAX_LEN_VALUE_NAME 16383

WCHAR * getJavaResource(WCHAR * location, const WCHAR * suffix);

void getJavaProperties(WCHAR * location, LauncherProperties * props, JavaProperties ** javaProps);

void findSystemJava(LauncherProperties * props);

JavaVersion * getJavaVersionFromString(char * string, DWORD * result);

char compareJavaVersion(JavaVersion * first, JavaVersion * second);

DWORD isJavaCompatible(JavaProperties *currentJava, JavaCompatible ** compatibleJava, DWORD number);

void printJavaProperties(LauncherProperties * props, JavaProperties * javaProps);

void freeJavaProperties(JavaProperties ** props);

void installJVM(LauncherProperties * props, LauncherResource *jvm);

JavaCompatible * newJavaCompatible();

#ifdef	__cplusplus
}
#endif

#endif	/* _JavaUtils_H */
