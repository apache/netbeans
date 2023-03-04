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
package org.netbeans.modules.j2ee.core.api.support.java;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * This class consists of static utility methods for working
 * with Java identifiers.
 * 
 * @author Erno Mononen
 */
public final class JavaIdentifiers {

    private JavaIdentifiers(){
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
        Parameters.notNull("packageName", packageName); //NOI18N

        if ("".equals(packageName)) {
            return true;
        }
        if (packageName.startsWith(".") || packageName.endsWith(".")) {// NOI18N
            return false;
        }
        if(packageName.equals("java") || packageName.startsWith("java.")) {//NOI18N
            return false;
        }

        String[] tokens = packageName.split("\\."); //NOI18N
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
     * Gets the fully qualified name for the given <code>fileObject</code>. If it
     * represents a java package, returns the name of the package (with dots as separators).
     *
     * @param fileObject the file object whose FQN is to be get; must not be null.
     * @return the FQN for the given file object or null.
     */
    public static String getQualifiedName(FileObject fileObject){
        Parameters.notNull("fileObject", fileObject); //NOI18N
        ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
        if (classPath != null) {
            return classPath.getResourceName(fileObject, '.', false);
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
