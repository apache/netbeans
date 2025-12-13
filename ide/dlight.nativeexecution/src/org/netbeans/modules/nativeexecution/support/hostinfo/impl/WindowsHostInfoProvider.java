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
package org.netbeans.modules.nativeexecution.support.hostinfo.impl;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.CpuFamily;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.Shell.ShellType;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.pty.NbStartUtility;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider.class, position = 90)
public class WindowsHostInfoProvider implements HostInfoProvider {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public HostInfo getHostInfo(ExecutionEnvironment execEnv) throws IOException, InterruptedException {
        // Windows is supported for localhosts only.
        if (!execEnv.isLocal() || !Utilities.isWindows()) {
            return null;
        }

        HostInfoImpl info = new HostInfoImpl();
        info.initTmpDirs();
        info.initUserDirs();

        Shell activeShell = WindowsSupport.getInstance().getActiveShell();

        if (activeShell != null && Shell.ShellType.CYGWIN.equals(activeShell.type)) {
            String nbstart = NbStartUtility.getInstance(execEnv.isLocal()).getPath(execEnv, info);
            String envPath = info.getEnvironmentFile();
            if (nbstart != null && envPath != null) {
                ProcessBuilder pb = new ProcessBuilder(nbstart, "--dumpenv", envPath); // NOI18N
                String pathKey = WindowsSupport.getInstance().getPathKey();
                pb.environment().put(pathKey, "/usr/local/bin;" + activeShell.bindir.getAbsolutePath() + ";/bin;" + pb.environment().get(pathKey)); // NOI18N
                ExitStatus result = ProcessUtils.execute(pb);
                if (!result.isOK()) {
                    Logger.getInstance().log(Level.INFO, "Failed to call nbstart -- {0}.", result.getErrorString()); // NOI18N
                }
            }
        }

        return info;
    }

    private static class HostInfoImpl implements HostInfo {

        private final OS os;
        private final Bitness osBitness;
        private final OSFamily osFamily;
        private final String osVersion;
        private final String osName;
        private final CpuFamily cpuFamily;
        private final int cpuNum;
        private final String hostname;
        private final String shell;
        private File tmpDirFile;
        private String tmpDir;
        private File userDirFile;
        private String userDir;
        private Map<String, String> environment;

        HostInfoImpl() {
            Collator collator = Collator.getInstance(Locale.US);
            collator.setStrength(Collator.PRIMARY);
            Map<String, String> env = new TreeMap<>(collator);
            env.putAll(System.getenv());

            // Use os.arch to detect bitness.
            // Another way is described in the following article:
            // http://blogs.msdn.com/david.wang/archive/2006/03/26/HOWTO-Detect-Process-Bitness.aspx
            osBitness = ("x86".equals(System.getProperty("os.arch"))) ? Bitness._32 : Bitness._64; // NOI18N
            osFamily = OSFamily.WINDOWS;
            osVersion = System.getProperty("os.version"); // NOI18N
            osName = System.getProperty("os.name"); // NOI18N
            cpuFamily = CpuFamily.X86;
            int _cpuNum = 1;

            try {
                _cpuNum = Integer.parseInt(env.get("NUMBER_OF_PROCESSORS")); // NOI18N
            } catch (RuntimeException ex) {
            }

            cpuNum = _cpuNum;
            hostname = env.get("COMPUTERNAME"); // NOI18N
            shell = WindowsSupport.getInstance().getShell();

            if (shell != null) {
                String path = new File(shell).getParent();
                env.put("PATH", path + ";" + env.get("PATH")); // NOI18N
            }

            environment = Collections.unmodifiableMap(env);

            os = new OS() {

                @Override
                public OSFamily getFamily() {
                    return osFamily;
                }

                @Override
                public String getName() {
                    return osName;
                }

                @Override
                public String getVersion() {
                    return osVersion;
                }

                @Override
                public Bitness getBitness() {
                    return osBitness;
                }
            };
        }

