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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
