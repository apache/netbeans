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
package org.openide.filesystems;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.*;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class MIMESupport69049Test extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport69049Test$Lkp");
        Logger logger = Logger.getLogger(MIMESupport.class.getName());
        logger.setLevel(Level.FINEST);
        logger.addHandler(new ErrMgr());
    }

    private Logger LOG;
    
    public MIMESupport69049Test (String testName) {
        super (testName);
    }

    protected Level logLevel() {
        return Level.FINE;
    }

    protected void setUp () throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected void tearDown () throws Exception {
    }

    public void testProblemWithRecursionInIssue69049() throws Throwable {
        Lkp lkp = (Lkp)Lookup.getDefault();
        @SuppressWarnings("unchecked")
        class Pair extends AbstractLookup.Pair implements Runnable {
            public MIMEResolver[] all;
            public MIMEResolver[] all2;
            public Throwable ex;
            public RequestProcessor.Task wait;
            
            
            protected boolean instanceOf(Class c) {
                LOG.info("instanceOf: " + c);
                return c.isAssignableFrom(getType());
            }

            protected boolean creatorOf(Object obj) {
                LOG.info("creatorOf: " + obj);
                return false;
            }

            public Object getInstance() {
                LOG.info("getInstance: " + all);
                if (all == null) {
                    all = MIMESupport.getResolvers();
                    assertNotNull("Computed", all);
                } else {
                    all2 = MIMESupport.getResolvers();
                    assertNotNull("Computed", all2);
                }
                LOG.info("after getInstance: " + all + " and " + all2);
                return null;
            }

            public Class getType() {
                return MIMEResolver.class;
            }

            public String getId() {
                return getType().getName();
            }

            public String getDisplayName() {
                return getId();
            }
            
            public void run() {
                try {
                    LOG.info("running");
                    if (wait != null) wait.waitFinished();
                    all = MIMESupport.getResolvers();
                    LOG.info("finishing");
                } catch (Throwable e) {
                    LOG.log(Level.INFO, "ending with exception", e);
                    ex = e;
                }
            }
            
            public void assertResults() throws Throwable {
                if (ex != null) {
                    throw ex;
                }

                MIMESupportHid.assertNonDeclarativeResolver("c1 is there", Lkp.c1, all);
            }
        }
        
        lkp.turn(Lkp.c1);
        Pair p = new Pair();
        lkp.ic.addPair(p);

        Pair run1 = new Pair();
        Pair run2 = new Pair();


        LOG.info("Starting the tasks");
        RequestProcessor.Task t2 = new RequestProcessor("t2").post(run2, 20, Thread.NORM_PRIORITY);
        run1.wait = t2;
        RequestProcessor.Task t1 = new RequestProcessor("t1").post(run1);
        
        t1.waitFinished();
        t2.waitFinished();
        
        LOG.info("Waiting for the tasks to finish");
        
        assertTrue("t1 done", t1.isFinished());
        assertTrue("t2 done", t2.isFinished());
        
        run1.assertResults();
        run2.assertResults();

        assertNotNull("Been in the query", p.all);
        assertEquals("In query we cannot do better than nothing", 0, p.all.length);
    }
    
    
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private ErrMgr err = new ErrMgr();
        private org.openide.util.lookup.InstanceContent ic;
        static MIMEResolver c1 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            
            public String toString() {
                return "C1";
            }
        };
        static MIMEResolver c2 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            public String toString() {
                return "C2";
            }
        };
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            this.ic = ic;
            
            turn(c1);
        }
        
        public void turn (MIMEResolver c) {
            ArrayList<Object> l = new ArrayList<Object>();
            l.add(err);
            l.add(c);
            ic.set (l, null);
        }
    }
    
    
    private static class ErrMgr extends Handler {
        private boolean block = true;
        
        public synchronized void publish(LogRecord r) {
            String s = r.getMessage();
            if (s.startsWith ("Computing resolvers")) {
                notifyAll();
                if (block) {
                    try {
                        wait(200);
                    } catch (InterruptedException ex) {
                        fail("Wrong exception");
                    }
                }
            }
            
            if (s.startsWith("Resolvers computed")) {
                block = false;
                notifyAll();
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }

    }
    
}
