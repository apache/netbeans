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

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;

/**
 * Compatibility test for all MetadataModel implementations. Subclasses
 * should override createModel() to return a ready or non-ready model.
 *
 * @author Andrei Badea
 */
public class MetadataModelCompatibilityTest extends NbTestCase {

    public MetadataModelCompatibilityTest(String name) {
        super(name);
    }

    /**
     * Return a metadata model to be tested for compatibility. This method
     * will only be called once for each test case.
     */
    protected MetadataModel<?> createModel(boolean ready) {
        return MetadataModelFactory.createMetadataModel(new SimpleMetadataModelImpl<Void>(ready));
    }

    /**
     * Called after <code>createModel()</code> with a <code>ready</code>
     * parameter equal to true to make the model become ready.
     */
    protected void makeReady(MetadataModel<?> model) {
        // not needed here, but needed by subclasses
    }

    public void testUncheckedExceptionsPropagatedWhenModelReady() throws IOException {
        doTestUncheckedExceptionsPropagatedWhenReady(createModel(true));
    }

    public void testCheckedExceptionsWrappedWhenModelReady() throws IOException {
        doTestCheckedExceptionsWrappedWhenModelReady(createModel(true));
    }

    public void testIOExceptionsWrappedWhenModelReady() throws IOException {
        doTestIOExceptionsWrappedWhenModelReady(createModel(true));
    }

    public void testFutureRethrowsUncheckedExceptionsWhenModelNotReady() throws IOException {
        doTestFutureRethrowsUncheckedExceptionsWhenModelNotReady(createModel(false));
    }

    public void testFutureRethrowsCheckedExceptions() throws IOException {
        doTestFutureRethrowsCheckedExceptions(createModel(false));
    }

    public void testFutureRethrowsIOExceptions() throws IOException {
        doTestFutureRethrowsIOExceptions(createModel(false));
    }

    private <T> void doTestUncheckedExceptionsPropagatedWhenReady(MetadataModel<T> model) throws IOException {
        assertTrue(model.isReady());
        try {
            model.runReadAction(new MetadataModelAction<T, Void>() {
                public Void run(T test) {
                    throw new RuntimeException("foo");
                }
            });
            fail();
        } catch (RuntimeException re) {
            assertEquals("foo", re.getMessage());
        }
        try {
            model.runReadActionWhenReady(new MetadataModelAction<T, Void>() {
                public Void run(T test) {
                    throw new RuntimeException("foo");
                }
            });
            fail();
        } catch (RuntimeException re) {
            assertEquals("foo", re.getMessage());
        }
    }

    private <T> void doTestCheckedExceptionsWrappedWhenModelReady(MetadataModel<T> model) throws IOException {
        assertTrue(model.isReady());
        try {
            model.runReadAction(new MetadataModelAction<T, Void>() {
                public Void run(T test) throws SQLException {
                    throw new SQLException("foo");
                }
            });
            fail();
        } catch (MetadataModelException mme) {
            SQLException cause = (SQLException)mme.getCause();
            assertEquals("foo", cause.getMessage());
        }
        try {
            model.runReadActionWhenReady(new MetadataModelAction<T, Void>() {
                public Void run(T test) throws SQLException {
                    throw new SQLException("foo");
                }
            });
            fail();
        } catch (MetadataModelException mme) {
            SQLException cause = (SQLException)mme.getCause();
            assertEquals("foo", cause.getMessage());
        }
    }

    private <T> void doTestIOExceptionsWrappedWhenModelReady(MetadataModel<T> model) throws IOException {
        assertTrue(model.isReady());
        try {
            model.runReadAction(new MetadataModelAction<T, Void>() {
                public Void run(T test) throws IOException {
                    throw new IOException("foo");
                }
            });
            fail();
        } catch (MetadataModelException mme) {
            IOException cause = (IOException)mme.getCause();
            assertEquals("foo", cause.getMessage());
        }
        try {
            model.runReadActionWhenReady(new MetadataModelAction<T, Void>() {
                public Void run(T test) throws IOException {
                    throw new IOException("foo");
                }
            });
            fail();
        } catch (MetadataModelException mme) {
            IOException cause = (IOException)mme.getCause();
            assertEquals("foo", cause.getMessage());
        }
    }

    private <T> void doTestFutureRethrowsUncheckedExceptionsWhenModelNotReady(MetadataModel<T> model) throws IOException {
        assertFalse(model.isReady());
        Future<Void> future = model.runReadActionWhenReady(new MetadataModelAction<T, Void>() {
            public Void run(T test) {
                throw new RuntimeException("foo");
            }
        });
        makeReady(model); // otherwise future.get() will never finish
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            RuntimeException cause = (RuntimeException)e.getCause();
            assertEquals("foo", cause.getMessage());
        } catch (InterruptedException e) {}
    }

    private <T> void doTestFutureRethrowsCheckedExceptions(MetadataModel<T> model) throws IOException {
        assertFalse(model.isReady());
        Future<Void> future = model.runReadActionWhenReady(new MetadataModelAction<T, Void>() {
            public Void run(T test) throws SQLException {
                throw new SQLException("foo");
            }
        });
        makeReady(model); // otherwise future.get() will never finish
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            SQLException cause = (SQLException)e.getCause();
            assertEquals("foo", cause.getMessage());
        } catch (InterruptedException e) {}
    }

    private <T> void doTestFutureRethrowsIOExceptions(MetadataModel<T> model) throws IOException {
        assertFalse(model.isReady());
        Future<Void> future = model.runReadActionWhenReady(new MetadataModelAction<T, Void>() {
            public Void run(T test) throws IOException {
                throw new IOException("foo");
            }
        });
        makeReady(model); // otherwise future.get() will never finish
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            IOException cause = (IOException)e.getCause();
            assertEquals("foo", cause.getMessage());
        } catch (InterruptedException e) {}
    }
}
