/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
