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

public class PlatformWindows extends Platform {
    public static final String NAME = "Windows"; // NOI18N

    private static final LibraryItem.StdLibItem[] standardLibrariesWindows = {
        StdLibraries.getStandardLibary("Mathematics"), // NOI18N
        StdLibraries.getStandardLibary("DataCompression"), // NOI18N
        StdLibraries.getStandardLibary("PosixThreads"), // NOI18N
    };
    
    public PlatformWindows() {
        super(NAME, "Windows", PlatformTypes.PLATFORM_WINDOWS); // NOI18N
    }
    
    @Override
    public LibraryItem.StdLibItem[] getStandardLibraries() {
        return standardLibrariesWindows;
    }
    
    @Override
    public String getLibraryNameWithoutExtension(String baseName) {
        return "lib" + baseName; // NOI18N
    }
    
    @Override
    public String getLibraryExtension() {
        return "dll"; // NOI18N
    }
    
    @Override
    public String getQtLibraryName(String baseName, String version) {
        int dot = version.indexOf('.'); // NOI18N
        String majorVersion = 0 <= dot? version.substring(0, dot) : version;
        return baseName + majorVersion + ".dll"; // NOI18N
    }

    @Override
    public String getLibraryLinkOption(String libName, String libDir, String libPath, String libSearchPath, CompilerSet compilerSet) {
        if (libName.endsWith(".dll")) { // NOI18N
            int i = libName.indexOf(".dll"); // NOI18N
            if (i > 0) {
                libName = libName.substring(0, i);
            }
            if (libName.startsWith("lib") || libName.startsWith("cyg")) { // NOI18N
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
