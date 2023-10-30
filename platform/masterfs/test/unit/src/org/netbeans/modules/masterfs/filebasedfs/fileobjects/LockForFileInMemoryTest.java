/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import org.netbeans.modules.masterfs.filebasedfs.fileobjects.LockForFile.HardLockRegistry;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.LockForFile.InMemoryHardLockRegistry;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class LockForFileInMemoryTest extends LockForFileTest {

    public LockForFileInMemoryTest(String testName) {
        super(testName);
    }

    @Override
    protected void runTest() throws Throwable {
        Throwable[] exc = new Throwable[1];

        Lookups.executeWith(new ProxyLookup(Lookups.exclude(Lookup.getDefault(), HardLockRegistry.class), Lookups.fixed(new InMemoryHardLockRegistry())), () -> {
            try {
                super.runTest();
            } catch (Throwable t) {
                exc[0] = t;
            }
        });

        if (exc[0] != null) {
            throw exc[0];
        }
    }

}
