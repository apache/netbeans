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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public abstract class CommonAttributeImpl extends NamedImpl
    implements Attribute {
    
    /** 
     * Creates a new instance of CommonAttributeImpl 
     */
    public CommonAttributeImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }
 
    protected Class getAttributeType(SchemaAttributes attr) {
        switch(attr) {
            case FIXED:
                return String.class;
            default:
                return super.getAttributeType(attr);
        }
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
    public void setFixed(String fixedValue) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, fixedValue);
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
    public void setDefault(String defaultValue) {
        setAttribute(DEFAULT_PROPERTY, SchemaAttributes.DEFAULT, defaultValue);
    }
    
    /**
     *
     */
    public NamedComponentReference<GlobalSimpleType> getType() { 
        return resolveGlobalReference(GlobalSimpleType.class, SchemaAttributes.TYPE);
    }
    
    /**
     *
     */
    public void setType(NamedComponentReference<GlobalSimpleType> type) {
        setAttribute(TYPE_PROPERTY, SchemaAttributes.TYPE, type);
    }

    /**
     *
     */
    public LocalSimpleType getInlineType() {
        java.util.Collection<LocalSimpleType> types = getChildren(LocalSimpleType.class);        
        if(!types.isEmpty()){
            return types.iterator().next();
        }
        return null;
    }
    
    /**
     *
     */
    public void setInlineType(LocalSimpleType type) {
        List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(LocalSimpleType.class, INLINE_TYPE_PROPERTY, type, classes);
    }
 
}
