/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                if (ElementKind.FIELD.equals(elementKind)) {
                    handleProperty(element);
                }
            } else {
                if (ElementKind.METHOD.equals(elementKind)) {
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
        if (ElementKind.METHOD.equals(element.getKind())) {
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
