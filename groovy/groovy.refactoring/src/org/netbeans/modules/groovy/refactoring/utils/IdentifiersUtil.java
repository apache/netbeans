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

package org.netbeans.modules.groovy.refactoring.utils;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * This class consists of static utility methods for working
 * with Groovy identifiers.
 *
 * @author Erno Mononen
 * @author Martin Janicek
 */
public final class IdentifiersUtil {

    private IdentifiersUtil(){
    }

    /**
     * Gets the new refactored name for the given <code>file</code>.
     *
     * @param file the file object for the class being renamed. Excepts that
     * the target class is the public top level class in the file.
     * @param rename the refactoring, must represent either package or folder rename.
     *
     * @return the new fully qualified name for the class being refactored.
     */
    public static String constructNewName(FileObject file, RenameRefactoring rename){
        Parameters.notNull("file", file); // NOI18N
        Parameters.notNull("rename", rename); // NOI18N

        String fqn = getQualifiedName(file);

        if (isPackageRename(rename)){
            return rename.getNewName() + "." + unqualify(fqn);
        }

        final FileObject folder = rename.getRefactoringSource().lookup(FileObject.class);
        final ClassPath classPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (classPath == null) {
            return "Cannot construct new name!"; //NOI18N
        }

        final FileObject root = classPath.findOwnerRoot(folder);

        String prefix = FileUtil.getRelativePath(root, folder.getParent()).replace('/','.'); // NOI18N
        String oldName = buildName(prefix, folder.getName());
        String newName = buildName(prefix, rename.getNewName());
        int oldNameIndex = fqn.lastIndexOf(oldName) + oldName.length();
        return newName + fqn.substring(oldNameIndex);

    }

    /**
     * Finds out whether the given <code>RenameRefactoring</code> is a package
     * rename or any other rename refactoring.
     *
     * @param rename the rename refactoring
     * @return true if it's package rename refactoring, false otherwise
     */
    public static boolean isPackageRename(RenameRefactoring rename){
        return rename.getRefactoringSource().lookup(NonRecursiveFolder.class) != null;
    }

    private static String buildName(String prefix, String name){
        if (prefix.length() == 0){
            return name;
        }
        return prefix + "." + name; // NOI18N
    }

    /**
     * Checks whether the given <code>packageName</code> represents a
     * valid name for a package.
     *
     * @param packageName the package name to check; must not be null.
     * @return true if the given <code>packageName</code> is a valid package
     * name, false otherwise.
     */
    public static boolean isValidPackageName(String packageName) {
        Parameters.notNull("packageName", packageName); // NOI18N

        if ("".equals(packageName)) { // NOI18N
            return true;
        }
        if (packageName.startsWith(".") || packageName.endsWith(".")) { // NOI18N
            return false;
        }

        String[] tokens = packageName.split("\\."); // NOI18N
        if (tokens.length == 0) {
            return Utilities.isJavaIdentifier(packageName);
        }
        for(String token : tokens) {
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns only the package name for the given <code>FileObject</code>.
     * This is actually just a combination of these two methods: <br/><br/>
     *
     * 1. {@link #getQualifiedName(org.openide.filesystems.FileObject) }<br/>
     * 2. {@link #unqualify(java.lang.String) }<br/>
     *
     * @param fo file for which we want to get package name
     * @return package name for the given file or empty string if not possible
     */
    public static String getPackageName(final FileObject fo) {
        final String fqn = getQualifiedName(fo);
        if (fqn != null) {
            return unqualify(fqn);
        }
        return ""; // NOI18N
    }

    /**
     * Gets the fully qualified name for the given <code>fileObject</code>. If it
     * represents a java package, returns the name of the package (with dots as separators).
     *
     * @param fo the file object whose FQN is to be get; must not be null.
     * @return the FQN for the given file object or null.
     */
    public static String getQualifiedName(final FileObject fo){
        Parameters.notNull("fileObject", fo); // NOI18N
        ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (classPath != null) {
            return classPath.getResourceName(fo, '.', false);
        }
        return null;
    }

    /**
     * Unqualifies the given <code>fqn</code>.
     *
     * @param fqn the fully qualified name unqualify. Must not be null or empty
     * and must represent a valid fully qualified name.
     * @return the unqualified name.
     * @throws IllegalArgumentException if the given <code>fqn</code> was not
     * a valid fully qualified name.
     */
    public static String unqualify(String fqn){
        checkFQN(fqn);
        int lastDot = fqn.lastIndexOf("."); //NOI18N
        if (lastDot < 0){
            return fqn;
        }
        return fqn.substring(lastDot + 1);
    }

    /**
     * Gets the package name of the given fully qualified class name.
     *
     * @param fqn the fully qualified class name. Must not be null or empty
     * and must represent a valid fully qualified name.
     * @return the name of the package, an empty string if there was no package.
     * @throws IllegalArgumentException if the given <code>fqn</code> was not
     * a valid fully qualified name.
     */
    public static String getPackageName(String fqn) {
        checkFQN(fqn);
        int lastDot = fqn.lastIndexOf("."); // NOI18N
        if (lastDot < 0){
            return "";
        }
        return fqn.substring(0, lastDot);
    }

    private static void checkFQN(String fqn){
        Parameters.notEmpty("fqn", fqn); //NOI18N
        if (!isValidPackageName(fqn)){
            throw new IllegalArgumentException("The given fqn [" + fqn + "] does not represent a fully qualified class name"); //NOI18N
        }
    }
}
