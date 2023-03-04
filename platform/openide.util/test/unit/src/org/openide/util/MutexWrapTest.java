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

package org.openide.util;

import java.util.concurrent.Executor;
import org.netbeans.junit.NbTestCase;


public class MutexWrapTest extends NbTestCase implements Executor {
    Mutex.Privileged p;
    Mutex m;
    ThreadLocal<Object> IN = new ThreadLocal<Object>();

    public MutexWrapTest(java.lang.String testName) {
        super(testName);
    }

    /** Sets up the test.
     */
    @Override
    protected void setUp () {
        p = new Mutex.Privileged ();
        m = new Mutex (p, this);
    }
    
    public void testRead() throws Exception {
        A arg = new A();
        Object ret = m.readAccess(arg);
        assertEquals("Return type is ok", arg, ret);
    }
    public void testWrite() throws Exception {
        A arg = new A();
        Object ret = m.writeAccess(arg);
        assertEquals("Return type is ok", arg, ret);
    }
    public void testExRead() throws Exception {
        E arg = new E();
        Object ret = m.readAccess(arg);
        assertEquals("Return type is ok", arg, ret);
    }
    public void testExWrite() throws Exception {
        E arg = new E();
        Object ret = m.writeAccess(arg);
        assertEquals("Return type is ok", arg, ret);
    }
    public void testRunRead() throws Exception {
        R arg = new R();
        m.readAccess(arg);
        assertTrue("Executed", arg.exec);
    }
    public void testRunWrite() throws Exception {
        R arg = new R();
        m.writeAccess(arg);
        assertTrue("Executed", arg.exec);
    }
    public void testPostRead() throws Exception {
        R arg = new R();
        m.postReadRequest(arg);
        assertTrue("Executed", arg.exec);
    }
    public void testPostReadFromWrite() throws Exception {
        R arg = new R();
        Del del = new Del();
        del.read = arg;
        m.writeAccess(del);
        assertTrue("Executed", arg.exec);
    }
    public void testPostReadFromRead() throws Exception {
        R arg = new R();
        Del del = new Del();
        del.read = arg;
        m.readAccess(del);
        assertTrue("Executed", arg.exec);
    }
    public void testPostWrite() throws Exception {
        R arg = new R();
        m.postWriteRequest(arg);
        assertTrue("Executed", arg.exec);
    }
    public void testPostWriteFromRead() throws Exception {
        R arg = new R();
        Del del = new Del();
        del.write = arg;
        m.readAccess(del);
        assertTrue("Executed", arg.exec);
    }
    public void testPostWriteFromWrite() throws Exception {
        R arg = new R();
        Del del = new Del();
        del.write = arg;
        m.writeAccess(del);
        assertTrue("Executed", arg.exec);
    }
    public void testReadAndRead() throws Exception {
        R arg = new R();
        Del del = new Del();
        del.readNow = arg;
        m.readAccess(del);
        assertTrue("Executed", arg.exec);
    }
    public void testWriteAndRead() throws Exception {
        R arg = new R();
        Del del = new Del();
        del.readNow = arg;
        m.writeAccess(del);
        assertTrue("Executed", arg.exec);
    }
    public void testWriteAndWrite() throws Exception {
        R arg = new R();
        Del del = new Del();
        del.writeNow = arg;
        m.writeAccess(del);
        assertTrue("Executed", arg.exec);
    }
    
    
    private class A implements Mutex.Action<Object> {
        boolean exec;
        
        public Object run() {
            exec = true;
            assertEquals("I am wrapped", MutexWrapTest.this, IN.get());
            return this;
        }
    }
    private class E implements Mutex.ExceptionAction<Object> {
        boolean exec;
        
        public Object run() {
            exec = true;
            assertEquals("I am wrapped", MutexWrapTest.this, IN.get());
            return this;
        }
    }
    private class R implements Runnable {
        boolean exec;
        
        public void run() {
            exec = true;
            assertEquals("I am wrapped", MutexWrapTest.this, IN.get());
        }
    }
    private class Del implements Runnable {
        Runnable write;
        Runnable read;
        Runnable writeNow;
        Runnable readNow;
        
        public void run() {
            if (write != null) {
                m.postWriteRequest(write);
            }
            if (read != null) {
                m.postReadRequest(read);
            }
            if (writeNow != null) {
                m.writeAccess(writeNow);
            }
            if (readNow != null) {
                m.readAccess(readNow);
            }
        }
    }
    public void execute(final Runnable run) {
        Object prev = IN.get();
        assertEquals("No previous value set", null, prev);
        IN.set(MutexWrapTest.this);
        try {
            run.run();
        } finally {
            IN.set(prev);
        }
        assertFalse("No read", m.isReadAccess());
        assertFalse("No write", m.isWriteAccess());
    }

}
