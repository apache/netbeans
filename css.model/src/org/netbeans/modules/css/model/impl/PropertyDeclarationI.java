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
