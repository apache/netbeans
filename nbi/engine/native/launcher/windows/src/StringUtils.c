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

#include <wchar.h>
#include <windows.h>
#include <winnls.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "StringUtils.h"



const char *  JVM_NOT_FOUND_PROP           = "nlw.jvm.notfoundmessage";
const char *  JVM_USER_DEFINED_ERROR_PROP  = "nlw.jvm.usererror";
    
const char * JVM_UNSUPPORTED_VERSION_PROP = "nlw.jvm.unsupportedversion";
const char * NOT_ENOUGH_FREE_SPACE_PROP   = "nlw.freespace";
const char * CANT_CREATE_TEMP_DIR_PROP    = "nlw.tmpdir";
const char * INTEGRITY_ERROR_PROP         = "nlw.integrity";
const char * OUTPUT_ERROR_PROP            = "nlw.output.error";
const char * JAVA_PROCESS_ERROR_PROP      = "nlw.java.process.error";
const char * EXTERNAL_RESOURE_LACK_PROP   = "nlw.missing.external.resource";
const char * BUNDLED_JVM_EXTRACT_ERROR_PROP = "nlw.bundled.jvm.extract.error";
const char * BUNDLED_JVM_VERIFY_ERROR_PROP  = "nlw.bundled.jvm.verify.error";

const char * ARG_OUTPUT_PROPERTY          = "nlw.arg.output";
const char * ARG_JAVA_PROP                = "nlw.arg.javahome";
const char * ARG_DEBUG_PROP               = "nlw.arg.verbose";
const char * ARG_TMP_PROP                 = "nlw.arg.tempdir";
const char * ARG_CPA_PROP                 = "nlw.arg.classpatha";
const char * ARG_CPP_PROP                 = "nlw.arg.classpathp";
const char * ARG_EXTRACT_PROP             = "nlw.arg.extract";
const char * ARG_DISABLE_SPACE_CHECK      = "nlw.arg.disable.space.check";
const char * ARG_LOCALE_PROP              = "nlw.arg.locale";
const char * ARG_SILENT_PROP              = "nlw.arg.silent";
const char * ARG_HELP_PROP                = "nlw.arg.help";

const char * MSG_CREATE_TMPDIR     = "nlw.msg.create.tmpdir";
const char * MSG_EXTRACT_DATA      = "nlw.msg.extract";
const char * MSG_JVM_SEARCH        = "nlw.msg.jvmsearch";
const char * MSG_SET_OPTIONS       = "nlw.msg.setoptions";
const char * MSG_RUNNING           = "nlw.msg.running";
const char * MSG_TITLE             = "nlw.msg.title";
const char * MSG_MESSAGEBOX_TITLE  = "nlw.msg.messagebox.title";
const char * MSG_PROGRESS_TITLE    = "nlw.msg.progress.title";

const char * EXIT_BUTTON_PROP     = "nlw.msg.button.error" ;   
const char * MAIN_WINDOW_TITLE     = "nlw.msg.main.title" ;




//adds string the the initial string and modifies totalWCHARs and capacity
// initial - the beginning of the string
// size - pointer to the value that contains the length of the initial string
//        It is modified to store returning string length
// addString - additional string
//


void freeI18NMessages(LauncherProperties * props) {
    if(props->i18nMessages!=NULL) {
        DWORD i=0;
        
        for(i=0;i<props->I18N_PROPERTIES_NUMBER;i++) {
            FREE(props->i18nMessages->properties[i]);
            FREE(props->i18nMessages->strings[i]);
        }        
        FREE(props->i18nMessages->properties);        
        FREE(props->i18nMessages->strings);        
        FREE(props->i18nMessages);
    }
}

char * searchA(const char * wcs1, const char * wcs2) {
    char *cp = (char *) wcs1;
    char *s1, *s2;
    
    if ( !*wcs2) {
        return (char *)wcs1;
    }
    
    while (*cp) {
        s1 = cp;
        s2 = (char *) wcs2;
        
        while ( *s1 && *s2 && !(*s1-*s2) ) {
            s1++, s2++;
        }
        if (!*s2) {
            return(cp);
        }
        cp++;
    }
    return(NULL);
}

