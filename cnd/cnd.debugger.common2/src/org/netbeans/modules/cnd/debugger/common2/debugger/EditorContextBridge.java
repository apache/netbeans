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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class EditorContextBridge {
    private static final EditorContextDispatcher contextDispatcher = EditorContextDispatcher.getDefault();

    public static String getCurrentURL() {
        return contextDispatcher.getCurrentURLAsString();
    }

    public static FileObject getCurrentFileObject() {
        return contextDispatcher.getCurrentFile();
    }

    public static String getCurrentFilePath() {
        FileObject currentFile = contextDispatcher.getCurrentFile();
        if (currentFile != null) {
            return currentFile.getPath();
        }
        return "";
    }
    
    public static String getMostRecentFilePath() {
        FileObject currentFile = getMostRecentFileObject();
        if (currentFile != null) {
            return currentFile.getPath();
        }
        return "";
    }

    public static FileObject getMostRecentFileObject() {
        return contextDispatcher.getMostRecentFile();
    }

    public static int getCurrentLineNumber() {
        return contextDispatcher.getCurrentLineNumber();
    }
    
    public static int getMostRecentLineNumber() {
        return contextDispatcher.getMostRecentLineNumber();
    }
    
    public static String getCurrentMIMEType() {
        FileObject fo = contextDispatcher.getCurrentFile();
        return fo != null ? fo.getMIMEType() : ""; // NOI18N
    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        contextDispatcher.addPropertyChangeListener(MIMENames.C_MIME_TYPE, l);
        contextDispatcher.addPropertyChangeListener(MIMENames.CPLUSPLUS_MIME_TYPE, l);
        contextDispatcher.addPropertyChangeListener(MIMENames.HEADER_MIME_TYPE, l);
        contextDispatcher.addPropertyChangeListener(MIMENames.ASM_MIME_TYPE, l);
        contextDispatcher.addPropertyChangeListener(MIMENames.FORTRAN_MIME_TYPE, l);
    }

}
