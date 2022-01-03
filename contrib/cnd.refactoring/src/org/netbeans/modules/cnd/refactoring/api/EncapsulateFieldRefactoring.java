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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring used for encapsulating one field.
 * It creates getter and setter for given field and replaces all references.
 */
public final class EncapsulateFieldRefactoring extends AbstractRefactoring {
    private String getterName;
    private CsmMethod defaultGetter;
    private String setterName;
    private CsmMethod defaultSetter;
    private Set<CsmVisibility> methodModifiers;
    private Set<CsmVisibility> fieldModifiers;
    private boolean alwaysUseAccessors;
    private boolean methodInline;
    private final CsmFile declFile;
    private final CsmFile defFile;
    private final CsmClass enclosingClass;
    /**
     * Creates a new instance of EncapsulateFieldRefactoring
     * @param field field to refactor
     */
    public EncapsulateFieldRefactoring(CsmField field, CsmFile declFile, CsmFile defFile) {
        super(Lookups.fixed(field));
        this.enclosingClass = field.getContainingClass();
        this.declFile = declFile;
        this.defFile = defFile;
    }
    
    public CsmField getSourceField() {
        return getRefactoringSource().lookup(CsmField.class);
    }

    public CsmClass getEnclosingClass() {
        return enclosingClass;
    }

    public CsmFile getClassDeclarationFile() {
        return this.declFile;
    }

    public CsmFile getClassDefinitionFile() {
        return this.defFile;
    }
    /**
     * Getter for property getterName
     * @return Value of property getterName
     */
    public String getGetterName() {
        return getterName;
    }

    /**
     * Getter for property setterName
     * @return Value of property setterName
     */
    public String getSetterName() {
        return setterName;
    }

    /**
     * Getter for property methodModifiers
     * @return Value of property methodModifiers
     */
    public Set<CsmVisibility> getMethodModifiers() {
        return methodModifiers;
    }

    /**
     * Getter for property fieldModifiers
     * @return Value of property fieldModifiers
     */
    public Set<CsmVisibility> getFieldModifiers() {
        return fieldModifiers;
    }

    /**
     * Getter for boolean property alwaysUseAccessors
     * @return Value of property alwaysUseAccessors
     */
    public boolean isAlwaysUseAccessors() {
        return alwaysUseAccessors;
    }

    /**
     * Getter for boolean property methodInline
     * @return Value of property methodInline
     */
    public boolean isMethodInline() {
        return methodInline;
    }

    public CsmMethod getDefaultGetter() {
        return defaultGetter;
    }

    public CsmMethod getDefaultSetter() {
        return defaultSetter;
    }

    public void setDefaultGetter(CsmMethod defaultGetter) {
        this.defaultGetter = defaultGetter;
    }

    public void setDefaultSetter(CsmMethod defaultSetter) {
        this.defaultSetter = defaultSetter;
    }

    /**
     * Setter for getterName property
     * @param getterName New value of getterName
     */
    public void setGetterName(String getterName) {
        this.getterName = getterName;
    }

    /**
     * Setter for setterName property
     * @param setterName New value of setterName
     */
    public void setSetterName(String setterName) {
        this.setterName = setterName;
    }

    /**
     * Setter for methodModifiers property
     * @param methodModifiers New value of methodModifiers
     */
    public void setMethodModifiers(Set<CsmVisibility> methodModifiers) {
        this.methodModifiers = Collections.unmodifiableSet(
                new HashSet<>(methodModifiers));
    }

    /**
     * Setter for fieldModifiers property
     * @param fieldModifiers New value of fieldModifiers
     */
    public void setFieldModifiers(Set<CsmVisibility> fieldModifiers) {
        this.fieldModifiers = Collections.unmodifiableSet(
                new HashSet<>(fieldModifiers));
    }

    /**
     * Setter for alwaysUseAccessors property
     * @param alwaysUseAccessors New value of alwaysUseAccessors
     */
    public void setAlwaysUseAccessors(boolean alwaysUseAccessors) {
        this.alwaysUseAccessors = alwaysUseAccessors;
    }

    /**
     * Setter for methodInline property
     * @param methodInline New value of methodInline
     */
    public void setMethodInline(boolean methodInline) {
        this.methodInline = methodInline;
    }
}
