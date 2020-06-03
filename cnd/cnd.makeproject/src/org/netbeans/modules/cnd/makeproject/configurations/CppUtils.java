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

package org.netbeans.modules.cnd.makeproject.configurations;

import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/** Miscellaneous utility classes useful for the C/C++/Fortran module */
public class CppUtils {

    private CppUtils() {
    }

    /**
     * Converts absolute Windows paths to paths without the ':'.
     * Example: C:/abc/def.c -> /cygdrive/c/abs/def.c
     */
    public static String normalizeDriveLetter(CompilerSet cs, String path) {
        if (path.length() > 1 && path.charAt(1) == ':') {// NOI18N
            String driveLetterPrefix = cs.getCompilerFlavor().getToolchainDescriptor().getDriveLetterPrefix();
            String res;
            if (driveLetterPrefix != null) {
                res = driveLetterPrefix + path.charAt(0) + path.substring(2);
            } else {
                // something wrong in tool clllection or project configuration
                res = "/" + path.charAt(0) + path.substring(2);// NOI18N
            }
            return res.replace('\\', '/');// NOI18N
        }
        return path;
    }

    @CheckReturnValue
    public static String getQmakeSpec(CompilerSet cs, int platform) {
        CompilerFlavor flavor = cs.getCompilerFlavor();
        String qmakespec = flavor.getToolchainDescriptor().getQmakeSpec();
        if (qmakespec != null && 0 <= qmakespec.indexOf("${os}")) { // NOI18N
            String os = null;
            switch (platform) {
                case PlatformTypes.PLATFORM_LINUX:
                    os = "linux"; // NOI18N
                    break;
                case PlatformTypes.PLATFORM_MACOSX:
                    os = "macx"; // NOI18N
                    break;
                case PlatformTypes.PLATFORM_SOLARIS_INTEL:
                case PlatformTypes.PLATFORM_SOLARIS_SPARC:
                    os = "solaris"; // NOI18N
                    break;
                case PlatformTypes.PLATFORM_WINDOWS:
                    os = "win32"; // NOI18N
                    break;
            }
            if (os == null) {
                qmakespec = null;
            } else {
                qmakespec = qmakespec.replaceAll("\\$\\{os\\}", os); // NOI18N
            }
        }
        return qmakespec;
    }

    public static String getDefaultDevelopmentHost(FileObject projectDirectory) {
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(projectDirectory);
        if (env.isLocal()) {
            env = ServerList.getDefaultRecord().getExecutionEnvironment();
        }
        return ExecutionEnvironmentFactory.toUniqueID(env);
    }

    public static String getDefaultDevelopmentHost(FileSystem projectFS) {
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(projectFS);
        if (env.isLocal()) {
            env = ServerList.getDefaultRecord().getExecutionEnvironment();
        }
        return ExecutionEnvironmentFactory.toUniqueID(env);
    }

    public static ExecutionEnvironment convertAfterReading(ExecutionEnvironment env, MakeConfiguration makeConfiguration) {
        if (env.isLocal()) {
            return makeConfiguration.getFileSystemHost();
        }
        return env;
    }    
    
    public static ExecutionEnvironment convertBeforeWriting(ExecutionEnvironment env, MakeConfiguration makeConfiguration) {
        if (env.isRemote() && env.equals(makeConfiguration.getFileSystemHost())) {
            return ExecutionEnvironmentFactory.getLocal();
        }
        return env;
    }
}
