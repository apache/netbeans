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
package org.netbeans.modules.php.apigen.annotations.parser;

import java.util.Collections;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine.ParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SeeLineParserTest extends NbTestCase {

    private SeeLineParser parser;

    public SeeLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new SeeLineParser();
    }

    public void testIsAnnotationLineParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsSeeParsedLine() throws Exception {
        assertTrue(parser.parse("see") instanceof ParsedLine);
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see");
        assertEquals("see", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Foo");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(7, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see\tFoo");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(7, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Foo::bar()");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo::bar()", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(7, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see\t\tFoo::bar()");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo::bar()", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(5, offsetRange.getStart());
            assertEquals(8, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Foo::$myProperty");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo::$myProperty", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(7, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see \\Omg\\My\\Foo::$myProperty");
        assertEquals("see", parsedLine.getName());
        assertEquals("\\Omg\\My\\Foo::$myProperty", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(15, offsetRange.getEnd());
            assertEquals("\\Omg\\My\\Foo", typeName);
        }
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Omg\\My\\Foo::$myProperty");
        assertEquals("see", parsedLine.getName());
        assertEquals("Omg\\My\\Foo::$myProperty", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(14, offsetRange.getEnd());
            assertEquals("Omg\\My\\Foo", typeName);
        }
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see   \t  Omg\\My\\Foo::$myProperty   \t\t  ");
        assertEquals("see", parsedLine.getName());
        assertEquals("Omg\\My\\Foo::$myProperty", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(9, offsetRange.getStart());
            assertEquals(19, offsetRange.getEnd());
            assertEquals("Omg\\My\\Foo", typeName);
        }
    }

    public void testValidUseCase_10() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Omg\\My\\Foo::$myProperty My super description.");
        assertEquals("see", parsedLine.getName());
        assertEquals("Omg\\My\\Foo::$myProperty My super description.", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(14, offsetRange.getEnd());
            assertEquals("Omg\\My\\Foo", typeName);
        }
    }

    public void testInvalidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see http://www.example.com");
        assertEquals("see", parsedLine.getName());
        assertEquals("http://www.example.com", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testInvalidUseCase_02() throws Exception {
        assertNull(parser.parse("omg"));
    }

}
