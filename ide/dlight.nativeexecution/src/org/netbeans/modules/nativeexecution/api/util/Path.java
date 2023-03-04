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

package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.openide.util.Utilities;

/**
 * Get path. Remembers additions to the path.
 *
 * @author gordonp
 */
public final class Path {
    
    private static ArrayList<String> list = new ArrayList<>();
    private static String pathName = null;
    
    static {
        String path = System.getenv("PATH"); // NOI18N
        if (Boolean.getBoolean("cnd.debug.use_altpath")) { // NOI18N
            // Its very hard to debug path problems on Windows because changing PATH is so hard. So these
            // properties let me do it without changing my real path
            path = System.getProperty("cnd.debug.altpath", path); // NOI18N
        }
        if (path != null) {
            StringTokenizer st = new StringTokenizer(path, File.pathSeparator); // NOI18N

            while (st.hasMoreTokens()) {
                String dir = st.nextToken();
                list.add(dir);
            }
        } else {
            if (Utilities.isUnix()) {
                list.add("/bin"); // NOI18N
                list.add("/usr/bin"); // NOI18N
                list.add("/sbin"); // NOI18N
                list.add("/usr/sbin"); // NOI18N
            } else if (Utilities.isWindows()) {
                list.add("C:/WINDOWS/System32"); // NOI18N
                list.add("C:/WINDOWS"); // NOI18N
                list.add("C:/WINDOWS/System32/WBem"); // NOI18N
            }
        }
        
    }
    
    private Path() {
    }

    /**
     * Read the PATH from the environment and make an array from it.
     * 
     * @return A list of all path directories
     */
    public static ArrayList<String> getPath() {
        return new ArrayList<>(list);
    }
    
    /**
     * Return the path with the correct path separator character.
     * This would be named toString() if it weren't a static method.
     * 
     * @return Path as a string (with OS specific directory separators)
     */
    public static String getPathAsString() {
        StringBuilder buf = new StringBuilder();
        
        for (String dir : list) {
            buf.append(dir);
            buf.append(File.pathSeparator);
        }
        return buf.substring(0, buf.length() - 1); // remove the trailing pathSeparator...
    }
    
    /**
     * This utility method makes it easier (on Windows) to replace PATH with one with
     * the same case. IZ 103016 updated PATH but it wasn't foud because Path wasn't
     * replaced. This will let us add a path using the exact same name.
     */
    public static String getPathName() {
        if (pathName == null) {
            if (Utilities.isWindows()) {
                for (String key : System.getenv().keySet()) {
                    if (key.equalsIgnoreCase("path")) { // NOI18N
                        pathName = key.substring(0, 4);
                        return pathName;
                    }
                }
            }
            pathName = "PATH"; // NOI18N
        }
        return pathName;
    }
    
    public static String findCommand(String cmd) {
        File file;
        String cmd2 = null;
        
        if (cmd.length() > 0) {
            if (Utilities.isWindows() && !cmd.endsWith(".exe")) { // NOI18N
                cmd2 = cmd + ".exe"; // NOI18N
            }

            for (String dir : list) {
                if (dir.equals(".")) { // NOI18N
                    file = new File(cmd);
                }
                else {
                    file = new File(dir, cmd);
                }
                if (file.exists() && !file.isDirectory()) {
                    return file.getAbsolutePath();
                } else {
                    if (Utilities.isWindows() && cmd.endsWith(".exe")){ // NOI18N
                        File file2 = new File(dir, cmd+".lnk"); // NOI18N
                        if (file2.exists() && !file.isDirectory()) {
                            return file.getAbsolutePath();
                        }
                    }
                }
                if (cmd2 != null) {
                    file = new File(dir, cmd2);
                    if (file.exists() && !file.isDirectory()) {
                        return file.getAbsolutePath();
                    } else {
                        File file2 = new File(dir, cmd2+".lnk"); // NOI18N
                        if (file2.exists() && !file.isDirectory()) {
                            return file.getAbsolutePath();
                        }
                    }
                }
            }
        }
        return null;
    }
}
