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

/*
 * This implements interface List, represents the xs:list element, which is a whitespace
 * separated list of values.
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

public class ListImpl extends SchemaComponentImpl  implements List {
    
    public ListImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.LIST,model));
    }
    
    /** Creates a new instance of ListImpl */
    public ListImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return List.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public NamedComponentReference<GlobalSimpleType> getType() {
        return resolveGlobalReference(GlobalSimpleType.class, SchemaAttributes.ITEM_TYPE);
    }
	
    public void setType(NamedComponentReference<GlobalSimpleType> type) {
        setAttribute(TYPE_PROPERTY, SchemaAttributes.ITEM_TYPE, type );
    }
	
    public LocalSimpleType getInlineType() {
        Collection<LocalSimpleType> types = getChildren(LocalSimpleType.class);
        if (types.size() > 1 || types.size() < 0) {
            throw new IllegalArgumentException("'" + SchemaElements.LIST + "' can only local simpleType child");
        }
        LocalSimpleType[] typesA = types.toArray(new LocalSimpleType[1]);
        if (typesA.length == 0) {
            return null;
        } else {
            return typesA[0];
        }
    }
	
    public void setInlineType(LocalSimpleType st) {
        java.util.List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(LocalSimpleType.class, INLINE_TYPE_PROPERTY, st, classes);
    }
}
