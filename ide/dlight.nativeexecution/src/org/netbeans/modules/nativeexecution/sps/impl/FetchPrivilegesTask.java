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
package org.netbeans.modules.nativeexecution.sps.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.Computable;
import org.netbeans.modules.nativeexecution.support.Logger;

public final class FetchPrivilegesTask implements Computable<ExecutionEnvironment, List<String>> {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public List<String> compute(ExecutionEnvironment execEnv) {
        /*
         * To find out actual privileges that tasks will have use
         * > ppriv -v $$ | grep [IL]
         *
         * and return intersection of list of I (inherit) and L (limit)
         * privileges...
         */

        ProcessUtils.ExitStatus res = null;
        try {
            String command = "/usr/bin/ppriv -v $$ | grep [IL]"; // NOI18N

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable("/bin/sh").setArguments("-c", command); // NOI18N

            res = ProcessUtils.execute(npb);

            if (res.exitCode != 0) {
                throw new IOException("Unable to get current privileges. Command " + // NOI18N
                        command + " failed with code " + res.exitCode); // NOI18N
            }

            List<String> iprivs = new ArrayList<>();
            List<String> lprivs = new ArrayList<>();

            List<String> out = res.getOutputLines();

            for (String str : out) {
                if (str.contains("I:")) { // NOI18N
                    String[] privs = str.substring(
                            str.indexOf(": ") + 2).split(","); // NOI18N
                    iprivs = Arrays.asList(privs);
                } else if (str.contains("L:")) { // NOI18N
                    String[] privs = str.substring(
                            str.indexOf(": ") + 2).split(","); // NOI18N
                    lprivs = Arrays.asList(privs);
                }
            }

            if (iprivs == null || lprivs == null) {
                return Collections.emptyList();
            }

            List<String> real_privs = new ArrayList<>();

            for (String ipriv : iprivs) {
                if (lprivs.contains(ipriv)) {
                    real_privs.add(ipriv);
                }
            }

            return real_privs;
        } catch (ConnectException ex) {
            return Collections.emptyList();
        } catch (IOException ex) {
            log.fine(ex.getMessage());
            if (res != null) {
                try {
                    ProcessUtils.logError(Level.FINE, log, res);
                } catch (IOException ioex) {
                }
            }
        }

        return Collections.emptyList();
    }
}
