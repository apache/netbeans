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
