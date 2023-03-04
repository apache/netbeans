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

package org.openide.filesystems;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 * URL mapper is often invoked from inside the lookup. That is why
 * it needs to be ready to survive strange states.
 *
 * Trying to mimic IZ 44365.
 *
 * @author Jaroslav Tulach
 */
public class URLMapperLookupTest extends NbTestCase {

    public URLMapperLookupTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.URLMapperLookupTest$Lkp");
        
        super.setUp();
        
        assertEquals ("Our lookup is registered", Lkp.class, org.openide.util.Lookup.getDefault().getClass());
    }
    
    public void testIfIAskForAnItemThatAsksURLMapperAndThenAskOnceMoreAllMappersAreAsked () 
    throws Exception {
        Object found = org.openide.util.Lookup.getDefault().lookup (QueryingPair.class);
        assertNotNull (found);
        
        MyUM.queried = null;
        java.net.URL url = new java.net.URL ("http://www.netbeans.org");
        URLMapper.findFileObject(url);
        
        assertEquals ("Really got the query thru", url, MyUM.queried);
    }

    /** This is a pair that as a part of its instanceOf method queries the URL resolver.
     */
    private static class QueryingPair extends org.openide.util.lookup.AbstractLookup.Pair {
        public boolean beBroken;
        
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
                    assertNull ("This query did not get thru", MyUM.queried);
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
     

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private static org.openide.util.lookup.InstanceContent ic;
        
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

            
            qp.beBroken = true;
        }

    } // end of Lkp
}
