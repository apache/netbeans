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
package org.netbeans.modules.masterfs.filebasedfs.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Stack;
import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;

/**
 *
 * @author rmatous
 */
public class Utils {
    private static final Boolean SENSITIVE = findCase();
    private static Boolean findCase() {
        String userDef = System.getProperty("org.netbeans.modules.masterfs.case"); // NOI18N
        if ("insensitive".equals(userDef)) { // NOI18N
            return false;
        } 
        if ("sensitive".equals(userDef)) { // NOI18N
            return true;
        }
        assert userDef == null : "Wrong value " + userDef;
        if (BaseUtilities.isMac()) {
            return false;
        }
        return null;
    }
    
    public static boolean equals(File f1, File f2) {
        if (f1 == null) {
            return f2 == null;
        }
        if (f2 == null) {
            return f1 == null;
        }
        if (SENSITIVE == null) {
            return f1.equals(f2);
        }
        if (SENSITIVE) {
            // same as in UnixFileSystem
            return f1.getPath().compareTo(f2.getPath()) == 0;
        } else {
            // same as in Win32FileSystem
            return f1.getPath().compareToIgnoreCase(f2.getPath()) == 0;
        }
    }
    public static int hashCode(final File file) {
        if (SENSITIVE == null) {
            return file.hashCode();
        }
        if (SENSITIVE) {
            // same as in UnixFileSystem
            return file.getPath().hashCode() ^ 1234321;
        } else {
            // same as in Win32FileSystem
            return file.getPath().toLowerCase(Locale.ENGLISH).hashCode() ^ 1234321;
        }
    }
    
    public static String getRelativePath(final File dir, final File file) {
        Stack<String> stack = new Stack<String>();
        File tempFile = file;
        while (tempFile != null && !equals(tempFile, dir)) {
            stack.push(tempFile.getName());
            tempFile = tempFile.getParentFile();
        }
        if (tempFile == null) {
            return null;
        }
        StringBuilder retval = new StringBuilder();
        while (!stack.isEmpty()) {
            retval.append(stack.pop());
            if (!stack.isEmpty()) {
                retval.append("/");//NOI18N
            }
        }
        return retval.toString();
    }

    public static void reassignLkp(FileObject from, FileObject to) {
        try {
            Class<?> c = Class.forName("org.openide.filesystems.FileObjectLkp");
            Method m = c.getDeclaredMethod("reassign", FileObject.class, FileObject.class);
            m.setAccessible(true);
            m.invoke(null, from, to);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            if (ex.getCause() instanceof Error) {
                throw (Error) ex.getCause();
            }
            throw new IllegalStateException(ex);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
