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
package org.netbeans.modules.refactoring.spi;

import java.util.Collection;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;

/** Interface implemented by guarded block refactoring handlers. Contains a callback method
 * that gets a RefactoringElementImplementation affecting a guarded block as a parameter and can return
 * the new RefactoringElementImplementation that will replace the passed RefactoringElementImplementations
 * in the result collection of refactoring elements for a given refactoring.
 *
 * @author Martin Matula
 */
public interface GuardedBlockHandler {
    /** Collects replacements for refactoring element affecting a guarded block.
     * @param proposedChange RefactoringElementImplementation that affects a guarded block.
     * @param replacements Empty collection where the method implementation should add the
     * replacement RefactoringElementImplementations if this GuardedBlockHandler can handle changes in the
     * guarded block the original RefactoringElementImplementation affects.
     * @param transaction collection of Transactions. 
     *
     * @return Problems found or null (if no problems were identified)
     */
    Problem handleChange(RefactoringElementImplementation proposedChange, Collection<RefactoringElementImplementation> replacements, Collection<Transaction> transaction);
}
