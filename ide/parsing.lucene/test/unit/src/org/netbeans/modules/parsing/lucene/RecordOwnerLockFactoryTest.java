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

package org.netbeans.modules.parsing.lucene;


import java.io.IOException;
import java.util.Collection;
import org.apache.lucene.store.Lock;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class RecordOwnerLockFactoryTest extends NbTestCase {

    private RecordOwnerLockFactory lockFactory;

    public RecordOwnerLockFactoryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.lockFactory = new RecordOwnerLockFactory();
    }

    public void testLock() throws IOException {
        final Lock lock = lockFactory.makeLock("test"); //NOI18N
        assertFalse(lock.isLocked());
        lock.obtain();
        assertTrue(lock.isLocked());
        lock.release();
        assertFalse(lock.isLocked());
    }

    public void testLockInstances() throws IOException {
        final Lock lock1 = lockFactory.makeLock("test"); //NOI18N
        final Lock lock2 = lockFactory.makeLock("test"); //NOI18N
        assertFalse(lock1.isLocked());
        assertFalse(lock2.isLocked());
        lock1.obtain();
        assertTrue(lock1.isLocked());
        assertTrue(lock2.isLocked());
        lock2.release();
        assertFalse(lock1.isLocked());
        assertFalse(lock2.isLocked());
    }

    public void testClearLock() throws IOException {
        Lock lock = lockFactory.makeLock("test"); //NOI18N
        assertFalse(lock.isLocked());
        lock.obtain();
        assertTrue(lock.isLocked());
        lockFactory.clearLock("test");  //NOI18N
        assertFalse(lock.isLocked());
    }

    public void testHasLocks() throws IOException {
        assertFalse(lockFactory.hasLocks());
        final Lock lock1 = lockFactory.makeLock("test1");   //NOI18N
        final Lock lock2 = lockFactory.makeLock("test2");   //NOI18N
        final Lock lock3 = lockFactory.makeLock("test3");   //NOI18N
        final Lock lock4 = lockFactory.makeLock("test4");   //NOI18N
        assertFalse(lockFactory.hasLocks());
        assertTrue(lock2.obtain());
        assertTrue(lockFactory.hasLocks());
        lock2.release();
        assertFalse(lockFactory.hasLocks());
        assertTrue(lock3.obtain());
        assertTrue(lockFactory.hasLocks());
        assertTrue(lock4.obtain());
        assertTrue(lockFactory.hasLocks());
        lockFactory.clearLock("test3"); //NOI18N
        assertTrue(lockFactory.hasLocks());
        assertTrue(lock2.obtain());
        lockFactory.clearLock("test4"); //NOI18N
        assertTrue(lockFactory.hasLocks());
        lock2.release();
        assertFalse(lockFactory.hasLocks());
    }

    public void testForceClearLocks() throws IOException {
        final Lock lock1 = lockFactory.makeLock("test1");   //NOI18N
        assertTrue(lock1.obtain());
        assertTrue(lockFactory.hasLocks());
        lockFactory.makeLock("test2");  //NOI18N
        assertTrue(lockFactory.makeLock("test3").obtain()); //NOI18N
        lockFactory.makeLock("test3").release();    //NOI18N
        assertTrue(lockFactory.hasLocks());
        Collection<? extends Lock> locks = lockFactory.forceClearLocks();
        assertEquals(1, locks.size());
        assertEquals(lock1, locks.iterator().next());
        assertFalse(lockFactory.hasLocks());
    }

}
