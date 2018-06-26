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

package org.netbeans.modules.j2ee.sun.validation.util;

import junit.framework.*;
import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;

import java.util.ArrayList;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class DisplayTest extends TestCase{
    /* A class implementation comment can go here. */

    public DisplayTest(String name){
        super(name);
    }


    public static void main(String args[]){
        junit.textui.TestRunner.run(suite());
    }


    public void testText() {
        CustomDisplay display = new CustomDisplay();

        
        ArrayList failureMessages = new ArrayList();
        ConstraintFailure failure_abc = 
            new ConstraintFailure("abc failed", "value_abc",            //NOI18N
                 "name_abc", "failureMessage_abc",                      //NOI18N
                    "genericFailureMessage_abc");                       //NOI18N
        ConstraintFailure failure_xyz = 
            new ConstraintFailure("constraint_xyz", "value_xyz",        //NOI18N
                "name_xyz", "failureMessage_xyz",                       //NOI18N
                    "genericFailureMessage_xyz");                       //NOI18N
        failureMessages.add(failure_abc);
        failureMessages.add(failure_xyz);
        display.text(failureMessages);

        //test to make sure text() reports error, if the Collection it is 
        //processing has objects that are not of type Failure.
        ArrayList failures = new ArrayList();
        failures.add(new Integer(5));
        failures.add("failure_message");                                //NOI18N
        display.text(failures);
    }


    /**
     * Define suite of all the Tests to run.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(DisplayTest.class);
        return suite;
    }


    /**
     * Initialize; allocate any resources needed to perform Tests.
     */
    protected void setUp() {
    }


    /**
     * Free all the resources initilized/allocated to perform Tests.
     */
    protected void tearDown() {
    }


    private void nyi() {
        ///fail("Not yet implemented");                                 //NOI18N
    }
    
    class CustomDisplay extends Display
    {
        CustomDisplay(){
            super();
        }

        protected void reportFailure(String message){
            assertTrue((message.equals("failureMessage_abc"))           //NOI18N
                    ||(message.equals("failureMessage_xyz")));          //NOI18N
        }

        protected void reportError(Object object){
            Class classObject = object.getClass();
            String objectType = classObject.getName();
            assertTrue((objectType.equals("java.lang.Integer"))         //NOI18N
                    ||(objectType.equals("java.lang.String")));         //NOI18N
        }
    }
}
