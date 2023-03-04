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

package org.openide.util.lookup;

import java.util.*;
import org.netbeans.junit.*;
import org.openide.util.Lookup;

public class InitializationBug44134Test extends NbTestCase {
    public InitializationBug44134Test (java.lang.String testName) {
        super(testName);
    }

    public void testThereShouldBe18Integers () throws Exception {
        FooManifestLookup foo = new FooManifestLookup ();

        Collection items = foo.lookup (new Lookup.Template (Integer.class)).allItems ();

        assertEquals ("18 of them", 18, items.size ());

        Iterator it = items.iterator ();
        while (it.hasNext()) {
            Lookup.Item t = (Lookup.Item)it.next ();
            assertEquals ("Is Integer", Integer.class, t.getInstance ().getClass ());
        }
    }


    public class FooManifestLookup extends AbstractLookup {
        public FooManifestLookup() {
            super();
        }

        @Override
        protected void initialize() {
            for (int i=0; i<18; i++) {
                try {
                    String id= "__" + i;

                    addPair(new FooLookupItem(new Integer(i),id));
                }
                catch (Exception e) {
                }
            }
        }

        public class FooLookupItem extends AbstractLookup.Pair {
            public FooLookupItem(Integer data, String id) {
                super();
                this.data=data;
                this.id=id;
            }

            protected boolean creatorOf(Object obj) {
                return obj == data;
            }

            public String getDisplayName() {
                return data.toString();
            }

            public Class getType () {
                return Integer.class;
            }

            protected boolean instanceOf (Class c) {
                return c.isInstance(data);
            }

            public Object getInstance() {
                return data;
            }

            public String getId() {
                return id;
            }

            private Integer data;
            private String id;
        }
    }

}
