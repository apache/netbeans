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
package org.netbeans.modules.openide.loaders;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class UIExceptionTest extends NbTestCase {

    public UIExceptionTest(String name) {
        super(name);
    }

    public void testSynchronization() {

        class TestException extends Exception {

            private final Semaphore s;

            public TestException(Semaphore s) {
                this.s = s;
            }

            @Override
            public synchronized Throwable getCause() {
                s.release();
                return super.getCause();
            }

            @Override
            public synchronized Throwable initCause(Throwable cause) {
                try {
                    s.tryAcquire(2, 100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                s.release(2);
                return super.initCause(cause);
            }
        }

        Semaphore s = new Semaphore(0);
        final TestException te = new TestException(s);
        final Exception[] e = new Exception[1];

        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    UIException.annotateUser(te, "msg", "locMsg", null, null);
                } catch (Exception ex) {
                    e[0] = ex;
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
        r.run();
        try {
            t.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertNull("No exception should be thrown", e[0]);
    }
}
