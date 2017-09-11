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

#ifndef _StringUtils_H
#define	_StringUtils_H

#include <windows.h>
#include <stdarg.h>
#include "Types.h"
#include "Errors.h"
#ifdef	__cplusplus
extern "C" {
#endif

extern const char *  JVM_NOT_FOUND_PROP;
extern const char *  JVM_USER_DEFINED_ERROR_PROP;
extern const char *  JVM_UNSUPPORTED_VERSION_PROP;
extern const char *  NOT_ENOUGH_FREE_SPACE_PROP;  
extern const char *  CANT_CREATE_TEMP_DIR_PROP;   
extern const char *  INTEGRITY_ERROR_PROP;   
extern const char *  OUTPUT_ERROR_PROP;          
extern const char *  JAVA_PROCESS_ERROR_PROP;    
extern const char *  EXTERNAL_RESOURE_LACK_PROP;    
extern const char *  BUNDLED_JVM_EXTRACT_ERROR_PROP;    
extern const char *  BUNDLED_JVM_VERIFY_ERROR_PROP;    
extern const char *  ARG_OUTPUT_PROPERTY;        
extern const char *  ARG_JAVA_PROP;              
extern const char *  ARG_DEBUG_PROP;             
extern const char *  ARG_TMP_PROP;               
extern const char *  ARG_CPA_PROP;               
extern const char *  ARG_CPP_PROP;               
extern const char *  ARG_EXTRACT_PROP;
extern const char *  ARG_DISABLE_SPACE_CHECK;
extern const char *  ARG_LOCALE_PROP;
extern const char *  ARG_SILENT_PROP;
extern const char *  ARG_HELP_PROP;
extern const char * MSG_CREATE_TMPDIR;     
extern const char * MSG_EXTRACT_DATA;     
extern const char * MSG_JVM_SEARCH;       
extern const char * MSG_SET_OPTIONS;      
extern const char * MSG_RUNNING;          
extern const char * MSG_TITLE;            
extern const char * MSG_MESSAGEBOX_TITLE; 
extern const char * MSG_PROGRESS_TITLE;   
extern const char * EXIT_BUTTON_PROP;     
extern const char * MAIN_WINDOW_TITLE;    
    
#define FREE(x) { \
	if((x)!=NULL) {\
	LocalFree(x); \
	(x)=NULL;\
	}\
}


#ifdef _MSC_VER
#define ZERO(x,y) SecureZeroMemory((x),(y));
#else
#define ZERO(x,y) ZeroMemory((x),(y));
#endif


    

    
    void freeI18NMessages(LauncherProperties * props);
    
    void getI18nPropertyTitleDetail(LauncherProperties * props, const char * name, WCHAR ** title, WCHAR ** detail);
    const WCHAR * getI18nProperty(LauncherProperties * props, const char * name);
    WCHAR * getDefaultString(const char *name);
    
    WCHAR * addString(WCHAR *  initial, WCHAR *addString, long number, WCHAR * totalWCHARs, WCHAR * capacity);
    char *  appendStringN(char *  initial, DWORD initialLength, const char * addString, DWORD addStringLength);
    WCHAR *  appendStringNW(WCHAR *  initial, DWORD initialLength, const WCHAR * addString, DWORD addStringLength);
    char * appendString(char *  initial, const char * addString);
    WCHAR * appendStringW(WCHAR *  initial, const WCHAR * addString);
    WCHAR * escapeString(const WCHAR * string);
    
    void freeStringList(StringListEntry **s);
    StringListEntry * addStringToList(StringListEntry * top, WCHAR * str);
    DWORD inList(StringListEntry * top, WCHAR * str);
    
    char *toChar(const WCHAR * string);
    char *toCharN(const WCHAR * string, DWORD length);
    WCHAR * toWCHAR(char * string);
    WCHAR * toWCHARn(char * string, DWORD length);
    
    WCHAR *createWCHAR(SizedString * sz);
    
    SizedString * createSizedString();
    char * int64ttoCHAR(int64t*);
    WCHAR * int64ttoWCHAR(int64t*);
    char * DWORDtoCHAR(DWORD);
    char * DWORDtoCHARN(DWORD,int);
    
    WCHAR * DWORDtoWCHAR(DWORD);
    WCHAR * DWORDtoWCHARN(DWORD,int);
    
    char * long2char(long value);
    char * long2charN(long value,int fillZeros);

    char * word2char(WORD value);
    char * word2charN(WORD value,int fillZeros);

    void freeSizedString(SizedString ** s);
        
    WCHAR * getLocaleName();
    
    WCHAR * newpWCHAR(DWORD length);
    char * newpChar(DWORD length);
    WCHAR * searchW( const WCHAR * wcs1, const WCHAR * wcs2);
    char * searchA(const char* str1, const char * str2);

    WCHAR ** newppWCHAR(DWORD length);
    char ** newppChar(DWORD length);
    int64t * newint64_t(DWORD low, DWORD high);
    int compare(int64t * size, DWORD value);
    int compareInt64t(int64t * a1, int64t * a2);
    void plus(int64t * size, DWORD value);
    void multiply(int64t * size, DWORD value);
    void minus(int64t * size, DWORD value);
    DWORD getLineSeparatorNumber(char *str);
    DWORD getLengthA(const char * message);
    DWORD getLengthW(const WCHAR * message);
    
    WCHAR * getErrorDescription(DWORD dw);
    WCHAR * formatMessageW(const WCHAR* message, const DWORD varArgsNumber, ...);    
    DWORD isOK(LauncherProperties * props);
#ifdef	__cplusplus
}
#endif

#endif	/* _StringUtils_H */
