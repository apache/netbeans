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

package org.netbeans.modules.groovy.refactoring.findusages.model;

import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.ElementKind;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Janicek
 */
public class VariableRefactoringElement extends RefactoringElement {

    private final ClassNode variableType;
    private final String variableName;


    /**
     * Basically we have two situations in which we want to create new Variable refactoring.
     * 1. Choosing field/property name (in this case the important information for us is an
     *    owner of the field/property - we don't care about type)
     * 2. Choosing variable name (this is more complicated because we need to resolve 
     *    proper variable type - which might be also dynamic)
     *
     * @param fileObject
     * @param variableType
     * @param variableName
     */
    public VariableRefactoringElement(
            @NonNull final FileObject fileObject,
            @NonNull final ClassNode variableType,
            @NonNull final String variableName) {
        
        super(fileObject, variableType);

        Parameters.notNull("fileObject", fileObject);
        Parameters.notNull("variableType", variableType);
        Parameters.notNull("variableName", variableName);

        this.variableType = variableType;
        this.variableName = variableName;
    }

    
    @Override
    public ElementKind getKind() {
        return ElementKind.VARIABLE;
    }

    @Override
    public String getShowcase() {
        return variableName + " : " + variableType.getNameWithoutPackage(); // NOI18N
    }

    @Override
    public String getName() {
        return getVariableName();
    }

    public String getVariableName() {
        return variableName;
    }

    /**
     * Returns the type name of the refactoring element. (e.g. for field declaration
     * "private GalacticMaster master" the method return "GalacticMaster")
     *
     * If the type is dynamic, the method returns "java.lang.Object".
     *
     * @return type of the refactoring element
     */
    public String getVariableTypeName() {
        return variableType.getName();
    }
}
