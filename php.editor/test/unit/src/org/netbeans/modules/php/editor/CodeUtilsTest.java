/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class CodeUtilsTest extends NbTestCase {

    public CodeUtilsTest(String name) {
        super(name);
    }

    public void testCommonNamespacePrefix() {
        assertEquals("\\A\\B\\", CodeUtils.getCommonNamespacePrefix("A\\B\\C\\fc", "A\\B\\fb"));
        assertEquals("\\A\\B\\", CodeUtils.getCommonNamespacePrefix("A\\B\\C\\fc", "\\A\\B\\fb"));
        assertEquals("\\A\\", CodeUtils.getCommonNamespacePrefix("A\\B\\fce", "A\\Bfce"));
        assertEquals("\\A\\", CodeUtils.getCommonNamespacePrefix("A\\B", "A\\fa"));
        assertEquals("\\A\\", CodeUtils.getCommonNamespacePrefix("A\\B", "A\\"));
        assertEquals("\\A\\", CodeUtils.getCommonNamespacePrefix("A\\B", "\\A\\"));
        assertEquals(null, CodeUtils.getCommonNamespacePrefix("\\A\\", "\\B\\"));
        assertEquals(null, CodeUtils.getCommonNamespacePrefix("A\\", "A"));
        assertEquals(null, CodeUtils.getCommonNamespacePrefix("A\\B", "AB\\fb"));
        assertEquals(null, CodeUtils.getCommonNamespacePrefix("A\\B", "AB\\"));
        assertEquals(null, CodeUtils.getCommonNamespacePrefix("cat", "car"));
        assertEquals(null, CodeUtils.getCommonNamespacePrefix("cat", "dog"));
        assertEquals(null, CodeUtils.getCommonNamespacePrefix("", "dog"));
    }

    public void testFullyQualifyNamespace() {
        assertEquals("\\A\\MyCls", CodeUtils.fullyQualifyNamespace("\\A\\MyCls"));
        assertEquals("\\A\\MyCls", CodeUtils.fullyQualifyNamespace("A\\MyCls"));
        assertEquals("", CodeUtils.fullyQualifyNamespace(""));
    }

    public void testCommonNamespacePrefixes0() {
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(Collections.emptyList());
        assertEquals(0, prefixes.size());
    }

    public void testCommonNamespacePrefixes1() {
        List<String> strings = Arrays.asList(
                "A\\B\\C\\fc",
                "A\\B\\fb",
                "A\\fa",
                "X\\fx");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(1, prefixes.size());
        assertEquals("\\A\\", prefixes.get(0));
    }

    public void testCommonNamespacePrefixes2() {
        List<String> strings = Arrays.asList(
                "A\\B\\C\\fc",
                "A\\B\\fb",
                "X\\fx");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(1, prefixes.size());
        assertEquals("\\A\\B\\", prefixes.get(0));
    }

    public void testCommonNamespacePrefixes3() {
        List<String> strings = Arrays.asList(
                "\\A\\B\\C\\fc",
                "A\\B\\fb",
                "X\\Y\\fy",
                "X\\fx");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(2, prefixes.size());
        assertEquals("\\A\\B\\", prefixes.get(0));
        assertEquals("\\X\\", prefixes.get(1));
    }

    public void testCommonNamespacePrefixes4() {
        List<String> strings = Arrays.asList(
                "A\\B\\C\\D\\fa",
                "A\\B\\C\\D\\fd",
                "A\\B\\C\\D\\fdd",
                "A\\B\\C\\fc",
                "\\A\\B\\C\\fc2",
                "A\\B\\fb",
                "A\\B\\fb1",
                "B\\fb1",
                "XY\\fxy",
                "X\\Y\\fy1",
                "X\\Y\\fy",
                "X\\fx",
                "Y\\fy");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(2, prefixes.size());
        assertEquals("\\A\\B\\", prefixes.get(0));
        assertEquals("\\X\\", prefixes.get(1));
    }

    public void testCommonNamespacePrefixes5() {
        List<String> strings = Arrays.asList(
                "MyClass",
                "A\\MyClass");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(0, prefixes.size());
    }

    public void testCommonNamespacePrefixes6() {
        List<String> strings = Arrays.asList(
                "MyClass",
                "A\\MyClass",
                "A\\MyOtherClass");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(1, prefixes.size());
        assertEquals("\\A\\", prefixes.get(0));
    }

    public void testCommonNamespacePrefixes7() {
        List<String> strings = Arrays.asList(
                "MyClass",
                "A\\MyClass",
                "B\\MyClass",
                "C\\MyClass");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(0, prefixes.size());
    }

    public void testCommonNamespacePrefixes8() {
        List<String> strings = Arrays.asList(
                "\\MyClass",
                "\\A\\MyClass",
                "\\A\\MyOtherClass");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(1, prefixes.size());
        assertEquals("\\A\\", prefixes.get(0));
    }

    public void testCommonNamespacePrefixes9() {
        List<String> strings = Arrays.asList(
                "\\MyClass",
                "\\A\\MyClass",
                "\\B\\MyClass",
                "\\C\\MyClass");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(0, prefixes.size());
    }

    public void testCommonNamespacePrefixes10() {
        List<String> strings = Arrays.asList(
                "A\\B\\C\\fc",
                "\\A\\B\\fb",
                "A\\fa",
                "X\\fx");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(1, prefixes.size());
        assertEquals("\\A\\", prefixes.get(0));
    }

    public void testCommonNamespacePrefixes11() {
        List<String> strings = Arrays.asList(
                "\\MyClass",
                "\\A\\MyClass",
                "B\\MyClass",
                "\\C\\MyClass");
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(strings);
        assertEquals(0, prefixes.size());
    }

}
