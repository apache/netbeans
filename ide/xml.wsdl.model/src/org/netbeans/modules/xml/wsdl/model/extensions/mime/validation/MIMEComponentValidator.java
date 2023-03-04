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

package org.netbeans.modules.xml.wsdl.model.extensions.mime.validation;

import java.util.HashSet;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author jyang
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class MIMEComponentValidator implements Validator {

    /** Creates a new instance of SOAPComponentValidator */
    private WSDLModel mModel;

    public MIMEComponentValidator() {
    }

    /**
     * Returns name of this validation service.
     */
    public String getName() {
        return getClass().getName();
    }

    public WSDLModel getWSDLModel() {
        return mModel;
    }

    /**
     * Validates given model.
     *
     * @param model model to validate.
     * @param validation reference to the validation context.
     * @param validationType the type of validation to perform
     * @return ValidationResult.
     */
    public ValidationResult validate(Model model, Validation validation,
            ValidationType validationType) {

        // Traverse the model
        if (model instanceof WSDLModel) {

            WSDLModel wsdlModel = (WSDLModel) model;
            mModel = wsdlModel;
            if (model.getState() == State.NOT_WELL_FORMED) {
                return null;
            }

            WSIAPValidator wsiValidator = new WSIAPValidator(this, mModel);
            wsiValidator.validate();
            List<ResultItem> resultItems = wsiValidator.getResultItems();
            HashSet<Model> models = new HashSet<Model>();
            models.add(model);
            return new ValidationResult(resultItems, models);
        }
        return null;
    }
}
