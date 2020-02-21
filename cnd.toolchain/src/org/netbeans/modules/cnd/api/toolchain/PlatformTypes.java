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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
