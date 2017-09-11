/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
        lock2.release();;
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
