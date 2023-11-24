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

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;

/**
 * @author Petr Slechta
 */
public class AnnotationHelpers {

    private AnnotationModelHelper helper;
    private PersistentObjectManager<WebServlet> webServletsPOM;
    private PersistentObjectManager<WebFilter> webFiltersPOM;
    private PersistentObjectManager<SecurityRoles> securityRolesPOM;

    public AnnotationHelpers(AnnotationModelHelper helper) {
        this.helper = helper;
    }

    // -------------------------------------------------------------------------
    public AnnotationModelHelper getHelper() {
        return helper;
    }

    public PersistentObjectManager<WebServlet> getWebServletPOM() {
        if (webServletsPOM == null)
            webServletsPOM = helper.createPersistentObjectManager(new AnnotationProvider("jakarta.servlet.annotation.WebServlet", "javax.servlet.annotation.WebServlet") {
                @Override
                WebServlet newItem(AnnotationModelHelper helper, TypeElement typeElement) {
                    return new WebServlet(helper, typeElement);
                }
            });
        return webServletsPOM;
    }

    public PersistentObjectManager<WebFilter> getWebFilterPOM() {
        if (webFiltersPOM == null)
            webFiltersPOM = helper.createPersistentObjectManager(new AnnotationProvider("jakarta.servlet.annotation.WebFilter", "javax.servlet.annotation.WebFilter") {
                @Override
                WebFilter newItem(AnnotationModelHelper helper, TypeElement typeElement) {
                    return new WebFilter(helper, typeElement);
                }
            });
        return webFiltersPOM;
    }

    public PersistentObjectManager<SecurityRoles> getSecurityRolesPOM() {
        if (securityRolesPOM == null)
            securityRolesPOM = helper.createPersistentObjectManager(new AnnotationProvider("jakarta.annotation.security.DeclareRoles", "javax.annotation.security.DeclareRoles") {
                @Override
                SecurityRoles newItem(AnnotationModelHelper helper, TypeElement typeElement) {
                    return new SecurityRoles(helper, typeElement);
                }
            });
        return securityRolesPOM;
    }

    // -------------------------------------------------------------------------
    private abstract class AnnotationProvider<T extends Refreshable> implements ObjectProvider<T> {

        private String[] annotationNames;

        AnnotationProvider(String... annotationNames) {
            this.annotationNames = annotationNames;
        }

        public List<T> createInitialObjects() throws InterruptedException {
            final List<T> result = new ArrayList<T>();
            for (String annotationName : annotationNames) {
                helper.getAnnotationScanner().findAnnotations(annotationName, AnnotationScanner.TYPE_KINDS, new AnnotationHandler() {
                    public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                        result.add(newItem(helper, type));
                    }
                });
            }
            return result;
        }

        public List<T> createObjects(TypeElement type) {
            final List<T> result = new ArrayList<T>();
            for (String annotationName : annotationNames) {
                if (helper.hasAnnotation(type.getAnnotationMirrors(), annotationName)) {
                    result.add(newItem(helper, type));
                }
            }
            return result;
        }

        public boolean modifyObjects(TypeElement type, List<T> objects) {
            assert objects.size() == 1;
            T item = objects.get(0);
            if (!item.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }

        abstract T newItem(AnnotationModelHelper helper, TypeElement typeElement);
    }

}
