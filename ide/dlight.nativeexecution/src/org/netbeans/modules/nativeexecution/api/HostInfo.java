/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.api;

import java.io.File;
import java.util.Map;

public interface HostInfo {

    public static enum CpuFamily {

        SPARC,
        X86,
        ARM,
        AARCH64,
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
