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

package org.netbeans.modules.form.codestructure;

import java.beans.PropertyEditor;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;

/**
 * @author Tomas Pavek
 */

public class FormCodeSupport {

    public static CodeExpressionOrigin createOrigin(Node.Property property) {
        if (property instanceof FormProperty)
            return new FormPropertyValueOrigin((FormProperty)property);
        else
            return new PropertyValueOrigin(property);
    }

    public static CodeExpressionOrigin createOrigin(Class type,
                                                    PropertyEditor prEd)
    {
        return new PropertyEditorOrigin(type, prEd);
    }

    public static CodeExpressionOrigin createOrigin(RADComponent component) {
        return new RADComponentOrigin(component);
    }

    public static void readPropertyExpression(CodeExpression expression,
                                              Node.Property property,
                                              boolean allowChangeFiring)
    {
        FormProperty fProperty = property instanceof FormProperty ?
                                 (FormProperty) property : null;

        if (fProperty != null) {
            if (!allowChangeFiring) {
                if (fProperty.isChangeFiring())
                    fProperty.setChangeFiring(false);
                else
                    allowChangeFiring = true; // just not to set firing back
            }

            Object metaOrigin = expression.getOrigin().getMetaObject();
            if (metaOrigin instanceof PropertyEditor)
                fProperty.setCurrentEditor((PropertyEditor)metaOrigin);
        }

        try {
            property.setValue(expression.getOrigin().getValue());
        }
        catch (Exception ex) { // ignore
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        expression.setOrigin(createOrigin(property));

        if (fProperty != null && !allowChangeFiring)
            fProperty.setChangeFiring(true);
    }

    public static void readPropertyStatement(CodeStatement statement,
                                             Node.Property property,
                                             boolean allowChangeFiring)
    {
        // expecting statement with one expression parameter
        CodeExpression[] params = statement.getStatementParameters();
        if (params.length != 1)
            throw new IllegalArgumentException();

        readPropertyExpression(params[0], property, allowChangeFiring);
    }

    // --------

    static final class PropertyValueOrigin implements CodeExpressionOrigin {
        private Node.Property property;

        public PropertyValueOrigin(Node.Property property) {
            this.property = property;
        }

        @Override
        public Class getType() {
            return property.getValueType();
        }

        @Override
        public CodeExpression getParentExpression() {
            return null;
        }

        @Override
        public Object getValue() {
            try {
                return property.getValue();
            }
            catch (Exception ex) {} // should no happen

            return null;
        }

        @Override
        public Object getMetaObject() {
            return property;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            try {
                PropertyEditor pred = property.getPropertyEditor();
                pred.setValue(property.getValue());
                return pred.getJavaInitializationString();
            }
            catch (Exception ex) {} // should not happen
            return null;
        }

        @Override
        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }
    }

    static final class FormPropertyValueOrigin implements CodeExpressionOrigin {
        private FormProperty property;

        public FormPropertyValueOrigin(FormProperty property) {
            this.property = property;
        }

        @Override
        public Class getType() {
            return property.getValueType();
        }

        @Override
        public CodeExpression getParentExpression() {
            return null;
        }

        @Override
        public Object getValue() {
            try {
                return property.getRealValue();
                // [or getValue() ??]
            }
            catch (Exception ex) {} // should no happen

            return null;
        }

        @Override
        public Object getMetaObject() {
            return property;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            return property.getJavaInitializationString();
        }

        @Override
        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }
    }

    static final class PropertyEditorOrigin implements CodeExpressionOrigin {
        private Class type;
        private PropertyEditor propertyEditor;

        public PropertyEditorOrigin(Class type, PropertyEditor prEd) {
            this.type = type;
            this.propertyEditor = prEd;
        }

        @Override
        public Class getType() {
            return type;
        }

        @Override
        public CodeExpression getParentExpression() {
            return null;
        }

        @Override
        public Object getValue() {
            return propertyEditor.getValue();
        }

        @Override
        public Object getMetaObject() {
            return propertyEditor;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            return propertyEditor.getJavaInitializationString();
        }

        @Override
        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }
    }

    static final class RADComponentOrigin implements CodeExpressionOrigin {
        private RADComponent component;

        public RADComponentOrigin(RADComponent component) {
            this.component = component;
        }

        @Override
        public Class getType() {
            return component.getBeanClass();
        }

        @Override
        public CodeExpression getParentExpression() {
            return null;
        }

        @Override
        public Object getMetaObject() {
            return component;
        }

        @Override
        public Object getValue() {
            return component.getBeanInstance();
        }

        @Override
        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            if (component == component.getFormModel().getTopRADComponent())
                return "this"; // NOI18N

            StringBuilder buf = new StringBuilder();

            buf.append("new "); // NOI18N
            buf.append(component.getBeanClass().getName().replace('&','.')); // NOI18N
            buf.append("()"); // NOI18N

            return buf.toString();
        }
    }
}
