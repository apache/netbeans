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

package org.netbeans.test.web.core.syntax;

import java.io.PrintStream;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.modules.web.core.syntax.deprecated.HtmlSyntax;
import org.netbeans.editor.ext.java.JavaSyntax;
import org.netbeans.junit.NbTestCase;

import org.netbeans.modules.web.core.syntax.deprecated.Jsp11Syntax;

/** Basic jsp multisyntax parser tests.
 *
 * @author  mf100882
 */
public class JspMultiSyntaxTest extends NbTestCase {
    
    //it's static since the junit creates a new instance of this class for each test method
    private static Jsp11Syntax syntax = new Jsp11Syntax(new HtmlSyntax(), new JavaSyntax());
    
    public JspMultiSyntaxTest() {
        super("jspmultisyntaxtest");
    }
    
    public void setUp() {
        //print out a header to the ref file
        getRef().println("'token image' [offset, length]; tokenID name; tokenID id; token category name; <list of token context names>\n--------------------\n");
    }
    
    public void tearDown() {
        compareReferenceFiles();
    }
    
    //test methods -----------
    
    public void testHtml() {
        dumpTokensForContent("<html>\n<body>\n<h1>hello</h1>\n</body>\n</html>");
    }
    
    public void testJavaScripting() {
        dumpTokensForContent("<html><%! int a = 1; %>\n\n<br>\n<%=\"hello\"%>\n<br>\n<% String s = \"world\"; %>\n</html>");
    }

    public void testJspDeclaration() {
        dumpTokensForContent("<%@page contentType=\"text/html\"%>\n"+
                             "<%@page pageEncoding=\"UTF-8\"%>" + 
                             "<%@taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%>");
    }
    
    public void testExpressionLanguage() {
        dumpTokensForContent("<html>${pageContext.request.contextPath}\n<br>" + 
                             "${pageContext.request.contextPath}\n " +
                             "${header[\"host\"]} <br>\n" + 
                             "${requestScope['javax.servlet.forward.servlet_path']}\n</html>");
    }
    
    public void testBug53102() {
        dumpTokensForContent("<html>\n<head />\n<he");
    }
     
    public void testBug52942() {
        dumpTokensForContent("<a href=\"<%= 1 %>\"  >Destination</a>");
    }
    
    public void testBugWrongJsptagType() {
        dumpTokensForContent("\n<a >\n");
    }
    
    public void test50283_1() {
        dumpTokensForContent("< /jsp:element >"); //should be marked as an error
    }
     
    public void test50283_2() {
        dumpTokensForContent("</ jsp:element >"); //should be marked as an error
    }
    
    public void testJspComment() {
        dumpTokensForContent("<html><%-- text \n new line --%></html>\n");
    }
    
    public void testSimpleJspTag() {
        dumpTokensForContent("</jsp:useBean id=\"sss\">");
    }
    
    //helper methods -----------
    
    private void dumpTokensForContent(String content) {
        loadContentToSyntax(content);
        dumpTokensData(getRef()); //print output to reference stream
    }
    
    private void dumpTokensData(PrintStream out) {
        TokenID tokenID = null;
        char[] buffer = syntax.getBuffer();
        String tokenImage = null;
        TokenContextPath tcp = null;
        do {
            //acquire all token relevant data
            tokenID = syntax.nextToken();
            
            if( tokenID == null ) break;
            
            tokenImage = new String(buffer, syntax.getTokenOffset(), syntax.getTokenLength());
            tcp = syntax.getTokenContextPath();
            
            //print it
            out.print("'" + SyntaxUtils.normalize(tokenImage) + "' ["+syntax.getTokenOffset() + ", " + syntax.getTokenLength() + "]; " + tokenID.getName() + "; " + tokenID.getNumericID() + "; "+ (tokenID.getCategory() != null ? tokenID.getCategory().getName() : "-") + "; ");
            SyntaxUtils.dumpTokenContextPath(tcp, out);
            out.println();
            
        }
        while(true);
    }
    
    private void loadContentToSyntax(String content) {
        //load syntax - scan the whole buffer - the buffer is last one
        char[] buffer = content.toCharArray();
        syntax.load(null, buffer, 0, buffer.length, true);
    }
    
}
