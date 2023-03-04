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
package org.netbeans.modules.php.symfony2.annotations.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SymfonyCommonLineAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public SymfonyCommonLineAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = SymfonyCommonLineAnnotationLineParser.getDefault();
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    \"short_bio\" = {@Assert\\NotBlank(), @Assert\\MaxLength(limit = 100, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("\"short_bio\" = {@Assert\\NotBlank(), @Assert\\MaxLength(limit = 100,", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(20, 35));
        assertNotNull(type1);
        assertEquals("Assert\\NotBlank", type1);
        String type2 = types.get(new OffsetRange(40, 56));
        assertNotNull(type2);
        assertEquals("Assert\\MaxLength", type2);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    @Assert\\NotBlank, @Assert\\MinLength(5), ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("@Assert\\NotBlank, @Assert\\MinLength(5),", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(5, 20));
        assertNotNull(type1);
        assertEquals("Assert\\NotBlank", type1);
        String type2 = types.get(new OffsetRange(23, 39));
        assertNotNull(type2);
        assertEquals("Assert\\MinLength", type2);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    @Assert\\NotBlank, type=\"Foo\\Bar\" ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("@Assert\\NotBlank, type=\"Foo\\Bar\"", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(5, 20));
        assertNotNull(type1);
        assertEquals("Assert\\NotBlank", type1);
        String type2 = types.get(new OffsetRange(28, 35));
        assertNotNull(type2);
        assertEquals("Foo\\Bar", type2);
    }

}
