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
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;

/**
 *
 * @author Martin Janicek
 */
public class FindMethodUsages extends AbstractFindUsages {

    public FindMethodUsages(RefactoringElement element) {
        super(element);
    }

    @Override
    protected List<AbstractFindUsagesVisitor> getVisitors(ModuleNode moduleNode, String defClass) {
        return singleVisitor(new FindMethodUsagesVisitor(moduleNode, element));
    }
}
