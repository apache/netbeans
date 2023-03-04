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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;

/**
 *
 * @author sdedic
 */
public final class StaticProperty extends PropertySetter {
    private String                      sourceClassName;
    private ElementHandle<TypeElement>  sourceType;
    
    StaticProperty(String sourceClassName, String name) {
        super(name);
        this.sourceClassName = sourceClassName;
    }
    
    public String getSourceClassName() {
        return sourceClassName;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitStaticProperty(this);
    }
    
    public ElementHandle<TypeElement>   getSourceClassHandle() {
        return sourceType;
    }
    
    void setSourceType(ElementHandle<TypeElement> handle) {
        this.sourceType = handle;
    }
    
    public String getPropertyName() {
        return super.getSourceName();
    }

    @Override
    public String getSourceName() {
        return sourceClassName + "." + super.getSourceName();
    }

    @Override
    @SuppressWarnings("rawtypes")
    void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
        super.resolve(nameHandle, typeHandle, sourceTypeHandle, info);
        sourceType = sourceTypeHandle;
    }
    
}
