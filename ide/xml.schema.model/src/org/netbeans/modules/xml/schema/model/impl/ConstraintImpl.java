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
import java.util.List;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Selector;
import org.w3c.dom.Element;/**
 *
 * @author Vidhya Narayanan
 */
public abstract class ConstraintImpl extends NamedImpl
	implements Constraint {
    
    /**
     * Creates a new instance of ConstraintImpl 
     */
    public ConstraintImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.KEY,model));
    }
    
    /**
     * Creates a new instance of ConstraintImpl
     */
    public ConstraintImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }
    
    /**
     *
     */
    public void setSelector(Selector s) {
        List<Class<? extends SchemaComponent>> classes = new ArrayList<Class<? extends SchemaComponent>>();
        classes.add(Annotation.class);
        setChild(Selector.class, SELECTOR_PROPERTY, s, classes);
    }
    
    /**
     *
     */
    public Selector getSelector() {
        Collection<Selector> elements = getChildren(Selector.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    /**
     *
     */
    public Collection<Field> getFields() {
        return getChildren(Field.class);
    }
    
    /**
     *
     */
    public void deleteField(Field field) {
        removeChild(FIELD_PROPERTY, field);
    }
    
    /**
     *
     */
    public void addField(Field field) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        list.add(Selector.class);
        addAfter(FIELD_PROPERTY, field, list);
    }
}
