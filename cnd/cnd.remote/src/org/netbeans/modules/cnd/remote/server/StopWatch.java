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
package org.netbeans.modules.cnd.remote.server;

import java.util.HashMap;
import java.util.Map;
/**
 *
 */
public abstract class StopWatch {

    public abstract void stop();

    /**
     * Creates and starts a stopwatch
     * @param enabled allows to avoid (1) too much code in client and (2) NPE
     * @param category each output line starts from prefix; it also acts as 1-st part of key
     * @param key key.toStrinbg() to be used as additional key (2-nd part of key)
     * @param message is printed after prefix and additional key; also acts as 3-st part of key
     * NB: indentation is done by (prefix + additionalKey)
     * @return
     */
    public static StopWatch createAndStart(boolean enabled, String category, Object key, String message, Object... arguments) {
        if (!enabled) {
            return DUMMY;
        }
        StringBuilder text = new StringBuilder();
        text.append(category).append(" [").append(key).append("]: "); //NOI18N
        String indentKey = category + key;
        int indent = indent(indentKey, +1);
        for (int i = 0; i < indent; i++) {
            text.append("    "); //NOI18N
        }
        text.append(String.format(message, arguments));
        return new Impl(text, indentKey);
    }

    private static int indent(String indentKey, int delta) {
        synchronized (lock) {
            Integer indent = indents.get(indentKey);
            indent = (indent == null) ? 0 : (indent + delta);
            indents.put(indentKey, indent);
            return indent;
        }
    }

    private static class Dummy extends StopWatch {
        @Override
        public void stop() {
        }
    }
    private static final StopWatch DUMMY = new Dummy();
    private static final Object lock = new Object();
    private static final Map<String, Integer> indents = new HashMap<>();
    //private static final Map<String, Impl> instances = new HashMap<>();

    private static class Impl extends StopWatch {

        private long time;
        private final CharSequence text;
        private final String indentKey;

        private Impl(CharSequence text, String indentKey) {
            this.text = text;
            this.indentKey = indentKey;
            time = System.currentTimeMillis();
            System.err.printf("[%d] %s starting...%n", System.currentTimeMillis(), text); //NOI18N
        }

        @Override
        public void stop() {
            time = System.currentTimeMillis() - time;
            System.err.printf("[%d] %s finished in %s ms%n", System.currentTimeMillis(), text, time); //NOI18N
            indent(indentKey, -1);
        }
    }
}
