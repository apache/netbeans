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
