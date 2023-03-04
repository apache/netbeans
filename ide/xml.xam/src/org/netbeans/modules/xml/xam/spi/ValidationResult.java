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

package org.netbeans.modules.xml.xam.spi;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *  Represents the results of validation from a validator.
 *
 * @author Praveen Savur
 */
public class ValidationResult {
    
    private Collection<ResultItem> validationResult;
    private Collection<Model> validatedModels;
    
    /**
     * Create an instance of validationResult.
     * @param validationResult ValidationResult
     * @param validatedModels List of models validated.
     */
    public ValidationResult(Collection<ResultItem> validationResult,
            Collection<Model> validatedModels) {
        this.validationResult = validationResult;
        this.validatedModels = validatedModels;
    }
    
    /**
     * Return the Validation Result
     * @return ValidationResult
     */
    public Collection<ResultItem> getValidationResult() {
        return validationResult;
    }
    
    /**
     * Return a list of Validated models.
     * @return Collection of Validated models.
     */
    public Collection<Model> getValidatedModels() {
        return validatedModels;
    }
}
