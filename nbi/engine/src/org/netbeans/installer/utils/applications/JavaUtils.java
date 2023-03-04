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

package org.netbeans.installer.utils.applications;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKCU;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKLM;

/**
 *
 * @author Kirill Sorokin
 */
public class JavaUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<File, JavaInfo> knownJdks = new HashMap<>();
    
    public static boolean isJavaHome(File javaHome) {
        if (!javaHome.exists() || !javaHome.isDirectory()) {
            return false;
        }
        
        File probe = getExecutable(javaHome);
        if (!probe.exists() || !probe.isFile()) {
            return false;
        }
        
        // check for lib subdir
        probe = new File(javaHome, "lib");
        if (!probe.exists() || !probe.isDirectory()) {
            return false;
        }
        
        String extension = "";
        if (SystemUtils.isWindows()) {
            extension = ".exe";
        }
        
        probe = new File(javaHome, "bin/javac" + extension);
        if (!probe.exists() || !probe.isFile()) {
            probe = new File(javaHome, "bin/java" + extension);
                if (!probe.exists() || !probe.isFile()) {
                    return false;
                }
        }           

        return true;
    }
    
    public static boolean isJdk(File javaHome) {
        if (!isJavaHome(javaHome)) {
            return false;
        }
        
        String extension = "";
        if (SystemUtils.isWindows()) {
            extension = ".exe";
        }

        if (!SystemUtils.isMacOS()) {
            File javac = new File(javaHome, "bin/javac" + extension);            
            if (!javac.exists() || !javac.isFile()) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean isRecommended(Version version) {
        /*to and from versions - not recommended - all JDK 7 versions older than 7u10 */        
        return version.olderThan(criticalLowVersion) || version.newerThan(criticalHighVersion);
    }
    
    public static void addJavaInfo(final File location, final JavaInfo info) {
        if(knownJdks.get(location)==null) {
            knownJdks.put(location, info);
        }
    }
    
    public static void removeJavaInfo(final File location) {
        if(knownJdks.get(location)!=null) {
            knownJdks.remove(location);
        }
    }
    
    public static Version getVersion(File javaHome) {
        final JavaInfo info = getInfo(javaHome);
        
        return (info == null) ? null : info.getVersion();
    }
    
    public static JavaInfo getInfo(final File javaHome) {
        return getInfo(javaHome, false);
    }
    
    public static JavaInfo getInfo(final File javaHome, boolean ignoreKnownJdks) {
        File location = FileUtils.getNormalizedPathFile(javaHome);
        
        if (!ignoreKnownJdks && knownJdks.get(location) != null) {
            return knownJdks.get(location);
        }
        
        if (!isJavaHome(location)) {
            return null;
        }
        
        final File executable = getExecutable(location);
        
        final File testJdk;
        try {
            testJdk = FileProxy.getInstance().getFile(TEST_JDK_URI);
        } catch (DownloadException e) {
            ErrorManager.notifyError(ResourceUtils.getString(
                    JavaUtils.class, ERROR_CANNOT_DOWNLOAD_TESTJDK_KEY, TEST_JDK_URI), e);
            return null;
        }
        
        JavaInfo jdkInfo = null;
        try {
            final ExecutionResults results = SystemUtils.executeCommand(
                    executable.getAbsolutePath(),
                    "-classpath",
                    testJdk.getParentFile().getAbsolutePath(),
                    TEST_JDK_CLASSNAME);
            
            jdkInfo = JavaInfo.getInfo(results.getStdOut());
            
            if (jdkInfo != null) {
                LogManager.log("... put jdk info to the Java map");
                knownJdks.put(location, jdkInfo);
            } else {
                LogManager.log("... can`t get jdkInfo from " + location);
            }
        } catch (IOException e) {
            LogManager.log(ResourceUtils.getString(
                    JavaUtils.class, ERROR_VERIFICATION_KEY), e);
        }
        
        if (!testJdk.delete()) {
            ErrorManager.notifyError(ResourceUtils.getString(
                    JavaUtils.class,  ERROR_CANNOT_DELETE_KEY, testJdk.getAbsolutePath()));
        }
        
        return jdkInfo;
    }
    
    public static File getExecutable(File javaHome) {
        if (SystemUtils.isWindows()) {
            return new File(javaHome, "bin/java.exe");
        } else {
            return new File(javaHome, "bin/java");
        }
    }
    
    public static File getExecutableW(File javaHome) {
        if (SystemUtils.isWindows()) {
            return new File(javaHome, "bin/javaw.exe");
        } else {
            return new File(javaHome, "bin/java");
        }
    }
    
    // windows-only /////////////////////////////////////////////////////////////////
    public static void createJdkKey(Version version, String javaHome) throws NativeException {
        if (!SystemUtils.isWindows()) {
            return;
        }
        
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        
        String key  = registry.constructKey(JDK_KEY, version.toJdkStyle());
        
        setJdkData(key, version, javaHome);
        updateJdkKey(registry.constructKey(JDK_KEY, version.toMinor()));
        updateCurrentVersion();
    }
    
    public static void deleteJdkKey(Version version, String javaHome) throws NativeException {
        if (!SystemUtils.isWindows()) {
            return;
        }
        
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        
        String key  = registry.constructKey(JDK_KEY, version.toJdkStyle());
        int section = getJDKRegistrySection(registry);
        if (registry.keyExists(section, key) && registry.valueExists(section, key, JAVAHOME_VALUE)) {
            String currentJavaHome = registry.getStringValue(section, key, JAVAHOME_VALUE);
            if (currentJavaHome.equals(javaHome)) {
                registry.deleteKey(section, key);
                updateJdkKey(registry.constructKey(JDK_KEY, version.toMinor()));
            }
        }
        
        updateCurrentVersion();
    }
    
    public static File findJDKHome(Version jdkVersion) {
        return findJavaHome(JDK_KEY, jdkVersion);
    }
    public static File findJreHome(Version jdkVersion) {
        return findJavaHome(JRE_KEY, jdkVersion);
    }
    
    private static File findJavaHome(String javaKey, Version jdkVersion) {
        File result = null;
        try {
            if(SystemUtils.isWindows()) {
                final String version = jdkVersion.toJdkStyle();
                LogManager.log("... checking if JDK " + version + " is already installed");
                WindowsRegistry winreg = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
                if(winreg.keyExists(HKLM, javaKey, version)) {
                    final String versKey = javaKey + WindowsRegistry.SEPARATOR + version;
                    if(winreg.valueExists(HKLM, versKey, JAVAHOME_VALUE)) {
                        final String javaHome = winreg.getStringValue(HKLM, versKey,JAVAHOME_VALUE);
                        if(JavaUtils.getInfo(new File(javaHome))!=null) {
                            result = new File(javaHome);
                        } else {
                            LogManager.log("... no Java at " + javaHome);
                        }
                    } else {
                        LogManager.log("... cannot find JavaHome value for this Java");
                    }
                } else {
                    LogManager.log("... cannot find key for this Java");
                }
            }
        } catch (NativeException e) {
            LogManager.log(e);
        }
        return result;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static int getJDKRegistrySection(WindowsRegistry registry) throws NativeException {
        return (registry.canModifyKey(HKLM,JDK_KEY) ? HKLM : HKCU);
    }
    
    private static void setJdkData(String key, Version version, String javaHome) throws NativeException {
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        int section = getJDKRegistrySection(registry);
        registry.createKey(section, key);
        registry.setStringValue(section, key, JAVAHOME_VALUE, javaHome);
        registry.setStringValue(section, key, MICROVERSION_VALUE, version.getMicro());
    }
    
    private static void updateJdkKey(String key) throws NativeException {
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        int section = getJDKRegistrySection(registry);
        registry.createKey(section, key);
        
        String  javaHome = null;
        Version version  = null;
        for (String subkey: registry.getSubKeys(section, JDK_KEY)) {
            if (subkey.startsWith(key) && !subkey.equals(key) && registry.valueExists(section, subkey, JAVAHOME_VALUE)) {
                final String  tempJavaHome = registry.getStringValue(section, subkey, JAVAHOME_VALUE);
                final Version tempVersion  = JavaUtils.getVersion(new File(tempJavaHome));
                if ((tempVersion != null) && ((version == null) || version.olderThan(tempVersion))) {
                    javaHome = tempJavaHome;
                    version  = tempVersion;
                }
            }
        }
        
        if ((version != null) && (javaHome != null)) {
            setJdkData(key, version, javaHome);
        } else {
            registry.deleteKey(section, key);
        }
    }
    
    private static void updateCurrentVersion() throws NativeException {
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        int section = getJDKRegistrySection(registry);
        registry.createKey(section, JDK_KEY);
        
        String  name    = null;
        Version version = null;
        
        for (String key: registry.getSubKeys(section, JDK_KEY)) {
            if (registry.valueExists(section, key, JAVAHOME_VALUE)) {
                String  tempName     = registry.getKeyName(key);
                String  tempJavaHome = registry.getStringValue(section, key, JAVAHOME_VALUE);
                Version tempVersion  = JavaUtils.getVersion(new File(tempJavaHome));
                if ((tempVersion != null) && ((version == null) || version.olderThan(tempVersion))) {
                    name    = tempName;
                    version = tempVersion;
                }
            }
        }
        
        if ((name != null) && (version != null)) {
            registry.setStringValue(section, JDK_KEY, CURRENT_VERSION_VALUE, name);
        } else {
            registry.deleteKey(section, JDK_KEY);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private JavaUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class JavaInfo {
        /////////////////////////////////////////////////////////////////////////////
        // Static
        public static JavaInfo getInfo(final String string) {
            final String[] lines = StringUtils.splitByLines(string);
            
            Version version = null;
            String vendor;
            String osName;
            String osArch;
            
            boolean nonFinal = false;
            
            if (lines.length == (TEST_JDK_OUTPUT_PARAMETERS + 1)) {
                final String javaVersion = lines[0]; // java.version
                final String javaVmVersion = lines[1]; // java.vm.version
                
                vendor = lines[2]; // java.vendor
                osName = lines[3]; // os.name
                osArch = lines[4]; // os.arch
                LogManager.log("... java.version    = " + javaVersion);
                LogManager.log("... java.vm.version = " + javaVmVersion);
                LogManager.log("... java.vendor     = " + vendor);
                LogManager.log("... os.name         = " + osName);
                LogManager.log("... os.arch         = " + osArch);
                
                String versionString;
                
                // if java.vm.version contains java.version, then use it, as it
                // usually contains more detailed info
                if (javaVmVersion.contains(javaVersion)) {
                    versionString = javaVmVersion.substring(
                            javaVmVersion.indexOf(javaVersion));
                } else {
                    versionString = javaVersion;
                }
                
                // check whether this particular jvm is non final
                final Matcher nonFinalMatcher = Pattern.compile(
                        NON_FINAL_JVM_PATTERN).matcher(versionString);
                if (nonFinalMatcher.find()) {
                    versionString = versionString.replaceAll(
                            NON_FINAL_JVM_PATTERN,
                            StringUtils.EMPTY_STRING);
                    
                    nonFinal = true;
                }
                
                // convert 1.6.0-b105 to 1.6.0.0.105
                if (versionString.matches(
                        "[0-9]+\\.[0-9]+\\.[0-9]+-b[0-9]+")) {
                    versionString = versionString.replace("-b", ".0.");
                }
                
                // convert 1.6.0_01-b105 to 1.6.0_01.105
                if (versionString.matches(
                        "[0-9]+\\.[0-9]+\\.[0-9]+_[0-9]+-b[0-9]+")) {
                    versionString = versionString.replace("-b", ".");
                }
                
                // hack for BEA: 1.6.0-20061129 -> 1.6.0.0.20061129
                if (vendor.contains("BEA")) {
                    versionString = versionString.replaceAll(
                            "([0-9]+\\.[0-9]+\\.[0-9])+-([0-9]+)",
                            "$1.0.$2");
                }
                LogManager.log("... version string : " + versionString);
                // and create the version
                final Matcher matcher = Pattern.
                        compile("[0-9][0-9_\\.\\-]+[0-9]").
                        matcher(versionString);
                
                if (matcher.find()) {
                    version = Version.getVersion(matcher.group());
                }
                // For JDK release version like 12, java.vm.version = 12+33 
                // which doesn't match above pattern string
                // hence use java.version string 12
                if (version == null) {
                    version = Version.getVersion(javaVersion);
                }

                // if the version was created successfully, then we can provide a
                // JavaInfo object
                if (version != null) {
                    return new JavaInfo(version, vendor, osArch, nonFinal);
                }
            } else {
                LogManager.log("... different lines number [" + lines.length + "]");
                for(int j=0;j<lines.length;j++) {
                    LogManager.log("... line [" + j + "] = [" + lines[j] + "]");
                }
            }
            
            return null;
        }
        
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        private Version version;
        private String vendor;
        
        private boolean nonFinal;
        private String arch;
        
        public JavaInfo(Version version, String vendor) {
            if (version.toString().endsWith("64")) {
                this.arch = "x64";
            } else {
                this.arch = "";
            }
            
            this.version = version;
            this.vendor = vendor;
            
            this.nonFinal = false;            
        }
        
        public JavaInfo(Version version, String vendor, boolean nonFinal) {
            this(version, vendor);
            
            this.nonFinal = nonFinal;
        }

        public JavaInfo(Version version, String vendor, String arch, boolean nonFinal) {
            this(version, vendor);
            this.arch = arch;
            this.nonFinal = nonFinal;
        }
        
        public Version getVersion() {
            return version;
        }
        
        public String getVendor() {
            return vendor;
        }
        
        public boolean isNonFinal() {
            return nonFinal;
        }
        public String getArch() {
            arch = arch.contains("64")? "x64" : arch;
            return arch;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static Version criticalLowVersion = Version.getVersion("1.7.0_00");
    public static Version criticalHighVersion = Version.getVersion("1.7.0_09");            
    
    public static final String JRE_NESTED_SUBDIR = "/bin/jre/"; //NOI18N
    
    public static final String JDK_KEY =
            "SOFTWARE\\JavaSoft\\Java Development Kit"; // NOI18N
    public static final String JRE_KEY =
            "SOFTWARE\\JavaSoft\\Java Runtime Environment"; // NOI18N
    
    public static final String JAVAHOME_VALUE
            = "JavaHome"; // NOI18N
    
    public static final String MICROVERSION_VALUE
            = "MicroVersion"; // NOI18N
    
    public static final String CURRENT_VERSION_VALUE =
            "CurrentVersion"; // NOI18N
    
    public static final String TEST_JDK_RESOURCE =
            "org/netbeans/installer/utils/applications/TestJDK.class"; // NOI18N
    
    public static final String TEST_JDK_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + TEST_JDK_RESOURCE;
    
    public static final String TEST_JDK_CLASSNAME =
            "TestJDK"; // NOI18N
    
    public static final int TEST_JDK_OUTPUT_PARAMETERS =
            5; // java.version, java.vm.version, java.vendor, os.name, os.arch
    
    public static final String NON_FINAL_JVM_PATTERN =
            "-(ea|rc[0-9]*|beta[0-9]*|preview[0-9]*|" + // NOI18N
            "dp[0-9]*|alpha[0-9]*|fcs)"; // NOI18N
    
    public static final String ERROR_VERIFICATION_KEY =
            "JU.error.verification";//NOI18N
    public static final String ERROR_CANNOT_DELETE_KEY =
            "JU.error.cannot.delete";//NOI18N
    public static final String ERROR_CANNOT_DOWNLOAD_TESTJDK_KEY =
            "JU.error.cannot.download.testjdk";//NOI18N
    public static final String ERROR_CANNOT_CANONIZE_KEY=
            "JU.error.cannot.canonize";//NOI18N
}
