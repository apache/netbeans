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
