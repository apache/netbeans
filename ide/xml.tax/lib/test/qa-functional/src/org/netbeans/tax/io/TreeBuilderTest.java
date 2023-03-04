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
