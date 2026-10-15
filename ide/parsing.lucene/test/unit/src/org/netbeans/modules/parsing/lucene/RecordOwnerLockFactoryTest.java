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
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
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

    public void testClosedLockIsDetected() throws IOException {
        Directory dir = new ByteBuffersDirectory();
        Lock lock = lockFactory.obtainLock(dir, "test");
        assertNotNull(lock);
        lock.close();
        boolean ioExceptionRecorded = false;
        try {
            lock.ensureValid();
            assertFalse("Lock was not invalidated", true);
        } catch (IOException ex) {
            ioExceptionRecorded = true;
        }
        assertTrue("IOException was expected but not thrown", ioExceptionRecorded);
    }

    public void testLockDetectsDuplicate() throws IOException {
        Directory dir = new ByteBuffersDirectory();
        Lock lock1 = lockFactory.obtainLock(dir, "test"); //NOI18N
        assertNotNull(lock1);
        lock1.ensureValid();
        boolean ioExceptionRecorded = false;
        try {
            Lock lock2 = lockFactory.obtainLock(dir, "test"); //NOI18N
        } catch (IOException ex) {
            ioExceptionRecorded = true;
        }
        assertTrue("IOException was expected but not thrown", ioExceptionRecorded);
        lock1.close();
    }

    public void testClearLock() throws IOException {
        Directory dir = new ByteBuffersDirectory();
        Lock lock = lockFactory.obtainLock(dir, "test");
        assertNotNull(lock);
        lock.ensureValid();
        lock.close();
        assertTrue(lockFactory.forceClearLocks().isEmpty());
    }

    public void testForceClearLocks() throws IOException {
        Directory dir = new ByteBuffersDirectory();
        Lock lock = lockFactory.obtainLock(dir, "test1");
        lock.ensureValid();
        assertTrue(lockFactory.hasLocks());
        Lock lock2 = lockFactory.obtainLock(dir, "test2");
        lock2.ensureValid();
        Lock lock3 = lockFactory.obtainLock(dir, "test3");
        lock3.ensureValid();
        lock2.close();
        lock3.close();
        assertTrue(lockFactory.hasLocks());
        Collection<? extends RecordOwnerLockFactory.DirectoryLockPair> locks = lockFactory.forceClearLocks();
        assertEquals(1, locks.size());
        assertEquals(new RecordOwnerLockFactory.DirectoryLockPair(dir, "test1"), locks.iterator().next());
        assertFalse(lockFactory.hasLocks());
    }

}
