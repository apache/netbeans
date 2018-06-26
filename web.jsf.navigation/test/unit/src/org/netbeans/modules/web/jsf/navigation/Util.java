/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public class Util {
    static {
        //JEditorPane.registerEditorKitForContentType(SchemaDataLoader.MIME_TYPE, XMLKit.class.getName());
        registerXMLKit();
    }
    
    public static void registerXMLKit() {
        String[] path = new String[] { "Editors", "text", "x-jsf+xml" };
        FileObject target = FileUtil.getConfigRoot();
        try {
            for (int i=0; i<path.length; i++) {
                FileObject f = target.getFileObject(path[i]);
                if (f == null) {
                    f = target.createFolder(path[i]);
                }
                target = f;
            }
            String name = "EditorKit.instance";
            if (target.getFileObject(name) == null) {
                FileObject f = target.createData(name);
                f.setAttribute("instanceClass", "org.netbeans.modules.xml.text.syntax.XMLKit");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
//
//    public static Document getResourceAsDocument(String path) throws Exception {
//        InputStream in = Util.class.getResourceAsStream(path);
//        return loadDocument(in);
//    }
//
//    public static Document loadDocument(InputStream in) throws Exception {
////	Document sd = new PlainDocument();
//        Document sd = new org.netbeans.editor.BaseDocument(
//                org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
//        return setDocumentContentTo(sd, in);
//    }
//    
//    public static Document setDocumentContentTo(Document doc, InputStream in) throws Exception {
//        BufferedReader br = new BufferedReader(new InputStreamReader(in));
//        StringBuffer sbuf = new StringBuffer();
//        try {
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                sbuf.append(line);
//                sbuf.append(System.getProperty("line.separator"));
//            }
//        } finally {
//            br.close();
//        }
//        doc.remove(0, doc.getLength());
//        doc.insertString(0,sbuf.toString(),null);
//        return doc;
//    }
//    
//    public static Document setDocumentContentTo(Document doc, String resourcePath) throws Exception {
//        return setDocumentContentTo(doc, Util.class.getResourceAsStream(resourcePath));
//    }
//
//    public static void setDocumentContentTo(DocumentModel model, String resourcePath) throws Exception {
//        setDocumentContentTo(((AbstractDocumentModel)model).getBaseDocument(), resourcePath);
//        model.sync();
//    }
//    
//    public static int count = 0;
////    public static JSFConfigModel loadRegistryModel(String resourcePath) throws Exception {
////        URI locationURI = new URI(resourcePath);
////        TestCatalogModel.getDefault().addURI(locationURI, getResourceURI(resourcePath));
////        ModelSource ms = TestCatalogModel.getDefault().getModelSource(locationURI); 
////        return JSFConfigModelFactory.getInstance().getModel(ms);
////    }
////    
////    
////    
////    public static JSFConfigModel loadRegistryModel(File schemaFile) throws Exception {
////        URI locationURI = new URI(schemaFile.getName());
////        TestCatalogModel.getDefault().addURI(locationURI, schemaFile.toURI());
////        ModelSource ms = TestCatalogModel.getDefault().getModelSource(locationURI);
////        return JSFConfigModelFactory.getInstance().getModel(ms);
////    }
//    
//    public static void dumpToStream(Document doc, OutputStream out) throws Exception{
//        PrintWriter w = new PrintWriter(out);
//        w.print(doc.getText(0, doc.getLength()));
//        w.close();
//        out.close();
//    }
//    
//    public static void dumpToFile(DocumentModel model, File f) throws Exception {
//        dumpToFile(((AbstractDocumentModel)model).getBaseDocument(), f);
//    }
//    
//    public static void dumpToFile(Document doc, File f) throws Exception {
//        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
//        PrintWriter w = new PrintWriter(out);
//        w.print(doc.getText(0, doc.getLength()));
//        w.close();
//        out.close();
//    }
//    
////    public static JSFConfigModel dumpAndReloadModel(JSFConfigModel sm) throws Exception {
////        return dumpAndReloadModel((Document) sm.getModelSource().getLookup().lookup(Document.class));
////    }
//    
//    public static File dumpToTempFile(Document doc) throws Exception {
//        File f = File.createTempFile("faces-config-tmp", "xml");
//        System.out.println("file: " + f.getAbsolutePath());
//        dumpToFile(doc, f);
//        return f;
//    }
//    
////    public static JSFConfigModel dumpAndReloadModel(Document doc) throws Exception {
////        File f = dumpToTempFile(doc);
////        URI dumpURI = new URI("dummyDump" + count++);
////        TestCatalogModel.getDefault().addURI(dumpURI, f.toURI());
////        ModelSource ms = TestCatalogModel.getDefault().getModelSource(dumpURI);
////        return JSFConfigModelFactory.getInstance().getModel(ms);
////    }
//    
//    public static Document loadDocument(File f) throws Exception {
//        InputStream in = new BufferedInputStream(new FileInputStream(f));
//        return loadDocument(in);
//    }
//        
//    public static URI getResourceURI(String path) throws RuntimeException {
//        try {
//            return Util.class.getResource(path).toURI();
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//    
//    public static File getTempDir(String path) throws Exception {
//        File tempdir = new File(System.getProperty("java.io.tmpdir"), path);
//        tempdir.mkdirs();
//        return tempdir;
//    }
}
