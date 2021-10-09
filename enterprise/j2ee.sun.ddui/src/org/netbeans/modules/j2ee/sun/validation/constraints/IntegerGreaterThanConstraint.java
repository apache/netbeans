/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.sun.validation.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.text.MessageFormat;

import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;

/**
 * IntegerGreaterThanConstraint  is a {@link Constraint} to validate numbers
 * between the given value and Integer.MAX_VALUE.
 * It implements <code>Constraint</code> interface and extends
 * {@link ConstraintUtils} class.
 * <code>match</code> method of this object returns empty 
 * <code>Collection</code> if the value being validated is a number between 
 * the given value and  Integer.MAX_VALUE; else it returns a 
 * <code>Collection</code> with a {@link ConstraintFailure} object in it. 
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class IntegerGreaterThanConstraint extends ConstraintUtils 
        implements Constraint {
    
    /**
     * A value represented by this <code>Constraint</code>.
     */
    private int value = -1;

    
    /** Creates a new instance of <code>IntegerGreaterThanConstraint</code>. */
    public IntegerGreaterThanConstraint() {
        value = -1;
    }


    /** Creates a new instance of <code>IntegerGreaterThanConstraint</code>. */
    public IntegerGreaterThanConstraint(String inputValue) {
        try {
           this.value = Integer.parseInt(inputValue);
        } catch(NumberFormatException e) {

            String format = 
                BundleReader.getValue("Error_failed_to_create");        //NOI18N
            Object[] arguments = 
                new Object[]{"IntegerGreaterThanConstraint"};           //NOI18N

            System.out.println(MessageFormat.format(format, arguments));
        }
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param inputValue the value to be validated.
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to construct
     * the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty if 
     * there are no failures. This method will fail, if the given value
     * is not between the value this Constraint represents and Integer.MAX_VALUE.
     */
    public Collection match(String inputValue, String name) {
        ArrayList failed_constrained_list = new ArrayList();
        if((inputValue != null) && (inputValue.length() != 0)){
            try {
                int intValue = Integer.parseInt(inputValue);
                
                if((intValue <= value) || (intValue > Integer.MAX_VALUE)){
                    addFailure(failed_constrained_list, name, inputValue);
                }
            } catch(NumberFormatException e) {
                addFailure(failed_constrained_list, name, inputValue);
            }
        }
        return failed_constrained_list;
    }


    /**
     * Sets the value represented by this object.
     * 
     * @param value the value represented by this object.
     */
    public void setValue(String value){
        try {
           this.value = Integer.parseInt(value);
        } catch(NumberFormatException e) {
            String format = 
                BundleReader.getValue("Error_failed_to_set");           //NOI18N
            Object[] arguments = 
                new Object[]{this.toString(), "Value"};                 //NOI18N

            System.out.println(MessageFormat.format(format, arguments));
        }
    }


    /**
     * Sets the value represented by this object.
     * 
     * @param value the value represented by this object.
     */
    public void setValue(Integer value){
        this.value = value;
    }


    /**
     * Prints this <code>Constraint</code>.
     */
    public void print() {
        super.print();

        String format = BundleReader.getValue("Name_Value_Pair_Format");//NOI18N
        Object[] arguments = 
            new Object[]{"Value", String.valueOf(value)};               //NOI18N
        System.out.println(MessageFormat.format(format, arguments));
    }


    private  void addFailure(Collection failed_constrained_list,
        String name, String inputValue){
        String failureMessage = formatFailureMessage(toString(),
            inputValue, name);

        String format = BundleReader.getValue(
            "MSG_IntegerGreaterThanConstraint_Failure");                //NOI18N
        Object[] arguments = 
            new Object[]{String.valueOf(value)};
        String genericFailureMessage = MessageFormat.format(format, arguments);

        failed_constrained_list.add(new ConstraintFailure(toString(),
            inputValue, name, failureMessage, genericFailureMessage));
    }
}
