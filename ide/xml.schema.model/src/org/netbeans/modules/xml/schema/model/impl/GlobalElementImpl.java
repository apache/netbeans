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
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalElement.Final;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 */
public class GlobalElementImpl extends ElementImpl implements GlobalElement {

    public GlobalElementImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.ELEMENT,model));
    }
    
    /**
     * Creates a new instance of GlobalElementImpl
     */
    public GlobalElementImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalElement.class;
	}
    
    /**
     *
     */
    public void setSubstitutionGroup(NamedComponentReference<GlobalElement> element) {
	setAttribute(SUBSTITUTION_GROUP_PROPERTY, SchemaAttributes.SUBSTITUTION_GROUP, element);
    }
    
    /**
     *
     */
    public NamedComponentReference<GlobalElement> getSubstitutionGroup() {
        return resolveGlobalReference(GlobalElement.class, SchemaAttributes.SUBSTITUTION_GROUP);
    }
    
    /**
     *
     */
    public void setAbstract(Boolean abstr) {
        setAttribute(ABSTRACT_PROPERTY, SchemaAttributes.ABSTRACT, abstr);
    }
    
    /**
     *
     */
    public Boolean isAbstract() {
        String s = getAttribute(SchemaAttributes.ABSTRACT);
        return s == null ? null : Boolean.valueOf(s);
    }
    

    public boolean getAbstractEffective() {
        Boolean v = isAbstract();
        return v == null ? getAbstractDefault() : v;
    }

    public boolean getAbstractDefault() {
        return false;
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
        return Util.convertEnumSet(Final.class, getModel().getSchema().getFinalDefaultDefault());
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
 
    protected Class getAttributeMemberType(SchemaAttributes attr) {
        switch(attr) {
            case FINAL:
                return Final.class;
            default:
                return super.getAttributeMemberType(attr);
        }
    }
}
