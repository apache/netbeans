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
