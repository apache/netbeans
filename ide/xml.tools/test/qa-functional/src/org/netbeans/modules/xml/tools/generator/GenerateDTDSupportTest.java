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
package org.netbeans.modules.xml.tools.generator;

import java.lang.reflect.Method;
import junit.textui.TestRunner;
import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tax.TreeDocument;
import org.netbeans.tax.TreeElement;
import org.netbeans.tests.xml.XTest;
import org.openide.filesystems.FileObject;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: GenerateDTDSupportTest
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 * GenerateDTDSupportTest checks Generate DTD action on XML document without DTD. The action is
 * accesible from popup menu on all element nodes.<BR>
 *
 * <BR><B>How it works:</B><BR>
 * Test opens XML document, generates DTD for document root element and writes the DTD into log.<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * None
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * DTD for the XML document.<BR>
 * <BR><B>Possible reasons of failure:</B>
 * <UL>
 * <LI type="circle">
 * <I>None<BR></I>
 * </LI>
 * </UL>
 * <BR><B>To Do:</B>
 * <UL>
 * <LI type="circle">
 * Test Generate DTD action on XML document with DTD (regenerate DTD).<BR>
 * </LI>
 * <LI type="circle">
 * Test Generate DTD action on different elements (no only on root element). <BR>
 * </LI>
 * </UL>
 * <P>
 */

public class GenerateDTDSupportTest extends XTest {
    
    /** Creates new GenerateDTDSupportTest */
    public GenerateDTDSupportTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        XMLDataObject dao = (XMLDataObject) TestUtil.THIS.findData("Node00.xml");
        if (dao == null) {
            fail("\"data/Node00.xml\" data object is not found!");
        }
        TreeEditorCookie cake = (TreeEditorCookie) dao.getCookie(TreeEditorCookie.class);
        TreeElement element = ((TreeDocument)cake.openDocumentRoot()).getDocumentElement();
        FileObject primFile = dao.getPrimaryFile();
        String name = primFile.getName() + "_" + element.getQName();
        FileObject folder = primFile.getParent();
        String encoding = null;
        try {
            encoding = element.getOwnerDocument().getEncoding();
        } catch (NullPointerException e) { /* NOTHING */ }
        
        GenerateDTDSupport gen = new GenerateDTDSupport(dao);
        // Original: String result = gen.xml2dtd (element, name, encoding);
        Method m = gen.getClass().getDeclaredMethod("xml2dtd", new Class[] {String.class, String.class});
        m.setAccessible(true);
        String result = (String) m.invoke(gen, new Object[] {name, encoding});
        
        ref(result);
        compareReferenceFiles();
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(GenerateDTDSupportTest.class);
    }
}
