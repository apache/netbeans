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

import org.netbeans.modules.refactoring.api.AbstractRefactoring;

/** Factory for a refactoring plugin (implementing {@link RefactoringPlugin} interface).
 * Implementations of this factory can be registered to the lookup. The refactorings
 * then get all the implementations of this interface from the lookup and call createInstance
 * method passing "this" as a parameter.
 *
 * @author Martin Matula
 */
public interface RefactoringPluginFactory {
    /** Creates and returns a new instance of the refactoring plugin or returns
     * null if the plugin is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the plugin should operate on.
     * @return Instance of RefactoringPlugin or null if the plugin is not applicable to
     * the passed refactoring.
     */
    RefactoringPlugin createInstance(AbstractRefactoring refactoring);
}
