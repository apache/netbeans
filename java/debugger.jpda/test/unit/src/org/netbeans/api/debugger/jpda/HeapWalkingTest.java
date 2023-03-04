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

package org.netbeans.api.debugger.jpda;

import java.util.List;
import junit.framework.Test;

import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.junit.NbTestCase;

/**
 * The test of heap walking functionality - retrieval of classes, instances and back references.
 * 
 * @author Martin Entlicher
 */
public class HeapWalkingTest extends NbTestCase {
    
    private JPDASupport     support;
    
    /** Creates a new instance of HeapWalkingTest */
    public HeapWalkingTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        return JPDASupport.createTestSuite(HeapWalkingTest.class);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        JPDASupport.removeAllBreakpoints ();
        LineBreakpoint lb = LineBreakpoint.create (
                Utils.getURL(System.getProperty ("test.dir.src")+
                             "org/netbeans/api/debugger/jpda/testapps/HeapWalkApp.java"),
                62
            );
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.HeapWalkApp"
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
    }
    
    public void testClasses () throws Exception {
        List<JPDAClassType> allClasses = support.getDebugger().getAllClasses();
        boolean foundHeapWalkApp = false;
        boolean foundMultiInstanceClass = false;
        System.out.println("All Classes size = "+allClasses.size());
        for (JPDAClassType type : allClasses) {
            //System.out.println("Have class: '"+type.getName()+"'");
            if (type.getName().equals("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp")) {
                foundHeapWalkApp = true;
            }
            if (type.getName().equals("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass")) {
                foundMultiInstanceClass = true;
            }
        }
        assertTrue("The class HeapWalkApp was not found!", foundHeapWalkApp);
        assertTrue("The class MultiInstanceClass was not found!", foundMultiInstanceClass);
        
        List<JPDAClassType> hClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp");
        assertEquals("HeapWalkApp classes bad number: ", 1, hClasses.size());
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        assertEquals("HeapWalkApp classes bad number: ", 1, mClasses.size());
    }

    public void testInstances () throws Exception {
        if (!support.getDebugger().canGetInstanceInfo()) {
            System.out.println("Can not retrieve instance information! Test is skipped.");
            return ; // Nothing to test
        }
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        JPDAClassType mClass = mClasses.get(0);
        assertEquals("Bad instance count: ", mClass.getInstanceCount(), 10);
        List<ObjectVariable> instances = mClass.getInstances(0);
        assertEquals("Bad number of instances: ", instances.size(), 10);
        long[] mClassesInstanceCounts = support.getDebugger().getInstanceCounts(mClasses);
        assertEquals("Bad number of instances: ", mClassesInstanceCounts[0], 10L);
        for (ObjectVariable instance : instances) {
            assertEquals("The class type differs: ", instance.getClassType(), mClass);
        }
    }
    
    public void testBackReferences() throws Exception {
        if (!support.getDebugger().canGetInstanceInfo()) {
            System.out.println("Can not retrieve instance information! Test is skipped.");
            return ; // Nothing to test
        }
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        JPDAClassType mClass = mClasses.get(0);
        List<ObjectVariable> instances = mClass.getInstances(0);
        List<ObjectVariable> referrers = instances.get(0).getReferringObjects(0);
        assertEquals("Bad number of referrers: ", referrers.size(), 1);
        
        List<JPDAClassType> hClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp");
        ObjectVariable hInstance = hClasses.get(0).getInstances(0).get(0);
        ObjectVariable var = referrers.get(0);
        while (var.getUniqueID() != hInstance.getUniqueID()) {
            var = var.getReferringObjects(0).get(0);
            assertNotNull("Object "+hInstance+" not found as a referrer to "+referrers.get(0), var);
        }
    }

}
