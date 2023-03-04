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

package org.netbeans.modules.groovy.refactoring;

import java.util.Collection;
import org.netbeans.modules.groovy.refactoring.RefactoringTaskFactory.RefactoringType;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Janicek
 */
@ServiceProvider(service = ActionsImplementationProvider.class, position=100)
public class RefactoringActionsProvider extends ActionsImplementationProvider {

    @Override
    public boolean canFindUsages(Lookup lookup) {
        return canBeDone(lookup, RefactoringType.FIND_USAGES);
    }

    @Override
    public boolean canRename(Lookup lookup) {
        return canBeDone(lookup, RefactoringType.RENAME);
    }

    @Override
    public boolean canMove(Lookup lookup) {
        return false;
        //return canBeDone(lookup, RefactoringType.MOVE);
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        createTask(lookup, RefactoringType.FIND_USAGES).run();
    }

    @Override
    public void doRename(Lookup lookup) {
        createTask(lookup, RefactoringType.RENAME).run();
    }

    @Override
    public void doMove(Lookup lookup) {
        createTask(lookup, RefactoringType.MOVE).run();
    }

    private boolean canBeDone(Lookup lookup, RefactoringType type) {
        RefactoringTask task = createTask(lookup, type);
        return isValid(lookup) && task != null && task.isValid();
    }

    private RefactoringTask createTask(Lookup lookup, RefactoringType type) {
        return RefactoringTaskFactory.createRefactoringTask(lookup, type);
    }

    private boolean isValid(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }

        Node node = nodes.iterator().next();
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return false;
        }

        if (GroovyProjectUtil.isGroovyFile(dataObject.getPrimaryFile())) {
            return true;
        }
        return false;
    }
}
