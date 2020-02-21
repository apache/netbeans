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
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;

public class PlatformSolaris  extends Platform {
    private static final LibraryItem.StdLibItem[] standardLibrariesSolaris = {
        StdLibraries.getStandardLibary("Motif"), // NOI18N
        StdLibraries.getStandardLibary("Mathematics"), // NOI18N
        StdLibraries.getStandardLibary("Yacc"), // NOI18N
        StdLibraries.getStandardLibary("Lex"), // NOI18N
        StdLibraries.getStandardLibary("SocketsNetworkServices"), // NOI18N
        StdLibraries.getStandardLibary("SolarisThreads"), // NOI18N
        StdLibraries.getStandardLibary("PosixThreads"), // NOI18N
        StdLibraries.getStandardLibary("Posix4"), // NOI18N
        StdLibraries.getStandardLibary("Internationalization"), // NOI18N
        StdLibraries.getStandardLibary("PatternMatching"), // NOI18N
        StdLibraries.getStandardLibary("Curses"), // NOI18N
    };
    
    public PlatformSolaris(String name, String displayName, int id) {
        super(name, displayName, id);
    }
    
    @Override
    public LibraryItem.StdLibItem[] getStandardLibraries() {
        return standardLibrariesSolaris;
    }
    
    @Override
    public String getLibraryNameWithoutExtension(String baseName) {
        return "lib" + baseName; // NOI18N
    }
    
    @Override
    public String getLibraryExtension() {
        return "so"; // NOI18N
    }
    
    @Override
    public String getLibraryLinkOption(String libName, String libDir, String libPath, String libSearchPath, CompilerSet compilerSet) {
        if (libName.endsWith(".so")) { // NOI18N
            int i = libName.indexOf(".so"); // NOI18N
            if (i > 0) {
                libName = libName.substring(0, i);
            }
            if (libName.startsWith("lib")) { // NOI18N
                libName = libName.substring(3);
            }
            StringBuilder buf = new StringBuilder();
            if (libSearchPath != null) {
                buf.append(compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getDynamicLibrarySearchFlag());
                buf.append('\'');
                buf.append(CndPathUtilities.escapeOddCharacters(libSearchPath));
                buf.append('\'');
                buf.append(' ');
            }
            buf.append(compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibrarySearchFlag());
            buf.append(CndPathUtilities.escapeOddCharacters(libDir));
            buf.append(' ');
            buf.append(compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibraryFlag());
            buf.append(CndPathUtilities.escapeOddCharacters(libName));
            return buf.toString();
        } else {
            return CndPathUtilities.escapeOddCharacters(libPath);
        }
    }
}
