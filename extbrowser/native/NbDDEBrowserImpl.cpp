/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

// BrowserClient.cpp : Defines the entry point for the DLL application.
//

// #include "stdafx.h"

#include <windows.h>
#include <wchar.h>
/*
#include <stdio.h>
#include <malloc.h>
#include <ddeml.h>
 */

#include <jni.h>

// ........................................................

extern "C" {

    /**********************************************************
     * Static variables
     *********************************************************/

    /** Instance ID for DDE */
    static DWORD InstId = 0;

    /** counter for topic generation */
    static DWORD dwcounter = 0;

    /**********************************************************
     * Static functions
     *********************************************************/

    static int InitDDE(void);

    static void ClearDde(void);

    static HDDEDATA CALLBACK DdeCallback
    (UINT Type, UINT Fmt, HCONV ConvHan,
            HSZ Str1, HSZ Str2,
            HDDEDATA Data, ULONG_PTR Dta1, ULONG_PTR Dta2);

    /**********************************************************
     * function 'InitDDE' WIN
     * desc	: Initialize DDE comunication
     * returns: 0 <==> OK
     *          other value <==> an error occured
     *********************************************************/
    static int InitDDE(void) {
        /** is this already initialized? */
        //	static char Set= 0;

        //	if(Set && InstId) return(0);
        if (InstId) return (0);

        if (DdeInitialize(&InstId, DdeCallback,
                APPCLASS_STANDARD /* |
					 APPCMD_FILTERINITS |
					 CBF_SKIP_ALLNOTIFICATIONS |
					 CBF_FAIL_ADVISES |
					 CBF_FAIL_EXECUTES |
					 CBF_FAIL_SELFCONNECTIONS |
					 CBF_SKIP_CONNECT_CONFIRMS |
					 CBF_SKIP_DISCONNECTS*/ |
                CBF_SKIP_REGISTRATIONS |
                CBF_SKIP_UNREGISTRATIONS, 0) != DMLERR_NO_ERROR) {
            printf("Error init DDE\n");
            return (-1);
        }
        //	printf("INIT DDE OK\n");
        // if(!Set) atexit(ClearDde);
        //	Set= 1;
        return (0);
    }

    /**********************************************************
     * function 'ClearDde' WINDOWS
     * desc	: quits comunitation through DDE
     *********************************************************/
    static void ClearDde(void) {
        if (!InstId) return;
        DdeUninitialize(InstId);
        InstId = 0;
    }

    /**********************************************************
     * function 'DDEcallback' WINDOWS
     * desc	: 
     *********************************************************/
    static HDDEDATA CALLBACK DdeCallback
    (UINT Type, UINT Fmt, HCONV ConvHan,
            HSZ Str1, HSZ Str2,
            HDDEDATA Data, ULONG_PTR Dta1, ULONG_PTR Dta2) {
        /** conversation handle */
        static HCONV shConv = NULL;

        char Item[256];
        char Service[256];
        char Topic[256];

        /* transaction type */
        switch (Type) {
                /* transaction type */
            case XTYP_CONNECT:

                if (!(DdeQueryString(InstId, Str1, Topic, 256, CP_WINANSI))) return (FALSE);
                if (!(DdeQueryString(InstId, Str2, Service, 256, CP_WINANSI))) return (FALSE);
                printf("XTYP_CONNECT >%s< >%s<\n", Service, Topic);
                fflush(stdout);


                // accept WWW_*Progress* topic
                if (!strcmp(Topic, "WWW_BeginProgress")) {
                    shConv = ConvHan;

                    dwcounter++;
                    return (HDDEDATA) TRUE;
                }
                if (!strcmp(Topic, "WWW_SetProgressRange"))
                    return (HDDEDATA) TRUE;
                if (!strcmp(Topic, "WWW_MakingProgress"))
                    return (HDDEDATA) TRUE;
                if (!strcmp(Topic, "WWW_EndProgress"))
                    return (HDDEDATA) TRUE;

                if (!strcmp(Topic, "WWW_URLEcho"))
                    return (HDDEDATA) TRUE;

                return (FALSE);

            case XTYP_POKE:

                if (!(DdeQueryString(InstId, Str1, Topic, 256, CP_WINANSI))) return (DDE_FNOTPROCESSED);
                if (!(DdeQueryString(InstId, Str2, Item, 256, CP_WINANSI))) return (DDE_FNOTPROCESSED);
                printf("XTYP_POKE >%s< >%s<\n", Topic, Item);
                fflush(stdout);

                if (!strcmp(Topic, "WWW_SetProgressRange")) {
                    return ((HDDEDATA) DDE_FACK);
                }
                if (!strcmp(Topic, "WWW_EndProgress")) {
                    return ((HDDEDATA) DDE_FACK);
                }

                if (!strcmp(Topic, "WWW_URLEcho")) {
                    return ((HDDEDATA) DDE_FACK);
                }
                return (DDE_FNOTPROCESSED);

            case XTYP_REQUEST:

                if (!(DdeQueryString(InstId, Str1, Topic, 256, CP_WINANSI))) return (DDE_FNOTPROCESSED);
                if (!(DdeQueryString(InstId, Str2, Item, 256, CP_WINANSI))) return (DDE_FNOTPROCESSED);
                printf("XTYP_REQUEST >%s< >%s<\n", Topic, Item);
                fflush(stdout);

                if (!strcmp(Topic, "WWW_BeginProgress")) {

                    HDDEDATA Dta;

                    if (!(Dta = DdeCreateDataHandle
                            (InstId, (unsigned char *) &dwcounter, sizeof (DWORD), 0, Str2, CF_TEXT, 0))) {
                        printf("CANNOT CREATE DATA\n");
                        return (NULL);
                    }
                    return (Dta);
                }// www_beginprogress
                else if (!strcmp(Topic, "WWW_MakingProgress")) {

                    HDDEDATA Dta;
                    char *s = "FALSE";

                    if (!(Dta = DdeCreateDataHandle
                            (InstId, (unsigned char*) s, strlen(s), 0, Str2, CF_TEXT, 0))) {
                        printf("CANNOT CREATE DATA\n");
                        return (NULL);
                    }
                    return (Dta);
                } // making progress


                return (NULL);

            case XTYP_XACT_COMPLETE:
                printf("XTYP_XACT_COMPLETE\n");
                return ((HDDEDATA) DDE_FACK);

            case XTYP_ADVDATA:
                printf("XTYP_ADVDATA\n");
                return ((HDDEDATA) DDE_FACK);

            case XTYP_ERROR:
                printf("XTYP_ERROR\n");
                break;

#if defined(DEBUG)
            case XTYP_CONNECT_CONFIRM:
                shConv = ConvHan;
                printf("XTYP_CONNECT_CONFIRM\n");
                break;

            case XTYP_DISCONNECT:
                printf("XTYP_DISCONNECT\n");
                break;

            case XTYP_REGISTER:
                printf("XTYP_REGISTER\n");
                break;

            case XTYP_UNREGISTER:
                printf("XTYP_UNREGISTER\n");
                break;

            default:
                if (Type & XCLASS_NOTIFICATION) {
                    printf("XCLASS_NOTIFICATION\n");
                }
                if (Type & XCLASS_FLAGS) {
                    printf("XCLASS_FLAGS\n");
                }
                if (Type & XCLASS_DATA) {
                    printf("XCLASS_DATA\n");
                }
                if (Type & XCLASS_BOOL) {
                    printf("XCLASS_BOOL\n");
                }
                printf("type not of interest\n");
#else
            default:
#endif /* DEBUG */
                break;
        }
        return (0);
    }

    /**********************************************************
     * function 
     * desc	 
     * 
     * param
     * returns
     * throws  
     *********************************************************/
    JNIEXPORT jbyteArray Java_org_netbeans_modules_extbrowser_NbDdeBrowserImpl_reqDdeMessage
    (JNIEnv *env,
            jobject obj,
            jstring server,
            jstring topic,
            jstring item,
            jint timeout) {
        DWORD len = 1024;
        char buff[1024];
        char msg[1024];
        jbyteArray retData = NULL;

        const char *Server = env->GetStringUTFChars(server, 0);
        const char *Topic = env->GetStringUTFChars(topic, 0);
        const char *Item = env->GetStringUTFChars(item, 0);

        HCONV Curr = 0;
        HSZ StrHproc, StrHitem, StrHtopic;
        HDDEDATA TrRes;
        DWORD Result; // transaction result

        // INIT (REINIT DDE)
        if (InitDDE()) {
            // printf("Cannot initialize DDE\n");
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            env->ThrowNew(excClass, "Cannot initialize DDE");
            return NULL;
        }

        // create string handles
        StrHproc = DdeCreateStringHandle(InstId, Server, CP_WINANSI);
        StrHtopic = DdeCreateStringHandle(InstId, Topic, CP_WINANSI);
        StrHitem = DdeCreateStringHandle(InstId, Item, CP_WINANSI);

        // connect
        if (!(Curr = DdeConnect(InstId, StrHproc, StrHtopic, NULL))) {
            // printf("Cannot connect to DDE server\n");
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            sprintf(msg, "DdeConnect errno >%X< when connecting to server %s, topic %s.\n", DdeGetLastError(InstId), Server, Topic);
            env->ThrowNew(excClass, msg);
            DdeFreeStringHandle(InstId, StrHtopic);
            DdeFreeStringHandle(InstId, StrHitem);
            DdeFreeStringHandle(InstId, StrHproc);
            ClearDde();
            return NULL;
        } else {
            //        printf("SEND TO >%s< TOPIC >%s< ITEM >%s<\n",
            //			      Server, Topic, Item); fflush (stdout);

            if (!(TrRes = DdeClientTransaction
                    (NULL, 0, Curr, StrHitem, CF_TEXT, XTYP_REQUEST, timeout, &Result))) {
                // printf("DDE Tx failed\n");
                jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                sprintf(msg, "DdeClientTransaction errno >%X<\n", DdeGetLastError(InstId));
                fflush(stdout);
                env->ThrowNew(excClass, msg);
            } else {

                // process result
                LPBYTE pData = DdeAccessData(TrRes, &len);
                memcpy(buff, pData, len);
                DdeUnaccessData(TrRes);
                DdeFreeDataHandle(TrRes);

                retData = env->NewByteArray(len);
                for (DWORD i = 0; i < len; i++)
                    env->SetByteArrayRegion(retData, i, 1, (jbyte*) (buff + i));
            }

            // release string handles
            DdeFreeStringHandle(InstId, StrHtopic);
            DdeFreeStringHandle(InstId, StrHitem);
            DdeFreeStringHandle(InstId, StrHproc);

            DdeDisconnect(Curr);
        }

        env->ReleaseStringUTFChars(server, Server);
        env->ReleaseStringUTFChars(topic, Topic);
        env->ReleaseStringUTFChars(item, Item);

        ClearDde();
        return retData;
    }

    /**********************************************************
     * Reads value from 
     * HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\Windows\CurrentVersion\\App Paths\\<browser>.exe
     * desc	 
     * 
     * param
     * returns
     * throws  
     *********************************************************/
    JNIEXPORT jstring Java_org_netbeans_modules_extbrowser_NbDdeBrowserImpl_getBrowserPath
    (JNIEnv *env,
            jclass cls,
            jstring browser) {
        DWORD len = 1024;
        DWORD type;
        char sSubKey[1024];
        char sReg[1024];
        char sPath[1024];
        HKEY hKey = NULL;
        jstring path = NULL;

        // In the past we used : 
        //    HKEY_CLASSES_ROOT\Applications\\<browser>.exe\shell\open\command key
        // It seems to be better to use :
        //    
        const char *b = env->GetStringUTFChars(browser, 0);
        sprintf(sSubKey, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\%s.exe", b);
        env->ReleaseStringUTFChars(browser, b);

        // open registry key
        //        printf("LOOKING FOR >%s<\n",
        // 		      sSubKey); fflush (stdout);
        if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, sSubKey, 0, KEY_READ, &hKey) != ERROR_SUCCESS) {
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            char msg[1024];
            strcpy(msg, "RegOpenKeyEx() failed for ");
            strcat(msg, sSubKey);
            strcat(msg, ".");
            env->ThrowNew(excClass, msg);
            return NULL;
        }

        // query value
        if (RegQueryValueEx(hKey, NULL, NULL, &type, (LPBYTE) sReg, &len) != ERROR_SUCCESS) {
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            env->ThrowNew(excClass, "RegQueryValueEx() failed.");
            return NULL;
        }

        // process data
        switch (type) {
            case REG_EXPAND_SZ:
                // expand variables
                if (!ExpandEnvironmentStrings(sReg, sPath, 1024)) {
                    jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                    env->ThrowNew(excClass, "ExpandEnvironmentStrings() failed.");
                    return NULL;
                }
                break;
            case REG_SZ:
                strcpy(sPath, sReg);
                break;
            default:
                jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                env->ThrowNew(excClass, "Unsupported registry type.");
                return NULL;
        }

        if (RegCloseKey(hKey) != ERROR_SUCCESS) {
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            env->ThrowNew(excClass, "RegCloseKey() failed.");
            return NULL;
        }

        if ((path = env->NewStringUTF(sPath)) == NULL) {
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            env->ThrowNew(excClass, "Error when retrieving path to browser.");
            return NULL;
        }

        return path;
    }

    /**********************************************************
     * function 
     * desc	 
     * 
     * param
     * returns
     * throws  
     *********************************************************/
    JNIEXPORT jstring Java_org_netbeans_modules_extbrowser_NbDdeBrowserImpl_getDefaultOpenCommand
    (JNIEnv *env,
            jclass cls) {
        DWORD len = 1024;
        DWORD type;
        // char sSubKey[1024];
        wchar_t sBrowser[1024];
        wchar_t sReg[1024];
        wchar_t sPath[1024];
        HKEY hKey = NULL;
        jstring path = NULL;

        // find HKEY_CLASSES_ROOT ".html"
        sBrowser[0] = '\0';

        // open registry key
        if (RegOpenKeyExW(HKEY_CLASSES_ROOT, L".html", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
            // query value
            if (RegQueryValueExW(hKey, NULL, NULL, &type, (LPBYTE) sReg, &len) == ERROR_SUCCESS) {
                // process data
                switch (type) {
                    case REG_SZ:
                        wcscpy(sBrowser, sReg);
                        break;
                    default:
                        jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                        env->ThrowNew(excClass, "Unsupported registry type.");
                        return NULL;
                }

                if (RegCloseKey(hKey) != ERROR_SUCCESS) {
                    jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                    env->ThrowNew(excClass, "RegCloseKey() failed.");
                    return NULL;
                }
            } else {
                jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                env->ThrowNew(excClass, "RegQueryValueEx() failed.");
                return NULL;
            }
        } else {
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            env->ThrowNew(excClass, "RegOpenKeyEx() failed for .html.");
            return NULL;
        }

        if (wcslen(sBrowser) == 0) {
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            env->ThrowNew(excClass, "Browser not found.");
            return NULL;
        }

        wcscat(sBrowser, L"\\shell\\open\\command");

        // now query real command
        // open registry key
        if (RegOpenKeyExW(HKEY_CLASSES_ROOT, sBrowser, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
            // query value
            len = 1024;
            sReg [0] = '\0';
            if (RegQueryValueExW(hKey, NULL, NULL, &type, (BYTE *) & sReg, &len) == ERROR_SUCCESS) {
                // process data
                switch (type) {
                    case REG_EXPAND_SZ:
                        // expand variables
                        if (!ExpandEnvironmentStringsW(sReg, sPath, 1024)) {
                            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                            env->ThrowNew(excClass, "ExpandEnvironmentStrings() failed.");
                            return NULL;
                        }
                        break;
                    case REG_SZ:
                        wcscpy(sPath, sReg);
                        break;
                    default:
                        jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                        env->ThrowNew(excClass, "Unsupported registry type.");
                        return NULL;
                }

                if (RegCloseKey(hKey) != ERROR_SUCCESS) {
                    jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                    env->ThrowNew(excClass, "RegCloseKey() failed.");
                    return NULL;
                }

            } else {
                jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
                env->ThrowNew(excClass, "RegQueryValueEx() failed.");
                return NULL;
            }

        } else {
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            env->ThrowNew(excClass, "RegOpenKeyEx() failed when trying to open 'browser' key.");
            return NULL;
        }

        // sPath contains command that starts default browser
        if ((path = env->NewString((jchar*) sPath, wcslen(sPath))) == NULL) {
            jclass excClass = env->FindClass("org/netbeans/modules/extbrowser/NbBrowserException");
            env->ThrowNew(excClass, "Error when retrieving path to browser.");
            return NULL;
        }

        return path;
    }

} // extern "C"
