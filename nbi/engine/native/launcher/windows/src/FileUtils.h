/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
