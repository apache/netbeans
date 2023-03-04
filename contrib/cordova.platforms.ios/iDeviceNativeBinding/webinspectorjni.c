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



