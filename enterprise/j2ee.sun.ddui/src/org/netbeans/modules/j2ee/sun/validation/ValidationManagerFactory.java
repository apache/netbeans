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

package org.netbeans.modules.j2ee.sun.validation;

import org.netbeans.modules.j2ee.sun.validation.util.ObjectFactory;


/**
 * ValidationManagerFactory is a factory to create {@link ValidationManager}
 * objects. Creates <code>ValidationManager</code> based on the given Validation
 * File.
 *
 * @see ValidationManager
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class ValidationManagerFactory {
    /* A class implementation comment can go here. */

    /** Creates a new instance of <code>ValidationManagerFactory</code> */
    public ValidationManagerFactory(){
    }


    /**
     * Creates default Validation Manager.
     * Default Validation Manager is created using default Validation File
     *
     * @return <code>ValidationManager</code> the default Validation Manager.
     * 
     */
    public ValidationManager getValidationManager(){
        return (ValidationManager) ObjectFactory.newInstance(
            "org.netbeans.modules.j2ee.sun.validation." +               //NOI18N
                "ValidationManager");                                   //NOI18N
    }


    /**
     * Creates Validation Manager based on the given Validation File.
     *
     * @param validationFile the Validation File. Validation File specifies
     * Validation rules(which Constraints to apply to which elements).
     * 
     * @return <code>ValidationManager</code> the Validation Manager based on
     * the given Validation File.
     */
    public ValidationManager getValidationManager(String validationFile){
        return (ValidationManager) ObjectFactory.newInstance(
            "org.netbeans.modules.j2ee.sun.validation." +               //NOI18N
                "ValidationManager", validationFile);                   //NOI18N
    }
}
