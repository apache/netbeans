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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.search.EditorFindSupport;
import org.netbeans.spi.editor.AbstractEditorAction;

/** Finds either selection or if there's no selection it finds
 * the word where the cursor is standing.
 */
// NOI18N
@EditorActionRegistration(name = BaseKit.findSelectionAction, iconResource = "org/netbeans/modules/editor/search/resources/find_selection.png") // NOI18N
public class FindSelectionAction extends AbstractEditorAction {
    static final long serialVersionUID = -5601618936504699565L;
    private static final Logger LOGGER = Logger.getLogger(FindSelectionAction.class.getName());

    public FindSelectionAction() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            EditorFindSupport findSupport = EditorFindSupport.getInstance();
            Caret caret = target.getCaret();
            int dotPos = caret.getDot();
            HashMap<String, Object> props = new HashMap<>(findSupport.createDefaultFindProperties());
            String searchWord = null;
            boolean revert = false;
            Boolean originalValue = null;
            Map<String, Object> revertMap = (Map<String, Object>) props.get(EditorFindSupport.REVERT_MAP);
            Boolean revertValue = revertMap != null ? (Boolean) revertMap.get(EditorFindSupport.FIND_WHOLE_WORDS) : null;
            if (Utilities.isSelectionShowing(caret)) {
                // valid selection
                searchWord = target.getSelectedText();
                originalValue = (Boolean) props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
                if (Boolean.FALSE.equals(revertValue)) {
                    revertMap.remove(EditorFindSupport.FIND_WHOLE_WORDS);
                } else {
                    revert = !Boolean.FALSE.equals(originalValue);
                }
            } else {
                // no selection, get current word
                try {
                    searchWord = Utilities.getIdentifier((BaseDocument) target.getDocument(), dotPos);
                    originalValue = (Boolean) props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.TRUE);
                    if (Boolean.TRUE.equals(revertValue)) {
                        revertMap.remove(EditorFindSupport.FIND_WHOLE_WORDS);
                    } else {
                        revert = !Boolean.TRUE.equals(originalValue);
                    }
                } catch (BadLocationException e) {
                    LOGGER.log(Level.WARNING, null, e);
                }
            }
            if (searchWord != null) {
                int n = searchWord.indexOf('\n');
                if (n >= 0) {
                    searchWord = searchWord.substring(0, n);
                }
                props.put(EditorFindSupport.FIND_WHAT, searchWord);
                if (revert) {
                    revertMap = new HashMap<>();
                    revertMap.put(EditorFindSupport.FIND_WHOLE_WORDS, originalValue != null ? originalValue : Boolean.FALSE);
                    props.put(EditorFindSupport.REVERT_MAP, revertMap);
                }
                props.put(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
                props.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, null);
                props.put(EditorFindSupport.FIND_BLOCK_SEARCH_END, null);
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui.getComponent().getClientProperty("AsTextField") == null) {
                    //NOI18N
                    findSupport.setFocusedTextComponent(eui.getComponent());
                }
                findSupport.putFindProperties(props);
                if (findSupport.find(null, false)) {
                    findSupport.addToHistory(new EditorFindSupport.SPW((String) props.get(EditorFindSupport.FIND_WHAT), (Boolean) props.get(EditorFindSupport.FIND_WHOLE_WORDS), (Boolean) props.get(EditorFindSupport.FIND_MATCH_CASE), (Boolean) props.get(EditorFindSupport.FIND_REG_EXP)));
                }
            }
        }
    }

}
