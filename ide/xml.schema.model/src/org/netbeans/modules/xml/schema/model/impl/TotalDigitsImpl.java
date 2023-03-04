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
import org.netbeans.modules.xml.schema.model.TotalDigits;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * <totalDigits
 *  fixed = boolean : false
 *  id = ID
 *  value = positiveInteger
 *  {any attributes with non-schema namespace . . .}>
 *  Content: (annotation?)
 * </totalDigits>
 *
 * @author Nam Nguyen
 */
public class TotalDigitsImpl extends CommonLength implements TotalDigits {
    
    public TotalDigitsImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.TOTAL_DIGITS,model));
    }
    
    /** Creates a new instance of TotalDigitsImpl */
    public TotalDigitsImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return TotalDigits.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    public String getComponentName() {
        return SchemaElements.TOTAL_DIGITS.toString();
    }
    
    @Override
    public void setValue(int v) {
        if (v < 1) {
            throw new IllegalArgumentException("Element 'totalDigits' can only have positive integer value.");
        }
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, Integer.valueOf(v));
    }
    
    @Override
    public int getValue() {
        String v = getAttribute(SchemaAttributes.VALUE);
        if (v == null) {
            return 1;
        }
        int i = Integer.valueOf(v);
        if (i < 1) {
            throw new IllegalArgumentException("Element '" + SchemaElements.TOTAL_DIGITS + "' got non-positive integer value.");
        }
        return i;
    }
    
}
