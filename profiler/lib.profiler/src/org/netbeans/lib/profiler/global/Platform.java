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

package org.netbeans.lib.profiler.global;

import java.io.File;
import java.io.IOException;


/**
 * Determination of the current platform (OS, hardware) and related services.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class Platform implements CommonConstants {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    /**
     * Operating system is Windows NT.
     */
    public static final int OS_WINNT = 1;

    /**
     * Operating system is Windows 95.
     */
    public static final int OS_WIN95 = 2;

    /**
     * Operating system is Windows 98.
     */
    public static final int OS_WIN98 = 4;

    /**
     * Operating system is Solaris.
     */
    public static final int OS_SOLARIS = 8;

    /**
     * Operating system is Linux.
     */
    public static final int OS_LINUX = 16;

    /**
     * Operating system is HP-UX.
     */
    public static final int OS_HP = 32;

    /**
     * Operating system is IBM AIX.
     */
    public static final int OS_AIX = 64;

    /**
     * Operating system is SGI IRIX.
     */
    public static final int OS_IRIX = 128;

    /**
     * Operating system is Sun OS.
     */
    public static final int OS_SUNOS = 256;

    /**
     * Operating system is Compaq TRU64 Unix
     */
    public static final int OS_TRU64 = 512;

    /**
     * Operating system is OS/2.
     */
    public static final int OS_OS2 = 1024;

    /**
     * Operating system is Mac.
     */
    public static final int OS_MAC = 2048;

    /**
     * Operating system is Windows 2000.
     */
    public static final int OS_WIN2000 = 4096;

    /**
     * Operating system is Compaq OpenVMS
     */
    public static final int OS_VMS = 8192;

    /**
     * Operating system is one of the Windows variants but we don't know which one it is
     */
    public static final int OS_WIN_OTHER = 16384;

    /**
     * Operating system is unknown.
     */
    public static final int OS_OTHER = 65536;

    /**
     * A mask for Windows platforms.
     */
    public static final int OS_WINDOWS_MASK = OS_WINNT | OS_WIN95 | OS_WIN98 | OS_WIN2000 | OS_WIN_OTHER;

    /**
     * A mask for Unix platforms.
     */
    public static final int OS_UNIX_MASK = OS_SOLARIS | OS_LINUX | OS_HP | OS_AIX | OS_IRIX | OS_SUNOS | OS_TRU64 | OS_MAC;

    /**
     * The operating system on which the tool runs
     */
    private static int operatingSystem = -1;
    private static String jdkDenoteString;
    private static int jdkVersion;
    private static int sysArch; // 32/64bit architecture

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Given the name of the directory containing the JFluid native libaries (either just the root JFluid libs dir,
     * or really the full path, depending on fullPathToLibSpecified parameter), return the full platform-dependent
     * name for the "profilerinterface" library contained in that directory. If non-null jdkString is specified, it
     * is used in the resulting path; otherwise the VM is queried for its version and the resulting string is used.
     *
     * @param fullAgentPath          The path to the library
     * @param fullPathToLibSpecified whether or not a full path is specified (ending at the platform level)
     * @param jdkString              CommonConstants.JDK_15_STRING
     * @return A path to the native library to be used for this platform
     */
    public static String getAgentNativeLibFullName(String fullAgentPath, boolean fullPathToLibSpecified, String jdkString,
                                                   int architecture) {
        boolean is64bitArch;

        if (jdkString == null) {
            jdkString = getJDKVersionString();
        }

        if (architecture == -1) {
            architecture = getSystemArchitecture();
        }

        is64bitArch = architecture == ARCH_64;

        if (jdkString.equals(JDK_17_STRING) || jdkString.equals(JDK_18_STRING) || jdkString.equals(JDK_19_STRING) || jdkString.equals(JDK_110_BEYOND_STRING)) {
            // for now, we use the same libs for 1.6 and beyond
            jdkString = JDK_16_STRING;
        }

        String libPrefix = ""; // NOI18N

        if (!isWindows()) { // Mac and UNIXes
            libPrefix = "lib"; // NOI18N
        }

        String libSuffix = ""; // NOI18N

        if (isWindows()) {
            libSuffix = ".dll"; // Windows // NOI18N
        } else if (isMac()) {
            libSuffix = ".jnilib"; // Mac // NOI18N
        } else if (isHpux()) {
            libSuffix = ".sl"; // HP-UX // NOI18N
        } else {
            libSuffix = ".so"; // UNIXes // NOI18N
        }

        String libSubPath = "/"; // NOI18N

        if (!fullPathToLibSpecified) {
            String libSubDir;
            if (isWindows()) {
               libSubDir = "windows"; // NOI18N
            } else if (isMac()) {
               libSubDir = "mac";
            } else if (isLinux()) {
               libSubDir = "linux"; // NOI18N
            } else if (isHpux()) {
               libSubDir = "hpux"; // NOI18N
            } else {
               libSubDir = "solaris"; // NOI18N
            }
            String procArch = null;

            if (is64bitArch) {
                if (isLinux() || isWindows() || isSolarisIntel()) {
                    procArch = "amd64"; // NOI18N
                } else if (isSolarisSparc()) {
                    procArch = "sparcv9"; // NOI18N
                } else if (isHpux()) {
                    procArch = "pa_risc2.0w"; // NOI18N
                }
            } else { // 32bit

                if (isSolarisIntel()) {
                    procArch = "i386"; // NOI18N
                } else if (isSolarisSparc()) {
                    procArch = "sparc"; // NOI18N
                } else if (isHpux()) {
                    procArch = "pa_risc2.0"; // NOI18N
                } else if (isLinuxArm()) {
                    if (isLinuxArmVfpHflt()) {
                        procArch = "arm-vfp-hflt"; // NOI18N
                    } else {
                        procArch = "arm"; // NOI18N
                    }
                }
            }

            if (procArch != null) {
                libSubDir += ("-" + procArch);
            }

            libSubPath = "/deployed/" + jdkString + "/" + libSubDir + "/"; // NOI18N
        }

        String fullPath = fullAgentPath;

        if (fullAgentPath.startsWith("\"")) { // NOI18N
            fullPath = fullAgentPath.substring(1, fullAgentPath.length() - 1);
        }

        fullPath = fullPath.replace('\\', '/'); // NOI18N

        return fullPath + libSubPath + libPrefix + "profilerinterface" + libSuffix; // NOI18N
    }

    /**
     * Returns JDK minor version
     */
    public static int getJDKMinorNumber(String jdkVersionString) {
        if (jdkVersionString == null) {
            return 0;
        }

        int minorIndex = jdkVersionString.lastIndexOf('_'); // NOI18N

        if ((minorIndex > 0) && (minorIndex < (jdkVersionString.length() - 1))) {
            String minorString = jdkVersionString.substring(minorIndex + 1);
            int subverIndex = minorString.indexOf('-'); // NOI18N

            if (subverIndex != -1) {
                minorString = minorString.substring(0, subverIndex);
            }

            return Integer.parseInt(minorString);
        }

        return 0;
    }
    
    /**
     * Returns the JFluid-internal JDK version number
     */
    private static int getJDKVersionNumber(String javaVersion) {
        int jdkVersion;

        if (javaVersion.startsWith("1.5")) { // NOI18N
            jdkVersion = JDK_15;
        } else if (javaVersion.startsWith("1.6")) { // NOI18N
            jdkVersion = JDK_16;
        } else if (javaVersion.startsWith("1.7")) { // NOI18N
            jdkVersion = JDK_17;
        } else if (javaVersion.startsWith("1.8")) { // NOI18N
            jdkVersion = JDK_18;
        } else if (javaVersion.startsWith("1.9")) { // NOI18N
            jdkVersion = JDK_19;
        } else if (javaVersion.startsWith("9")) { // NOI18N
            jdkVersion = JDK_19;
        } else if (javaVersion.equals("CVM")) { // NOI18N
            jdkVersion = JDK_CVM;
        } else {
            try {
                if (Integer.parseInt(javaVersion.replaceAll("[.\\-+].*", "")) >= 10) {
                    jdkVersion = JDK_110_BEYOND;
                } else {
                    jdkVersion = JDK_UNSUPPORTED;
                }
            } catch (NumberFormatException ex) {
                //ignore
                jdkVersion = JDK_UNSUPPORTED;
            }
        }
        return jdkVersion;
    }

    
    /**
     * Returns the JFluid-internal JDK version number
     */
    public static int getJDKVersionNumber() {
        if (jdkVersion == 0) {
            jdkVersion = getJDKVersionNumber(getJavaVersionString());
        }
        return jdkVersion;
    }

    /**
     * Returns the string for, essentially, JFluid directory corresponding to a particular JDK version the TA runs on.
     * Currently it's "jdk15" for JDK 1.5 version and "jdk16" for JDK 1.6 version, "jdk17" for JDK 1.7 version,
     *  "jdk18" for JDK 1.8 version and "cvm" for CVM
     */
    public static String getJDKVersionString(String javaVersionString) {
        int jdkVersionNumber = getJDKVersionNumber(javaVersionString);
        
        switch (jdkVersionNumber) {
            case JDK_15: return JDK_15_STRING;
            case JDK_16: return JDK_16_STRING;
            case JDK_17: return JDK_17_STRING;
            case JDK_18: return JDK_18_STRING;
            case JDK_19: return JDK_19_STRING;
            case JDK_110_BEYOND: return JDK_110_BEYOND_STRING;
            case JDK_CVM: return JDK_CVM_STRING;
            case JDK_UNSUPPORTED: return JDK_UNSUPPORTED_STRING;
        }
        System.err.println("Unsupported java "+javaVersionString);
        return JDK_UNSUPPORTED_STRING;
    }

    /**
     * Returns the string for, essentially, JFluid directory corresponding to a particular JDK version the TA runs on.
     * Currently it's "jdk15" for JDK 1.5 version, "jdk16" for JDK 1.6 version, "jdk17" for JDK 1.7 version,
     * "jdk18" for JDK 1.8 version, "jdk19" for JDK 1.9 version and "cvm" for CVM
     */
    public static String getJDKVersionString() {
        if (jdkDenoteString == null) {
            jdkDenoteString = getJDKVersionString(getJavaVersionString());
        }
        return jdkDenoteString;
    }

    public static String getJavaVersionString() {
        // This is ugly hack for CVM. CVM cannot be identified using java.version
        // system property and we have to use java.vm.name which is hardcoded
        // to "CVM"
        String vmVersion = System.getProperty("java.vm.name");   // NOI18N
        if ("CVM".equals(vmVersion)) {
            return vmVersion;
        }
        return System.getProperty("java.version");  // NOI18N
    }
    
    public static String getJFluidNativeLibDirName(String fullJFluidPath, String jdkString, int architecture) {
        String jFluidNativeLibFullName = getAgentNativeLibFullName(fullJFluidPath, false, jdkString, architecture);

        return jFluidNativeLibFullName.substring(0, jFluidNativeLibFullName.lastIndexOf('/')); // NOI18N
    }

    /**
     * Test whether we are running on Linux
     */
    public static boolean isLinux() {
        return (getOperatingSystem() == OS_LINUX);
    }

    /**
     * Test whether the supplied OS name is Linux
     */
    public static boolean isLinux(String osName) {
        return (getOperatingSystem(osName) == OS_LINUX);
    }

    public static boolean isMac() {
        return (getOperatingSystem() == OS_MAC);
    }
    
    public static boolean isHpux() {
        return (getOperatingSystem() == OS_HP);
    }
    
    /**
     * Get the operating system on which we are is running.
     * Returns one of the <code>OS_*</code> constants (such as {@link #OS_WINNT})
     */
    public static int getOperatingSystem() {
        if (operatingSystem == -1) {
            String osName = System.getProperty("os.name"); // NOI18N
            operatingSystem = getOperatingSystem(osName);
        }

        return operatingSystem;
    }

    public static int getOperatingSystem(String osName) {
        if ("Windows NT".equals(osName)) { // NOI18N

            return OS_WINNT;
        } else if ("Windows 95".equals(osName)) { // NOI18N

            return OS_WIN95;
        } else if ("Windows 98".equals(osName)) { // NOI18N

            return OS_WIN98;
        } else if ("Windows 2000".equals(osName)) { // NOI18N

            return OS_WIN2000;
        } else if (osName.startsWith("Windows ")) { // NOI18N

            return OS_WIN_OTHER;
        } else if ("Solaris".equals(osName)) { // NOI18N

            return OS_SOLARIS;
        } else if (osName.startsWith("SunOS")) { // NOI18N

            return OS_SOLARIS;
        } else if (osName.endsWith("Linux")) { // NOI18N

            return OS_LINUX;
        } else if ("HP-UX".equals(osName)) { // NOI18N

            return OS_HP;
        } else if ("AIX".equals(osName)) { // NOI18N

            return OS_AIX;
        } else if ("Irix".equals(osName)) { // NOI18N

            return OS_IRIX;
        } else if ("SunOS".equals(osName)) { // NOI18N

            return OS_SOLARIS;
        } else if ("Digital UNIX".equals(osName)) { // NOI18N

            return OS_TRU64;
        } else if ("OS/2".equals(osName)) { // NOI18N

            return OS_OS2;
        } else if ("OpenVMS".equals(osName)) { // NOI18N

            return OS_VMS;
        } else if (osName.equalsIgnoreCase("mac os x")) { // NOI18N

            return OS_MAC;
        } else if (osName.startsWith("Darwin")) { // NOI18N

            return OS_MAC;
        } else {
            return OS_OTHER;
        }
    }

    public static String getProfilerUserDir() throws IOException {
        String customDir = System.getProperty("nbprofiler.home"); // NOI18N

        if (customDir != null) {
            File d = new File(customDir);

            if (!d.exists()) {
                if (!d.mkdir()) {
                    throw new IOException("Could not create directory " + customDir); // NOI18N
                }
            }

            return customDir;
        } else {
            // use default location
            String dir = System.getProperty("user.home") + File.separator + ".nbprofiler"; // NOI18N
            File d = new File(dir);

            if (!d.exists()) {
                if (!d.mkdir()) {
                    throw new IOException("Could not create directory " + dir); // NOI18N
                }
            }

            return dir;
        }
    }

    /**
     * Test whether we are running on Solaris
     */
    public static boolean isSolaris() {
        return (getOperatingSystem() == OS_SOLARIS);
    }

    /**
     * Test whether we are running on Solaris on Intel processor
     */
    public static boolean isSolarisIntel() {
        String procArch = System.getProperty("os.arch"); // NOI18N

        return isSolaris() && (procArch.endsWith("86") || procArch.equals("amd64")); // NOI18N
    }

    /**
     * Test whether we are running on Solaris on SPARC processor
     */
    public static boolean isSolarisSparc() {
        String procArch = System.getProperty("os.arch"); // NOI18N

        return isSolaris() && procArch.startsWith("sparc"); // NOI18N
    }

    /**
     * Test whether we are running on Linux on ARM processor
     */
    public static boolean isLinuxArm() {
        String procArch = System.getProperty("os.arch"); // NOI18N
        
        return isLinux() && procArch.startsWith("arm"); // NOI18N
    }

    /**
     * Test whether we are running on Linux on ARM processor with Hard float ABI
     */
    public static boolean isLinuxArmVfpHflt() {
        String procArch = System.getProperty("sun.arch.abi"); // NOI18N
        
        return isLinux() && isLinuxArm() && "gnueabihf".equals(procArch); // NOI18N
    }
    
    /**
     * Returns system architecture: 32/64bit
     */
    public static int getSystemArchitecture() {
        if (sysArch == 0) {
            String architecture = System.getProperty("sun.arch.data.model"); // NOI18N
            sysArch = getSystemArchitecture(architecture);
        }
        return sysArch;
    }
    
    public static int getSystemArchitecture(String arch) {
        return "64".equals(arch) ? ARCH_64 : ARCH_32;
    }

    /**
     * Test whether we are running on some variant of Unix. Linux is included as well as the commercial vendors.
     */
    public static boolean isUnix() {
        return (getOperatingSystem() & OS_UNIX_MASK) != 0;
    }

    /**
     * Test whether we are is running on some variant of Windows
     */
    public static boolean isWindows() {
        return (getOperatingSystem() & OS_WINDOWS_MASK) != 0;
    }

    /**
     * Test whether the supplied OS name is some variant of Windows
     */
    public static boolean isWindows(String osName) {
        return (getOperatingSystem(osName) & OS_WINDOWS_MASK) != 0;
    }

    /**
     * Returns true if current system architecture is 32bit
     */
    public static boolean is32bitArchitecture() {
        return getSystemArchitecture() == ARCH_32;
    }

    /**
     * Returns true if current system architecture is 64bit
     */
    public static boolean is64bitArchitecture() {
        return getSystemArchitecture() == ARCH_64;
    }

    /**
     * Returns true if the given JVM version supports dynaimc attach
     */
    public static boolean supportsDynamicAttach(String jdkVersionString) {
        return CommonConstants.JDK_16_STRING.equals(jdkVersionString) 
               || CommonConstants.JDK_17_STRING.equals(jdkVersionString)
               || CommonConstants.JDK_18_STRING.equals(jdkVersionString)
               || CommonConstants.JDK_19_STRING.equals(jdkVersionString)
               || CommonConstants.JDK_110_BEYOND_STRING.equals(jdkVersionString);
    }

    /**
     * Returns true if the given JVM version passed as String correctly reports "sleeping" state
     */
    public static boolean supportsThreadSleepingStateMonitoring(String jdkVersionString) {
        return ((jdkVersionString != null)
               && (jdkVersionString.equals(JDK_15_STRING) ||
		   jdkVersionString.equals(JDK_16_STRING) ||
		   jdkVersionString.equals(JDK_17_STRING) ||
		   jdkVersionString.equals(JDK_18_STRING) ||
		   jdkVersionString.equals(JDK_19_STRING) ||
		   jdkVersionString.equals(JDK_110_BEYOND_STRING) ||
		   jdkVersionString.equals(JDK_CVM_STRING)));
    }

    /**
     * Returns true if the current JVM correctly reports "sleeping" state
     */
    public static boolean thisVMSupportsThreadSleepingStateMonitoring() {
        return supportsThreadSleepingStateMonitoring(getJDKVersionNumber());
    }

    /**
     * Returns true if the given JVM version number correctly reports "sleeping" state
     */
    private static boolean supportsThreadSleepingStateMonitoring(int jdkVersionNumber) {
        return ((jdkVersionNumber == JDK_15) ||
		(jdkVersionNumber == JDK_16) ||
		(jdkVersionNumber == JDK_17) ||
		(jdkVersionNumber == JDK_18) ||
		(jdkVersionNumber == JDK_19) ||
		(jdkVersionNumber == JDK_110_BEYOND) ||
		(jdkVersionNumber == JDK_CVM));
    }
}
