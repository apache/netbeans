/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.lib;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.html.editor.lib.api.HtmlParserFactory;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementsIterator;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.plain.TextElement;
import org.netbeans.modules.html.editor.lib.test.TestBase;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author marekfukala
 */
public class XmlSyntaxTreeBuilderTest extends TestBase {
    
    public XmlSyntaxTreeBuilderTest(String name) {
        super(name);
    }

    public void testBasic() {
        String code = "<x></x>";
        Node root = parse(code, HtmlModelFactory.getModel(HtmlVersion.HTML5));

        Element div = ElementUtils.query(root, "x"); 
        assertNotNull(div);
    } 
    
    public void testNodesHasParentsSet() {
        String code = "<x><y><z/></y></x>";
        Node root = parse(code, HtmlModelFactory.getModel(HtmlVersion.HTML5));

        Element x = ElementUtils.query(root, "x"); 
        assertNotNull(x);
        Element y = ElementUtils.query(root, "x/y"); 
        assertNotNull(y);
        Element z = ElementUtils.query(root, "x/y/z"); 
        assertNotNull(z);
        
        assertNull(root.parent());
        assertEquals(root, x.parent());
        assertEquals(x, y.parent());
        assertEquals(y, z.parent());
        
    }
     //Bug 197608 - Non-html tags offered as closing tags using code completion
    public void testIssue197608() throws ParseException, BadLocationException {
        String code = "<div></di";
        Node root = parse(code, HtmlModelFactory.getModel(HtmlVersion.HTML5));

        Element div = ElementUtils.query(root, "div"); 
        assertNotNull(div);
//        AstNodeUtils.dumpTree(root);
    } 
    
    public void testTextNodesInParseTree() throws ParseException, BadLocationException {
        String code = "<html>1<div>2</div>3</html>";
        //             012345678901234567890123456789
        //             0         1         2
        
        Properties p = new Properties();
        p.setProperty("add_text_nodes", "true");
        
        Node root = parse(code, p, HtmlModelFactory.getModel(HtmlVersion.HTML5));
//        ElementUtils.dumpTree(root);
        
        OpenTag html = ElementUtils.query(root, "html");
        
        Collection<Element> texts = html.children(ElementType.TEXT);
        assertNotNull(texts);
        assertEquals(2, texts.size());
        
        Iterator<Element> textsItr = texts.iterator();
        
        Element first = textsItr.next();
        assertNotNull(first);
        assertTrue(first.type() == ElementType.TEXT);
        
        Element second = textsItr.next();
        assertTrue(second.type() == ElementType.TEXT);
        assertNotNull(second);
        assertEquals(19, second.from());
        assertEquals(20, second.to());
        assertEquals("3", second.image().toString());
        
        
    }
    
    public void testEmptyTagSelfCloseSupport() throws ParseException, BadLocationException {
        String code = "<html><meta><div></div></html>";
        
        Properties p = new Properties();
        p.setProperty("follow_html_model", "true");
        
        Node root = parse(code, p, HtmlModelFactory.getModel(HtmlVersion.HTML5));

//        ElementUtils.dumpTree(root);
        
        Element div = ElementUtils.query(root, "html/div"); 
        assertNotNull(div);
    }
    
    private Node parse(String code, Object... lookupContent) {
        final HtmlSource source = new HtmlSource(code);
        ElementsIteratorHandle handle = new ElementsIteratorHandle() {
            @Override
            public Iterator<Element> getIterator() {
                return new ElementsIterator(source);
            }
        };
        InstanceContent ic = new InstanceContent();
        ic.add(handle);
        for(Object o : lookupContent) {
            ic.add(o);
        }
        return XmlSyntaxTreeBuilder.makeUncheckedTree(source, null, new AbstractLookup(ic));
    }

}
