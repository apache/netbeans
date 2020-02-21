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
