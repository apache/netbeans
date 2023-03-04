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
package org.netbeans.api.java.source.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.text.PlainDocument;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author lahvac
 */
public class Utils {

    public static Map<String, String> setCodePreferences(Map<String, String> values) {
        Preferences preferences = CodeStylePreferences.get(new PlainDocument(), JavacParser.MIME_TYPE).getPreferences();
        Map<String, String> origValues = new HashMap<String, String>();
        for (String key : values.keySet()) {
            origValues.put(key, preferences.get(key, null));
        }
        setValues(preferences, values);

        return origValues;
    }

    private static void setValues(Preferences p, Map<String, String> values) {
        for (Entry<String, String> e : values.entrySet()) {
            if (e.getValue() != null) {
                p.put(e.getKey(), e.getValue());
            } else {
                p.remove(e.getKey());
            }
        }
    }

    public static class MapBuilder<K, V> {
        public static <K, V> MapBuilder<K, V> create() {
            return new MapBuilder<K, V>();
        }
        private final Map<K, V> result = new HashMap<K, V>();
        public MapBuilder<K, V> add(K k, V v) {
            result.put(k, v);
            return this;
        }
        public Map<K, V> build() {
            return result;
        }
    }
}
