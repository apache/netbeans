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
public class OneToOneLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public OneToOneLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = new TypedParametersAnnotationLineParser();
    }

    public void testIsAnnotationParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsOneToOneParsedLine_01() throws Exception {
        assertTrue(parser.parse("OneToOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsOneToOneParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\OneToOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsOneToOneParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\OneToOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsOneToOneParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\OneToOne(targetEntity=\"Customer\")") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("OneToOnes"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("OneToOne");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("OneToOne", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("OneToOne   ");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("OneToOne", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("OneToOne\t\t  ");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("OneToOne", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("OneToOne(targetEntity=\"Customer\")");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Customer\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("OneToOne", type1);
        String type2 = types.get(new OffsetRange(23, 31));
        assertEquals("Customer", type2);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\OneToOne(targetEntity=\"Customer\")  \t");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Customer\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 20));
        assertEquals("Annotations\\OneToOne", type1);
        String type2 = types.get(new OffsetRange(35, 43));
        assertEquals("Customer", type2);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\OneToOne(targetEntity=\"Customer\")  \t");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Customer\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("\\Foo\\Bar\\OneToOne", type1);
        String type2 = types.get(new OffsetRange(32, 40));
        assertEquals("Customer", type2);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("onetoone");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("onetoone", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\onetoone(targetEntity=\"Customer\")  \t");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Customer\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("\\Foo\\Bar\\onetoone", type1);
        String type2 = types.get(new OffsetRange(32, 40));
        assertEquals("Customer", type2);
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\onetoone(targetEntity=\"Customer::class\")  \t");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Customer::class\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("\\Foo\\Bar\\onetoone", type1);
        String type2 = types.get(new OffsetRange(32, 40));
        assertEquals("Customer", type2);
    }

    public void testValidUseCase_10() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\onetoone(targetEntity=Customer::class)  \t");
        assertEquals("OneToOne", parsedLine.getName());
        assertEquals("(targetEntity=Customer::class)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("\\Foo\\Bar\\onetoone", type1);
        String type2 = types.get(new OffsetRange(31, 39));
        assertEquals("Customer", type2);
    }

}
