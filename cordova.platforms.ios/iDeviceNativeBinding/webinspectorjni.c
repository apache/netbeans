/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libimobiledevice/libimobiledevice.h>
#include <libimobiledevice/webinspector.h>

static webinspector_client_t client = NULL;
static idevice_t device = NULL;

JNIEXPORT void JNICALL Java_org_netbeans_modules_cordova_platforms_ios_WebInspectorJNIBinding_nstart(JNIEnv * env, jobject thiz) {
    if (IDEVICE_E_SUCCESS != idevice_new(&device, NULL)) {
        throwException(env, "No device found");
        return;
    }

    webinspector_error_t error = webinspector_client_start_service(device, &client, "webinspector");
    if (error != WEBINSPECTOR_E_SUCCESS) {
        throwException(env, "Could not connect to the webinspector", error);
        return;
    }

    return;
}

JNIEXPORT jboolean JNICALL Java_org_netbeans_modules_cordova_platforms_ios_WebInspectorJNIBinding_nisDeviceConnected(JNIEnv * env, jobject thiz) {
    idevice_t dev = NULL;
    
    if (IDEVICE_E_SUCCESS != idevice_new(&dev, NULL)) {
        return JNI_FALSE;
    }
    if (dev != NULL) {
        idevice_free(dev);
        dev = NULL;
    }
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_org_netbeans_modules_cordova_platforms_ios_WebInspectorJNIBinding_nstop(JNIEnv * env, jobject thiz) {
    if (client != NULL) {
        webinspector_client_free(client);
        client = NULL;
    }
    if (device != NULL) {
        idevice_free(device);
        device = NULL;
    }
    return;
}

JNIEXPORT jstring JNICALL Java_org_netbeans_modules_cordova_platforms_ios_WebInspectorJNIBinding_nreceiveMessage(JNIEnv * env, jobject thiz, jint timeout_ms) {
    plist_t plist = NULL;

    webinspector_error_t res = webinspector_receive_with_timeout(client, &plist, timeout_ms);
    if (res != WEBINSPECTOR_E_SUCCESS || plist == NULL) {
        char str[100];
        sprintf(str, "Error receiving message: %d", res);
        //throwException(env, str);
        if (plist != NULL) {
            plist_free(plist);
        }
        return NULL;
    }

    char * xml = NULL;
    int xmllength = 0;
    plist_to_xml(plist, &xml, &xmllength);
    plist_free(plist);

    if (xml == NULL) {
        throwException(env, "Error converting plist to xml.");
        return NULL;
    }

    jstring retval = (*env)->NewStringUTF(env, xml);
    free(xml);

    return retval;
}

JNIEXPORT void JNICALL Java_org_netbeans_modules_cordova_platforms_ios_WebInspectorJNIBinding_nsendMessage(JNIEnv * env, jobject thiz, jstring command) {
    char const * const xml = (*env)->GetStringUTFChars(env, command, 0);
    int const xmllength = strlen(xml);

    plist_t plist = NULL;
    plist_from_xml(xml, xmllength, &plist);
    if (!plist) {
        throwException(env, "Failed to create plist from xml.");
        return;
    }

    webinspector_send(client, plist);
    plist_free(plist);

    (*env)->ReleaseStringUTFChars(env, command, xml);
    return;
}

jint throwException(JNIEnv *env, char *message) {
    jclass exClass;
    char *className = "java/lang/IllegalStateException";

    exClass = (*env)->FindClass(env, className);
    return (*env)->ThrowNew(env, exClass, message);
}



