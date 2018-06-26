/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
            super(helper, JndiResourcesDefinition.ANN_JMS_DESTINATION);
        }

        @Override
        public JmsDestinationImpl createObject(AnnotationModelHelper helper, TypeElement typeElement) {
            return new JmsDestinationImpl(helper, typeElement);
        }
    }

    static final class JmsDestinationsProvider extends AbstractProvider<JmsDestinationsImpl> {

        public JmsDestinationsProvider(AnnotationModelHelper helper) {
            super(helper, JndiResourcesDefinition.ANN_JMS_DESTINATIONS);
        }

        @Override
        JmsDestinationsImpl createObject(AnnotationModelHelper helper, TypeElement typeElement) {
            return new JmsDestinationsImpl(helper, typeElement);
        }
    }

    private abstract static class AbstractProvider<T extends Refreshable> implements ObjectProvider<T> {

        private String annotationType;
        private AnnotationModelHelper helper;

        AbstractProvider(AnnotationModelHelper helper, String annotationType) {
            this.annotationType = annotationType;
            this.helper = helper;
        }

        @Override
        public List<T> createInitialObjects() throws InterruptedException {
            final List<T> result = new LinkedList<T>();
            helper.getAnnotationScanner().findAnnotations(annotationType, AnnotationScanner.TYPE_KINDS,
                    new AnnotationHandler() {
                        @Override
                        public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                            result.add(createObject(helper, type));
                        }
                    });
            return result;
        }

        @Override
        public List<T> createObjects(TypeElement type) {
            final List<T> result = new ArrayList<T>();
            if (type.getKind() == ElementKind.CLASS || type.getKind() == ElementKind.INTERFACE || type.getKind() == ElementKind.ENUM) {
                if (helper.hasAnyAnnotation(type.getAnnotationMirrors(), Collections.singleton(annotationType))) {
                    result.add(createObject(helper, type));
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
