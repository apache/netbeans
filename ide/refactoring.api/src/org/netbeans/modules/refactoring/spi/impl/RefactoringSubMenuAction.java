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
package org.netbeans.modules.refactoring.spi.impl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.TextAction;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin Matula
 */
public final class RefactoringSubMenuAction extends TextAction implements Presenter.Menu, Presenter.Popup {

    private static final Logger LOG = Logger.getLogger(RefactoringSubMenuAction.class.getName());
    private final boolean showIcons;
    
    public static RefactoringSubMenuAction create(FileObject o) {
        return new RefactoringSubMenuAction(true);
    }
    
    public static JMenu createMenu() {
        RefactoringSubMenuAction action = new RefactoringSubMenuAction(true);
        return (JMenu) action.getMenuPresenter();
    }
    
    /** Creates a new instance of TestMenu */
    RefactoringSubMenuAction(boolean showIcons) {
        super(NbBundle.getMessage(RefactoringSubMenuAction.class, "LBL_Action"));
        this.showIcons = showIcons;
    }
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
    }
    
    @Override
    public javax.swing.JMenuItem getMenuPresenter() {
        return new SubMenu();
    }
    
    @Override
    public javax.swing.JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof RefactoringSubMenuAction);
    }
    
    @Override
    public int hashCode() {
        return 1;
    }
    
    private final class SubMenu extends JMenu {
        
        private boolean createMenuLazily = true;
        private boolean wasSeparator;
        private boolean shouldAddSeparator;
        
        public SubMenu() {
            super((String) RefactoringSubMenuAction.this.getValue(Action.NAME));
            if (showIcons)
                setMnemonic(NbBundle.getMessage(RefactoringSubMenuAction.class, "LBL_ActionMnemonic").charAt(0));
        }
        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        @Override
        public JPopupMenu getPopupMenu() {
            if (createMenuLazily) {
                createMenuItems();
                createMenuLazily = false;
            }
            return super.getPopupMenu();
        }
        
        /** Creates items when actually needed. */
        private void createMenuItems() {
            removeAll();
            FileObject fo = FileUtil.getConfigFile("Menu/Refactoring"); // NOI18N
            DataFolder df = fo == null ? null : DataFolder.findFolder(fo);
                
            if (df != null) {
                wasSeparator = true;
                shouldAddSeparator = false;
                DataObject actionObjects[] = df.getChildren();
                for (int i = 0; i < actionObjects.length; i++) {
                    InstanceCookie ic = actionObjects[i].getCookie(InstanceCookie.class);
                    if (ic == null) continue;
                    Object instance;
                    try {
                        instance = ic.instanceCreate();
                    } catch (IOException e) {
                        // ignore
                        LOG.log(Level.WARNING, actionObjects[i].toString(), e);
                        continue;
                    } catch (ClassNotFoundException e) {
                        // ignore
                        LOG.log(Level.WARNING, actionObjects[i].toString(), e);
                        continue;
                    }
                    
                    if (instance instanceof Presenter.Popup) {
                        JMenuItem temp = ((Presenter.Popup)instance).getPopupPresenter();
                        if (temp instanceof DynamicMenuContent) {
                            for (JComponent presenter : ((DynamicMenuContent) temp).getMenuPresenters()) {
                                addPresenter(presenter);
                            }
                        } else {
                            addPresenter(temp);
                        }
                    } else if (instance instanceof Action) {
                        // if the action is the refactoring action, pass it information
                        // whether it is in editor, popup or main menu
                        JMenuItem mi = new JMenuItem();
                        Actions.connect(mi, (Action) instance, true);
                        addPresenter(mi);
                    } else if (instance instanceof JSeparator) {
                        addPresenter((JSeparator) instance);
                    }
                }
            }
        }

        private void addPresenter(JComponent presenter) {
            if (!showIcons && presenter instanceof AbstractButton) {
                ((AbstractButton) presenter).setIcon(null);
            }

            boolean isSeparator = presenter == null || presenter instanceof JSeparator;

            if (isSeparator) {
                if (!wasSeparator) {
                    shouldAddSeparator = true;
                    wasSeparator = true;
                }
            } else {
                if (shouldAddSeparator) {
                    addSeparator();
                    shouldAddSeparator = false;
                }
                add(presenter);
                wasSeparator = false;
            }

        }
        
    }
}
