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

package org.netbeans.modules.python.qshell.richexecution;

/**
 * Describes the kind of operating system we're running on.
 * <br>
 * Pty's are notoriously un-standardized. Their use varies subtly between
 * Linux, BSD and Solaris, not to mention MacOS.
 * This enum helps with customization of behaviour.
 * @author ivan
 */
public enum OS {

    OTHER, LINUX, WINDOWS, SOLARIS, MACOS;

    /**
     * Returns the kind of OS we're running on.
     * @return The kind of OS we're running on.
     */
    public static OS get() {
        String osName = System.getProperty("os.name");
        osName = osName.toLowerCase();
        System.out.printf("os.name: \'%s\'\n", osName);

        if (osName.startsWith("windows")) {
            return OS.WINDOWS;
        } else if (osName.startsWith("linux")) {
            return OS.LINUX;
        } else if (osName.startsWith("sunos")) {
            return OS.SOLARIS;
        } else if (osName.startsWith("mac os x")) {
            return OS.MACOS ;
        } else {
            return OS.OTHER;
        }
    }

    public static String platform() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        System.out.printf("os.name: \'%s\' os.arch: '%s'\n", osName, osArch);
        String platform = null;
        if (osName.equals("sunos")) {
            if (osArch.equals("sparc") || osArch.equals("sparcv9"))
                platform = "solaris-sparc";
            else if (osArch.equals("x86") || osArch.equals("amd64"))
                platform = "solaris-intel";
        } else if (osName.equals("linux")) {
            if (osArch.equals("i386") || osArch.equals("amd64"))
                platform = "linux-intel";
        }
        return platform;
    }
}
