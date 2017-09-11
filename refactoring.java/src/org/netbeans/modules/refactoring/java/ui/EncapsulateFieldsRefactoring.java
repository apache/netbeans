/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    private Set<Modifier> fieldModifiers = Collections.emptySet();;
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
