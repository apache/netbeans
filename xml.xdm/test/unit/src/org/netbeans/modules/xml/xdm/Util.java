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

package org.netbeans.modules.xml.xdm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.TestModel3;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.diff.DefaultElementIdentity;
import org.netbeans.modules.xml.xdm.diff.DiffFinder;
import org.netbeans.modules.xml.xdm.diff.Difference;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 *
 * @author nn136682
 */
public class Util {
    
    public static Document getResourceAsDocument(String path) throws Exception {
        InputStream in = Util.class.getResourceAsStream(path);
        return loadDocument(in);
    }
    
    public static String getResourceAsString(String path) throws Exception {
        InputStream in = Util.class.getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        return sbuf.toString();
    }
    
    public static org.netbeans.modules.xml.xdm.nodes.Document loadXdmDocument(String resourcePath) throws Exception {
        XDMModel model = loadXDMModel(resourcePath);
        return model.getDocument();
    }
    
    public static Document loadDocument(InputStream in) throws Exception {
        Document sd = new BaseDocument(true, "text/xml"); //NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    sbuf.append(System.getProperty("line.separator"));
                }
                sbuf.append(line);
            }
        } finally {
            br.close();
        }
        sd.insertString(0,sbuf.toString(),null);
        return sd;
    }
    
    public static XDMModel loadXDMModel(String resourcePath) throws Exception {
        return loadXDMModel(getResourceAsDocument(resourcePath));
    }
    
    public static XDMModel loadXDMModel(Document doc) throws Exception {
	Lookup lookup = Lookups.singleton(doc);
	ModelSource ms = new ModelSource(lookup, true);
        XDMModel m = new XDMModel(ms);
        m.sync();
        return m;
    }
    
    public static void dumpToStream(Document doc, OutputStream out) throws Exception{
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }
    
    public static void dumpToFile(Document doc, File f) throws Exception {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }
    
    public static File dumpToTempFile(Document doc) throws Exception {
        File f = File.createTempFile("xdm-tester-", null);
        dumpToFile(doc, f);
        return f;
    }
    
    public static Document loadDocument(File f) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        return loadDocument(in);
    }

    public static void setDocumentContentTo(Document doc, File f) throws Exception {
        setDocumentContentTo(doc, new FileInputStream(f));
    }
    
    public static Document setDocumentContentTo(Document doc, InputStream in) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        doc.remove(0, doc.getLength());
        doc.insertString(0,sbuf.toString(),null);
        return doc;
    }
    
    public static Document setDocumentContentTo(Document doc, String resourcePath) throws Exception {
        return setDocumentContentTo(doc, Util.class.getResourceAsStream(resourcePath));
    }
    
    public static List<Element> getChildElementsByTag(Node parent, String tag) {
        List<Element> children = new ArrayList<Element>();
        NodeList nl = parent.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element e = (Element) nl.item(i);
                if (e.getTagName().equals(tag))
                    children.add(e);
            }
        }
        return children;
    }
        
    public static Element getChildElementByTag(Node parent, String tag) {
        List<Element> result = getChildElementsByTag(parent, tag);
        return result.size() > 0 ? result.get(0) : null;
    }

    public static ElementIdentity getDefaultElementIdentity() {
		//establish DOM element identities
		DefaultElementIdentity eID = new DefaultElementIdentity();
		eID.addIdentifier( "id" );
		eID.addIdentifier( "index" );
		eID.addIdentifier( "name" );
		eID.addIdentifier( "ref" );
        return eID;
    }
    
    public static List<Difference> diff(String path1, String path2) throws Exception {
        XDMModel m1 = loadXDMModel(path1);
        XDMModel m2 = loadXDMModel(path2);
        return diff(m1, m2);
    }
    
    public static List<Difference> diff(XDMModel m1, XDMModel m2) throws Exception {
        return new DiffFinder(getDefaultElementIdentity()).findDiff(
            m1.getDocument(), m2.getDocument());
    }
    
    public static TestModel3 loadModel(Document doc) throws Exception {
        return new TestModel3(doc);
    }
    
    public static TestModel3 loadModel(String path) throws Exception {
        return new TestModel3(getResourceAsDocument(path));
    }
    
    public static TestModel3 loadModel(File f) throws Exception {
        return new TestModel3(loadDocument(f));
    }
    
    public static TestModel3 dumpAndReloadModel(TestModel3 sm) throws Exception {
        Document doc = sm.getBaseDocument();
        File f = dumpToTempFile(doc);
        return loadModel(f);
    }

    /**
     * Converts XAM model to text form.
     * The underlaying XDM model is used for serialization actually.
     * @param model
     * @return
     */
    public static String getXdmBasedModelText(AbstractDocumentModel model) {
        org.w3c.dom.Document w3cDoc = model.getAccess().getDocumentRoot();
        assert w3cDoc instanceof org.netbeans.modules.xml.xdm.nodes.Document;
        FlushVisitor fv = new FlushVisitor();
        String docBuf = fv.flushModel((org.netbeans.modules.xml.xdm.nodes.Document)w3cDoc);
        return docBuf;
    }

    /**
     * Converts XAM model to text form.
     * Only XAM model is used here. The output doesn't contain any attributes
     * because they aren't under control of XAM model.
     * 
     * This function was initially intended to be used by JUnitTests
     * @param model
     * @return
     */
    public static String getXamBasedModelText(AbstractDocumentModel model) {
        DocumentComponent root = model.getRootComponent();
        StringBuilder output = new StringBuilder();
        buildXamBasedModelText(output, root, "");
        return output.toString();
    }

    private static void buildXamBasedModelText(StringBuilder output,
            DocumentComponent comp, String indent) {
        //
        String compTagName = comp.getPeer().getTagName();
        int childCoun = 0;
        //
        output.append(indent).append("<").append(compTagName);
        try {
            List<DocumentComponent> children = comp.getChildren();
            if (!children.isEmpty()) {
                String newIndent = indent + "    ";
                for (DocumentComponent child : children) {
                    output.append(System.getProperty("line.separator"));
                    buildXamBasedModelText(output, child, newIndent);
                    childCoun++;
                }
            }
        } finally {
            if (childCoun > 0) {
                output.append(System.getProperty("line.separator")).append(indent);
            }
            output.append("/>");
        }
    }

}
