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
package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.lang.String;
import java.lang.String;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;

/**
 *
 * @author Andrei Badea
 */
public final class AttributesHelper {

    private final TypeElement typeElement;
    private final AnnotationModelHelper helper;
    private final PropertyHandler propertyHandler;
    private final boolean fieldAccess;

    public AttributesHelper(AnnotationModelHelper helper, TypeElement typeElement, PropertyHandler propertyHandler) {
        this.helper = helper;
        this.typeElement = typeElement;
        this.propertyHandler = propertyHandler;
        if (typeElement == null) {
            fieldAccess = true;
            return;
        }
        List<? extends Element> elements = typeElement.getEnclosedElements();
        fieldAccess = EntityMappingsUtilities.hasFieldAccess(helper, elements);
    }

    public void parse() {
        if (typeElement == null) {
            return;
        }
        for (Element element : typeElement.getEnclosedElements()) {
            ElementKind elementKind = element.getKind();
            if (fieldAccess) {
                if (ElementKind.FIELD == elementKind) {
                    handleProperty(element);
                }
            } else {
                if (ElementKind.METHOD == elementKind) {
                    handleProperty(element);
                }
            }
        }
    }

    private void handleProperty(Element element) {
        if (element.getModifiers().contains(Modifier.STATIC)) {
            return;
        }
        String propertyName = element.getSimpleName().toString();
        if (ElementKind.METHOD == element.getKind()) {
            propertyName = EntityMappingsUtilities.getterNameToPropertyName(propertyName);
            if (propertyName == null) {
                return;
            }
            ExecutableElement el = (ExecutableElement) element;
            if(el.getParameters()!=null && el.getParameters().size()>0)return; //getter shouldn't have any parameters, if there are any it's not a property
        }
        propertyHandler.handleProperty(element, propertyName);
    }

    public boolean hasFieldAccess() {
        return fieldAccess;
    }

    public interface PropertyHandler {
        /**
         * Handle the given property (either a <code>VariableElement</code> for the
         * property field or an <code>ExecutableElement</code> for the property getter method.
         *
         * @param element never null.
         * @param propertyName never null.
         */
        void handleProperty(Element element, String propertyName);
    }
}
