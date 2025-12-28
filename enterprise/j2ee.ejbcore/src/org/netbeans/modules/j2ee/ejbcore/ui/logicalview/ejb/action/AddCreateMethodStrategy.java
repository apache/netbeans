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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizerFactory;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.action.CreateMethodGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Pavel Buzek
 * @author Martin Adamek
 */
public class AddCreateMethodStrategy extends AbstractAddMethodStrategy {

    public AddCreateMethodStrategy(String name) {
        super (name);
    }

    public AddCreateMethodStrategy() {
        super(NbBundle.getMessage(AddCreateMethodStrategy.class, "LBL_AddCreateMethodAction"));
    }

    protected MethodModel getPrototypeMethod(boolean jakartaVariant) {
        return MethodModel.create(
                "create",
                "void",
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList(jakartaVariant ? "jakarta.ejb.CreateException" : "javax.ejb.CreateException"),
                Collections.<Modifier>emptySet()
                );
    }

    protected MethodCustomizer createDialog(FileObject fileObject, final MethodModel methodModel) throws IOException{
        String className = _RetoucheUtil.getMainClassName(fileObject);
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
        boolean hasRemote = ejbMethodController != null ? ejbMethodController.hasRemote() : false;
        boolean hasLocal = ejbMethodController != null ? ejbMethodController.hasLocal() : false;
        MethodsNode methodsNode = getMethodsNode();
        return MethodCustomizerFactory.createMethod(
                getTitle(),
                methodModel,
                ClasspathInfo.create(fileObject),
                hasRemote,
                hasLocal,
                methodsNode == null ? hasLocal : methodsNode.isLocal(),
                methodsNode == null ? hasRemote : methodsNode.isRemote(),
                _RetoucheUtil.getMethods(fileObject, className)
                );
    }

    public MethodType.Kind getPrototypeMethodKind() {
        return MethodType.Kind.CREATE;
    }

    protected void generateMethod(final MethodModel method, boolean isOneReturn, final boolean publishToLocal,
            final boolean publishToRemote, String ejbql, FileObject ejbClassFO, String ejbClass) throws IOException {
        final CreateMethodGenerator generator = CreateMethodGenerator.create(ejbClass, ejbClassFO);

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    generator.generate(method, publishToLocal, publishToRemote);
                } catch (IOException ioe) {
                    Logger.getLogger(AddBusinessMethodStrategy.class.getName()).log(Level.WARNING, null, ioe);
                }
            }
        });
    }

    public boolean supportsEjb(FileObject fileObject,final String className) {

        final AtomicBoolean isEntityOrStateful = new AtomicBoolean(false);

        EjbJar ejbModule = getEjbModule(fileObject);
        if (ejbModule != null) {
            try {
                MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
                if (metadataModel.isReady()) {
                    isEntityOrStateful.set(metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Boolean>() {
                        @Override
                        public Boolean run(EjbJarMetadata metadata) throws Exception {
                            Ejb ejb = metadata.findByEjbClass(className);
                            if (ejb instanceof Entity) {
                                return true;
                            } else if (ejb instanceof Session) {
                                return Session.SESSION_TYPE_STATEFUL.equals(((Session) ejb).getSessionType());
                            }
                            return Boolean.FALSE;
                        }
                    }));
                } else {
                    JavaSource javaSource = JavaSource.forFileObject(fileObject);
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            TypeElement te = cc.getElements().getTypeElement(className);
                            if (te == null) {
                                return;
                            }

                            isEntityOrStateful.set(AddBusinessMethodStrategy.isEntity(cc, te)
                                    || AddBusinessMethodStrategy.isStateful(cc, te));
                        }
                    }, true);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return isEntityOrStateful.get();
    }

}
