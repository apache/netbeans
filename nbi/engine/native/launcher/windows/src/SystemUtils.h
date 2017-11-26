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

#ifndef _SystemUtils_H
#define	_SystemUtils_H

#ifdef	__cplusplus
extern "C" {
#endif

DWORD is9x();
DWORD isNT();
DWORD isXP();
DWORD is2k();
DWORD is2003();
DWORD is2008();
DWORD is7();
DWORD isVista();
void  initWow64();

extern BOOL IsWow64;

#ifdef	__cplusplus
}
#endif

#endif	/* _SystemUtils_H */

