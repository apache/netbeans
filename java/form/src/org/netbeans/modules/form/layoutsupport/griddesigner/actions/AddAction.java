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

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JMenuItem;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.VisualReplicator;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridInfoProvider;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridUtils;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteMenuView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.NbBundle;

/**
 * Action that allows to add a new component into the grid.
 *
 * @author Jan Stola
 */
public class AddAction extends AbstractGridAction {
    /**
     * Visual replicator responsible for the replication
     * of the container with the grid.
     */
    private VisualReplicator replicator;

    /**
     * Creates new {@code AddAction}.
     * 
     * @param replicator visual replicator responsible for
     * the replication of the container with the grid.
     */
    public AddAction(VisualReplicator replicator) {
        this.replicator = replicator;
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        // This action provides just popup presenter. The actual
        // addition of the component is performed by AddComponentAction.
        return null;
    }
    
    @Override
    public JMenuItem getPopupPresenter(final GridActionPerformer performer) {
        PaletteMenuView menu = new PaletteMenuView(new NodeAcceptor() {
            @Override
            public boolean acceptNodes(Node[] nodes) {
                if (nodes.length != 1) {
                    return false;
                }
                PaletteItem paletteItem = nodes[0].getCookie(PaletteItem.class);
                if (paletteItem == null) {
                    return false;
                }
                performer.performAction(new AddComponentAction(paletteItem));
                return true;
            }
        });
        menu.disableHack();
        String name = NbBundle.getMessage(AddAction.class, "AddAction_Name"); // NOI18N
        menu.setText(name);
        return menu;
    }

    /**
     * Action that adds one component into the grid.
     */
    class AddComponentAction extends AbstractGridAction {
        /** Palette item that describes the component to add. */
        private PaletteItem pItem;

        /**
         * Creates new {@code AddComponentAction}.
         * 
         * @param pItem palette item that describes the component to add.
         */
        AddComponentAction(PaletteItem pItem) {
            this.pItem = pItem;
        }

        @Override
        public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
            GridInfoProvider info = gridManager.getGridInfo();
            int columns = info.getColumnCount();
            int rows = info.getRowCount();
            GridUtils.removePaddingComponents(gridManager);
            
            RADVisualContainer container = (RADVisualContainer)replicator.getTopMetaComponent();
            FormModel formModel = container.getFormModel();
            RADComponent metacomp = formModel.getComponentCreator().createComponent(
                    pItem, container, null);
            if (metacomp != null && isSubComponent(metacomp, container)) {
                replicator.addComponent(metacomp);
                Component comp = (Component)replicator.getClonedComponent(metacomp);
                gridManager.setGridX(comp, context.getFocusedColumn());
                gridManager.setGridY(comp, context.getFocusedRow());
                context.setSelectedComponents(Collections.singleton(comp));
            }

            gridManager.updateLayout(false);
            GridUtils.revalidateGrid(gridManager);
            GridUtils.addPaddingComponents(gridManager, columns, rows);
            GridUtils.revalidateGrid(gridManager);
            return null;
        }

    }

    private static boolean isSubComponent(RADComponent metacomp, RADVisualContainer container) {
        if (metacomp instanceof RADVisualComponent) {
            RADVisualComponent component = (RADVisualComponent)metacomp;
            List<RADVisualComponent> components = Arrays.asList(container.getSubComponents());
            return components.contains(component);
        } else {
            return false;
        }
    }

}
