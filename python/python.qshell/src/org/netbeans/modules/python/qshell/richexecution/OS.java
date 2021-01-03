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

package org.netbeans.modules.python.qshell.richexecution;

/**
 * Describes the kind of operating system we're running on.
 * <br>
 * Pty's are notoriously un-standardized. Their use varies subtly between
 * Linux, BSD and Solaris, not to mention MacOS.
 * This enum helps with customization of behaviour.
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
