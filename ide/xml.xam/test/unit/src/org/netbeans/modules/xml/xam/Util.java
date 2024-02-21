/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Util.java
 *
 * Created on October 4, 2005, 7:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xam;

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
import java.net.URI;
import java.nio.file.Files;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.dom.ReadOnlyAccess;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author nn136682
 */
public class Util {
    public static final String EMPTY_XSD = "resources/Empty.xml";
    
    public static javax.swing.text.Document getResourceAsDocument(String path) throws Exception {
        InputStream in = Util.class.getResourceAsStream(path);
        return loadDocument(in);
    }
    
    public static javax.swing.text.Document loadDocument(InputStream in) throws Exception {
	javax.swing.text.Document sd = ReadOnlyAccess.Provider.getInstance().loadSwingDocument(in);
        return sd;
    }
    
    public static TestModel2 loadModel(String path) throws Exception {
        TestModel2 model = new TestModel2(getResourceAsDocument(path));
        return model;
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
        File f = Files.createTempFile("xsm", "xsd").toFile();
        dumpToFile(doc, f);
        return f;
    }
    
    public static Document loadDocument(File f) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        return loadDocument(in);
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

    public static TestModel2 dumpAndReloadModel(DocumentModel sm) throws Exception {
        Document doc = (Document) sm.getModelSource().getLookup().lookup(Document.class);
        File f = dumpToTempFile(doc);
        return new TestModel2(loadDocument(f));
    }

    public static URI getResourceURI(String path) throws Exception {
        return Util.class.getResource(path).toURI();
    }
    
    public static File getResourceFile(String path) throws Exception {
        return new File(getResourceURI(path));
    } 

    public static ModelSource createModelSource(Document doc) {
        Lookup lookup = Lookups.fixed(new Object[] { doc } );
        return new ModelSource(lookup, true);
    }
    
    public static ModelSource createModelSource(String path) throws Exception {
        Document doc = Util.getResourceAsDocument(path);
        File file = Util.getResourceFile(path);
        Lookup lookup = Lookups.fixed(new Object[] { doc, file } );
        return new ModelSource(lookup, true);
    }
    
}
