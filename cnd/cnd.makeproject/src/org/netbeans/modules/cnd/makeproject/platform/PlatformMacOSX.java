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

package org.netbeans.modules.cnd.makeproject.platform;

import org.netbeans.modules.cnd.makeproject.api.configurations.Platform;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;

public class PlatformMacOSX extends Platform {
    public static final String NAME = "MacOSX"; // NOI18N
    public static final String LIBRARY_SUFFIX = ".dylib"; // NOI18N

    private static final LibraryItem.StdLibItem[] standardLibrariesLinux = {
//        StdLibraries.getStandardLibary("Motif"), // NOI18N
        StdLibraries.getStandardLibary("Mathematics"), // NOI18N
        StdLibraries.getStandardLibary("DataCompression"), // NOI18N
        StdLibraries.getStandardLibary("PosixThreads"), // NOI18N
        StdLibraries.getStandardLibary("Curses"), // NOI18N
        StdLibraries.getStandardLibary("DynamicLinking"), // NOI18N
    };
    
    public PlatformMacOSX() {
        super(NAME, "Mac OS X", PlatformTypes.PLATFORM_MACOSX); // NOI18N
    }
    
    @Override
    public LibraryItem.StdLibItem[] getStandardLibraries() {
        return standardLibrariesLinux;
    }
    
    @Override
    public String getLibraryNameWithoutExtension(String baseName) {
        return "lib" + baseName; // NOI18N
    }
    
    @Override
    public String getLibraryExtension() {
        return "dylib"; // NOI18N
    }

    @Override
    public String getQtLibraryName(String baseName, String version) {
        return getLibraryName(baseName + "." + version); // NOI18N
    }

    @Override
    public String getLibraryLinkOption(String libName, String libDir, String libPath, String libSearchPath, CompilerSet compilerSet) {
        if (libName.endsWith(LIBRARY_SUFFIX)) {
            int i = libName.indexOf(LIBRARY_SUFFIX);
            if (i > 0) {
                libName = libName.substring(0, i);
            }
            if (libName.startsWith("lib")) { // NOI18N
                libName = libName.substring(3);
            }
            return compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibrarySearchFlag()
                    + CndPathUtilities.escapeOddCharacters(libDir)
                    + " " + compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibraryFlag() // NOI18N
                    + CndPathUtilities.escapeOddCharacters(libName);
        } else {
            return CndPathUtilities.escapeOddCharacters(libPath);
        }
    }
}
