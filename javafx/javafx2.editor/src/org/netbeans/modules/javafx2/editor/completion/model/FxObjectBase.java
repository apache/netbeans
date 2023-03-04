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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;

/**
 * Abstract base for object instances.
 * You probably want to use more concrete subclasses, {@link FxReference} for immutable
 * references to other instances, or {@link FxInstance} for new instances or copies.
 *
 * @author sdedic
 */
public abstract class FxObjectBase extends FxNode {
    /**
     * Class name, as appears in the source
     */
    private String sourceName;
    
    /**
     * Resolved java type, if it exists
     */
    private ElementHandle<TypeElement>   javaType;
    
    private FxBean info;
    
    /**
     * Class name as it appears in the source text
     * @return 
     */
    public String getSourceName() {
        return sourceName;
    }

    void setSourceName(String className) {
        this.sourceName = className;
    }

    /**
     * Provides handle to the java {@link TypeElement}, that corresponds to the
     * object's class. May return {@code null}, if the type was not resolved.
     * 
     * @return 
     */
    @CheckForNull
    public ElementHandle<TypeElement> getJavaType() {
        return javaType;
    }

    void setJavaType(ElementHandle<TypeElement> javaType) {
        this.javaType = javaType;
    }
    
    public String getResolvedName() {
        return javaType == null ? null : javaType.getQualifiedName();
    }

    @SuppressWarnings("unchecked")
    @Override
    void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
        this.javaType = nameHandle;
        this.info = (FxBean)info;
    }
    
    public FxBean getDefinition() {
        return info;
    }
    
    
}
