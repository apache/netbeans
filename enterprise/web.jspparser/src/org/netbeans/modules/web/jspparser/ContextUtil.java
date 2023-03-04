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

package org.netbeans.modules.web.jspparser;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Static utilities related to web context stuff - relative paths, relative objects etc.
 * @author Petr Jiricka
 */
public final class ContextUtil {

    private ContextUtil() {
    }

    /** Returns a message for a given throwable. Optionally includes the throwable
     * stack trace in the message.
     * @param throwable throwable for which to construct the message
     * @param includeStackTrace whether to include a stack trace of the throwable in the message
     * @return an appropriate message for the throwable
     */
    public static String getThrowableMessage(Throwable throwable,
            boolean includeStackTrace) {
        if (includeStackTrace) {
            StringWriter swriter = new StringWriter();
            PrintWriter pw = new PrintWriter(swriter);
            throwable.printStackTrace(pw);
            pw.close();
            return swriter.toString();
        }
        return throwable.getMessage();
    }
    
    /**********************************
     * Copied over from WebModuleUtils.
     **********************************
     */
    
    /** Decides whether a given file is in the subtree defined by the given folder.
     * Similar to <code>org.openide.filesystems.FileUtil.isParentOf (FileObject folder, FileObject fo)</code>,
     * but also accepts the case that <code>fo == folder</code>
     */
    public static boolean isInSubTree(FileObject folder, FileObject fo) {
        if (fo == folder) {
            return true;
        }
        return FileUtil.isParentOf(folder, fo);
    }

    /** Finds a relative resource path between rootFolder and relativeObject. 
     * @return relative path between rootFolder and relativeObject. The returned path
     * never starts with a '/'. It never ends with a '/'.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static String findRelativePath(FileObject rootFolder, FileObject relativeObject) {
        String rfp = rootFolder.getPath();
        String rop = relativeObject.getPath();
        // check that they share the start of the path 
        if (!isInSubTree(rootFolder, relativeObject)) {
            // #146242 - remove debug messages when issue is solved
            String message = relativeObject + " not under " + rootFolder + "\n";  //NOI18N
            FileSystem fs = null;
            try {
                fs = rootFolder.getFileSystem();
            } catch (FileStateInvalidException ex) {
                fs = null;
            }
            message += rootFolder + " valid=" + rootFolder.isValid() + " id=" + System.identityHashCode(rootFolder) + " filesystem=" + fs + "\n";  //NOI18N
            try {
                fs = relativeObject.getFileSystem();
            } catch (FileStateInvalidException ex) {
                fs = null;
            }
            message += relativeObject + " valid=" + relativeObject.isValid() + " id=" + System.identityHashCode(relativeObject) + " filesystem=" + fs + "\n";  //NOI18N
            FileObject parent = relativeObject.getParent();
            while (parent != null && !rfp.equals(parent.getPath())) {
                try {
                    fs = parent.getFileSystem();
                } catch (FileStateInvalidException ex) {
                    fs = null;
                }
                message += parent + " valid=" + parent.isValid() + " id=" + System.identityHashCode(parent) + " filesystem=" + fs + "\n";  //NOI18N
                parent = parent.getParent();
            }
            if (parent == null) {
                fs = null;
            } else {
                try {
                    fs = parent.getFileSystem();
                } catch (FileStateInvalidException ex) {
                    fs = null;
                }
            }
            String valid = parent != null ? String.valueOf(parent.isValid()) : "null"; // NOI18N
            message += parent + " valid=" + valid + " id=" + System.identityHashCode(parent) + " filesystem=" + fs + "\n";  //NOI18N
            throw new IllegalArgumentException(message);
            //throw new IllegalArgumentException("" + rootFolder + " / " + relativeObject); // NOI18N
        }
        // now really return the result
        String result = rop.substring(rfp.length());
        if (result.startsWith("/")) { // NOI18N
            result = result.substring(1);
        }
        return result;
    }
    
    /** Finds a relative context path between rootFolder and relativeObject. 
     * Similar to <code>findRelativePath(FileObject, FileObject)</code>, only 
     * different slash '/' conventions.
     * @return relative context path between rootFolder and relativeObject. The returned path
     * always starts with a '/'. It ends with a '/' if the relative object is a directory.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static String findRelativeContextPath(FileObject rootFolder, FileObject relativeObject) {
        String result = "/" + findRelativePath(rootFolder, relativeObject); // NOI18N
        return relativeObject.isFolder() ? (result + "/") : result; // NOI18N
    }
}
