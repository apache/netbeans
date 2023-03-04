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


import javax.swing.Action;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest2 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest2(String name) {
        super(name);
    }
    
    public void testCanFindSomeActionUsingSettings() throws Exception {
        twiddle(m2, TWIDDLE_ENABLE);
        try {
            assertTrue("Some instance of Action with name 'SomeAction' found in lookup after module installation",
                existsSomeAction(Action.class));
        } finally {
            ERR.log("Before twidle disable");
            twiddle(m2, TWIDDLE_DISABLE);
            ERR.log("After twidle disable");
        }
        assertTrue("The action was removed from lookup after module uninstallation",
            !existsSomeAction(Action.class));
    }
    
}
