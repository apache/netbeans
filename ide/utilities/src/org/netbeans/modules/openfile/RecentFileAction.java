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

package org.netbeans.modules.openfile;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.openfile.RecentFiles.HistoryItem;
import org.openide.awt.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Action that presents list of recently closed files/documents.
 *
 * @author Dafe Simonek
 */
@ActionRegistration(lazy=false,
    displayName="#LBL_RecentFileAction_Name"
)
@ActionID(category="System", id="org.netbeans.modules.openfile.RecentFileAction")
@ActionReference(path="Menu/File", position=900)
public class RecentFileAction extends AbstractAction
        implements Presenter.Menu, ChangeListener, PropertyChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(RecentFileAction.class);

    /** property of menu items where we store fileobject to open */
    private static final String PATH_PROP =
                     "RecentFileAction.Recent_File_Path";              // NOI18N

    private static final String OFMSG_PATH_IS_NOT_DEFINED =
                     NbBundle.getMessage(RecentFileAction.class,
                                         "OFMSG_PATH_IS_NOT_DEFINED"); // NOI18N

    private static final String OFMSG_FILE_NOT_EXISTS =
                     NbBundle.getMessage(RecentFileAction.class,
                                         "OFMSG_FILE_NOT_EXISTS");     // NOI18N

    private static final String OFMSG_NO_RECENT_FILE =
                     NbBundle.getMessage(RecentFileAction.class,
                                         "OFMSG_NO_RECENT_FILE");     // NOI18N

    private JMenu menu;
    private boolean recreate = true;
    
    public RecentFileAction() {
        super(NbBundle.getMessage(RecentFileAction.class,
                                  "LBL_RecentFileAction_Name")); // NOI18N

        RecentFiles.addPropertyChangeListener(this);
    }

    /********* Presenter.Menu impl **********/
    
    @Override
    public JMenuItem getMenuPresenter() {
        if (menu == null) {
            menu = new UpdatingMenu(this);
            menu.setMnemonic(NbBundle.getMessage(RecentFileAction.class,
                              "MNE_RecentFileAction_Name").charAt(0)); // NOI18N

            menu.getModel().addChangeListener(this);
            fillSubMenu();
        }
        return menu;
    }

    // Implementation of change listener ---------------------------------------
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if ( RecentFiles.PROPERTY_RECENT_FILES.equals( e.getPropertyName() ) ) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    recreate = true;
                }
            });
        }
    }

    /******** ChangeListener impl *********/

    @Override
    public void stateChanged(ChangeEvent e) {
        if (menu.getModel().isSelected()) {
            fillSubMenu();
        }
    }
    
    /** Fills submenu with recently closed files got from RecentFiles support */
    private void fillSubMenu () {
        if (recreate && RecentFiles.hasRecentFiles()) {
            menu.removeAll();
            List<HistoryItem> files = RecentFiles.getRecentFiles();
            boolean first = true;
            for (final HistoryItem hItem : files) {
                try { // #188403
                    JMenuItem jmi = newSubMenuItem(hItem);
                    menu.add(jmi);
                    if( first ) {
                        Object accel = getValue( Action.ACCELERATOR_KEY );
                        if( accel instanceof KeyStroke ) {
                            jmi.setAccelerator( (KeyStroke)accel );
                        }
                        first = false;
                    }
                } catch (Exception ex) {
                    continue;
                }
            }
            ensureSelected();
            recreate = false;
        }
    }

    /**
     * Creates and configures an item of the submenu according to the given
     * {@code HistoryItem}.
     * @param hItem the {@code HistoryItem}.
     * @return the munu item.
     */
    private JMenuItem newSubMenuItem(final HistoryItem hItem) {
        final String path = hItem.getPath();
        final JMenuItem jmi = new JMenuItem(hItem.getFileName()) {
            public @Override void menuSelectionChanged(boolean isIncluded) {
                super.menuSelectionChanged(isIncluded);
                if (isIncluded) {
                    StatusDisplayer.getDefault().setStatusText(path);
                }
            }
        };
        jmi.putClientProperty(PATH_PROP, path);
        jmi.addActionListener(this);
        jmi.setIcon(hItem.getIcon());
        jmi.setToolTipText(path);
        return jmi;
    }

    /** Workaround for JDK bug 6663119, it ensures that first item in submenu
     * is correctly selected during keyboard navigation.
     */
    private void ensureSelected () {
        if (menu.getMenuComponentCount() <=0) {
            return;
        }
        
        Component first = menu.getMenuComponent(0);
        if (!(first instanceof JMenuItem)) {
            return;
        }
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return; // probably a mouseless computer
        }
        Point loc = pointerInfo.getLocation();
        SwingUtilities.convertPointFromScreen(loc, menu);
        MenuElement[] selPath =
                MenuSelectionManager.defaultManager().getSelectedPath();
        
        // apply workaround only when mouse is not hovering over menu
        // (which signalizes mouse driven menu traversing) and only
        // when selected menu path contains expected value - submenu itself 
        if (!menu.contains(loc) && selPath.length > 0 && 
                menu.getPopupMenu() == selPath[selPath.length - 1]) {
            // select first item in submenu through MenuSelectionManager
            MenuElement[] newPath = new MenuElement[selPath.length + 1];
            System.arraycopy(selPath, 0, newPath, 0, selPath.length);
            JMenuItem firstItem = (JMenuItem)first;
            newPath[selPath.length] = firstItem;
            MenuSelectionManager.defaultManager().setSelectedPath(newPath);
        }
    }
    
    /** Opens recently closed file, using OpenFile support.
     *
     * Note that method works as action handler for individual submenu items
     * created in fillSubMenu, not for whole RecentFileAction.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        String path = null;
        String msg = null;
        if (source instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) source;
            path = (String) menuItem.getClientProperty(PATH_PROP);
        } else {
            List<HistoryItem> items = RecentFiles.getRecentFiles();
            if( !items.isEmpty() ) {
                HistoryItem item = RecentFiles.getRecentFiles().get( 0 );
                path = item.getPath();
            } else {
                msg = OFMSG_NO_RECENT_FILE;
            }
        }
        if( null == msg )
            msg = openFile(path);
        if (msg != null) {
            StatusDisplayer.getDefault().setStatusText(msg);
            Toolkit.getDefaultToolkit().beep();
            RecentFiles.pruneHistory();
        }
    }

    /**
     * Open a file.
     * @param path the path to the file or {@code null}.
     * @return error message or {@code null} on success.
     */
    private String openFile(String path) {
        if(path == null || path.length() == 0) {
            return OFMSG_PATH_IS_NOT_DEFINED;
        }
        File f = new File(path);
        if (!f.exists()) {
            return OFMSG_FILE_NOT_EXISTS;
        }
        File nf = FileUtil.normalizeFile(f);
        return OpenFile.open(FileUtil.toFileObject(nf), -1);
    }
    
    /** Menu that checks its enabled state just before is populated */
    private class UpdatingMenu extends JMenu implements DynamicMenuContent {
        
        private final JComponent[] content = new JComponent[] { this };
        
        public UpdatingMenu (Action action) {
            super(action);
        }
    
        @Override
        public JComponent[] getMenuPresenters() {
            return content;
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

        @Override public boolean isEnabled() {
            return RecentFiles.hasRecentFiles();
        }
    }
}
