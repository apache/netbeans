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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
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
public final class EventsModel extends DefaultTreeModel implements
        JavaHierarchyModel
{
    
    private static final long serialVersionUID = -4924076156788647582L;
    
    private static final Logger LOG = Logger.getLogger(
            EventsModel.class.getName());
    
    public EventsModel( List<VariableElement> fields ,
            CompilationController controller ,MetadataModel<WebBeansModel> model ) 
    {
        super( null );
        myModel = model;
        myHandles = new ArrayList<ElementHandle<VariableElement>>( fields.size());
        for (VariableElement field : fields) {
            ElementHandle<VariableElement> handle = ElementHandle.create( field );
            myHandles.add( handle );
        }
        
        update( fields , controller );
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
    
    private void update( List<VariableElement> vars , 
            CompilationController controller) 
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        for (VariableElement var : vars) {
            FileObject fileObject = SourceUtils.getFile(ElementHandle
                    .create(var), controller.getClasspathInfo());
            InjectableTreeNode<Element> node = 
                new InjectableTreeNode<Element>(fileObject, var,  
                        (DeclaredType)controller.getElementUtilities().
                        enclosingTypeElement(var).asType(), false,controller);
            root.add( node );
        }
        setRoot(root);
    }
    
    private void updateHandles( final List<ElementHandle<VariableElement>> handles ) {
        try {
            getModel().runReadAction(
                    new MetadataModelAction<WebBeansModel, Void>() {

                        public Void run( WebBeansModel model ) {
                            List<VariableElement> list = 
                                new ArrayList<VariableElement>(handles.size());
                            for (ElementHandle<VariableElement> handle : 
                                handles)
                            {
                                VariableElement var = handle.resolve( 
                                        model.getCompilationController());
                                if ( var != null ){
                                    list.add( var );
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
    
    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    
    private MetadataModel<WebBeansModel> myModel;
    private List<ElementHandle<VariableElement>> myHandles;

}
