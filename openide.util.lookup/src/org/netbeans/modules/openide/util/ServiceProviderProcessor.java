/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.util.lookup.implspi.AbstractServiceProviderProcessor;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ServiceProviderProcessor extends AbstractServiceProviderProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            ServiceProvider.class.getCanonicalName(),
            ServiceProviders.class.getCanonicalName()
        ));
    }

    /** public for ServiceLoader */
    public ServiceProviderProcessor() {}

    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element el : roundEnv.getElementsAnnotatedWith(ServiceProvider.class)) {
            ServiceProvider sp = el.getAnnotation(ServiceProvider.class);
            if (sp == null) {
                continue;
            }
            register(el, ServiceProvider.class, sp);
        }
        for (Element el : roundEnv.getElementsAnnotatedWith(ServiceProviders.class)) {
            ServiceProviders spp = el.getAnnotation(ServiceProviders.class);
            if (spp == null) {
                continue;
            }
            for (ServiceProvider sp : spp.value()) {
                register(el, ServiceProviders.class, sp);
            }
        }
        return true;
    }

    private void register(Element clazz, Class<? extends Annotation> annotation, ServiceProvider svc) {
        try {
            svc.service();
            assert false;
            return;
        } catch (MirroredTypeException e) {
            register(clazz, annotation, e.getTypeMirror(), svc.path(), svc.position(), svc.supersedes());
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element annotated, AnnotationMirror annotation, ExecutableElement attr, String userText) {
        if (processingEnv == null || annotated == null || !annotated.getKind().isClass()) {
            return Collections.emptyList();
        }

        if (   annotation == null
            || !"org.openide.util.lookup.ServiceProvider".contentEquals(((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName())) {
            return Collections.emptyList();
        }

        if (!"service".contentEquals(attr.getSimpleName())) {
            return Collections.emptyList();
        }

        TypeElement jlObject = processingEnv.getElementUtils().getTypeElement("java.lang.Object");

        if (jlObject == null) {
            return Collections.emptyList();
        }
        
        Collection<Completion> result = new LinkedList<Completion>();
        List<TypeElement> toProcess = new LinkedList<TypeElement>();

        toProcess.add((TypeElement) annotated);

        while (!toProcess.isEmpty()) {
            TypeElement c = toProcess.remove(0);

            result.add(new TypeCompletion(c.getQualifiedName().toString() + ".class"));

            List<TypeMirror> parents = new LinkedList<TypeMirror>();

            parents.add(c.getSuperclass());
            parents.addAll(c.getInterfaces());

            for (TypeMirror tm : parents) {
                if (tm == null || tm.getKind() != TypeKind.DECLARED) {
                    continue;
                }

                TypeElement type = (TypeElement) processingEnv.getTypeUtils().asElement(tm);

                if (!jlObject.equals(type)) {
                    toProcess.add(type);
                }
            }
        }

        return result;
    }

    private static final class TypeCompletion implements Completion {

        private final String type;

        public TypeCompletion(String type) {
            this.type = type;
        }

        public String getValue() {
            return type;
        }

        public String getMessage() {
            return null;
        }
        
    }

}
