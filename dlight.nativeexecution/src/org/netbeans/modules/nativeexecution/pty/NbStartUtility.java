/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
    private final static NbStartUtility instance = new NbStartUtility();

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
                case SUNOS:
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
