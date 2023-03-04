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

package org.netbeans.modules.j2ee.sun.validation.impl;

import junit.framework.*;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class ValidateeImplTest extends TestCase{
    /* A class implementation comment can go here. */

    public ValidateeImplTest(String name){
        super(name);
    }


    public static void main(String args[]){
        junit.textui.TestRunner.run(suite());
    }


    public void testCreate(){
        nyi();
    }


    /**
     * Define suite of all the Tests to run.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(ValidateeImplTest.class);
        return suite;
    }
    
    
    /**
     * Initialize; allocate any resources needed to perform Tests.
     */
    protected void setUp(){
    }
    
    
    /**
     * Free all the resources initilized/allocated to perform Tests.
     */
    protected void tearDown(){
    }
    
    
    private void nyi(){
        ///fail("Not yet implemented");                                 //NOI18N
    }
}
