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
package org.netbeans.modules.cnd.callgraph.impl;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 */
public class GoToOccurrenceAction extends AbstractAction implements Presenter.Popup {
    public static final int FUNCTION = 0;
    public static final int CALLER = 1;
    public static final int CALLEE = 2;
    
    private Call.Occurrence occurrence;
    private JMenuItem menuItem;
    
    public GoToOccurrenceAction(Call.Occurrence occurrence) {
        this.occurrence = occurrence;
        putValue(Action.NAME, getString("GoToOccurrence")); // NOI18N
        menuItem = new JMenuItem(this); 
        Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
    }

    public JMenuItem getPopupPresenter() {
        return menuItem;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (occurrence != null) {
            occurrence.open();
        }
    }
    
    private String getString(String key) {
        return NbBundle.getMessage(getClass(), key);
    }
    
}
