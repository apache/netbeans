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

package threaddemo.locking;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test Locks.eventHybrid.
 * @author Jesse Glick
 */
public class EventHybridLockTest extends TestCase {

    public EventHybridLockTest(String name) {
        super(name);
    }

    public void testEventHybrid() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        // test canRead, canWrite, correct thread used by synch write methods
        assertTrue(!Locks.eventHybrid().canRead());
        assertTrue(!Locks.eventHybrid().canWrite());
        assertTrue(Locks.eventHybrid().read(new LockAction<Boolean>() {
            public Boolean run() {
                return !EventQueue.isDispatchThread() &&
                        Locks.eventHybrid().canRead() &&
                        !Locks.eventHybrid().canWrite();
            }
        }));
        assertTrue(Locks.eventHybrid().read(new LockExceptionAction<Boolean,Exception>() {
            public Boolean run() throws Exception {
                return !EventQueue.isDispatchThread() &&
                        Locks.eventHybrid().canRead() &&
                        !Locks.eventHybrid().canWrite();
            }
        }));
        assertTrue(Locks.eventHybrid().write(new LockAction<Boolean>() {
            public Boolean run() {
                return EventQueue.isDispatchThread() &&
                        Locks.eventHybrid().canRead() &&
                        Locks.eventHybrid().canWrite();
            }
        }));
        assertTrue(Locks.eventHybrid().write(new LockExceptionAction<Boolean,Exception>() {
            public Boolean run() throws Exception {
                return EventQueue.isDispatchThread() &&
                        Locks.eventHybrid().canRead() &&
                        Locks.eventHybrid().canWrite();
            }
        }));
        final boolean[] b = new boolean[1];
        // Test that while in AWT randomly, can read but not write.
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                b[0] = EventQueue.isDispatchThread() &&
                       Locks.eventHybrid().canRead() &&
                       !Locks.eventHybrid().canWrite();
            }
        });
        assertTrue(b[0]);
        // While in AWT, can enter read or write directly.
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                b[0] = Locks.eventHybrid().read(new LockAction<Boolean>() {
                    public Boolean run() {
                        return EventQueue.isDispatchThread() &&
                                Locks.eventHybrid().canRead() &&
                                !Locks.eventHybrid().canWrite();
                    }
                });
            }
        });
        assertTrue(b[0]);
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                b[0] = Locks.eventHybrid().write(new LockAction<Boolean>() {
                    public Boolean run() {
                        return EventQueue.isDispatchThread() &&
                                Locks.eventHybrid().canRead() &&
                                Locks.eventHybrid().canWrite();
                    }
                });
            }
        });
        assertTrue(b[0]);
    }
    
    public void testEventHybridLocking() throws Exception {
        // XXX test locking out readers by writer or vice-versa
    }
    
    public void testEventHybridReadToWriteForbidden() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        Locks.eventHybrid().read(new LockAction<Void>() {
            public Void run() {
                assertTrue(!EventQueue.isDispatchThread());
                assertTrue(Locks.eventHybrid().canRead());
                assertTrue(!Locks.eventHybrid().canWrite());
                try {
                    Locks.eventHybrid().write(new LockAction<Void>() {
                        public Void run() {
                            fail();
                            return null;
                        }
                    });
                } catch (IllegalStateException e) {
                    // OK.
                }
                return null;
            }
        });
        final Error[] err = new Error[1];
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    Locks.eventHybrid().read(new LockAction<Void>() {
                        public Void run() {
                            assertTrue(EventQueue.isDispatchThread());
                            assertTrue(Locks.eventHybrid().canRead());
                            assertTrue(!Locks.eventHybrid().canWrite());
                            try {
                                Locks.eventHybrid().write(new LockAction<Void>() {
                                    public Void run() {
                                        fail();
                                        return null;
                                    }
                                });
                            } catch (IllegalStateException e) {
                                // OK.
                            }
                            return null;
                        }
                    });
                } catch (Error e) {
                    err[0] = e;
                }
            }
        });
        if (err[0] != null) throw err[0];
        Locks.eventHybrid().write(new LockAction<Void>() {
            public Void run() {
                assertTrue(Locks.eventHybrid().canWrite());
                Locks.eventHybrid().read(new LockAction<Void>() {
                    public Void run() {
                        assertTrue(Locks.eventHybrid().canRead());
                        assertTrue(!Locks.eventHybrid().canWrite());
                        try {
                            Locks.eventHybrid().write(new LockAction<Void>() {
                                public Void run() {
                                    fail();
                                    return null;
                                }
                            });
                        } catch (IllegalStateException e) {
                            // OK.
                        }
                        return null;
                    }
                });
                return null;
            }
        });
    }
    
    public void testEventHybridWriteToReadPermitted() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        Locks.eventHybrid().write(new LockAction<Void>() {
            public Void run() {
                assertTrue(EventQueue.isDispatchThread());
                try {
                    Locks.eventHybrid().read(new LockAction<Void>() {
                        public Void run() {
                            // OK.
                            return null;
                        }
                    });
                } catch (IllegalStateException e) {
                    fail(e.toString());
                }
                return null;
            }
        });
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Locks.eventHybrid().write(new LockAction<Void>() {
                    public Void run() {
                        assertTrue(EventQueue.isDispatchThread());
                        try {
                            Locks.eventHybrid().read(new LockAction<Void>() {
                                public Void run() {
                                    // OK.
                                    return null;
                                }
                            });
                        } catch (IllegalStateException e) {
                            fail(e.toString());
                        }
                        return null;
                    }
                });
            }
        });
    }
    
    public void testEventHybridPostedRequests() throws Exception {
        // XXX postRR, postWR
    }
    
    public void testEventHybridExceptions() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        // test that checked excs from M.EA throw correct ME
        try {
            Locks.eventHybrid().read(new LockExceptionAction<Void,IOException>() {
                public Void run() throws IOException {
                    throw new IOException();
                }
            });
            fail();
        } catch (IOException e) {
            // OK
        }
        // but that unchecked excs are passed thru
        try {
            Locks.eventHybrid().read(new LockExceptionAction<Void,IOException>() {
                public Void run() throws IOException {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        } catch (IOException e) {
            fail(e.toString());
        }
        // similarly for unchecked excs from M.A
        try {
            Locks.eventHybrid().read(new LockAction<Void>() {
                public Void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        } catch (RuntimeException e) {
            fail(e.toString());
        }
        // and blocking runnables
        try {
            Locks.eventHybrid().read(new Runnable() {
                public void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK.
        }
        try {
            Locks.eventHybrid().write(new Runnable() {
                public void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK.
        }
    }
    
}
