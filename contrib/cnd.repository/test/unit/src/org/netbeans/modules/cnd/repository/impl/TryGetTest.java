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
package org.netbeans.modules.cnd.repository.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.keys.TestLargeKey;
import org.netbeans.modules.cnd.repository.keys.TestSmallKey;
import org.netbeans.modules.cnd.repository.keys.TestValue;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 * Tests Repository.tryGet()
 *
 */
public class TryGetTest extends GetPutTestBase {

    public TryGetTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(TryGetTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testTryGet() {
        _test(new TestSmallKey("small_1", getUnitID()), new TestValue("small_obj_1"));
        _test(new TestLargeKey("large_1", getUnitID()), new TestValue("large_obj_1"));

    }
    private final AtomicBoolean readFlag = new AtomicBoolean(false);

    @Override
    public void onReadHook() {
        readFlag.set(true);
    }

    @Override
    public void onWriteHook() {
    }

    @Override
    public void onRemoveHook() {
    }
    
    private void _test(Key key, TestValue value) {

        Repository.startup(0);
        Repository.put(key, value);

        Persistent v2 = Repository.get(key);
        assertNotNull(v2);
        assertEquals(value, v2);

        readFlag.set(false);

//        RepositoryTestUtils.debugClear();

        //v2 = _tryGet(key);
        //assertNull(v2);
    }
}
