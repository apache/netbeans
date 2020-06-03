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

import java.io.File;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.remote.api.ui.AutocompletionProvider;
import org.netbeans.modules.remote.ui.spi.AutocompletionProviderFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = AutocompletionProviderFactory.class)
public class PathsCompletionProviderFactory implements AutocompletionProviderFactory {
    
    private static final RequestProcessor RP = new RequestProcessor("PathsCompletionProviderFactory", 1); // NOI18N

    @Override
    public AutocompletionProvider newInstance(ExecutionEnvironment env) {
        try {
            return new Provider(env);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    @Override
    public boolean supports(ExecutionEnvironment env) {
        return ConnectionManager.getInstance().isConnectedTo(env);
    }

    private final static class Provider implements AutocompletionProvider {

        private final static int cacheSizeLimit = 20;
        private final static int cacheLifetime = 1000 * 60 * 10; // 10 min
        private final ExecutionEnvironment env;
        private String homeDir = null;
        private final LinkedList<CachedValue> cache = new LinkedList<>();
        private final Task cleanUpTask;

        public Provider(final ExecutionEnvironment env) throws IOException {
            this.env = env;
            cleanUpTask = RP.post(new Runnable() {

                @Override
                public void run() {
                    synchronized (cache) {
                        cache.clear();
                    }
                }
            }, cacheLifetime);
        }

        @Override
        public List<String> autocomplete(String str) {
            cleanUpTask.schedule(cacheLifetime);
            boolean absolutePaths = false;

            if ("~".equals(str) || ".".equals(str)) { // NOI18N
                List<String> dir = new ArrayList<>();

                if (".".equals(str) && env.isLocal()) { // NOI18N
                    dir.add(new File("").getAbsolutePath() + '/'); // NOI18N
                } else {
                    try {
                        HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                        dir.add(hostInfo.getUserDir() + '/');
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (CancellationException ex) {
                        // don't report cancellatoin exception
                    }
                }

                return dir;
            }

            if (str.startsWith("~")) { // NOI18N
                str = str.replaceFirst("~", getHomeDir()); // NOI18N
                absolutePaths = true;
            }

            if (!str.startsWith(".") && !str.startsWith("/") && !str.startsWith("~")) { // NOI18N
                return Collections.<String>emptyList();
            }

            List<String> result = new ArrayList<>();

            int idx = str.lastIndexOf('/') + 1;
            String dir = str.substring(0, idx);
            String file = str.substring(idx);
            List<String> content = listDir(dir);

            for (String c : content) {
                if (c.startsWith(file)) {
                    if (absolutePaths) {
                        result.add(dir + c);
                    } else {
                        result.add(c);
                    }
                }
            }

            return result;
        }

        private List<String> listDir(String dir) {
            synchronized (cache) {
                for (int i = 0; i < cache.size(); i++) {
                    CachedValue cv = cache.get(i);
                    if (cv.key.equals(dir)) {
                        cache.remove(i); // touch
                        cache.add(cv);
                        return cv.value;
                    }
                }
            }

            List<String> content = new ArrayList<>();

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
            npb.setExecutable("/bin/ls").setArguments("-1FL", dir); // NOI18N

            ProcessUtils.ExitStatus result = ProcessUtils.execute(npb);
            if (result.isOK()) {
                for (String s : result.getOutputLines()) {
                    if (s.endsWith("*")) { // NOI18N
                        content.add(s.substring(0, s.length() - 1));
                    } else if (s.endsWith("/")) {// NOI18N
                        content.add(s);
                    }
                }
            }

            synchronized (cache) {
                cache.add(new CachedValue(dir, content));

                while (cache.size() > cacheSizeLimit) {
                    cache.removeFirst();
                }
            }

            return content;
        }

        private synchronized String getHomeDir() {
            if (homeDir == null) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    homeDir = hostInfo.getUserDir();
                } catch (Exception ex) {
                    // fallback... 
                    homeDir = "/home/" + env.getUser() + "/"; // NOI18N
                }
            }

            return homeDir;
        }
    }

    private final static class CachedValue {

        final String key;
        final List<String> value;

        public CachedValue(String key, List<String> value) {
            this.key = key;
            this.value = value;
        }
    }
}
