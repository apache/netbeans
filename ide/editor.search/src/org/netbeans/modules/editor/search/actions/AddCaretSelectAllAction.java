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

package org.netbeans.modules.editor.search.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.search.EditorFindSupport;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 * Select
 */
// NOI18N
@EditorActionRegistration(
        name = "addCaretSelectAll", category = "edit.multicaret")
public class AddCaretSelectAllAction extends AbstractEditorAction {

    private static final Logger LOGGER = Logger.getLogger(AddCaretSelectAllAction.class.getName());

    public AddCaretSelectAllAction() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            Caret caret = target.getCaret();
            if (!Utilities.isSelectionShowing(caret)) {
                try {
                    int[] identifierBlock = Utilities.getIdentifierBlock((BaseDocument) target.getDocument(), caret.getDot());
                    if (identifierBlock != null) {
                        caret.setDot(identifierBlock[0]);
                        caret.moveDot(identifierBlock[1]);
                    }
                } catch (BadLocationException e) {
                    LOGGER.log(Level.WARNING, null, e);
                }
            }

            EditorFindSupport findSupport = EditorFindSupport.getInstance();
            HashMap<String, Object> props = new HashMap<>(findSupport.createDefaultFindProperties());
            String searchWord = target.getSelectedText();
            if (searchWord != null) {
                int n = searchWord.indexOf('\n');
                if (n >= 0) {
                    searchWord = searchWord.substring(0, n);
                }
                props.put(EditorFindSupport.FIND_WHAT, searchWord);
                Document doc = target.getDocument();
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui.getComponent().getClientProperty("AsTextField") == null) { //NOI18N
                    findSupport.setFocusedTextComponent(eui.getComponent());
                }
                findSupport.putFindProperties(props);
                findSupport.find(null, false);

                if (caret instanceof EditorCaret) {
                    EditorCaret editorCaret = (EditorCaret) caret;
                    try {
                        int[] blocks = findSupport.getBlocks(new int[]{-1, -1}, doc, 0, doc.getLength());
                        if (blocks[0] >= 0 && blocks.length % 2 == 0) {
                            List<Position> newCarets = new ArrayList<>();

                            for (int i = 0; i < blocks.length; i += 2) {
                                int start = blocks[i];
                                int end = blocks[i + 1];
                                if (start == -1 || end == -1) {
                                    break;
                                }
                                Position startPos = doc.createPosition(start);
                                Position endPos = doc.createPosition(end);
                                newCarets.add(endPos);
                                newCarets.add(startPos);
                            }

                            editorCaret.replaceCarets(newCarets, null); // TODO handle biases
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
}
