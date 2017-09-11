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
