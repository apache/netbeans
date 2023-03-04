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
import org.openide.util.lookup.AbstractLookup.Pair;

/**
 * Trying to mimic IZ 49418.
 *
 * @author Radek Matous
 */
public class MIMESupport49418Test extends NbTestCase {
    private FileSystem lfs;
    private static FileObject mimeFo;
    private static final String MIME_TYPE = "text/x-opqr";

    public MIMESupport49418Test(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport49418Test$Lkp");
        super.setUp();
        assertEquals("Our lookup is registered", Lkp.class, Lookup.getDefault().getClass());
        lfs = TestUtilHid.createLocalFileSystem(getName(), new String[]{"A.opqr", });
        mimeFo = lfs.findResource("A.opqr");
        assertNotNull(mimeFo);
    }


    public void testMIMEResolution()
            throws Exception {
        assertNull(Lookup.getDefault().lookup(Runnable.class));
        assertEquals(MIME_TYPE, mimeFo.getMIMEType());

    }

    /**
     * This is a pair that as a part of its instanceOf method queries the URL resolver.
     */
    @SuppressWarnings("unchecked")
    private static class QueryingPair extends Pair {
        public boolean beBroken;

        public String getId() {
            return getType().toString();
        }

        public String getDisplayName() {
            return getId();
        }

        public Class getType() {
            return getClass();
        }

        protected boolean creatorOf(Object obj) {
            return obj == this;
        }

        protected boolean instanceOf(Class c) {
            if (beBroken) {
                beBroken = false;
                assertEquals("content/unknown", mimeFo.getMIMEType());

            }
            return c.isAssignableFrom(getType());
        }

        public Object getInstance() {
            return this;
        }
    }


    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private static org.openide.util.lookup.InstanceContent ic;

        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }

        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            this.ic = ic;
        }

        protected void initialize() {
            // a small trick to make the InheritanceTree storage to be used
            // because if the amount of elements in small, the ArrayStorage is 
            // used and it does not have the same problems like InheritanceTree
            for (int i = 0; i < 1000; i++) {
                ic.add(new Integer(i));
            }

            QueryingPair qp = new QueryingPair();
            ic.addPair(qp);
            ic.add(new MIMEResolver() {
                public String findMIMEType(FileObject fo) {
                    return MIME_TYPE;
                }
            });


            qp.beBroken = true;
        }

    } // end of Lkp
}
