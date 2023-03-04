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

import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.openide.util.Cancellable;
import org.openide.util.Parameters;

/**
 * This class holds details of a Problem
 * @author Jan Becicka
 * @since 1.5.0
 */
public final class ProblemDetails {

    private ProblemDetailsImplementation pdi;

    ProblemDetails (ProblemDetailsImplementation pdi) {
        this.pdi=pdi;
    }

    /**
     * This method will typically invoke component with ProblemDetails.
     * It is fully upon clients, how this component will be implemented.
     * @param rerunRefactoringAction this action is passed to client component
     * to allow clients to invoke refactoring once the Problem is fixed.
     * @param parent component, which can be cancelled
     * @see ProblemDetailsImplementation
     */
    public void showDetails(@NonNull Action rerunRefactoringAction, @NonNull Cancellable parent) {
        Parameters.notNull("rerunRefactoringAction", rerunRefactoringAction); // NOI18N
        Parameters.notNull("parent", parent); // NOI18N
        pdi.showDetails(rerunRefactoringAction, parent);
    }
    
    /**
     * Message that will be displayed in parameters panel as a hint to suggest user,
     * that there are more details available.
     * @return string representation of hint
     */
    @NonNull
    public String getDetailsHint() {
        return pdi.getDetailsHint();
    }
}