WCHAR * searchW(const WCHAR * wcs1, const WCHAR * wcs2) {
    WCHAR *cp = (WCHAR *) wcs1;
    WCHAR *s1, *s2;
    
    if ( !*wcs2) {
        return (WCHAR *)wcs1;
    }
    
    while (*cp) {
        s1 = cp;
        s2 = (WCHAR *) wcs2;
        
        while ( *s1 && *s2 && !(*s1-*s2) ) {
            s1++, s2++;
        }
        if (!*s2) {
            return(cp);
        }
        cp++;
    }
    return(NULL);
}

void getI18nPropertyTitleDetail(LauncherProperties * props, const char * name, WCHAR ** title, WCHAR ** detail) {
    const WCHAR * prop = getI18nProperty(props,name);    
    WCHAR * detailStringSep = searchW(prop, L"\n");
    
    if(detailStringSep == NULL) {
        *title = appendStringW(NULL, prop);
        *detail = NULL;
    } else {
        DWORD dif = getLengthW(prop) - getLengthW(detailStringSep);
        *title = appendStringNW(NULL, 0, prop, dif);
        *detail = appendStringW(NULL, prop + (dif + 1));
    }    
}
const WCHAR * getI18nProperty(LauncherProperties * props, const char * name) {
    if(name==NULL) return NULL;
    if(name!=NULL && props->i18nMessages!=NULL) {
        DWORD i;
        for(i=0;i<props->I18N_PROPERTIES_NUMBER;i++) {
            char * pr = props->i18nMessages->properties[i];
            if(pr!=NULL) { // hope so it`s true
                if(lstrcmpA(name, pr)==0) {
                    return props->i18nMessages->strings[i];
                }
            }
        }
    }
    return getDefaultString(name);
}

