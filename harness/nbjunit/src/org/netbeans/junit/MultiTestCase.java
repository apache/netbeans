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

package org.netbeans.junit;

/**
 *
 * @author Alexander Pepin
 */
public abstract class MultiTestCase extends NbTestCase{
    
    /**
     * Creates a new instance of MultiTestCase.
     * Set the class name as a name of the testcase.  
     */
    public MultiTestCase() {
        super(null);
        setName(this.getClass().getSimpleName());
    }
    
    /**
     * Creates a new instance of MultiTestCase with the given name.
     * @param name name of test case
     */
    public MultiTestCase(String name) {
        super(name);
    }
    
    private Throwable err = null;
    /**
     * Internal method to set an error occured while preparation for executing the testcase.
     */
    void setError(Throwable e){
        err = e;
    }
    
    /**
     * Internal method overriding the method of the TestCase class.
     * @exception Throwable if any exception is thrown
     */
    protected void runTest() throws Throwable {
        if(err != null)
            throw err;
        System.out.println("MultiTestCase:runTest "+getName());
        execute();
    }

    /**
     * Is a method to be executed to perform testing. 
     */
    protected abstract void execute();
    
}
