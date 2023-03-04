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
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalComplexType.Block;
import org.netbeans.modules.xml.schema.model.GlobalComplexType.Final;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;


/**
 *
 * @author rico
 */
public class GlobalComplexTypeImpl extends CommonComplexTypeImpl implements GlobalComplexType{
    
    /** Creates a new instance of GlobalComplexTypeImpl */
    public GlobalComplexTypeImpl(SchemaModelImpl model) {
        super(model,createNewComponent(SchemaElements.COMPLEX_TYPE, model));
    }
    
    public GlobalComplexTypeImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType()
	{
		return GlobalComplexType.class;
	}
    
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
    
    public void setBlock(Set<Block> block) {
        setAttribute(BLOCK_PROPERTY, SchemaAttributes.BLOCK,
                block == null ? null : 
                    Util.convertEnumSet(Block.class, block));
    }
    
    public Set<Block> getBlock() {
        String s = getAttribute(SchemaAttributes.BLOCK);
        return s == null ? null : Util.valuesOf(Block.class, s);
    }
    
    public Set<Final> getFinalEffective() {
        Set<Final> v = getFinal();
        return v == null ? getFinalDefault() : v;
    }

    public Set<Final> getFinalDefault() {
        return Util.convertEnumSet(Final.class, getModel().getSchema().getFinalDefaultEffective());
    }

    public Set<Block> getBlockEffective() {
        Set<Block> v = getBlock();
        return v == null ? getBlockDefault() : v;
    }

    public Set<Block> getBlockDefault() {
        return Util.convertEnumSet(Block.class, getModel().getSchema().getBlockDefaultEffective());
    }

    public void setAbstract(Boolean isAbstract) {
        setAttribute(ABSTRACT_PROPERTY, SchemaAttributes.ABSTRACT, isAbstract);
    }
    
    public Boolean isAbstract() {
        String s = getAttribute(SchemaAttributes.ABSTRACT);
        return s == null ? null : Boolean.valueOf(s);
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    protected Class getAttributeMemberType(SchemaAttributes attr) {
        switch(attr) {
            case FINAL:
                return Final.class;
            case BLOCK:
                return Block.class;
            default:
                return super.getAttributeMemberType(attr);
        }
    }

    public boolean getEffectiveAbstract() {
        Boolean v = isAbstract();
        return v == null ? getAbstractDefault() : v;
    }

    public boolean getAbstractDefault() {
        return false;
    }

    public boolean getAbstractEffective() {
        Boolean v = isAbstract();
        return v == null ? getAbstractDefault() : v;
    }
}
