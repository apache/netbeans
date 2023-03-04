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
