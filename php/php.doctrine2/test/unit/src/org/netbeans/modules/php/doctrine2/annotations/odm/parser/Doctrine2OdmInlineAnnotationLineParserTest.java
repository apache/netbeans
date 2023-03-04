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
package org.netbeans.modules.php.doctrine2.annotations.odm.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Doctrine2OdmInlineAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public Doctrine2OdmInlineAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = Doctrine2OdmInlineAnnotationLineParser.getDefault();
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Index(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Index(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 19));
        assertEquals("Index", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Foo\\Index(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Foo\\Index(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 23));
        assertEquals("Foo\\Index", type1);
    }

    public void testInvalidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Blah\\Blah(name=\"user_idx\", columns={\"email\"})} ");
        assertNull(parsedLine);
    }

}
