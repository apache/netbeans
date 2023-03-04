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

import org.netbeans.modules.xml.schema.model.LengthFacet;
import org.w3c.dom.Element;

/**
 * Common implementation for length-related components.
 *
 * @author nn136682
 */

public abstract class CommonLength extends SchemaComponentImpl implements LengthFacet {

    /** Creates a new instance of CommonLength */
    public CommonLength(SchemaModelImpl model, Element e) {
        super(model, e);
    }

    public abstract String getComponentName();

    public void setValue(int v) {
        if (v < 0) {
            throw new IllegalArgumentException("Element '" + getComponentName() + "' can only have positive integer value.");
        }
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, String.valueOf(v));
    }
    
    public int getValue() {
        String v = super.getAttribute(SchemaAttributes.VALUE);
        if (v == null) {
            return 1;
        }
        int i = Integer.valueOf(v);
        if (i < 0) {
            throw new IllegalArgumentException("Element '" + getComponentName() + "' can only has positive integer value.");
        }
        return i;
    }
    
    public Boolean isFixed() {
        String v = super.getAttribute(SchemaAttributes.FIXED);
        return v == null ? null : Boolean.valueOf(v);
    }
    
    public void setFixed(Boolean isFixed) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, isFixed);
    }
    
    protected Class getAttributeType(SchemaAttributes attr) {
        switch(attr) {
            case FIXED:
                return String.class;
            default:
                return super.getAttributeType(attr);
        }
    }

    public boolean getFixedEffective() {
        Boolean v = isFixed();
        return v == null ? getFixedDefault() : v;
    }

    public boolean getFixedDefault() {
        return false;
    }
}
