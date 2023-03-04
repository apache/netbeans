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
package org.netbeans.modules.php.doctrine2.annotations.orm.parser;

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
public class ChangeTrackingPolicyLineParserTest extends NbTestCase {
    private ParameterizedAnnotationLineParser parser;

    public ChangeTrackingPolicyLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = new ParameterizedAnnotationLineParser();
    }

    public void testIsAnnotationParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsChangeTrackingPolicyParsedLine_01() throws Exception {
        assertTrue(parser.parse("ChangeTrackingPolicy") instanceof ParsedLine);
    }

    public void testReturnValueIsChangeTrackingPolicyParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\ChangeTrackingPolicy") instanceof ParsedLine);
    }

    public void testReturnValueIsChangeTrackingPolicyParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\ChangeTrackingPolicy") instanceof ParsedLine);
    }

    public void testReturnValueIsChangeTrackingPolicyParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\ChangeTrackingPolicy(\"NOTIFY\")") instanceof ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("ChangeTrackingPolicys"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ChangeTrackingPolicy");
        assertEquals("ChangeTrackingPolicy", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String value = entry.getValue();
            assertEquals(0, offsetRange.getStart());
            assertEquals(20, offsetRange.getEnd());
            assertEquals("ChangeTrackingPolicy", value);
        }
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ChangeTrackingPolicy   ");
        assertEquals("ChangeTrackingPolicy", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String value = entry.getValue();
            assertEquals(0, offsetRange.getStart());
            assertEquals(20, offsetRange.getEnd());
            assertEquals("ChangeTrackingPolicy", value);
        }
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ChangeTrackingPolicy\t\t  ");
        assertEquals("ChangeTrackingPolicy", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String value = entry.getValue();
            assertEquals(0, offsetRange.getStart());
            assertEquals(20, offsetRange.getEnd());
            assertEquals("ChangeTrackingPolicy", value);
        }
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ChangeTrackingPolicy(\"NOTIFY\")");
        assertEquals("ChangeTrackingPolicy", parsedLine.getName());
        assertEquals("(\"NOTIFY\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String value = entry.getValue();
            assertEquals(0, offsetRange.getStart());
            assertEquals(20, offsetRange.getEnd());
            assertEquals("ChangeTrackingPolicy", value);
        }
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\ChangeTrackingPolicy(\"NOTIFY\")  \t");
        assertEquals("ChangeTrackingPolicy", parsedLine.getName());
        assertEquals("(\"NOTIFY\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String value = entry.getValue();
            assertEquals(0, offsetRange.getStart());
            assertEquals(32, offsetRange.getEnd());
            assertEquals("Annotations\\ChangeTrackingPolicy", value);
        }
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\ChangeTrackingPolicy(\"NOTIFY\")  \t");
        assertEquals("ChangeTrackingPolicy", parsedLine.getName());
        assertEquals("(\"NOTIFY\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String value = entry.getValue();
            assertEquals(0, offsetRange.getStart());
            assertEquals(29, offsetRange.getEnd());
            assertEquals("\\Foo\\Bar\\ChangeTrackingPolicy", value);
        }
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("changetrackingpolicy");
        assertEquals("ChangeTrackingPolicy", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String value = entry.getValue();
            assertEquals(0, offsetRange.getStart());
            assertEquals(20, offsetRange.getEnd());
            assertEquals("changetrackingpolicy", value);
        }
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\changetrackingpolicy(\"NOTIFY\")  \t");
        assertEquals("ChangeTrackingPolicy", parsedLine.getName());
        assertEquals("(\"NOTIFY\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String value = entry.getValue();
            assertEquals(0, offsetRange.getStart());
            assertEquals(29, offsetRange.getEnd());
            assertEquals("\\Foo\\Bar\\changetrackingpolicy", value);
        }
    }

}
