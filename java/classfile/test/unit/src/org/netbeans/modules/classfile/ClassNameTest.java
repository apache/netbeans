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
package org.netbeans.modules.classfile;
/*
 * ClassNameTest.java
 * JUnit based test
 *
 * Created on September 3, 2004, 2:09 PM
 */

import junit.framework.TestCase;

/**
 *
 * @author tball
 */
public class ClassNameTest extends TestCase {
    
    public ClassNameTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() {
        ClassName.clearCache();
    }
    
    /**
     * Test of getClassName method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testGetClassName() {
        assertNotNull(ClassName.getClassName("Ljava/lang/String;"));
        assertNotNull(ClassName.getClassName("java/io/File"));
        assertNull(ClassName.getClassName(null));
        assertNull(ClassName.getClassName(""));
    }

    /**
     * Test of getType method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testGetType() {
        ClassName cn = ClassName.getClassName("Ljava/lang/String$CaseInsensitiveComparator;");
        assertEquals(cn.getType(), "java/lang/String$CaseInsensitiveComparator");
        cn = ClassName.getClassName("[Ljava/io/File;");
        assertEquals(cn.getType(), "[java/io/File");
        cn = ClassName.getClassName("[I");
        assertEquals(cn.getType(), "[I");
    }

    /**
     * Test of getInternalName method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testGetInternalName() {
        ClassName cn = ClassName.getClassName("Ljava/lang/String$CaseInsensitiveComparator;");
        assertEquals(cn.getInternalName(), "java/lang/String$CaseInsensitiveComparator");
        cn = ClassName.getClassName("[Ljava/io/File;");
        assertEquals(cn.getInternalName(), "java/io/File");
        cn = ClassName.getClassName("[I");
        assertEquals(cn.getInternalName(), "I");
    }

    /**
     * Test of getExternalName method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testGetExternalName() {
        ClassName cn = ClassName.getClassName("Ljava/lang/String$CaseInsensitiveComparator;");
        assertEquals(cn.getExternalName(), "java.lang.String.CaseInsensitiveComparator");
        cn = ClassName.getClassName("[Ljava/io/File;");
        assertEquals(cn.getExternalName(), "java.io.File[]");
        cn = ClassName.getClassName("[I");
        assertEquals(cn.getExternalName(), "I[]");
    }

    /**
     * Test of getPackage method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testGetPackage() {
        ClassName cn = ClassName.getClassName("Ljava/lang/String$CaseInsensitiveComparator;");
        assertEquals(cn.getPackage(), "java.lang");
        cn = ClassName.getClassName("[Ljava/io/File;");
        assertEquals(cn.getPackage(), "java.io");
        cn = ClassName.getClassName("[I");
        assertEquals(cn.getPackage(), "");
    }

    /**
     * Test of getSimpleName method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testGetSimpleName() {
        ClassName cn = ClassName.getClassName("Ljava/lang/String$CaseInsensitiveComparator;");
        assertEquals(cn.getSimpleName(), "String.CaseInsensitiveComparator");
        cn = ClassName.getClassName("[Ljava/io/File;");
        assertEquals(cn.getSimpleName(), "File[]");
        cn = ClassName.getClassName("[I");
        assertEquals(cn.getSimpleName(), "I[]");
    }

    /**
     * Test of equals method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testEquals() {
        ClassName cn1 = ClassName.getClassName("Ljava/io/File;");
        ClassName cn2 = ClassName.getClassName("Ljava/io/File;");
        ClassName cn3 = ClassName.getClassName("Ljava/lang/String;");
        assertTrue(cn1.equals(cn2));
        assertFalse(cn1.equals(cn3));
    }

    /**
     * Test of compareTo method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testCompareTo() {
        ClassName cn1 = ClassName.getClassName("Ljava/io/File;");
        ClassName cn2 = ClassName.getClassName("Ljava/io/File;");
        ClassName cn3 = ClassName.getClassName("Ljava/lang/String;");
        assertTrue(cn1.compareTo(cn2) == 0);
        assertTrue(cn1.compareTo(cn3) < 0);
        assertTrue(cn3.compareTo(cn2) > 0);
    }

    /**
     * Test of compare method, of class org.netbeans.modules.classfile.ClassName.
     */
    public void testCompare() {
        ClassName cn1 = ClassName.getClassName("Ljava/io/File;");
        ClassName cn2 = ClassName.getClassName("Ljava/io/File;");
        ClassName cn3 = ClassName.getClassName("Ljava/lang/String;");
        assertTrue(cn1.compare(cn1, cn2) == 0);
        assertTrue(cn1.compare(cn1, cn3) < 0);
        assertTrue(cn3.compare(cn3, cn2) > 0);
    }
}
