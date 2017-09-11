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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api;

import java.io.File;
import java.util.Map;

public interface HostInfo {

    public static enum CpuFamily {

        SPARC,
        X86,
        ARM,
        UNKNOWN;
    }

    public static enum OSFamily {

        SUNOS,
        LINUX,
        WINDOWS,
        MACOSX,
        FREEBSD,
        UNKNOWN;

        public boolean isUnix() {
            switch (this) {
                case LINUX:
                case MACOSX:
                case SUNOS:
                case FREEBSD:
                    return true;
                case WINDOWS:
                    return false;
                case UNKNOWN:
                    return false;
                default:
                    throw new IllegalStateException("Unexpected OSFamily: " + this); //NOI18N
            }
        }

        /**
         * Returns CamelCase name of the family. Like: SunOS; Linux; Windows;
         * MacOSX.
         *
         * @return CamelCase name
         */
        public String cname() {
            switch (this) {
                case LINUX:
                    return "Linux"; // NOI18N
                case MACOSX:
                    return "MacOSX"; // NOI18N
                case SUNOS:
                    return "SunOS"; // NOI18N
                case FREEBSD:
                    return "FreeBSD"; // NOI18N
                case WINDOWS:
                    return "Windows"; // NOI18N
                case UNKNOWN:
                    return "UNKNOWN"; // NOI18N
                default:
                    throw new IllegalStateException("Unexpected OSFamily: " + this); //NOI18N
            }
        }
    }

    public static enum Bitness {

        _32,
        _64;

        public static Bitness valueOf(int bitness) {
            return bitness == 64 ? _64 : _32;
        }

        @Override
        public String toString() {
            return (this == _32) ? "32" : "64"; // NOI18N
        }
    }

    public static interface OS {

        public OSFamily getFamily();

        public String getName();

        public String getVersion();

        public Bitness getBitness();
    }

    public OS getOS();

    public CpuFamily getCpuFamily();

    public int getCpuNum();

    public OSFamily getOSFamily();

    public String getHostname();

    public String getLoginShell();

    public String getShell();

    public Map<String, String> getEnvironment();

    public String getTempDir();

    public File getTempDirFile();

    public String getUserDir();

    public File getUserDirFile();
    
    /** Gets the numeric current user ID  */
    public int getUserId();

    /** Gets the current user main group ID (in numeric form)  */
    public int getGroupId();

    /** Gets the current user main group name */
    public String getGroup();
   
    /** Gets the current user all group IDs (in numeric form)  */
    public int[] getAllGroupIDs();

    /** Gets the current user all group names  */
    public String[] getAllGroups();

    /**
     * @return time difference in milliseconds between remote and localhost
     */
    public long getClockSkew();

    public String getEnvironmentFile();
}
