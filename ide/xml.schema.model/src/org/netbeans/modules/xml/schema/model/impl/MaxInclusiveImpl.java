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

import org.netbeans.modules.xml.schema.model.MaxInclusive;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This implements interface represents the xs:maxInclusive facet.
 *
 * @author nn136682
 */
public class MaxInclusiveImpl extends BoundaryElement implements MaxInclusive {

    public MaxInclusiveImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.MAX_INCLUSIVE, model));
    }
    
    /** Creates a new instance of MaxInclusiveImpl */
    public MaxInclusiveImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return MaxInclusive.class;
	}

    public String getComponentName() {
        return SchemaElements.MAX_INCLUSIVE.getName();
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }    
}
