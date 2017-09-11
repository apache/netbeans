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
package org.netbeans.modules.css.lib.api;

import javax.swing.text.BadLocationException;
import org.antlr.runtime.CommonToken;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.lib.Css3Lexer;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.TokenNode;
import static org.junit.Assert.*;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class NodeUtilTest extends NbTestCase {
    
    public NodeUtilTest(String name) {
        super(name);
    }

    public void test_getChildTokenNode() throws BadLocationException, ParseException {
        String code = "@import \"file.css\";";
        CssParserResult res = TestUtil.parse(code);
        
//        TestUtil.dumpResult(res);
        Node imports = NodeUtil.query(res.getParseTree(), "styleSheet/imports/importItem/resourceIdentifier"); 
        assertNotNull(imports);
        
        Node value = NodeUtil.getChildTokenNode(imports, CssTokenId.STRING);
        assertNotNull(value);
        
        assertEquals("\"file.css\"", value.image().toString());
    }
    
    public void test_getTrimmedNodeRange() {
        String source = " hello! ";
        //               012345678
        CommonToken token = new CommonToken(Css3Lexer.IDENT);
        token.setText(source);
        token.setStartIndex(0);
        token.setStopIndex(7); //len - 1 -> points to last char not the end!
        
        Node node = new TokenNode(source, token);
        
        assertEquals(" hello! ", node.image().toString());
        int[] result = NodeUtil.getTrimmedNodeRange(node);

        assertEquals(1, result[0]);
        assertEquals(7, result[1]);
        
    }
    
    public void test_getSelectorNodeRange() throws BadLocationException, ParseException {
        String code = "h1 { color: red; } ";
        //             01234567890123456789
        CssParserResult result = TestUtil.parse(code);
        
//        TestUtil.dumpResult(result);
        
        Node ruleSet = NodeUtil.query(result.getParseTree(), TestUtil.bodysetPath + "rule");
        assertNotNull(ruleSet);
        
        int[] range = NodeUtil.getRuleBodyRange(ruleSet);
        assertNotNull(range);
        
        assertEquals(3, range[0]);
        assertEquals(18, range[1]);
        
    }
    
    
    public void test_findNonTokenNodeAtOffset_on_error() throws BadLocationException, ParseException {
        String code = "@";
        //             01234567890123456789
        CssParserResult result = TestUtil.parse(code);
        
//        TestUtil.dumpResult(result);
        
        Node tokenNode = NodeUtil.findNodeAtOffset(result.getParseTree(), 1);
        assertNotNull(tokenNode);
        
        assertEquals(NodeType.token, tokenNode.type());
        
        Node node = NodeUtil.findNonTokenNodeAtOffset(result.getParseTree(), 1);
        assertNotNull(node);
        
        assertEquals(node, tokenNode.parent());
        
        assertEquals("error", node.name());
        
    }
    
    public void test_findNodeAtOffset() throws BadLocationException, ParseException {
        String code = "h1 { color: red; }";
        //             01234567890123456789
        CssParserResult result = TestUtil.parse(code);
        
//        TestUtil.dumpResult(result);
        
        Node tokenNode = NodeUtil.findNodeAtOffset(result.getParseTree(), 1);
        assertNotNull(tokenNode);
        
        assertEquals(NodeType.token, tokenNode.type());
        assertEquals("h1", tokenNode.image().toString());
    }
        
}
