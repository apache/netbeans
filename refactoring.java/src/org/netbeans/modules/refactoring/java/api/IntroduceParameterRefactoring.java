/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
