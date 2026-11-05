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
package org.netbeans.modules.nativeexecution.pty;

import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HelperUtility;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.Shell.ShellType;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Andrew
 */
public class NbStartUtility extends HelperUtility {

    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("enable.nbstart", "true")); // NOI18N
    private static final NbStartUtility instanceRemote = new NbStartUtility(false);
    private static final NbStartUtility instanceLocal = new NbStartUtility(true);

    // Hack to be able to differentiate between local and remote execution
    // getLocalFile(Hostinfo) needs this on Windows WSL. Implemented like this
    // as HelperUtility is exported and Hostinfo can't be used to detect if
    // execution is local
    private final boolean local;

    public NbStartUtility(boolean local) {
        super("bin/nativeexecution/${osname}-${platform}${_isa}/pty"); // NOI18N
        this.local = local;
    }

    public static NbStartUtility getInstance(boolean local) {
        return local ? instanceLocal : instanceRemote;
    }

    @Override
    protected File getLocalFile(final HostInfo hostInfo) throws MissingResourceException {
        String osname = hostInfo.getOS().getFamily().cname();
        Shell activeShell = WindowsSupport.getInstance().getActiveShell();
        if(local && activeShell != null && activeShell.type == Shell.ShellType.WSL) {
            osname = "Linux";
        }
        String platform = hostInfo.getCpuFamily().name().toLowerCase();
        String bitness = hostInfo.getOS().getBitness() == HostInfo.Bitness._64 ? "_64" : ""; // NOI18N

        // This method is called while HostInfo initialization so we cannot
        // use MacroExpander here (the same is for parent methods)
        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        StringBuilder path = new StringBuilder("bin/nativeexecution/"); // NOI18N
        path.append(osname).append('-').append(platform).append(bitness).append("/pty"); // NOI18N

        File file = fl.locate(path.toString(), codeNameBase, false);

        if (file == null || !file.exists()) {
            throw new MissingResourceException(path.toString(), null, null);
        }

        return file;
    }

    public boolean isSupported(ExecutionEnvironment executionEnvironment) {
        try {
            return isSupported(HostInfoUtils.getHostInfo(executionEnvironment));
        } catch (IOException | CancellationException ex) {
            return false;
        }
    }

    public boolean isSupported(HostInfo hostInfo) {
        if (!ENABLED) {
            return false;
        }

        try {
            switch (hostInfo.getOS().getFamily()) {
                case MACOSX, SUNOS, LINUX -> {
                    try {
                        return getLocalFile(hostInfo) != null;
                    } catch (MissingResourceException ex) {
                    }
                    return false;
                }
                case WINDOWS -> {
                    Shell shell = WindowsSupport.getInstance().getActiveShell();
                    if(shell != null && shell.getValidationStatus().isValid() && shell.type == ShellType.WSL) {
                        return true;
                    } else {
                        return false;
                    }
                }
                case FREEBSD -> {
                    // For now will disable it on Windows, as there are some
                    // side-effects with paths (need deeper studying)
//                    Shell activeShell = WindowsSupport.getInstance().getActiveShell();
//                    if (activeShell == null || !Shell.ShellType.CYGWIN.equals(activeShell.type)) {
//                        return false;
//                    }
//                    return getPath(executionEnvironment) != null;
                    return false;
                }
                default -> {
                    return false;
                }
            }
        } catch (Exception ex) {
            return false;
        }
    }
}
