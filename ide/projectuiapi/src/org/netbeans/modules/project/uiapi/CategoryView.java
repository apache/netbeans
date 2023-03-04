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

package org.netbeans.modules.project.uiapi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * @author Petr Hrebejk
 */
public class CategoryView extends JPanel implements ExplorerManager.Provider, PropertyChangeListener {
    private static final @StaticResource String DEFAULT_CATEGORY = "org/netbeans/modules/project/uiapi/defaultCategory.gif";

    private ExplorerManager manager;
    private BeanTreeView btv;
    private CategoryModel categoryModel;

    public CategoryView( CategoryModel categoryModel ) {

        this.categoryModel = categoryModel;

        // See #36315
        manager = new ExplorerManager();

        setLayout( new BorderLayout() );

        Dimension size = new Dimension( 220, 4 );
        btv = new BeanTreeView();    // Add the BeanTreeView
        btv.setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        btv.setPopupAllowed( false );
        btv.setRootVisible( false );
        btv.setDefaultActionAllowed( false );
        btv.setMinimumSize( size );
        btv.setPreferredSize( size );
        btv.setMaximumSize( size );
        btv.setDragSource (false);
        this.add( btv, BorderLayout.CENTER );
        manager.setRootContext( createRootNode( categoryModel ) );
        manager.addPropertyChangeListener( this );
        categoryModel.addPropertyChangeListener( this );
        btv.expandAll();
        selectNode( categoryModel.getCurrentCategory() );

        btv.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CategoryView.class,"AN_CatgoryView"));
        btv.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CategoryView.class,"AD_CategoryView"));

    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public void addNotify() {
        super.addNotify();
        btv.expandAll();
        btv.requestFocusInWindow();
    }

    // Private methods -----------------------------------------------------

    private void selectNode( ProjectCustomizer.Category category ) {

        Node node = findNode( category, manager.getRootContext() );

        if ( node != null ) {
            try {
                manager.setSelectedNodes( new Node[] { node } );
            }
            catch ( PropertyVetoException e ) {
                // No node will be selected
            }
        }

    }

    private Node findNode( ProjectCustomizer.Category category, Node node ) {

        Children ch = node.getChildren();

        if ( ch != null && ch != Children.LEAF ) {
            Node nodes[] = ch.getNodes( true );

            if ( nodes != null ) {
                for (Node child : nodes) {
                    ProjectCustomizer.Category cc = child.getLookup().lookup(ProjectCustomizer.Category.class);

                    if ( cc == category ) {
                        return child;
                    }
                    else {
                        Node n = findNode(category, child);
                        if ( n != null ) {
                            return n;
                        }
                    }
                }
            }
        }

        return null;
    }


    private Node createRootNode( CategoryModel categoryModel ) {
        ProjectCustomizer.Category rootCategory = ProjectCustomizer.Category.create( "root", "root", null, categoryModel.getCategories() ); // NOI18N
        return new CategoryNode( rootCategory );
    }

    // Implementation of property change listener --------------------------

    public void propertyChange(PropertyChangeEvent evt) {

        Object source = evt.getSource();
        String propertyName = evt.getPropertyName();

        if ( source== manager && ExplorerManager.PROP_SELECTED_NODES.equals( propertyName ) ) {
            Node nodes[] = manager.getSelectedNodes();
            if ( nodes == null || nodes.length <= 0 ) {
                return;
            }
            Node node = nodes[0];

            ProjectCustomizer.Category category = node.getLookup().lookup(ProjectCustomizer.Category.class);
            if ( category != categoryModel.getCurrentCategory() ) {
                categoryModel.setCurrentCategory( category );
            }
        }

        if ( source == categoryModel && CategoryModel.PROP_CURRENT_CATEGORY.equals( propertyName ) ) {
            selectNode( (ProjectCustomizer.Category)evt.getNewValue() );
        }

    }


    // Private Inner classes -----------------------------------------------

    /** Node to be used for configuration
     */
    private static class CategoryNode extends AbstractNode implements PropertyChangeListener {

        private Image icon = ImageUtilities.loadImage( DEFAULT_CATEGORY); // NOI18N

        private final ProjectCustomizer.Category category;

        public CategoryNode( ProjectCustomizer.Category category ) {
            super( ( category.getSubcategories() == null || category.getSubcategories().length == 0 ) ?
                        Children.LEAF : new CategoryChildren( category.getSubcategories() ),
                   Lookups.fixed(category));
            setName( category.getName() );
            this.category = category;
            setDisplayName( category.getDisplayName() );

            if ( category.getIcon() != null ) {
                this.icon = category.getIcon();
            }
            Utilities.getCategoryChangeSupport(category).addPropertyChangeListener(this);
        }

        @Override
        public String getHtmlDisplayName() {
            return category.isValid() ? null :
                "<html><font color=\"!nb.errorForeground\">" + // NOI18N
                    category.getDisplayName() + "</font></html>"; // NOI18N
        }

        @Override
        public Image getIcon( int type ) {
            return this.icon;
        }

        @Override
        public Image getOpenedIcon( int type ) {
            return getIcon( type );
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (CategoryChangeSupport.VALID_PROPERTY.equals(evt.getPropertyName())) {
                fireDisplayNameChange(null, null);
            }
        }

    }

    /** Children used for configuration
     */
    private static class CategoryChildren extends Children.Keys<ProjectCustomizer.Category> {

        private final ProjectCustomizer.Category[] descriptions;

        public CategoryChildren( ProjectCustomizer.Category[] descriptions ) {
            this.descriptions = descriptions;
        }

        // Children.Keys impl --------------------------------------------------

        @Override
        public void addNotify() {
            setKeys( descriptions );
        }

        @Override
        public void removeNotify() {
            setKeys(new ProjectCustomizer.Category[0]);
        }

        @Override
        protected Node[] createNodes(ProjectCustomizer.Category c) {
            return new Node[] {new CategoryNode(c)};
        }
    }

}


