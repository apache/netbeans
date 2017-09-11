/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
