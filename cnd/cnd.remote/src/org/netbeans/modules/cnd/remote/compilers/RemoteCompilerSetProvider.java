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
