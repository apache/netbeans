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

package org.netbeans.test.gui.web.extbrowser.execution;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.NbFrameOperator;



import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.HTMLNode;


import org.netbeans.jellytools.actions.ExecuteAction;

import org.netbeans.junit.NbTestSuite;



import org.netbeans.jemmy.Waiter;

import org.netbeans.web.test.nodes.JSPNode;
import org.netbeans.web.test.nodes.ServletNode;
import org.netbeans.test.gui.web.util.JSPServletResponseWaitable;
import org.netbeans.test.gui.web.util.HttpRequestWaitable;
import org.netbeans.test.gui.web.util.BrowserUtils;



import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

public class ExecuteSwingBrowser extends JellyTestCase {
    private static String workDir = null;     
    private static String webModule = null;
    private static String wmName = "wm1";
    private static String fSep = System.getProperty("file.separator");
    private static String iSep = "|";
    private static String classes = "Classes";
    private static String servletForExecution = "ServletForExecution";
    private static String htmlForExecution = null;;
    private static String wmForExecution = "wmForExecution"; 
    private static String urlToRedirectFromHTML = "RedirectFromHtmlForExecution.html";
    private static String jspForExecution = null;
    private static String pkg = "execution";
    private static ExplorerOperator explorer = null;
    private String servletId = "cebde3e2-e8f1-4421-8a1c-df11dcc6e79a";
    private String jspId     = "c78eae2b-39f2-4b41-b2be-032e5373d7f4";
    private String wmId      = "9bc4ac0b-0a21-452a-9e51-ca9df3c2fa04";
    private int defaultPort = 1357;
    private int port = 2468;
    private String defaultAnswer = "HTTP/1.0 200 OK\nServer: FFJ Automated Tests SimpleServ\nLast-Modified: Fri, 12 Jul 2002 09:53:56 GMT\nContent-Length: 281\nConnection: close\nContent-Type: text/html\n\n<html>\n<head>\n   <title>Tests passed</title>\n</head>\n<body>\n<center><H1>Request Accepted</H1></center>\n</body>\n</html>";
    


    public ExecuteSwingBrowser(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
         
    //method required by JUnit
    public static junit.framework.Test suite() {
	workDir = System.getProperty("extbrowser.workdir").replace('/', fSep.charAt(0));
	webModule = workDir + fSep + wmName;
	htmlForExecution = webModule + iSep + "html" + iSep + "HtmlFileForExecution";
	jspForExecution = webModule + iSep + "jsp" + iSep + "JSPForExecution";
	pkg = webModule + iSep + "WEB-INF" + iSep + classes + iSep + pkg;
	return new NbTestSuite(ExecuteSwingBrowser.class);
    }
    
    public void testExecuteHtml() {
	NbFrameOperator fo = null;
	HTMLNode node1 = null;
	BrowserUtils.setSwingBrowser();
	try {
	    node1 = new HTMLNode(htmlForExecution);
	}catch(Exception e) {
	    fail("Not found: " + htmlForExecution);
	}
	new ExecuteAction().perform(node1);
	BrowserUtils.handleSwingBrowserDialog();
	try {
	    fo = new NbFrameOperator("Web Browser");
	}catch(Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started.");
	}
	JTextFieldOperator tf = new JTextFieldOperator(fo,0);
	if(tf.getText().indexOf("HtmlFileForExecution")==-1) {
	    fail("Wrong URL in SWING browser: " + tf.getText());
	}
	JTextComponentOperator tf1 = new JTextComponentOperator(fo,0);
	System.out.println("TEXTFIELD 1: " + tf1.getText());
	if(tf1.getText().indexOf("Html file executed correctly")==-1) {
	    fail("Wrong CONTENT shown in SWING browser: " + tf1.getText());
	}
    }
    
    public void testExecuteSevlet() {
	ServletNode node1 = null;
	BrowserUtils.setSwingBrowser();
	try {
	    node1 = new ServletNode(pkg + iSep + servletForExecution);
	}catch(Exception e) {
	    fail("Not found: " + servletForExecution);
	}
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(servletId, defaultAnswer, port);
	node1.execute();
	BrowserUtils.handleSwingBrowserDialog();
	Waiter w = new Waiter(jsrw);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }

    public void testExecuteJSP() {
	JSPNode node1 = null;
	BrowserUtils.setSwingBrowser();
	try {
	    node1 = new JSPNode(jspForExecution);
	}catch(Exception e) {
	    fail("Not found: " + jspForExecution);
	}
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(jspId, defaultAnswer, port);
	node1.execute();
	BrowserUtils.handleSwingBrowserDialog();
	Waiter w = new Waiter(jsrw);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }

    public void testExecuteWebModule() {
	FolderNode node1 = null;
	BrowserUtils.setSwingBrowser();
	try {
	    node1 = new FolderNode(workDir + fSep + wmForExecution + iSep + "WEB-INF");
	}catch(Exception e) {
	    fail("Web Module for execution not found");
	}
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(wmId, defaultAnswer, port);
	new ExecuteAction().perform(node1);
	BrowserUtils.handleSwingBrowserDialog();
	Waiter w = new Waiter(jsrw);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }
    
   
}
