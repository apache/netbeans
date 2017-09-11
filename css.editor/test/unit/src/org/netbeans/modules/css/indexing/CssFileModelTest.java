/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.indexing;

import java.util.Collection;
import java.util.Iterator;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssFileModelTest extends CssTestBase {

    private static final CssLanguage CSS_LANGUAGE = new CssLanguage();

    public CssFileModelTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CssParserResult.IN_UNIT_TESTS = true;
    }

    public void testBasic() throws ParseException {
        String code = ".myclass { color: red }  #myid { color: blue }";
        //             01234567890123456789012345678901234567890123456789
        //             0         1         2         3         4
        Document doc = getDocument(code);
        assertNotNull(doc);

        Source source = Source.create(doc);
        assertNotNull(source);

        assertEquals("text/css", source.getMimeType());

        CssFileModel model = CssFileModel.create(source);
        assertNotNull(model);

        Collection<Entry> classes = model.getClasses();
        assertNotNull(classes);

        assertEquals(1, classes.size());
        Entry e = classes.iterator().next();

        assertNotNull(e);
        assertEquals("myclass", e.getName());
        assertTrue(e.isValidInSourceDocument());
        assertEquals(new OffsetRange(9, 23), e.getBodyRange());
        assertEquals(new OffsetRange(9, 23), e.getDocumentBodyRange());
        assertEquals(0, e.getLineOffset());
        assertFalse(e.isVirtual());
        assertEquals(code, e.getLineText());
        assertEquals(new OffsetRange(1, 8), e.getRange());
        assertEquals(new OffsetRange(1, 8), e.getDocumentRange());

        Collection<Entry> ids = model.getIds();
        assertNotNull(ids);

        assertEquals(1, ids.size());
        e = ids.iterator().next();
        assertNotNull(e);
        assertEquals("myid", e.getName());
    }

    public void testBasicInEmbeddedCss() throws ParseException {
        String code = "<html><head><title>x</title><style>.myclass { color: red }  #myid { color: blue }</style></head></html>";
        //             0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        //             0         1         2         3         4         5         6         7         8         9

        Document doc = getDocument(code, "text/html");
        assertNotNull(doc);

        Source source = Source.create(doc);
        assertNotNull(source);
        assertEquals("text/html", source.getMimeType());

        CssFileModel model = CssFileModel.create(source);
        assertNotNull(model);

        Collection<Entry> classes = model.getClasses();
        assertNotNull(classes);

        assertEquals(1, classes.size());
        Entry e = classes.iterator().next();

        assertNotNull(e);
        assertEquals("myclass", e.getName());
        assertTrue(e.isValidInSourceDocument());
        assertEquals(new OffsetRange(9, 23), e.getBodyRange());
        assertEquals(new OffsetRange(44, 58), e.getDocumentBodyRange());
        assertEquals(0, e.getLineOffset());
        assertFalse(e.isVirtual());
        assertEquals(".myclass { color: red }  #myid { color: blue }", e.getLineText());
        assertEquals(new OffsetRange(1, 8), e.getRange());
        assertEquals(new OffsetRange(36, 43), e.getDocumentRange());

        Collection<Entry> ids = model.getIds();
        assertNotNull(ids);

        assertEquals(1, ids.size());
        e = ids.iterator().next();
        assertNotNull(e);
        assertEquals("myid", e.getName());
    }

    public void testNoCssCode() throws ParseException {
        String code = "";
        Document doc = getDocument(code);
        assertNotNull(doc);

        Source source = Source.create(doc);
        assertNotNull(source);

        CssFileModel model = CssFileModel.create(source);
        assertNotNull(model);

        Collection<Entry> classes = model.getClasses();
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
        Collection<Entry> ids = model.getIds();
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    public void testImports() throws ParseException {
        //SASS import allows multiple resources in one import
        try {
            setScssSource();

            String code = "@import \"file1\", \"file2\";";
            CssParserResult result = TestUtil.parse(code);
            assertResultOK(result);
                    
            CssFileModel model = CssFileModel.create(result);
            assertNotNull(model);

            Collection<Entry> imports = model.getImports();
            assertNotNull(imports);

            assertEquals(2, imports.size());

            Iterator<Entry> entries = imports.iterator();

            assertTrue(entries.hasNext());
            Entry i1 = entries.next();
            assertNotNull(i1);
            assertEquals("file1", i1.getName());

            assertTrue(entries.hasNext());
            Entry i2 = entries.next();
            assertNotNull(i2);
            assertEquals("file2", i2.getName());
        } finally {
            setPlainSource();
        }
    }

    @Override
    protected String getPreferredMimeType() {
        return CssLanguage.CSS_MIME_TYPE;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return CSS_LANGUAGE;
    }
}