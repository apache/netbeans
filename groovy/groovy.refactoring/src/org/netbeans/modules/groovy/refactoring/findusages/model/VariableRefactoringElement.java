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
