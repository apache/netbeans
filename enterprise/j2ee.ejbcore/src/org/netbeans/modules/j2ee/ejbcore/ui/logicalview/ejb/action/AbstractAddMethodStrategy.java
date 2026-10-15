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
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Strategy for visual support for adding various methods into an EJB.
 *
 * @author Pavel Buzek
 * @author Martin Adamek
 */
public abstract class AbstractAddMethodStrategy {

    private final String name;

    public AbstractAddMethodStrategy(String name) {
        this.name = name;
    }

    protected abstract MethodModel getPrototypeMethod(boolean jakartaVariant);

    /** Describes method type handled by this action. */
    public abstract MethodType.Kind getPrototypeMethodKind();

    protected abstract MethodCustomizer createDialog(FileObject fileObject, MethodModel methodModel) throws IOException;

    protected abstract void generateMethod(MethodModel method, boolean isOneReturn, boolean publishToLocal, boolean publishToRemote,
            String ejbql, FileObject ejbClassFO, String ejbClass) throws IOException;

    public abstract boolean supportsEjb(FileObject fileObject, String className);

    public String getTitle() {
        return name;
    }

    public void addMethod(final FileObject fileObject, final String className) throws IOException {
        if (className == null) {
            return;
        }

        boolean jakartaVariant = true;
        ClassPath cp = ClassPath.getClassPath(fileObject, ClassPath.COMPILE);
        if (cp != null) {
            jakartaVariant = cp.findResource("javax/ejb/Stateless.class") == null // NOI18N
                    || cp.findResource("jakarta/ejb/Stateless.class") != null; // NOI18N
        }
        final MethodModel methodModel = getPrototypeMethod(jakartaVariant);
        ScanDialog.runWhenScanFinished(new Runnable() {
            @Override
            public void run() {
                try {
                    final MethodCustomizer methodCustomizer = createDialog(fileObject, methodModel);
                    if (methodCustomizer.customizeMethod()) {
                        MethodModel method = methodCustomizer.getMethodModel();
                        boolean isOneReturn = methodCustomizer.finderReturnIsSingle();
                        boolean publishToLocal = methodCustomizer.publishToLocal();
                        boolean publishToRemote = methodCustomizer.publishToRemote();
                        String ejbql = methodCustomizer.getEjbQL();
                        generateMethod(method, isOneReturn, publishToLocal, publishToRemote, ejbql, fileObject, className);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, getTitle());
    }

    protected EjbJar getEjbModule(FileObject fileObject) {
        return org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
    }

    protected static MethodsNode getMethodsNode() {
        Node[] nodes = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]);
        if (nodes.length != 1) {
            return null;
        }
        return nodes[0].getLookup().lookup(MethodsNode.class);
    }

    /**
     * Gets whether the type of given {@code TypeElement} is Entity bean.
     * @param compilationController compilationController
     * @param typeElement examined element
     * @return {@code true} if the element is subtype of {@code jakarta.ejb.EntityBean} or {@code javax.ejb.EntityBean}, {@code false} otherwise
     */
    protected static boolean isEntity(CompilationController compilationController, TypeElement typeElement) {
        Parameters.notNull("compilationController", compilationController);
        Parameters.notNull("typeElement", typeElement);

        TypeElement entity = compilationController.getElements().getTypeElement("javax.ejb.EntityBean");
        TypeElement entityJakarta = compilationController.getElements().getTypeElement("jakarta.ejb.EntityBean");
        return (entity != null && (compilationController.getTypes().isSubtype(typeElement.asType(), entity.asType())))
                || (entityJakarta != null && (compilationController.getTypes().isSubtype(typeElement.asType(), entityJakarta.asType())));
    }

    /**
     * Gets whether the type of given {@code TypeElement} is Session bean.
     * @param compilationController compilationController
     * @param typeElement examined element
     * @return {@code true} if the element is subtype of {@code javax.ejb.Stateless},
     * {@code javax.ejb.Stateful} or {@code javax.ejb.Singleton}, {@code false} otherwise
     */
    protected static boolean isSession(CompilationController compilationController, TypeElement typeElement) {
        Parameters.notNull("compilationController", compilationController);
        Parameters.notNull("typeElement", typeElement);

        TypeElement stateless = compilationController.getElements().getTypeElement("javax.ejb.Stateless");
        TypeElement stateful = compilationController.getElements().getTypeElement("javax.ejb.Stateful");
        TypeElement singleton = compilationController.getElements().getTypeElement("javax.ejb.Singleton");
        TypeElement statelessJakarta = compilationController.getElements().getTypeElement("jakarta.ejb.Stateless");
        TypeElement statefulJakarta = compilationController.getElements().getTypeElement("jakarta.ejb.Stateful");
        TypeElement singletonJakarta = compilationController.getElements().getTypeElement("jakarta.ejb.Singleton");
        return (stateless != null && compilationController.getTypes().isSubtype(typeElement.asType(), stateless.asType()))
                || (stateful != null && compilationController.getTypes().isSubtype(typeElement.asType(), stateful.asType()))
                || (singleton != null && compilationController.getTypes().isSubtype(typeElement.asType(), singleton.asType()))
                || (statelessJakarta != null && compilationController.getTypes().isSubtype(typeElement.asType(), statelessJakarta.asType()))
                || (statefulJakarta != null && compilationController.getTypes().isSubtype(typeElement.asType(), statefulJakarta.asType()))
                || (singletonJakarta != null && compilationController.getTypes().isSubtype(typeElement.asType(), singletonJakarta.asType()));
    }

    /**
     * Gets whether the type of given {@code TypeElement} is Stateful Session bean.
     * @param compilationController compilationController
     * @param typeElement examined element
     * @return {@code true} if the element is subtype of {@code javax.ejb.Stateful}, {@code false} otherwise
     */
    protected static boolean isStateful(CompilationController compilationController, TypeElement typeElement) {
        Parameters.notNull("compilationController", compilationController);
        Parameters.notNull("typeElement", typeElement);

        TypeElement stateful = compilationController.getElements().getTypeElement("javax.ejb.Stateful");
        TypeElement statefulJakarta = compilationController.getElements().getTypeElement("jakarta.ejb.Stateful");
        return (stateful != null && compilationController.getTypes().isSubtype(typeElement.asType(), stateful.asType()))
                || (statefulJakarta != null && compilationController.getTypes().isSubtype(typeElement.asType(), statefulJakarta.asType()));
    }

}
