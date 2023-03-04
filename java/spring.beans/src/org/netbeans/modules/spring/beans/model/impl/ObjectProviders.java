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

    private abstract static class AbstractProvider<T extends Refreshable> implements ObjectProvider<T> {

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
