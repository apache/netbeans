/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
