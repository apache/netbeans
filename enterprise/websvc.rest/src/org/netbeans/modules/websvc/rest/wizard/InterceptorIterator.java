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
public class InterceptorIterator extends AbstractJaxRsFeatureIterator
{
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractJaxRsFeatureIterator#createPanel(org.openide.WizardDescriptor)
     */
    @Override
    protected Panel<?> createPanel( WizardDescriptor wizard ) {
        return new InterceptorPanel( wizard );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractJaxRsFeatureIterator#getTitleKey()
     */
    @Override
    protected String getTitleKey() {
        return "TXT_CreateJaxRsInterceptor";                // NOI18N
    }


    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.ProgressInstantiatingIterator#instantiate(org.netbeans.api.progress.ProgressHandle)
     */
    @Override
    public Set<?> instantiate( ProgressHandle handle ) throws IOException {
        handle.start();
        
        handle.progress(NbBundle.getMessage(InterceptorIterator.class, 
                "TXT_GenerateInterceptorFile"));                            // NOI18N
        
        FileObject targetFolder = Templates.getTargetFolder(getWizard());
        String name = Templates.getTargetName(getWizard());
        FileObject interceptorClass = GenerationUtils.createClass(targetFolder,
                name, null );
        
        implementInterceptors(interceptorClass);
        
        handle.finish();
        return Collections.singleton(interceptorClass);
    }
    
    private void implementInterceptors( FileObject filterClass ) throws IOException{
        JavaSource javaSource = JavaSource.forFileObject(filterClass);
        if ( javaSource == null ){
            return;
        }
        
        final boolean reader = Boolean.TRUE.equals(
                getWizard().getProperty(InterceptorPanel.READER));
        final boolean writer = Boolean.TRUE.equals(
                getWizard().getProperty(InterceptorPanel.WRITER));
        javaSource.runModificationTask( new Task<WorkingCopy>() {
            
            @Override
            public void run( WorkingCopy copy ) throws Exception {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree newTree = tree;
                TreeMaker treeMaker = copy.getTreeMaker();
                
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);

                AnnotationTree provider = genUtils
                        .createAnnotation("javax.ws.rs.ext.Provider"); // NOI18N
                newTree = genUtils.addAnnotation(newTree, provider);
                
                LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
                if (reader) {
                    params.put("context",
                            "javax.ws.rs.ext.ReaderInterceptorContext");    // NOI18N
                    newTree = genUtils.addImplementsClause(newTree,
                            "javax.ws.rs.ext.ReaderInterceptor");           // NOI18N
                    MethodTree method = createMethod(genUtils, treeMaker,
                            "aroundReadFrom", Object.class.getName() ,params);// NOI18N
                    newTree = treeMaker.addClassMember(newTree, method);
                }
                if (writer) {
                    params.clear();
                    params.put("responseContext",
                            "javax.ws.rs.ext.WriterInterceptorContext");    // NOI18N
                    newTree = genUtils.addImplementsClause(newTree,
                            "javax.ws.rs.ext.WriterInterceptor");           // NOI18N
                    MethodTree method = createMethod(genUtils, treeMaker,
                            "aroundWriteTo",  params);                      // NOI18N
                    newTree = treeMaker.addClassMember(newTree, method);
                }

                copy.rewrite(tree, newTree);
            }
        }).commit();
    }
    
}
