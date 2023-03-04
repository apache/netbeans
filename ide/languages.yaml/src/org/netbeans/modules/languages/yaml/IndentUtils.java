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
package org.netbeans.modules.languages.yaml;

import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class IndentUtils {

    private IndentUtils() {
    }

    public static int getIndentSize(Document doc) {
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        return prefs.getInt(SimpleValueNames.SPACES_PER_TAB, 2);
    }

    private static void indent(final StringBuilder sb, final int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
    }

    public static String getIndentString(final int indent) {
        StringBuilder sb = new StringBuilder(indent);
        indent(sb, indent);
        return sb.toString();
    }

}
