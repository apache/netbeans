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

import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Moved platform types from cnd.makeproject to here so CompilerSetManager can use them. Because
 * its an interface, cnd.makeproject's Platform class can implement this interface and still have
 * the same platform types/values.
 * 
 */
public final class PlatformTypes {

    // Platform id's
    public static final int PLATFORM_SOLARIS_SPARC = 0;
    public static final int PLATFORM_SOLARIS_INTEL = 1;
    public static final int PLATFORM_LINUX = 2;
    public static final int PLATFORM_WINDOWS = 3;
    public static final int PLATFORM_MACOSX = 4;
    public static final int PLATFORM_GENERIC = 5;
    public static final int PLATFORM_NONE = 6;
    private static int defaultPlatform = -1;

    private PlatformTypes() {
    }

    public static int getPlatformFromHostInfo(HostInfo hostInfo) {
        HostInfo.OSFamily osFamily = hostInfo.getOSFamily();
        switch (osFamily) {
            case SUNOS:
                HostInfo.CpuFamily cpuFamily = hostInfo.getCpuFamily();
                switch (cpuFamily) {
                    case SPARC:
                        return PLATFORM_SOLARIS_SPARC;
                    case X86:
                        return PLATFORM_SOLARIS_INTEL;
                    case ARM:
                    case UNKNOWN:
                    default:
                        Exceptions.printStackTrace(new IllegalStateException(
                                "Unexpected cpu type " + cpuFamily + " for " + osFamily)); //NOI18N
                        return PLATFORM_NONE;
                }
            case LINUX:
                return PLATFORM_LINUX;
            case WINDOWS:
                return PLATFORM_WINDOWS;
            case MACOSX:
                return PLATFORM_MACOSX;
            case UNKNOWN:
            case FREEBSD:
                return PLATFORM_GENERIC;
            default:
                Exceptions.printStackTrace(new IllegalStateException(
                        "Unexpected os " + osFamily)); //NOI18N
                return PLATFORM_NONE;
        }
    }

    public static int getDefaultPlatform() {
        if (defaultPlatform <= 0) {
            String arch = System.getProperty("os.arch"); // NOI18N
            if (Utilities.isWindows()) {
                defaultPlatform = PlatformTypes.PLATFORM_WINDOWS;
            } else if (Utilities.isMac()) {
                defaultPlatform = PlatformTypes.PLATFORM_MACOSX;
            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                if (arch.indexOf("86") >= 0) { // NOI18N
                    defaultPlatform = PlatformTypes.PLATFORM_SOLARIS_INTEL;
                } else {
                    defaultPlatform = PlatformTypes.PLATFORM_SOLARIS_SPARC;
                }
            } else if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                defaultPlatform = PlatformTypes.PLATFORM_LINUX;
            } else {
                defaultPlatform = PlatformTypes.PLATFORM_GENERIC;
            }
        }
        return defaultPlatform;
    }

    public static String toString(int platform) {
        String out;
        switch (platform) {
            case PLATFORM_SOLARIS_SPARC:
                out = "PLATFORM_SOLARIS_SPARC"; // NOI18N
                break;
            case PLATFORM_SOLARIS_INTEL:
                out = "PLATFORM_SOLARIS_INTEL"; // NOI18N
                break;
            case PLATFORM_LINUX:
                out = "PLATFORM_LINUX"; // NOI18N
                break;
            case PLATFORM_WINDOWS:
                out = "PLATFORM_WINDOWS"; // NOI18N
                break;
            case PLATFORM_MACOSX:
                out = "PLATFORM_MACOSX"; // NOI18N
                break;
            case PLATFORM_GENERIC:
                out = "PLATFORM_GENERIC"; // NOI18N
                break;
            case PLATFORM_NONE:
                out = "PLATFORM_NONE"; // NOI18N
                break;
            default:
                out = "Error"; // NOI18N
        }
        return out;
    }
}
