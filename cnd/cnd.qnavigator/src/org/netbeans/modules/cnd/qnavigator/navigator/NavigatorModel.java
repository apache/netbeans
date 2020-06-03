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

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.qnavigator.navigator.CsmFileFilter.SortMode;
import org.netbeans.modules.cnd.qnavigator.navigator.CsmFileModel.PreBuildModel;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 */
public class NavigatorModel {

    private final DataObject cdo;
    private final FileObject fo;
    private final NavigatorPanelUI ui;
    private final Action[] actions;
    private final AbstractNode root;

    private final CsmFileModel fileModel;
    private final static class Lock {}
    private final Object lock = new Lock();
    private final CsmFile csmFile;

    public NavigatorModel(DataObject cdo, FileObject fo, NavigatorPanelUI ui, final String mimeType, final CsmFile csmFile) {
        this.cdo = cdo;
        this.fo = fo;
        this.ui = ui;
        actions = new Action[]{
            new SortByNameAction(),
            new SortBySourceAction(),
            new GroupByKindAction(),
            new ExpandAllAction(),
            null,
            new FilterSubmenuAction(mimeType),
                              };
        root = new AbstractNode(new Children.Array()) {
            @Override
            public Action[] getActions(boolean context) {
                return actions;
            }

            @Override
            public Image getIcon(int type) {
                if (csmFile != null) {
                    return CsmImageLoader.getImage(csmFile);
                } else {
                    return NavigatorModel.this.cdo.getNodeDelegate().getIcon(type);
                }
            }
            
        };
        fileModel = new CsmFileModel(new CsmFileFilter(), actions);
        this.csmFile = csmFile;
    }

    public DataObject getDataObject() {
        return cdo;
    }

    public CsmFile getCsmFile() {
        return csmFile;
    }
    
    void removeNotify() {
        synchronized(lock) {
            fileModel.clear();
            setChildren(null);
            if (ui != null) {
                ui.selectNodes(new Node[] {});
            }
        }
    }
    
    private void setChildren(final Node[] nodes) {
        final Children children = root.getChildren();
        if (!Children.MUTEX.isReadAccess()){
             Children.MUTEX.writeAccess(new Runnable(){
                @Override
                public void run() {
                    // first remove all existing
                    children.remove(children.getNodes());
                    if (nodes != null) {
                        children.add(nodes);
                    }
                }
            });
        }
    }

    public Node getRoot(){
        return root;
    }

    private void refresh(boolean force) {
        update(new AtomicBoolean(false), force);
    }

    void update(AtomicBoolean canceled, boolean force) {
        if (csmFile == null) {
            synchronized(lock) {
                fileModel.clear();
                setChildren(new Node[]{});
            }
        } else {
            try {
                CsmCacheManager.enter();
                PreBuildModel buildPreModel = fileModel.buildPreModel(cdo, fo, csmFile, canceled);
                if (!canceled.get()) {
                    synchronized(lock) {
                        if (fileModel.buildModel(buildPreModel, csmFile, force)){
                            setChildren(fileModel.getNodes());
                        }
                    }
                }
            } finally {
                CsmCacheManager.leave();
            }
        }
        if (!canceled.get()) {
            if (ui != null) {
                ui.newContentReady();
            }
        }
    }

    private int storeSelection(){
        if (ui != null) {
            Node[] selection = ui.getExplorerManager().getSelectedNodes();
            if (selection != null && selection.length == 1) {
                Node selected = selection[0];
                if (selected instanceof CppDeclarationNode) {
                    return ((CppDeclarationNode) selected).getOffset();
                }
            }
        }
        return -1;
    }

    void setSelection(long caretLineNo, JEditorPane jEditorPane, AtomicBoolean canceled, CharSequence text) {
        synchronized(lock) {
            Node node = fileModel.setSelection(caretLineNo);
            if (node != null) {
                if (ui != null) {
                    ui.selectNodes(new Node[]{node});
                }
                if (jEditorPane == null) {
                    return;
                }
                BreadCrumbsFactory.createBreadCrumbs(caretLineNo, node, jEditorPane, cdo, canceled, text);
            }
        }
    }

    private JEditorPane findCurrentJEditorPane() {
        if (cdo != null) {
            JTextComponent comp = EditorRegistry.lastFocusedComponent();
            DataObject obj = CsmUtilities.getDataObject(comp);
            if (cdo.equals(obj) && comp instanceof JEditorPane) {
                return (JEditorPane) comp;
            }
        }
        return null;
    }

    public CsmFileFilter getFilter(){
        return fileModel.getFilter();
    }

    private class ShowForwardFunctionDeclarationsAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowForwardFunctionDeclarationsAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowForwardFunctionDeclarationsText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowForwardFunctionDeclarationsAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowForwardFunctionDeclarations(!fileModel.getFilter().isShowForwardFunctionDeclarations());
            int selection = storeSelection();
            refresh(false);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowForwardFunctionDeclarations());
            return menuItem;
        }
    }

    private class ShowForwardClassDeclarationsAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowForwardClassDeclarationsAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowForwardClassDeclarationsText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowForwardClassDeclarationsAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowForwardClassDeclarations(!fileModel.getFilter().isShowForwardClassDeclarations());
            int selection = storeSelection();
            refresh(false);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowForwardClassDeclarations());
            return menuItem;
        }
    }


    private class ShowMacroAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowMacroAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowMacroText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowMacroAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowMacro(!fileModel.getFilter().isShowMacro());
            int selection = storeSelection();
            refresh(false);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowMacro());
            return menuItem;
        }
    }

    private class ShowIncludeAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowIncludeAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowIncludeText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowIncludeAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowInclude(!fileModel.getFilter().isShowInclude());
            int selection = storeSelection();
            refresh(false);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowInclude());
            return menuItem;
        }
    }

    private class ShowTypedefAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowTypedefAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowTypedefText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowTypedefAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowTypedef(!fileModel.getFilter().isShowTypedef());
            int selection = storeSelection();
            refresh(false);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowTypedef());
            return menuItem;
        }
    }

    private class ShowVariableAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowVariableAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowVariableText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowVariableAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowVariable(!fileModel.getFilter().isShowVariable());
            int selection = storeSelection();
            refresh(false);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowVariable());
            return menuItem;
        }
    }

    private class ShowFieldAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowFieldAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowFieldText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowFieldAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowField(!fileModel.getFilter().isShowField());
            int selection = storeSelection();
            refresh(false);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowField());
            return menuItem;
        }
    }

    private class ShowUsingAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowUsingAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowUsingText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowUsingAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowUsing(!fileModel.getFilter().isShowUsing());
            int selection = storeSelection();
            refresh(false);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowUsing());
            return menuItem;
        }
    }
    private class SortByNameAction extends AbstractAction implements Presenter.Popup {
        private final JRadioButtonMenuItem menuItem;
        public SortByNameAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "SortByNameText")); // NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/qnavigator/resources/sortAlpha.png", false)); // NOI18N
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(SortByNameAction.this);
            //Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (fileModel.getFilter().getSortMode() != SortMode.Name) {
                fileModel.getFilter().setSortMode(SortMode.Name);
                int selection = storeSelection();
                refresh(true);
                if (selection >= 0) {
                    setSelection(selection, findCurrentJEditorPane(), null, null);
                }
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().getSortMode()== SortMode.Name);
            return menuItem;
        }
    }

    private class SortBySourceAction extends AbstractAction implements Presenter.Popup {
        private final JRadioButtonMenuItem menuItem;
        public SortBySourceAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "SortBySourceText")); // NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/qnavigator/resources/sortPosition.png", false)); // NOI18N
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(SortBySourceAction.this);
            //Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (fileModel.getFilter().getSortMode() != SortMode.Offset) {
                fileModel.getFilter().setSortMode(SortMode.Offset);
                int selection = storeSelection();
                refresh(true);
                if (selection >= 0) {
                    setSelection(selection, findCurrentJEditorPane(), null, null);
                }
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().getSortMode() == SortMode.Offset);
            return menuItem;
        }
    }

    private class GroupByKindAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public GroupByKindAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "GroupByKindText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(GroupByKindAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setGroupByKind(!fileModel.getFilter().isGroupByKind());
            int selection = storeSelection();
            refresh(true);
            if (selection >= 0) {
                setSelection(selection, findCurrentJEditorPane(), null, null);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isGroupByKind());
            return menuItem;
        }
    }

    private class ExpandAllAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ExpandAllAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ExpandAll")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ExpandAllAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setExpandAll(!fileModel.getFilter().isExpandAll());
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isExpandAll());
            return menuItem;
        }
    }

    private class FilterSubmenuAction extends AbstractAction implements Presenter.Popup {
        private final String mimeType;

        public FilterSubmenuAction(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return createSubmenu();
        }

        private JMenuItem createSubmenu () {
            JMenuItem menu = new JMenu(NbBundle.getMessage(NavigatorModel.class, "FilterSubmenu")); //NOI18N
            boolean isC = MIMENames.isHeaderOrCppOrC(mimeType);
            boolean isCpp = MIMENames.isHeaderOrCpp(mimeType);
            boolean isCnd = MIMENames.isFortranOrHeaderOrCppOrC(mimeType);
            if (isC) {
                menu.add(new ShowForwardClassDeclarationsAction().getPopupPresenter());
                menu.add(new ShowForwardFunctionDeclarationsAction().getPopupPresenter());
                menu.add(new ShowMacroAction().getPopupPresenter());
            }
            if (isCnd) {
                menu.add(new ShowIncludeAction().getPopupPresenter());
            }
            if (isC) {
                menu.add(new ShowTypedefAction().getPopupPresenter());
            }
            if (isCnd) {
                menu.add(new ShowVariableAction().getPopupPresenter());
                menu.add(new ShowFieldAction().getPopupPresenter());
            }
            if (isCpp) {
                menu.add(new ShowUsingAction().getPopupPresenter());
            }
            return menu;
        }
    }
    
    @Override
    public String toString() {
        return "" + cdo;
    }
}
