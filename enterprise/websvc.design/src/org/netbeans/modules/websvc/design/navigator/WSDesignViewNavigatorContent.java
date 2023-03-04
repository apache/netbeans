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

/*
 * WSDesignViewNavigatorContent.java
 *
 * Created on April 9, 2007, 5:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.navigator;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author rico
 */
public class WSDesignViewNavigatorContent extends JPanel
        implements ExplorerManager.Provider, PropertyChangeListener{
    
    /** Explorer manager for the tree view. */
    private ExplorerManager explorerManager;
    /** Our schema component node tree view. */
    private TreeView treeView;
    
    
    /** Creates a new instance of WSDesignViewNavigatorContent */
    public WSDesignViewNavigatorContent() {
        setLayout(new BorderLayout());
        explorerManager = new ExplorerManager();
        treeView = new BeanTreeView();
        explorerManager.addPropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent arg0) {
        
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    public void navigate(DataObject implClass){
        add(treeView, BorderLayout.CENTER);
        AbstractNode root = new AbstractNode(Children.create(new WSChildFactory(implClass), true));
        root.setName(NbBundle.getMessage(WSDesignViewNavigatorContent.class, "LBL_Operations"));
        getExplorerManager().setRootContext(root);
        revalidate();
        repaint();
    }
    
    public static class WSChildFactory extends ChildFactory<MethodModel>{
        DataObject implClass;
        public WSChildFactory(DataObject implClass){
            this.implClass = implClass;
        }
        
        @Override
        protected Node createNodeForKey(MethodModel key) {
            AbstractNode n = new AbstractNode(Children.LEAF);
            n.setName(key.getOperationName());
            return n;
        }
        
        @Override
        protected boolean createKeys(List<MethodModel> list){
            if(implClass != null){
                ServiceModel model = ServiceModel.getServiceModel(implClass.getPrimaryFile());
                if ( model != null ){
                    List<MethodModel> operations = model.getOperations();
                    if ( operations != null ){
                        list.addAll(model.getOperations());
                    }
                }
            }
            return true;
        }

    }
    
}
