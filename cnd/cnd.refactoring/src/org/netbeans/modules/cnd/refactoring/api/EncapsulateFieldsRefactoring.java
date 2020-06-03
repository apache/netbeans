/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.refactoring.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Encapsulate fields refactoring. This is a composed refactoring (uses instances of {@link org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldRefactoring}
 * to encapsulate several fields at once.
 *
 */
public final class EncapsulateFieldsRefactoring extends AbstractRefactoring {
    
    private Collection<EncapsulateFieldInfo> refactorFields = Collections.emptyList();
    private Set<CsmVisibility> methodModifiers = Collections.emptySet();
    private Set<CsmVisibility> fieldModifiers = Collections.emptySet();;
    private CsmFile classDeclFile = null;
    private CsmFile classDefFile = null;
    private boolean alwaysUseAccessors;
    private boolean methodInline;

    /** Creates a new instance of EcapsulateFields.
     * @param selectedObject field to encapsulate, whatever tree of class
     *          containing trees to encapsulate
     */
    public EncapsulateFieldsRefactoring(CsmObject selectedObject, CsmContext editorContext) {
        super(createLookup(selectedObject, editorContext));
    }

    private static Lookup createLookup(CsmObject selectedObject, CsmContext editorContext) {
        assert selectedObject != null || editorContext != null: "must be non null object to refactor";
        if (editorContext == null) {
            return Lookups.fixed(selectedObject);
        } else if (selectedObject == null) {
            return Lookups.fixed(editorContext);
        } else {
            return Lookups.fixed(selectedObject, editorContext);
        }
    }
    /**
     * Getter for property refactorFields
     * @return Value of refactorFields
     */
    public Collection<EncapsulateFieldInfo> getRefactorFields() {
        return refactorFields;
    }

    /**
     * Getter for property methodModifier
     * @return Value of methodModifier
     */
    public Set<CsmVisibility> getMethodModifiers() {
        return methodModifiers;
    }

    /**
     * Getter for property fieldModifier
     * @return Value of fieldModifier
     */
    public Set<CsmVisibility> getFieldModifiers() {
        return fieldModifiers;
    }

    /**
     * Getter for property alwaysUseAccessors
     * @return Value of alwaysUseAccessors
     */
    public boolean isAlwaysUseAccessors() {
        return alwaysUseAccessors;
    }

    /**
     * Gtter for property methodInline
     * @return Value of methodInline
     */
    public boolean isMethodInline() {
        return methodInline;
    }

    /**
     *  Setter for property methodInline
     * @param methodInline New value of property methodInline
     */
    public void setMethodInline(boolean methodInline) {
        this.methodInline = methodInline;
    }
    
    /**
     * Setter for property refactorFields
     * @param refactorFields New value of property refactorFields
     */
    public void setRefactorFields(Collection<EncapsulateFieldInfo> refactorFields) {
        this.refactorFields = Collections.unmodifiableCollection(
                new ArrayList<>(refactorFields));
    }

    /**
     * Setter for property methodModifier
     * @param methodModifier New value of property methodModifier
     */
    public void setMethodModifiers(Set<CsmVisibility> methodModifier) {
        this.methodModifiers = methodModifier;
    }

    /**
     * Setter for property fieldModifier
     * @param fieldModifier New value of property fieldModifier
     */
    public void setFieldModifiers(Set<CsmVisibility> fieldModifier) {
        this.fieldModifiers = fieldModifier;
    }

    /**
     * Setter for property alwaysUseAccessors
     * @param alwaysUseAccessors New value of property alwaysUseAccessors
     */
    public void setAlwaysUseAccessors(boolean alwaysUseAccessors) {
        this.alwaysUseAccessors = alwaysUseAccessors;
    }

    /**
     * @return the classDeclFile
     */
    public CsmFile getClassDeclarationFile() {
        return classDeclFile;
    }

    /**
     * @param classDeclFile the classDeclFile to set
     */
    public void setClassDeclarationFile(CsmFile classDeclFile) {
        this.classDeclFile = classDeclFile;
    }

    /**
     * @return the classDefFile
     */
    public CsmFile getClassDefinitionFile() {
        return classDefFile;
    }

    /**
     * @param classDefFile the classDefFile to set
     */
    public void setClassDefinitonFile(CsmFile classDefFile) {
        this.classDefFile = classDefFile;
    }

    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Represents data from the panel.
     */
    public static final class EncapsulateFieldInfo {
        final CsmField field;
        final CsmMethod defaultGetter;
        final CsmMethod defaultSetter;
        final String getterName;
        final String setterName;
        /**
         * Creates an instance of Encapsulate Field Info
         * @param field 
         * @param getterName 
         * @param setterName 
         */
        public EncapsulateFieldInfo(CsmField field, String getterName, String setterName,
                CsmMethod defaultGetter, CsmMethod defaultSetter) {
            this.field = field;
            this.getterName = getterName;
            this.setterName = setterName;
            this.defaultGetter = defaultGetter;
            this.defaultSetter = defaultSetter;
        }
        
        /**
         * Getter for property getterName.
         *
         * @return Value of property getterName.
         */
        public String getGetterName() { return getterName; }
        
        /**
         * Getter for property setterName.
         * 
         * @return Value of property setterName.
         */
        public String getSetterName() { return setterName; }
        
        /**
         * Getter for property field.
         *
         * @return Value of property field.
         */
        public CsmField getField() { return field; }

        public CsmMethod getDefaultGetter() { return defaultGetter; }

        public CsmMethod getDefaultSetter() { return defaultSetter; }
    }
}
