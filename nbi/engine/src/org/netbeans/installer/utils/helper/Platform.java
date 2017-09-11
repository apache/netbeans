/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper;

import java.util.List;
import static org.netbeans.installer.utils.helper.PlatformConstants.*;

public enum Platform {
    /////////////////////////////////////////////////////////////////////////////////
    // Values
    GENERIC(null, null, null, null, "Generic"),
    UNIX(OS_FAMILY_UNIX, null, null, null, "Unix"),
    
    WINDOWS(OS_FAMILY_WINDOWS, null, null, null, "Windows"),
    WINDOWS_X86(OS_FAMILY_WINDOWS, HARDWARE_X86, null, null, "Windows X86"),
    WINDOWS_X64(OS_FAMILY_WINDOWS, HARDWARE_X64, null, null, "Windows X64"),
    WINDOWS_IA64(OS_FAMILY_WINDOWS, HARDWARE_IA64, null, null, "Windows Itanium"),    

    LINUX(OS_FAMILY_LINUX, null, null, null, "Linux"),
    LINUX_X86(OS_FAMILY_LINUX, HARDWARE_X86, null, null, "Linux X86"),
    LINUX_X64(OS_FAMILY_LINUX, HARDWARE_X64, null, null, "Linux X64"),
    LINUX_PPC(OS_FAMILY_LINUX, HARDWARE_PPC, null, null, "Linux PowerPC"),
    LINUX_PPC64(OS_FAMILY_LINUX, HARDWARE_PPC64, null, null, "Linux PowerPC X64"),
    LINUX_SPARC(OS_FAMILY_LINUX, HARDWARE_SPARC, null, null, "Linux SPARC"),
    LINUX_IA64(OS_FAMILY_LINUX, HARDWARE_IA64, null, null, "Linux Itanium"),
    
    SOLARIS(OS_FAMILY_SOLARIS, null, null, null, "Solaris"),
    SOLARIS_X86(OS_FAMILY_SOLARIS, HARDWARE_X86, null, null, "Solaris X86"),
    SOLARIS_SPARC(OS_FAMILY_SOLARIS, HARDWARE_SPARC, null, null, "Solaris SPARC"),
    
    MACOSX(OS_FAMILY_MACOSX, null, null, null, "MacOS X"),
    MACOSX_X86(OS_FAMILY_MACOSX, HARDWARE_X86, null, null, "Mac OS X Intel"),
    MACOSX_X64(OS_FAMILY_MACOSX, HARDWARE_X64, null, null, "Mac OS X Intel X64"),
    MACOSX_PPC(OS_FAMILY_MACOSX, HARDWARE_PPC, null, null, "Mac OS X PowerPC"),
    MACOSX_PPC64(OS_FAMILY_MACOSX, HARDWARE_PPC64, null, null, "Mac OS X PowerPC X64"),
    
    BSD(OS_FAMILY_BSD, null, null, null, "BSD"),
    BSD_X86(OS_FAMILY_BSD, HARDWARE_X86, null, null, "BSD X86"),    
    BSD_X64(OS_FAMILY_BSD, HARDWARE_X64, null, null, "BSD X64"),
    BSD_PPC(OS_FAMILY_BSD, HARDWARE_PPC, null, null, "BSD PowerPC"),
    BSD_PPC64(OS_FAMILY_BSD, HARDWARE_PPC64, null, null, "BSD PowerPC X64"),
    BSD_SPARC(OS_FAMILY_BSD, HARDWARE_SPARC, null, null, "BSD SPARC"),    
    
    FREEBSD(OS_FAMILY_FREEBSD, null, null, null, "FreeBSD"),
    FREEBSD_X86(OS_FAMILY_FREEBSD, HARDWARE_X86, null, null, "FreeBSD X86"),
    FREEBSD_X64(OS_FAMILY_FREEBSD, HARDWARE_X64, null, null, "FreeBSD X64"),
    FREEBSD_PPC(OS_FAMILY_FREEBSD, HARDWARE_PPC, null, null, "FreeBSD PowerPC"),
    FREEBSD_PPC64(OS_FAMILY_FREEBSD, HARDWARE_PPC64, null, null, "FreeBSD PowerPC X64"),
    FREEBSD_SPARC(OS_FAMILY_FREEBSD, HARDWARE_SPARC, null, null, "FreeBSD SPARC"),
    
