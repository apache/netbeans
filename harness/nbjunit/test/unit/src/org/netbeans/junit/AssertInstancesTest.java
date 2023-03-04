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

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;

/** Checks that we can do proper logging.
 *
 * @author Jaroslav Tulach
 */
public class AssertInstancesTest extends NbTestCase {
    private static Object hold;
    
    private Logger LOG;
    
    public AssertInstancesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
    }

    public void testCannotGC() throws Throwable {
        String s = new String("Ahoj");
        hold = s;
        
        Log.enableInstances(LOG, null, Level.FINEST);
        LOG.log(Level.FINE, "Text", s);
        
        try {
            Log.assertInstances("Cannot GC");
        } catch (AssertionFailedError ex) {
            // ok
            return;
        }
        
        fail("The unreleased reference shall be spotted");
    }

    public void testCannotGCWithAWrongAndRightName() throws Throwable {
        String s = new String("Ahoj");
        hold = s;

        Log.enableInstances(LOG, "NoText", Level.FINEST);
        Log.enableInstances(LOG, "3rdText", Level.FINEST);


        LOG.log(Level.FINE, "3rdText", s);
        LOG.log(Level.FINE, "NoText", new String("OK"));

        Log.assertInstances("OK, GC as nothing holds OK", "NoText");
        try {
            Log.assertInstances("Cannot GC", "3rdText");
        } catch (AssertionFailedError ex) {
            // ok
            return;
        }
        fail("The unreleased reference shall be spotted");
    }
    
    public void testCannotGCWithNameOfMessages() throws Throwable {
        String s = new String("Ahoj");
        hold = s;
        
        Log.enableInstances(LOG, "Text", Level.FINEST);
        LOG.log(Level.FINE, "Text", s);
        
        try {
            Log.assertInstances("Cannot GC");
        } catch (AssertionFailedError ex) {
            // ok
            return;
        }
        
        fail("The unreleased reference shall be spotted");
    }
    
    public void testCannotGCButOKAsNotTrackedMessage() throws Throwable {
        String s = new String("Ahoj");
        hold = s;
        
        Log.enableInstances(LOG, "StrangeMessages", Level.FINEST);
        LOG.log(Level.FINE, "Text", s);
        LOG.log(Level.FINE, "StrangeMessages", new String("some not hold instance"));
        
        Log.assertInstances("Can GC because the object is not tracked");
    }
    
    
    public void testCanGC() throws Throwable {
        String s = new String("Ahoj");
        
        Log.enableInstances(LOG, null, Level.FINEST);
        LOG.log(Level.FINE, "Text", s);
        
        s = null;
        
        Log.assertInstances("Can GC without problems");
    }

    public void testFailIfNoMessage() throws Throwable {
        String s = new String("Ahoj");
        hold = s;
        
        Log.enableInstances(LOG, "StrangeMessages", Level.FINEST);
        
        try {
            Log.assertInstances("Fails as there is no message");
        } catch (AssertionFailedError ex) {
            return;
        }
        
        fail("Shall fails as there is no logged object");
    }
    
}
