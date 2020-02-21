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

package org.netbeans.modules.cnd.toolchain.compilerset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.BaseFolder;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator;
import org.openide.util.Utilities;

/**
 *
 */
public final class ToolUtils {
    private static String cygwinBase;
    private static final WeakHashMap<String, String> commandCache = new WeakHashMap<String, String>();

    private ToolUtils() {
    }

    /**
     * Get the Cygwin base directory from Cygwin.xml (toolchain definition, which users the Windows registry) or the user's path
     */
    public static String getCygwinBase() {
        if (cygwinBase == null) {
            ToolchainDescriptor td = ToolchainManagerImpl.getImpl().getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS); // NOI18N
            if (td != null) {
                String cygwinBin = getBaseFolder(td, PlatformTypes.PLATFORM_WINDOWS);
                if (cygwinBin != null) {
                    cygwinBase = cygwinBin.substring(0, cygwinBin.length() - 4).replace("\\", "/"); // NOI18N
                }
            }
            if (cygwinBase == null) {
                for (String dir : Path.getPath()) {
                    dir = dir.toLowerCase().replace("\\", "/"); // NOI18N
                    if (dir.contains("cygwin")) { // NOI18N
                        if (dir.endsWith("/")) { // NOI18N
                            dir = dir.substring(0, dir.length() - 1);
                        }
                        if (dir.toLowerCase().endsWith("/usr/bin")) { // NOI18N
                            cygwinBase = dir.substring(0, dir.length() - 8);
                            break;
                        } else if (dir.toLowerCase().endsWith("/bin")) { // NOI18N
                            cygwinBase = dir.substring(0, dir.length() - 4);
                            break;
                        }
                    }
                }
            }
        }
        return cygwinBase;
    }

    /**
     * Get the command folder (toolchain definition, which users the Windows registry) or the user's path
     */
    public static String getCommandFolder(CompilerSet cs) {
        if (!Utilities.isWindows()) {
            return null;
        }
        String res = detectCommandFolder(cs);
        if (res != null) {
            return res;
        }
        return null;
    }

    /**
     * Find command folder by toolchain definitions, which users the Windows registry or the user's path
     */
    public static String getCommandFolder() {
        if (!Utilities.isWindows()) {
            return null;
        }
        CompilerSetManager csm = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal());
        for(CompilerSet cs : csm.getCompilerSets()) {
            String res = cs.getCommandFolder();
            if (res != null) {
                return res;
            }
        }
        ToolchainManagerImpl tcm = ToolchainManagerImpl.getImpl();
        for(ToolchainDescriptor td : tcm.getToolchains(PlatformTypes.PLATFORM_WINDOWS)){
            if (td != null) {
                String res = detectCommandFolder(td);
                if (res != null) {
                    return res;
                }
            }
        }
        return findMsysInPath();
    }

    /**
     * Find command folder by toolchain definitions, which users the Windows registry or the user's path
     */
    public static String getCommandFolder(CompilerSetManager csm) {
        for(CompilerSet cs : csm.getCompilerSets()) {
            String res = cs.getCommandFolder();
            if (res != null) {
                return res;
            }
        }
        ToolchainManagerImpl tcm = ToolchainManagerImpl.getImpl();
        for(ToolchainDescriptor td : tcm.getToolchains(PlatformTypes.PLATFORM_WINDOWS)){
            if (td != null) {
                String res = detectCommandFolder(td);
                if (res != null) {
                    return res;
                }
            }
        }
        return findMsysInPath();
    }

    public static String getPlatformName(int platform) {
        switch (platform) {
            case PlatformTypes.PLATFORM_LINUX:
                return "linux"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
                return "sun_sparc"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
                return "sun_intel"; // NOI18N
            case PlatformTypes.PLATFORM_WINDOWS:
                return "windows"; // NOI18N
            case PlatformTypes.PLATFORM_MACOSX:
                return "mac"; // NOI18N
            default:
                return "none"; // NOI18N
        }
    }
    public static boolean isPlatforSupported(int platform, ToolchainDescriptor d) {
        switch (platform) {
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
                for (String p : d.getPlatforms()) {
                    if ("sun_sparc".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
                for (String p : d.getPlatforms()) {
                    if ("sun_intel".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_LINUX:
                for (String p : d.getPlatforms()) {
                    if ("linux".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_WINDOWS:
                for (String p : d.getPlatforms()) {
                    if ("windows".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_MACOSX:
                for (String p : d.getPlatforms()) {
                    if ("mac".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_GENERIC:
                for (String p : d.getPlatforms()) {
                    if ("unix".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_NONE:
                for (String p : d.getPlatforms()) {
                    if ("none".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    public static int computeLocalPlatform() {
        String os = System.getProperty("os.name").toLowerCase(); // NOI18N

        if (os.contains("sunos")) { // NOI18N
            String os_arch = System.getProperty("os.arch", "");	// NOI18N
            int platform_arch = PlatformTypes.PLATFORM_SOLARIS_INTEL;
            if (os_arch.toLowerCase().contains("sparc")) {// NOI18N
                platform_arch = PlatformTypes.PLATFORM_SOLARIS_SPARC;		// NOI18N
            }
            return platform_arch;
        } else if (os.contains("windows")) { // NOI18N
            return PlatformTypes.PLATFORM_WINDOWS;
        } else if (os.contains("linux")) { // NOI18N
            return PlatformTypes.PLATFORM_LINUX;
        } else if (os.contains("mac") || os.contains("darwin")) { // NOI18N
            return PlatformTypes.PLATFORM_MACOSX;
        } else {
            return PlatformTypes.PLATFORM_GENERIC;
        }
    }

    public static String findCommand(CompilerSet cs, String name) {
        String path = Path.findCommand(name);
        if (path == null) {
            String dir = cs.getCommandFolder();
            if (dir != null) {
                path = findCommand(name, dir); // NOI18N
            }
        }
        return path;
    }

    public static String findCommand(String cmd, String dir) {
        File file;
        String cmd2 = null;
        if (cmd.length() > 0) {
            if (Utilities.isWindows() && !cmd.endsWith(".exe")) { // NOI18N
                cmd2 = cmd + ".exe"; // NOI18N
            }

            file = new File(dir, cmd);
            if (file.exists()) {
                return file.getAbsolutePath();
            } else {
                if (Utilities.isWindows() && cmd.endsWith(".exe")){// NOI18N
                    File file2 = new File(dir, cmd+".lnk");// NOI18N
                    if (file2.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
            if (cmd2 != null) {
                file = new File(dir, cmd2);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
                File file2 = new File(dir, cmd2+".lnk");// NOI18N
                if (file2.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }



    public static String replaceOddCharacters(String s, char replaceChar) {
        int n = s.length();
        StringBuilder ret = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\t') ||
                    (c == ':') || (c == '\'') ||
                    (c == '*') || (c == '\"') ||
                    (c == '[') || (c == ']') ||
                    (c == '(') || (c == ')')) {
                ret.append(replaceChar);
            } else {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    /**
     * Same as the C library dirname function: given a path, return
     * its directory name. Unlike dirname, however, return null if
     * the file is in the current directory rather than ".".
     */
    public static String getDirName(String path) {
        int sep = path.lastIndexOf('/');
        if (sep == -1) {
            sep = path.lastIndexOf('\\');
        }
        if (sep != -1) {
            return path.substring(0, sep);
        }
        return null;
    }

    /**
     * This utility method makes it easier (on Windows) to replace PATH with one with
     * the same case. IZ 103016 updated PATH but it wasn't foud because Path wasn't
     * replaced. This will let us add a path using the exact same name.
     */
    public String getPathName(ExecutionEnvironment executionEnvironment) {
        if (executionEnvironment.isLocal() && Utilities.isWindows()) {
            HostInfoProvider.getEnv(executionEnvironment);
            for (String key : HostInfoProvider.getEnv(executionEnvironment).keySet()) {
                if (key.toLowerCase().equals("path")) { // NOI18N
                    return key.substring(0, 4);
                }
            }
        }
        return "PATH"; // NOI18N
    }

    public static boolean isMyFolder(String path, ToolchainDescriptor d, int platform, boolean known, ToolKind tool) {
        boolean res = isMyFolderImpl(path, d, platform, known, tool);
        if (ToolchainManagerImpl.TRACE && res) {
            System.err.println("Path [" + path + "] belongs to tool chain " + d.getName()); // NOI18N
        }
        return res;
    }

    /**
     *
     * @param path
     * @param d
     * @param platform
     * @param known if path known the methdod does not check path pattern
     * @return
     */
    private static boolean isMyFolderImpl(String path, ToolchainDescriptor d, int platform, boolean known, ToolKind tool) {
        ToolDescriptor c = d.getC();
        if (tool == PredefinedToolKind.DebuggerTool) {
            c = d.getDebugger();
        }
        if (c == null || c.getNames().length == 0) {
            return false;
        }
        Pattern pattern = null;
        if (!known) {
            if (c instanceof CompilerDescriptor) {
                CompilerDescriptor cd = (CompilerDescriptor) c;
                if (cd.getPathPattern() != null) {
                    if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                        pattern = Pattern.compile(cd.getPathPattern(), Pattern.CASE_INSENSITIVE);
                    } else {
                        pattern = Pattern.compile(cd.getPathPattern());
                    }
                }
                if (pattern != null) {
                    if (!pattern.matcher(path).find()) {
                        String f = cd.getExistFolder();
                        if (f == null) {
                            return false;
                        }
                        File folder = new File(path + "/" + f); // NOI18N
                        if (!folder.exists() || !folder.isDirectory()) {
                            return false;
                        }
                    }
                }
            }
        }
        File file = new File(path + "/" + c.getNames()[0]); // NOI18N
        if (!file.exists()) {
            file = new File(path + "/" + c.getNames()[0] + ".exe"); // NOI18N
            if (!file.exists()) {
                file = new File(path + "/" + c.getNames()[0] + ".exe.lnk"); // NOI18N
                if (!file.exists()) {
                    return false;
                }
            }
        }
        if (c.getVersionFlags() != null && c.getVersionPattern() != null) {
            String flag = c.getVersionFlags();
            pattern = Pattern.compile(c.getVersionPattern());
            String command = LinkSupport.resolveWindowsLink(file.getAbsolutePath());
            String s = getCommandOutput(path, command, flag, true);
            boolean res = pattern.matcher(s).find();
            if (ToolchainManagerImpl.TRACE && !res) {
                System.err.println("No match for pattern [" + c.getVersionPattern() + "]:"); // NOI18N
                System.err.println("Run " + path + "/" + c.getNames()[0] + " " + flag + "\n" + s); // NOI18N
            }
            return res;
        } else if (c.getFingerPrintFlags() != null && c.getFingerPrintPattern() != null) {
            String flag = c.getFingerPrintFlags();
            String command = LinkSupport.resolveWindowsLink(file.getAbsolutePath());
            String s = getCommandOutput(path, command,flag, false);
            pattern = Pattern.compile(c.getFingerPrintPattern());
            final Matcher matcher = pattern.matcher(s);
            boolean res = matcher.find();
            if (ToolchainManagerImpl.TRACE && !res) {
                System.err.println("No match for pattern [" + c.getFingerPrintPattern() + "]:"); // NOI18N
                System.err.println("Run " + path + "/" + c.getNames()[0] + " " + flag + "\n" + s); // NOI18N
            }
            //if (res && matcher.groupCount() >= 1) {
            //    String version = matcher.group(1);
            //    System.err.println(version);
            //}
            return res;
        }
        return true;
    }

    public static String getBaseFolder(ToolchainDescriptor d, int platform) {
        if (platform != PlatformTypes.PLATFORM_WINDOWS) {
            return null;
        }
        List<BaseFolder> list = d.getBaseFolders();
        if (list == null || list.isEmpty()) {
            return null;
        }
        for (BaseFolder folder : list) {
            String pattern = folder.getFolderPattern();
            String key = folder.getFolderKey();
            if (key == null || pattern == null) {
                continue;
            }
            String base = readRegistry(key, pattern);
            if (base == null) {
                continue;
            }
            if (folder.getFolderSuffix() != null) {
                base += "/" + folder.getFolderSuffix(); // NOI18N
            }
            return base;
        }
        return null;
    }

    private static String detectCommandFolder(ToolchainDescriptor d) {
        List<BaseFolder> list = d.getCommandFolders();
        if (list == null || list.isEmpty()) {
            return null;
        }
        String base;
        for (BaseFolder folder : list) {
            String pattern = folder.getFolderPattern();
            String key = folder.getFolderKey();
            if (key == null || pattern == null) {
                continue;
            }
            base = readRegistry(key, pattern);
            if (base != null && folder.getFolderSuffix() != null) {
                base += "\\" + folder.getFolderSuffix(); // NOI18N
            }
            if (base != null) {
                if (new File(base).exists()) {
                    return base;
                }
            }
        }
        for (BaseFolder folder : list) {
            // search for unregistered msys
            String pattern = folder.getFolderPathPattern();
            if (pattern != null && pattern.length() > 0) {
                Pattern p = Pattern.compile(pattern);
                for (String dir : Path.getPath()) {
                    if (p.matcher(dir).find()) {
                        if (new File(dir).exists()) {
                            return dir;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public static String detectCommandFolder(CompilerSet cs) {
        ToolchainDescriptor d = cs.getCompilerFlavor().getToolchainDescriptor();
        String res = detectCommandFolder(d);
        if (res != null) {
            return res;
        }
        List<BaseFolder> list = d.getCommandFolders();
        if (list != null) {
            for (BaseFolder folder : list) {
                // search command folder in relative paths
                String relPath = folder.getRelativePath();
                if (relPath != null && relPath.length() > 0) {
                    String dir = cs.getDirectory()+"/"+relPath; // NOI18N
                    dir = CndFileUtils.normalizeAbsolutePath(dir);
                    if (new File(dir).exists()) {
                        return dir;
                    }
                }
            }
        }
        return findMsysInPath();
    }

    private static String findMsysInPath() {
        for (String dir : Path.getPath()) {
            dir = dir.toLowerCase().replace("\\", "/"); // NOI18N
            if (dir.contains("/msys/1.0") && dir.contains("/bin")) { // NOI18N
                if (new File(dir).exists()) {
                    return dir;
                }
            }
        }
        return null;
    }

    private static String readRegistry(String key, String pattern) {
        String cachedResult = commandCache.get("reg " + key); // NOI18N
        if (cachedResult != null) {
            return cachedResult;
        }
        if (ToolchainManagerImpl.TRACE) {
            System.err.println("Read registry " + key); // NOI18N
        }
        String base = null;
        Pattern p = Pattern.compile(pattern);
        WindowsRegistryIterator regIterator = WindowsRegistryIterator.get(key, null, true);
        try {
            while (regIterator.hasNext()) {
                String[] res = regIterator.next();
                if (res != null) {
                    for (String line : res) {
                        if (ToolchainManagerImpl.TRACE) {
                            System.err.println("\t" + line); // NOI18N
                        }
                        Matcher m = p.matcher(line.trim());
                        if (m.find() && m.groupCount() == 1) {
                            base = m.group(1).trim();
                            if (ToolchainManagerImpl.TRACE) {
                                System.err.println("\tFound " + base); // NOI18N
                            }
                            return base;
                        }
                    }
                }
            }
        } finally {
            commandCache.put("reg " + key, base == null ? "" : base); // NOI18N
        }
        return null;
    }

    private static String getCommandOutput(String path, String command, String flags, boolean bothStreams) {
        String res = commandCache.get(command+" "+flags); // NOI18N
        if (res != null) {
            //System.err.println("Get command output from cache #"+command); // NOI18N
            return res;
        }
        ArrayList<String> args = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(flags," "); // NOI18N
        while(st.hasMoreTokens()) {
            args.add(st.nextToken());
        }
        if (path == null) {
            path = ""; // NOI18N
        }
        ExitStatus status = ProcessUtils.executeInDir(path, ExecutionEnvironmentFactory.getLocal(), command, args.toArray(new String[args.size()]));
        StringBuilder buf = new StringBuilder();
        //if (status.isOK()){
        if (status.getOutputString() != null) {
            buf.append(status.getOutputString());
        }
        buf.append('\n');
        if (bothStreams) {
            if (status.getErrorString() != null) {
                buf.append(status.getErrorString());
            }
        }
        commandCache.put(command+" "+flags, buf.toString()); // NOI18N
        return buf.toString();
    }
}
