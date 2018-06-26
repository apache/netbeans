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
 *
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
