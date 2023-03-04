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

import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;

/**
 *
 * @author sdedic
 */
public abstract class PropertyValue extends FxNode {
    /**
     * Property name
     */
    private String  name;

    /**
     * Type of the property, if known
     */
    @NullAllowed
    private TypeMirrorHandle typeHandle;
    
    /**
     * Resolved FxProperty for this property
     */
    private FxProperty    propertyInfo;
    
    PropertyValue(String name) {
        this.name = name;
    }
    
    public String getSourceName() {
        return name;
    }
    
    public String getPropertyName() {
        if (name == null && propertyInfo != null) {
            return propertyInfo.getName();
        }
        return name;
    }
    
    @Override
    public Kind getKind() {
        return Kind.Property;
    }

    public TypeMirrorHandle getTypeHandle() {
        return typeHandle;
    }
    
    void setTypeHandle(TypeMirrorHandle handle) {
        this.typeHandle = handle;
    }

    void setPropertyInfo(FxProperty info) {
        this.propertyInfo = info;
    }
    
    public FxProperty getPropertyInfo() {
        return propertyInfo;
    }
    
    @Override
    void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
        this.typeHandle = typeHandle;
        this.propertyInfo = (FxProperty)info;
    }
    
}
