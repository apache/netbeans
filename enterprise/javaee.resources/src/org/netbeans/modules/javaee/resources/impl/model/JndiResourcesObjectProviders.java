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
package org.netbeans.modules.javaee.resources.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.javaee.resources.api.JndiResourcesDefinition;
import org.netbeans.modules.javaee.resources.api.model.Refreshable;

/**
 * Hold all JNDI resource object providers.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JndiResourcesObjectProviders {

    static final class JmsDestinationProvider extends AbstractProvider<JmsDestinationImpl> {

        JmsDestinationProvider(AnnotationModelHelper helper) {
            super(helper, JndiResourcesDefinition.ANN_JMS_DESTINATION, JndiResourcesDefinition.ANN_JMS_DESTINATION_JAKARTA);
        }

        @Override
        public JmsDestinationImpl createObject(AnnotationModelHelper helper, TypeElement typeElement) {
            return new JmsDestinationImpl(helper, typeElement);
        }
    }

    static final class JmsDestinationsProvider extends AbstractProvider<JmsDestinationsImpl> {

        public JmsDestinationsProvider(AnnotationModelHelper helper) {
            super(helper, JndiResourcesDefinition.ANN_JMS_DESTINATIONS, JndiResourcesDefinition.ANN_JMS_DESTINATIONS_JAKARTA);
        }

        @Override
        JmsDestinationsImpl createObject(AnnotationModelHelper helper, TypeElement typeElement) {
            return new JmsDestinationsImpl(helper, typeElement);
        }
    }

    private abstract static class AbstractProvider<T extends Refreshable> implements ObjectProvider<T> {

        private String[] annotationTypes;
        private AnnotationModelHelper helper;

        AbstractProvider(AnnotationModelHelper helper, String... annotationTypes) {
            this.annotationTypes = annotationTypes;
            this.helper = helper;
        }

        @Override
        public List<T> createInitialObjects() throws InterruptedException {
            final List<T> result = new LinkedList<T>();
            for (String annotationType : annotationTypes) {
                helper.getAnnotationScanner().findAnnotations(annotationType, AnnotationScanner.TYPE_KINDS,
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
            for (String annotationType : annotationTypes) {
                if (type.getKind() == ElementKind.CLASS || type.getKind() == ElementKind.INTERFACE || type.getKind() == ElementKind.ENUM) {
                    if (helper.hasAnyAnnotation(type.getAnnotationMirrors(), Collections.singleton(annotationType))) {
                        result.add(createObject(helper, type));
                    }
                }
            }
            return result;
        }

        @Override
        public boolean modifyObjects(TypeElement type, List<T> objects) {
            assert objects.size() == 1;
            T object = objects.get(0);
            assert object != null;
            if (!object.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }

        abstract T createObject(AnnotationModelHelper helper, TypeElement typeElement);

    }

}
