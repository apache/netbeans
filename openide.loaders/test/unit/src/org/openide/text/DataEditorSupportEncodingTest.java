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
