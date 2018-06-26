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
