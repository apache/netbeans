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
 *
 * @author Alexander Pepin
 */
public abstract class MultiTestSuite extends NbTestSuite{
    
    /**
     * Creates a new instance of MultiTestSuite
     */
    public MultiTestSuite() {
        setName(this.getClass().getSimpleName());
    }
    
    /**
     * Constructs a MultiTestSuite with the given name.
     * @param name name of the test suite
     */
    public MultiTestSuite(String name){
        super(name);
    }
    
    /**
     * Factory method returns a new instance of a testcases.
     * Should return null if there are no more testcases to be executed.
     * @return test case
     */
    protected abstract MultiTestCase nextTestCase();
    
    /**
     * Runs the tests and collects their result in a TestResult.
     * @param result collector for result
     */
    public void run(TestResult result) {
        if(isPrepared()){
            runAllTests(result);
            cleanit();
        }
        if(gotFailed())
            createFailLog(result);
    }
    
    /**
     * Creates all testcases and runs them.
     * @param result collector for result
     */
    protected void runAllTests(TestResult result){
        MultiTestCase testCase = null;
        while((testCase = nextTestCase()) != null){
            runTest(testCase, result);
        }
    }
    
    //stubs
    
    /**
     * The method is called before executing tests.
     * Can be overridden to perform preliminary actions.
     */
    public void prepare(){}
    /**
     * The method is called after executing tests.
     * Can be overridden to perform closing actions.
     */
    public void cleanup(){}
    
//Safe preparation and cleaning
    private Throwable err = null;
    
    private final boolean isPrepared(){
        boolean result = false;
        try{
            prepare();
            result = true;
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while preparing for test "+getName()+": "+e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    private final void cleanit(){
        try{
            cleanup();
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while cleaning after test "+getName()+": "+e.toString());
            e.printStackTrace();
        }
    }
    
    private final boolean gotFailed(){
        return err != null;
    }
    
    private final void createFailLog(TestResult result){
        //Create a new test case
        final String nameFailed = getName()+"FailLog";
        MultiTestCase dummy = new MultiTestCase(){
            public void execute(){}
        };
        dummy.setName(nameFailed);
        dummy.setError(err);
        runTest(dummy, result);
    }
    
}
