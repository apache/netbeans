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

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 * @author Chris Webster
 */
public abstract class LocalAttributeBaseImpl extends CommonAttributeImpl {
    
    /**
     *
     */
    public LocalAttributeBaseImpl(SchemaModelImpl model) {
        super(model,createNewComponent(SchemaElements.ATTRIBUTE, model));
    }
    
    /**
     *
     */
    public LocalAttributeBaseImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

    /**
     *
     */
    public LocalAttribute.Use getUse() {
        String s = getAttribute(SchemaAttributes.USE);
        return s == null ? null : Util.parse(Use.class, s);
    }
    
    public Use getUseEffective() {
        Use v = getUse();
        return v == null ? getUseDefault() : v;
    }

    public Use getUseDefault() {
        return Use.OPTIONAL;
    }

    /**
     *
     */
    public void setUse(LocalAttribute.Use use) {
        setAttribute(LocalAttribute.USE_PROPERTY, SchemaAttributes.USE, use);
    }

    /**
     *
     */
    public NamedComponentReference<GlobalAttribute> getRef() {
        return resolveGlobalReference(GlobalAttribute.class, SchemaAttributes.REF);
    }
    
    /**
     *
     */
    public void setRef(NamedComponentReference<GlobalAttribute> attribute) {
        setAttribute(LocalAttribute.REF_PROPERTY, SchemaAttributes.REF, attribute);
    }

    /**
     *
     */
    public Form getForm() {
        String s = getAttribute(SchemaAttributes.FORM);
        return s == null ? null : Util.parse(Form.class, s);
    }

    public Form getFormEffective() {
        Form v = getForm();
        return v == null ? getFormDefault() : v;
    }

    public Form getFormDefault() {
        return getModel().getSchema().getAttributeFormDefaultEffective();
    }

    /**
     *
     */
    public void setForm(Form form) {
        setAttribute(LocalAttribute.FORM_PROPERTY, SchemaAttributes.FORM, form);
    }
    
}
