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

package org.netbeans.test.junit.pkgtestcreation.test;

import java.util.ArrayList;

/**
 *
 * @author ms159439
 */
public class TestClass {
    
    /**
     * Creates a new instance of TestClass
     */
    public TestClass() {
    }
    
    /**
     * public method
     */
    public int add(int a, int b) {
        return a + b;
    }
    
    /**
     * protected method
     */
    protected int arrayListCount(ArrayList a) {
        return a.size();
    }
    
    /**
     * private method
     * should not be included in any test
     */
    private int subs(int a, int b) {
        return a - b;
    }
    
    /**
     * friendly (pacakge private) method
     */ 
    double sqr(double a) {
        return Math.pow(a, 2);
    }
    
    /**
     * static friendly method
     */ 
    static double thirdPow(double a) {
        return Math.pow(a, 3);
    }
}
