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

package org.netbeans.modules.refactoring.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * This class is just holder for parameters of Multiple Copy Refactoring. 
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class MultipleCopyRefactoring extends AbstractRefactoring {

    private Lookup target;

    /**
     * Public constructor takes Lookup containing objects to refactor as parameter.
     * Multiple Copy Refactoring Refactoring currently does not have any implementation.
     * @param objectsToCopy store your objects into Lookup
     */
    public MultipleCopyRefactoring (@NonNull Lookup objectsToCopy) {
        super(objectsToCopy);
    }

    /**
     * Target where copy should be created
     * Multiple Copy Refactoring Refactoring currently does not have any implementation.
     * @param target
     */
    public void setTarget(@NonNull Lookup target) {
        Parameters.notNull("target", target); // NOI18N
        this.target = target;
    }
    
    /**
     * Target where copy should be created
     * Multiple Copy Refactoring Refactoring currently does not have any implementation.
     * @return target
     */
    public @CheckForNull Lookup getTarget() {
        return target;
    }
}


