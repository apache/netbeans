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
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.EditorUI;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.search.ReplaceBar;
import org.netbeans.modules.editor.search.SearchBar;
import org.netbeans.modules.editor.search.SearchNbEditorKit;
import org.netbeans.spi.editor.AbstractEditorAction;

public class SearchAction extends AbstractEditorAction {

    @EditorActionRegistration(name = SearchNbEditorKit.SEARCH_ACTION,
    menuText = "#" + SearchNbEditorKit.SEARCH_ACTION + "_menu_text") // NOI18N
    public static Action create(Map<String, ?> attrs) {
        return new SearchAction(attrs);
    }

    public SearchAction(Map<String, ?> attrs) {
        super(attrs);
        putValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY, org.openide.actions.FindAction.class.getName());
    }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            if ((target instanceof JEditorPane) && ((JEditorPane) target).getEditorKit() instanceof SearchNbEditorKit) {
                target = SearchBar.getInstance().getActualTextComponent();
            }
            //need to find if it has extended editor first, otherwise getExtComponent() will create all sidebars
            //and other parts of full editor if action is assigned to just editor pane and broke later action logic.
            JPanel jp = null;
            Object clientProperty = target.getClientProperty(SearchNbEditorKit.PROP_SEARCH_CONTAINER);
            if (clientProperty instanceof JPanel) {
                jp = (JPanel) clientProperty;
            } else {
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui != null) {
                    JComponent comp = eui.hasExtComponent() ? eui.getExtComponent() : null;
                    if (comp != null) {
                        jp = SearchNbEditorKit.findComponent(comp, SearchNbEditorKit.SearchJPanel.class, 5);
                    }
                }
            }
            if (jp != null) {
                SearchBar searchBarInstance = SearchBar.getInstance(target);
                jp.add(searchBarInstance);
                ReplaceBar replaceBarInstance = ReplaceBar.getInstance(searchBarInstance);
                if (replaceBarInstance.isVisible()) {
                    replaceBarInstance.looseFocus();
                }
                searchBarInstance.gainFocus(false);
                SearchNbEditorKit.makeSearchAndReplaceBarPersistent();
            }
        }

    }
    }
