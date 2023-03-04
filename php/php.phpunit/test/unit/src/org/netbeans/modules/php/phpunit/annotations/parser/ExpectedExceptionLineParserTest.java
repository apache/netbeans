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
package org.netbeans.modules.php.phpunit.annotations.parser;

import org.netbeans.modules.php.phpunit.annotations.parser.ExpectedExceptionLineParser;
import java.util.Collections;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ExpectedExceptionLineParserTest extends NbTestCase {
    private ExpectedExceptionLineParser parser;

    public ExpectedExceptionLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new ExpectedExceptionLineParser();
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException Exception");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("Exception", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(18, offsetRange.getStart());
            assertEquals(27, offsetRange.getEnd());
            assertEquals("Exception", typeName);
        }
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException ");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException\t");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException\tException");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("Exception", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(18, offsetRange.getStart());
            assertEquals(27, offsetRange.getEnd());
            assertEquals("Exception", typeName);
        }
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException           Exception     ");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("Exception", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(28, offsetRange.getStart());
            assertEquals(37, offsetRange.getEnd());
            assertEquals("Exception", typeName);
        }
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException\t    \tException   \t   ");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("Exception", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(23, offsetRange.getStart());
            assertEquals(32, offsetRange.getEnd());
            assertEquals("Exception", typeName);
        }
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException\t\\My\\Cool\\Exception");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("\\My\\Cool\\Exception", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(18, offsetRange.getStart());
            assertEquals(36, offsetRange.getEnd());
            assertEquals("\\My\\Cool\\Exception", typeName);
        }
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException\tMy\\Cool\\Exception");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("My\\Cool\\Exception", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(18, offsetRange.getStart());
            assertEquals(35, offsetRange.getEnd());
            assertEquals("My\\Cool\\Exception", typeName);
        }
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException\tMy\\Cool\\Exception\tMy description");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("My\\Cool\\Exception\tMy description", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(18, offsetRange.getStart());
            assertEquals(35, offsetRange.getEnd());
            assertEquals("My\\Cool\\Exception", typeName);
        }
    }

    public void testValidUseCase_10() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("expectedException\tMy\\Cool\\Exception   My description");
        assertEquals("expectedException", parsedLine.getName());
        assertEquals("My\\Cool\\Exception   My description", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(18, offsetRange.getStart());
            assertEquals(35, offsetRange.getEnd());
            assertEquals("My\\Cool\\Exception", typeName);
        }
    }

    public void testInvalidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("@throws Exception");
        assertNull(parsedLine);
    }

    public void testInvalidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("foo Exception");
        assertNull(parsedLine);
    }

    public void testInvalidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("throw Exception");
        assertNull(parsedLine);
    }

}
