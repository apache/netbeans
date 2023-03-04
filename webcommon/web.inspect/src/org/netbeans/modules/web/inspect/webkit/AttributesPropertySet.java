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
package org.netbeans.modules.web.inspect.webkit;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 * Attributes property set of {@code DOMNode}.
 *
 * @author Jan Stola
 */
class AttributesPropertySet extends SortedPropertySet<AttributesPropertySet.AttributeProperty> {
    /** Name of attributes property set. */
    private static final String NAME = "attributes"; // NOI18N
    /** The owner of this property set. */
    private final DOMNode node;

    /**
     * Creates a new {@code AttributesPropertySet}.
     * 
     * @param node owner of the property set.
     */
    AttributesPropertySet(DOMNode node) {
        super(NAME, NbBundle.getMessage(AttributesPropertySet.class, "AttributesPropertySet.name"), null); // NOI18N
        setValue("tabName", NbBundle.getMessage(AttributesPropertySet.class, "AttributesPropertySet.tabName")); // NOI18N
        this.node = node;
    }

    @Override
    void update() {
        Node n = node.getNode();
        // Remove properties corresponding to removed attributes
        for (org.openide.nodes.Node.Property property : getProperties()) {
            Node.Attribute attribute = n.getAttribute(property.getName());
            if (attribute == null) {
                removeProperty((AttributeProperty)property);
            }
        }
        // Add properties corresponding to added attributes
        for (Node.Attribute attr : n.getAttributes()) {
            String name = attr.getName();
            AttributeProperty property = getProperty(name);
            if (property == null) {
                addProperty(new AttributeProperty(attr));
            }
        }
    }

    /**
     * Property representing one attribute.
     */
    static class AttributeProperty extends PropertySupport.ReadOnly<String> {
        /** Attribute represented by this property. */
        private final Node.Attribute attribute;

        /**
         * Creates a new {@code AttributeProperty}.
         * 
         * @param attribute attribute represented by the property.
         */
        AttributeProperty(Node.Attribute attribute) {
            super(attribute.getName(), String.class, attribute.getName(), null);
            this.attribute = attribute;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return attribute.getValue();
        }
        
    }
    
}
