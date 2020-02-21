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
package org.netbeans.modules.cnd.remote.mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.spi.remote.HostInfoProviderFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.remote.HostInfoProviderFactory.class)
public class RemoteHostInfoProviderFactory implements HostInfoProviderFactory {

    public static class RemoteHostInfo extends HostInfoProvider {

        private final ExecutionEnvironment executionEnvironment;
        private Map<String, String> envCache = null;
        private Boolean isCshShell;

        @Override
        public boolean fileExists(String path) {
            try {
                return HostInfoUtils.fileExists(executionEnvironment, path);
            } catch (IOException | InterruptedException ex) {
                return false; // so it was before - see RemoteCommandSupport
            }
        }

        @Override
        public String getLibDir() {
            String tmpDir;
            try {
                tmpDir = HostInfoUtils.getHostInfo(executionEnvironment).getTempDir();
            } catch (Throwable ex) {
                tmpDir = "/var/tmp"; // NOI18N
            }
            String libDir = tmpDir + "/tools"; // NOI18N
            return libDir;
        }

        private RemoteHostInfo(ExecutionEnvironment executionEnvironment) {
            this.executionEnvironment = executionEnvironment;
        }

        @Override
        public synchronized PathMap getMapper() {
            return RemotePathMap.getPathMap(executionEnvironment);
        }

        @Override
        public synchronized Map<String, String> getEnv() {
            if (envCache == null) {
                envCache = new HashMap<>();
                ProcessUtils.ExitStatus rc = ProcessUtils.execute(executionEnvironment, "env"); // NOI18N
                if (rc.isOK()) {
                    String val = rc.getOutputString();
                    String[] lines = val.split("\n"); // NOI18N
                    for (int i = 0; i < lines.length; i++) {
                        int pos = lines[i].indexOf('=');
                        if (pos > 0) {
                            envCache.put(lines[i].substring(0, pos), lines[i].substring(pos + 1));
                        }
                    }
                }
            }
            return envCache;
        }
    }

    private final static Map<ExecutionEnvironment, RemoteHostInfo> env2hostinfo =
            new HashMap<>();

    public static synchronized RemoteHostInfo getHostInfo(ExecutionEnvironment execEnv) {
        RemoteHostInfo hi = env2hostinfo.get(execEnv);
        if (hi == null) {
            hi = new RemoteHostInfo(execEnv);
            env2hostinfo.put(execEnv, hi);
        }
        return hi;
    }

    public boolean canCreate(ExecutionEnvironment execEnv) {
        return execEnv.isRemote();
    }

    public HostInfoProvider create(ExecutionEnvironment execEnv) {
        return getHostInfo(execEnv);
    }
}
