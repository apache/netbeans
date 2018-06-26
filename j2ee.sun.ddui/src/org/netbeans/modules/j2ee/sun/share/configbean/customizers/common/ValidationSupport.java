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
