/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
