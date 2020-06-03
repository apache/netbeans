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
package org.netbeans.modules.dlight.sendto.util;

import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileObject;

/**
 *
 */
public final class Utils {
    private static final String SHELL_MACRO = "${SHELL}"; //NOI18N
    private static final String DEFAULT_SHELL = "/bin/sh"; //NOI18N

    private Utils() {
    }

    public static ExecutionEnvironment getExecutionEnvironment(final FileObject fo) {
        if (fo == null) {
            throw new NullPointerException();
        }

        ExecutionEnvironment result = null;

        URL url = fo.toURL();

        if (url == null) {
            return ExecutionEnvironmentFactory.getLocal();
        }

        String protocol = url.getProtocol();

        if ("rfs".equals(protocol)) { // NOI18N
            result = ExecutionEnvironmentFactory.createNew(url.getUserInfo(), url.getHost(), url.getPort());
        }

        return result == null ? ExecutionEnvironmentFactory.getLocal() : result;
    }

    public static String substituteShell(String scriptExecutor, ExecutionEnvironment env) {
        if (scriptExecutor.indexOf(SHELL_MACRO) >= 0 || scriptExecutor.isEmpty()) {
            String shell = DEFAULT_SHELL;

            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                String hostShell = hostInfo.getShell();

                if (hostShell != null) {
                    shell = hostShell;
                }

                if (scriptExecutor.isEmpty()) {
                    scriptExecutor = shell;
                }
            } catch (IOException ex) {
            } catch (ConnectionManager.CancellationException ex) {
            }
            if (scriptExecutor.isEmpty()) {
                scriptExecutor = shell;
            } else {
                scriptExecutor = scriptExecutor.replace(SHELL_MACRO, shell); //NOI18N
            }
        }
        return scriptExecutor;
    }
}
