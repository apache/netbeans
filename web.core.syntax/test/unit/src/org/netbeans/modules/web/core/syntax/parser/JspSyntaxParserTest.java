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
package org.netbeans.modules.web.core.syntax.parser;

import java.util.Iterator;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.test.web.core.syntax.TestBase;
import org.openide.filesystems.FileObject;
import static org.junit.Assert.*;

/**
 *
 * @author marekfukala
 */
public class JspSyntaxParserTest extends TestBase {

    private static final String DATA_DIR_BASE = "syntaxparser/";

    public JspSyntaxParserTest(String name) {
        super(name);
    }

    public void testTag() {
        String content = "<jsp:useBean class=\"java.util.List\" > hello </jsp:useBean>";
        //                0123456789012345678 901234567890123 456789012345678901234567890123456789
        //                0         1          2         3          4         5         6
        Result result = JspSyntaxParser.parse(content);

        assertNotNull(result);

        List<JspSyntaxElement> elements = result.elements();
        assertNotNull(elements);

        assertEquals(3, elements.size());

        //first
        JspSyntaxElement el = elements.get(0);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.OPENTAG, el.kind());
        assertEquals("<jsp:useBean class=\"java.util.List\" >", el.text());
        JspSyntaxElement.OpenTag openTag = (JspSyntaxElement.OpenTag) el;
        assertEquals("jsp:useBean", openTag.name());
        assertFalse(openTag.isEmpty());
        assertEquals(0, openTag.from());
        assertEquals(37, openTag.to());
        List<JspSyntaxElement.Attribute> attrs = openTag.attributes();
        assertNotNull(attrs);
        assertEquals(1, attrs.size());
        JspSyntaxElement.Attribute attr = attrs.get(0);
        assertNotNull(attr);
        assertEquals("class", attr.getName());
        assertEquals(13, attr.getNameOffset());
        assertEquals("\"java.util.List\"", attr.getValue());
        assertEquals(19, attr.getValueOffset());
        assertEquals("\"java.util.List\"".length(), attr.getValueLength());

