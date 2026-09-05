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
package org.netbeans.modules.php.blade.editor.format;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.php.blade.editor.preferences.GeneralPreferencesUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author bhaidu
 */
public class BladeFormatter implements Formatter {
    private static final Logger LOGGER = Logger.getLogger(BladeFormatter.class.getName());

    public BladeFormatter() {
    }

    @Override
    public void reformat(Context context, ParserResult compilationInfo) {

        LineDocument doc = LineDocumentUtils.as(context.document(), LineDocument.class);
        if (doc == null) {
            return;
        }
        if (context.isIndent() && !isBladeIndentEnabled(doc)) {
            return;
        } else if (!isBladeFormattingEnabled(doc)) {
            return;
        }

        int indentSize = getIndentSize(context.document());

        long start = System.currentTimeMillis();
        Runnable rn = new Runnable() {
            @Override
            public void run() {
                //the text can update between reformatting
                LineDocument doc = LineDocumentUtils.as(context.document(), LineDocument.class);
                if (doc == null) {
                    return;
                }
                try {
                    String currentText = doc.getText(0, doc.getLength());

                    if (context.isIndent()) {
                        int lineStart = context.lineStartOffset(context.caretOffset());
                        String lineText = doc.getText(lineStart, context.caretOffset() - lineStart);
                        if (!lineText.isEmpty() && lineText.replaceAll(" ", "").isEmpty()) { //NOI18N
                            return;
                        }
                    }
                    (new BladeFormatterService()).format(context, currentText, indentSize);

                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        };

        String lookupClassName = Lookup.getDefault().getClass().getSimpleName();
        if (lookupClassName.equals("MockLookup")) { //NOI18N
            //test mode
            rn.run();
        } else {
            SwingUtilities.invokeLater(rn);
        }
        //run after html indent is finished
        
        if (LOGGER.isLoggable(Level.FINE)) {
            long end = System.currentTimeMillis();
            LOGGER.log(Level.FINE, "Reformat took: {0} ms", (end - start)); //NOI18N
        }
    }

    @Override
    public void reindent(Context context) {
        reformat(context, null);
    }

    @Override
    public boolean needsParserResult() {
        return false;
    }

    @Override
    public int indentSize() {
        return 4;
    }

    @Override
    public int hangingIndentSize() {
        return 4;
    }

    static int getIndentSize(Document doc) {
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        return prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 4);
    }

    static boolean isBladeIndentEnabled(Document doc) {
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        return prefs.getBoolean(GeneralPreferencesUtils.ENABLE_INDENTATION, false);
    }

    static boolean isBladeFormattingEnabled(Document doc) {
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        return prefs.getBoolean(GeneralPreferencesUtils.ENABLE_FORMATTING, false);
    }
}
