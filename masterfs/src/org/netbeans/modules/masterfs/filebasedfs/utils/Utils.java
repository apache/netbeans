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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
