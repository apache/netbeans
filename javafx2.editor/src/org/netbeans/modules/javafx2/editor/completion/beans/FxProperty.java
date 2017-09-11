/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
