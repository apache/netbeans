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
package org.netbeans.modules.jakarta.web.beans.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.TypeElement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.jakarta.web.beans.api.model.BeansModel;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.navigation.actions.WebBeansActionHelper;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public final class DecoratorsModel extends DefaultTreeModel implements
        JavaHierarchyModel
{
    
    private static final long serialVersionUID = 5096971301097791384L;
    
    private static final Logger LOG = Logger.getLogger(
            DecoratorsModel.class.getName());

    public DecoratorsModel( Collection<TypeElement> decorators , 
            BeansModel beansModel, CompilationController controller ,
            MetadataModel<WebBeansModel> model) 
    {
        super(null);
        myModel = model;
        
        myHandles = new ArrayList<ElementHandle<TypeElement>>( decorators.size());
        myEnabledDecorators = new LinkedHashSet<ElementHandle<TypeElement>>();
        
        LinkedHashSet<TypeElement> enabled = WebBeansActionHelper.
            getEnabledDecorators( decorators,beansModel, myEnabledDecorators, 
                    controller);
        for (TypeElement decorator : decorators ) {
            myHandles.add( ElementHandle.create( decorator ));
        }
        
        update( decorators , enabled , controller );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.JavaHierarchyModel#fireTreeNodesChanged()
     */
    @Override
    public void fireTreeNodesChanged() {
        super.fireTreeNodesChanged(this, getPathToRoot((TreeNode)getRoot()), 
                null, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.JavaHierarchyModel#update()
     */
    @Override
    public void update() {
        updateHandles( myHandles , myEnabledDecorators);
    }
    
    private void updateHandles( final List<ElementHandle<TypeElement>> handles,
            final LinkedHashSet<ElementHandle<TypeElement>> enabled )
    {
        try {
            getModel().runReadAction(
                    new MetadataModelAction<WebBeansModel, Void>() {

                        @Override
                        public Void run( WebBeansModel model ) {
                            List<TypeElement> list = new ArrayList<TypeElement>(
                                    handles.size());
                            LinkedHashSet<TypeElement> set = 
                                new LinkedHashSet<TypeElement>();
                            for (ElementHandle<TypeElement> handle : handles) {
                                TypeElement type = handle.resolve(model
                                        .getCompilationController());
                                if (type != null) {
                                    list.add(type);
                                }
                                if (enabled.contains(handle)) {
                                    set.add(type);
                                }
                            }
                            update(list, set, model.getCompilationController());
                            return null;
                        }
                    });

            return;
        }
        catch (MetadataModelException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void update( Collection<TypeElement> foundDecorators,
            LinkedHashSet<TypeElement> enabled, CompilationController controller )
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        LinkedHashSet<TypeElement> allDecorators = new LinkedHashSet<TypeElement>();
        allDecorators.addAll( enabled );
        allDecorators.addAll( foundDecorators );
        
        for (TypeElement type : allDecorators) {
            FileObject fileObject = SourceUtils.getFile(ElementHandle
                    .create(type), controller.getClasspathInfo());
            TypeTreeNode node = new TypeTreeNode(fileObject, type,  
                        !enabled.contains(type ),controller);
            root.add( node );
        }
        setRoot(root);                
    }
    
    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    
    private MetadataModel<WebBeansModel> myModel;
    private List<ElementHandle<TypeElement>> myHandles;
    private LinkedHashSet<ElementHandle<TypeElement>> myEnabledDecorators;
}
