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

package org.netbeans.modules.websvc.rest.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.model.api.RestApplications;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;

/**
 *
 * @author mkuchtiak
 */
public class RestApplicationsImpl implements RestApplications {
    private Project project;
    private AnnotationModelHelper helper;
    private volatile PersistentObjectManager<RestApplicationImpl> restApplicationManager;

    public static RestApplicationsImpl create(AnnotationModelHelper helper, Project project) {
        RestApplicationsImpl instance =  new RestApplicationsImpl(helper, project);
        instance.initialize();
        return instance;

    }

    private void initialize() {
        restApplicationManager = helper.createPersistentObjectManager(new RestApplicationsProvider());
    }

    private RestApplicationsImpl(AnnotationModelHelper helper, Project project) {
        this.project = project;
        this.helper = helper;
    }

    public List<RestApplication> getRestApplications() {
        return new ArrayList<RestApplication>(restApplicationManager.getObjects());
    }

    private final class RestApplicationsProvider implements ObjectProvider<RestApplicationImpl> {


        public List<RestApplicationImpl> createInitialObjects() throws InterruptedException {
            final Map<TypeElement, RestApplicationImpl> result =
                    new HashMap<TypeElement, RestApplicationImpl>();
            findAnnotations(RestConstants.APPLICATION_PATH_JAKARTA, EnumSet.of(ElementKind.CLASS), result);
            findAnnotations(RestConstants.APPLICATION_PATH, EnumSet.of(ElementKind.CLASS), result);
            return new ArrayList<RestApplicationImpl>(result.values());
        }

        public List<RestApplicationImpl> createObjects(TypeElement type) {
            if (Utils.isRestApplication(type, helper)) {
                return Collections.singletonList(new RestApplicationImpl(helper, type));
            }
            return Collections.emptyList();
        }

        public boolean modifyObjects(TypeElement type, List<RestApplicationImpl> objects) {
            assert objects.size() == 1;
            if ( !objects.get(0).refresh(type)){
                objects.remove(0);
                return true;
            }
            return false;
        }
        
        private void findAnnotations (
                String annotationType, EnumSet<ElementKind> kinds,
                final Map<TypeElement, RestApplicationImpl> result) throws InterruptedException {

            helper.getAnnotationScanner().findAnnotations(annotationType, kinds,
                    new AnnotationHandler() {
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    if (!result.containsKey(type)) {
                        result.put(type, new RestApplicationImpl(helper, type));
                    }
                }
            });
        }

    }

}
