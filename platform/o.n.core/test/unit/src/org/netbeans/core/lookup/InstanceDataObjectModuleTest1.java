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

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest1 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest1(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        //System.setProperty("org.netbeans.core.modules", "0");
        TestRunner.run(new NbTestSuite(InstanceDataObjectModuleTest1.class));
    }
    
    public void testCanFindSomeActionUsingDotInstance() throws Exception {
        twiddle(m1, TWIDDLE_ENABLE);
        try {
            assertTrue("Some instance of Action with name 'SomeAction' found in lookup after module installation",
                existsSomeAction(Action.class));
        } finally {
            twiddle(m1, TWIDDLE_DISABLE);
        }
        assertTrue("The action was removed from lookup after module uninstallation",
            !existsSomeAction(Action.class));
    }
    
}
