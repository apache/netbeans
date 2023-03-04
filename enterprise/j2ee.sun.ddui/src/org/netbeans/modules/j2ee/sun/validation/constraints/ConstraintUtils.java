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

import java.text.MessageFormat;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;


/**
 * ConstraintUtils is an <code>Object</code> providing the Utilities.
 * <code>formatFailureMessage</code> methods, are the utility methods
 * to format the failure messages. These Methods are used to format
 * failure messages. Method <code>print</code> is the utility method
 * to print this <code>Object</code>
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
class ConstraintUtils {
    /* A class implementation comment can go here. */

    
    /** Creates a new instance of ConstraintUtils */
    ConstraintUtils() {
    }


    /**
    * Prints this <code>Object</code>
    */
    void print() {
        String format = BundleReader.getValue("Name_Value_Pair_Format");//NOI18N
        Object[] arguments = new Object[]{"Constraint", this};          //NOI18N
        System.out.println(MessageFormat.format(format, arguments));
    }


    /**
    * Formats the failure message from the given information.
    * 
    * @param constraint the failed <code>Constraint</code> name
    * @param value the value the <code>constriant</code> failed for
    * @param name the name of the <code>value</code> the 
    * <code>constriant</code> failed for
    *
    * @return the formatted failure message
    */
    String formatFailureMessage(String constraint, String value,
            String name){
        String failureMessage = null;
        if(!((constraint == null) || (constraint.length() == 0) ||
            (value == null) || (name == null) || (name.length() == 0))){

            String format = BundleReader.getValue("MSG_Failure");       //NOI18N
            Object[] arguments = new Object[]{constraint, value, name};

            failureMessage = MessageFormat.format(format, arguments);
        }
        return failureMessage;
    }


    /**
    * Formats the failure message from the given information.
    * 
    * @param constraint the failed <code>Constraint</code> name
    * @param name the name of the element, the <code>constriant</code>
    * failed for
    *
    * @return the formatted failure message
    */
    String formatFailureMessage(String constraint, String name){
        String failureMessage = null;
        if(!((constraint == null) || (constraint.length() == 0) ||
                (name == null) || (name.length() == 0))){

            String format = BundleReader.getValue("MSG_Failure_1");     //NOI18N
            Object[] arguments = new Object[]{constraint, name};

            failureMessage = MessageFormat.format(format, arguments);
        }
        return failureMessage;
    }
}
