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

package org.netbeans.upgrade;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.junit.NbTestCase;

/**
 * @author sherold
 */
public final class AutoUpgradeTest extends NbTestCase {

    public AutoUpgradeTest (String name) {
        super (name);
    }
    
    public void testComparatorUpgrade() throws Exception {
        // verify version ordering
        List<String> versions = Stream.of("13", "12.3", "14", "12.4", "8.0", "12.4.301")
                .sorted(AutoUpgrade.APACHE_VERSION_COMPARATOR.reversed()).collect(Collectors.toList());
        assertEquals(6, versions.size());
        assertEquals("14", versions.get(0));
        assertEquals("13", versions.get(1));
        assertEquals("12.4.301", versions.get(2));
        assertEquals("12.4", versions.get(3));
        assertEquals("12.3", versions.get(4));
        assertEquals("8.0", versions.get(5));
    }

 }
