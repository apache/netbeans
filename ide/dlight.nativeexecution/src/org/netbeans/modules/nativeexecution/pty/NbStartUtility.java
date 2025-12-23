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
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Andrew
 */
public class NbStartUtility extends HelperUtility {

    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("enable.nbstart", "true")); // NOI18N
    private static final NbStartUtility instance = new NbStartUtility();

    public NbStartUtility() {
        super("bin/nativeexecution/${osname}-${platform}${_isa}/pty"); // NOI18N
    }

    public static NbStartUtility getInstance() {
        return instance;
    }

    @Override
    protected File getLocalFile(final HostInfo hostInfo) throws MissingResourceException {
        String osname = hostInfo.getOS().getFamily().cname();
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
        } catch (IOException ex) {
            return false;
        } catch (CancellationException ex) {
            return false;
        }
    }

    public boolean isSupported(HostInfo hostInfo) {
        if (!ENABLED) {
            return false;
        }

        try {
            switch (hostInfo.getOS().getFamily()) {
                case MACOSX:
                case LINUX:
                    try {
                        return getLocalFile(hostInfo) != null;
                    } catch (MissingResourceException ex) {
                    }
                    return false;
                case WINDOWS:
                case FREEBSD:
                    // For now will disable it on Windows, as there are some
                    // side-effects with paths (need deeper studying)
//                    Shell activeShell = WindowsSupport.getInstance().getActiveShell();
//                    if (activeShell == null || !Shell.ShellType.CYGWIN.equals(activeShell.type)) {
//                        return false;
//                    }
//                    return getPath(executionEnvironment) != null;
                    return false;
                default:
                    return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }
}
