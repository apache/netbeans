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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class BusinessMethodGenerator extends AbstractMethodGenerator {

    private BusinessMethodGenerator(String ejbClass, FileObject ejbClassFileObject) {
        super(ejbClass, ejbClassFileObject);
    }

    public static BusinessMethodGenerator create(String ejbClass, FileObject ejbClassFileObject) {
        return new BusinessMethodGenerator(ejbClass, ejbClassFileObject);
    }

    /**
     * Generates methods for local/remote interfaces.
     * <p>
     * <b>Should be called outside EDT.</b>
     */
    public void generate(final MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {

        Map<String, String> interfaces = getInterfaces();
        String local = interfaces.get(EntityAndSession.LOCAL);
        final String remote = interfaces.get(EntityAndSession.REMOTE);

        // local interface
        if (generateLocal && local != null) {
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    methodModel.getReturnType(),
                    null,
                    methodModel.getParameters(),
                    methodModel.getExceptions(),
                    methodModel.getModifiers()
                    );
            addMethodToInterface(methodModelCopy, local);
        }

        // remote interface, add RemoteException if it's not there (in EJB 2.1)
        if (generateRemote && remote != null) {

            final List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());

            MetadataModel<EjbJarMetadata> metadataModel = EjbJar.getEjbJar(ejbClassFileObject).getMetadataModel();
            BigDecimal version = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, BigDecimal>() {
                public BigDecimal run(EjbJarMetadata metadata) throws Exception {
                    return metadata.getRoot().getVersion();
                }
            });
            final boolean isEjb2x = (version != null && version.doubleValue() <= 2.1);

            JavaSource javaSource = JavaSource.forFileObject(ejbClassFileObject);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    if (isEjb2x) {
                        exceptions.add("java.rmi.RemoteException"); // NOI18N
                    } else {
                        TypeElement typeElement = controller.getElements().getTypeElement(ejbClass);
                        TypeMirror remoteType = controller.getElements().getTypeElement("java.rmi.Remote").asType(); // NOI18N
                        if (typeElement != null) {
                            for (TypeMirror typeMirror : typeElement.getInterfaces()) {
                                if (controller.getTypes().isSameType(remoteType, typeMirror)) {
                                    if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) { // NOI18N
                                        exceptions.add("java.rmi.RemoteException"); // NOI18N
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);

            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    methodModel.getReturnType(),
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    methodModel.getModifiers()
                    );
            addMethodToInterface(methodModelCopy, remote);
        }

        // ejb class
        // add all specified annothations and join Override if has local, remote interfaces
        List<MethodModel.Annotation> annotations = new ArrayList<MethodModel.Annotation>();
        if (!methodModel.getAnnotations().isEmpty()) {
            annotations.addAll(methodModel.getAnnotations());
        }
        if ((generateLocal && local != null) || (generateRemote && remote != null)) {
            annotations.add(MethodModel.Annotation.create("java.lang.Override")); //NOI18N
        }
        // add 'public' modifier
        MethodModel methodModelCopy = MethodModel.create(
                methodModel.getName(),
                methodModel.getReturnType(),
                methodModel.getBody(),
                methodModel.getParameters(),
                methodModel.getExceptions(),
                Collections.singleton(Modifier.PUBLIC),
                annotations
                );

        addMethod(methodModelCopy, ejbClassFileObject, ejbClass);

    }

}
