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
package org.netbeans.modules.python.editor;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.util.Exceptions;

/**
 * Override the indent action; see
 *   http://www.netbeans.org/issues/show_bug.cgi?id=150830
 * for justification.
 *
 */
public class IndentAction extends BaseKit.InsertTabAction {
    public IndentAction() {
    }

    @Override
    public Class getShortDescriptionBundleClass() {
        return IndentAction.class;
    }

    @Override
    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }

            try {
                final BaseDocument doc = (BaseDocument)target.getDocument();
                final int offset = target.getCaretPosition();
                if ((target.getSelectionStart() == target.getSelectionEnd()) &&
                   (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset))) {
                    doc.runAtomicAsUser(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DocumentUtilities.setTypingModification(doc, true);
                                int indentLevelSize = IndentUtils.indentLevelSize(doc);
                                int column = Utilities.getVisualColumn(doc, offset);
                                // Truncate
                                column = (column / indentLevelSize) * indentLevelSize + indentLevelSize;
                                GsfUtilities.setLineIndentation(doc, offset, column);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            } finally {
                                DocumentUtilities.setTypingModification(doc, false);
                            }
                        }
                    });
                    return;
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }

            // The behavior is correct when you have text on the line so just
            // delegate
            super.actionPerformed(evt, target);
        }
    }
}
