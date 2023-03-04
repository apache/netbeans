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
package org.netbeans.modules.php.editor.parser.annotation;

import java.util.Collections;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LinkLineParserTest extends PHPTestBase {
    private LinkLineParser parser;

    public LinkLineParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new LinkLineParser();
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link http://www.seznam.cz");
        assertEquals("link", parsedLine.getName());
        assertEquals("http://www.seznam.cz", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link ");
        assertEquals("link", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link\t");
        assertEquals("link", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link\thttp://www.seznam.cz");
        assertEquals("link", parsedLine.getName());
        assertEquals("http://www.seznam.cz", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link           http://www.seznam.cz     ");
        assertEquals("link", parsedLine.getName());
        assertEquals("http://www.seznam.cz", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link\t    \thttp://www.seznam.cz   \t   ");
        assertEquals("link", parsedLine.getName());
        assertEquals("http://www.seznam.cz", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link");
        assertEquals("link", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testInvalidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("@link http://www.seznam.cz");
        assertNull(parsedLine);
    }

    public void testInvalidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("foo http://www.seznam.cz");
        assertNull(parsedLine);
    }

    public void testInvalidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("linked http://www.seznam.cz");
        assertNull(parsedLine);
    }

}
