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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.compilers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetProvider;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainScriptGenerator;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 */
public class RemoteCompilerSetProvider implements CompilerSetProvider {

    private CompilerSetScriptManager manager;
    private final ExecutionEnvironment env;
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    /*package-local*/ RemoteCompilerSetProvider(ExecutionEnvironment env) {
        if (env == null) {
            throw new IllegalArgumentException("ExecutionEnvironment should not be null"); //NOI18N
        }
        this.env = env;
    }

    @Override
    public void init() {
        manager = new CompilerSetScriptManager(env);
        if (!canceled.get()) {
            manager.runScript();
        }
    }

    @Override
    public boolean cancel() {
        canceled.set(true);
        CompilerSetScriptManager aManager = manager;
        if (aManager != null) {
            return aManager.cancel();
        }
        return false;
    }

    @Override
    public int getPlatform() {
        String platform = manager.getPlatform();
        if (platform == null || platform.length() == 0) {
            RemoteUtil.LOGGER.warning("RCSP.getPlatform: Got null response on platform"); //NOI18N
            platform = ""; //NOI18N
        }
        if (platform.startsWith("Windows") || platform.startsWith("PLATFORM_WINDOWS")) { // NOI18N
            return PlatformTypes.PLATFORM_WINDOWS;
        } else if (platform.startsWith("Linux") || platform.startsWith("PLATFORM_LINUX")) { // NOI18N
            return PlatformTypes.PLATFORM_LINUX;
        } else if (platform.startsWith("SunOS")) { // NOI18N
            return platform.contains("86") ? PlatformTypes.PLATFORM_SOLARIS_INTEL : PlatformTypes.PLATFORM_SOLARIS_SPARC; // NOI18N
        } else if (platform.startsWith("PLATFORM_SOLARIS_INTEL")) { // NOI18N
            return PlatformTypes.PLATFORM_SOLARIS_INTEL;
        } else if (platform.startsWith("PLATFORM_SOLARIS_SPARC")) { // NOI18N
            return PlatformTypes.PLATFORM_SOLARIS_SPARC;
        } else if (platform.toLowerCase(Locale.getDefault()).startsWith("mac") || platform.startsWith("PLATFORM_MACOSX")) { // NOI18N
            return PlatformTypes.PLATFORM_MACOSX;
        } else {
            return PlatformTypes.PLATFORM_GENERIC;
        }
    }

    @Override
    public boolean hasMoreCompilerSets() {
        if (canceled.get()) {
            return false;
        }
        return manager.hasMoreCompilerSets();
    }

    @Override
    public String getNextCompilerSetData() {
        return manager.getNextCompilerSetData();
    }

    @Override
    public String[] getCompilerSetData(String path) {
        try {
            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
            HostInfo hinfo = HostInfoUtils.getHostInfo(env);
            pb.setExecutable(hinfo.getShell()).setArguments("-s"); // NOI18N
            ProcessUtils.ExitStatus res = ProcessUtils.execute(pb, ToolchainScriptGenerator.generateScript(path, hinfo).getBytes("UTF-8")); //NOI18N
            List<String> lines = res.getOutputLines();
            if (!res.isOK()) {
               RemoteUtil.LOGGER.log(Level.WARNING, "CSSM.runScript: FAILURE {0}", res.exitCode); // NOI18N
               ProcessUtils.logError(Level.ALL, RemoteUtil.LOGGER, res);
            } else {
                return lines.toArray(new String[lines.size()]);
            }
        } catch (CancellationException ex) {
            // don't report CancellationException
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.WARNING, "CSSM.runScript: IOException [{0}]", ex.getMessage()); // NOI18N
        }
        return null;
    }

}
