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
import org.openide.util.Parameters;

/** Class used to represent problems encountered when performing
 * various refactoring calls. Problems can be chained (using setNext method)
 * - every problem can point to the following problem.
 *
 * @author Martin Matula
 */
public final class Problem {
    private final boolean fatal;
    private final String message;
    private Problem next = null;
    private ProblemDetails details;

    /** Creates new instance of Problem class.
     * @param fatal Indicates whether the problem is fatal.
     * @param message Textual description of the problem.
     */
    public Problem(boolean fatal, @NonNull String message) {
        this(fatal, message, null);
    }
    
    /** Creates new instance of Problem class.
     * @param fatal Indicates whether the problem is fatal.
     * @param message Textual description of the problem.
     * @param details Problem details
     * @see ProblemDetails
     */
    public Problem(boolean fatal, @NonNull String message, ProblemDetails details) {
        Parameters.notNull("message", message); // NOI18N
        this.fatal = fatal;
        this.message = message;
        this.details = details;
    }
    
    /** Indicates whether the problem is fatal.
     * @return <code>true</code> if the problem is fatal, otherwise returns <code>false</code>.
     */
    public boolean isFatal() {
        return fatal;
    }
    
    /** Returns textual description of the problem.
     * @return Textual description of the problem.
     */
    @NonNull
    public String getMessage() {
        return message;
    }
    
    /** Returns the following problem (or <code>null</code> if there none).
     * @return The following problem.
     */
    @CheckForNull
    public Problem getNext() {
        return next;
    }
    
    /**
     * Sets the following problem. The problem can be set only once - subsequent
     * attempts to call this method will result in IllegalStateException.
     * @param next The following problem.
     * @throws java.lang.IllegalStateException subsequent attempts to call this method will result in IllegalStateException.
     */
    public void setNext(@NonNull Problem next) throws IllegalStateException {
        Parameters.notNull("next", next); // NOI18N
        if (this.next != null) {
            throw new IllegalStateException("Cannot change \"next\" property of Problem."); //NOI18N
        }
        this.next = next;
    }

    /**
     * Getter for ProblemDetails
     * @return instance of ProblemDetails or null
     */
    @CheckForNull
    public ProblemDetails getDetails() {
        return details;
    }
}
