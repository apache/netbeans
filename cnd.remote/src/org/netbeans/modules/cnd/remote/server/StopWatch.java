/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
