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
