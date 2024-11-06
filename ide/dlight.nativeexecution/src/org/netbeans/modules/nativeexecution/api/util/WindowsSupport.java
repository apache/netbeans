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
package org.netbeans.modules.nativeexecution.api.util;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.Shell.ShellType;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport.ShellValidationStatus;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.windows.PathConverter;
import org.netbeans.modules.nativeexecution.support.windows.PathConverter.PathType;
import org.netbeans.modules.nativeexecution.support.windows.SimpleConverter;
import org.openide.util.Utilities;

/**
 * Currently remote Windows execution is not considered...
 *
 */
public final class WindowsSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final Object initLock = new Object();
    private static final String SHELL_PROVIDER = System.getProperty("org.netbeans.modules.nativeexecution.api.util.WindowsSupport.shellProvider", null);
    private static final String WSL_REG_BASE = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Lxss";
    private static WindowsSupport instance;
    private boolean initialized = false;
    private Shell activeShell = null;
    private PathConverter pathConverter = null;
    private final AtomicReference<String> pathKeyRef = new AtomicReference<>();
    private Charset charset;

    private WindowsSupport() {
    }

    public static WindowsSupport getInstance() {
        synchronized (initLock) {
            if (instance == null) {
                instance = new WindowsSupport();
            }
            if (!instance.initialized) {
                instance.init();
            }
            return instance;
        }
    }

    public String getShell() {
        return activeShell == null ? null : activeShell.shell;
    }

    public void init() {
        init(null);

        if (Utilities.isWindows()) {
            if (activeShell == null) {
                log.fine("WindowsSupport: no shell found"); // NOI18N
            } else {
                log.log(Level.FINE, "WindowsSupport: found {0} shell in {1}", new Object[]{activeShell.type, activeShell.bindir.getAbsolutePath()}); // NOI18N
            }
        }
    }

    public void init(String searchDir) {
        synchronized (initLock) {
            if (!Utilities.isWindows()) {
                return;
            }

            pathConverter = new SimpleConverter();
            activeShell = findShell(searchDir);
            initCharset();
            initialized = true;
        }
    }

    private boolean isCheckShellProvider(ShellType shellType) {
        if (shellType == null) {
            return true;
        }
        return SHELL_PROVIDER == null || SHELL_PROVIDER.trim().isEmpty() || shellType.name().equals(SHELL_PROVIDER);
    }

    private Shell findShell(String searchDir) {
        Shell shell;
        Shell candidate = null;

        // 0. Try wsl

        if (isWslAvailable() && isCheckShellProvider(ShellType.WSL)) {
            File distributionPath = getWslDefaulDistributionFile();
            if (distributionPath != null) {
                for (String s : new String[]{"/usr/bin/bash", "/bin/bash"}) {
                    File wslShell = new File(distributionPath, s);
                    if (wslShell.exists()) {
                        candidate = new Shell(ShellType.WSL, s, wslShell.getParentFile());
                        ShellValidationStatus validationStatus = ShellValidationSupport.getValidationStatus(candidate);
                        if (validationStatus.isValid() && !validationStatus.hasWarnings()) {
                            return candidate;
                        }
                    }
                }
            }
        }

        // 1. Try to find cygwin ...

        if (isCheckShellProvider(ShellType.CYGWIN)) {
            String[][] cygwinRegKeys = new String[][]{
                new String[]{"SOFTWARE\\cygwin\\setup", "rootdir", ".*rootdir.*REG_SZ(.*)"}, // NOI18N
                new String[]{"SOFTWARE\\Wow6432Node\\cygwin\\setup", "rootdir", ".*rootdir.*REG_SZ(.*)"}, // NOI18N
                new String[]{"SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\/", "native", ".*native.*REG_SZ(.*)"}, // NOI18N
                new String[]{"SOFTWARE\\Wow6432Node\\Cygnus Solutions\\Cygwin\\mounts v2\\/", "native", ".*native.*REG_SZ(.*)"}, // NOI18N
            };

            for (String[] regKey : cygwinRegKeys) {
                shell = initShell(ShellType.CYGWIN, queryWindowsRegistry(
                        regKey[0], regKey[1], regKey[2]));

                // If found cygwin in registry - it is assumed to be valid -
                // just choose one
                if (shell != null) {
                    return shell;
                }
            }
        }

        // No cygwin in the registry...
        // try msys

        if (isCheckShellProvider(ShellType.MSYS)) {
            String[] msysRegKeys = new String[]{
                "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MSYS-1.0_is1", // NOI18N
                "SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MSYS-1.0_is1", // NOI18N
            };

            for (String regKey : msysRegKeys) {
                shell = initShell(ShellType.MSYS, queryWindowsRegistry(
                        regKey,
                        "Inno Setup: App Path", // NOI18N
                        ".*REG_SZ(.*)")); // NOI18N

                if (shell != null) {
                    // Again, if found one - use it
                    return shell;
                }
            }
        }

        // Registry search failed ;(
        // Try to find something in PATH
        String paths = System.getenv("PATH"); // NOI18N

        if (searchDir != null && searchDir.length() > 0) {
            paths = searchDir + ';' + paths;
        }

        if (paths != null) {
            for (String path : paths.split(";")) { // NOI18N
                File sh = new File(path, "bash.exe"); // NOI18N
                if (!(sh.exists() && sh.canRead())) {
                    sh = new File(path, "sh.exe"); // NOI18N
                }
                File parent = sh.getParentFile();

                if (sh.exists() && sh.canRead()) {
                    if ("bin".equals(parent.getName())) { // NOI18N
                        // Looks like we have found something...
                        // An attempt to understand what exactly we have found
                         if (new File(parent, "msysinfo").exists() && isCheckShellProvider(ShellType.MSYS)) { // NOI18N
                            // Looks like this one is msys...
                            // As no valid cygwin was found - use it
                            return new Shell(ShellType.MSYS, sh.getAbsolutePath(), parent);
                        } else if (new File(parent, "msys-2.0.dll").exists() && isCheckShellProvider(ShellType.MSYS)) { // NOI18N
                            // Looks like this one is msys2...
                            // As no valid cygwin was found - use it
                            return new Shell(ShellType.MSYS, sh.getAbsolutePath(), parent);
                        } else if (new File(parent, "cygwin1.dll").exists() && isCheckShellProvider(ShellType.CYGWIN)) { // NOI18N
                            // Looks like this one is sygwin...
                            // As no valid cygwin was found - use it
                            return new Shell(ShellType.CYGWIN, sh.getAbsolutePath(), parent);
                        } else if (new File(parent, "cygcheck.exe").exists() && isCheckShellProvider(ShellType.CYGWIN)) { // NOI18N
                            // Well ...
                            // The problem is that is is not in registry...
                            // I.e. we will use it if on msys found on the system...
                            if (candidate == null) {
                                candidate = new Shell(ShellType.CYGWIN, sh.getAbsolutePath(), parent);
                            }
                            // Still there is a chance that this installation is
                            // OK (even if it is not in the registry).
                            // This could be the case if it is in [?]:/cygwin
                            ShellValidationStatus validationStatus = ShellValidationSupport.getValidationStatus(candidate);

                            if (validationStatus.isValid() && !validationStatus.hasWarnings()) {
                                return candidate;
                            }
                        }
                    }

                }
            }
        }

        // if we found some "broken" cygwin - it will be in candidate...
        // or it will be null if nothing found
        return candidate;
    }

    public int getWinPID(int shellPID) {
        if (activeShell == null) {
            return shellPID;
        }

        ProcessBuilder pb;
        File psFile = new File(activeShell.bindir, "ps.exe"); // NOI18N

        if (!psFile.exists()) {
            return shellPID;
        }

        String psCommand = psFile.getAbsolutePath();

        switch (activeShell.type) {
            case CYGWIN -> pb = new ProcessBuilder(psCommand, "-W", "-p", Integer.toString(shellPID)); // NOI18N
            case MSYS -> pb = new ProcessBuilder(psCommand, "-W"); // NOI18N
            default -> {
                return shellPID;
            }
        }

        ExitStatus res = ProcessUtils.execute(pb);
        List<String> output = res.getOutputLines();
        Pattern pat = Pattern.compile("[I]*[\t ]*([0-9]+)[\t ]*([0-9]+)[\t ]*([0-9]+)[\t ]*([0-9]+).*"); // NOI18N
        for (String s : output) {
            Matcher m = pat.matcher(s);
            if (m.matches()) {
                int pid = Integer.parseInt(m.group(1));
                if (pid == shellPID) {
                    return Integer.parseInt(m.group(4));
                }
            }
        }
        return shellPID;
    }

    private String queryWindowsRegistry(String key, String param, String regExpr) {
        WindowsRegistryIterator registryIterator = WindowsRegistryIterator.get(key, param);
        Pattern pattern = Pattern.compile(regExpr);
        while (registryIterator.hasNext()) {
            String[] output = registryIterator.next();
            if (output != null) {
                for (String line : output) {
                    Matcher m = pattern.matcher(line);
                    if (m.matches()) {
                        return m.group(1).trim();
                    }
                }
            }
        }
        return null;
    }

    public String convertToCygwinPath(String winPath) {
        return convert(PathType.WINDOWS, PathType.CYGWIN, winPath, true);
    }

    public String convertFromCygwinPath(String cygwinPath) {
        return convert(PathType.CYGWIN, PathType.WINDOWS, cygwinPath, true);
    }

    public String convertToMSysPath(String winPath) {
        return convert(PathType.WINDOWS, PathType.MSYS, winPath, true);
    }

    public String convertFromMSysPath(String msysPath) {
        return convert(PathType.MSYS, PathType.WINDOWS, msysPath, true);
    }

    public String convertToWSL(String winPath) {
        return convert(PathType.WINDOWS, PathType.WSL, winPath, true);
    }

    /**
     * Cygwin is preferrable shell (over msys). So it cygwin is installed we
     * will always use it's for shell
     *
     * @param path
     * @return
     */
    public String convertToShellPath(String path) {
        return activeShell == null ? null : convert(PathType.WINDOWS, activeShell.type.toPathType(), path, true);
    }

    public String convertToWindowsPath(String path) {
        return activeShell == null ? null : convert(activeShell.type.toPathType(), PathType.WINDOWS, path, true);
    }

    public String convertToAllShellPaths(String paths) {
        return activeShell == null ? null : convert(PathType.WINDOWS, activeShell.type.toPathType(), paths, false);
    }

    private String convert(PathType from, PathType to, String path, boolean isSinglePath) {
        if (to == null || from == null) {
            return null;
        }

        if (from == PathType.CYGWIN || to == PathType.CYGWIN) {
            if (activeShell == null || activeShell.type != ShellType.CYGWIN) {
                // This means we don't know how to correctly deal with cygwin paths
                return null;
            }
        }

        return isSinglePath
                ? pathConverter.convert(from, to, path)
                : pathConverter.convertAll(from, to, path);
    }

    /**
     * @return charset to be used when 'communicating' with a shell
     */
    public Charset getShellCharset() {
        return charset;
    }

    private Shell initShell(ShellType type, String root) {
        if (root == null) {
            return null;
        }

        File sh = new File(root + "/bin/bash.exe"); // NOI18N
        if (!sh.exists() || !sh.canRead()) {
            sh = new File(root + "/bin/sh.exe"); // NOI18N
        }
        if (!sh.exists() || !sh.canRead()) {
            return null;
        }

        return new Shell(type, sh.getAbsolutePath(), sh.getParentFile().getAbsoluteFile());
    }

    public Shell getActiveShell() {
        return activeShell;
    }

    private void initCharset() {
        charset = Charset.defaultCharset();

        if (activeShell == null || activeShell.type != ShellType.CYGWIN) {
            return;
        }

        // 1. is LANG defined?
        try {
            // Will not use NativeProcessBuilder here. This may lead to deadlock
            // if no HostInfo is available. (See bugs #202550, #202568)
            // Actually, there is no any need in using NBP here.

            ExitStatus result = ProcessUtils.execute(new ProcessBuilder(activeShell.shell, "--login", "-c", "echo $LANG")); // NOI18N

            if (result.isOK()) {
                String shellOutput = result.getOutputString();
                int dotIndex = shellOutput.indexOf('.');
                if (dotIndex >= 0) {
                    shellOutput = shellOutput.substring(dotIndex + 1).trim();
                }
                try {
                    charset = Charset.forName(shellOutput);
                    return;
                } catch (Exception ex) {
                }
            }

            String cygwinVersion = null;
            ProcessBuilder pb = new ProcessBuilder(activeShell.bindir + "\\uname.exe", "-r"); // NOI18N
            ExitStatus res = ProcessUtils.execute(pb);
            String output = res.getOutputString();
            Pattern p = Pattern.compile("^([0-9\\.]*).*"); // NOI18N
            Matcher m = p.matcher(output);
            if (m.matches()) {
                cygwinVersion = m.group(1);
            }
            if (cygwinVersion == null) {
                return;
            }

            if (cygwinVersion.startsWith("1.7")) { // NOI18N
                charset = StandardCharsets.UTF_8;
            }
        } catch (Exception ex) {
        }
    }

    public String getPathKey() {
        if (pathKeyRef.get() == null) {
            ProcessBuilder pb = new ProcessBuilder(""); // NOI18N
            String pathKey = "PATH"; // NOI18N
            for (String key : pb.environment().keySet()) {
                if ("PATH".equalsIgnoreCase(key)) { //NOI18N
                    pathKey = key;
                    break;
                }
            }
            pathKeyRef.compareAndSet(null, pathKey);
        }
        return pathKeyRef.get();
    }

    private boolean isWslAvailable() {
        return new File(System.getenv("windir"), "system32/wsl.exe").exists();
    }

    private String getWslDefaulDistributionName() {
        if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, WSL_REG_BASE, "DefaultDistribution")) {
            String defaultDistribution = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, WSL_REG_BASE, "DefaultDistribution");
            if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, WSL_REG_BASE + "\\" + defaultDistribution, "DistributionName")) {
                String distributionName = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, WSL_REG_BASE + "\\" + defaultDistribution, "DistributionName");
                return distributionName;
            }
        }
        return null;
    }

    File getWslDefaulDistributionFile() {
        String distributionName = getWslDefaulDistributionName();
        if(distributionName != null) {
            return new File("\\\\wsl.localhost\\", distributionName);
        } else {
            return null;
        }
    }
}
