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

#ifndef _ExtractUtils_H
#define	_ExtractUtils_H

#include <windows.h>
#include "StringUtils.h"
#include "JavaUtils.h"
#include "Errors.h"

#ifdef	__cplusplus
extern "C" {
#endif    
    
    extern const DWORD STUB_FILL_SIZE;
    
    void skipStub(LauncherProperties * props);
    
    void loadI18NStrings(LauncherProperties * props);
    
    WCHARList * newWCHARList(DWORD number) ;
    void readLauncherProperties(LauncherProperties * props);    
    void freeWCHARList(WCHARList ** plist);
    void freeLauncherResource(LauncherResource ** file);
    
    void extractJVMData(LauncherProperties * props);
    void extractData(LauncherProperties *props);
    
#ifdef	__cplusplus
}
#endif

#endif	/* _ExtractUtils_H */
