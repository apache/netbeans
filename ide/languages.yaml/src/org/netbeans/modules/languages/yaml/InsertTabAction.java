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
package org.netbeans.modules.languages.yaml;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class InsertTabAction extends BaseAction {

    private static final List<Action> CUSTOM_ACTIONS = List.of(new InsertTabAction());

    public InsertTabAction() {
        super(BaseKit.insertTabAction);
    }

    @Override
    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
        BaseKit.InsertTabAction insertTabAction = new BaseKit.InsertTabAction();
        insertTabAction.actionPerformed(evt, target);
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }
            final Caret caret = target.getCaret();
            final BaseDocument doc = (BaseDocument) target.getDocument();
            doc.runAtomic(new TabReplacer(doc, caret.getDot()));
        }
    }

    private static class TabReplacer implements Runnable {
        private static final String TAB_CHARACTER = "\t"; //NOI18N
        private static final Logger LOGGER = Logger.getLogger(TabReplacer.class.getName());
        private final BaseDocument baseDocument;
        private final int caretOffset;

        public TabReplacer(final BaseDocument baseDocument, final int caretOffset) {
            this.baseDocument = baseDocument;
            this.caretOffset = caretOffset;
        }

        @Override
        public void run() {
            try {
                tryReplaceTab();
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        private void tryReplaceTab() throws BadLocationException {
            int rowFirstNonWhite = Utilities.getRowFirstNonWhite(baseDocument, caretOffset);
            if (shouldBeReplaced(rowFirstNonWhite, caretOffset)) {
                replaceTab();
            }
        }

        private void replaceTab() throws BadLocationException {
            final int rowStart = LineDocumentUtils.getLineStart(baseDocument, caretOffset);
            assert caretOffset >= rowStart : "Caret: " + caretOffset + " rowStart: " + rowStart;
            final String indentString = baseDocument.getText(rowStart, caretOffset - rowStart);
            if (indentString.contains(TAB_CHARACTER)) {
                final String newIndentString = indentString.replace(TAB_CHARACTER, " ".repeat(getSpacesPerTab(baseDocument)));
                baseDocument.replace(rowStart, caretOffset - rowStart, newIndentString, null);
            }
        }

        private static boolean shouldBeReplaced(final int firstNonWhiteCharOffset, final int caretOffset) {
            return firstNonWhiteCharOffset >= caretOffset || firstNonWhiteCharOffset == -1;
        }

    }

    private static int getSpacesPerTab(Document doc) {
        return CodeStylePreferences.get(doc).getPreferences()
                .getInt(SimpleValueNames.SPACES_PER_TAB, 2);
    }

    public static List<Action> createCustomActions() {
        return CUSTOM_ACTIONS;
    }

}
