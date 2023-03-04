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

import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.Lookup;

public class AbstractLookupArrayStorageTest extends AbstractLookupBaseHid {
    public AbstractLookupArrayStorageTest(java.lang.String testName) {
        super(testName, null);
    }

    public static TestSuite suite () {
        NbTestSuite suite = new NbTestSuite ();
        suite.addTest (new PL (2));
        suite.addTest (new AL (1));
        suite.addTest (new AL (-1));
        suite.addTest (new PL (-1));
        suite.addTest (new AL (5));
        suite.addTest (new PL (3));
        suite.addTest (new AL (2000));
        suite.addTest (new PL (2000));
        return suite;
    }

    static final class AL extends ArrayTestSuite {
        public AL (int trash) {
            super (trash);
        }
        
        public Lookup createLookup (Lookup lookup) {
            return lookup;
        }
        
        public void clearCaches () {
        }
        
    }
    
    static final class PL extends ArrayTestSuite {
        public PL (int trash) {
            super (trash);
        }
        
        public Lookup createLookup (Lookup lookup) {
            return  new ProxyLookup (new Lookup[] { lookup });
        }
        
        public void clearCaches () {
        }
        
    }
    
    private abstract static class ArrayTestSuite extends NbTestSuite 
    implements AbstractLookupBaseHid.Impl {
        private int trash;
        
        public ArrayTestSuite (int trash) {
            super (AbstractLookupArrayStorageTest.class);
            this.trash = trash;
            
            int cnt = this.countTestCases();
            for (int i = 0; i < cnt; i++) {
                Object o = this.testAt (i);
                AbstractLookupBaseHid t = (AbstractLookupBaseHid)o;
                t.impl = this;
            }
        }
        
        public Lookup createInstancesLookup (InstanceContent ic) {
            if (trash == -1) {
                return new AbstractLookup (ic, new ArrayStorage ());
            } else {
                return new AbstractLookup (ic, new ArrayStorage (new Integer (trash)));
            }
        }
        
        
    }
}
