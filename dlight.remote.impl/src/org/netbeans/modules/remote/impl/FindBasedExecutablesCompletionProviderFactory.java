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

package org.netbeans.modules.remote.impl;

import org.netbeans.modules.remote.api.ui.AutocompletionProvider;
import org.netbeans.modules.remote.ui.spi.AutocompletionProviderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */

//@ServiceProvider(service = AutocompletionProviderFactory.class)
// This provider is not, actualy, very useful, as it doesn't give the full path
// to the executable at the end... Plus in dialogs it is not a common practice
// to have completion for executables, taken from PATH...
//
//@ServiceProvider(service = AutocompletionProviderFactory.class)

public class FindBasedExecutablesCompletionProviderFactory implements AutocompletionProviderFactory {

    public AutocompletionProvider newInstance(ExecutionEnvironment env) {
        try {
            return new Provider(env);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public boolean supports(ExecutionEnvironment env) {
        return ConnectionManager.getInstance().isConnectedTo(env);
    }

    private final static class Provider implements AutocompletionProvider {

        private String[] executables = null;
        private final FutureTask<String[]> fetchTask;

        private Provider(ExecutionEnvironment env) throws IOException {
            fetchTask = new FutureTask<>(new Find(env));
            RequestProcessor.getDefault().post(fetchTask);
        }

        public List<String> autocomplete(String str) {
            if ("".equals(str)) { // NOI18N
                return Collections.<String>emptyList();
            }

            if (executables == null) {
                try {
                    executables = fetchTask.get();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            List<String> result = new ArrayList<>();

            boolean found = false;

            for (String exec : executables) {
                if (exec.startsWith(str)) {
                    result.add(exec);
                    found = true;
                } else if (found) {
                    break;
                }
            }

            return result;
        }

        private static final class Find implements Callable<String[]> {

            private final ExecutionEnvironment env;

            private Find(ExecutionEnvironment env) {
                this.env = env;
            }

            public String[] call() throws Exception {
                TreeSet<String> result = new TreeSet<>();

                try {
                    NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                    npb.setExecutable("/bin/sh").setArguments("-c", "find `echo $PATH|tr : ' '` -type f -perm -+x 2>/dev/null"); // NOI18N
                    ProcessUtils.ExitStatus rc = ProcessUtils.execute(npb);
                    for (String s : rc.getOutputLines()) {
                        int idx = s.lastIndexOf('/') + 1;
                        if (idx > 0) {
                            result.add(s.substring(idx));
                        }
                    }
                } catch (Exception ex) {
                }

                return result.toArray(new String[0]);
            }
        }
    }
}
