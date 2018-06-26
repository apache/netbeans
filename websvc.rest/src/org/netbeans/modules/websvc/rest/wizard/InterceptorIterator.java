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
