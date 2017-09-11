package org.netbeans.modules.editor.search.actions;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */


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
