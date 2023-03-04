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
package org.openide.text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnmappableCharacterException;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sdedic
 */
public class DataEditorSupportEncodingTest  extends NbTestCase {
    private FileObject  testFileObject;
    private String  encodingName;
    
    public DataEditorSupportEncodingTest(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        clearWorkDir();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        
        testFileObject = fs.getRoot().createData("Plain", "txt");
        
        MockLookup.setInstances(new FileEncodingQueryImplementation() {

            @Override
            public Charset getEncoding(FileObject file) {
               if (file == testFileObject) {
                   return Charset.forName(encodingName);
               } 
               return null;
            }
            
        });
        
    }
    
    private static final String CZECH_STRING_UTF = "\u017Elu\u0165ou\u010Dk\u00FD k\u016F\0148";
    
    public void testNationalCharactersSaved() throws Exception {
        DataObject d = DataObject.find(testFileObject);
        
        encodingName = "ISO-8859-2"; // NOI18N
        
        EditorCookie o = d.getLookup().lookup(EditorCookie.class);
        StyledDocument doc = o.openDocument();
        doc.insertString(0, CZECH_STRING_UTF, null);
        
        o.saveDocument();

        // try to open the file
        InputStream istm = testFileObject.getInputStream();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(istm, "ISO-8859-2")); // NOI18N
            String line = r.readLine();

            assertEquals("Text differs", CZECH_STRING_UTF, line); // NOI18N
        } finally {
            istm.close();
        }
    }
    
    public void testIncompatibleCharacter() throws Exception {
        DataObject d = DataObject.find(testFileObject);
        
        encodingName = "ISO-8859-1"; // NOI18N
        
        EditorCookie o = d.getLookup().lookup(EditorCookie.class);
        StyledDocument doc = o.openDocument();
        doc.insertString(0, CZECH_STRING_UTF, null);
        
        try {
            o.saveDocument();
            
            // try to open the file
            InputStream istm = testFileObject.getInputStream();
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(istm, "ISO-8859-2")); // NOI18N
                String line = r.readLine();
                
                int questionMarkPos = line.indexOf('?'); // NOI18N
                
                assertTrue("Should save question marks", questionMarkPos != -1); // NOI18N
            } finally {
                istm.close();
            }
            //fail("Exception expected");
        } catch (UnmappableCharacterException ex) {
            // expected exceptiom
        }
    }
}
