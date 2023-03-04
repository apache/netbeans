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

import java.util.Arrays;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.implspi.ActiveQueue;

/** Testing memory consumption of various AbstractLookup aspects.
 */
public class AbstractLookupMemoryTest extends NbTestCase {
    public AbstractLookupMemoryTest(java.lang.String testName) {
        super(testName);
    }

    public void testEmptySize () {
        AbstractLookup instanceLookup = new AbstractLookup ();
        assertSize ("Empty lookup should be small", 16, instanceLookup);

        InstanceContent ic = new InstanceContent ();
        instanceLookup = new AbstractLookup (ic);
        assertSize ("Lookup with InstanceContent should be small as well", 16, instanceLookup);
    }

    public void testPairSize () {
        AbstractLookup.Pair pair = new EmptyPair ();
        assertSize ("Pair occupies only 16 bytes", 16, pair);
    }

    public void testPairWithOnePointerSize () {
        AbstractLookup.Pair pair = new OneItemPair ();
        assertSize ("Pair occupies only 16 bytes", 16, pair);
    }

    public void testLookupWithPairs () {
        Lookup.Template<Object> t = new Lookup.Template<Object>(Object.class);
        class L implements org.openide.util.LookupListener {
            public int cnt;
            public void resultChanged (org.openide.util.LookupEvent ev) {
                cnt++;
            }
        }
        L listener = new L ();
        L listener2 = new L ();

        EmptyPair[] pairs = {
            new EmptyPair(),
            new EmptyPair(),
            new EmptyPair(),
            new EmptyPair(),
        };
        Object[] ignore = {
            pairs[0],
            pairs[1],
            pairs[2],
            pairs[3],
            t,
            ActiveQueue.queue(),
            listener,
            listener2,
            new Integer (11) // trashhold is shared
        };

        AbstractLookup.Content c = new AbstractLookup.Content ();
        AbstractLookup l = new AbstractLookup (c, (Integer)ignore[ignore.length - 1]);

        c.addPair ((EmptyPair)ignore[0]);
        assertSize ("Should be really small (not counting the pair sizes)", Collections.singleton (l), 56, ignore);

        c.addPair ((EmptyPair)ignore[1]);
        assertSize ("Is bigger I guess (not counting the pair sizes)", Collections.singleton (l), 56, ignore);

        c.setPairs(Arrays.asList(pairs).subList(0, 3));
        assertSize ("Even bigger (not counting the pair sizes)", Collections.singleton (l), 64, ignore);

        c.setPairs(Arrays.asList(pairs).subList(0, 4));
        assertSize ("Now not that much(not counting the pair sizes)", Collections.singleton (l), 64, ignore);

        Lookup.Result res = l.lookup (t);

        assertSize ("After creating a result", Collections.singleton (l), 120, ignore);

        res.addLookupListener (listener);

        assertSize ("And attaching one listener", Collections.singleton (l), 120, ignore);

        res.addLookupListener (listener2);
        assertSize ("Second listener makes the situation much worse", Collections.singleton (l), 200, ignore);
        res.removeLookupListener(listener2);
        assertSize ("But removing it returns us back to original size", Collections.singleton (l), 120, ignore);


        assertEquals ("Current for pairs are in", res.allItems ().size (), 4); // also activates the listener
        assertSize ("and making the listener to work", Collections.singleton (l), 120, ignore);

        c.removePair ((EmptyPair)ignore[0]);
        assertEquals ("A changes has been delivered", 1, listener.cnt);
    }

    /** Simple pair with no data */
    static class EmptyPair extends AbstractLookup.Pair {
        protected boolean creatorOf(Object obj) { return false; }
        public String getDisplayName() { return ""; }
        public String getId() { return ""; }
        public Object getInstance() { return null; }
        public Class getType() { return Object.class; }
        protected boolean instanceOf(Class c) { return c == getType (); }
    } // end of EmptyPair

    /** Pair with one item (like InstanceContent.Pair) */
    private static class OneItemPair extends EmptyPair {
        private Object pointer;
    }
}
