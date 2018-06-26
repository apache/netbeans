/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
