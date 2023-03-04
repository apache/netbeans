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

package org.netbeans.modules.html.validation;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.validation.LinesMapper.Line;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class LinesMapperTest extends NbTestCase{

    public LinesMapperTest(String name) {
        super(name);
    }

    public void testOneLineCodeWithoutEOL() throws SAXException {
        String code = "<div>ahoj</div>";
        LinesMapper lm = new LinesMapper();
        lm.start();
        lm.characters(code.toCharArray(), 0, code.length());
        lm.end();

        assertEquals(1, lm.getLinesCount());
        Line l = lm.getLine(0);

        assertNotNull(l);

        assertEquals(0, l.getOffset());
        assertEquals(code.length(), l.getEnd());
        assertEquals(code, l.getText());
        assertEquals(code, l.getTextWithEndLineChars());
    }

    public void testOneLineCodeWithLF() throws SAXException {
        String code = "<div>ahoj</div>\n";
        LinesMapper lm = new LinesMapper();
        lm.start();
        lm.characters(code.toCharArray(), 0, code.length());
        lm.end();

        assertEquals(2, lm.getLinesCount());
        Line l = lm.getLine(0);

        assertNotNull(l);

        assertEquals(0, l.getOffset());
        assertEquals(code.length(), l.getEnd());
        assertEquals(code.substring(0, code.length() - 1), l.getText());
        assertEquals(code, l.getTextWithEndLineChars());
    }

     public void testOneLineCodeWithCRLF() throws SAXException {
        String code = "<div>ahoj</div>\r\n";
        LinesMapper lm = new LinesMapper();
        lm.start();
        lm.characters(code.toCharArray(), 0, code.length());
        lm.end();

        assertEquals(2, lm.getLinesCount());
        Line l = lm.getLine(0);

        assertNotNull(l);

        assertEquals(0, l.getOffset());
        assertEquals(code.length(), l.getEnd());
        assertEquals(code.substring(0, code.length() - 2), l.getText());
        assertEquals(code, l.getTextWithEndLineChars());
    }

}