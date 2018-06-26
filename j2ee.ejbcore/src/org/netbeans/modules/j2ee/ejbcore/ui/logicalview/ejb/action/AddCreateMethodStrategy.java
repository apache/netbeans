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

    protected MethodModel getPrototypeMethod() {
        return MethodModel.create(
                "create",
                "void",
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList("javax.ejb.CreateException"),
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
