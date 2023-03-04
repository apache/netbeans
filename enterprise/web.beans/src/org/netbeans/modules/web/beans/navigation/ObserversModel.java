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
package org.netbeans.modules.web.beans.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public final class ObserversModel extends DefaultTreeModel implements
        JavaHierarchyModel
{
    
    private static final long serialVersionUID = -7252090049644279891L;
    
    private static final Logger LOG = Logger.getLogger(
            ObserversModel.class.getName());

    public ObserversModel( List<ExecutableElement> methods ,
            CompilationController controller ,MetadataModel<WebBeansModel> model ) 
    {
        super( null );
        myModel = model;
        myHandles = new ArrayList<ElementHandle<ExecutableElement>>( methods.size());
        for (ExecutableElement method : methods) {
            ElementHandle<ExecutableElement> handle = ElementHandle.create( method );
            myHandles.add( handle );
        }
        
        update( methods , controller );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.JavaHierarchyModel#fireTreeNodesChanged()
     */
    @Override
    public void fireTreeNodesChanged() {
        super.fireTreeNodesChanged(this, getPathToRoot((TreeNode)getRoot()), 
                null, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.JavaHierarchyModel#update()
     */
    @Override
    public void update() {
        updateHandles( myHandles );
    }
    
    private void updateHandles( final List<ElementHandle<ExecutableElement>> handles ) {
        try {
            getModel().runReadAction(
                    new MetadataModelAction<WebBeansModel, Void>() {

                        public Void run( WebBeansModel model ) {
                            List<ExecutableElement> list = 
                                new ArrayList<ExecutableElement>(handles.size());
                            for (ElementHandle<ExecutableElement> handle : 
                                handles)
                            {
                                ExecutableElement method = handle.resolve( 
                                        model.getCompilationController());
                                if ( method != null ){
                                    list.add( method );
                                }
                            }
                            update( list , model.getCompilationController());
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

    private void update( List<ExecutableElement> methods , 
            CompilationController controller) 
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        Map<Element, InjectableTreeNode<? extends Element>> methodsMap= 
            new LinkedHashMap<Element, InjectableTreeNode<? extends Element>>();
        
        for (ExecutableElement method : methods) {
            FileObject fileObject = SourceUtils.getFile(ElementHandle
                    .create(method), controller.getClasspathInfo());
            MethodTreeNode node = new MethodTreeNode(fileObject, 
                    method, (DeclaredType)controller.getElementUtilities().
                    enclosingTypeElement(method).asType(),
                    false, controller);
            insertTreeNode( methodsMap , method , node , root ,  controller);
            
        }
        setRoot(root);
    }
    
    private void insertTreeNode(
            Map<Element, InjectableTreeNode<? extends Element>> methods,
            ExecutableElement method, MethodTreeNode node,
            DefaultMutableTreeNode root, CompilationController controller )
    {
        InjectablesModel.insertTreeNode(methods, method, node, root, controller);
    }

    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    
    private MetadataModel<WebBeansModel> myModel;
    private List<ElementHandle<ExecutableElement>> myHandles;

}
