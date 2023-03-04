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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.w3c.dom.Element;
/**
 *
 * @author rico
 */
public abstract class CommonSimpleTypeImpl extends SchemaComponentImpl implements SimpleType{

    /** Creates a new instance of CommonSimpleTypeImpl */
    public CommonSimpleTypeImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }
    
    public void setDefinition(SimpleTypeDefinition def) {
        if(def == null){
            throw new IllegalArgumentException(
                    "Element 'simpleType' must have either 'restriction' or 'list' or 'union'");
        }
        List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(SimpleTypeDefinition.class, DEFINITION_PROPERTY, def, classes);
    }
    
    public SimpleTypeDefinition getDefinition() {
        Collection<SimpleTypeDefinition> elements = getChildren(SimpleTypeDefinition.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        //TODO should we throw exception if there is no definition?
        return null;
    }
    
}
