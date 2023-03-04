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

package org.netbeans.modules.j2ee.sun.validation.constraints;

import org.netbeans.modules.j2ee.sun.validation.Failure;

/**
 * ConstraintFailure is a Validation failure Object.
 * It provides the following failure information; Constraint failed,
 * the value it failed for; the name of the value it failed for,
 * failure message and the generic failure message.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ConstraintFailure implements Failure{

    /**
     * The name of the failed <code>Constraint</code>.
     */
    private String constraint = null;

    /**
     * The name of the value, the <code>Constraint</code> failed for.
     */
    private String name = null;

    /**
     * The value,  the <code>Constraint</code> failed for.
     */
    private Object value = null;

    /**
     * The failure message.
     */
    private String failureMessage = null;


    /**
     * The generic failure message.
     */
    private String genericFailureMessage = null;


    /** Creates a new instance of ConstraintFailure */
    public ConstraintFailure(String constraint,
        Object value, String name, String failureMessage, 
                String genericFailureMessage) {
            this.constraint =  constraint;
            this.value = value;
            this.failureMessage = failureMessage;
            this.name = name;
            this.genericFailureMessage = genericFailureMessage;
    }


    /**
     * Returns the failed <code>Constraint</code> this Object represents.
     */
    public String getConstraint(){
        return constraint;
    }


    /**
     * Returns the value failed for the <code>Constraint</code>
     * represented by this Object.
     */
    public Object getFailedValue(){
        return value;
    }


    /**
     * Returns an failure message for this failure.
     */
    public String failureMessage(){
        return failureMessage;
    }


    /**
     * Returns the name of the value failed for the 
     * <code>Constraint</code> represented by this Object.
     */
    public String getName(){
        return name;
    }


    /**
     * Returns generic message for this failure.
     */
    public String getGenericfailureMessage(){
        return genericFailureMessage;
    }
}
