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

import java.util.ArrayList;
import java.util.Collection;
import java.text.MessageFormat;

import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;

/**
 * RangeConstraint  is a {@link Constraint} to validate a
 * numeric element against the given range.
 * It implements <code>Constraint</code> interface and extends
 * {@link ConstraintUtils} class.
 * <code>match</code> method of this object returns an empty
 * <code>Collection</code> if the value being validated belongs to the range
 * represented by this <code>Constraint</code>; else it returns a
 * <code>Collection</code> with a {@link ConstraintFailure} object in it.
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class RangeConstraint extends ConstraintUtils
                implements Constraint {

    /**
     * A start value of the range represented by 
     * this <code>Constraint</code>.
     */
    private Double startValue = null;

    /**
     * An end value of the range represented by
     * this <code>Constraint</code>.
     */
    private Double endValue = null;


    /** Creates a new instance of RangeConstraint */
    public RangeConstraint() {
        startValue = null;
        endValue = null;
    }


    /** Creates a new instance of <code>RangeConstraint</code>. */
    public RangeConstraint(String startValue, String endValue) {
        try {
           this.startValue = new Double(startValue);
           this.endValue = new Double(endValue);
        } catch(NumberFormatException e) {
            String format = 
                BundleReader.getValue("Error_failed_to_create");        //NOI18N
            Object[] arguments = 
                new Object[]{"RangeConstaint"};                         //NOI18N

            System.out.println(MessageFormat.format(format, arguments));
        }
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated.
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to
     * construct the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty
     * if there are no failures. This method will fail, if the given value
     * is non-numeric or its numeric & does not belong to the range 
     * represented by this object.
     */
    public Collection match(String value, String name) {
        ArrayList failed_constrained_list = new ArrayList();
        
        if((startValue == null) || (endValue == null)) {
            return failed_constrained_list; 
        }
        
        if((value != null) && (value.length() != 0)){
            try {
                Double val = new Double(value);
                if((val.compareTo(startValue) < 0) ||
                    (val.compareTo(endValue) > 0)){
                    addFailure(failed_constrained_list, name, value);
                }
            } catch(NumberFormatException e) {
                addFailure(failed_constrained_list, name, value);            }
        }
        return failed_constrained_list;
    }


    /**
     * Sets the start value of the range represented by this object.
     * 
     * @param value the value to be set as the start value of the 
     * range represented by this object.
     */
    public void setRangeStart(String value){
        try {
           startValue = new Double(value);
        } catch(NumberFormatException e) {
            String format = 
                BundleReader.getValue("Error_failed_to_set");           //NOI18N
            Object[] arguments = 
                new Object[]{this.toString(), "Range Start"};           //NOI18N

            System.out.println(MessageFormat.format(format, arguments));
        }
    }


    /**
     * Sets the end value of the range represented by this object.
     * 
     * @param value the value to be set as the end value of the
     * range represented by this object.
     */
    public void setRangeEnd(String value){
        try {
           endValue = new Double(value);
        } catch(NumberFormatException e) {
            String format = 
                BundleReader.getValue("Error_failed_to_set");           //NOI18N
            Object[] arguments = 
                new Object[]{this.toString(), "Range End"};             //NOI18N

            System.out.println(MessageFormat.format(format, arguments));
        }
    }


    /**
     * Prints this <code>Constraint</code>.
     */
    public void print() {
        super.print();
        
        String format = BundleReader.getValue("Name_Value_Pair_Format");//NOI18N
        Object[] arguments = 
            new Object[]{"Range Start", startValue};                    //NOI18N
        System.out.println(MessageFormat.format(format, arguments));

        arguments = new Object[]{"Range End", endValue};                //NOI18N
        System.out.println(MessageFormat.format(format, arguments));
    }


    /**
     * Sets the start value of the range represented by this object.
     * 
     * @param value the value to be set as the start value of the 
     * range represented by this object.
     */
    public void setRangeStart(Double value){
           startValue = value;
    }


    /**
     * Sets the end value of the range represented by this object.
     * 
     * @param value the value to be set as the end value of the
     * range represented by this object.
     */
    public void setRangeEnd(Double value){
        endValue = value;
    }
    
    
    private  void addFailure(Collection failed_constrained_list,
        String name, String value){
        String failureMessage = formatFailureMessage(toString(), value,  name);
        
        String format = BundleReader.getValue(
            "MSG_RangeConstraint_Failure");                             //NOI18N
        String range = startValue + " - " + endValue;
        Object[] arguments = new Object[]{range};
        String genericFailureMessage = 
            MessageFormat.format(format, arguments);

        failed_constrained_list.add(new ConstraintFailure(toString(),
            value, name, failureMessage, genericFailureMessage));
    }
}
