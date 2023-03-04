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

import javax.swing.Action;
import org.openide.util.Cancellable;

/**
 * Typical implementation will invoke UI component on showDetails() call
 * This UI component will display ProblemDetails. There will be a button, or
 * similar UI control, which will be connected to rerunRefactoringAction to
 * invoke refactoring again once the Problem is fixed.
 * @author Jan Becicka
 * @since 1.5.0
 */
public interface ProblemDetailsImplementation {

    /**
     * This method will typically invoke component with ProblemDetails.
     * It is fully upon clients, how this component will be implemented.
     * @param rerunRefactoringAction this action is passed to client component
     * @param parent parent component, than can be closed by cancel method.
     * to allow clients to rerun refactoring once the Problem is fixed.
     */
    void showDetails(Action rerunRefactoringAction, Cancellable parent);

    /**
     * Message that will be displayed in parameters panel as a hint to suggest user,
     * that there are more details available.
     * @return string representation of details hint
     */
    String getDetailsHint();
    
}
