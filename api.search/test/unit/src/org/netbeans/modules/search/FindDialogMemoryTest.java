/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
