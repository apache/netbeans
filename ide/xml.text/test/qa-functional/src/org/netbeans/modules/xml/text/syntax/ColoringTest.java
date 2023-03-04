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
package org.netbeans.modules.xml.text.syntax;

import org.openide.execution.NbfsURLConnection;
import org.openide.loaders.DataFolder;
import org.netbeans.tax.*;
import org.netbeans.modules.xml.DTDDataObject;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.EditorCookie;

import org.netbeans.modules.xml.XMLDataObject;
import org.openide.nodes.CookieSet;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.tests.xml.XTest;
import org.openide.loaders.DataObject;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: CreateSimpleXML
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * This test creates simple XML document with DTD and writes it into output.
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * 1) create empty XML document from template<BR>
 * 2) create new Document Type and add it into document<BR>
 * 3) append XML elements<BR>
 * 4) write the document into output<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * XML document with DTD.<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on December 20, 2000, 12:33 PM
 * <P>
 */
public class ColoringTest extends XTest {
    private static String XML_TEMPLATE = "XML/XMLwithDTD.xml";
    private static String DOCUMENT_NAME = "Books";
    private static String DTD_SYS_ID = "simple.dtd";
    private static String INTERNAL_DTD = "internalDTD.dtd";
    private static int TREE_LEVELS = 3;
    
    /** Creates new CoreSettingsTest */
    public ColoringTest(String testName) {
        super(testName);
    }
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
    public void testXMLColoring() throws Exception {
        dumpTokens("XMLColoring", "xml");
    }
    
    public void testDTDColoring() throws Exception {
        dumpTokens("DTDColoring", "dtd");
    }
    
    public void testCSSColoring() throws Exception {
        dumpTokens("CSSColoring", "css");
    }
    
    // LIBS ////////////////////////////////////////////////////////////////////
    
    public void dumpTokens(String fileName, String ext) throws Exception {
        String pkgName = getClass().getPackage().getName();
        DataObject obj = TestUtil.THIS.findDataObject(pkgName + ".data", fileName, ext);
        EditorCookie ed = (EditorCookie) obj.getCookie(EditorCookie.class);
        BaseDocument doc = (BaseDocument) ed.openDocument();
        ExtSyntaxSupport ess = (ExtSyntaxSupport) doc.getSyntaxSupport();
        TokenItem token = ess.getTokenChain(0, doc.getLength());
        
        while (token != null) {
            TokenID tokenID = token.getTokenID();
            ref(tokenID.getName()+ ": " + token.getImage());
            token = token.getNext();
        }
        compareReferenceFiles();
    }
    
    // MAIN ////////////////////////////////////////////////////////////////////
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(ColoringTest.class);
    }
}
