/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            return lookup.keySet().contains(c);
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
            builder.append(text.substring(lastChange, text.length()));
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
