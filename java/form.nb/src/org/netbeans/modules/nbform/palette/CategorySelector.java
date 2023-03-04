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

package org.netbeans.modules.nbform.palette;

import javax.swing.*;

import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.nodes.Node;
import org.openide.explorer.view.ListView;
import org.openide.explorer.*;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;

/**
 * A simple panel allowing the user to choose one of the palette categories.
 * Used by ChooseCategoryWizardPanel in the "Add to Palette" wizard for
 * choosing the target category for added components.
 */

class CategorySelector extends JPanel implements ExplorerManager.Provider {

    private ExplorerManager explorerManager;

    CategorySelector() {
        explorerManager = new ExplorerManager();	
        explorerManager.setRootContext(getCategoryRootNode());	
	
        ListView listView = new ListView();
        // Issue 50703 - restore the default scroll pane's border
        JScrollPane scrollPane = new JScrollPane();
        listView.setBorder(scrollPane.getBorder());
        listView.getAccessibleContext().setAccessibleDescription(
            PaletteUtils.getBundleString("ACSD_CTL_PaletteCategories")); // NOI18N
        listView.setPopupAllowed(false);
        listView.setTraversalAllowed(false);
	
        JLabel categoryLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, 
                PaletteUtils.getBundleString("CTL_PaletteCategories")); // NOI18N
        if ((listView.getViewport() != null) && (listView.getViewport().getView() != null)) {
            categoryLabel.setLabelFor(listView.getViewport().getView());
        } else {
            categoryLabel.setLabelFor(listView);
        }

        getAccessibleContext().setAccessibleDescription(
            PaletteUtils.getBundleString("ACSD_PaletteCategoriesSelector")); // NOI18N

        setLayout(new java.awt.BorderLayout(0, 5));
        add(categoryLabel, java.awt.BorderLayout.NORTH);
        add(listView, java.awt.BorderLayout.CENTER);
    }

    private Node getCategoryRootNode() {
	Node root = new AbstractNode(new Children.Array());	
	
	Node[] paleteCategories = PaletteUtils.getCategoryNodes(PaletteUtils.getPaletteNode(), false);
	Node[] categoryNodes = new Node[paleteCategories.length];
	
	for (int i = 0; i < paleteCategories.length; i++) {
	    categoryNodes[i] = new FilterNode(paleteCategories[i], Children.LEAF);
	}		
	
	root.getChildren().add(categoryNodes);
	getExplorerManager().setRootContext(root);	

	return root;    
    }
    
    public static String selectCategory() {
        CategorySelector selector = new CategorySelector();
        selector.setBorder(new javax.swing.border.EmptyBorder(12, 12, 0, 11));
        DialogDescriptor dd = new DialogDescriptor(
            selector,
            PaletteUtils.getBundleString("CTL_SelectCategory_Title"), // NOI18N
            true,
            null);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);

        return dd.getValue() == DialogDescriptor.OK_OPTION ?
            selector.getSelectedCategory() : null;
    }

    String getSelectedCategory() {
        Node[] selected = explorerManager.getSelectedNodes();
        return selected.length == 1 ? selected[0].getName() : null;
    }

    // ExplorerManager.Provider
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
        return new java.awt.Dimension(400, 300);
    }
}