WCHAR * getDefaultString(const char *name) {
    if(lstrcmpA(name, JVM_NOT_FOUND_PROP)==0) {
        return L"Can`t find suitable JVM. Specify it with %s argument";
    } else if(lstrcmpA(name, NOT_ENOUGH_FREE_SPACE_PROP)==0) {
        return L"Not enought free space at %s";
    } else if(lstrcmpA(name, CANT_CREATE_TEMP_DIR_PROP)==0) {
        return L"Can`t create temp directory %s";
    } else if(lstrcmpA(name, INTEGRITY_ERROR_PROP)==0) {
        return L"Integrity error. File %s is corrupted";
    } else if(lstrcmpA(name, JVM_USER_DEFINED_ERROR_PROP)==0) {
        return L"Can`t find JVM at %s";
    } else if(lstrcmpA(name, JVM_UNSUPPORTED_VERSION_PROP)==0) {
        return L"Unsupported JVM at %s";
    } else if(lstrcmpA(name, OUTPUT_ERROR_PROP)==0) {
        return L"Can`t create file %s.\nError: %s";
    } else if(lstrcmpA(name, JAVA_PROCESS_ERROR_PROP)==0) {
        return L"Java error:\n%s";
    } else if(lstrcmpA(name, ARG_JAVA_PROP)==0) {
        return L"%s Using specified JVM";
    }  else if(lstrcmpA(name, ARG_OUTPUT_PROPERTY)==0) {
        return L"%s Output all stdout/stderr to the file";
    } else if(lstrcmpA(name, ARG_DEBUG_PROP)==0) {
        return L"%s Use verbose output";
    } else if(lstrcmpA(name, ARG_TMP_PROP)==0) {
        return L"%s Use specified temporary dir for extracting data";
    } else if(lstrcmpA(name, ARG_CPA_PROP)==0) {
        return L"%s Append classpath";
    }  else if(lstrcmpA(name, ARG_CPP_PROP)==0) {
        return L"%s Prepend classpath";
    } else if(lstrcmpA(name, ARG_EXTRACT_PROP)==0) {
        return L"%s Extract all data";
    }  else if(lstrcmpA(name, ARG_HELP_PROP)==0) {
        return L"%s Using this help";
    } else if(lstrcmpA(name, ARG_DISABLE_SPACE_CHECK)==0) {
        return L"%s Disable free space check";
    } else if(lstrcmpA(name, ARG_LOCALE_PROP )==0) {
        return L"%s Use specified locale for messagess";       
    } else if(lstrcmpA(name, ARG_SILENT_PROP )==0) {
        return L"%s Run silently";       
    } else if(lstrcmpA(name, MSG_CREATE_TMPDIR)==0) {
        return L"Creating tmp directory...";
    } else if(lstrcmpA(name, MSG_EXTRACT_DATA)==0) {
        return L"Extracting data...";
    } else if(lstrcmpA(name, MSG_JVM_SEARCH)==0) {
        return L"Finding JVM...";
    } else if(lstrcmpA(name, MSG_RUNNING)==0) {
        return L"Running JVM...";
    } else if(lstrcmpA(name, MSG_SET_OPTIONS)==0) {
        return L"Setting command options...";
    } else if(lstrcmpA(name, MSG_MESSAGEBOX_TITLE)==0) {
        return L"Message";
    } else if(lstrcmpA(name, MSG_PROGRESS_TITLE)==0) {
        return L"Running";
    } else if(lstrcmpA(name, EXIT_BUTTON_PROP)==0) {
        return L"Exit";
    } else if(lstrcmpA(name, MAIN_WINDOW_TITLE)==0) {
        return L"NBI Launcher";
    } else if(lstrcmpA(name, EXTERNAL_RESOURE_LACK_PROP)==0) {
        return L"Can`t run launcher\nThe following file is missing : %s";
    } else if(lstrcmpA(name, BUNDLED_JVM_EXTRACT_ERROR_PROP)==0) {
        return L"Can`t run prepare bundled JVM";
    } else if(lstrcmpA(name, BUNDLED_JVM_VERIFY_ERROR_PROP)==0) {
        return L"Can`t run verify bundled JVM";
    }
    return NULL;   
}

DWORD getLengthA(const char * message) {
    return (message!=NULL) ? lstrlenA(message) : 0;
}

DWORD getLengthW(const WCHAR * message) {
    return (message!=NULL) ? lstrlenW(message) : 0;
}

//adds string the the initial string
char *  appendStringN(char *  initial, DWORD initialLength, const char * addString, DWORD addStringLength) {
    DWORD length = initialLength + addStringLength + 1;
    if (length > 1) {
        char * tmp = newpChar(length+1);
        DWORD i=0;
        if(initialLength!=0) {
            for(i=0;i<initialLength;i++) {
                tmp[i]=initial[i];
            }
            FREE(initial);
        }
        for(i=0;i<addStringLength;i++) {
            tmp[i+initialLength] = addString[i];
        }
        
        return tmp;
    } else {
        return NULL;
    }
}

char * appendString(char *  initial, const char * addString) {
    return appendStringN(initial, getLengthA(initial), addString, getLengthA(addString));
}


//adds string the the initial string
WCHAR *  appendStringNW(WCHAR *  initial, DWORD initialLength, const WCHAR * addString, DWORD addStringLength) {
    
    DWORD length = initialLength + addStringLength + 1;
    if(length>1) {
        
        WCHAR * tmp = newpWCHAR(length+1);
        DWORD i=0;
        
        if(initialLength!=0) {
            for(i=0;i<initialLength;i++) {
                tmp[i]=initial[i];
            }
            FREE(initial);
        }
        
        for(i=0;i<addStringLength;i++) {
            tmp[i+initialLength] = addString[i];
        }
        tmp[length] = 0;
        
        return tmp;
    } else {
        
        return NULL;
    }
}

WCHAR * appendStringW(WCHAR *  initial, const WCHAR * addString) {
    return appendStringNW(initial, getLengthW(initial), addString, getLengthW(addString));
}

