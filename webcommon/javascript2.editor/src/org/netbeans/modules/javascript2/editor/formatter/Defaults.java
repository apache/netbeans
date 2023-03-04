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
package org.netbeans.modules.javascript2.editor.formatter;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public final class Defaults {

    private static final Map<String, String> JSON_SPECIAL_VALUES = new HashMap<>();

    static {
        JSON_SPECIAL_VALUES.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        JSON_SPECIAL_VALUES.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS.name());
    }

    public static Provider getInstance(String mimeType) {
        if (JsTokenId.JAVASCRIPT_MIME_TYPE.equals(mimeType)) {
            return new FmtOptions.BasicDefaultsProvider();
        } else if (JsTokenId.JSON_MIME_TYPE.equals(mimeType)) {
            Provider basic = new FmtOptions.BasicDefaultsProvider();
            return new ProxyDefaultsProvider(basic, JSON_SPECIAL_VALUES);
        }
        throw new IllegalStateException("Unsupported mime type " + mimeType);
    }


    public static interface Provider {

        int getDefaultAsInt(String key);

        boolean getDefaultAsBoolean(String key);

        String getDefaultAsString(String key);
    }

    public static class ProxyDefaultsProvider implements Provider {

        private final Provider provider;

        private final Map<String, String> defaults;

        public ProxyDefaultsProvider(Provider provider, Map<String, String> defaults) {
            this.provider = provider;
            this.defaults = defaults;
        }

        @Override
        public int getDefaultAsInt(String key) {
            synchronized (defaults) {
                if (defaults.containsKey(key)) {
                    return Integer.parseInt(defaults.get(key));
                }
            }
            return provider.getDefaultAsInt(key);
        }

        @Override
        public boolean getDefaultAsBoolean(String key) {
            synchronized (defaults) {
                if (defaults.containsKey(key)) {
                    return Boolean.parseBoolean(defaults.get(key));
                }
            }
            return provider.getDefaultAsBoolean(key);
        }

        @Override
        public String getDefaultAsString(String key) {
            synchronized (defaults) {
                if (defaults.containsKey(key)) {
                    return defaults.get(key);
                }
            }
            return provider.getDefaultAsString(key);
        }

    }
}
