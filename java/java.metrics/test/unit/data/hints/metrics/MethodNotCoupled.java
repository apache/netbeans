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
package test;

import junit.extensions.ActiveTestSuite;
import junit.extensions.RepeatedTest;
import junit.extensions.TestDecorator;
import junit.extensions.TestSetup;
import junit.framework.*;
import junit.runner.BaseTestRunner;
import junit.runner.TestRunListener;
/**
 *
 * @author sdedic
 */
public class MethodNotCoupled extends TestCase {
    /**
     * Goal of this method is to reach >= 15 different types referenced,
     * in various constructions
     */
    // 3 refs
    public TestCase coupledMethod(TestSuite suite) throws ComparisonFailure {
        // +1 = 4 ref to super method, 1 ref to TestResult type
        run(new TestResult());
        
        // +1 = 5 ref to class that declares the constant
        int res = TestRunListener.STATUS_ERROR;
        
        try {
            // no reference, the same outermost element
            switch (m()) {
                // +1 = 6, ONE comes from a different class
                case ONE:
                    break;
                
                case TWO:
                    // +1 = 7
                    throw new CoupledException();
                    
                default:
                    // +1 = 8
                    throw new org.w3c.dom.DOMException(0, null);
            }
        // +1 = 9
        } catch (AssertionFailedError e) {
            
        }
        
        // +1 = 10, for nextElement().select = TestFailure reference
        run().errors().nextElement().exceptionMessage();
        
        // +2 = 13, for array type and the new array expr.
        TestListener[] arr = new BaseTestRunner[1];
        
        // +1 = 14 for this unneeded typecast
        Object o = ((ActiveTestSuite)suite);
        
        // +1 = 15 for instanceof
        assert o instanceof RepeatedTest;
        
        
        // exactly the limit, but add something which is by default ignored:
        Assert.assertEquals(null, null);
        if (o != null) {
            throw new IllegalArgumentException();
        } else {
            java.util.Iterator it;
            throw new IllegalStateException();
        }
        return null;
    }
    
    
    /**
     * Just a helper method used in the coupledMethod; I need
     * something that returns an enum
     */
    CoupledEnum m() {
        return CoupledEnum.ONE;
    }
}
