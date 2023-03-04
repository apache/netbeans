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

#include <jvmpi.h>
#include <string.h>
#include <stdlib.h>

static JavaVM *jvm;
static JNIEnv *env;
static JVMPI_Interface *jvmpi;

extern "C" JNIEXPORT jint JNICALL JVM_OnLoad(JavaVM *jvm, char *options, void *reserved);
static void notifyEvent(JVMPI_Event *event);

static int total = 0;

const char * traced_methods[] = {
    // java.beans.PropertyChangeListener
    "propertyChange", "(Ljava/beans/PropertyChangeEvent;)V",

    // java.beans.VetoableChangeListener
    "vetoableChange", "(Ljava/beans/PropertyChangeEvent;)V",

    // javax.swing.event.ChangeListener
    "stateChanged", "(Ljavax/swing/event/ChangeEvent;)V",

    // javax.swing.event.DocumentListener
    "insertUpdate", "(Ljavax/swing/event/DocumentEvent;)V",
    "removeUpdate", "(Ljavax/swing/event/DocumentEvent;)V",
    "changedUpdate", "(Ljavax/swing/event/DocumentEvent;)V",

    // javax.swing.event.UndoableEditListener
    "undoableEditHappened", "(Ljavax/swing/event/UndoableEditEvent;)V",

    
    // org.netbeans.core.awt.BuButtonBar$ButtonBarListener
    "buttonPressed", "(L(org/netbeans/core/awt/ButtonBar$ButtonBarEvent;)V",


    // org.openide.util.datatransfer.ClipboardListener
    "clipboardChanged", "(Lorg/openide/util/datatransfer/ClipboardEvent;)V",

    // org.openide.compiler.CompilerListener
    "compilerProgress", "(Lorg.openide.compiler/ProgressEvent;)V",
    "compilerError", "(Lorg.openide.compiler.ErrorEvent;)V",

    // org.netbeans.core.execution.ExecutionListener
    "startedExecution", "(Lorg/netbeans/core/execution/ExecutionEvent;)V",
    "finishedExecution", "(Lorg/netbeans/core/execution/ExecutionEvent;)V",

    // org.openide.filesystems.FileChangeListener
    "fileFolderCreated", "(Lorg/openide/filesystems/FileEvent;)V",
    "fileDataCreated", "(Lorg/openide/filesystems/FileEvent;)V",
    "fileChanged", "(Lorg/openide/filesystems/FileEvent;)V",
    "fileDeleted", "(Lorg/openide/filesystems/FileEvent;)V",
    "fileRenamed", "(Lorg/openide/filesystems/FileRenameEvent;)V",
    "fileAttributeChanged",  "(Lorg/openide/filesystems/FileAttributeEvent;)V"

    //org.netbeans.core.projects.FileStateManager$FileStatusListener
    "fileStatusChanged", "(Lorg/openide/filesystems/FileObject;)V",

    // org.openide.filesystems.FileStatusListener
    "annotationChanged", "(Lorg/openide/filesystems/FileStatusEvent;)V",
    
    // org.netbeans.core.windows.frames.FrameTypeListener
    "frameDeactivated", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",
    "frameClosed", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",
    "frameDeiconified", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",
    "frameNormalized", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",
    "frameOpened", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",
    "frameIconified", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",
    "frameClosing", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",
    "frameActivated", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",
    "frameMaximized", "(Lorg/netbeans/core/windows/frames/FrameTypeEvent;)V",

    // org.openide.util.LookupListener
    "resultChanged", "(Lorg/openide/util/LookupEvent;)V",

    // org.openide.nodes.NodeListener
    "childrenAdded", "(Lorg/openide/nodes/NodeMemberEvent;)V",
    "childrenRemoved", "(Lorg/openide/nodes/NodeMemberEvent;)V",
    "childrenReordered", "(Lorg/openide/nodes/NodeReorderEvent;)V",
    "nodeDestroyed", "(Lorg/openide/nodes/NodeEvent;)V",

    // org.openide.loaders.OperationListener
    "operationPostCreate", "(Lorg/openide/loaders/OperationEvent;)V",
    "operationCopy", "(Lorg/openide/loaders/OperationEvent$Copy;)V",
    "operationMove", "(Lorg/openide/loaders/OperationEvent$Move;)V",
    "operationDelete", "(Lorg/openide/loaders/OperationEvent;)V",
    "operationRename", "(Lorg/openide/loaders/OperationEvent$Rename;)V",
    "operationCreateShadow", "(Lorg/openide/loaders/OperationEvent$Copy;)V",
    "operationCreateFromTemplate", "(Lorg/openide/loaders/OperationEvent$Copy;)V",

    // org.openide.filesystems.RepositoryListener
    "fileSystemAdded", "(Lorg/openide/filesystems/RepositoryEvent;)V",
    "fileSystemRemoved", "(Lorg/openide/filesystems/RepositoryEvent;)V",
    "fileSystemPoolReordered", "(Lorg/openide/filesystems/RepositoryReorderedEvent;)V",

    // org.openide.util.TaskListener
    "taskFinished", "(Lorg/openide/util/Task;)V",

    // org.netbeans.core.windows.TopComponentListener
    "topComponentActivated", "(Lorg/netbeans/core/windows/TopComponentChangedEvent;)V",
    "topComponentOpened", "(Lorg/netbeans/core/windows/TopComponentChangedEvent;)V",
    "topComponentClosed", "(Lorg/netbeans/core/windows/TopComponentChangedEvent;)V",
    "selectedNodesChanged", "(Lorg/netbeans/core/windows/SelectedNodesChangedEvent;)V",

    // org.openide.util.datatransfer.TransferListener
    "accepted", "(I)V",
    "rejected", "(V)V"
    "ownershipLost", "(V)V",
    NULL };

