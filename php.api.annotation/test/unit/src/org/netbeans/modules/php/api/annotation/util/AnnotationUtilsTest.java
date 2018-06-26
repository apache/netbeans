/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.api.annotation.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AnnotationUtilsTest extends NbTestCase {

    private static final String ANNOTATION_NAME = "Annotation";

    public AnnotationUtilsTest(String name) {
        super(name);
    }

    public void testValidUseCase_01() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation("\\Foo\\Bar\\Baz\\" + ANNOTATION_NAME, ANNOTATION_NAME));
    }

    public void testValidUseCase_02() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation("Foo\\Bar\\Baz\\" + ANNOTATION_NAME, ANNOTATION_NAME));
    }

    public void testValidUseCase_03() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation(ANNOTATION_NAME, ANNOTATION_NAME));
    }

    public void testValidUseCase_04() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation(ANNOTATION_NAME.toLowerCase(), ANNOTATION_NAME));
    }

    public void testValidUseCase_05() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation("Foo\\Bar\\Baz\\" + ANNOTATION_NAME.toLowerCase(), ANNOTATION_NAME));
    }

    public void testInvalidUseCase_01() throws Exception {
        assertFalse(AnnotationUtils.isTypeAnnotation(ANNOTATION_NAME + "\\Foo\\Bar\\Baz\\", ANNOTATION_NAME));
    }

    public void testInvalidUseCase_02() throws Exception {
        assertFalse(AnnotationUtils.isTypeAnnotation("\\Foo\\Bar" + ANNOTATION_NAME + "\\Baz\\", ANNOTATION_NAME));
    }

    public void testExtractParamTypes() throws Exception {
        Set<String> discriminatorMapRegexs = new HashSet<String>();
        discriminatorMapRegexs.add(""); //NOI18N
        Map<OffsetRange, String> types = AnnotationUtils.extractTypesFromParameters("DiscriminatorMap({\"person\" = \" Person \", \"employee\" = \" Employee \"})", discriminatorMapRegexs);
        assertNotNull(types);
        assertTrue(!types.isEmpty());
        assertEquals(2, types.size());
        assertTrue(types.containsValue("Person"));
        assertTrue(types.containsValue("Employee"));
        assertTrue(types.containsKey(new OffsetRange(31, 37)));
        assertTrue(types.containsKey(new OffsetRange(56, 64)));
    }

    public void testQualifiedExtractParamTypes() throws Exception {
        Set<String> discriminatorMapRegexs = new HashSet<String>();
        discriminatorMapRegexs.add(""); //NOI18N
        Map<OffsetRange, String> types = AnnotationUtils.extractTypesFromParameters("DiscriminatorMap({\"person\" = \" My\\Person \", \"employee\" = \" \\Full\\Q\\Employee \"})", discriminatorMapRegexs);
        assertNotNull(types);
        assertTrue(!types.isEmpty());
        assertEquals(2, types.size());
        assertTrue(types.containsValue("My\\Person"));
        assertTrue(types.containsValue("\\Full\\Q\\Employee"));
        assertTrue(types.containsKey(new OffsetRange(31, 40)));
        assertTrue(types.containsKey(new OffsetRange(59, 75)));
    }

    public void extractJustSomeParamTypes_01() throws Exception {
        Set<String> manyToOneRegexs = new HashSet<String>();
        manyToOneRegexs.add("targetEntity"); //NOI18N
        Map<OffsetRange, String> types = AnnotationUtils.extractTypesFromParameters("ManyToOne(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")", manyToOneRegexs);
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("ManyToOne", type1);
        String type2 = types.get(new OffsetRange(24, 28));
        assertEquals("Cart", type2);
    }

    public void extractJustSomeParamTypes_02() throws Exception {
        Set<String> manyToOneRegexs = new HashSet<String>();
        manyToOneRegexs.add("targetEntity"); //NOI18N
        Map<OffsetRange, String> types = AnnotationUtils.extractTypesFromParameters("ManyToOne(targetEntity=\"\\Foo\\Cart\", cascade={\"all\"}, fetch=\"EAGER\")", manyToOneRegexs);
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("ManyToOne", type1);
        String type2 = types.get(new OffsetRange(24, 33));
        assertEquals("\\Foo\\Cart", type2);
    }

    public void testNotNullTypeAnnotation_01() throws Exception {
        try {
            AnnotationUtils.isTypeAnnotation(null, "");
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testNotNullTypeAnnotation_02() throws Exception {
        try {
            AnnotationUtils.isTypeAnnotation("", null);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testNotNullExtracParamTypes() throws Exception {
        try {
            AnnotationUtils.extractTypesFromParameters(null, Collections.EMPTY_SET);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testNotNullExtracParamTypesRegexs() throws Exception {
        try {
            AnnotationUtils.extractTypesFromParameters("", null);
            fail();
        } catch (NullPointerException ex) {}
    }

}
