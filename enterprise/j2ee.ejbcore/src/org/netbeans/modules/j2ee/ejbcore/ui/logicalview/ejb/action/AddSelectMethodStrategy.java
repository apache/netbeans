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

import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
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
import org.netbeans.modules.j2ee.ejbcore.action.SelectMethodGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Pavel Buzek
 */
public class AddSelectMethodStrategy extends AbstractAddMethodStrategy {

    public AddSelectMethodStrategy() {
        super(NbBundle.getMessage(AddSelectMethodStrategy.class, "LBL_AddSelectMethodAction"));
    }

    public AddSelectMethodStrategy(String name) {
        super(name);
    }

    @Override
    public MethodModel getPrototypeMethod(boolean jakartaVariant) {
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.ABSTRACT);
        return MethodModel.create(
                "ejbSelectBy",
                "int",
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList(jakartaVariant ? "jakarta.ejb.FinderException" : "javax.ejb.FinderException"),
                modifiers
                );
    }

    protected MethodCustomizer createDialog(FileObject fileObject, final MethodModel methodModel) throws IOException {
        String className = _RetoucheUtil.getMainClassName(fileObject);
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
        return MethodCustomizerFactory.selectMethod(
                getTitle(),
                methodModel,
                ClasspathInfo.create(fileObject),
                ejbMethodController.createDefaultQL(methodModel),
                _RetoucheUtil.getMethods(fileObject, className)
                );
    }

//    @SuppressWarnings("deprecation") //NOI18N
//    protected void okButtonPressed(final MethodCustomizer methodCustomizer, final MethodType methodType,
//            final FileObject fileObject, String classHandle) throws java.io.IOException {
//        ProgressHandle handle = ProgressHandleFactory.createHandle("Adding method");
//        try {
//            handle.start(100);
//            String className = _RetoucheUtil.getMainClassName(fileObject);
//            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
//            MethodModel method = methodType.getMethodElement();
//            EntityMethodController entityMethodController = (EntityMethodController) ejbMethodController;
//            entityMethodController.addSelectMethod(method, methodCustomizer.getEjbQL(), getEjbModule(fileObject).getDeploymentDescriptor());
//            handle.progress(99);
//        } finally {
//            handle.finish();
//        }
//    }

    public MethodType.Kind getPrototypeMethodKind() {
        return MethodType.Kind.SELECT;
    }

    protected void generateMethod(final MethodModel method, final boolean isOneReturn,
                                  final boolean publishToLocal, final  boolean publishToRemote,
                                  final String ejbql, FileObject ejbClassFO, String ejbClass) throws IOException {
        final SelectMethodGenerator generator = SelectMethodGenerator.create(ejbClass, ejbClassFO);

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    generator.generate(method, publishToLocal, publishToRemote, isOneReturn, ejbql);
                } catch (IOException ioe) {
                    Logger.getLogger(SelectMethodGenerator.class.getName()).log(Level.WARNING, null, ioe);
                }
            }
        });

    }

    public boolean supportsEjb(FileObject fileObject,final String className) {

        final AtomicBoolean isCMP = new AtomicBoolean(false);

        EjbJar ejbModule = getEjbModule(fileObject);
        if (ejbModule != null) {
            MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
            try {
                if (metadataModel.isReady()) {
                    isCMP.set(metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Boolean>() {
                        @Override
                        public Boolean run(EjbJarMetadata metadata) {
                            Ejb ejb = metadata.findByEjbClass(className);
                            if (ejb instanceof Entity) {
                                Entity entity = (Entity) ejb;
                                return Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType());
                            }
                            return false;
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

                            isCMP.set(AddBusinessMethodStrategy.isEntity(cc, te)
                                    && isAbstract(cc, te));
                        }

                        private boolean isAbstract(CompilationController cc, TypeElement te) {
                            return te.getModifiers().contains(Modifier.ABSTRACT);

                        }
                    }, true);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return isCMP.get();
    }
}