        //second
        el = elements.get(1);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());
        //text shared element - no offset can be obtained
        try {
            el.from();
            assertTrue(false);
        } catch (AssertionError e) {
            //ok
        }
        try {
            el.to();
            assertTrue(false);
        } catch (AssertionError e) {
            //ok
        }

        assertEquals("<n/a>", el.text());

        //third
        el = elements.get(2);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.ENDTAG, el.kind());
        JspSyntaxElement.EndTag endTag = (JspSyntaxElement.EndTag) el;
        assertEquals("jsp:useBean", endTag.name());
        assertEquals(44, endTag.from());
        assertEquals(58, endTag.to());

    }

    public void testMoreTags() {
        String content = "<jsp:x><jsp:useBean class=\"java.util.List\" > hello </jsp:useBean></jsp:x>";
        Result result = JspSyntaxParser.parse(content);

        assertNotNull(result);

        List<JspSyntaxElement> elements = result.elements();
        assertNotNull(elements);

        assertEquals(5, elements.size());

        Iterator<JspSyntaxElement> els = elements.iterator();
        JspSyntaxElement el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.OPENTAG, el.kind());
        assertEquals("jsp:x", ((JspSyntaxElement.Named) el).name());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.OPENTAG, el.kind());
        assertEquals("jsp:useBean", ((JspSyntaxElement.Named) el).name());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.ENDTAG, el.kind());
        assertEquals("jsp:useBean", ((JspSyntaxElement.Named) el).name());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.ENDTAG, el.kind());
        assertEquals("jsp:x", ((JspSyntaxElement.Named) el).name());

    }

    public void testDirective() {
        String content = "prefix <%@page import=\"java.util.List\" %> postfix";
        //                0123456789012345678901 234567890123456 789012345678901234567890123456789
        //                0         1         2          3          4        5         6
        Result result = JspSyntaxParser.parse(content);

        assertNotNull(result);

        List<JspSyntaxElement> elements = result.elements();
        assertNotNull(elements);

        assertEquals(3, elements.size());

        //second - the directive
        JspSyntaxElement el = elements.get(1);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.DIRECTIVE, el.kind());
        assertEquals("<%@page import=\"java.util.List\" %>", el.text());
        JspSyntaxElement.Directive dir = (JspSyntaxElement.Directive) el;
        assertEquals("page", dir.name());
        assertEquals(7, dir.from());
        assertEquals(41, dir.to());
        List<JspSyntaxElement.Attribute> attrs = dir.attributes();
        assertNotNull(attrs);
        assertEquals(1, attrs.size());
        JspSyntaxElement.Attribute attr = attrs.get(0);
        assertNotNull(attr);
        assertEquals("import", attr.getName());
        assertEquals(15, attr.getNameOffset());
        assertEquals("\"java.util.List\"", attr.getValue());
        assertEquals(22, attr.getValueOffset());
        assertEquals("\"java.util.List\"".length(), attr.getValueLength());

        //text elements around
        el = elements.get(0);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());

        el = elements.get(2);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());

    }

    public void testMoreDirectives() {
        String content = "<%@page class=\"java.util.List\" %><%@taglib %>";
        Result result = JspSyntaxParser.parse(content);

        assertNotNull(result);

        List<JspSyntaxElement> elements = result.elements();
        assertNotNull(elements);

        assertEquals(2, elements.size());

        Iterator<JspSyntaxElement> els = elements.iterator();
        JspSyntaxElement el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.DIRECTIVE, el.kind());
        assertEquals("page", ((JspSyntaxElement.Named) el).name());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.DIRECTIVE, el.kind());
        assertEquals("taglib", ((JspSyntaxElement.Named) el).name());

    }

    public void testComment() {
        String content = "prefix<%-- comment --%>postfix";
        //                0123456789012345678901234567890
        //                0         1          2        3
        Result result = JspSyntaxParser.parse(content);

        assertNotNull(result);

        List<JspSyntaxElement> elements = result.elements();
        assertNotNull(elements);

        assertEquals(3, elements.size());

        //first
        JspSyntaxElement el = elements.get(0);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());

        //second - comment
        el = elements.get(1);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.COMMENT, el.kind());
        assertEquals("<%-- comment --%>", el.text());
        assertEquals(6, el.from());
        assertEquals(23, el.to());

        //third
        el = elements.get(2);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());
    }

    public void testMix() {
        String content = "<%@page %><jsp:x>text<%-- comment --%></jsp:x>";
        Result result = JspSyntaxParser.parse(content);

        assertNotNull(result);

        List<JspSyntaxElement> elements = result.elements();
        assertNotNull(elements);

        assertEquals(5, elements.size());

        Iterator<JspSyntaxElement> els = elements.iterator();
        JspSyntaxElement el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.DIRECTIVE, el.kind());
        assertEquals("page", ((JspSyntaxElement.Named) el).name());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.OPENTAG, el.kind());
        assertEquals("jsp:x", ((JspSyntaxElement.Named) el).name());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.COMMENT, el.kind());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.ENDTAG, el.kind());
        assertEquals("jsp:x", ((JspSyntaxElement.Named) el).name());

    }

    public void testCase001() throws Exception {
        testFile("case001.jsp");
    }

     public void testScripting() {
        String content = "prefix<% java %>postfix";
        //                0123456789012345678901234567890
        //                0         1          2        3
        Result result = JspSyntaxParser.parse(content);

        assertNotNull(result);

        List<JspSyntaxElement> elements = result.elements();
        assertNotNull(elements);

        assertEquals(3, elements.size());

        //first
        JspSyntaxElement el = elements.get(0);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());

        //second - comment
        el = elements.get(1);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.SCRIPTING, el.kind());
        assertEquals("<% java %>", el.text());
        assertEquals(6, el.from());
        assertEquals(16, el.to());

        //third
        el = elements.get(2);
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());
    }
    
    private void testFile(String testFile) throws Exception {
        FileObject source = getTestFile(DATA_DIR_BASE + testFile);
        BaseDocument doc = getDocument(source);
        String code = doc.getText(0, doc.getLength());
        Result result = JspSyntaxParser.parse(code);
        assertNotNull(result);
        StringBuffer output = new StringBuffer();
        dump(result, output);
        assertDescriptionMatches(source, output.toString(), false, ".pass", true);
    }

    private void dump(Result result, StringBuffer output) {
        List<JspSyntaxElement> elements = result.elements();
        for(JspSyntaxElement el : elements) {
            output.append(el);
            output.append('\n');
        }
    }
    
}
