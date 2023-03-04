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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;

/**
 * Validation clients use this interface to start validation on a model.
 * Validator implementation can use this to optimize computing validation results
 * by finding which models are already validated.
 *
 * @author Nam Nguyen
 * @author Praveen Savur
 */
public class Validation {
    
    public Validation() {
      myValidationResult = new ArrayList<ResultItem>();
      myValidatedModels = new ArrayList<Model>();
    }
        
    /**
     * Validates the model.
     * Note: Clients should call this method on a Validation instance only
     * once. The same Validation instance should not be reused.
     *
     * @param model Contains the model for which validation has to be provided.
     * @param type Type of validation: complete or partial.
     */
    public void validate(Model model, ValidationType type) {
        isStopped = false;
        
        if (myValidatedModels.contains(model)) {
            return;
        }
        myValidatedModels.add(model);
//System.out.println();
//System.out.println();

        for (Validator validator : ourValidators) {
            if (isStopped) {
//System.out.println("-- STOPPED !!! --");
                myValidationResult = null;
                myValidatedModels = new ArrayList<Model>();
                break;
            }
//System.out.println("see provider: " + validator.getClass().getName());
            ValidationResult result = validator.validate(model, this, type);

            if (result == null) {
                continue;
            }
//System.out.println("            : errors found");
            myValidationResult.addAll(result.getValidationResult());
            myValidatedModels.addAll(result.getValidatedModels());
        }
//System.out.println();
        List<ResultItem> errors = new LinkedList<ResultItem>();
        List<ResultItem> advices = new LinkedList<ResultItem>();
        List<ResultItem> warnings = new LinkedList<ResultItem>();

        if (myValidationResult == null) {
            myValidationResult = new ArrayList<ResultItem>();
            return;
        }
        for (ResultItem item : myValidationResult) {
          if (item.getType() == ResultType.ERROR) {
            errors.add(item);
          }
          else if (item.getType() == ResultType.ADVICE) {
            advices.add(item);
          }
          else if (item.getType() == ResultType.WARNING) {
            warnings.add(item);
          }
        }
        myValidationResult = new LinkedList<ResultItem>();
        myValidationResult.addAll(warnings);
        myValidationResult.addAll(advices);
        myValidationResult.addAll(errors);
    }
    
    public List<ResultItem> getValidationResult() {
        if (myValidationResult == null) {
            return null;
        }
        return Collections.unmodifiableList(myValidationResult);
    }
    
    public List<Model> getValidatedModels() {
        return Collections.unmodifiableList(myValidatedModels);
    }
    
    public enum ValidationType {
        COMPLETE, PARTIAL
    }
    
    private static void lookupProviders() {
        if (ourValidators!= null) {
            return;
        }
        ourValidators = new ArrayList<Validator>();
        Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(Validator.class));
        
        for (Object object : result.allInstances()) {
            ourValidators.add((Validator) object);
        }
    }

    public static void stop() {
//System.out.println("-- PLEASE STOP --");
        isStopped = true;
    }

    private static Collection<Validator> ourValidators;
    
    static {
        lookupProviders();        
    }
    
    private List<Model> myValidatedModels;
    private List<ResultItem> myValidationResult;
    private static boolean isStopped = false;
}
