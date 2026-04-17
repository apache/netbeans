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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.io.File;

import junit.framework.*;

import org.netbeans.modules.j2ee.sun.validation.Constants;
import org.netbeans.modules.j2ee.sun.validation.util.Utils;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class BundleReaderTest extends TestCase{
    /* A class implementation comment can go here. */

    public BundleReaderTest(String name){
        super(name);
    }


    public static void main(String args[]){
        junit.textui.TestRunner.run(suite());
    }


    public void testGetValue() {
        String str = BundleReader.getValue("non_existing_key");         //NOI18N
        assertTrue(str.equals("non_existing_key"));                     //NOI18N
        str = BundleReader.getValue("MSG_NumberConstraint_Failure");    //NOI18N
        assertTrue(!str.equals("MSG_NumberConstraint_Failure"));        //NOI18N
    }


    public void testCreate() {
        String bundleFile = "org/netbeans/modules/" +                   //NOI18N
            "j2ee/sun/validation/Bundle.properties";                    //NOI18N
        Utils utils = new Utils();
        boolean fileExists = utils.fileExists(bundleFile);
        String str = 
            BundleReader.getValue("MSG_NumberConstraint_Failure");      //NOI18N
        
        if(fileExists){
            assertTrue(!str.equals("MSG_NumberConstraint_Failure"));    //NOI18N
        } else {
            assertTrue(str.equals("MSG_NumberConstraint_Failure"));     //NOI18N
        }
    }


    /**
     * Define suite of all the Tests to run.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(BundleReaderTest.class);
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
        //fail("Not yet implemented");                                 //NOI18N
    }
}
