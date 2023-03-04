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
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;

/**
 * Finds all subtypes for the given super-type. It goes through the whole
 * inheritance tree, not only direct super-types.
 *
 * @author Martin Janicek
 */
public class FindAllSubtypes extends AbstractFindUsages {

    public FindAllSubtypes(RefactoringElement element) {
        super(element);
    }

    @Override
    protected List<AbstractFindUsagesVisitor> getVisitors(ModuleNode moduleNode, String defClass) {
        return singleVisitor(new FindAllSubtypesVisitor(moduleNode, element));
    }

    
    private static class FindAllSubtypesVisitor extends AbstractFindUsagesVisitor {

        private final ClassNode findingParent;

        
        public FindAllSubtypesVisitor(ModuleNode moduleNode, RefactoringElement element) {
            super(moduleNode);
            assert element.getNode() instanceof ClassNode;
            this.findingParent = (ClassNode) element.getNode();
        }

        @Override
        public void visitClass(final ClassNode node) {
            if (node.isDerivedFrom(findingParent) && !node.equals(findingParent)) {
                usages.add(node);
            }
            super.visitClass(node);
        }
    }
}
