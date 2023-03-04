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

package org.netbeans.modules.openfile;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Opens files when requested. Main functionality.
 * @author Jaroslav Tulach, Jesse Glick
 * @author Marian Petras
 */
public final class OpenFile {

    /** do not instantiate */
    private OpenFile() {}

    /**
     * Open a file (object) at the beginning.
     * @param fileObject the file to open
     * @param line 
     * @return error message or null on success
     * @usecase  API
     */
    public static String open(FileObject fileObject, int line) {
        for (OpenFileImpl impl : Lookup.getDefault().lookupAll(OpenFileImpl.class)) {
            if (impl.open(fileObject, line)) {
                return null;
            }
        }
        return NbBundle.getMessage(OpenFile.class, "MSG_FileIsNotPlainFile", fileObject);
    }
    
    /**
     * Opens a file.
     *
     * @param  file  file to open (must exist)
     * @param  line  line number to try to open to (starting at zero),
     *               or <code>-1</code> to ignore
     * @return null on success, otherwise the error message
     * @usecase CallbackImpl, OpenFileAction
     */
    static String openFile(File file, int line) {
        String msg = checkFileExists(file);
        if (msg != null) {
            return msg;
        }
                              
        FileObject fileObject;
        fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject != null) {
            return open(fileObject, line);
        }
        return NbBundle.getMessage(OpenFile.class, "MSG_FileDoesNotExist", file);
    }
    
    /**
     * Checks whether the specified file exists.
     * If the file doesn't exists, displays a message.
     * <p>
     * The code for displaying the message is running in a separate thread
     * so that it does not block the current thread.
     *
     * @param  file  file to check for existence
     * @return  null on success, otherwise the error message
     */
    private static String checkFileExists(File file) {
        if (!file.exists() || (!file.isFile() && !file.isDirectory())) {
            return NbBundle.getMessage(OpenFile.class, "MSG_fileNotFound", file.toString());  //NOI18N
        } else {
            return null;
        }
    }
}
