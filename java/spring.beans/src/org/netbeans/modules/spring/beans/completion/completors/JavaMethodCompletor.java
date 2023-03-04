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
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.CompletorUtils;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.Public;
import org.netbeans.modules.spring.java.Static;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class JavaMethodCompletor extends Completor {

    public JavaMethodCompletor(int invocationOffset) {
        super(invocationOffset);
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        return context.getCurrentTokenOffset() + 1;
    }

    @Override
    protected void compute(final CompletionContext context) throws IOException {
        final String classBinaryName = getTypeName(context);
        final Public publicFlag = getPublicFlag(context);
        final Static staticFlag = getStaticFlag(context);
        final int argCount = getArgCount(context);

        if (classBinaryName == null || classBinaryName.equals("")) {
            return;
        }

        final JavaSource javaSource = JavaUtils.getJavaSource(context.getFileObject());
        if (javaSource == null) {
            return;
        }

        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController controller) throws Exception {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                if (classBinaryName == null) {
                    return;
                }
                TypeElement classElem = JavaUtils.findClassElementByBinaryName(classBinaryName, controller);
                if (classElem == null) {
                    return;
                }

                ElementUtilities eu = controller.getElementUtilities();
                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {

                    public boolean accept(Element e, TypeMirror type) {
                        // XXX : display methods of java.lang.Object? 
                        // Displaying them adds unnecessary clutter in the completion window
                        if (e.getKind() == ElementKind.METHOD) {
                            TypeElement te = (TypeElement) e.getEnclosingElement();
                            if (te.getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
                                return false;
                            }

                            // match name
                            if (!e.getSimpleName().toString().startsWith(context.getTypedPrefix())) {
                                return false;
                            }

                            ExecutableElement method = (ExecutableElement) e;
                            // match argument count
                            if (argCount != -1 && method.getParameters().size() != argCount) {
                                return false;
                            }

                            // match static
                            if (staticFlag != Static.DONT_CARE) {
                                boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
                                if ((isStatic && staticFlag == Static.NO) || (!isStatic && staticFlag == Static.YES)) {
                                    return false;
                                }
                            }

                            // match public
                            if (publicFlag != Public.DONT_CARE) {
                                boolean isPublic = method.getModifiers().contains(Modifier.PUBLIC);
                                if ((isPublic && publicFlag == Public.NO) || (!isPublic && publicFlag == Public.YES)) {
                                    return false;
                                }
                            }

                            return true;
                        }

                        return false;
                    }
                };

                Iterable<? extends Element> methods = eu.getMembers(classElem.asType(), acceptor);

                methods = filter(methods);

                for (Element e : methods) {
                    SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createMethodItem(
                            getAnchorOffset(), (ExecutableElement) e, e.getEnclosingElement() != classElem,
                            controller.getElements().isDeprecated(e));
                    addCacheItem(item);
                }
            }
        }, false);
    }

    @Override
    public boolean canFilter(CompletionContext context) {
        return CompletorUtils.canFilter(context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset(), CompletorUtils.JAVA_IDENTIFIER_ACCEPTOR);
    }
    
    @Override
    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        return CompletorUtils.filter(getCacheItems(), context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset());
    }

    /**
     * Should the method be public
     */
    protected abstract Public getPublicFlag(CompletionContext context);

    /**
     * Should the method be static
     */
    protected abstract Static getStaticFlag(CompletionContext context);

    /**
     * Number of arguments of the method
     */
    protected abstract int getArgCount(CompletionContext context);

    /**
     * Binary name of the class which should be searched for methods
     */
    protected abstract String getTypeName(CompletionContext context);

    /**
     * Post process applicable methods, for eg. return only those
     * methods which return do not return void
     */
    protected Iterable<? extends Element> filter(Iterable<? extends Element> methods) {
        return methods;
    }
}
