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
package org.netbeans.modules.websvc.rest.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;

/**
 * @author ads
 *
 */
public class JaxRsFilterIterator extends AbstractJaxRsFeatureIterator
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractJaxRsFeatureIterator#createPanel(org.openide.WizardDescriptor)
     */
    @Override
    protected Panel<?> createPanel( WizardDescriptor wizard ) {
        return new JaxRsFilterPanel(wizard);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractJaxRsFeatureIterator#getTitleKey()
     */
    @Override
    protected String getTitleKey() {
        return "TXT_CreateJaxRsFilter";             // NOI18N
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.ProgressInstantiatingIterator#instantiate(org.netbeans.api.progress.ProgressHandle)
     */
    @Override
    public Set<?> instantiate( ProgressHandle handle ) throws IOException {
        handle.start();
        
        handle.progress(NbBundle.getMessage(JaxRsFilterIterator.class, 
                "TXT_GenerateFilterFile"));
        
        FileObject targetFolder = Templates.getTargetFolder(getWizard());
        String name = Templates.getTargetName(getWizard());
        FileObject filterClass = GenerationUtils.createClass(targetFolder,name, null );
        
        implementFilters(filterClass);
        
        handle.finish();
        return Collections.singleton(filterClass);
    }
    
    private void implementFilters( FileObject filterClass ) throws IOException{
        JavaSource javaSource = JavaSource.forFileObject(filterClass);
        if ( javaSource == null ){
            return;
        }
        
        final boolean client = Boolean.TRUE.equals(
                getWizard().getProperty(JaxRsFilterPanel.CLIENT_FILTER));
        final boolean server = Boolean.TRUE.equals(
                getWizard().getProperty(JaxRsFilterPanel.SERVER_FILTER));
        final boolean request = Boolean.TRUE.equals(
                getWizard().getProperty(JaxRsFilterPanel.REQUEST));
        final boolean response = Boolean.TRUE.equals(
                getWizard().getProperty(JaxRsFilterPanel.RESPONSE));
        final boolean addPreMatch = Boolean.TRUE.equals(
                getWizard().getProperty(JaxRsFilterPanel.PRE_MATCHING));
        final boolean addProvider = Boolean.TRUE.equals(
                getWizard().getProperty(JaxRsFilterPanel.PROVIDER));
        javaSource.runModificationTask( new Task<WorkingCopy>() {
            
            @Override
            public void run( WorkingCopy copy ) throws Exception {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree newTree = tree;
                TreeMaker treeMaker = copy.getTreeMaker();
                
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                
                if ( addPreMatch ){
                    AnnotationTree preMatching = genUtils.
                            createAnnotation("javax.ws.rs.container.PreMatching");
                    newTree = genUtils.addAnnotation(newTree, preMatching);
                }
                if ( addProvider ){
                    AnnotationTree provider = genUtils.
                            createAnnotation("javax.ws.rs.ext.Provider");
                    newTree = genUtils.addAnnotation(newTree, provider);
                }
                
                LinkedHashMap<String,String> params = new LinkedHashMap<String, String>();
                if ( client ){
                    if ( request ){
                        params.put("requestContext", 
                                "javax.ws.rs.client.ClientRequestContext");     // NOI18N
                        newTree = genUtils.addImplementsClause(newTree, 
                                "javax.ws.rs.client.ClientRequestFilter");      // NOI18N
                        MethodTree method = createMethod(genUtils, treeMaker, params);
                        newTree = treeMaker.addClassMember( newTree, method);   
                    }
                    if ( response ){
                        params.put("requestContext", 
                                "javax.ws.rs.client.ClientRequestContext");     // NOI18N
                        params.put("responseContext", 
                                "javax.ws.rs.client.ClientResponseContext");    // NOI18N
                        newTree = genUtils.addImplementsClause(newTree, 
                                "javax.ws.rs.client.ClientResponseFilter");     // NOI18N
                        MethodTree method = createMethod(genUtils, treeMaker, params);
                        newTree = treeMaker.addClassMember( newTree, method);
                    }
                }
                if ( server ){
                    params.clear();
                    if ( request ){
                        params.put("requestContext", 
                                "javax.ws.rs.container.ContainerRequestContext");// NOI18N
                        newTree = genUtils.addImplementsClause(newTree, 
                                "javax.ws.rs.container.ContainerRequestFilter");// NOI18N
                        MethodTree method = createMethod(genUtils, treeMaker, params);
                        newTree = treeMaker.addClassMember( newTree, method);
                    }
                    if ( response ){
                        params.put("requestContext", 
                                "javax.ws.rs.container.ContainerRequestContext");// NOI18N
                        params.put("responseContext",
                                "javax.ws.rs.container.ContainerResponseContext");// NOI18N
                        newTree = genUtils.addImplementsClause(newTree, 
                                "javax.ws.rs.container.ContainerResponseFilter");// NOI18N
                        MethodTree method = createMethod(genUtils, treeMaker, params);
                        newTree = treeMaker.addClassMember( newTree, method);
                    }
                }
                copy.rewrite(tree, newTree);
            }
        }).commit();
    }
    
    private MethodTree createMethod(GenerationUtils genUtils,TreeMaker maker, 
            LinkedHashMap<String,String> methodParams)
    {
        return createMethod(genUtils, maker, "filter", methodParams);       // NOI18N
    }

}
