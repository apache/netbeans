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
package org.netbeans.modules.refactoring.java.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/** Encapsulate fields refactoring. This is a composed refactoring (uses instances of {@link org.netbeans.modules.refactoring.api.EncapsulateFieldRefactoring}
 * to encapsulate several fields at once.
 *
 * @author Pavel Flaska
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldsRefactoring extends AbstractRefactoring {
    
    private Collection<EncapsulateFieldInfo> refactorFields = Collections.emptyList();
    private Set<Modifier> methodModifiers = Collections.emptySet();
    private Set<Modifier> fieldModifiers = Collections.emptySet();
    private boolean alwaysUseAccessors;
    private boolean isGeneratePropertyChangeSupport;
    private boolean isGenerateVetoableSupport;
    

    /** Creates a new instance of EcapsulateFields.
     * @param selectedObject field to encapsulate, whatever tree of class
     *          containing trees to encapsulate
     */
    public EncapsulateFieldsRefactoring(TreePathHandle selectedObject) {
        super(Lookups.fixed(selectedObject));
    }
    
    public EncapsulateFieldsRefactoring(Collection<TreePathHandle> selectedObjects) {
        super(Lookups.fixed(selectedObjects.iterator().next(), selectedObjects));
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
    public Set<Modifier> getMethodModifiers() {
        return methodModifiers;
    }

    /**
     * Getter for property fieldModifier
     * @return Value of fieldModifier
     */
    public Set<Modifier> getFieldModifiers() {
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
     * Setter for property refactorFields
     * @param refactorFields New value of property refactorFields
     */
    public void setRefactorFields(Collection<EncapsulateFieldInfo> refactorFields) {
        this.refactorFields = Collections.unmodifiableCollection(
                new ArrayList<EncapsulateFieldInfo>(refactorFields));
    }

    /**
     * Setter for property methodModifier
     * @param methodModifier New value of property methodModifier
     */
    public void setMethodModifiers(Set<Modifier> methodModifier) {
        this.methodModifiers = methodModifier;
    }

    /**
     * Setter for property fieldModifier
     * @param fieldModifier New value of property fieldModifier
     */
    public void setFieldModifiers(Set<Modifier> fieldModifier) {
        this.fieldModifiers = fieldModifier;
    }

    /**
     * Setter for property alwaysUseAccessors
     * @param alwaysUseAccessors New value of property alwaysUseAccessors
     */
    public void setAlwaysUseAccessors(boolean alwaysUseAccessors) {
        this.alwaysUseAccessors = alwaysUseAccessors;
    }

    public TreePathHandle getSelectedObject() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }
    
    /**
     * 
     * @return
     */
    public boolean isGeneratePropertyChangeSupport() {
        return isGeneratePropertyChangeSupport;
    }

    /**
     * 
     * @param isGeneratePropertyChangeSupport
     */
    public void setGeneratePropertyChangeSupport(boolean isGeneratePropertyChangeSupport) {
        this.isGeneratePropertyChangeSupport = isGeneratePropertyChangeSupport;
    }

    /**
     * 
     * @return
     */
    public boolean isGenerateVetoableSupport() {
        return isGenerateVetoableSupport;
    }

    /**
     * 
     * @param isGenerateVetoableSupport
     */
    public void setGenerateVetoableSupport(boolean isGenerateVetoableSupport) {
        this.isGenerateVetoableSupport = isGenerateVetoableSupport;
    }    
    

    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Represents data from the panel.
     */
    public static final class EncapsulateFieldInfo {
        TreePathHandle field;
        String getterName;
        String setterName;
        
        /**
         * Creates an instance of Encapsulate Field Info
         * @param field 
         * @param getterName 
         * @param setterName 
         */
        public EncapsulateFieldInfo(TreePathHandle field, String getterName, String setterName) {
            this.field = field;
            this.getterName = getterName;
            this.setterName = setterName;
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
        public TreePathHandle getField() { return field; }
        
    }
}
