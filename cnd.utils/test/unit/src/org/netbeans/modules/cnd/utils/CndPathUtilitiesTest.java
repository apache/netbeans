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
