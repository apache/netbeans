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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.search.EditorFindSupport;
import org.netbeans.spi.editor.AbstractEditorAction;

/** 
 * Select a whole word if there is not selection on word otherwise it adds a new caret for selected text
 */
// NOI18N
@EditorActionRegistration(
        name = "addCaretSelectNext", category = "edit.multicaret")
public class AddCaretSelectNextAction extends AbstractEditorAction {
    private static final Logger LOGGER = Logger.getLogger(AddCaretSelectNextAction.class.getName());

    public AddCaretSelectNextAction() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            Caret caret = target.getCaret();
            if (Utilities.isSelectionShowing(caret)) {
                EditorFindSupport findSupport = EditorFindSupport.getInstance();
                HashMap<String, Object> props = new HashMap<>(findSupport.createDefaultFindProperties());
                String searchWord = target.getSelectedText();
                int n = searchWord.indexOf('\n');
                if (n >= 0) {
                    searchWord = searchWord.substring(0, n);
                }
                props.put(EditorFindSupport.FIND_WHAT, searchWord);
                props.put(EditorFindSupport.ADD_MULTICARET, Boolean.TRUE);
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui.getComponent().getClientProperty("AsTextField") == null) { //NOI18N
                    findSupport.setFocusedTextComponent(eui.getComponent());
                }
                findSupport.putFindProperties(props);
                findSupport.find(null, false);
                props.put(EditorFindSupport.ADD_MULTICARET, Boolean.FALSE);
                findSupport.putFindProperties(props);
            } else {
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
        }
    }

}
