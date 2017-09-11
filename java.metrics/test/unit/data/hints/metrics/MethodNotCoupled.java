/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