    OPENBSD(OS_FAMILY_OPENBSD, null, null, null, "OpenBSD"),
    OPENBSD_X86(OS_FAMILY_OPENBSD, HARDWARE_X86, null, null, "OpenBSD X86"),
    OPENBSD_X64(OS_FAMILY_OPENBSD, HARDWARE_X64, null, null, "OpenBSD X64"),
    OPENBSD_PPC(OS_FAMILY_OPENBSD, HARDWARE_PPC, null, null, "OpenBSD PowerPC"),
    OPENBSD_PPC64(OS_FAMILY_OPENBSD, HARDWARE_PPC64, null, null, "OpenBSD PowerPC X64"),
    OPENBSD_SPARC(OS_FAMILY_OPENBSD, HARDWARE_SPARC, null, null, "OpenBSD SPARC"),
    
    AIX(OS_FAMILY_AIX, null, null, null, "AIX"),
    AIX_PPC(OS_FAMILY_AIX, HARDWARE_PPC, null, null, "AIX PowerPC"),
    AIX_PPC64(OS_FAMILY_AIX, HARDWARE_PPC64, null, null, "AIX PowerPC X64"),

    HPUX(OS_FAMILY_HPUX, null, null, null, "HP-UX"),
    HPUX_IA64(OS_FAMILY_HPUX, HARDWARE_IA64, null, null, "HP-UX Itanium"),
    HPUX_PA_RISC(OS_FAMILY_HPUX, HARDWARE_PA_RISC, null, null, "HP-UX PA-RISC"),
    HPUX_PA_RISC20(OS_FAMILY_HPUX, HARDWARE_PA_RISC20, null, null, "HP-UX PA-RISC2.0"),
    ;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String osFamily;
    private String hardwareArch;
    private String osVersion;
    private String osFlavor;
    
    private String codeName;
    private String displayName;
    
    private Platform(
            final String osFamily,
            final String hardwareArch,
            final String osVersion,
            final String osFlavor,
            final String displayName) {
        this.osFamily = osFamily;
        this.hardwareArch = hardwareArch;
        this.osVersion = osVersion;
        this.osFlavor = osFlavor;
        
        if (osFamily != null) {
            this.codeName = osFamily;
            
            if (hardwareArch != null) {
                this.codeName += SEPARATOR + hardwareArch;
                
                if (osVersion != null) {
                    this.codeName += SEPARATOR + osVersion;
                    
                    if (osFlavor != null) {
                        this.codeName += SEPARATOR + osFlavor;
                    }
                }
            }
        } else {
            this.codeName = "generic"; 
        }
        
        this.displayName = displayName;
    }
    
    public String getOsFamily() {
        return osFamily;
    }
    
    public String getHardwareArch() {
        return hardwareArch;
    }
    
    public String getOsVersion() {
        return osVersion;
    }
    
    public String getOsFlavor() {
        return osFlavor;
    }
    
    public String getCodeName() {
        return codeName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isCompatibleWith(final Platform platform) {
        if (platform.osFamily!=null &&
                !platform.osFamily.equals(osFamily)) {
            if(platform.osFamily.equals(OS_FAMILY_UNIX) && 
                    !OS_FAMILY_WINDOWS.equals(osFamily)) {
                return true;
            }
            if(platform.osFamily.equals(OS_FAMILY_BSD)) {
                boolean osFamilyMatches = OS_FAMILY_FREEBSD.equals(osFamily) || 
                                           OS_FAMILY_OPENBSD.equals(osFamily) || 
                                           OS_FAMILY_MACOSX.equals(osFamily);
                return osFamilyMatches && 
                        (platform.hardwareArch==null || platform.hardwareArch.equals(hardwareArch));
            }
            return false;
        }
        
        if ((platform.hardwareArch != null) &&
                !platform.hardwareArch.equals(hardwareArch)) {
            return false;
        }
        
        if ((platform.osVersion != null) &&
                !platform.osVersion.equals(osVersion)) {
            return false;
        }
        
        if ((platform.osFlavor != null) &&
                !platform.osFlavor.equals(osFlavor)) {
            return false;
        }
        
        return true;
    }
    
    public boolean isCompatibleWith(final List<Platform> platforms) {
        for (Platform candidate: platforms) {
            if (isCompatibleWith(candidate)) {
                return true;
            }
        }
        
        return false;
    }
    
    
    @Override
    public String toString() {
        return codeName;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String SEPARATOR = 
            "-";
}

