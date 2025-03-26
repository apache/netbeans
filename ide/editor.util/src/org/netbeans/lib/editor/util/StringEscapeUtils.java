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
package org.netbeans.lib.editor.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.52
 */
public class StringEscapeUtils {

    private static enum Escape {
        AMP('&', "&amp;"), QUOT('\"', "&quot;"), LT('<', "&lt;"), GT('>', "&gt;"); // NOI18N

        private final Character character;
        private final String escapedString;
        private static final Map<Character, Escape> lookup = new HashMap<Character, Escape>();
        static {
            for (Escape d : Escape.values()) {
                lookup.put(d.getCharacter(), d);
            }
        }

        private Escape(char character, String escapedChar) {
            this.character = character;
            this.escapedString = escapedChar;
        }

        public Character getCharacter() {
            return character;
        }

        public String getEscapedString() {
            return escapedString;
        }

        public static Escape get(char c) {
            return lookup.get(c);
        }

        public static boolean isInBasicEscape(char c) {
            return lookup.containsKey(c);
        }
    }

    /*
     * Escapes the characters with HTML entities.
     *
     * It changes basic characters ", &, <, >
     */
    public static String escapeHtml(String text) {
        StringBuilder builder = null;
        int lastChange = 0;
        for (int i = 0; i < text.length(); i++) {
            final char chr = text.charAt(i);
            if (Escape.isInBasicEscape(chr)) {
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(text.substring(lastChange, i));
                lastChange = i + 1;
                builder.append(Escape.get(chr).getEscapedString());
            }
        }
        if (builder == null) {
            return text;
        } else {
            builder.append(text.substring(lastChange));
            return builder.toString();
        }
    }

    /*
     * Inversion function of escapeHtml.
     */
    public static String unescapeHtml(String text) {
        return text
                .replace(Escape.QUOT.getEscapedString(), Escape.QUOT.getCharacter().toString())
                .replace(Escape.LT.getEscapedString(), Escape.LT.getCharacter().toString())
                .replace(Escape.GT.getEscapedString(), Escape.GT.getCharacter().toString())
                .replace(Escape.AMP.getEscapedString(), Escape.AMP.getCharacter().toString());
    }

}
