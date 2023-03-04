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

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SequenceGeneratorLineParserTest extends NbTestCase {
    private ParameterizedAnnotationLineParser parser;

    public SequenceGeneratorLineParserTest(String name) {
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

    public void testReturnValueIsSequenceGeneratorParsedLine_01() throws Exception {
        assertTrue(parser.parse("SequenceGenerator") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsSequenceGeneratorParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\SequenceGenerator") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsSequenceGeneratorParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\SequenceGenerator") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsSequenceGeneratorParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\SequenceGenerator(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("SequenceGenerators"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("SequenceGenerator");
        assertEquals("SequenceGenerator", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("SequenceGenerator", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("SequenceGenerator   ");
        assertEquals("SequenceGenerator", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("SequenceGenerator", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("SequenceGenerator\t\t  ");
        assertEquals("SequenceGenerator", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("SequenceGenerator", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("SequenceGenerator(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)");
        assertEquals("SequenceGenerator", parsedLine.getName());
        assertEquals("(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("SequenceGenerator", type1);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\SequenceGenerator(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)  \t");
        assertEquals("SequenceGenerator", parsedLine.getName());
        assertEquals("(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 29));
        assertEquals("Annotations\\SequenceGenerator", type1);

    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\SequenceGenerator(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)  \t");
        assertEquals("SequenceGenerator", parsedLine.getName());
        assertEquals("(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 26));
        assertEquals("\\Foo\\Bar\\SequenceGenerator", type1);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("sequencegenerator");
        assertEquals("SequenceGenerator", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("sequencegenerator", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\sequencegenerator(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)  \t");
        assertEquals("SequenceGenerator", parsedLine.getName());
        assertEquals("(sequenceName=\"tablename_seq\", initialValue=1, allocationSize=100)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 26));
        assertEquals("\\Foo\\Bar\\sequencegenerator", type1);
    }

}
