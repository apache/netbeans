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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
