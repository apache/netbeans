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

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring used for changing method signature. It changes method declaration
 * and also all its references (callers).
 *
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see org.netbeans.modules.refactoring.api.AbstractRefactoring
 * @see org.netbeans.modules.refactoring.api.RefactoringSession
 *
 */
public final class ChangeParametersRefactoring extends AbstractRefactoring {
    
    // table of all the changes - it contains all the new parameters and also
    // changes in order
    private ParameterInfo[] paramTable;
    // new vibility
    private CsmVisibility visibility;
    // where to use default values
    private boolean useDefaultValueInFunctionDeclaration;
    
    /**
     * Creates a new instance of change parameters refactoring.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public ChangeParametersRefactoring(CsmObject method, CsmContext editorContext) {
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
     * Getter for new vibility
     * @return vibility
     */
    public CsmVisibility getVisibility() {
        return visibility;
    }

    public boolean isUseDefaultValueOnlyInFunctionDeclaration() {
        return useDefaultValueInFunctionDeclaration;
    }
    /**
     * Sets new parameters for a method
     * @param paramTable new parameters
     */
    public void setParameterInfo(ParameterInfo[] paramTable) {
        this.paramTable = paramTable;
    }

    /**
     * Sets vibility for method
     * @param vibility new vibility
     */
    public void setVisibility(CsmVisibility visibility) {
        this.visibility = visibility;
    }

    public void setUseDefaultValueOnlyInFunctionDeclaration(boolean onlyInDef) {
        this.useDefaultValueInFunctionDeclaration = onlyInDef;
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
        int origIndex;
        CharSequence name;
        CharSequence type;
        CharSequence defaultVal;

        /**
         * Creates a new instanceof of ParameterInfo. This constructor can be
         * used for newly added parameters or changed original parameters.
         * When you call method with -1 origIndex, you have to provide not
         * null values in all other pamarameters, otherwise it throws an
         * IllegalArgumentException.
         *
         * @param  origIndex  for newly added parameters, use -1, otherwise
         *                    use index in original parameters list
         * @param  name       parameter name 
         * @param  type       parameter type
         * @param  defaultVal should be provided for the all new parameters.
         *                    For changed parameters, it is ignored.
         */
        public ParameterInfo(int origIndex, CharSequence name, CharSequence type, CharSequence defaultVal) {
            // new parameter
            // if (origIndex == -1 && (name == null || defaultVal == null || type == null || name.length() == 0 || defaultVal.length() == 0)) {
            //    throw new IllegalArgumentException(NbBundle.getMessage(ChangeParameters.class, "ERR_NoValues"));
            // }
            this.origIndex = origIndex;
            this.name = trim(name);
            this.type = trim(type);
            // do not set default value for existing parameters
            this.defaultVal = origIndex == -1 ? trim(defaultVal) : null;
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
        /**
         * Creates a new instance of ParameterInfo. This constructor is used
         * for existing non-changed parameters. All the values except original
         * position in parameters list is set to null.
         *
         * @param  origIndex  position index in original parameters list
         */
        public ParameterInfo(int origIndex) {
            this(origIndex, null, null, null);
        }
        
        /**
         * Returns value of original parameter index.
         *
         * @return  original index of parameter in parameters list
         */
        public int getOriginalIndex() { return origIndex; }
        
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

        /**
         * Returns value of the default value in case of the new parameter.
         * Otherwise, it returns null.
         *
         * @return default value for new parameter, otherwise null.
         */
        public CharSequence getDefaultValue() { return defaultVal; }
    }
    
    ////////////////////////////////////////////////////////////////////////////
}
