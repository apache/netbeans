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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/** 
 * Extract Interface Refactoring.
 * 
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see org.netbeans.modules.refactoring.api.AbstractRefactoring
 * @see org.netbeans.modules.refactoring.api.RefactoringSession
 *
 * @author Martin Matula
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class ExtractInterfaceRefactoring extends AbstractRefactoring {
    // name of the new class to be created
    private String ifcName;
    private List<ElementHandle<ExecutableElement>> methods;
    private List<ElementHandle<VariableElement>> fields;
    private List<TypeMirrorHandle<TypeMirror>> implementz;
    
    /** Creates a new instance of ExtractInterfaceRefactoring 
     * @param sourceType Type the members of which should be extracted into an interface.
     */
    public ExtractInterfaceRefactoring(TreePathHandle sourceType) {
        super(Lookups.fixed(sourceType));
    }
    
    /** Returns the type the members of which should be extracted into an interface
     * by this refactoring.
     * @return Source of the members to be extracted.
     */
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }

    // --- PARAMETERS ----------------------------------------------------------
    
    /** Returns name of the interface to be created.
     * @return Name of the new interface or null if it is not set.
     */
    public String getInterfaceName() {
        return ifcName;
    }

    /** Sets the name of the interface to be created.
     * @param ifcName Name of the new interface.
     */
    public void setInterfaceName(String ifcName) {
        this.ifcName = ifcName;
    }

    /**
     * Gets methods to extract.
     * @return list of methods
     */
    public List<ElementHandle<ExecutableElement>> getMethods() {
        return methods != null? methods: Collections.<ElementHandle<ExecutableElement>>emptyList();
    }

    /**
     * Sets public methods to extract.
     * @param methods list of methods
     */
    public void setMethods(List<ElementHandle<ExecutableElement>> methods) {
        if (methods == null) {
            throw new NullPointerException();
        }
        List<ElementHandle<ExecutableElement>> l = new ArrayList<ElementHandle<ExecutableElement>>(methods);
        this.methods = Collections.<ElementHandle<ExecutableElement>>unmodifiableList(l);
    }

    /**
     * Gets fields to extract.
     * @return list of fields
     */
    public List<ElementHandle<VariableElement>> getFields() {
        return fields != null? fields: Collections.<ElementHandle<VariableElement>>emptyList();
    }

    /**
     * Sets public static final fields with default value to extract.
     * @param fields list of fields
     */
    public void setFields(List<ElementHandle<VariableElement>> fields) {
        if (fields == null) {
            throw new NullPointerException();
        }
        List<ElementHandle<VariableElement>> l = new ArrayList<ElementHandle<VariableElement>>(fields);
        this.fields = Collections.<ElementHandle<VariableElement>>unmodifiableList(l);
    }

    /**
     * Gets interfaces to extract.
     * @return list of interfaces
     */
    public List<TypeMirrorHandle<TypeMirror>> getImplements() {
        return implementz != null? implementz: Collections.<TypeMirrorHandle<TypeMirror>>emptyList();
    }

    /**
     * Sets interfaces to extract.
     * @param implementz list of interfaces
     */
    public void setImplements(List<TypeMirrorHandle<TypeMirror>> implementz) {
        if (implementz == null) {
            throw new NullPointerException();
        }
        List<TypeMirrorHandle<TypeMirror>> l = new ArrayList<TypeMirrorHandle<TypeMirror>>(implementz);
        this.implementz = Collections.<TypeMirrorHandle<TypeMirror>>unmodifiableList(l);
    }
}
