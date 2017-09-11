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
package org.netbeans.tax.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import junit.textui.TestRunner;
import org.netbeans.modules.xml.tax.parser.ParserLoader;
import org.netbeans.tax.TreeDTD;
import org.netbeans.tax.TreeDocument;
import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeDocumentType;
import org.netbeans.tax.TreeNode;
import org.netbeans.tax.TreeObjectList;
import org.netbeans.tax.TreeParameterEntityReference;
import org.netbeans.tax.TreeParentNode;
import org.netbeans.tests.xml.XTest;
import org.openide.xml.EntityCatalog;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 *
 * @author  ms113234
 *
 */
public class TreeBuilderTest extends XTest {
    
    /** Creates new CoreSettingsTest */
    public TreeBuilderTest(String testName) {
        super(testName);
    }
    
    public void testAttlistWithRefs() throws Exception {
        parseDocument("data/attlist-with-refs.dtd", TreeDTD.class);
    }
    
    public void testAttlistWithRefsInstance() throws Exception {
        parseDocument("data/attlist-with-refs-instance.xml", TreeDocument.class);
    }
    
    public void testLevele1() throws Exception {
        parseDocument("data/dir/level1.dtd", TreeDTD.class);
    }
    
    public void testDistributed() throws Exception {
        parseDocument("data/distributed.xml", TreeDocument.class);
    }
    
    public void xtestPeRefInIcludeSection() throws Exception { // issue #18096
        parseDocument("data/pe-ref-in-include-section.dtd", TreeDTD.class);
    }
    
    public void testIncludeIgnoreSection() throws Exception {
        parseDocument("data/include-ignore-section.dtd", TreeDTD.class);
    }
    
//    public void testTwoColonsInElementName() throws Exception { //issue #22197
//        parseDocument("data/two-colons-in-element-name.xml", TreeDocument.class);
//    }
    
    /**
     * Parses XML or DTD document ant writes it into golden file.
     * @param name  document's name
     * @param clazz document's class
     */
    private void parseDocument(String name, Class clazz) {
        ByteArrayOutputStream errOut = new ByteArrayOutputStream();
        final PrintStream errStream = new PrintStream(errOut);
        
        try {
            // ClassLoader myl = ParserLoader.getInstance();
            ClassLoader myl = getClass().getClassLoader();
            InputSource in = new InputSource(new InputStreamReader(this.getClass().getResourceAsStream(name)));
            in.setSystemId(getClass().getResource(name).toExternalForm());
            
            TreeStreamBuilderErrorHandler errHandler = new TreeStreamBuilderErrorHandler() {
                public void message(int type, SAXParseException ex) {
                    ex.printStackTrace(new PrintStream(errStream, true));
                }
            };
            
            Class klass = myl.loadClass("org.netbeans.tax.io.XNIBuilder");
            Constructor cons = klass.getConstructor(new Class[] {Class.class, InputSource.class, EntityResolver.class, TreeStreamBuilderErrorHandler.class});
            TreeBuilder builder = (TreeBuilder) cons.newInstance(new Object[] {clazz, in, EntityCatalog.getDefault(), errHandler});
            TreeDocumentRoot document = (TreeDocumentRoot) builder.buildDocument();
            
            assertEquals("", errOut.toString());
            ref(docToString((TreeParentNode) document));
        } catch (Exception ex) {
            ex.printStackTrace(errStream);
            fail("\nParse document " + name +" failed.\n" + errOut.toString());
        }
        compareReferenceFiles();
    }
    
    /**
     * Converts document to string.
     */
    private String docToString(TreeParentNode parent) throws Exception {
        String str ="";
        
        if (TreeDocument.class.isInstance(parent)) {
            // insert external DTD
            Iterator it = parent.getChildNodes(TreeDocumentType.class).iterator();
            while (it.hasNext()) {
                TreeDocumentType docType = (TreeDocumentType) it.next();
                
                str += listToString(docType.getExternalDTD());
                str += "\n<!---  End of External DTD  --->\n\n";
            }
        }
        TestUtil tu = TestUtil.THIS;
        str += TestUtil.THIS.nodeToString(parent);
        return str;
    }
    
    private String listToString(TreeObjectList list) throws Exception {
        Iterator it = list.iterator();
        String str = "";
        
        while (it.hasNext()) {
            TreeNode node = (TreeNode) it.next();
            str += TestUtil.THIS.nodeToString(node) + "\n";
            
            if (TreeParameterEntityReference.class.isInstance(node)) {
                TreeParameterEntityReference ref = (TreeParameterEntityReference) node;
                
                str += "\n<!---  Parameter Entity Reference: " + ref.getName() + "  --->\n\n";
                str += listToString(ref.getChildNodes());
            }
        }
        return str;
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(TreeBuilderTest.class);
    }
}
