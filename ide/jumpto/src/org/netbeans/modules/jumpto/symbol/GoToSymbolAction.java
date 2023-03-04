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

package org.netbeans.modules.jumpto.symbol;

import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.JumpList;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/** 
 * @author Petr Hrebejk
 * @author Tomas Zezula
 */
public class GoToSymbolAction extends AbstractAction {
    
    static final Logger LOGGER = Logger.getLogger(GoToSymbolAction.class.getName()); // Used from the panel as well
    
    
    private String title;

    /** Creates a new instance of OpenTypeAction */
    public GoToSymbolAction() {
        this(NbBundle.getMessage( GoToSymbolAction.class, "DLG_GoToSymbol"));
    }
    
    public GoToSymbolAction(String title) {
        super( NbBundle.getMessage( GoToSymbolAction.class,"TXT_GoToSymbol")  );
        this.title = title;
    }    
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        SymbolDescriptor typeDescriptor = getSelectedSymbol();
        if (typeDescriptor != null) {
            JumpList.checkAddEntry();
            typeDescriptor.open();
        }
    }
            
    public SymbolDescriptor getSelectedSymbol() {
        SymbolDescriptor result = null;
        try {
            final JButton okButton = new JButton (NbBundle.getMessage(GoToSymbolAction.class, "CTL_OK"));
            final ContentProviderImpl cp = new ContentProviderImpl(okButton);
            final GoToPanelImpl panel = new GoToPanelImpl(cp);
            final Dialog dialog = DialogFactory.createDialog(title, panel, cp, okButton);
            cp.setDialog(dialog);
            
            Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
            String initSearchText;
            if (arr.length > 0) {
                EditorCookie ec = arr[0].getCookie (EditorCookie.class);
                if (ec != null) {
                    JEditorPane recentPane = NbDocument.findRecentEditorPane(ec);
                    if (recentPane != null) {
                        initSearchText = org.netbeans.editor.Utilities.getSelectionOrIdentifier(recentPane);
                        if (initSearchText != null && org.openide.util.Utilities.isJavaIdentifier(initSearchText)) {
                            panel.setInitialText(initSearchText);
                        }
                    }
                }
            }            
            
            dialog.setVisible(true);
            result = panel.getSelectedSymbol();

        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return result;
    }
    
    @Override
    public boolean isEnabled () {
        return OpenProjects.getDefault().getOpenProjects().length>0;
    }
}
