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
