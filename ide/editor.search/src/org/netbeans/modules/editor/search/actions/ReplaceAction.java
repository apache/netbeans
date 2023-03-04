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

public class ReplaceAction extends AbstractEditorAction {

    static final long serialVersionUID = -1;

    @EditorActionRegistration(name = SearchNbEditorKit.REPLACE_ACTION,
            menuText="#" + SearchNbEditorKit.REPLACE_ACTION + "_menu_text") // NOI18N
    public static Action create(Map<String,?> attrs) {
        return new ReplaceAction(attrs);
    }

    public ReplaceAction(Map<String, ?> attrs) {
        super(attrs);
        putValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY, org.openide.actions.ReplaceAction.class.getName());
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            if ((target instanceof JEditorPane) && ((JEditorPane) target).getEditorKit() instanceof SearchNbEditorKit) {
                target = SearchBar.getInstance().getActualTextComponent();
            }
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
            if (jp != null && target.isEditable()) {
                SearchBar searchBar = SearchBar.getInstance(target);
                jp.add(searchBar);
                jp.add(ReplaceBar.getInstance(searchBar));
                ReplaceBar.getInstance(searchBar).gainFocus(false);
                SearchNbEditorKit.makeSearchAndReplaceBarPersistent();
            }

        }
    }
}
