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

package org.netbeans.modules.cnd.discovery.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 */
public class DiscoveryUtils {

    private DiscoveryUtils() {
    }
    
    public static ProjectBridge getProjectBridge(ProjectProxy project) {
        if (project != null) {
            Project p = project.getProject();
            if (p != null){
                ProjectBridge bridge = new ProjectBridge(p);
                if (bridge.isValid()) {
                    return bridge;
                }
            }
        }
        return null;
    }
    
    public static String resolveSymbolicLink(FileSystem fileSystem, final String aPath) {
        if (fileSystem == null || FileSystemProvider.getExecutionEnvironment(fileSystem).isLocal()) {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                    @Override
                    public String run() throws IOException {
                        String path = aPath;
                        for (int i = 0; i < 5; i++) {
                            final Path file = Paths.get(Utilities.toURI(new File(path)));
                            if (Files.isSymbolicLink(file)) {
                                Path to = Files.readSymbolicLink(file);
                                if (!to.isAbsolute()) {
                                    to = file.getParent().resolve(to).normalize();
                                }
                                if (Files.isRegularFile(to)) {
                                    return to.toString();
                                }
                                path = to.toString();
                            } else {
                                return null;
                            }
                        }
                        return null;
                    }
                });
            } catch (Exception ex) {
                CndUtils.printStackTraceOnce(ex);
                return null;
            }
        } else {
            try {
                FileObject fo = fileSystem.findResource(aPath);
                if (fo == null) {
                    return null;
                }
                if (FileSystemProvider.isLink(fo)) {
                    return FileSystemProvider.resolveLink(fo);
                }
                return null;
            } catch (Exception ex) {
                CndUtils.printStackTraceOnce(ex);
                return null;
            }
        }
    }
    
    public static Set<String> getCompilerNames(ProjectProxy project, PredefinedToolKind kind) {
        Project p = null;
        if (project != null) {
            p = project.getProject();
        }
        return BuildTraceSupport.getCompilerNames(p, kind);
    }

    public static String getRelativePath(String base, String path) {
        if (path.equals(base)) {
            return path;
        } else if (path.startsWith(base + '/')) { // NOI18N
            return path.substring(base.length()+1);
        } else if (path.startsWith(base + '\\')) { // NOI18N
            return path.substring(base.length() + 1);
        } else if (!(path.startsWith("/") || path.startsWith("\\") || // NOI18N
                     path.length() > 2 && path.charAt(2)==':')) { // NOI18N
            return path;
        } else {
            StringTokenizer stb = new StringTokenizer(base, "\\/"); // NOI18N
            StringTokenizer stp = new StringTokenizer(path, "\\/"); // NOI18N
            int match = 0;
            String pstring = null;
            while(stb.hasMoreTokens() && stp.hasMoreTokens()) {
                String bstring = stb.nextToken();
                pstring = stp.nextToken();
                if (bstring.equals(pstring)) {
                    match++;
                } else {
                    break;
                }
            }
            if (match <= 1){
                return path;
            }
            StringBuilder s = new StringBuilder();
            while(stb.hasMoreTokens()) {
                String bstring = stb.nextToken();
                s.append("..").append(File.separator); // NOI18N
            }
            s.append("..").append(File.separator).append(pstring); // NOI18N
            while(stp.hasMoreTokens()) {
                s.append(File.separator).append(stp.nextToken()); // NOI18N
            }
            return s.toString();
        }
    }

    public static String normalizeAbsolutePath(String path) {
        boolean caseSensitive = CndFileUtils.isSystemCaseSensitive();
        if (!caseSensitive) {
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
        }
        String normalized;
        // small optimization for true case sensitive OSs
        if (!caseSensitive || (path.endsWith("/.") || path.endsWith("\\.") || path.contains("..") || path.contains("./") || path.contains(".\\"))) { // NOI18N
            normalized = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
        } else {
            normalized = path;
        }
        return normalized;
    }

    /**
     * Path is include path like:
     * .
     * ../
     * include
     * Returns path in Unix style
     */
    public static String convertRelativePathToAbsolute(SourceFileProperties source, String path){
        if ( !( path.startsWith("/") || (path.length()>1 && path.charAt(1)==':') ) ) { // NOI18N
            if (path.equals(".")) { // NOI18N
                path = source.getCompilePath();
            } else {
                path = source.getCompilePath()+File.separator+path;
            }
            File file = new File(path);
            path = CndFileUtils.normalizeFile(file).getAbsolutePath();
        }
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/'); // NOI18N
        }
        return path;
    }
    
    public static String removeQuotes(String path) {
        if (path.length() >= 2 && (path.charAt(0) == '\'' && path.charAt(path.length() - 1) == '\'' || // NOI18N
            path.charAt(0) == '"' && path.charAt(path.length() - 1) == '"')) {// NOI18N

            path = path.substring(1, path.length() - 1); // NOI18N
        }
        return path;
    }

    // reverse of the CndPathUtilities.escapeOddCharacters(String s)
    public static String removeEscape(String s) {
        int n = s.length();
        StringBuilder ret = new StringBuilder(n);
        char prev = 0;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\t') || // NOI18N
                    (c == ':') || (c == '\'') || // NOI18N
                    (c == '*') || (c == '\"') || // NOI18N
                    (c == '[') || (c == ']') || // NOI18N
                    (c == '(') || (c == ')') || // NOI18N
                    (c == ';')) { // NOI18N
                if (prev == '\\') { // NOI18N
                    ret.setLength(ret.length()-1);
                }
            }
            ret.append(c);
            prev = c;
        }
        return ret.toString();
    }
}
