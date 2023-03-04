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

package org.netbeans.performance.j2se.footprints;


import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;


/**
 * Measure Out Of The Box memory fooprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class OutOfTheBox extends MemoryFootprintTestCase {

    public static final String suiteName="J2SE Footprints suite";
    
    
    /**
     * Creates a new instance of OutOfTheBox
     * @param testName the name of the test
     */
    public OutOfTheBox(String testName) {
        super(testName);
        prefix = "Out Of The Box Startup |";
    }

    /**
     * Creates a new instance of OutOfTheBox
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OutOfTheBox(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Out Of The Box Startup |";
    }
    
    public void testMeasureMemoryFootprint() {
        super.testMeasureMemoryFootprint();
    }
    
    public ComponentOperator open(){
        return null;
    }
    
    @Override
    public void setUp() {
        //do nothing
    }
    
    public void prepare() {
    }
    
    @Override
    public void close(){
    }
    
    @Override
    public void shutdown(){
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OutOfTheBox("measureMemoryFooprint"));
    }
    
}
