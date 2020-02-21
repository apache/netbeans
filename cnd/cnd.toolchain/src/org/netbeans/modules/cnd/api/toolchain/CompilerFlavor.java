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

package org.netbeans.modules.cnd.api.toolchain;

import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerFlavorImpl;

/**
 *
 */
public abstract class CompilerFlavor {
    
    public static List<CompilerFlavor> getFlavors(int platform) {
        return CompilerFlavorImpl.getFlavors(platform);
    }

    public static CompilerFlavor getUnknown(int platform) {
        return CompilerFlavorImpl.getUnknown(platform);
    }

    public static CompilerFlavor toFlavor(String name, int platform) {
        return CompilerFlavorImpl.toFlavor(name, platform);
    }

    /**
     *
     * @return The tool collection descriptor that loaded from xml file from folder CND/ToolChain/ in file system
     */
    public abstract ToolchainDescriptor getToolchainDescriptor();

    /**
     *
     * @return True if tool chain like to GNU compilers
     */
    public abstract boolean isGnuCompiler();

    /**
     *
     * @return True if tool chain like to SunStudio compilers
     */
    public abstract boolean isSunStudioCompiler();

    /**
     *
     * @return True if tool chain like to Windows Cygwin compilers
     */
    public abstract boolean isCygwinCompiler();

    /**
     *
     * @return True if tool chain like to Windows MinGW compilers
     */
    public abstract boolean isMinGWCompiler();
    

    protected CompilerFlavor() {
        if (!getClass().equals(CompilerFlavorImpl.class)) {
            throw new UnsupportedOperationException("this class can not be overriden by clients"); // NOI18N
        }
    }
}
