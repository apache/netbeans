/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
