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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.netbeans.modules.jakarta.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public final class InterceptorsModel extends DefaultTreeModel 
    implements JavaHierarchyModel 
{

    private static final long serialVersionUID = 8135037731227112414L;
    
    private static final Logger LOG = Logger.getLogger(
            InterceptorsModel.class.getName());
    
    public InterceptorsModel( InterceptorsResult result ,
            CompilationController controller ,MetadataModel<WebBeansModel> model ) 
    {
        super( null );
        myModel = model;
        List<TypeElement> interceptors = result.getAllInterceptors();
        myHandles = new ArrayList<ElementHandle<TypeElement>>( interceptors.size());
        myDisabledInterceptors = new HashSet<ElementHandle<TypeElement>>();
        Set<TypeElement> disabled = new HashSet<TypeElement>();
        for (TypeElement interceptor : interceptors) {
            ElementHandle<TypeElement> handle = ElementHandle.create( interceptor );
            myHandles.add( handle );
            if ( result.isDisabled( interceptor )){
                myDisabledInterceptors.add( handle );
                disabled.add( interceptor );
            }
        }
        
        update( interceptors , disabled , controller );
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
        updateHandles( myHandles , myDisabledInterceptors);
    }
    
    private void update( List<TypeElement> types , Set<TypeElement> disabled,
            CompilationController controller) 
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        for (TypeElement type : types) {
            FileObject fileObject = SourceUtils.getFile(ElementHandle
                    .create(type), controller.getClasspathInfo());
            TypeTreeNode node = new TypeTreeNode(fileObject, type,  
                        disabled.contains(type ),controller);
            root.add( node );
        }
        setRoot(root);
    }
    
    private void updateHandles( final List<ElementHandle<TypeElement>> handles ,
            final Set<ElementHandle<TypeElement>> disabled ) 
    {
        try {
            getModel().runReadAction(
                    new MetadataModelAction<WebBeansModel, Void>() {

                        @Override
                        public Void run( WebBeansModel model ) {
                            List<TypeElement> list = 
                                new ArrayList<TypeElement>(handles.size());
                            Set<TypeElement> set = new HashSet<TypeElement>();
                            for (ElementHandle<TypeElement> handle : 
                                handles)
                            {
                                TypeElement type = handle.resolve( 
                                        model.getCompilationController());
                                if ( type != null ){
                                    list.add( type );
                                }
                                if ( disabled.contains( handle )){
                                    set.add( type );
                                }
                            }
                            update( list , set, model.getCompilationController());
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
    
    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    
    private MetadataModel<WebBeansModel> myModel;
    private List<ElementHandle<TypeElement>> myHandles;
    private Set<ElementHandle<TypeElement>> myDisabledInterceptors;
}
