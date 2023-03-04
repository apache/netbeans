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

package org.netbeans.modules.search;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  Marian Petras
 */
public class FindDialogMemoryTest extends NbTestCase {

    private FindDialogMemory memory;

    public FindDialogMemoryTest() {
        super("FindDialogMemoryTest");
    }

    @Override
    public void setUp() throws Exception {
        Constructor<FindDialogMemory> constructor
                = FindDialogMemory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        memory = constructor.newInstance();
    }
    
    public void testFileNamePatternStorage() throws Exception {
        assertNotNull(memory.getFileNamePatterns());
        memory.getFileNamePatterns().clear(); // clear default values
        assertTrue(memory.getFileNamePatterns().isEmpty());

        final String elem0 = "abc";
        final String elem1 = "KLM";
        final String elem2 = "123";
        final String fillPrefix = "fill-";

        /* check that the item has been added */
        memory.storeFileNamePattern(elem0);
        assertEquals(1, memory.getFileNamePatterns().size());
        assertEquals(elem0, memory.getFileNamePatterns().get(0));

        /* check that a one-item list is unchanged after adding a duplicate: */
        memory.storeFileNamePattern(elem0);
        assertEquals(1, memory.getFileNamePatterns().size());
        assertEquals(elem0, memory.getFileNamePatterns().get(0));

        /* check that a new unique item is added to the tail of the list: */
        memory.storeFileNamePattern(elem1);
        assertEquals(2, memory.getFileNamePatterns().size());
        assertEquals(elem0, memory.getFileNamePatterns().get(0));
        assertEquals(elem1, memory.getFileNamePatterns().get(1));

        /* check that a non-unique item is just moved to the end of the list: */
        memory.storeFileNamePattern(elem0);
        assertEquals(2, memory.getFileNamePatterns().size());
        assertEquals(elem1, memory.getFileNamePatterns().get(0));
        assertEquals(elem0, memory.getFileNamePatterns().get(1));

        /* check that a third unique item is actually added to the list: */
        memory.storeFileNamePattern(elem2);
        assertEquals(3, memory.getFileNamePatterns().size());
        assertEquals(elem1, memory.getFileNamePatterns().get(0));
        assertEquals(elem0, memory.getFileNamePatterns().get(1));
        assertEquals(elem2, memory.getFileNamePatterns().get(2));

        /*
         * check that the list is unchanged after an attempt to re-add
         * an element that is already at the tail of the list:
         */
        memory.storeFileNamePattern(elem2);
        assertEquals(3, memory.getFileNamePatterns().size());
        assertEquals(elem1, memory.getFileNamePatterns().get(0));
        assertEquals(elem0, memory.getFileNamePatterns().get(1));
        assertEquals(elem2, memory.getFileNamePatterns().get(2));

        /* check that a middle element is moved to the tail if re-added: */
        memory.storeFileNamePattern(elem0);
        assertEquals(3, memory.getFileNamePatterns().size());
        assertEquals(elem1, memory.getFileNamePatterns().get(0));
        assertEquals(elem2, memory.getFileNamePatterns().get(1));
        assertEquals(elem0, memory.getFileNamePatterns().get(2));

        /* check that the first element is moved to the tail if re-added: */
        memory.storeFileNamePattern(elem1);
        assertEquals(3, memory.getFileNamePatterns().size());
        assertEquals(elem2, memory.getFileNamePatterns().get(0));
        assertEquals(elem0, memory.getFileNamePatterns().get(1));
        assertEquals(elem1, memory.getFileNamePatterns().get(2));

        /* check that the list can contain the specified number of elements: */
        int currCount = memory.getFileNamePatterns().size();
        int maxCount = getMaxFileNameCount();
        int i = 0;
        while (i < maxCount - currCount) {
            memory.storeFileNamePattern(String.format("%s%03d", fillPrefix, i++));
        }
        assertEquals(elem2, memory.getFileNamePatterns().get(0));
        assertEquals(elem0, memory.getFileNamePatterns().get(1));
        assertEquals(elem1, memory.getFileNamePatterns().get(2));
        for (int j = currCount; j < maxCount; j++) {
            assertEquals(String.format("%s%03d", fillPrefix, j - currCount),
                         memory.getFileNamePatterns().get(j));
        }

        /*
         * check that the oldest elements are removed if there would not be
         * enough space for new elements:
         */
        while (i < maxCount) {
            memory.storeFileNamePattern(String.format("%s%03d", fillPrefix, i++));
        }
        for (int j = 0; j < maxCount; j++) {
            assertEquals(String.format("%s%03d", fillPrefix, j),
                         memory.getFileNamePatterns().get(j));
        }

    }

    public void testReplacePatternSpecified() {
        memory.setReplacePatternSpecified(true);
        assertTrue(memory.isReplacePatternSpecified());
        memory.setReplacePatternSpecified(false);
        assertFalse(memory.isReplacePatternSpecified());
    }

    private static int getMaxFileNameCount() throws Exception {
        Field field = FindDialogMemory.class
                      .getDeclaredField("maxFileNamePatternCount");
        field.setAccessible(true);
        return field.getInt(null);
    }

}
