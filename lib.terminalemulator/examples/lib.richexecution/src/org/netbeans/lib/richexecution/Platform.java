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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.richexecution;

/**
 * Describes the kind of platform system running on.
 * <br>
 * Pty's are notoriously un-standardized. Their use varies subtly between
 * Linux, BSD and Solaris, not to mention MacOS.
 * This enum helps with customization of behaviour.
 * @author ivan
 */
enum Platform {

    Other,

    LinuxIntel32,
    LinuxIntel64,

    WindowsIntel32,

    SolarisSparc32,
    SolarisSparc64,
    SolarisIntel32,
    SolarisIntel64,

    MacosIntel32;

    /**
     * Returns the platform we're running on.
     * @return The kind of platform we're running on.
     */
    public static Platform get() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        // System.out.printf("Platform.get: os.name: \'%s\' os.arch: '%s'\n", osName, osArch);
        if (osName.equals("sunos")) {
            if (osArch.equals("sparc"))
                return SolarisSparc32;
	    else if (osArch.equals("sparcv9"))
                return SolarisSparc64;
            else if (osArch.equals("x86"))
                return SolarisIntel32;
            else if (osArch.equals("amd64"))
                return SolarisIntel64;
	    else
		return Other;
        } else if (osName.equals("linux")) {
            if (osArch.equals("i386"))
                return LinuxIntel32;
	    else if (osArch.equals("amd64"))
                return LinuxIntel64;
	    else
		return Other;
        } else if (osName.startsWith("mac os x")) {
            if (osArch.equals("i386"))
                return MacosIntel32;
	    else
		return Other;
        } else {
		return Other;
	}
    }

    /**
     * Returns the platform string used for finding helper executables.
     * @return One of "linux-intel", "solaris-sparc", "solaris-intel", "mac-intel" or null.
     */
    public String platform() {
	switch (get()) {
	    case LinuxIntel32:
	    case LinuxIntel64:
                return "linux-intel";
	    case SolarisSparc32:
	    case SolarisSparc64:
                return "solaris-sparc";
	    case SolarisIntel32:
	    case SolarisIntel64:
                return "solaris-intel";
	    case MacosIntel32:
                return "mac-intel";
	    case WindowsIntel32:
	    case Other:
	    default:
		return null;
	}
    }
}
