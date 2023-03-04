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
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

/** Interface implemented by refactoring plugins. Contains callback methods which a particular refactoring
 * calls to check pre-conditions, validity of parameters and collect refactoring elements.
 * It is expected that the refactoring that this plugin operates on is passed to the plugin
 * in its constructor by a corresponding implementation of {@link RefactoringPluginFactory}.
 *
 * @author Martin Matula
 */
public interface RefactoringPlugin {
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    Problem preCheck();
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    Problem checkParameters();
    
    /** Fast checks parameters of the refactoring. This method will be used for 
     * online error checking.
     * @return Problems found or null (if no problems were identified)
     */
    Problem fastCheckParameters();
    
    
    /**
     * Asynchronous request to cancel ongoing long-term request (such as preCheck(), checkParameters() or prepare())
     */
    void cancelRequest();
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements RefactoringElementsBag of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    Problem prepare(RefactoringElementsBag refactoringElements);
}
