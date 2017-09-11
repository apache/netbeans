/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.beans.model.impl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.spring.api.beans.SpringAnnotations;

class ObjectProviders {

    static final class SpringBeanProvider extends AbstractProvider<SpringBeanImpl> implements ObjectProvider<SpringBeanImpl> {

        SpringBeanProvider(AnnotationModelHelper helper) {
            super(helper, SpringAnnotations.SPRING_COMPONENTS);
        }

        @Override
        public SpringBeanImpl createObject(AnnotationModelHelper helper, TypeElement typeElement) {
            return new SpringBeanImpl(helper, typeElement);
        }
    }

    private static abstract class AbstractProvider<T extends Refreshable> implements ObjectProvider<T> {

        private Set<String> annotationTypeNames;
        private AnnotationModelHelper helper;

        AbstractProvider(AnnotationModelHelper helper, Set<String> annotationTypeNames) {
            this.annotationTypeNames = annotationTypeNames;
            this.helper = helper;
        }

        @Override
        public List<T> createInitialObjects() throws InterruptedException {
            final List<T> result = new LinkedList<T>();
            for (String annotationName : getAnnotationTypeNames()) {
                helper.getAnnotationScanner().findAnnotations(annotationName, EnumSet.of(ElementKind.CLASS),
                        new AnnotationHandler() {

                            @Override
                            public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                                result.add(createObject(helper, type));
                            }
                        });
            }
            return result;
        }

        @Override
        public List<T> createObjects(TypeElement type) {
            final List<T> result = new ArrayList<T>();
            if (type.getKind() == ElementKind.CLASS || type.getKind() == ElementKind.INTERFACE) {
                if (helper.hasAnyAnnotation(type.getAnnotationMirrors(), getAnnotationTypeNames())) {
                    result.add(createObject(helper, type));
                }
            }
            return result;
        }

        @Override
        public boolean modifyObjects(TypeElement type, List<T> objects) {
            boolean isModified = false;
            for (Iterator<T> it = objects.iterator(); it.hasNext();) {
                T object = it.next();
                if (!object.refresh(type)) {
                    it.remove();
                    isModified = true;
                }
            }
            return isModified;
        }

        abstract T createObject(AnnotationModelHelper helper, TypeElement typeElement);

        private Set<String> getAnnotationTypeNames() {
            return annotationTypeNames;
        }
    }
}
