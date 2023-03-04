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

package org.netbeans.modules.diff;

import java.io.File;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
public class Utils {

    private static RequestProcessor parallelRP;

    public static Task createParallelTask (Runnable runnable) {
        RequestProcessor rp = getParallelRequestProcessor();
        return rp.create(runnable);
    }

    public static Task postParallel (Runnable runnable) {
        RequestProcessor rp = getParallelRequestProcessor();
        return rp.post(runnable);
    }

    /**
     * Opens a file in the editor area.
     *
     * @param file a File to open
     */
    public static void openFile (File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                OpenCookie oc = dao.getCookie(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                }
            } catch (DataObjectNotFoundException e) {
                // nonexistent DO, do nothing
            }
        }
    }

    private static RequestProcessor getParallelRequestProcessor() {
        if (parallelRP == null) {
            parallelRP = new RequestProcessor("Diff.ParallelTasks", 5); //NOI18N
        }
        return parallelRP;
    }

    /**
     * Checks if the fo is binary.
     *
     * @param fo fileobject to check
     * @return true if the fileobject cannot be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentBinary (FileObject fo) {
        if (fo == null) return false;
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getCookie(EditorCookie.class) == null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }

    private Utils () {
        
    }
}
