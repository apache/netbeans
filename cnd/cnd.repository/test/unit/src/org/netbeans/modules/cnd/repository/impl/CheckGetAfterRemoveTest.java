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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.keys.TestLargeKey;
import org.netbeans.modules.cnd.repository.keys.TestSmallKey;
import org.netbeans.modules.cnd.repository.keys.TestValue;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 *
 */
public class CheckGetAfterRemoveTest extends GetPutTestBase {

    public CheckGetAfterRemoveTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(CheckGetAfterRemoveTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetAfterRemove() throws InterruptedException {
        TestSmallKey smallKey = new TestSmallKey("small_1", getUnitID());
        TestLargeKey largeKey = new TestLargeKey("large_1", getUnitID());
        Repository.openUnit(smallKey.getUnitId());
        Repository.openUnit(largeKey.getUnitId());
        _test(smallKey, new TestValue("small_obj_1"));
        _test(largeKey, new TestValue("large_obj_1"));
        Repository.shutdown();
        Repository.startup(0);
        Repository.openUnit(getUnitID());
        _test(smallKey, new TestValue("small_obj_1"));
        _test(largeKey, new TestValue("large_obj_1"));
        _test1(smallKey, new TestValue("small_obj_1"));
        _test1(largeKey, new TestValue("large_obj_1"));
    }
    private final AtomicBoolean readFlag = new AtomicBoolean(false);
    private volatile CountDownLatch writeLatch;

    @Override
    public void onReadHook() {
        readFlag.set(true);
    }

    @Override
    public void onWriteHook() {
        sleep(1000);
        writeLatch.countDown();
    }

    @Override
    public void onRemoveHook() {
    }
    
    private void _test(Key key, TestValue value) throws InterruptedException {
        writeLatch = new CountDownLatch(1);
        Repository.startup(0);
        Repository.put(key, value);

        Persistent v2 = Repository.get(key);

        assertNotNull(v2);
        assertEquals(value, v2);

        writeLatch.await();
        long time = System.currentTimeMillis();
        readFlag.set(false);
        Repository.remove(key);
        while ((v2 = Repository.get(key)) != null) {
            assertFalse("get shouldn't cause reading object from disk after remove", readFlag.get());
            assertNotNull(v2);
            assertEquals(value, v2);
            if (System.currentTimeMillis() - time > 30000) {
                break;
            }
        }
        assertFalse("get shouldn't cause reading object from disk after remove", readFlag.get());
//        RepositoryTestUtils.debugClear();
    }

    private void _test1(Key key, TestValue value) throws InterruptedException {
        writeLatch = new CountDownLatch(1);
        Repository.startup(0);
        Repository.put(key, value);
        Repository.shutdown();
        Repository.startup(0);
        Repository.openUnit(getUnitID());
        
        Persistent v2 = Repository.get(key);

        assertNotNull(v2);
        assertEquals(value, v2);

        writeLatch.await();
        long time = System.currentTimeMillis();
        readFlag.set(false);
        Repository.remove(key);
        while ((v2 = Repository.get(key)) != null) {
            assertFalse("get shouldn't cause reading object from disk after remove", readFlag.get());
            assertNotNull(v2);
            assertEquals(value, v2);
            if (System.currentTimeMillis() - time > 30000) {
                break;
            }
        }
        assertFalse("get shouldn't cause reading object from disk after remove", readFlag.get());
//        RepositoryTestUtils.debugClear();
    }
}
