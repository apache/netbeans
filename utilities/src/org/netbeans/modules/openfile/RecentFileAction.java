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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.openfile;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.openfile.RecentFiles.HistoryItem;
import org.openide.awt.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
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
        implements Presenter.Menu, PopupMenuListener, ChangeListener {

    private static final RequestProcessor RP =
            new RequestProcessor(RecentFileAction.class);

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
    
    public RecentFileAction() {
        super(NbBundle.getMessage(RecentFileAction.class,
                                  "LBL_RecentFileAction_Name")); // NOI18N
    }
    
    /********* Presenter.Menu impl **********/
    
    @Override
    public JMenuItem getMenuPresenter() {
        if (menu == null) {
            menu = new UpdatingMenu(this);
            menu.setMnemonic(NbBundle.getMessage(RecentFileAction.class,
                              "MNE_RecentFileAction_Name").charAt(0)); // NOI18N
            // #115277 - workaround, PopupMenuListener don't work on Mac 
            if (!Utilities.isMac()) {
                menu.getPopupMenu().addPopupMenuListener(this);
            } else {
                menu.addChangeListener(this);
            }
        }
        return menu;
    }
    
    /******* PopupMenuListener impl *******/

    /* Fills submenu when popup is about to be displayed.
     * Note that argument may be null on Mac due to #115277 fix
     */
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
        fillSubMenu();
    }
    
    /* Clears submenu when popup is about to be hidden.
     * Note that argument may be null on Mac due to #115277 fix
     */
    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
        menu.removeAll();
    }
    
    @Override
    public void popupMenuCanceled(PopupMenuEvent arg0) {
    }

    /******** ChangeListener impl *********/

    /** Delegates to popupMenuListener based on menu current selection status */ 
    @Override
    public void stateChanged(ChangeEvent e) {
        if (menu.isSelected()) {
            popupMenuWillBecomeVisible(null);
        } else {
            popupMenuWillBecomeInvisible(null);
        }
    }
    
    /** Fills submenu with recently closed files got from RecentFiles support */
    private void fillSubMenu () {
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
            setEnabled(RecentFiles.hasRecentFiles());
            return content;
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }
    }

}
