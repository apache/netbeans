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
package org.netbeans.modules.css.model.impl;

import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.model.api.Expression;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Prio;
import org.netbeans.modules.css.model.api.Property;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.PropertyValue;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class PropertyDeclarationI extends ModelElement implements PropertyDeclaration {

    private Property property;
    private PropertyValue propertyValue;
    private Prio prio;
    private ResolvedProperty resolvedProperty;
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(PropertyValue value) {
            propertyValue = value;
        }

        @Override
        public void elementAdded(Property value) {
            property = value;
        }

        @Override
        public void elementAdded(Prio value) {
            prio = value;
        }
    };

    public PropertyDeclarationI(Model model) {
        super(model);
         
        //default elements
        addTextElement(getIndent()); //not acc. to the grammar!

        addEmptyElement(Property.class);
        addTextElement(":");
        addTextElement(" ");
        addEmptyElement(PropertyValue.class);
        addEmptyElement(Prio.class);
    }

    public PropertyDeclarationI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public Property getProperty() {
        return property;
    }

    @Override
    public void setProperty(Property property) {
        setElement(property);
    }

    @Override
    public PropertyValue getPropertyValue() {
        return propertyValue;
    }

    @Override
    public void setPropertyValue(PropertyValue value) {
        setElement(value);
    }

    @Override
    public Prio getPrio() {
        return prio;
    }

    @Override
    public void setPrio(Prio prio) {
        setElement(prio);
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    protected Class getModelClass() {
        return PropertyDeclaration.class;
    }

    @Override
    public synchronized ResolvedProperty getResolvedProperty() {
        FileObject file = getModel().getLookup().lookup(FileObject.class);
        if (resolvedProperty == null) {
            PropertyDefinition pmodel = Properties.getPropertyDefinition(getProperty().getContent().toString().trim());
            if (pmodel != null) {
                Expression expression = getPropertyValue().getExpression();
                CharSequence content = expression != null ? expression.getContent() : "";
                resolvedProperty = ResolvedProperty.resolve(file, pmodel, content);
            }
        }
        return resolvedProperty;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        b.append("(");
        Property p = getProperty();
        b.append(p == null ? "null" : p.getContent());
        PropertyValue pv = getPropertyValue();
        b.append(":");
        Expression e = pv == null ? null : pv.getExpression();
        b.append(e == null ? "null" : e.getContent());
        b.append(getPrio() == null ? "" : "!");
        b.append(")");

        return b.toString();
    }
}
