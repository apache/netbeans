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
import org.netbeans.modules.xml.schema.model.BoundaryFacet;
import org.w3c.dom.Element;

/**
 * Common class for element class representing bounding value.
 *
 * @author nn136682
 */
public abstract class BoundaryElement extends SchemaComponentImpl implements BoundaryFacet {

    /** Creates a new instance of BoundaryElement */
    public BoundaryElement(SchemaModelImpl model, Element e) {
        super(model, e);
    }

    public abstract String getComponentName();

    public void setValue(String v) {
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, v);
    }
    
    public String getValue() {
        String v = super.getAttribute(SchemaAttributes.VALUE);
        if (v == null) {
            throw new IllegalArgumentException("Element '" + getComponentName() + "' got null value.");
        }
        return v;
    }
    
    public Boolean isFixed() {
        String s = getAttribute(SchemaAttributes.FIXED);
        return s == null ? null : Boolean.valueOf(s);
    }
    
    public void setFixed(Boolean isFixed) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, isFixed);
    }

    public boolean getFixedDefault() {
        return false;
    }
	
    public boolean getFixedEffective() {
        Boolean v = isFixed();
        return v == null ? getFixedDefault() : v;
    }
}
    
