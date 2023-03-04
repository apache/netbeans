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
package org.netbeans.modules.nativeexecution.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

public final class EnvReader implements Callable<Map<String, String>> {

    private final InputStream is;
    private final boolean remote;

    public EnvReader(final InputStream is, final boolean remote) {
        this.is = is;
        this.remote = remote;
    }

    @Override
    public Map<String, String> call() throws Exception {
        Map<String, String> result = new HashMap<>();
        BufferedReader br = ProcessUtils.getReader(is, remote);
        String s = null;
        StringBuilder buffer = new StringBuilder();

        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            s = br.readLine();

            if (s == null) {
                break;
            }

            if (s.trim().length() == 0) {
                continue;
            }

            buffer.append(s.trim());

            if (s.charAt(s.length() - 1) != '\\') {
                String str = buffer.toString();
                buffer.setLength(0);

                int epos = str.indexOf('=');

                if (epos < 0) {
                    continue;
                }

                String var = str.substring(0, epos);
                String val = str.substring(epos + 1);
                result.put(var.trim(), val.trim());
            }
        }

        return result;
    }
}
