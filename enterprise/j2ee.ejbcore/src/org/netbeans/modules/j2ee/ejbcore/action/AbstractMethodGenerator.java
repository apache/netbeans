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

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AbstractAddMethodStrategy;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Adamek
 */
public abstract class AbstractMethodGenerator {

    protected final String ejbClass;
    protected final FileObject ejbClassFileObject;
    protected final org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule;

    protected AbstractMethodGenerator(String ejbClass, FileObject ejbClassFileObject) {
        Parameters.notNull("ejbClass", ejbClass);
        Parameters.notNull("ejbClassFileObject", ejbClassFileObject);
        this.ejbClass = ejbClass;
        this.ejbClassFileObject = ejbClassFileObject;
        this.ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(ejbClassFileObject);
    }

    /**
     * Founds business interface if exists and adds method there, adds method into interface <code>className</code> otherwise
     */
    protected void addMethodToInterface(final MethodModel methodModel, String className) throws IOException {
        String ci = findCommonInterface(ejbClass, className);
        // ci == null if there is no 'business' interface
        final String commonInterface = (ci != null) ? ci : className;
        final FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, commonInterface);

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    addMethod(methodModel, fileObject, commonInterface);
                } catch (IOException ioe) {
                    Logger.getLogger(AbstractAddMethodStrategy.class.getName()).log(Level.WARNING, null, ioe);
                }
            }
        });
    }

    /**
     * Adds method to class.
     * <p>
     * <b>Should be called outside EDT.</b>
     */
    protected static void addMethod(final MethodModel methodModel, FileObject fileObject, final String className) throws IOException {
        if (fileObject != null && methodModel != null){
            JavaSource javaSource = JavaSource.forFileObject(fileObject);
            javaSource.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                    boolean generateDefaultBody = (typeElement.getKind() != ElementKind.INTERFACE) && !methodModel.getModifiers().contains(Modifier.ABSTRACT);
                    MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel, generateDefaultBody);
                    ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                    ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                    workingCopy.rewrite(classTree, newClassTree);
                }
            }).commit();
        }
    }

    protected synchronized void saveXml() throws IOException {
        FileObject ddFileObject = ejbModule.getDeploymentDescriptor();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFileObject); // EJB 2.1
        if (ejbJar != null) {
                ejbJar.write(ddFileObject);
        }
    }

    /**
     * Returns map of EJB interface class names, where keys are appropriate constants from {@link EntityAndSession}
     */
    protected Map<String, String> getInterfaces() throws IOException {
        Future<Map<String, String>> futureResult = ejbModule.getMetadataModel().runReadActionWhenReady(new MetadataModelAction<EjbJarMetadata, Map<String, String>>() {
            public Map<String, String> run(EjbJarMetadata metadata) throws Exception {
                EntityAndSession ejb = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                Map<String, String> result = new HashMap<String, String>();
                if (ejb != null){
                    result.put(EntityAndSession.LOCAL, ejb.getLocal());
                    result.put(EntityAndSession.LOCAL_HOME, ejb.getLocalHome());
                    result.put(EntityAndSession.REMOTE, ejb.getRemote());
                    result.put(EntityAndSession.HOME, ejb.getHome());
                }
                return result;
            }
        });
        try {
            return futureResult.get();
        } catch (Exception ex) {
            Logger.getLogger(AbstractMethodGenerator.class.getName()).log(Level.WARNING, null, ex);
            return Collections.<String, String>emptyMap();
        }
    }

    private static String findCommonInterface(final String className1, final String className2) throws IOException {
        //TODO: RETOUCHE
        return null;
    }

}