WCHAR * escapeString(const WCHAR * string) {
    DWORD length = getLengthW(string);
    WCHAR *result = newpWCHAR(length * 2 + 4);
    DWORD i=0;
    DWORD r=0;
    DWORD bsCounter = 0;    
    int quoting = searchW(string, L" ") || searchW(string, L"\t");
    if(quoting) {
        result[r++] = '\"';
    }
    for(i=0;i<length;i++) {
        const WCHAR c = string[i];
        switch(c) {
            case '\\' :
                bsCounter++;
                break;
            case '"':
                bsCounter++;
                do {
                    result[r++] = '\\';
                    bsCounter--;
                } while(bsCounter>0);
            default:
                bsCounter = 0;                
                break;
        }
        result[r++] = c;
    }
    if(quoting) {
        while(bsCounter>0) {
            result[r++] = '\\';
            bsCounter--;
        }
        result[r++] = '\"';
    }
    result[r] = '\0';    
    return result;     
}

char * DWORDtoCHARN(DWORD value, int fillZeros) {
    int digits = 0;
    DWORD tmpValue = value;
    int i = 0;
    char * str;

    do {
        digits++;
        tmpValue = tmpValue / 10;
    } while(tmpValue!=0);
    tmpValue = value;
    if(digits < fillZeros) {
        digits = fillZeros;
    }
    str = (char*) LocalAlloc(LPTR, sizeof(char)*(digits +1));
    str[digits] = '\0';
    for(i=0;i<digits;i++) {
        str [digits - i - 1] = '0' + (char) (tmpValue - ((tmpValue / 10) * 10));
        tmpValue = tmpValue / 10;
    }
    return str;
}

WCHAR * DWORDtoWCHARN(DWORD value, int fillZeros) {
    int digits = 0;
    DWORD tmpValue = value;
    int i = 0;
    WCHAR * str;

    do {
        digits++;
        tmpValue = tmpValue / 10;
    } while(tmpValue!=0);
    tmpValue = value;
    if(digits < fillZeros) {
        digits = fillZeros;
    }
    
    str = (WCHAR*) LocalAlloc(LPTR, sizeof(WCHAR)*(digits +1));
    str[digits] = L'\0';
    for(i=0;i<digits;i++) {
        str [digits - i - 1] = L'0' + (WCHAR) (tmpValue - ((tmpValue / 10) * 10));
        tmpValue = tmpValue / 10;
    }
    return str;
}
WCHAR * DWORDtoWCHAR(DWORD value) {
    return DWORDtoWCHARN(value, 0);
}

char * DWORDtoCHAR(DWORD value) {
    return DWORDtoCHARN(value,0);
}

char * long2charN(long value, int fillZeros) {
    int digits = 0;
    long tmpValue = value;
    int i = 0;
    char * str;
    
    do {
        digits++;
        tmpValue = tmpValue / 10;
    } while(tmpValue!=0);
    tmpValue = value;
    if(digits < fillZeros) {
        digits = fillZeros;
    }
    str = (char*) LocalAlloc(LPTR, sizeof(char)*(digits +1));
    str[digits] = '\0';
    for(i=0;i<digits;i++) {
        str [digits - i - 1] = '0' + (char) (tmpValue - ((tmpValue / 10) * 10));
        tmpValue = tmpValue / 10;
    }
    return str;
}

char * long2char(long value) {
    return long2charN(value,0);
}

char * word2charN(WORD value, int fillZeros) {
    int digits = 0;
    WORD tmpValue = value;
    int i = 0;
    char * str;

    do {
        digits++;
        tmpValue = tmpValue / 10;
    } while(tmpValue!=0);
    tmpValue = value;
    if(digits < fillZeros) {
        digits = fillZeros;
    }
    str = (char*) LocalAlloc(LPTR, sizeof(char)*(digits +1));
    str[digits] = '\0';
    for(i=0;i<digits;i++) {
        str [digits - i - 1] = '0' + (char) (tmpValue - ((tmpValue / 10) * 10));
        tmpValue = tmpValue / 10;
    }
    return str;
}

