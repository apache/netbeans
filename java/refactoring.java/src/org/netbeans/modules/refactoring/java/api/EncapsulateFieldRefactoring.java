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
package org.netbeans.modules.refactoring.java.api;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring used for changing encapsulate field.
 * It creates getter and setter for given field and replaces all references.
 * @author Tomas Hurka
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldRefactoring extends AbstractRefactoring {
    private String getterName,setterName;
    private Set<Modifier> methodModifiers;
    private Set<Modifier> fieldModifiers;
    private boolean alwaysUseAccessors;
    private boolean isGeneratePropertyChangeSupport;
    private boolean isGenerateVetoableSupport;
    
    /**
     * Creates a new instance of EncapsulateFieldRefactoring
     * @param field field to refactor
     */
    public EncapsulateFieldRefactoring(TreePathHandle field) {
        super(Lookups.fixed(field));
    }
    
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
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
    public Set<Modifier> getMethodModifiers() {
        return methodModifiers;
    }

    /**
     * Getter for property fieldModifiers
     * @return Value of property fieldModifiers
     */
    public Set<Modifier> getFieldModifiers() {
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
    public void setMethodModifiers(Set<Modifier> methodModifiers) {
        EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        modifiers.addAll(methodModifiers);
        this.methodModifiers = Collections.unmodifiableSet(modifiers);
    }

    /**
     * Setter for fieldModifiers property
     * @param fieldModifiers New value of fieldModifiers
     */
    public void setFieldModifiers(Set<Modifier> fieldModifiers) {
        EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        modifiers.addAll(fieldModifiers);
        this.fieldModifiers = Collections.unmodifiableSet(modifiers);
    }

    /**
     * Setter for alwaysUseAccessors property
     * @param alwaysUseAccessors New value of alwaysUseAccessors
     */
    public void setAlwaysUseAccessors(boolean alwaysUseAccessors) {
        this.alwaysUseAccessors = alwaysUseAccessors;
    }

    /**
     * Should be PropertyChangeSupport generated?
     * @return true if PropertyChangeSupport should be generated
     * @since 1.32
     */
    public boolean isGeneratePropertyChangeSupport() {
        return isGeneratePropertyChangeSupport;
    }

    /**
     * Should be PropertyChangeSupport generated?
     * @param isGeneratePropertyChangeSupport true if PropertyChangeSupport should be generated
     * @since 1.32
     */
    public void setGeneratePropertyChangeSupport(boolean isGeneratePropertyChangeSupport) {
        this.isGeneratePropertyChangeSupport = isGeneratePropertyChangeSupport;
    }

    /**
     * Should be VetoableChangeSupport generated?
     * @return true if VetoableChangeSupport should be generated
     * @since 1.32
     */
    public boolean isGenerateVetoableChangeSupport() {
        return isGenerateVetoableSupport;
    }

    /**
     * Should be VetoableChangeSupport generated?
     * @param isGenerateVetoableChangeSupport true if VetoableChangeSupport should be generated
     * @since 1.32
     */
    public void setGenerateVetoableSupport(boolean isGenerateVetoableChangeSupport) {
        this.isGenerateVetoableSupport = isGenerateVetoableChangeSupport;
    }
    
}
