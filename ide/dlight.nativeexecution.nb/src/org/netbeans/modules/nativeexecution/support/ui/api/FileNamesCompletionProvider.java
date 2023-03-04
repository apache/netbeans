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
package org.netbeans.modules.nativeexecution.support.ui.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author akrasny
 */
public abstract class FileNamesCompletionProvider implements AutocompletionProvider {

    private static final int cacheSizeLimit = 20;
    private static final int cacheLifetime = 1000 * 60 * 10; // 10 min
    private final ExecutionEnvironment env;
    private final LinkedList<CachedValue> cache = new LinkedList<>();
    private final Task cleanUpTask;
    private final AtomicBoolean enabled = new AtomicBoolean();
    private final ConnectionListener listener = new Listener();
   // private final static Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();

    public FileNamesCompletionProvider(final ExecutionEnvironment env) {
        this.env = env;
        ConnectionManager.getInstance().addConnectionListener(listener);
        enabled.set(ConnectionManager.getInstance().isConnectedTo(env));
        cleanUpTask = RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                synchronized (cache) {
                    cache.clear();
                }
            }
        }, cacheLifetime);
    }

    @Override
    public final List<String> autocomplete(String str) {
        if (!enabled.get()) {
            return Collections.emptyList();
        }

        cleanUpTask.schedule(cacheLifetime);

        if (!str.startsWith(".") && !str.startsWith("/") && !str.startsWith("~")) { // NOI18N
            return Collections.<String>emptyList();
        }

        List<String> result = new ArrayList<>();

        int idx = str.lastIndexOf('/') + 1;
        String dir = str.substring(0, idx);
        String file = str.substring(idx);
        List<String> content = getList(dir);

        for (String c : content) {
            if (c.startsWith(file)) {
                result.add(c);
            }
        }

        return result;
    }

    protected abstract List<String> listDir(String dir);

    private List<String> getList(String dir) {
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

        List<String> content = null;

        try {
            content = listDir(dir);
        } catch (Throwable th) {
//            if (log.isLoggable(Level.WARNING)) {
//                log.log(Level.WARNING, "Exception in " + getClass().getName() + ".listDir(" + dir + ")", th); // NOI18N
//            }
        }

        if (content == null) {
            content = Collections.emptyList();
        }

        synchronized (cache) {
            cache.add(new CachedValue(dir, content));

            while (cache.size() > cacheSizeLimit) {
                cache.removeFirst();
            }
        }

        return content;
    }

    private class Listener implements ConnectionListener {

        @Override
        public void connected(ExecutionEnvironment environment) {
            if (env.equals(environment)) {
                enabled.set(true);
            }
        }

        @Override
        public void disconnected(ExecutionEnvironment environment) {
            if (env.equals(environment)) {
                enabled.set(false);
            }
        }
    }

    private static final class CachedValue {

        final String key;
        final List<String> value;

        public CachedValue(String key, List<String> value) {
            this.key = key;
            this.value = value;
        }
    }
}
