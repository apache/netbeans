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
package org.netbeans.modules.jumpto.common;

import java.awt.Color;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Tests if the pattern will be highlighted the correct way.
 * @author markiewb
 */
public class HighlightingNameFormatterTest {

    private Color fg = Color.WHITE;
    private Color bg = Color.BLACK;
    private HighlightingNameFormatter cut;

    @Before
    public void before() {
        cut = HighlightingNameFormatter.Builder.create().buildCustomFormatter("[%s]");    //NOI18N
    }

    @Test
    public void testFormatTypeName_CamelCase() {
        String typeName = "AbstractDummyBarTest";

        assertEquals("[AbstractDummyBarTest]", cut.formatName(typeName, "AbstractDummyBarTest", true));
        assertEquals("[Abstra]ct[D]ummy[B]arTest", cut.formatName(typeName, "AbstraDB", true));
        assertEquals("[A]bstract[Dum]my[B]arTest", cut.formatName(typeName, "ADumB", true));
        assertEquals("[A]bstract[D]ummy[Ba]rTest", cut.formatName(typeName, "ADBa", true));
        assertEquals("[A]bstract[D]ummy[B]ar[Test]", cut.formatName(typeName, "ADBTest", true));
        assertEquals("[Ab]stract[Du]mmy[B]ar[Test]", cut.formatName(typeName, "AbDuBTest", true));
    }

    @Test
    public void testRegExp() {
        String typeName = "PrinterMakeAndModel";
        assertEquals("PrinterM[a]keAnd[Model]", cut.formatName(typeName, "*a??Model", false));
    }

    @Test
    public void testFormatTypeName_NullOrEmpty() {
        String typeName = "AbstractDummyBarTest";
        assertEquals("AbstractDummyBarTest", cut.formatName(typeName, null, true));
        assertEquals("AbstractDummyBarTest", cut.formatName(typeName, "", true));
    }

    @Test
    public void testFormatTypeName_Wildcard_CaseSensitive() {
        String typeName = "AbstractDummyBarTest";

        assertEquals("[A]bstractDummyBar[Test]", cut.formatName(typeName, "A*Test", true));
        assertEquals("[A]bstractDummy[B]ar[Test]", cut.formatName(typeName, "A*B*Test", true));
        assertEquals("[A]bstractDummy[BarTest]", cut.formatName(typeName, "A*Bar*Test", true));
    }

    @Test
    public void testFormatTypeName_Wildcard_CaseInSensitive() {
        String typeName = "AbstractDummyBarTest";

        assertEquals("[A]bstractDummyBar[Test]", cut.formatName(typeName, "A*Test", false));
        assertEquals("[Ab]stractDummyBar[Test]", cut.formatName(typeName, "A*B*Test", false));
        assertEquals("[A]bstractDummy[BarTest]", cut.formatName(typeName, "A*Bar*Test", false));
    }

    @Test
    public void testFormatTypeName_FullFormat() {
        cut = HighlightingNameFormatter.Builder.create().buildColorFormatter(fg, bg);
        String typeName = "AbstractDummyBarTest";
        assertEquals("<font style=\"background-color:ffffff; font-weight:bold; color:000000; white-space:nowrap\">A</font>bstract<font style=\"background-color:ffffff; font-weight:bold; color:000000; white-space:nowrap\">D</font>ummy<font style=\"background-color:ffffff; font-weight:bold; color:000000; white-space:nowrap\">B</font>ar<font style=\"background-color:ffffff; font-weight:bold; color:000000; white-space:nowrap\">Test</font>", cut.formatName(typeName, "ADBTest", false));
    }

}
