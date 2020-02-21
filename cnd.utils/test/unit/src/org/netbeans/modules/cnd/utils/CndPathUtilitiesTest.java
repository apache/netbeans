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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 */
public class CndPathUtilitiesTest {

    @Test
    public void testGetPathNameArrayWin() {
        CndPathUtilities.testSetWindows(true);
        assertArrayEquals(new String[] {"C:", "tmp", "test.cpp"}, // NOI18N
                     CndPathUtilities.getPathNameArray("C:\\tmp\\test.cpp")); // NOI18N
        assertArrayEquals(new String[] {"C:", "tmp", "test.cpp"}, // NOI18N
                     CndPathUtilities.getPathNameArray("C:/tmp/test.cpp")); // NOI18N
        assertArrayEquals(new String[] {"tmp", "test.cpp"}, // NOI18N
                     CndPathUtilities.getPathNameArray("\\tmp\\test.cpp")); // NOI18N
        assertArrayEquals(new String[] {"tmp", "test.cpp"}, // NOI18N
                     CndPathUtilities.getPathNameArray("/tmp/test.cpp")); // NOI18N
        assertArrayEquals(new String[] {}, // NOI18N
                     CndPathUtilities.getPathNameArray("tmp/test.cpp")); // NOI18N
    }


    @Test
    public void testGetPathNameArray() {
        CndPathUtilities.testSetWindows(false);
        assertArrayEquals(new String[]{"tmp", "test.cpp"}, // NOI18N
                CndPathUtilities.getPathNameArray("\\tmp\\test.cpp")); // NOI18N
        assertArrayEquals(new String[]{"tmp", "test.cpp"}, // NOI18N
                CndPathUtilities.getPathNameArray("/tmp/test.cpp")); // NOI18N
        assertArrayEquals(new String[]{}, // NOI18N
                CndPathUtilities.getPathNameArray("tmp/test.cpp")); // NOI18N
    }

    @Test
    public void testIsPathAbsoluteWin() {
        CndPathUtilities.testSetWindows(true);
        assertTrue(CndPathUtilities.isPathAbsolute("C:\\tmp\\test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("C:/tmp/test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("C:/./test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("\\temp\\..\\tmp\\test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("/tmp/test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("/../tmp/test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("/.")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("../tmp/test.cpp")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("./")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("tmp\\test.cpp")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("C:")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("C:\\")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("C:/")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("C:a")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("C:\\a")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("C:/a")); // NOI18N
    }
    
    @Test
    public void testIsPathAbsolute() {
        CndPathUtilities.testSetWindows(false);
        assertTrue(CndPathUtilities.isPathAbsolute("\\temp\\..\\tmp\\test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("/tmp/test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("/../tmp/test.cpp")); // NOI18N
        assertTrue(CndPathUtilities.isPathAbsolute("/.")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("../tmp/test.cpp")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("./")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("tmp\\test.cpp")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("C:")); // NOI18N
        assertFalse(CndPathUtilities.isPathAbsolute("C:a")); // NOI18N
    }

    @Test
    public void test158596() {
        CndPathUtilities.testSetWindows(true);
        assertEquals("c", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("\\C:\\f", "C:\\f\\c"))); // NOI18N
    }

    @Test
    public void test185323Win() {
        CndPathUtilities.testSetWindows(true);
        assertEquals("../b", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("\\C:\\a", "C:\\b"))); // NOI18N
        assertEquals("../home2", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("/export/home1", "/export/home2"))); // NOI18N
        assertEquals("..", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("\\C:\\a", "C:\\"))); // NOI18N
        assertEquals("..", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("/export/home1", "/export"))); // NOI18N
    }
    
    @Test
    public void test185323() {
        CndPathUtilities.testSetWindows(false);
        assertEquals("../home2", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("/export/home1", "/export/home2"))); // NOI18N
        assertEquals("..", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("/export/home1", "/export"))); // NOI18N
    }

    @Test
    public void testToRelativePathWin() {
        CndPathUtilities.testSetWindows(true);
        assertEquals("D:\\tmp\\test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("C:\\tmp", "D:\\tmp\\test.cpp")); // NOI18N
        assertEquals("/tmp/test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("/temp/", "/tmp/test.cpp")); // NOI18N
        assertEquals("1\\test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("C:\\tmp", "C:\\tmp\\1\\test.cpp")); // NOI18N
        assertEquals("1/test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("/tmp", "/tmp/1/test.cpp")); // NOI18N
        assertEquals("test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("C:\\tmp", "C:\\tmp\\test.cpp")); // NOI18N
        assertEquals("test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("/tmp", "/tmp/test.cpp")); // NOI18N
        assertEquals("../../../3/2/1/test.cpp", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("C:\\tmp\\dir\\1\\2\\3", "C:\\tmp\\dir\\3\\2\\1\\test.cpp"))); // NOI18N
        assertEquals("../../../3/2/1/test.cpp", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("/tmp/dir/1/2/3", "/tmp/dir/3/2/1/test.cpp"))); // NOI18N
        assertEquals(".", // NOI18N
                     CndPathUtilities.toRelativePath("C:\\", "C:\\")); // NOI18N
        assertEquals("D:\\", // NOI18N
                     CndPathUtilities.toRelativePath("C:\\", "D:\\")); // NOI18N
        assertEquals(".", // NOI18N
                     CndPathUtilities.toRelativePath("/tmp", "/tmp")); // NOI18N
    }

    @Test
    public void testToRelativePath() {
        CndPathUtilities.testSetWindows(false);
        assertEquals("D:\\tmp\\test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("C:\\tmp", "D:\\tmp\\test.cpp")); // NOI18N
        assertEquals("/tmp/test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("/temp/", "/tmp/test.cpp")); // NOI18N
        assertEquals("1/test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("/tmp", "/tmp/1/test.cpp")); // NOI18N
        assertEquals("test.cpp", // NOI18N
                     CndPathUtilities.toRelativePath("/tmp", "/tmp/test.cpp")); // NOI18N
        assertEquals("../../../3/2/1/test.cpp", // NOI18N
                     normalize(CndPathUtilities.toRelativePath("/tmp/dir/1/2/3", "/tmp/dir/3/2/1/test.cpp"))); // NOI18N
        assertEquals("D:\\", // NOI18N
                     CndPathUtilities.toRelativePath("C:\\", "D:\\")); // NOI18N
        assertEquals(".", // NOI18N
                     CndPathUtilities.toRelativePath("/tmp", "/tmp")); // NOI18N
    }
       
    private String normalize(String path) {
        return path.replace('\\', '/');
    }

}
