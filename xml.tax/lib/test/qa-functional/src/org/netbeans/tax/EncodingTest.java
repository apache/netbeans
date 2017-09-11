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
package org.netbeans.tax;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import junit.textui.TestRunner;
import org.netbeans.modules.xml.XMLDataObject;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tests.xml.XTest;
import org.openide.cookies.CloseCookie;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: Encoding Test
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 * Tests check whether the documents saved with different encodings are identical.<BR>
 *
 * <BR><B>How it works:</B><BR>
 * Test doing for each encoding:<BR>
 * - save document with selected encoding<BR>
 * - close the document<BR>
 * - reload the document from disk<BR>
 * - check if the reloaded document and the original are identical<BR>
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
 * <P>
 */

public class EncodingTest extends XTest {
    /** Creates new CoreSettingsTest */
    public EncodingTest(String testName) {
        super(testName);
    }
    
    public void testEncoding() throws Exception {
        final String DATA_OBJECT = "encoding.xml";
        final String NDATA_OBJECT = "newEncoding.xml";
        TreeEditorCookie cake;
        
        // prepare data
        XMLDataObject original = (XMLDataObject) TestUtil.THIS.findData(DATA_OBJECT);
        if (original == null) {
            fail("\"" + DATA_OBJECT + "\" data object not found!");
        }
        cake = (TreeEditorCookie) original.getCookie(TreeEditorCookie.class);        
        TreeElement docRoot = ((TreeDocument)cake.openDocumentRoot()).getDocumentElement();
        String defEncoding =  cake.getDocumentRoot().getEncoding();
        String gString = TestUtil.THIS.nodeToString(docRoot);
        Iterator encodings = TreeUtilities.getSupportedEncodings().iterator();
        
        // prepare workdir
        File workDir = getWorkDir();
        try {
            clearWorkDir();
        } catch (IOException ex) {
            log("clearWorkDir() throws: " + ex);
        }
        
        FileSystem fs = TestUtil.THIS.mountDirectory(workDir);
        DataFolder dataFolder = DataFolder.findFolder(fs.getRoot());
        
        while (encodings.hasNext()) {
            String encoding = (String) encodings.next();
            String fileName = encoding + ".xml";
            try {
                dbg.println("Testing encoding: " + encoding + " ... ");
                if (encoding.equals(defEncoding)) break;  // Nothing to test.
                
                // create new document, set encoding, save and close it
                XMLDataObject xdao = (XMLDataObject) original.createFromTemplate(dataFolder, encoding);
                cake = (TreeEditorCookie) xdao.getCookie(TreeEditorCookie.class);        
                TreeDocument newDoc = (TreeDocument) cake.openDocumentRoot();
                newDoc.setEncoding(encoding);
                TestUtil.THIS.saveDataObject(xdao);
                CloseCookie cc = (CloseCookie) xdao.getCookie(CloseCookie.class);
                cc.close();
                
                // read the document and check his content
                cake = (TreeEditorCookie) xdao.getCookie(TreeEditorCookie.class);        
                TreeElement newRoot = ((TreeDocument) cake.getDocumentRoot()).getDocumentElement();
                String nString = TestUtil.THIS.nodeToString(newRoot);
                assertEquals("Encoding: " + encoding + ", documents are differ", gString, nString);
            } catch (Exception ex) {
                ex.printStackTrace(dbg);
                fail("Encoding: " + encoding + ", test faill due:\n" + ex);
            }
        }
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        DEBUG = true;
        TestRunner.run(EncodingTest.class);
    }
}
