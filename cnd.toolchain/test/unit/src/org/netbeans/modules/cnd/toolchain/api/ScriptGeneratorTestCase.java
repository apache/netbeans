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
package org.netbeans.modules.cnd.toolchain.api;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainScriptGenerator;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainManagerImpl;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.openide.util.Exceptions;

/**
 *
 */
public class ScriptGeneratorTestCase extends NativeExecutionBaseTestCase {

    public ScriptGeneratorTestCase(String testName) {
        super(testName);
    }

    public void testScript() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        HostInfo info = HostInfoUtils.getHostInfo(env);
        String s = ToolchainScriptGenerator.generateScript(null, info);
        runScript(s, info, env, true);
        String path = "/opt/solarisstudio12.3/bin";
        s = ToolchainScriptGenerator.generateScript(path, info);
        System.err.println(path);
        runScript(s, info, env, false);
    }

    public void runScript(String script, HostInfo info, ExecutionEnvironment env, boolean full) {
        String platform = null;
        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
        pb.setExecutable(info.getShell()).setArguments("-s"); // NOI18N
        ProcessUtils.ExitStatus res = ProcessUtils.execute(pb, script.getBytes());

        List<String> lines = res.getOutputLines();
        if (res.exitCode != 0) {
            assert false;
        } else {
            int i = 0;
            for (String s : lines) {
                System.err.println(s);
                if (full) {
                    if (i == 0) {
                        platform = s;
                    } else {
                        checkLine(s, platform);
                    }
                } else {
                    checkLine(s, info);
                }
                i++;
            }
        }
    }

    private void checkLine(String s, String platform) {
        String[] arr = s.split(";");
        assert arr.length > 1;
        String flavor = arr[0];
        int p = -1;
        if ("PLATFORM_SOLARIS_SPARC".equals(platform)) {
            p = PlatformTypes.PLATFORM_SOLARIS_SPARC;
        } else if ("PLATFORM_SOLARIS_INTEL".equals(platform)) {
            p = PlatformTypes.PLATFORM_SOLARIS_INTEL;
        } else if ("PLATFORM_LINUX".equals(platform)) {
            p = PlatformTypes.PLATFORM_LINUX;
        } else if ("PLATFORM_WINDOWS".equals(platform)) {
            p = PlatformTypes.PLATFORM_WINDOWS;
        } else if ("PLATFORM_MACOSX".equals(platform)) {
            p = PlatformTypes.PLATFORM_MACOSX;
        } else if ("PLATFORM_NONE".equals(platform)) {
            p = PlatformTypes.PLATFORM_NONE;
        }
        for(ToolchainDescriptor toolchain : ToolchainManagerImpl.getImpl().getToolchains(p)) {
            if (toolchain.getName().equals(flavor)) {
                return;
            }
        }
        assert false;
    }
    
    private void checkLine(String s, HostInfo host) {
        String[] arr = s.split(";");
        assert arr.length > 1;
        String flavor = arr[0];
        int p;
        switch(host.getOSFamily()) {
            case LINUX:
                p = PlatformTypes.PLATFORM_LINUX;
                break;
            case MACOSX:
                p = PlatformTypes.PLATFORM_MACOSX;
                break;
            case SUNOS:
                if (host.getCpuFamily() == HostInfo.CpuFamily.SPARC) {
                    p = PlatformTypes.PLATFORM_SOLARIS_SPARC;
                } else {
                    p = PlatformTypes.PLATFORM_SOLARIS_INTEL;
                }
                break;
            case WINDOWS:
                p = PlatformTypes.PLATFORM_WINDOWS;
                break;
            case FREEBSD:
            case UNKNOWN:
            default:
                p = PlatformTypes.PLATFORM_NONE;
                break;
        }
        for(ToolchainDescriptor toolchain : ToolchainManagerImpl.getImpl().getToolchains(p)) {
            if (toolchain.getName().equals(flavor)) {
                return;
            }
        }
        assert false;
    }
}