char * word2char(WORD value) {
    return word2charN(value,0);
}

char * int64ttoCHAR(int64t* value) {
    if(value->High==0) {
        return DWORDtoCHAR(value->Low);
    } else {
        char * high = DWORDtoCHAR(value->High);
        char * low  = DWORDtoCHAR(value->Low);
        DWORD highLength = getLengthA(high);
        DWORD lowLength  = getLengthA(low);

        char * str = newpChar(highLength + lowLength + 9);
        DWORD i = 0;
        str[0] = '[';
        str[1] = 'H';
        str[2] = ']';
        str[3] = '[';
        str[4] = 'L';
        str[5] = ']';
        str[6] = '=';

        for(i = 0; i < highLength; i++) {
            str [7 + i] = high[i];
        }
        str [7 + highLength] =L',';
        for(i = 0; i < lowLength;i++) {
            str [8 + highLength + i] = low[i];
        }
        str [8 + highLength + lowLength] = '\0';
        FREE(high);
        FREE(low);
        return str;
    }
}
WCHAR * int64ttoWCHAR(int64t*value) {
    if(value->High==0) {
        return DWORDtoWCHAR(value->Low);
    } else {
        WCHAR * high = DWORDtoWCHAR(value->High);
        WCHAR * low  = DWORDtoWCHAR(value->Low);
        DWORD highLength = getLengthW(high);
        DWORD lowLength  = getLengthW(low);

        WCHAR * str = newpWCHAR(highLength + lowLength + 9);
        DWORD i = 0;
        str[0] = L'[';
        str[1] = L'H';
        str[2] = L']';
        str[3] = L'[';
        str[4] = L'L';
        str[5] = L']';
        str[6] = L'=';

        for(i = 0; i < highLength;i++) {
            str [7 + i] = high[i];
        }
        str [7 + highLength] = L',';
        for(i = 0; i < lowLength;i++) {
            str [8 + highLength + i] = low[i];
        }
        str [8 + highLength + lowLength] = L'\0';
        FREE(high);
        FREE(low);
        return str;
    }
}

void freeStringList(StringListEntry **ss) {
    while ( (*ss) !=NULL) {
        StringListEntry * tmp = (*ss)->next;
        FREE((*ss)->string);
        FREE((*ss));
        * ss = tmp;
    }
}

DWORD inList(StringListEntry * top, WCHAR * str) {    
    StringListEntry * tmp = top;
    while(tmp!=NULL) {
        if(lstrcmpW(tmp->string, str)==0) {
            return 1;            
        }
        tmp = tmp->next;
    }
    return 0;
}

StringListEntry * addStringToList(StringListEntry * top, WCHAR * str) {
    StringListEntry * ss = (StringListEntry*) LocalAlloc(LPTR,sizeof(StringListEntry));    
    ss->string = appendStringW(NULL, str);
    ss->next   = top;
    return ss;
}


DWORD getLineSeparatorNumber(char *str) {
    DWORD result = 0;
    char *ptr = str;
    if(ptr!=NULL) {
        while((ptr = searchA(ptr, "\n"))!=NULL) {
            ptr++;
            result++;
            if(ptr==NULL)  break;
        }
    }
    return result;
}

char *toCharN(const WCHAR * string, DWORD n) {    
    DWORD len = 0;
    DWORD length = 0;
    char * str = NULL;
    if(string==NULL) return NULL;
    //static DWORD excludeCodepages [] = { 50220, 50221, 50222, 50225, 50227, 50229, 52936, 54936, 57002,  57003, 57004, 57005, 57006, 57007, 57008, 57009, 57010, 57011, 65000, 42};
    //int symbols = 0;
    len = getLengthW(string);    
    if(n<len) len = n;
    length = WideCharToMultiByte(CP_ACP, 0, string, len, NULL, 0, 0, NULL);
    str = newpChar(length+1);
    WideCharToMultiByte(CP_ACP, 0, string, len, str, length, 0, NULL);
    return str;
}

