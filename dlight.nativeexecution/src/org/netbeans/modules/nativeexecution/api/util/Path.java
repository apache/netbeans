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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                    if (key.toLowerCase().equals("path")) { // NOI18N
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
