/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.support.filesearch.impl;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchParams;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher.class, position = 100)
public class RemoteFileSearcherImpl implements FileSearcher {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public String searchFile(FileSearchParams fileSearchParams) {
        final ExecutionEnvironment execEnv = fileSearchParams.getExecEnv();

        if (execEnv.isLocal()) {
            return null;
        }

        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);

            if (hostInfo == null) {
                return null;
            }

            List<String> sp = new ArrayList<>(fileSearchParams.getSearchPaths());

            if (fileSearchParams.isSearchInUserPaths()) {
                String path = hostInfo.getEnvironment().get("PATH"); // NOI18N
                if (path != null) {
                    sp.addAll(Arrays.asList(path.split(":"))); // NOI18N
                }
            }

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(hostInfo.getShell()).setArguments("-s"); // NOI18N

            Process p = npb.call();
            ProcessUtils.ignoreProcessError(p);

            OutputStreamWriter os = new OutputStreamWriter(p.getOutputStream());
            for (String path : sp) {
                if (path.indexOf('"') >= 0) { // Will not use paths with "
                    continue;
                }

                os.append("/bin/ls \"").append(path); // NOI18N
                os.append("/" + fileSearchParams.getFilename() + "\" 2>/dev/null || \\\n"); // NOI18N
            }

            os.append("(echo \"Not Found\" && exit 1)\n"); // NOI18N
            os.append("exit $?\n"); // NOI18N

            os.flush();
            os.close();

            String line = ProcessUtils.readProcessOutputLine(p);
            int result = p.waitFor();

            return (result != 0 || line == null || "".equals(line.trim())) ? null : line.trim(); // NOI18N
        } catch (Throwable th) {
            log.log(Level.FINE, "Execption in UnixFileSearcherImpl:", th); // NOI18N
        }

        return null;
    }
}
