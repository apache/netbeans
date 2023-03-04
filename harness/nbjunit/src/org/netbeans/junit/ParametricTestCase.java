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
 * Extension to MultiTestCase class.
 * @author Alexander Pepin
 */
public abstract class ParametricTestCase extends MultiTestCase{
    
    /**
     * Creates a new instance of ParametricTestCase.
     */
    public ParametricTestCase() {
        super();
    }
    
    /**
     * Creates a new instance of ParametricTestCase with the given name.
     * @param name name of test case
     */
    public ParametricTestCase(String name) {
        super(name);
    }
    
    
    /**
     * Is called by ParametricTestSuite before calling <code>execute()</code>.
     * Can be overridden to perform some initializing.
     *
     * @param parameter initializing parameter of type <code>Object</code>.
     */
    protected void parametrize(Object parameter){
    }

}
