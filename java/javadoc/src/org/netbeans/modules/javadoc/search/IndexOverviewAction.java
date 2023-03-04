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

package org.netbeans.modules.javadoc.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.StatusDisplayer;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;

/**
 * Action which shows mounted Javadoc filesystems with known indexes as a submenu,
 * so you can choose a Javadoc set.
 *
 * @author Jesse Glick
 */
public final class IndexOverviewAction extends SystemAction implements Presenter.Menu {
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.javadoc.search.IndexOverviewAction.IndexMenu"); // NOI18N
 
    public IndexOverviewAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public @Override void actionPerformed(ActionEvent ev) {
        // do nothing -- should never be called
    }
    
    public @Override String getName() {
        return NbBundle.getMessage(IndexOverviewAction.class, "CTL_INDICES_MenuItem");
    }
    
    protected @Override String iconResource() {
        return null;//"org/netbeans/modules/javadoc/resources/JavaDoc.gif"; // NOI18N
    }
    
    public @Override HelpCtx getHelpCtx() {
        return new HelpCtx("javadoc.search"); // NOI18N
    }
    
    public @Override JMenuItem getMenuPresenter() {
        return new IndexMenu();
    }
    
    /**
     * Lazy menu which when added to its parent menu, will begin creating the
     * list of filesystems and finding their titles. When the popup for it
     * is created, it will create submenuitems for each available index.
     */
    private final class IndexMenu extends JMenu implements HelpCtx.Provider, DynamicMenuContent {

        private static final int MAX_ITEMS = 20;
        
        private int itemHash = 0;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public IndexMenu() {
            Mnemonics.setLocalizedText(this, IndexOverviewAction.this.getName());
            //setIcon(IndexOverviewAction.this.getIcon());
            // model listening is the only lazy menu procedure that works on macosx
            getModel().addChangeListener(new ChangeListener() {
                public @Override void stateChanged(ChangeEvent e) {
                    if (getModel().isSelected()) {
                        getPopupMenu2();
                    }
                }
            });
        }
        
        public @Override HelpCtx getHelpCtx() {
            return IndexOverviewAction.this.getHelpCtx();
        }
        
        public @Override JComponent[] getMenuPresenters() {
            return new JComponent[] {this};
        }
        
        public @Override JComponent[] synchMenuPresenters(JComponent[] items) {
            return items;
        }
        
//        public void addNotify() {
//            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
//                err.log("addNotify");
//            }
//            super.addNotify();
//            IndexBuilder.getDefault();
//        }
        
        public void getPopupMenu2() {
            List<IndexBuilder.Index> data = IndexBuilder.getDefault().getIndices(false);
            if (data == null) {
                // do not block EDT in case inices are not computed yet
                itemHash = 0;
                removeAll();
                add(new MoreReferencesMenuItem());
                return;
            }
            
            int newHash = data.hashCode();
            if (newHash != itemHash) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("recreating popup menu (" + itemHash + " -> " + newHash + ")");
                }
                itemHash = newHash;
                // Probably need to recreate the menu.
                removeAll();
                int size = data.size();
                if (size != data.size()) {
                    throw new IllegalStateException();
                }
                if (size > 0) {
                    for (int i = 0; i < size && i < MAX_ITEMS; i++) {
                        IndexBuilder.Index index = data.get(i);
                        add(new IndexMenuItem(index.display, index.fo));
                    }
                    add(new MoreReferencesMenuItem());
                } else {
                    JMenuItem dummy = new JMenuItem(NbBundle.getMessage(IndexOverviewAction.class, "CTL_no_indices_found"));
                    dummy.setEnabled(false);
                    add(dummy);
                }
            }
        }
        
    }

    /**
     * Menu item representing one Javadoc index.
     */
    private final class IndexMenuItem extends JMenuItem implements ActionListener, HelpCtx.Provider {
        
        private final URL loc;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public IndexMenuItem(String display, URL index) {
            super(display);
            loc = index;
            addActionListener(this);
        }
        
        public @Override void actionPerformed(ActionEvent ev) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(loc);
        }
        
        public @Override HelpCtx getHelpCtx() {
            return IndexOverviewAction.this.getHelpCtx();
        }
        
        public @Override
        void menuSelectionChanged(boolean isIncluded) {
            super.menuSelectionChanged(isIncluded);
            if (isIncluded) {
                StatusDisplayer.getDefault().setStatusText(loc.toString());
            }
        }
    }

    private static final class MoreReferencesMenuItem extends JMenuItem implements ActionListener {

        @SuppressWarnings("LeakingThisInConstructor")
        public MoreReferencesMenuItem() {
            Mnemonics.setLocalizedText(this, NbBundle.getMessage(IndexOverviewAction.class, "CTL_MORE_INDICES_MenuItem"));
            addActionListener(this);
        }

        public @Override void actionPerformed(ActionEvent e) {
            URL u = ReferencesPanel.showInWindow();
            if (u != null) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(u);
            }
        }

    }
    
}
