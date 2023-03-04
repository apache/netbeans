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
package org.netbeans.modules.refactoring.java.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * Introduce parameter refactoring
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see org.netbeans.modules.refactoring.api.AbstractRefactoring
 * @see org.netbeans.modules.refactoring.api.RefactoringSession
 *
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 * 
 * @since 1.28
 */
public final class IntroduceParameterRefactoring extends AbstractRefactoring {
    
    private boolean isFinal;
    private boolean isReplaceAll;
    private boolean overloadMethod;
    private String parameterName;
    
    /**
     * Creates a new instance of introduce parameter refactoring.
     * The element to be refactored can be either an expression, e.g. <pre>a + b</pre>, or
     * an variable declaration, e.g. <pre>String s = "Hello World!";</pre>
     *
     * @param handle the element to be refactored
     */
    public IntroduceParameterRefactoring(@NonNull TreePathHandle handle) {
        super(Lookups.singleton(handle));
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
     */
    public void setOverloadMethod(boolean overloadMethod) {
        this.overloadMethod = overloadMethod;
    }

    /**
     * Make the new Parameter final in the method declaration.
     * e.g.
     * <pre>
     * int foo(final in newparam) {
     *     return newparam;
     * }
     * </pre>
     * @return true if the new parameter will be final, false otherwise
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Make the new Parameter final in the method declaration.
     * e.g.
     * <pre>
     * int foo(final in newparam) {
     *     return newparam;
     * }
     * </pre>
     * @param isFinal true if the new parameter should be final
     */
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Replace all occurrences.
     * e.g.
     * <pre>
     * int foo() {
     *    Sysmte.out.printline(1+1);
     *    return 1+1;
     * }
     * </pre>
     * if "1+1" is selected, the code is converted to 
     * <pre>
     * int foo(int newparam) {
     *    Sysmte.out.printline(newparam);
     *    return newparam;
     * }
     * </pre>
     * 
     * @return true if all occurrences will be changed
     */
    public boolean isReplaceAll() {
        return isReplaceAll;
    }

    /**
     * Replace all occurrences.
     * e.g.
     * <pre>
     * int foo() {
     *    Sysmte.out.printline(1+1);
     *    return 1+1;
     * }
     * </pre>
     * if "1+1" is selected, the code is converted to 
     * <pre>
     * int foo(int newparam) {
     *    Sysmte.out.printline(newparam);
     *    return newparam;
     * }
     * </pre>
     * 
     * @param isReplaceAll true if all occurrences should be changed
     */
    public void setReplaceAll(boolean isReplaceAll) {
        this.isReplaceAll = isReplaceAll;
    }

    /**
     * The name used for the new parameter.
     * @return the name of the new parameter
     */
    public @NonNull String getParameterName() {
        return parameterName;
    }

    /**
     * Change the name to use for the new Parameter.
     * @param parameterName a non empty String to use as the parameter name
     */
    public void setParameterName(@NonNull String parameterName) {
        Parameters.notNull("parameterName", parameterName);
        this.parameterName = parameterName;
    }
}
