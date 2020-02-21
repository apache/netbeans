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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class IntroduceMethodRefactoring extends AbstractRefactoring {

    // table of all the changes - it contains all the new parameters and also
    public final static int PARAM_BY_REF = 0;
    public final static int PARAM_NAME = 1;
    public final static int PARAM_TYPE = 2;
    // changes in order
    private ParameterInfo[] paramTable;
    private String functionName;
    private IntroduceMethodContext introduceMethodContext;
    private String methodDeclaration;
    private String methodDefinition;
    private String methodCall;
    private int declarationInsetOffset;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public int getDeclarationInsetOffset() {
        return declarationInsetOffset;
    }

    public void setDeclarationInsetOffset(int declarationInsetOffset) {
        this.declarationInsetOffset = declarationInsetOffset;
    }

    public String getMethodDeclaration() {
        return methodDeclaration;
    }

    public void setMethodDeclaration(String methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public String getMethodDefinition() {
        return methodDefinition;
    }

    public void setMethodDefinition(String methodDefinition) {
        this.methodDefinition = methodDefinition;
    }

    public String getMethodCall() {
        return methodCall;
    }

    public void setMethodCall(String methodCall) {
        this.methodCall = methodCall;
    }

    /**
     * Creates a new instance of change parameters refactoring.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public IntroduceMethodRefactoring(CsmObject method, CsmContext editorContext) {
        super(createLookup(method, editorContext));
    }

    private static Lookup createLookup(CsmObject method, CsmContext editorContext) {
        assert method != null || editorContext != null: "must be non null object to refactor";
        if (editorContext == null) {
            return Lookups.fixed(method);
        } else if (method == null) {
            return Lookups.fixed(editorContext);
        } else {
            return Lookups.fixed(method, editorContext);
        }
    }

    /**
     * Getter for new parameters
     * @return array of new parameters
     */
    public ParameterInfo[] getParameterInfo() {
        return paramTable;
    }

    /**
     * Sets new parameters for a method
     * @param paramTable new parameters
     */
    public void setParameterInfo(ParameterInfo[] paramTable) {
        this.paramTable = paramTable;
    }

    public IntroduceMethodContext getIntroduceMethodContext() {
        return introduceMethodContext;
    }

    public void setIntroduceMethodContext(IntroduceMethodContext introduceMethodContext) {
        this.introduceMethodContext = introduceMethodContext;
    }

    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Represents one item for setParameters(List params) list parameter.
     * Item contains information about changes in method parameters.
     * Parameter can be added, changed or moved to another position.
     */
    public static final class ParameterInfo {
        private final boolean byRef;
        private final CharSequence name;
        private final CharSequence type;

        /**
         * Creates a new instanceof of ParameterInfo. This constructor can be
         * used for newly added parameters or changed original parameters.
         * When you call method with -1 origIndex, you have to provide not
         * null values in all other pamarameters, otherwise it throws an
         * IllegalArgumentException.
         *
         * @param  name       parameter name
         * @param  type       parameter type
         */
        public ParameterInfo(boolean byRef, CharSequence name, CharSequence type) {
            this.byRef = byRef;
            this.name = trim(name);
            this.type = trim(type);
        }

        private CharSequence trim(CharSequence cs) {
            CharSequence out = cs;
            if (cs != null) {
                out = cs.toString().trim();
                if (out.length() == cs.length()) {
                    out = cs;
                }
            }
            return out;
        }

        public boolean isByRef() {
            return byRef;
        }

        /**
         * Returns value of the name of parameter. If the name was not
         * changed, returns null.
         *
         * @return  new name for parameter or null in case that it was not changed.
         */
        public CharSequence getName() { return name; }

        /**
         * Returns value of the type of parameter. If the name was not
         * changed, returns null.
         *
         * @return new type for parameter or null if it was not changed.
         */
        public CharSequence getType() { return type; }
    }

    public interface IntroduceMethodContext {

        public enum FunctionKind {
            Function, // function
            MethodDefinition, // method definition (out of class)
            MethodDeclarationDefinition // method declaration-definition (inside class)
        }

        Document getDocument();

        CsmClass getEnclosingClass();

        CsmFunctionDefinition getFunction();

        CsmFunction getFunctionDeclaration();

        FunctionKind getFunctionKind();

        List<VariableContext> getImportantVariables();

        CsmScope getInsertScope();

        // point before containing function
        int getInsetionOffset();

        int getSelectionFrom();

        int getSelectionTo();

        boolean isApplicable(AtomicBoolean canceled);

        boolean isC();

    }

    public interface VariableContext {

        List<CsmReference> getReferences();

        CsmVariable getVariable();

        boolean isAccessAfter();

        boolean isAccessBefore();

        boolean isAccessInside();

        boolean isTopLevelDeclaration();

        boolean isWriteAccessInside();
    }
}
