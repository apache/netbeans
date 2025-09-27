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
package org.netbeans.modules.refactoring.java.api;

import com.sun.source.tree.VariableTree;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
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
 * @author  Pavel Flaska
 * @author  Tomas Hurka
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 */
public final class ChangeParametersRefactoring extends AbstractRefactoring {
    
    private Object selectedObject;
    // table of all the changes - it contains all the new parameters and also
    // changes in order
    private ParameterInfo[] paramTable;
    // new modifier
    private Set<Modifier> modifiers;
    private String methodName;
    private String returnType;
    private boolean overloadMethod;
    
    /**
     * Creates a new instance of change parameters refactoring.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public ChangeParametersRefactoring(TreePathHandle method) {
        super(Lookups.singleton(method));
    }
    
    /**
     * Getter for new parameters
     * @return array of new parameters
     */
    public ParameterInfo[] getParameterInfo() {
        return paramTable;
    }
    
    /**
     * Getter for new modifiers
     * @return modifiers
     */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    
    /**
     * Get the new return type.
     * 
     * @return returnType, null if it will not change
     * @since 1.25
     */
    public @CheckForNull String getReturnType() {
        return returnType;
    }

    /**
     * Get the new method name.
     * 
     * @return methodName, null if it will no change
     * @since 1.25
     */
    public @CheckForNull String getMethodName() {
        return methodName;
    }

    /**
     * Sets new parameters for a method
     * @param paramTable new parameters
     */
    public void setParameterInfo(ParameterInfo[] paramTable) {
        this.paramTable = paramTable;
    }

    /**
     * Sets modifiers for method
     * @param modifiers new modifiers
     */
    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }
    
    /**
     * Sets the new method name.
     * 
     * @param methodName the new method name, null if it does not change
     * @since 1.25
     */
    public void setMethodName(@NullAllowed String methodName) {
        this.methodName = methodName;
    }

    /**
     * Sets the new return type for the method.
     * 
     * @param returnType the return type to set, null if it does not change
     * @since 1.25
     */
    public void setReturnType(@NullAllowed String returnType) {
        this.returnType = returnType;
    }
    
    /**
     * Create a new overloading method with the new parameter and let the existing
     * method delegate to it:
     * e.g.
     * <pre>
     * int foo() {
     *   return 1+1;
     * }
     * </pre>
     * if "1+1" is selected, the code is converted to 
     * <pre>
     * int foo() {
     *    return foo(1+1);
     * }
     * 
     * int foo(int newparam) {
     *    return newparam;
     * }
     * </pre>
     * 
     * @return true if a new overloading method will be created, false otherwise
     * 
     * @since 1.28
     */
    public boolean isOverloadMethod() {
        return overloadMethod;
    }

    /**
     * Create a new overloading method with the new parameter and let the existing
     * method delegate to it:
     * e.g.
     * <pre>
     * int foo() {
     *   return 1+1;
     * }
     * </pre>
     * if "1+1" is selected, the code is converted to 
     * <pre>
     * int foo() {
     *    return foo(1+1);
     * }
     * 
     * int foo(int newparam) {
     *    return newparam;
     * }
     * </pre>
     * 
     * @param overloadMethod true if you want to create a new overloading method
     * 
     * @since 1.28
     */
    public void setOverloadMethod(boolean overloadMethod) {
        this.overloadMethod = overloadMethod;
    }
    
    // INNER CLASSES
    /**
     * Represents one item for setParameters(List params) list parameter.
     * Item contains information about changes in method parameters.
     * Parameter can be added, changed or moved to another position.
     */
    public static final class ParameterInfo {
        /**
         * Return the type of the given parameter as it appears in the source code.
         *
         * @param info the relevant {@code CompilationInfo}
         * @param parameter the parameter for which the type should be detected
         * @return the parameter type as it appears in the source code
         * @since 1.92
         */
        public static String parameterTypeFromSource(CompilationInfo info, VariableElement parameter) {
            VariableTree parTree = (VariableTree) info.getTrees().getTree(parameter);
            ExecutableElement method = (ExecutableElement) parameter.getEnclosingElement();
            int index = method.getParameters().indexOf(parameter);
            if (method.isVarArgs() && index == method.getParameters().size() - 1) {
                return parTree.getType().toString().replace("[]", "..."); // NOI18N
            } else {
                return parTree.getType().toString();
            }
        }

        int origIndex;
        String name;
        String type;
        String defaultVal;

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
        public ParameterInfo(int origIndex, String name, String type, String defaultVal) {
            // new parameter
            // if (origIndex == -1 && (name == null || defaultVal == null || type == null || name.length() == 0 || defaultVal.length() == 0)) {
            //    throw new IllegalArgumentException(NbBundle.getMessage(ChangeParameters.class, "ERR_NoValues"));
            // }
            this.origIndex = origIndex;
            this.name = name;
            this.type = type;
            // do not set default value for existing parameters
            this.defaultVal = origIndex == -1 ? defaultVal : null;
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
        public String getName() { return name; }

        /**
         * Returns value of the type of parameter. If the name was not
         * changed, returns null.
         *
         * @return new type for parameter or null if it was not changed.
         */
        public String getType() { return type; }

        /**
         * Returns value of the default value in case of the new parameter.
         * Otherwise, it returns null.
         *
         * @return default value for new parameter, otherwise null.
         */
        public String getDefaultValue() { return defaultVal; }
    }
    
}
