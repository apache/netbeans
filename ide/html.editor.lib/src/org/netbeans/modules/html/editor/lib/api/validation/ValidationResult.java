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

package org.netbeans.modules.html.editor.lib.api.validation;

import java.util.Collection;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;

/**
 *
 * @author marekfukala
 */
public final class ValidationResult {

    private boolean success;
    private Validator validator;
    private ValidationContext context;
    private Collection<ProblemDescription> problems;

    public ValidationResult(Validator validator, ValidationContext context, Collection<ProblemDescription> problems, boolean success) {
        this.validator = validator;
        this.problems = problems;
        this.success = success;
        this.context = context;
    }

    /**
     * Get the value of success
     *
     * @return the value of success
     */
    public boolean isSuccess() {
        return success;
    }

    public String getValidatorName() {
        return validator.getValidatorName();
    }

    public Collection<ProblemDescription> getProblems() {
        return problems;
    }

    public ValidationContext getContext() {
        return context;
    }

}