typedef struct {
    jmethodID method_id;
    const char *class_name;
    const char *method_name;
    const char *method_sig;
    int counter;
} mentry_t;

static mentry_t* allMethods[1024 * 16];
static int allMethodsLength = 0;

mentry_t* lookupMethodIDHelper(jmethodID mid, int start, int end) {
    if (start >= end)
        return NULL;

    int pivot = (start + end) / 2;
    
    if (mid == allMethods[pivot]->method_id)
        return allMethods[pivot];
    
    mentry_t* e = lookupMethodIDHelper(mid, start, pivot - 1);
    if (e != NULL)
        return e;
    else
        return lookupMethodIDHelper(mid, pivot + 1, end);
}

mentry_t* lookupMethodID(jmethodID mid) {
    return lookupMethodIDHelper(mid, 0, allMethodsLength);
}

void storeNewMethodID(jmethodID mid, const char* classname, const char* mname, const char* msig) {
    if (allMethodsLength >= (sizeof allMethods / sizeof allMethods[0])) {
        fprintf(stderr, "jtrace> allMethods buffer exceeded\n");
        exit(1);
    }
        
    mentry_t* e = new mentry_t;
    e->method_id = mid;
    e->class_name = strdup(classname);
    e->method_name = strdup(mname);
    e->method_sig = strdup(msig);
    e->counter = 0;

    int i = 0;
    while (i < allMethodsLength && allMethods[i]->method_id < mid) {
        i++;
    }
    int j = allMethodsLength;
    while (j > i) {
        allMethods[j] = allMethods[j-1];
        j--;
    }
    allMethods[i] = e;
    allMethodsLength++;
}

JNIEXPORT jint JNICALL JVM_OnLoad(JavaVM *aJvm, char *options, void *reserved) {
    fprintf(stderr, "jtrace> %s\n", "initializing .....");

    jvm = aJvm;

    if (jvm->GetEnv((void**)&env, JNI_VERSION_1_2)) {
        fprintf(stderr, "jtrace> %s\n", "error in obtaining JNI interface pointer");
        return JNI_ERR;
    }
    
    if ((jvm->GetEnv((void **)&jvmpi, JVMPI_VERSION_1)) < 0) {
        fprintf(stderr, "jtrace> %s\n", "error in obtaining JVMPI interface pointer");
        return JNI_ERR;
    }

    jvmpi->NotifyEvent = notifyEvent;
    
    jvmpi->EnableEvent(JVMPI_EVENT_CLASS_LOAD, NULL);
    jvmpi->EnableEvent(JVMPI_EVENT_METHOD_ENTRY2, NULL);
    jvmpi->EnableEvent(JVMPI_EVENT_METHOD_EXIT, NULL);

    fprintf(stderr, "jtrace> %s\n", ".... ok\n");
    return JNI_OK;
}

void notifyEvent(JVMPI_Event *event) {
    switch (event->event_type) {
        case JVMPI_EVENT_CLASS_LOAD:
            {
//            fprintf(stderr, "jtrace> loaded %s\n", event->u.class_load.class_name);
                for (int i = 0; i < event->u.class_load.num_methods; i++) {
                    JVMPI_Method *m = & event->u.class_load.methods[i];

//                fprintf(stderr, "jtrace>   %s%s\n", m->method_name, m->method_signature);

                    for (const char **p = traced_methods; *p != NULL; p += 2) {
                        if (0 == strcmp(m->method_name, *p) && 0 == strcmp(m->method_signature, *(p+1))) {
                            storeNewMethodID(m->method_id, event->u.class_load.class_name,
                                             m->method_name, m->method_signature);
                            break;
                        }
                    }
                }
            }
            break;

        case JVMPI_EVENT_METHOD_ENTRY2:
            {
                jmethodID mid = event->u.method_entry2.method_id;
                jobjectID obj = event->u.method_entry2.obj_id;
                
                mentry_t* e = lookupMethodID(mid);
//                 if (e == NULL) {
//                     jobjectID classid = jvmpi->GetMethodClass(mid);
//                     jint res = jvmpi->RequestEvent(JVMPI_EVENT_CLASS_LOAD, classid);
//                     if (res != JVMPI_SUCCESS) {
//                         // warning
//                     }
//                     e = lookupMethodID(mid);
//                 }
                if (e != NULL) {
                    fprintf(stderr, "jtrace> [%5d,%5d] <<< ENTERED %s.%s%s\n", (total++), (e->counter++), e->class_name, e->method_name, e->method_sig);
                }
            }
            break;

        case JVMPI_EVENT_METHOD_EXIT:
            {
                jmethodID mid = event->u.method.method_id;
                mentry_t* e = lookupMethodID(mid);
                if (e != NULL) {
                    fprintf(stderr, "jtrace> [%5d,%5d] >>> EXITED  %s.%s%s\n", (total), (e->counter), e->class_name, e->method_name, e->method_sig);
                }
            }
            break;
    }
}
