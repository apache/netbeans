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
package org.netbeans.modules.refactoring.java.api;

import java.util.Collections;
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
        this.methodModifiers = Collections.unmodifiableSet(
                new HashSet<Modifier>(methodModifiers));
    }

    /**
     * Setter for fieldModifiers property
     * @param fieldModifiers New value of fieldModifiers
     */
    public void setFieldModifiers(Set<Modifier> fieldModifiers) {
        this.fieldModifiers = Collections.unmodifiableSet(
                new HashSet<Modifier>(fieldModifiers));
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
