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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import static org.junit.Assert.*;

/**
 * Test donated by Mr. Komrska. Seems to pass with 6.5.
 * @author komrska
 */
public final class KomrskaLookupTest {
    private TestLookupManager lookupManager=null;
    private StringBuffer result=null;
    
    //
    
    private void addToLookup(final TestLookupItemA object) {
        result.append('A');
        lookupManager.add(object);
    }
    private void removeFromLookup(final TestLookupItemA object) {
        result.append('A');
        lookupManager.remove(object);
    }

    private void addToLookup(final TestLookupItemB object) {
        result.append('B');
        lookupManager.add(object);
    }
    private void removeFromLookup(final TestLookupItemB object) {
        result.append('B');
        lookupManager.remove(object);
    }
    
    public String getResult() {
        return result.toString();
    }
    
    //

    @Before
    public void setUp() {
        lookupManager=new TestLookupManager();
        result=new StringBuffer();
    }
    
    @After
    public void tearDown() {
        lookupManager=null;
        result=null;
    }
    
    @Test
    public void testLookupBug() {
        TestLookupItemA itemA1=new TestLookupItemA();
        TestLookupItemB itemB1=new TestLookupItemB();
        //
        addToLookup(itemA1);
        addToLookup(itemB1);
        removeFromLookup(itemA1);
        removeFromLookup(itemB1);
        addToLookup(itemB1);
        removeFromLookup(itemB1);
        //
        addToLookup(itemA1);
        addToLookup(itemB1);
        removeFromLookup(itemA1);
        removeFromLookup(itemB1);
        addToLookup(itemB1);
        removeFromLookup(itemB1);
        //
        addToLookup(itemA1);
        addToLookup(itemB1);
        removeFromLookup(itemA1);
        removeFromLookup(itemB1);
        addToLookup(itemB1);
        removeFromLookup(itemB1);
        //
        assertEquals(getResult(),lookupManager.getResult());
    }

    public static final class TestLookupItemA {}
    public static final class TestLookupItemB {}
    public static final class TestLookupManager {
        private InstanceContent instanceContent=new InstanceContent();
        private AbstractLookup abstractLookup=new AbstractLookup(instanceContent);

        private Lookup.Result<TestLookupItemA> resultA=null;
        private Lookup.Result<TestLookupItemB> resultB=null;

        private LookupListener listenerA=new LookupListener() {
            public void resultChanged(LookupEvent event) {
                result.append('A');
            }
        };
        private LookupListener listenerB=new LookupListener() {
            public void resultChanged(LookupEvent event) {
                result.append('B');
            }
        };

        private StringBuffer result=new StringBuffer();

        //

        public TestLookupManager() {
            Lookup.Template<TestLookupItemA> templateA=
                new Lookup.Template<TestLookupItemA>(TestLookupItemA.class);
            resultA=abstractLookup.lookup(templateA);
            resultA.addLookupListener(listenerA);
            resultA.allInstances().size();
            //
            Lookup.Template<TestLookupItemB> templateB=
                new Lookup.Template<TestLookupItemB>(TestLookupItemB.class);
            resultB=abstractLookup.lookup(templateB);
            resultB.addLookupListener(listenerB);
            resultB.allInstances().size();
            // WORKAROUND
            // instanceContent.add(Boolean.TRUE);
        }

        //

        public void add(Object item) {
            instanceContent.add(item);
        }
        public void remove(Object item) {
            instanceContent.remove(item);
        }
        public String getResult() {
            return result.toString();
        }
    }

}
