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
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Chris Webster
 */
public class GlobalGroupImpl extends NamedImpl
implements GlobalGroup {
     public GlobalGroupImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.GROUP,model));
    }

    public GlobalGroupImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalGroup.class;
	}

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }
    
    public void setDefinition(LocalGroupDefinition definition) {    
        List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(LocalGroupDefinition.class, DEFINITION_PROPERTY, 
            definition, classes);
    }

    public LocalGroupDefinition getDefinition() {
        List<LocalGroupDefinition> ld = getChildren(LocalGroupDefinition.class);
        return ld.isEmpty() ? null : ld.get(0);
    }
}
