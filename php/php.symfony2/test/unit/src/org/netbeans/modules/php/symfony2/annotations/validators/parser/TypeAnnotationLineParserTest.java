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
package org.netbeans.modules.php.symfony2.annotations.validators.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;


/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TypeAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public TypeAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = SymfonyValidatorsAnnotationLineParser.getDefault();
    }

    public void testTypeParser() {
        assertNotNull(parser.parse("Type"));
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("Types"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Type");
        assertNotNull(parsedLine);
        assertEquals("Type", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 4));
        assertNotNull(type1);
        assertEquals("Type", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Type()  ");
        assertNotNull(parsedLine);
        assertEquals("Type", parsedLine.getName());
        assertEquals("()", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 4));
        assertNotNull(type1);
        assertEquals("Type", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\Type");
        assertNotNull(parsedLine);
        assertEquals("Type", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 16));
        assertNotNull(type1);
        assertEquals("Annotations\\Type", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Type(type=\"integer\", message=\"The value\")");
        assertNotNull(parsedLine);
        assertEquals("Type", parsedLine.getName());
        assertEquals("(type=\"integer\", message=\"The value\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 4));
        assertNotNull(type1);
        assertEquals("Type", type1);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Type(type=\"Foo\\Bar\\Baz\", message=\"The value\")");
        assertNotNull(parsedLine);
        assertEquals("Type", parsedLine.getName());
        assertEquals("(type=\"Foo\\Bar\\Baz\", message=\"The value\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 4));
        assertNotNull(type1);
        assertEquals("Type", type1);
        String type2 = types.get(new OffsetRange(11, 22));
        assertNotNull(type2);
        assertEquals("Foo\\Bar\\Baz", type2);
    }

}
