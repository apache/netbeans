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
package org.netbeans.modules.javafx2.editor.completion.beans;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypeMirrorHandle;

/**
 * Describes a property or an attached property. 
 * 
 * @author sdedic
 */
public final class FxProperty extends FxDefinition {
    /**
     * Simple properties have from-string converters (value-ofs)
     */
    private boolean simple;
    
    /**
     * Type of the property
     */
    private TypeMirrorHandle  type;
    
    /**
     * Object type that the attached property accepts.
     */
    private TypeMirrorHandle objectType;
    
    /**
     * Property getter method, which returns 
     */
    private ElementHandle<ExecutableElement> observableAccessor;

    /**
     * Accessor used to set the property. Getter in the case of readonly
     * properties
     */
    private ElementHandle<ExecutableElement> accessor;
    
    /**
     * Kind of definition (property, attached, list, map)
     */
    private FxDefinitionKind kind;

    /**
     * Type of the data accepted by the property. For attache properties, this is the
     * type of the attached value.
     * <p/>
     * May return {@code null}, if the type could not be resolved.
     * 
     * @return value type
     */
    @NonNull
    public TypeMirrorHandle getType() {
        return type;
    }

    /**
     * For attached properties, the type of object the value should be attached to.
     * {@code null} for normal properties. May return {@code null}, if the object
     * type cannot be resolved
     * 
     * @return 
     */
    @CheckForNull
    public TypeMirrorHandle getObjectType() {
        return objectType;
    }

    /**
     * Accessor method. Setter for {@link Kind#SETTER}, getter for
     * readonly {@link Kind#MAP} or {@link Kind#LIST} and attach set method
     * for {@link Kind#ATTACHED}.
     * 
     * @return accessor handle
     */
    @NonNull
    public ElementHandle<ExecutableElement> getAccessor() {
        return accessor;
    }
    
    public ElementHandle<ExecutableElement> getObservableAccessor() {
        return observableAccessor;
    }

    /**
     * Returns kind of the property. The property may be {@link FxDefinitionKind#ATTACHED}
     * for static or attached properties, {@link FxDefinitionKind#SETTER} for normal
     * r/w properties, {@link FxDefinitionKind#LIST} for read-only lists and finally
     * {@link FxDefinitionKind#MAP} for read-only maps.
     * 
     * @return kind of property
     */
    public FxDefinitionKind getKind() {
        return kind;
    }
    
    void setObjectType(TypeMirrorHandle objectType) {
        this.objectType = objectType;
    }

    void setAccessor(ElementHandle<ExecutableElement> accessor) {
        this.accessor = accessor;
    }

    void setObservableAccessors(ElementHandle<ExecutableElement> observableAccessor) {
        this.observableAccessor = observableAccessor;
    }

    void setSimple(boolean simple) {
        this.simple = simple;
    }
    
    void setType(TypeMirrorHandle type) {
        this.type = type;
    }

    FxProperty(String name, FxDefinitionKind kind) {
        super(name);
        this.kind = kind;
    }

    /**
     * Determines whether the property is 'simple'. Simple properties can be
     * written as FXML attributes, there's String > property type conversion
     * available.
     * 
     * @return true, if the property is simple
     */
    public boolean isSimple() {
        return simple;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Property[");
        sb.append("name: ").append(getName()).
                append("; kind: ").append(getKind()).
                append("; simple: ").append(isSimple()).
                append("; type: ").append(getType()).
                append("; target: ").append(getObjectType()).
                append("; accessor: ").append(getAccessor());
        sb.append("]");
        
        return sb.toString();
    }
}
