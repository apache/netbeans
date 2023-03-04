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
