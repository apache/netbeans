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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sdedic
 */
public class PropertySetter extends PropertyValue implements HasContent {
    /**
     * Character content of the property value, if the value is simple
     */
    private Object  valueContent;
    
    /**
     * If true, this PE corresponds to the default property and its
     * Element is not present in the source.
     */
    private boolean implicit;
    
    /**
     * Nested instances
     */
    private List<FxObjectBase>    valueBeans = Collections.emptyList();

    PropertySetter(String propertyName) {
        super(propertyName);
    }
    
    PropertySetter asImplicitDefault() {
        this.implicit = true;
        return this;
    }

    public boolean isImplicit() {
        return implicit;
    }
    
    public List<FxObjectBase> getValues() {
        return Collections.unmodifiableList(valueBeans);
    }
    
    /**
     * Returns the content of the property value.
     * @return 
     */
    public CharSequence getContent() {
        CharSequence c = getValContent(valueContent);
        if (c != valueContent) {
            valueContent = c;
        }
        return c;
    }
    
    void addValue(FxObjectBase instance) {
        if (valueBeans.isEmpty()) {
            valueBeans = new ArrayList<FxObjectBase>();
        }
        valueBeans.add(instance);
    }
    
    @Override
    public void accept(FxNodeVisitor v) {
        v.visitPropertySetter(this);
    }
    
    static Object addCharContent(Object valueContent, CharSequence content) {
        if (valueContent == null) {
            valueContent = content;
        } else if (valueContent instanceof CharSequence) {
            List<CharSequence> parts = new ArrayList<CharSequence>();
            parts.add((CharSequence)valueContent);
            parts.add(content);
            valueContent = parts;
        } else if (valueContent instanceof List) {
            ((List<CharSequence>)(List)valueContent).add(content);
        }
        return valueContent;
    }
    
    static CharSequence getValContent(Object valueContent) {
        if (valueContent == null) {
            return null;
        }
        if (valueContent instanceof CharSequence) {
            return (CharSequence)valueContent;
        } else if (valueContent instanceof List) {
            return new CompoundCharSequence(0, (List<CharSequence>)(List)valueContent, -1);
        }
        throw new IllegalStateException();
    }
    
    void addContent(CharSequence content) {
        valueContent = addCharContent(valueContent, content);
    }

    @Override
    void addChild(FxNode child) {
        super.addChild(child);
        if (child instanceof FxObjectBase) {
            addValue((FxObjectBase)child);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    
}
