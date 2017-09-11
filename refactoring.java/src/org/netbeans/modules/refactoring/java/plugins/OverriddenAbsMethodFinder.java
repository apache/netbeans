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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
