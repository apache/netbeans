/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *   markiewb@netbeans.org
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
