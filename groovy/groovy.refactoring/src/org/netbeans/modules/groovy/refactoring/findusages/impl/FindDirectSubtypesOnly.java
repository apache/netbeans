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

package org.netbeans.modules.groovy.refactoring.findusages.impl;

import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.modules.groovy.editor.api.ASTUtils.FakeASTNode;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;

/**
 * Find only direct subtypes for the given declaration class.
 *
 * @author Martin Janicek
 */
public class FindDirectSubtypesOnly extends AbstractFindUsages {

    public FindDirectSubtypesOnly(RefactoringElement element) {
        super(element);
    }

    @Override
    protected List<AbstractFindUsagesVisitor> getVisitors(ModuleNode moduleNode, String defClass) {
        return singleVisitor(new FindDirectSubtypesOnlyVisitor(moduleNode, defClass));
    }


    private static class FindDirectSubtypesOnlyVisitor extends AbstractFindUsagesVisitor {

        private final String findingFqn;

        
        public FindDirectSubtypesOnlyVisitor(ModuleNode moduleNode, String findingFqn) {
            super(moduleNode);
            this.findingFqn = findingFqn;
        }

        @Override
        public void visitClass(ClassNode clazz) {
            if (findingFqn.equals(ElementUtils.getTypeName(clazz.getSuperClass()))) {
                // Oh my goodness I have absolutely no idea why the hack getSuperClass() doesn't return valid initiated superclass
                // and the method with a weird name getUnresolvedSuperClass(false) is actually returning resolved super class (with
                // line/column numbers set)
                usages.add(new FakeASTNode(clazz.getUnresolvedSuperClass(false), clazz.getSuperClass().getNameWithoutPackage()));
            }
            for (ClassNode interfaceNode : clazz.getInterfaces()) {
                if (findingFqn.equals(ElementUtils.getTypeName(interfaceNode))) {
                    usages.add(new FakeASTNode(ElementUtils.getType(interfaceNode), ElementUtils.getTypeName(interfaceNode)));
                }
            }
            super.visitClass(clazz);
        }
    }
}
