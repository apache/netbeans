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

package org.netbeans.lib.profiler.utils;


/**
 *
 * @author Jaroslav Bachorik
 */
public class Wildcards {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final String ALLWILDCARD = "*"; // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static boolean matchesWildcard(String wildcard, String loadedClassName) {
        //    System.err.println("Matches wildcard: "+loadedClassName+", wild: "+wildcard + " : " + (loadedClassName.startsWith(wildcard) && (loadedClassName.indexOf('/', wildcard.length()) == -1)));
        boolean packageWildcard = false;
        if (wildcard.endsWith(Wildcards.ALLWILDCARD)) { // package wild card - instrument all classes including subpackages
            wildcard = Wildcards.unwildPackage(wildcard);
            packageWildcard = true;
        }
        if (!loadedClassName.startsWith(wildcard)) {
            if (packageWildcard && loadedClassName.equals(wildcard.substring(0,wildcard.length()-1))) {
                return true;
            }
            return false;
        }
        return packageWildcard || (loadedClassName.indexOf('/', wildcard.length()) == -1); // NOI18N
    }


    public static boolean isMethodWildcard(String methodName) {
        return (methodName != null) ? (methodName.equals(ALLWILDCARD) || methodName.equals("<all>")) : false; // NOI18N
    }

    public static boolean isPackageWildcard(String className) {
        if (className == null) {
            return false;
        }

        return (className.length() == 0 // empty string is default package wildcard
        ) || className.endsWith("/") // ends with '/', means package wildcard // NOI18N
               || className.endsWith(".") // ends with '.', means package wildcard // NOI18N
               || className.endsWith(ALLWILDCARD); // ends with the default WILDCARD (*)
    }

    public static String unwildPackage(String packageMask) {
        if (packageMask == null) {
            return null;
        }

        //    System.out.print("Performing unwildPackage() : " + packageMask);
        if (packageMask.endsWith(ALLWILDCARD)) {
            //      String newPk = packageMask.substring(0, packageMask.length() - 2);
            //      System.out.println(" -> " + newPk);
            return packageMask.substring(0, packageMask.length() - 1).intern();
        }

        return packageMask.intern();
    }
}
