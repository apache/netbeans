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

package org.netbeans.modules.form.actions;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.awt.event.*;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.undo.UndoableEdit;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that encloses selected components in a given container.
 * 
 * @author Tomas Pavek, Jan Stola
 */
public class EncloseAction extends NodeAction {
    
    @Override
    public String getName() {
        return NbBundle.getBundle(EncloseAction.class).getString("ACT_EncloseInContainer"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        List<RADComponent> comps = getComponents(nodes);
        return comps != null && getContainer(comps) != null && enclosableComponents(comps);
    }

    @Override
    protected void performAction(Node[] nodes) {
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new ContainersMenu(getName(), getComponents(getActivatedNodes()));
        menu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(menu, EncloseAction.class.getName());
        return menu;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private static List<RADComponent> getComponents(Node[] nodes) {
        return FormUtils.getSelectedLayoutComponents(nodes);
    }

    private static RADVisualContainer getContainer(List components) {
        RADVisualContainer commonParent = null;
        for (Object comp : components) {
            if (comp instanceof RADVisualComponent) {
                RADVisualContainer parent = ((RADVisualComponent)comp).getParentContainer();
                if (parent == null || (commonParent != null && parent != commonParent)) {
                    return null;
                }
                if (commonParent == null) {
                    commonParent = parent;
                }
            } else {
                return null;
            }
        }
        return commonParent;
    }

    private boolean enclosableComponents(List<RADComponent> components) {
        FormModel formModel = null;
        String[] compIds = new String[components.size()];
        int i = 0;
        for (RADComponent metacomp : components) {
            compIds[i++] = metacomp.getId();
            if (formModel == null) {
                formModel = metacomp.getFormModel();
            }
        }
        FormDesigner formDesigner = FormEditor.getFormDesigner(formModel);
        return formDesigner != null && formDesigner.getLayoutDesigner() != null
            && formDesigner.getLayoutDesigner().canEncloseInContainer(compIds);
    }

    private static List<CategoryInfo> getCategoryInfos() {
        Node[] nodes = PaletteUtils.getCategoryNodes(PaletteUtils.getPaletteNode(), true);
        List<CategoryInfo> infos = new ArrayList<CategoryInfo>(nodes.length);
        for (Node node : nodes) {
            List<PaletteItem> containers = getAllContainers(node);
            CategoryInfo info = new CategoryInfo();
            info.categoryNode = node;
            info.containers = containers;
            infos.add(info);
        }
        return infos;
    }

    private static List<PaletteItem> getAllContainers(Node subMenuNode) {
        List<PaletteItem> list = new ArrayList<PaletteItem>();
        for (Node itemNode : PaletteUtils.getItemNodes(subMenuNode, true)) {
            PaletteItem item = itemNode.getLookup().lookup(PaletteItem.class);
            if (item == null) {
                continue;
            }
            if (PaletteItem.TYPE_CHOOSE_BEAN.equals(item.getExplicitComponentType())) {
                continue;
            }
            Class cls = item.getComponentClass();
            if (cls != null
                  && JComponent.class.isAssignableFrom(cls)
                  && !MenuElement.class.isAssignableFrom(cls)
                  && FormUtils.isContainer(cls)) {
                list.add(item);
            }
        }
        // sort the PaletteItems alphabetically
        list.sort(new Comparator<PaletteItem>() {
            @Override
            public int compare(PaletteItem o1, PaletteItem o2) {
                return o1.getNode().getDisplayName().compareTo(o2.getNode().getDisplayName());
            }
        });
        return list;
    }

    private static class CategoryInfo {
        Node categoryNode;
        List<PaletteItem> containers;
    }

    private static class ContainersMenu extends JMenu {
        private boolean initialized = false;
        private List<RADComponent> components;

        private ContainersMenu(String name, List<RADComponent> components) {
            super(name);
            this.components = components;
        }

        @Override
        public JPopupMenu getPopupMenu() {
            final JPopupMenu popup = super.getPopupMenu();
            if (!initialized) {
                popup.removeAll();
                String waitTxt = NbBundle.getBundle(EncloseAction.class).getString("MSG_EncloseInPleaseWait"); // NOI18N
                JMenuItem waitItem = new JMenuItem(waitTxt);
                waitItem.setEnabled(false);
                popup.add(waitItem);
                // Find the containers outside EQ, see issue 123794
                FormUtils.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        final List<CategoryInfo> categories = getCategoryInfos();
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                popup.removeAll();
                                fillPaletteCategoriesSubMenu(categories);
                                popup.pack();
                            }
                        });
                    }
                });
                initialized = true;
            }
            return popup;
        }

        private void fillPaletteCategoriesSubMenu(List<CategoryInfo> categories) {
            for (CategoryInfo category : categories) {
                Node categoryNode = category.categoryNode;
                if (!categoryNode.isLeaf()) {
                    JMenu menu = new JMenu(categoryNode.getDisplayName());
                    if (!category.containers.isEmpty()) {
                        for (PaletteItem item : category.containers) {
                            JMenuItem mi = new JMenuItem(item.getNode().getDisplayName());
                            HelpCtx.setHelpIDString(mi, EncloseAction.class.getName());
                            menu.add(mi);
                            mi.addActionListener(new EncloseActionListener(item));
                        }
                        add(menu);
                    }
                }
            }
            // If there is just one sub-menu then dissolve it
            if (getMenuComponentCount() == 1) {
                JMenu menu = (JMenu)getMenuComponent(0);
                remove(menu);
                for (Component menuItem : menu.getMenuComponents()) {
                    add(menuItem);
                }
            }
        }

        private class EncloseActionListener implements ActionListener {
            private PaletteItem paletteItem;

            EncloseActionListener(PaletteItem paletteItem) {
                this.paletteItem = paletteItem;
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                RADVisualContainer metacont = getContainer(components);
                if (metacont != null) {
                    FormModel formModel = metacont.getFormModel();
                    MetaComponentCreator creator = formModel.getComponentCreator();
                    if (metacont.getLayoutSupport() == null) { // free design
                        LayoutModel layoutModel = formModel.getLayoutModel();
                        Object layoutUndoMark = null;
                        UndoableEdit layoutEdit = null;
                        boolean autoUndo = true; // in case of unexpected error, for robustness
                        try {
                            // create and add the new container
                            RADComponent newComp = creator.createComponent(paletteItem, metacont, null);
                            boolean success = (newComp instanceof RADVisualContainer);
                            if (!success) {
                                String msg = NbBundle.getMessage(EncloseAction.class, "MSG_EncloseInNotEmpty"); // NOI18N
                                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(msg));
                            } else {
                                final RADVisualContainer newCont = (RADVisualContainer)newComp;

                                // This also added the container's layout component to
                                // layout model (and registered undo edit). But we want
                                // the layout component to be added by LayoutDesigner as
                                // part of the enclosing operation, so we remove it here.
                                // From now we also need to care about layout undo.
                                layoutUndoMark = layoutModel.getChangeMark();
                                layoutEdit = layoutModel.getUndoableEdit();
                                layoutModel.removeComponent(newCont.getId(), false); // to be added by LayoutDesigner
                                String[] compIds = new String[components.size()];
                                int i = 0;
                                for (RADComponent metacomp : components) {
                                    compIds[i++] = metacomp.getId();
                                }
                                for (RADComponent metacomp : components) {
                                    formModel.removeComponent(metacomp, false);
                                }
                                success = creator.addComponents(components, newCont); // this does not affect layout model
                                if (success) {
                                    final FormDesigner formDesigner = FormEditor.getFormDesigner(formModel);
                                    formDesigner.getLayoutDesigner().encloseInContainer(compIds, newCont.getId());
                                    // "components" would get normally selected as last added,
                                    // but we rather want to select the new container
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            formDesigner.setSelectedComponent(newCont);
                                        }
                                    });
                                } else {
                                    String msg = NbBundle.getMessage(EncloseAction.class, "MSG_EncloseInFailed"); // NOI18N
                                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(msg)); 
                                }
                            }
                            autoUndo = !success;
                        } finally {
                            if (layoutUndoMark != null && !layoutUndoMark.equals(layoutModel.getChangeMark())) {
                                formModel.addUndoableEdit(layoutEdit);
                            }
                            if (autoUndo) {
                                formModel.forceUndoOfCompoundEdit();
                            }
                        }
                    } else { // old layout support
                        // [TBD]
                    }
                }
            }
        }
    }

}
