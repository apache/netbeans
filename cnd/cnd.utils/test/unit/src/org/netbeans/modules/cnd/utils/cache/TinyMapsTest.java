/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.utils.cache;

import java.util.Collections;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class TinyMapsTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testCreateMap() {
        for (int initialCapacity = 0; initialCapacity < 65; initialCapacity++) {
            Map<?, ?> result = TinyMaps.createMap(initialCapacity);
            Assert.assertNotNull(result);
            if (initialCapacity == 0) {
                Assert.assertSame("have to be empty collection " + result.getClass().getSimpleName(), Collections.emptyMap(), result);
            } else if (initialCapacity == 1) {
                Assert.assertTrue("have to be TinySingletonMap " + result.getClass().getSimpleName(), result instanceof TinySingletonMap<?,?>);
            } else if (initialCapacity == 2) {
                Assert.assertTrue("have to be TinyTwoValuesMap " + result.getClass().getSimpleName(), result instanceof TinyTwoValuesMap<?,?>);
            } else if (initialCapacity <= 4) {
                Assert.assertTrue("have to be TinyMap4 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyMap4<?,?>);
            } else if (initialCapacity <= 6) {
                Assert.assertTrue("have to be TinyMap6 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyMap6<?,?>);
            } else if (initialCapacity <= 8) {
                Assert.assertTrue("have to be TinyMap8 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyMap8<?,?>);
            } else if (initialCapacity <= 16) {
                Assert.assertTrue("have to be TinyMap16 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyMap16<?,?>);
            } else if (initialCapacity <= 32) {
                Assert.assertTrue("have to be TinyMap16 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyHashMap32<?,?>);
            } else if (initialCapacity <= 64) {
                Assert.assertTrue("have to be TinyMap16 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyHashMap64<?,?>);
            } else if (initialCapacity <= 128) {
                Assert.assertTrue("have to be TinyMap16 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyHashMap128<?,?>);
            } else {
                Assert.assertTrue("have to be TinyHashMap " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyHashMap<?,?>);
            }
        }
    }

    @Test
    public void testExpandForNextKey() {
        System.out.println("expandForNextKey");
        Map<Object, Object> orig = null;
        Object newElem = null;
        Map expResult = null;
        Map result = TinyMaps.expandForNextKey(orig, newElem);
//        Assert.assertEquals(expResult, result);
    }
}
