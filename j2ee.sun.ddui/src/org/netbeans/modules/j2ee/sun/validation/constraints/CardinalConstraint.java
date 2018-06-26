/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.sun.validation.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.text.MessageFormat;

import org.netbeans.modules.j2ee.sun.validation.Constants;
import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;

/**
 * CardinalConstraint  is a {@link Constraint} to validate the structure.
 * It implements <code>Constraint</code> interface and extends
 * {@link ConstraintUtils} class.
 * <code>match</code> method of this object returns empty 
 * <code>Collection</code> if the value being validated conforms to the 
 * structure specified by this <code>Constraint</code>; else it returns a 
 * <code>Collection</code> with a {@link ConstraintFailure} object in it.
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class CardinalConstraint extends ConstraintUtils
                implements Constraint{
 
    /**
     * The structure represented by this <code>Constraint</code>.
     */
    private int cardinal =  Constants.MANDATORY_ELEMENT;


    /** Creates a new instance of CardinalConstraint */
    public CardinalConstraint() {
        cardinal = Constants.MANDATORY_ELEMENT;
    }


    /** Creates a new instance of <code>CardinalConstraint</code>. */
    public CardinalConstraint(int cardinal) {
        this.cardinal = cardinal;
    }


    /** Creates a new instance of <code>CardinalConstraint</code>. */
    public CardinalConstraint(String cardinal) {
        try {
           this.cardinal = Integer.parseInt(cardinal);
        } catch(NumberFormatException e) {
            String format = 
                BundleReader.getValue("Error_failed_to_create");        //NOI18N
            Object[] arguments = 
                new Object[]{"CardinalConstraint"};                     //NOI18N
            System.out.println(MessageFormat.format(format, arguments));
        }
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated.
     * This value must be of appropiate type, that is , either an Object or
     * an array of Objects, in consistence with the Cardinal represented
     * by this Object.
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to
     * construct the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty 
     * if there are no failures. This <code>Constraint</code> will
     * fail if the value does not conform to the structure represented
     * by this object.
     */
    public Collection match(String value, String name){
        return match((Object)value, name);
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated.
     * This value must be of appropriate type, that is , either an Object or
     * an array of Objects, in consistence with the Cardinal represented
     * by this Object.
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to
     * construct the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty 
     * if there are no failures. This method will fail if the value does
     * not conform to the structure represented by this object.
     */
    public Collection match(Object value, String name) {
        Collection failed_constrained_list = 
                new ArrayList();

        if(Constants.MANDATORY_ARRAY == cardinal) {
            Object[]  values = null;
            try {
                values =  (Object[])value;
            } catch(ClassCastException exception){
                System.out.println(BundleReader.getValue(
                   "Error_expects_argument_one_to_be_an_array_of_Objects"));//NOI18N
            }

            if((null == values) || (values.length  <  1)){
                String failureMessage = formatFailureMessage(toString(), name);
                failed_constrained_list.add(new ConstraintFailure(toString(),
                    null, name, failureMessage, BundleReader.getValue(
                        "MSG_CardinalConstraint_Failure")));            //NOI18N
            }
        } else {
            if(Constants.MANDATORY_ELEMENT == cardinal){
                if (null == value){
                    String failureMessage =
                        formatFailureMessage(toString(), name);
                    failed_constrained_list.add(
                        new ConstraintFailure(toString(), null, name,
                            failureMessage, BundleReader.getValue(
                                "MSG_CardinalConstraint_Failure")));    //NOI18N
                }
            }
        }
        return failed_constrained_list;
    }


    /**
     * Sets the given cardinal as the structure
     * represented by this object.
     * 
     * @param cardinal the cardinal to be set
     * as the structure represented by this object.
     */
    public void setCardinal(int cardinal){
        this.cardinal = cardinal;
    }


    /**
     * Sets the given cardinal as the structure
     * represented by this object.
     * 
     * @param cardinal the cardinal to be set
     * as the structure represented by this object.
     */
    public void setCardinal(String cardinal){
        try {
           this.cardinal = Integer.parseInt(cardinal);
        } catch(NumberFormatException e) {
            String format = 
                BundleReader.getValue("Error_failed_to_set");           //NOI18N
            Object[] arguments = 
                new Object[]{this.toString(), "Cardinal"};              //NOI18N

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
            new Object[]{"Cardinal", String.valueOf(cardinal)};         //NOI18N
        System.out.println(MessageFormat.format(format, arguments));
    }
}
