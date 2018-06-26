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
import org.netbeans.modules.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
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
