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

package org.netbeans.modules.maven.apisupport;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;

/**
 * @author Dafe Simonek
 */
public class PublicPackagesPanelTest extends NbTestCase {

    public PublicPackagesPanelTest(String testName) {
        super(testName);
    }

    @Test
    public void testGetPublicPackagesForPlugin() {
        SortedMap<String, Boolean> selItems = new TreeMap<String, Boolean>();
        selItems.put("a.b.c", true);
        selItems.put("a.b.d", true);
        selItems.put("a", true);
        selItems.put("a.b.e", true);
        selItems.put("something.different", true);
        selItems.put("a.b", true);
        selItems.put("just.another", false);

        SortedSet<String> expResult = new TreeSet<String>();
        expResult.add("a.*");
        expResult.add("something.different");

        SortedSet<String> result = PublicPackagesPanel.getPublicPackagesForPlugin(selItems);

        assertEquals(expResult, result);

        SortedMap<String, Boolean> unchanged = new TreeMap<String, Boolean>();
        unchanged.put("a.b.c", true);
        unchanged.put("a.b.d", true);
        unchanged.put("a", true);

        SortedSet<String> expResult2 = new TreeSet<String>();
        expResult2.add("a");
        expResult2.add("a.b.c");
        expResult2.add("a.b.d");

        SortedSet<String> result2 = PublicPackagesPanel.getPublicPackagesForPlugin(unchanged);

        assertEquals(expResult2, result2);
    }

}