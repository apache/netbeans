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

package org.netbeans.core.osgi;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;

public class DependencyQueueTest extends NbTestCase {

    public DependencyQueueTest(String n) {
        super(n);
    }

    public void testOfferNoDeps() {
        DependencyQueue<String,String> q = new DependencyQueue<String,String>();
        assertEquals(list("1"), q.offer("1", set(), set(), set()));
    }
    
    public void testOfferApiAndImpl() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list("api"), q.offer("api", set("api"), set(), set()));
        assertEquals(list("impl"), q.offer("impl", set(), set("api"), set()));
    }
    
    public void testOfferImplRequiresAPI() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("impl", set(), set("api"), set()));
        assertEquals(list("api", "impl"), q.offer("api", set("api"), set(), set()));
    }
    
    public void testAPIThatNeedsItsImpl() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("api", set("api"), set(), set("impl")));
        assertEquals(list(), q.offer("client", set(), set("api"), set()));
        assertEquals(list("api", "impl", "client"), q.offer("impl", set("impl"), set("api"), set()));
    }
    public void testSimpleChain3requires2requires1() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("3", set("3"), set("2"), set()));
        assertEquals(list(), q.offer("2", set("2"), set("1"), set()));
        assertEquals(list("1", "2", "3"), q.offer("1", set("1"), set(), set()));
    }
    
    public void test1requires2needs3requires1() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("1", set("1"), set("2"), set()));
        assertEquals(list(), q.offer("2", set("2"), set(), set("3")));
        assertEquals(list("2", "1", "3"), q.offer("3", set("3"), set("1"), set()));
    }
    
    public void testAffectedByOrderOnJDK8_1requires2and0_2needs3_3requires1_Oisthere() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("1", set("1"), set("2", "0"), set()));
        assertEquals(list(), q.offer("2", set("2"), set(), set("3")));
        assertEquals(list(), q.offer("3", set("3"), set("1"), set()));
        assertEquals(list("0", "2", "1", "3"), q.offer("0", set("0"), set(), set()));
    }

    public void testRetract() {
        DependencyQueue<String,String> q = new DependencyQueue<String,String>();
        q.offer("1", set(), set(), set());
        assertEquals(list("1"), q.retract("1"));
        q = new DependencyQueue<String,String>();
        q.offer("api", set("api"), set(), set());
        q.offer("impl", set(), set("api"), set());
        assertEquals(list("impl"), q.retract("impl"));
        assertEquals(list("api"), q.retract("api"));
        q = new DependencyQueue<String,String>();
        q.offer("api", set("api"), set(), set());
        q.offer("impl", set(), set("api"), set());
        assertEquals(list("impl", "api"), q.retract("api"));
        assertEquals(list(), q.retract("impl"));
    }

    // XXX testGC

    private static List<String> list(String... items) {
        return Arrays.asList(items);
    }

    private static Set<String> set(String... items) {
        return new TreeSet<String>(list(items));
    }

}
