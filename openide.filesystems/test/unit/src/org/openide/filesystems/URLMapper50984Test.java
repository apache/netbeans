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

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 * Trying to mimic IZ 50984. Used to create following deadlock:
 *


 "Deadlock processor" daemon prio=1 tid=0x081f3370 nid=0x61b9 in Object.wait() [0x465af000..0x465af610]
    at java.lang.Object.wait(Native Method)
    - waiting on <0x69917db0> (a java.lang.Class)
    at java.lang.Object.wait(Object.java:474)
    at org.openide.filesystems.URLMapper.getInstances(URLMapper.java:223)
    - locked <0x69917db0> (a java.lang.Class)
    at org.openide.filesystems.URLMapper.findFileObject(URLMapper.java:177)
    at org.openide.filesystems.URLMapper50984Test$QueryingPair.instanceOf(URLMapper50984Test.java:117)
    at org.openide.util.lookup.InheritanceTree$1TwoJobs.before(InheritanceTree.java:424)
    at org.openide.util.lookup.InheritanceTree.classToNode(InheritanceTree.java:494)
    at org.openide.util.lookup.InheritanceTree.searchClass(InheritanceTree.java:513)
    at org.openide.util.lookup.InheritanceTree.lookup(InheritanceTree.java:197)
    at org.openide.util.lookup.DelegatingStorage.lookup(DelegatingStorage.java:128)
    at org.openide.util.lookup.AbstractLookup.lookupItem(AbstractLookup.java:314)
    at org.openide.util.lookup.AbstractLookup.lookup(AbstractLookup.java:297)
    at org.openide.filesystems.URLMapper50984Test$1DoubleEntry.run(URLMapper50984Test.java:52)
    at org.openide.util.Task.run(Task.java:136)
    at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:330)
    at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:686)

"main" prio=1 tid=0x0805c248 nid=0x61ad in Object.wait() [0xbfffc000..0xbfffcd98]
    at java.lang.Object.wait(Native Method)
    - waiting on <0x659601e8> (a java.lang.Object)
    at java.lang.Object.wait(Object.java:474)
    at org.openide.util.lookup.AbstractLookup.enterStorage(AbstractLookup.java:102)
    - locked <0x659601e8> (a java.lang.Object)
    at org.openide.util.lookup.AbstractLookup.access$400(AbstractLookup.java:35)
    at org.openide.util.lookup.AbstractLookup$R.allItemsWithoutBeforeLookup(AbstractLookup.java:746)
    at org.openide.util.lookup.AbstractLookup$R.allInstances(AbstractLookup.java:691)
    at org.openide.filesystems.URLMapper.getInstances(URLMapper.java:238)
    at org.openide.filesystems.URLMapper.findFileObject(URLMapper.java:177)
    at org.openide.filesystems.URLMapper50984Test$1DoubleEntry.closeToLookup(URLMapper50984Test.java:61)
    at org.openide.filesystems.URLMapper50984Test.testDeadlockInIssue50984(URLMapper50984Test.java:70)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
    at java.lang.reflect.Method.invoke(Method.java:585)
    at junit.framework.TestCase.runTest(TestCase.java:154)
    at junit.framework.TestCase.runBare(TestCase.java:127)
    at junit.framework.TestResult$1.protect(TestResult.java:106)
    at junit.framework.TestResult.runProtected(TestResult.java:124)
    at junit.framework.TestResult.run(TestResult.java:109)
    at junit.framework.TestCase.run(TestCase.java:118)
    at org.netbeans.junit.NbTestCase.run(NbTestCase.java:119)
    at junit.framework.TestSuite.runTest(TestSuite.java:208)
    at junit.framework.TestSuite.run(TestSuite.java:203)
    at org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner.run(JUnitTestRunner.java:289)
    at org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner.launch(JUnitTestRunner.java:656)
    at org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner.main(JUnitTestRunner.java:558)
 
 
 * @author Jaroslav Tulach
 */
public class URLMapper50984Test extends NbTestCase {    
    public URLMapper50984Test(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.URLMapper50984Test$Lkp");
        
        super.setUp();
        
        assertEquals ("Our lookup is registered", Lkp.class, org.openide.util.Lookup.getDefault().getClass());
    }
    
    public void testDeadlockInIssue50984 () throws Exception {
        class DoubleEntry implements Runnable {
            private org.openide.util.RequestProcessor RP = new org.openide.util.RequestProcessor ("Deadlock processor");
            public Exception e;
            
            public void run () {
                try {
                    if (!RP.isRequestProcessorThread ()) {
                        RP.post (this);
                        synchronized (this) {
                            wait (200);
                        }
                    } else {
                        QueryingPair.beBroken = true;
                        Lookup.getDefault ().lookup (QueryingPair.class);
                    }
                } catch (Exception ex) {
                    this.e = ex;
                }
            }
            
            public void closeToLookup () throws Exception {
                java.net.URL url = new java.net.URL ("http://www.netbeans.org");
                URLMapper.findFileObject(url);
                synchronized (this) {
                    notifyAll ();
                }
            }
        }
        
        DoubleEntry d = new DoubleEntry ();
        Lkp.runnable = d;
        d.closeToLookup ();    
        
        if (d.e != null) {
            throw d.e;
        }
    }

    private static final class MyUM extends URLMapper {
        public static java.net.URL queried;
        
        public org.openide.filesystems.FileObject[] getFileObjects(java.net.URL url) {
            queried = url;
            return null;
        }

        public java.net.URL getURL(org.openide.filesystems.FileObject fo, int type) {
            return null;
        }
    }
    
    /** This is a pair that as a part of its instanceOf method queries the URL resolver.
     */
    @SuppressWarnings("unchecked")
    private static class QueryingPair extends org.openide.util.lookup.AbstractLookup.Pair {
        public static boolean beBroken;
        
        public java.lang.String getId() {
            return getType ().toString();
        }

        public java.lang.String getDisplayName() {
            return getId ();
        }

        public java.lang.Class getType() {
            return getClass ();
        }

       protected boolean creatorOf(java.lang.Object obj) {
            return obj == this;
        }

        protected boolean instanceOf(java.lang.Class c) {
            if (beBroken) {
                beBroken = false;
                try {
                    assertNull ("is still null", MyUM.queried);
                    java.net.URL url = new java.net.URL ("http://www.netbeans.org");
                    URLMapper.findFileObject(url);
                    fail("Lookup is not reentrant so this line should never be called.");
                } catch (java.net.MalformedURLException ex) {
                    ex.printStackTrace();
                    fail ("No exceptions: " + ex.getMessage ());
                }
            }
            return c.isAssignableFrom(getType ());
        }

        public java.lang.Object getInstance() {
            return this;
        }
    }
    
     

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private static org.openide.util.lookup.InstanceContent ic;
        static volatile Runnable runnable;
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            this.ic = ic;
        }

        protected void initialize() {
            // a small trick to make the InheritanceTree storage to be used
            // because if the amount of elements in small, the ArrayStorage is 
            // used and it does not have the same problems like InheritanceTree
            for (int i = 0; i < 1000; i++) {
                ic.add (new Integer (i));
            }

            QueryingPair qp = new QueryingPair();
            ic.addPair (qp);
            ic.add (new MyUM ());
        }

        protected void beforeLookup (org.openide.util.Lookup.Template template) {
            Runnable r = runnable;
            runnable = null;
            if (r != null) {
                r.run ();
            }
            super.beforeLookup(template);
        }

    } // end of Lkp
}
