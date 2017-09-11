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
