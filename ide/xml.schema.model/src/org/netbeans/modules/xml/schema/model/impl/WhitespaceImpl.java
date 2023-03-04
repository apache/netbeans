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

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Whitespace;
import org.netbeans.modules.xml.schema.model.Whitespace.Treatment;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public class WhitespaceImpl extends SchemaComponentImpl implements Whitespace{

    public WhitespaceImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.WHITESPACE,model));
    }

    /** Creates a new instance of WhitespaceImpl */
    public WhitespaceImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Whitespace.class;
	}
    
    public Boolean isFixed() {
        String v = getAttribute(SchemaAttributes.FIXED);
        return v == null ? null : Boolean.valueOf(v);
    }
    
    public void setFixed(Boolean isFixed) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, isFixed);
    }
    
    public boolean getFixedEffective() {
        Boolean v = isFixed();
        return v == null ? getFixedDefault() : v;
    }

    public boolean getFixedDefault() {
        return false;
    }
    
    public Treatment getValue() {
        String s = this.getAttribute(SchemaAttributes.VALUE);
        return s == null ? null : Util.parse(Treatment.class, s);
    }
    
    public void setValue(Treatment whitespaceTreatment) {
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, whitespaceTreatment);
    }

    /**
     * Visitor providing
     */
    public void accept(org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor visitor) {
        visitor.visit(this);
    }

    protected Class getAttributeType(SchemaAttributes attr) {
        switch(attr) {
            case VALUE:
                return Treatment.class;
            default:
                return super.getAttributeType(attr);
        }
    }

}
