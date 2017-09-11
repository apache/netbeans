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
            String nbstart = NbStartUtility.getInstance().getPath(execEnv, info);
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
            } catch (Exception ex) {
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
            File _userDirFile = null;
            String _userDir = null;
            String ioUserDir = System.getProperty("user.home"); // NOI18N

            /**
             * Some magic with temp dir... In case of non-ascii chars in
             * username use hashcode instead of plain name as in case of MinGW
             * (without cygwin) execution may (will) fail...
             */
            String username = environment.get("USERNAME"); // NOI18N

            if (username != null) {
                for (int i = 0; i < username.length(); i++) {
                    char c = username.charAt(i);

                    if (Character.isDigit(c) || c == '_') {
                        continue;
                    }

                    if (c >= 'A' && c <= 'Z') {
                        continue;
                    }

                    if (c >= 'a' && c <= 'z') {
                        continue;
                    }

                    username = "" + username.hashCode(); // NOI18N
                    break;
                }
            }

            _userDirFile = new File(ioUserDir); // NOI18N
            _userDir = _userDirFile.getAbsolutePath();

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
            return getTempDir() + "/env"; // NOI18N
        }
    }
}
