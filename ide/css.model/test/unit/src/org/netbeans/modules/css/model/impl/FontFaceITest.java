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
package org.netbeans.modules.css.model.impl;

import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.FontFace;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class FontFaceITest extends ModelTestBase {

    public FontFaceITest(String name) {
        super(name);
    }

    public void testBasic() throws BadLocationException, ParseException {
        String code = "@font-face { font-family: Gentium; }";

        StyleSheet styleSheet = createStyleSheet(code);

//        TestUtil.dumpResult(TestUtil.parse(code));

        List<FontFace> ffs = styleSheet.getBody().getFontFaces();
        assertNotNull(ffs);
        assertEquals(1, ffs.size());

        FontFace ff = ffs.get(0);
        assertNotNull(ff);

        Declarations declarations = ff.getDeclarations();
        assertNotNull(declarations);

        List<Declaration> decls = declarations.getDeclarations();
        assertNotNull(decls);
        assertEquals(1, decls.size());

    }

    public void testAddRemove() {
        String code = "@font-face { font-family: Gentium; }";

        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);

        List<FontFace> ffs = styleSheet.getBody().getFontFaces();
        assertNotNull(ffs);
        assertEquals(1, ffs.size());

        FontFace fontFace = ffs.get(0);
        assertNotNull(fontFace);

        Declarations declarations = fontFace.getDeclarations();
        assertNotNull(declarations);

        List<Declaration> decls = declarations.getDeclarations();
        assertNotNull(decls);
        assertEquals(1, decls.size());

        styleSheet.getBody().removeFontFace(fontFace);

        ffs = styleSheet.getBody().getFontFaces();
        assertNotNull(ffs);
        assertEquals(0, ffs.size());

        assertEquals("", model.getModelSource().toString());

        ElementFactory ef = model.getElementFactory();
        PropertyDeclaration declaration = ef.createPropertyDeclaration(
                ef.createProperty("src"),
                ef.createPropertyValue(ef.createExpression("url(http://example.com/fonts/Gentium.ttf)")),
                false);
        declarations = ef.createDeclarations(declaration);
        fontFace = ef.createFontFace(declarations);

        styleSheet.getBody().addFontFace(fontFace);

        assertEquals("\n"
                + "@font-face {\n"
                + "    src: url(http://example.com/fonts/Gentium.ttf);\n"
                + "}\n", model.getModelSource().toString());

    }
}
