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

package org.netbeans.junit;

import junit.framework.TestResult;

/**
 * Extension to MultiTestSuite class.
 * Performs automatic creation and running all testcases predefined with parameters.
 * @author Alexander Pepin */
public abstract class ParametricTestSuite extends MultiTestSuite{
    
    /**
     * Creates a new instance of ParametricTestSuite
     */
    public ParametricTestSuite() {
        super();
    }
    
    /**
     * Constructs a ParametricTestSuite with the given name.
     * @param name name of the suite
     */
    public ParametricTestSuite(String name){
        super(name);
    }
    
    /**
     * Returns an array of testcases for the given parameter.
     * @param parameter parametre to retrieve the test case frome
     * @return array of testcases
     */
    protected abstract ParametricTestCase[] cases(Object parameter);
    /**
     * Returns an array of parameters for this suite.
     * @return array of parameters
     */
    protected abstract Object[] getParameters();
    
    /**
     * Factory method returns a new instance of a testcases.
     * Overrides the basic method so that it's needless any more.
     * @return test case
     */
    protected final MultiTestCase nextTestCase(){
        return null;
    }
    
    /**
     * Creates all testcases and runs them.
     */
    protected void runAllTests(TestResult result){
        for(Object parameter: getParameters()){
            for(ParametricTestCase testCase: cases(parameter)){
                if(testCase != null){
                    testCase.parametrize(parameter);
                    runTest(testCase, result);
                }
            }
        }
    }
    
}

