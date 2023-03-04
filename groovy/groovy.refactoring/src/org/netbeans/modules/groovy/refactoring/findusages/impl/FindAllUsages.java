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

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;

/**
 * This strategy is used in refactoring other then Find Usages. 
 *
 * {@link FindTypeUsages} doesn't find all usages in all files (e.g. if we are 
 * finding usages for a specific class type, we don't want to see constructor in
 * the result, but on the other hand these are pretty important for rename refactoring).
 *
 * Because of that we need to have two different implementation. One for FindUsages
 * itself and the second one as a base used by other refactoring types.
 * 
 * @author Martin Janicek
 */
public class FindAllUsages extends AbstractFindUsages {

    public FindAllUsages(RefactoringElement element) {
        super(element);
    }


    @Override
    protected List<AbstractFindUsagesVisitor> getVisitors(ModuleNode moduleNode, String defClass) {
        List<AbstractFindUsagesVisitor> visitors = new ArrayList<AbstractFindUsagesVisitor>();

        visitors.add(new FindTypeUsagesVisitor(moduleNode, defClass));
        visitors.add(new FindConstructorUsagesVisitor(moduleNode, element));
        visitors.add(new FindClassDeclarationVisitor(moduleNode, defClass));

        return visitors;
    }
}
