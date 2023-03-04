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

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;


/**
 * Common interface for validation services on models and components.
 * Typical implementation would implement a domain-specific subtype and publish
 * the implementation using {@link org.openide.util.lookup.ServiceProvider}.
 * Typical client would lookup and select applicable services for the validation
 * target model.
 *
 * @author Nam Nguyen
 * @author Ritesh
 * @author Praveen Savur
 */

public interface Validator {
    
    /**
     * Returns name of this validation service.
     * @return Name of the validator.
     */
    String getName();
    
    /**
     * Validates given model.
     * @return ValidationResult.
     * @param validationType Type of validation. Complete(slow) or partial(fast). 
     * @param model model to validate.
     * @param validation reference to the validation context.
     */
    ValidationResult validate(Model model, Validation validation, ValidationType validationType);
    
    public enum ResultType {
        ADVICE, WARNING, ERROR
    }
    
    public class ResultItem {
        private Validator validator;
        private ResultType type;
        private Component component;
        private String description;
        private int lineNumber;
        private int columnNumber;
        private Model model;
        
        /**
         * Constructor to create an instance of ResultItem
         * @param validator Reference to validator.
         * @param type Type of message.
         * @param component Component to which this resultItem points.
         * @param desc Message text string.
         */
        public ResultItem(Validator validator, ResultType type, Component component, String desc) {
          this(validator, type, desc, component, -1, -1, null);
        }         

        /**
         * Constructor to create an instance of ResultItem
         * @param validator Reference to validator.
         * @param type Type of message.
         * @param desc Message text string.
         * @param lineNumber Line number where this error happens.
         * @param columnNumber Column Number where this error happens.
         * @param model Model on which this is reported.
         */
        public ResultItem(Validator validator, ResultType type, String desc, int lineNumber, int columnNumber, Model model) {
          this(validator, type, desc, null, lineNumber, columnNumber, model);
        }    
        
        private ResultItem(Validator validator, ResultType type,
          String desc, Component component, int lineNumber, int columnNumber, Model model)
        {
          this.validator = validator;
          this.type = type;
          this.description = desc;
          this.lineNumber = lineNumber;
          this.columnNumber = columnNumber; 
          this.component = component;

          if (model != null) {
            this.model = model;
          }
          else {
            this.model = (component == null) ? null : component.getModel();
          }
        }    
        
        /**
         * Get the validator which generated this error.
         * @return The validator that generated this ResultItem.
         */
        public Validator getValidator() {
            return validator;
        }
        
        /**
         * Returns type of validation result.
         * @return Type of message. Advice/Warning or Error.
         */
        public ResultType getType() {
            return type;
        }
        
        /**
         * Returns target component of the validation result.
         * @return Component on which this validation result is reported.
         * Return value can be null if the model is non-well formed, in this case
         * use line/column numbers.
         * Either getComponents() or getLineNumber/getColumnNumber() will be valid.
         */
        public Component getComponents() {
            return component;
        }
        
        /**
         * Returns description of the validation result item.
         * @return Message describing advice/warning or error.
         */
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Line position of advice/warning/error.
         * @return Line number on which this ResultItem was reported on.
         * Use Component if line number is -1.
         * Either getComponents() or getLineNumber/getColumnNumber() will be valid.
         */
        public int getLineNumber() {
            return lineNumber;
        }
        
        /**
         * Column position of advice/warning/error.
         * @return Column number on which this ResultItem was reported on.
         * Use Component if column number is -1.
         * Either getComponents() or getLineNumber/getColumnNumber() will be valid.
         */
        public int getColumnNumber() {
            return columnNumber;
        }
        
        /**
         * Model on which this ResultItem was reported on.
         * @return Model
         */
        public Model getModel() {
            return model;
        }
    }
}
