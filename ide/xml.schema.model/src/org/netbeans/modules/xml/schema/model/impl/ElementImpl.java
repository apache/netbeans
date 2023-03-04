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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.Element.Block;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
/**
 *
 * @author Vidhya Narayanan
 */
public abstract class ElementImpl extends NamedImpl implements Element {
    
    /**
     * Creates a new instance of CommonElementImpl
     */
    public ElementImpl(SchemaModelImpl model, org.w3c.dom.Element el) {
        super(model, el);
    }
    
    protected Class getAttributeMemberType(SchemaAttributes attr) {
        switch(attr) {
            case BLOCK:
                return Block.class;
            default:
                return super.getAttributeMemberType(attr);
        }
    }
    
    public void setDefault(String defaultValue) {
        setAttribute(DEFAULT_PROPERTY ,SchemaAttributes.DEFAULT, defaultValue);
    }
    
    public void setFixed(String fixed) {
        setAttribute(FIXED_PROPERTY ,SchemaAttributes.FIXED, fixed);
    }
    
    public void setType(NamedComponentReference<? extends GlobalType> t) {
        setAttribute(LocalElement.TYPE_PROPERTY, SchemaAttributes.TYPE, t);
    }
    
    public void setNillable(Boolean nillable) {
        setAttribute(NILLABLE_PROPERTY, SchemaAttributes.NILLABLE, nillable);
    }
    
    /**
     *
     */
    public void addConstraint(Constraint c) {
        Collection<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
	list.add(Annotation.class);
	list.add(LocalType.class);
        addAfter(CONSTRAINT_PROPERTY, (SchemaComponent) c, list);
    }
    
    /**
     *
     */
    public void removeConstraint(Constraint c) {
        removeChild(CONSTRAINT_PROPERTY , (SchemaComponent) c);
    }
    
    /**
     *
     */
    public void setInlineType(LocalType t) {
        Collection<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        setChild(LocalType.class, LocalElement.INLINE_TYPE_PROPERTY, t, list);
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

    public Set<Block> getBlockEffective() {
        Set<Block> v = getBlock();
        return v == null ? getBlockDefault() : v;
    }

    public Set<Block> getBlockDefault() {
        Set<Schema.Block> v = getModel().getSchema().getBlockDefaultEffective();
        return Util.convertEnumSet(Block.class, v);
    }

    /**
     *
     */
    public Collection<Constraint> getConstraints() {
        return getChildren(Constraint.class);
    }
    
    /**
     *
     */
    public String getDefault() {
        return getAttribute(SchemaAttributes.DEFAULT);
    }
    
    /**
     *
     */
    public String getFixed() {
        return getAttribute(SchemaAttributes.FIXED);
    }
    
    /**
     *
     */
    public LocalType getInlineType() {
        Collection<LocalType> elements = getChildren(LocalType.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    /**
     *
     */
    public  NamedComponentReference<? extends GlobalType> getType() {
       return resolveGlobalReference(GlobalType.class, SchemaAttributes.TYPE);
    }
    
    /**
     *
     */
    public Boolean isNillable() {
        String s = getAttribute(SchemaAttributes.NILLABLE);
        return s == null ? null : Boolean.parseBoolean(s);
    }

    public boolean getNillableDefault() {
        return false;
    }

    public boolean getNillableEffective() {
        Boolean v = isNillable();
        return v == null ? getNillableDefault() : v;
    }
    
}
