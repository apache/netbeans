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

package org.netbeans.core.lookup;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

import javax.swing.Action;
import org.openide.loaders.DataObject;
import org.openide.cookies.InstanceCookie;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest3 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest3(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        //System.setProperty("org.netbeans.core.modules", "0");
        TestRunner.run(new NbTestSuite(InstanceDataObjectModuleTest3.class));
    }
    
    public void testReloadChangesInstance() throws Exception {
        twiddle(m1, TWIDDLE_ENABLE);
        try {
            DataObject obj1 = findIt("Services/Misc/inst-1.instance");
            InstanceCookie inst1 = (InstanceCookie)obj1.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance", inst1);
            Action a1 = (Action)inst1.instanceCreate();
            twiddle(m1, TWIDDLE_RELOAD);
            // Make sure the changes take effect?
            Thread.sleep(2000);
            DataObject obj2 = findIt("Services/Misc/inst-1.instance");
            //System.err.println("obj1 == obj2: " + (obj1 == obj2)); // OK either way
            InstanceCookie inst2 = (InstanceCookie)obj2.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance", inst2);
            assertTrue("InstanceCookie changed", inst1 != inst2);
            Action a2 = (Action)inst2.instanceCreate();
            assertTrue("Action changed", a1 != a2);
            assertTrue("Correct action", "SomeAction".equals(a2.getValue(Action.NAME)));
            assertTrue("Old obj invalid or has no instance",
                !obj1.isValid() || obj1.getCookie(InstanceCookie.class) == null);
        } finally {
            twiddle(m1, TWIDDLE_DISABLE);
        }
    }
    
}