char * toChar(const WCHAR * string) {
    return toCharN(string, getLengthW(string));
}

WCHAR *createWCHAR(SizedString * sz) {
    char * str = sz->bytes;
    DWORD len = sz->length;
    int unicodeFlags;    
    DWORD i;
    char * string = NULL;
    char * ptr = NULL;
    WCHAR * wstr = NULL;
    if(str==NULL) return NULL;
    //static DWORD excludeCodepages [] = { 50220, 50221, 50222, 50225, 50227, 50229, 52936, 54936, 57002,  57003, 57004, 57005, 57006, 57007, 57008, 57009, 57010, 57011, 65000, 42};
    
    string = appendStringN(NULL, 0 , str, len);    
    ptr = string;
    unicodeFlags = -1 ;
    if(len>=2) {
        BOOL hasBOM        = (*ptr == '\xFF' && *(ptr+1) == '\xFE');
        BOOL hasReverseBOM = (*ptr == '\xFE' && *(ptr+1) == '\xFF');
        
        if (IsTextUnicode(string, len, &unicodeFlags) || hasBOM || hasReverseBOM) {
            //text is unicode
            len-= 2;
            ptr+= 2;
            if (unicodeFlags & IS_TEXT_UNICODE_REVERSE_SIGNATURE || hasReverseBOM) {
                //we need to change bytes order
                char c;
                for (i = 0 ; i < len/2 ; i++) {
                    c = ptr [2 * i] ;
                    ptr [2 * i] = ptr [2 * i + 1] ;
                    ptr [2 * i + 1] = c;
                }
            }
        }
        
    }
    wstr = newpWCHAR(len/2+1);
    
    for(i=0;i<len/2;i++) {
        ptr[2*i] = (ptr[2*i]) & 0xFF;
        ptr[2*i+1] = (ptr[2*i+1])& 0xFF;
        wstr[i] = ((unsigned char) ptr[2*i]) + (((unsigned char)ptr[2*i+1])  << 8);
    }
    
    FREE(string);
    return wstr;
}
WCHAR *toWCHARn(char * str, DWORD n) {
    DWORD len = 0;
    DWORD length = 0;
    WCHAR * wstr = NULL;
    if(str==NULL) return NULL;
    //static DWORD excludeCodepages [] = { 50220, 50221, 50222, 50225, 50227, 50229, 52936, 54936, 57002,  57003, 57004, 57005, 57006, 57007, 57008, 57009, 57010, 57011, 65000, 42};
    len = getLengthA(str);
    if(n<len) len = n;
    length = MultiByteToWideChar(CP_ACP, 0, str, len, NULL, 0);
    wstr = newpWCHAR(length+1);
    MultiByteToWideChar(CP_ACP, 0, str, len, wstr, length);
    return wstr;
}

WCHAR * toWCHAR( char *string) {
    return toWCHARn(string, getLengthA(string));
}

SizedString * createSizedString() {
    SizedString * s = (SizedString*)LocalAlloc(LPTR,sizeof(SizedString));
    s->bytes = NULL;
    s->length = 0;
    return s;
}

void freeSizedString(SizedString ** s) {
    if(*s!=NULL) {
        FREE((*s)->bytes);
        FREE((*s));
        *s = NULL;
    }
}


WCHAR * getLocaleName() {
    LANGID langID = LANGIDFROMLCID(GetUserDefaultLCID());
    LCID localeID = MAKELCID(langID, SORT_DEFAULT);
    const   DWORD MAX_LENGTH = 512;
    WCHAR * lang = newpWCHAR(MAX_LENGTH);
    WCHAR * country = newpWCHAR(MAX_LENGTH);
    WCHAR * locale = NULL;
    GetLocaleInfoW(localeID, LOCALE_SISO639LANGNAME, lang, MAX_LENGTH);
    GetLocaleInfoW(localeID, LOCALE_SISO3166CTRYNAME, country, MAX_LENGTH);    
    locale = appendStringW(appendStringW(appendStringW(NULL, lang), L"_"), country);
    FREE(country);
    FREE(lang);
    return locale;
}

