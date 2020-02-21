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