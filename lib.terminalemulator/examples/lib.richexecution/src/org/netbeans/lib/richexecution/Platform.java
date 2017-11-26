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
