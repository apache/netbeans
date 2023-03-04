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

package org.netbeans.modules.php.project.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import static org.junit.Assert.*;
import org.openide.util.Pair;

public class PhpProjectUtilsTest extends NbTestCase {

    public PhpProjectUtilsTest(String name) {
        super(name);
    }

    public void testImplode() {
        final List<String> items = Arrays.asList("one", "two");
        assertEquals("one" + PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR + "two", StringUtils.implode(items, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR));
    }

    public void testExplode() {
        final String[] items = {"one", "two"};
        String string = "one" + PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR + "two";
        assertArrayEquals(items, StringUtils.explode(string, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR).toArray(new String[0]));

        // test for empty string (relative path ".")
        string = "one" + PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR + "" + PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR + "two";
        assertArrayEquals(new String[] {"one", "", "two"}, StringUtils.explode(string, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR).toArray(new String[0]));
    }

    public void testResolveFile() throws Exception {
        File workDir = getWorkDir();
        assertEquals(workDir, PhpProjectUtils.resolveFile(workDir, null));
        assertEquals(workDir, PhpProjectUtils.resolveFile(workDir, ""));
        assertEquals(workDir, PhpProjectUtils.resolveFile(workDir, " "));
        assertEquals(new File(workDir, "a.php"), PhpProjectUtils.resolveFile(workDir, "a.php"));
        assertEquals(new File(new File(workDir, "myfolder"), "a.php"), PhpProjectUtils.resolveFile(workDir, "myfolder/a.php"));
    }

    public void testIntervals() {
        // empty
        assertTrue(PhpProjectUtils.getIntervals(Collections.<Integer>emptyList()).isEmpty());
        // one
        List<Integer> numbers = Arrays.asList(1);
        List<Pair<Integer, Integer>> intervals = PhpProjectUtils.getIntervals(numbers);
        assertEquals(1, intervals.size());
        assertEquals(Pair.of(1, 1), intervals.get(0));
        // one-more
        numbers = Arrays.asList(3, 4, 1, 5);
        intervals = PhpProjectUtils.getIntervals(numbers);
        assertEquals(2, intervals.size());
        assertEquals(Pair.of(1, 1), intervals.get(0));
        assertEquals(Pair.of(3, 5), intervals.get(1));
        // one-more-one
        numbers = Arrays.asList(99, 3, 4, 1, 5);
        intervals = PhpProjectUtils.getIntervals(numbers);
        assertEquals(3, intervals.size());
        assertEquals(Pair.of(1, 1), intervals.get(0));
        assertEquals(Pair.of(3, 5), intervals.get(1));
        assertEquals(Pair.of(99, 99), intervals.get(2));
        // more
        numbers = Arrays.asList(1, 2, 3, 5, 4);
        intervals = PhpProjectUtils.getIntervals(numbers);
        assertEquals(1, intervals.size());
        assertEquals(Pair.of(1, 5), intervals.get(0));
        // more-more
        numbers = Arrays.asList(1, 2, 3, 5, 4, 58, 59, 60);
        intervals = PhpProjectUtils.getIntervals(numbers);
        assertEquals(2, intervals.size());
        assertEquals(Pair.of(1, 5), intervals.get(0));
        assertEquals(Pair.of(58, 60), intervals.get(1));
        // more-one-more-more
        numbers = Arrays.asList(1, 2, 3, 5, 4, 58, 77, 78, 79, 100, 101, 102);
        intervals = PhpProjectUtils.getIntervals(numbers);
        assertEquals(4, intervals.size());
        assertEquals(Pair.of(1, 5), intervals.get(0));
        assertEquals(Pair.of(58, 58), intervals.get(1));
        assertEquals(Pair.of(77, 79), intervals.get(2));
        assertEquals(Pair.of(100, 102), intervals.get(3));
        // more-one-more-one
        numbers = Arrays.asList(1, 2, 3, 5, 4, 58, 77, 78, 79, 100);
        intervals = PhpProjectUtils.getIntervals(numbers);
        assertEquals(4, intervals.size());
        assertEquals(Pair.of(1, 5), intervals.get(0));
        assertEquals(Pair.of(58, 58), intervals.get(1));
        assertEquals(Pair.of(77, 79), intervals.get(2));
        assertEquals(Pair.of(100, 100), intervals.get(3));
    }

}