WCHAR * newpWCHAR(DWORD length) {
    WCHAR * res = (WCHAR*) LocalAlloc(LPTR,sizeof(WCHAR) * length);
    ZERO(res, length * sizeof(WCHAR));
    return res;
}
WCHAR ** newppWCHAR(DWORD length) {
    return (WCHAR**) LocalAlloc(LPTR,sizeof(WCHAR *) * length);
}


char * newpChar(DWORD length) {
    char * res = (char*) LocalAlloc(LPTR,sizeof(char) * length);
    ZERO(res, length * sizeof(char));
    return res;
}

char ** newppChar(DWORD length) {
    return (char**) LocalAlloc(LPTR,sizeof(char*) * length);
}

int compare(int64t * size, DWORD value) {
    if (size->High > 0) return 1;
    
    if (size->Low > value)  
        return 1;
    else if(size->Low == value) 
        return 0;
    else //if(size->Low < value)  
        return -1;    
}
int compareInt64t(int64t * a1, int64t * a2) {
    if (a1->High > a2->High) {
        return 1;
    } else if(a1->High == a2->High) {
        if (a1->Low > a2->Low)  {
            return 1;
        } else if(a1->Low == a2->Low)  {
            return 0;
        }
    }
    return -1;
}

void plus(int64t * size, DWORD value) {
    if(value!=0) {
        if((MAXDWORD - size->Low) >= (value - 1)) {
            size->Low = size->Low + value;
        } else {
            size->High = size->High + 1;
            size->Low  = value - (MAXDWORD - size->Low) - 1;
        }
    }
}
void multiply(int64t * size, DWORD value) {
    if(value==0) {
        size->Low = 0;
        size->High = 0;
    } else {
        DWORD i = 0;
        DWORD low = size->Low;
        DWORD high = size->High;
        size->High = 0;
        for(; i < value - 1 ; i++) {
            plus(size, low);
        }
        size->High += high * value;
    }
}

void minus(int64t * size, DWORD value) {
    if(value!=0) {
        if(size->Low < value) {
            size->High = size->High -1;
            size->Low = size->Low + (MAXDWORD - value) + 1;
        } else {
            size->Low = size->Low - value;
        }}
}
int64t * newint64_t(DWORD low, DWORD high) {
    int64t * res = (int64t *) LocalAlloc(LPTR,sizeof(int64t));
    res->Low = low;
    res->High = high;
    return res;
}
WCHAR * getErrorDescription(DWORD dw) {
    WCHAR * lpMsgBuf;
    WCHAR * lpDisplayBuf = NULL;
    WCHAR * res = DWORDtoWCHAR(dw);
    
    FormatMessageW( FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
    NULL, dw, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPWSTR) &lpMsgBuf, 0, NULL );
    
    lpDisplayBuf = appendStringW(lpDisplayBuf, L"Error code (");
    lpDisplayBuf = appendStringW(lpDisplayBuf, res);
    lpDisplayBuf = appendStringW(lpDisplayBuf, L"): ");
    lpDisplayBuf = appendStringW(lpDisplayBuf, lpMsgBuf);

    FREE(lpMsgBuf);
    FREE(res);
    
    return lpDisplayBuf;
    
}

WCHAR * formatMessageW(const WCHAR* message, const DWORD varArgsNumber, ...) {
    DWORD totalLength=getLengthW(message);
    DWORD counter=0;
    WCHAR * result = NULL;
    
    va_list ap;
    va_start(ap, varArgsNumber);
    
    while((counter++)<varArgsNumber) {
        WCHAR * arg = va_arg( ap, WCHAR * );
        totalLength+=getLengthW(arg);
    }
    va_end(ap);
    
    result = newpWCHAR(totalLength + 1);
    va_start(ap, varArgsNumber);
    wvsprintfW(result, message, ap);
    va_end(ap);
    return result;
}

DWORD isOK(LauncherProperties * props) {
    return (props->status == ERROR_OK);
}
