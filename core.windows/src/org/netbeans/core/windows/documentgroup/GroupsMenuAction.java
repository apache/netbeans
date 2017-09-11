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

package org.netbeans.core.windows.documentgroup;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * Popup menu listing all available document groups or just a single item to 
 * create a new document group.
 * 
 * @author S. Aubrecht
 */
@NbBundle.Messages({
    "CTL_CloseDocumentGroupAction=<none>",
    "CTL_NewGroupAction=New Document Group...",
    "CTL_ManageGroupsAction=Manage..."
})
public class GroupsMenuAction extends AbstractAction implements Presenter.Menu {

    private static final JMenu menu = new JMenu(NbBundle.getMessage(GroupsMenuAction.class, "Menu_DOCUMENT_GROUPS"));
    
    private GroupsMenuAction() {
        super( NbBundle.getMessage(GroupsMenuAction.class, "Menu_DOCUMENT_GROUPS"));
    }
    
    public static AbstractAction create() {
//        if( Boolean.getBoolean("nb.document.groups.enable") ) //NOI18N
            return new GroupsMenuAction();
//        return null;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        if (EventQueue.isDispatchThread()) {
            fillMenu(menu);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fillMenu(menu);
                }
            });
        }
        return menu;
    }
    
    static void refreshMenu() {
        fillMenu(menu);
    }
    
    private static void fillMenu( JMenu menu ) {
        menu.removeAll();
        GroupsManager gm = GroupsManager.getDefault();
        DocumentGroupImpl current = gm.getCurrentGroup();
        List<DocumentGroupImpl> groups = gm.getGroups();
        
        //new group
        menu.add( new AbstractAction(NbBundle.getMessage(GroupsMenuAction.class, "CTL_NewGroupAction")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewGroupPanel panel = new NewGroupPanel();
                panel.showDialog();
            }
        } ); //NOI18N
        if( !groups.isEmpty() ) {
            menu.addSeparator();
            //close group
            JRadioButtonMenuItem item = new JRadioButtonMenuItem( new AbstractAction(NbBundle.getMessage(GroupsMenuAction.class, "CTL_CloseDocumentGroupAction")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    closeGroup();
                }
            } );//NOI18N
            item.setSelected( null == current );
            menu.add( item );
            
            for( DocumentGroupImpl group : groups ) {
                item = new JRadioButtonMenuItem( new OpenGroupAction(group) );
                item.setSelected( group.equals( current ) );
                menu.add( item );
            }
            menu.addSeparator();
            menu.add( new AbstractAction(NbBundle.getMessage(GroupsMenuAction.class, "CTL_ManageGroupsAction")) {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    ManageGroupsPanel panel = new ManageGroupsPanel();
                    panel.showDialog();
                }
            } );//NOI18N
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //do nothing, just a placeholder
    }

    private static class OpenGroupAction extends AbstractAction {

        private final DocumentGroupImpl group;

        public OpenGroupAction( DocumentGroupImpl group ) {
            super( group.toString() );
            this.group = group;
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            final PleaseWait wait = new PleaseWait();
            wait.install();
            SwingUtilities.invokeLater( new Runnable() {

                @Override
                public void run() {
                    try {
                        group.open();
                        refreshMenu();
                    } finally {
                        wait.uninstall();
                    }
                }
            });
        }
    }
    
    private static void closeGroup() {
        final DocumentGroupImpl selGroup = GroupsManager.getDefault().getCurrentGroup();
        if( null != selGroup ) {
            final PleaseWait wait = new PleaseWait();
            wait.install();
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    try {
                        if( selGroup.close() ) {
                            GroupsManager.closeAllDocuments();
                        }
                        refreshMenu();
                    } finally {
                        wait.uninstall();
                    }
                }
            });
        }
    }
}