        public void initTmpDirs() throws IOException {
            String ioTmpDir = System.getProperty("java.io.tmpdir"); // NOI18N
            final File ioTmpDirFile = new File(ioTmpDir);

            if (checkForNonLatin(ioTmpDir) == false) {
                log.log(Level.WARNING, "Default tmp dir [{0}] has spaces/non-latin chars in the path. " + // NOI18N
                        "It is recommended to use a path without spaces/non-latin chars for tmp dir. " + // NOI18N
                        "Either change TEMP environment variable in System Properties or use " + // NOI18N
                        "-J-Djava.io.tmpdir=c:\\tmp to change the temp dir.", ioTmpDir); // NOI18N
            }

            /**
             * Some magic with temp dir... In case of non-ascii chars in
             * username use hashcode instead of plain name as in case of MinGW
             * (without cygwin) execution may (will) fail...
             */
            String username = environment.get("USERNAME"); // NOI18N

            if (checkForNonLatin(username) == false) {
                username = "" + username.hashCode(); // NOI18N
            }

            int suffix = 0;

            File tmpDirBase = new File(ioTmpDir, "dlight_" + username); // NOI18N
            tmpDirBase.mkdirs();

            while (!tmpDirBase.canWrite() && suffix < 5) {
                log.log(Level.WARNING, "WindowsHostInfoProvider: {0} is not writable", tmpDirBase.getPath()); // NOI18N
                suffix++;
                tmpDirBase = new File(ioTmpDir, "dlight_" + username + "_" + suffix); // NOI18N
                tmpDirBase.mkdirs();
            }

            if (tmpDirBase.canWrite()) {
                ioTmpDir = tmpDirBase.getPath();
                String nbKey = HostInfoFactory.getNBKey();
                suffix = 0;
                tmpDirBase = new File(ioTmpDir, nbKey);
                tmpDirBase.mkdirs();

                while (!tmpDirBase.canWrite() && suffix < 5) {
                    log.log(Level.WARNING, "WindowsHostInfoProvider: {0} is not writable", tmpDirBase.getPath()); // NOI18N
                    suffix++;
                    tmpDirBase = new File(ioTmpDir, nbKey + "_" + suffix); // NOI18N
                    tmpDirBase.mkdirs();
                }
            }

            if (!tmpDirBase.canWrite()) {
                tmpDirBase = ioTmpDirFile;
            }

            if (!tmpDirBase.canWrite()) {
                log.log(Level.WARNING, "WindowsHostInfoProvider: {0} is not writable", tmpDirBase.getPath()); // NOI18N
            }

            tmpDir = (shell == null) ? tmpDirBase.getPath()
                    : WindowsSupport.getInstance().convertToShellPath(tmpDirBase.getPath());

            tmpDirFile = tmpDirBase;
        }

        public void initUserDirs() throws IOException {
            String ioUserDir = System.getProperty("user.home"); // NOI18N

            File _userDirFile = new File(ioUserDir);
            String _userDir = _userDirFile.getAbsolutePath();

            if (shell != null) {
                _userDir = WindowsSupport.getInstance().convertToShellPath(_userDir);
            }


            userDirFile = _userDirFile;
            userDir = _userDir;
        }

        @Override
        public OS getOS() {
            return os;
        }

        @Override
        public CpuFamily getCpuFamily() {
            return cpuFamily;
        }

        @Override
        public int getCpuNum() {
            return cpuNum;
        }

        @Override
        public OSFamily getOSFamily() {
            return osFamily;
        }

        @Override
        public String getHostname() {
            return hostname;
        }

        @Override
        public String getShell() {
            return shell;
        }

        @Override
        public String getLoginShell() {
            return shell;
        }

        @Override
        public String getTempDir() {
            return tmpDir;
        }

        @Override
        public File getTempDirFile() {
            return tmpDirFile;
        }

        @Override
        public String getUserDir() {
            return userDir;
        }

        @Override
        public File getUserDirFile() {
            return userDirFile;
        }

        @Override
        public long getClockSkew() {
            return 0;
        }

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Map<String, String> getEnvironment() {
            return environment;
        }

        @Override
        public int getUserId() {
            return 0; // no implementation for Windows so far
        }

        @Override
        public int getGroupId() {
            return 0; // no implementation for Windows so far
        }

        @Override
        public int[] getAllGroupIDs() {
            return new int[0]; // no implementation for Windows so far
        }

        @Override
        public String getGroup() {
            return ""; // no implementation for Windows so far
        }

        @Override
        public String[] getAllGroups() {
            return new String[0]; // no implementation for Windows so far
        }

        private boolean checkForNonLatin(String str) {
            if (str == null) {
                // NULL is OK?
                return true;
            }

            String okChars = "~-_/\\:."; // NOI18N

            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);

                if (Character.isDigit(c) || okChars.indexOf(c) >= 0) {
                    continue;
                }

                if (c >= 'A' && c <= 'Z') {
                    continue;
                }

                if (c >= 'a' && c <= 'z') {
                    continue;
                }

                return false;
            }

            return true;
        }

        @Override
        public String getEnvironmentFile() {
            Shell activeShell = WindowsSupport.getInstance().getActiveShell();
            if(activeShell != null && activeShell.type == ShellType.WSL) {
                return null;
            } else {
                return getTempDir() + "/env"; // NOI18N
            }
        }
    }
}
