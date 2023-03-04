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
/*
 * ValidationSupport.java
 *
 * Created on November 11, 2003, 10:59 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;
import org.netbeans.modules.j2ee.sun.validation.ValidationManager;
import org.netbeans.modules.j2ee.sun.validation.ValidationManagerFactory;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ValidationSupport {

    private final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); // NOI18N
    
    private ValidationManager validationManager;


    /** Creates a new instance of ValidationSupport */
    public ValidationSupport() {
        ValidationManagerFactory validationManagerFactory = 
            new ValidationManagerFactory();
        validationManager = validationManagerFactory.getValidationManager();
    }


    public Collection validate(String value, String xpath, String label){
        ArrayList errors = new ArrayList();

        Collection failures = 
            validationManager.validateIndividualProperty(value,xpath,label);

        if(failures != null){
            Iterator iterator = failures.iterator();
            ConstraintFailure failure;
            String error;

            while(iterator.hasNext()){
                Object object  = iterator.next();

                if(object instanceof ConstraintFailure){
                    failure = (ConstraintFailure)object;
                    error = failure.getName() + ": " +                  //NOI18N
                        failure.getGenericfailureMessage();             
                    errors.add(error);
                }
            }
       }
        return errors;
    }


    /**
     * Returns true if the given xpath represents mandatory field
     * 
     * @param xpath the given xpath.
     *
     * @return <code>boolean</code> <code>true</code> if the given xpath is 
     * of mandatory field; else returns <code>false</code>
     */
    public boolean isRequiredProperty(String xpath){
        boolean isRequired = false;
        String property = ""; //NOI18N
        java.util.Collection errors = validate(property, xpath, null);
        if(!errors.isEmpty()){
            isRequired = true;
        }
        return isRequired;
    }


    /**
     * Returns marked-label for the given label. Marked labels are used in case
     * of madatory fields.
     * 
     * @param label the given label
     *
     * @return <code>String</code> the marked label. Marked label is formed by 
     * appending "*  " to the given field.
     */
    public String getMarkedLabel(String label){
        String format = bundle.getString("FMT_Required_Field_Label");   //NOI18N
        String requiedMark = bundle.getString("LBL_RequiredMark");      //NOI18N
        Object[] arguments = new Object[]{requiedMark, label};  
        return MessageFormat.format(format, arguments);
    }
}
