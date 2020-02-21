/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 */
public final class CommonUtilities {
    /**
     * ld.so expands macros in an rpath specification to the directory containing the program or shared object.
     */
    public static final String ORIGIN = "$ORIGIN"; // NOI18N

    private CommonUtilities() {
    }

    public static String getLdLibraryPath() {
        return getLdLibraryPath(ExecutionEnvironmentFactory.getLocal());
    }
    
    public static String getLdLibraryPath(ExecutionEnvironment eenv) {
        String ldLibraryPathName = getLdLibraryPathName(eenv);
        String paths = HostInfoProvider.getEnv(eenv).get(ldLibraryPathName);
        if (paths == null) {
            paths = "";
        }
        return appendDefaultPaths(eenv, paths);
    }

    public static String getLdLibraryPath(MakeConfiguration activeConfiguration) {
        String ldLibraryPathName = getLdLibraryPathName(activeConfiguration);
        String ldLibPath = activeConfiguration.getProfile().getEnvironment().getenv(ldLibraryPathName);
        ExecutionEnvironment eenv = activeConfiguration.getDevelopmentHost().getExecutionEnvironment();
        if (ldLibPath != null) {
            try {
                ldLibPath = MacroExpanderFactory.getExpander(eenv).expandMacros(ldLibPath, HostInfoUtils.getHostInfo(eenv).getEnvironment());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (ldLibPath == null) {
            ldLibPath = HostInfoProvider.getEnv(eenv).get(ldLibraryPathName); // NOI18N
        }
        if (ldLibPath == null) {
            ldLibPath = "";  // NOI18N
        }
        return appendDefaultPaths(eenv, ldLibPath);
    }
    
    public static String addSearchPaths(String ldLibPath, List<String> searchPaths, String binary) {
        if (searchPaths == null) {
            return ldLibPath;
        }
        String separator = ":";  // NOI18N
        binary = binary.replace('\\', '/');  // NOI18N
        int i = binary.lastIndexOf('/');  // NOI18N
        String binaryPath = "";  // NOI18N
        if (i > 0) {
            binaryPath = binary.substring(0, i);
        }
        List<String> paths = new ArrayList<>();
        for(String s : searchPaths) {
            if (s.indexOf(';') > 0) {  // NOI18N
                separator = ";";  // NOI18N
            }
            for(String path : s.split(separator)) {
                if (path.startsWith(ORIGIN)) {
                    path = binaryPath+path.substring(ORIGIN.length());
                } else if (path.startsWith(".")) {  // NOI18N
                    path = binaryPath+"/"+path;  // NOI18N
                }
                if (!paths.contains(path)) {
                    paths.add(path);
                }
            }
        }
        if (!paths.isEmpty()) {
            StringBuilder buf = new StringBuilder(ldLibPath);
            for(String p : paths) {
                if (buf.length() > 0) {
                    buf.append(separator);
                }
                buf.append(p);
            }
            ldLibPath = buf.toString();
        }
        return ldLibPath;
    }

    private static String appendDefaultPaths(ExecutionEnvironment eenv, String paths) {
        paths = appendConfigLD(eenv, paths);
        PlatformInfo platformInfo = PlatformInfo.getDefault(eenv);
        switch (platformInfo.getPlatform()) {
            case PlatformTypes.PLATFORM_WINDOWS:
                break;
            case PlatformTypes.PLATFORM_MACOSX:
                if (!paths.isEmpty()) {
                    paths += ":"; //NOI18N
                }
                paths += "/usr/lib:/usr/local/lib:/Library/Frameworks:/System/Library/Frameworks";  //NOI18N
                break;
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
            case PlatformTypes.PLATFORM_LINUX:
            default:
                if (!paths.isEmpty()) {
                    paths += ":"; //NOI18N
                }
                paths += "/lib:/usr/lib";  //NOI18N
        }
        return paths;
    }

    private static String appendConfigLD(ExecutionEnvironment eenv, String paths) {
        PlatformInfo platformInfo = PlatformInfo.getDefault(eenv);
        switch (platformInfo.getPlatform()) {
            case PlatformTypes.PLATFORM_WINDOWS:
            case PlatformTypes.PLATFORM_MACOSX:
                break;
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
                break;
            case PlatformTypes.PLATFORM_LINUX:
                paths = appendConfigLD(eenv, paths, "/etc/ld.so.conf", 0); //NOI18N
                break;
            default:
                break;
        }
        return paths;
    }
    
    private static String appendConfigLD(ExecutionEnvironment eenv, String paths, String file, int level) {
        if (level > 5) {
            // trivial antiloop
            return paths;
        }
        FileObject config = FileSystemProvider.getFileObject(eenv, file);
        if (config.isValid() &&  config.canRead()) {
            try {
                Iterator<String> iterator = config.asLines().iterator();
                while(iterator.hasNext()) {
                    String line = iterator.next().trim();
                    if (line.startsWith("#")) { //NOI18N
                        continue;
                    }
                    if (line.indexOf('=')>=0) {
                        // TODO The format is "dirname=TYPE", where TYPE can be libc4, libc5, or libc6
                        continue;
                    }
                    if (line.startsWith("hwcap")) { //NOI18N
                        // TODO
                        continue;
                    }
                    if (line.startsWith("include")) { //NOI18N
                        String redirect = line.substring(7).trim();
                        if (!redirect.startsWith("/")) { //NOI18N
                            redirect = config.getParent().getPath()+"/"+redirect; //NOI18N
                        }
                        int several = redirect.indexOf('*'); //NOI18N
                        if (several < 0) {
                            paths = appendConfigLD(eenv, paths, redirect, level + 1);
                        } else {
                            int folderIndex = redirect.lastIndexOf('/'); //NOI18N
                            if (folderIndex > 0 && folderIndex < several) {
                                String pattern = redirect.substring(folderIndex+1);
                                pattern = pattern.replace(".", "\\.").replace( "*", ".*" ).replace( '?', '.' ); //NOI18N
                                try {
                                    Pattern regex = Pattern.compile(pattern);
                                    String folder = redirect.substring(0, folderIndex);
                                    FileObject configFolder = FileSystemProvider.getFileObject(eenv, folder);
                                    if (config.isValid() && config.canRead() && configFolder.isFolder()) {
                                        for(FileObject children : configFolder.getChildren()) {
                                            String name = children.getNameExt();
                                            if (regex.matcher(name).find()) {
                                                paths = appendConfigLD(eenv, paths, children.getPath(), level + 1);
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    // possible PatternSyntaxException
                                }
                            }
                        }
                    } else {
                        if (!paths.isEmpty()) {
                            paths += ":"; // NOI18N
                        }
                        paths += line;
                    }
                }
            } catch (IOException ex) {
            }
        }
        return paths;
    }
    
    private static String getLdLibraryPathName(ExecutionEnvironment eenv) {
        PlatformInfo platformInfo = PlatformInfo.getDefault(eenv);
        switch (platformInfo.getPlatform()) {
            case PlatformTypes.PLATFORM_WINDOWS:
                return platformInfo.getPathName();
            case PlatformTypes.PLATFORM_MACOSX:
                return "DYLD_LIBRARY_PATH"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
            case PlatformTypes.PLATFORM_LINUX:
            default:
                return "LD_LIBRARY_PATH"; // NOI18N
        }
    }

    private static String getLdLibraryPathName(MakeConfiguration conf) {
        switch (conf.getDevelopmentHost().getBuildPlatform()) {
            case PlatformTypes.PLATFORM_WINDOWS:
                PlatformInfo pi = conf.getPlatformInfo();
                return pi.getPathName();
            case PlatformTypes.PLATFORM_MACOSX:
                return "DYLD_LIBRARY_PATH"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
            case PlatformTypes.PLATFORM_LINUX:
            default:
                return "LD_LIBRARY_PATH"; // NOI18N
        }
    }
}
