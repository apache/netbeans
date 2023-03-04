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

package org.netbeans.modules.java.platform.util;

/** Utility class to woth with file names. */
public class FileNameUtil {

    /** Return absolute path computed from home path and relative path.
     * @param home home directory
     * @param relative the relative part of the file path
     * @return computed absolute path
     */
    public static String computeAbsolutePath(String home, String relative) {
        String path;
        String separator = System.getProperty("file.separator");
        if (home.endsWith("\\") || home.endsWith("/")) // NOI18N
            home = home.substring(0, home.length() - 1);
        if (relative.startsWith("\\") || relative.startsWith("/")) // NOI18N
            relative = relative.substring(1);
        
        path = home + separator + relative;
        if (separator.equals("/")) // NOI18N
            path = path.replace('\\', separator.charAt(0));
        else if (separator.equals("\\")) // NOI18N
            path = path.replace('/', separator.charAt(0));
        
        return path;
    }
}
