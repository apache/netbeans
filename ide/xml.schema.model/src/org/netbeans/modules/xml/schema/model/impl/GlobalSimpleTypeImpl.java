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

import java.util.Set;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType.Final;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class GlobalSimpleTypeImpl extends CommonSimpleTypeImpl implements GlobalSimpleType{

    /** Creates a new instance of GlobalSimpleTypeImpl */
    public GlobalSimpleTypeImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.SIMPLE_TYPE,model));
    }
    
    public GlobalSimpleTypeImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalSimpleType.class;
	}
    
    protected Class getAttributeMemberType(SchemaAttributes attribute) {
        switch(attribute) {
            case FINAL:
                return Final.class;
            default:
                return super.getAttributeType(attribute);
        }
    }
    
    //setters/getters of attributes
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, SchemaAttributes.NAME, name);
    }
    
    
    public String getName() {
        return getAttribute(SchemaAttributes.NAME);
    }
    
    @Override
    public String toString() {
        return getName();
    }
            
    public void setFinal(Set<Final> finalValue) {
        setAttribute(FINAL_PROPERTY, SchemaAttributes.FINAL, 
                finalValue == null ? null : 
                    Util.convertEnumSet(Final.class, finalValue));
    }
    
    public Set<Final> getFinal() {
        String s = getAttribute(SchemaAttributes.FINAL);
        return s == null ? null : Util.valuesOf(Final.class, s);
    }
 
    public Set<Final> getFinalEffective() {
        Set<Final> v = getFinal();
        return v == null ? getFinalDefault() : v;
    }

    public Set<Final> getFinalDefault() {
        return Util.convertEnumSet(Final.class, getModel().getSchema().getFinalDefaultEffective());
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
