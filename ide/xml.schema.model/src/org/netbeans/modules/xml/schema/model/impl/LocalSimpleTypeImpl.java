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

import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class LocalSimpleTypeImpl extends CommonSimpleTypeImpl implements LocalSimpleType{

    /** Creates a new instance of LocalSimpleTypeImpl */
    protected LocalSimpleTypeImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.SIMPLE_TYPE, model));
    }

    public LocalSimpleTypeImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return LocalSimpleType.class;
	}

    /**
     * Visitor providing
     */
    public void accept(org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor visitor) {
        visitor.visit(this);
    }


}
