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

package org.netbeans.modules.refactoring.java.plugins;

import java.util.Collection;
import java.util.HashSet;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Bharath Ravi Kumar
 */
final class OverriddenAbsMethodFinder implements CancellableTask<CompilationController>{

    private final HashSet<ElementHandle<ExecutableElement>> allMethods;
    private Problem problem;
    
    OverriddenAbsMethodFinder(HashSet<ElementHandle<ExecutableElement>> allMethods) {
        this.allMethods = allMethods;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void run(CompilationController javac) throws Exception {
        for (ElementHandle<ExecutableElement> method : allMethods) {
            ExecutableElement el = method.resolve(javac);
            Collection<ExecutableElement> overriddenMethods = JavaRefactoringUtils.getOverriddenMethods(el, javac);
            for (ExecutableElement overriddenMethod : overriddenMethods) {
                ElementHandle<ExecutableElement> handle = ElementHandle.create(overriddenMethod);
                if(!allMethods.contains(handle)) {
                    TypeElement type1 = javac.getElementUtilities().enclosingTypeElement(el);
                    TypeElement type2 = javac.getElementUtilities().enclosingTypeElement(overriddenMethod);
                    Problem prob = new Problem(false, NbBundle.getMessage(OverriddenAbsMethodFinder.class, "WRN_Implements", overriddenMethod.getSimpleName(), type1.getQualifiedName(), type2.getQualifiedName()));
                    problem = JavaPluginUtils.chainProblems(problem, prob);
                }
            }
        }
    }

    public Problem getProblem() {
        return problem;
    }

}
