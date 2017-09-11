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

import junit.textui.TestRunner;
import org.netbeans.modules.xml.DTDDataObject;
import org.netbeans.modules.xml.XMLDataObject;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tests.xml.XTest;
import org.openide.loaders.DataFolder;
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
public class CreateXMLTest extends XTest {
    private static String XML_EXT = "xml";
    private static String DOCUMENT_NAME = "Delme";
    private static String DTD_SYS_ID = "simpleXXL.dtd";
    private static String INTERNAL_DTD = "internalDTD.dtd";
    
    /** Creates new CoreSettingsTest */
    public CreateXMLTest(String testName) {
        super(testName);
    }
    
    public void testCreateXML() throws Exception {
        String content
        = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"
        + "<root/>\n";
        
        try {
            // delete document if exists
            DataObject dao = TestUtil.THIS.findData(DOCUMENT_NAME + '.' + XML_EXT);
            if (dao != null) dao.delete();
            // create new Data Object
            DataFolder dataFolder = DataFolder.findFolder(TestUtil.THIS.findData("").getPrimaryFile());
            XMLDataObject xmlDataObject = (XMLDataObject) TestUtil.createDataObject(dataFolder, DOCUMENT_NAME, XML_EXT, content);
            TreeEditorCookie cake = (TreeEditorCookie) xmlDataObject.getCookie(TreeEditorCookie.class);
            TreeDocument document = (TreeDocument) cake.openDocumentRoot();
            
            // Create Document Type
            DTDDataObject dtdDataObject = (DTDDataObject) TestUtil.THIS.findData(INTERNAL_DTD);
            cake = (TreeEditorCookie) dtdDataObject.getCookie(TreeEditorCookie.class);
            TreeDTD treeDTD = (TreeDTD) cake.openDocumentRoot();
            TreeDocumentType docType = new TreeDocumentType(DOCUMENT_NAME);
            docType.setSystemId(DTD_SYS_ID);
            TreeChild child = treeDTD.getFirstChild();
            while (child != null) {
                docType.appendChild((TreeChild) child.clone());
                child = child.getNextSibling();
            }
            document.setDocumentType(docType);
            
            // Create document
            TreeElement root = document.getDocumentElement();
            // Create root node
            root.addAttribute(new TreeAttribute("manager", "Tom Jerry"));
            root.addAttribute("id", "a");
            // Create node Product
            TreeElement product = new TreeElement("Product");
            root.appendChild(product);
            root.appendChild(new TreeText("\n"));
            product.addAttribute("isbn", "123456");
            product.addAttribute(new TreeAttribute("id", "b"));
            product.appendChild(new TreeText("\nXML Book\n"));
            // Create node Descript
            TreeElement descript = new TreeElement("Descript");
            product.appendChild(descript);
            product.appendChild(new TreeText("\n"));
            descript.addAttribute("lang", "Eng");
            descript.appendChild(new TreeText("\n"));
            descript.appendChild(new TreeText("The book describe how is using XML in"));
            descript.appendChild(new TreeText("\n"));
            descript.appendChild(new TreeGeneralEntityReference("company"));
            descript.appendChild(new TreeText("from "));
            descript.appendChild(new TreeGeneralEntityReference("cz"));
            descript.appendChild(new TreeText("\n"));
            descript.appendChild(new TreeText("Very important is author\n"));
            descript.appendChild(new TreeGeneralEntityReference("notice"));
            descript.appendChild(new TreeText("\n"));
            
            TestUtil.saveDataObject(xmlDataObject);
            ref(TestUtil.nodeToString(document));
            compareReferenceFiles();
        } catch (Exception ex) {
            log("\nCreating XML fails due:\n", ex);
            ex.printStackTrace();
            fail("\nCreating XML fails due:\n" + ex);
        }
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(CreateXMLTest.class);
    }
}
