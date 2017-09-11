/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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
