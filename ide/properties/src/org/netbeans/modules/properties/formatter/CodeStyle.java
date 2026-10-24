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

package org.netbeans.modules.properties.formatter;

import java.util.prefs.Preferences;

import javax.swing.text.Document;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.filesystems.FileObject;

import static org.netbeans.modules.properties.formatter.FmtOptions.expandTabToSpaces;
import static org.netbeans.modules.properties.formatter.FmtOptions.indentSize;
import static org.netbeans.modules.properties.formatter.FmtOptions.rightMargin;
import static org.netbeans.modules.properties.formatter.FmtOptions.spacesPerTab;
import static org.netbeans.modules.properties.formatter.FmtOptions.tabSize;
import static org.netbeans.modules.properties.formatter.FmtOptions.useProjectEncoding;

/**
 * Adapted from CodeStyle in Javascript2 Editor
 */
public final class CodeStyle {

    private final Preferences preferences;

    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }

    public static CodeStyle get(Document doc) {
        return new CodeStyle(CodeStylePreferences.get(doc).getPreferences());
    }

    public static CodeStyle get(FileObject doc) {
        return new CodeStyle(CodeStylePreferences.get(doc).getPreferences());
    }

    // General tabs and indents ------------------------------------------------

    public boolean expandTabToSpaces() {
        return preferences.getBoolean(expandTabToSpaces,  FmtOptions.getDefaultAsBoolean(expandTabToSpaces));
    }

    public int getTabSize() {
        return preferences.getInt(tabSize, FmtOptions.getDefaultAsInt(tabSize));
    }

    public int getIndentSize() {
        return preferences.getInt(indentSize, FmtOptions.getDefaultAsInt(indentSize));
    }

    public int getSpacesPerTab() {
        return preferences.getInt(spacesPerTab, FmtOptions.getDefaultAsInt(spacesPerTab));
    }

    public int getRightMargin() {
        return preferences.getInt(rightMargin, FmtOptions.getDefaultAsInt(rightMargin));
    }

    public boolean useProjectEncoding() {
        return preferences.getBoolean(useProjectEncoding, FmtOptions.getDefaultAsBoolean(useProjectEncoding));
    }

}
