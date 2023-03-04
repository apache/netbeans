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
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.modules.editor.search.EditorFindSupport;
import org.netbeans.modules.editor.search.SearchBar;
import org.netbeans.modules.editor.search.SearchNbEditorKit;
import org.netbeans.spi.editor.AbstractEditorAction;

// NOI18N

@EditorActionRegistration(name = BaseKit.findNextAction, iconResource = "org/netbeans/modules/editor/search/resources/find_next.png") // NOI18N
public class FindNextAction  extends AbstractEditorAction {
    static final long serialVersionUID = 6878814427731642684L;

    public FindNextAction() {
        super();
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
            JTextComponent component;
            if (eui == null) {
                component = SearchBar.getInstance().getActualTextComponent();
            } else {
                component = eui.getComponent();
            }
            
            if (component.getClientProperty("AsTextField") == null) {
                //NOI18N
                EditorFindSupport.getInstance().setFocusedTextComponent(component);
            }
            SearchNbEditorKit.openFindIfNecessary(component, evt);
            EditorFindSupport.getInstance().find(null, false);
            SearchBar searchBarInstance = SearchBar.getInstance();
            if (searchBarInstance.isVisible()) {
                searchBarInstance.showNumberOfMatches(null, -1);
            }
        }
    }

}
