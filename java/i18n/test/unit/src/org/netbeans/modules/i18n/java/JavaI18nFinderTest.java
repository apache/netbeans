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
package org.netbeans.modules.i18n.java;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.swing.text.StyledDocument;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.i18n.HardCodedString;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Theofanis Oikonomou <theofanis at netbeans.org>
 */
public class JavaI18nFinderTest extends NbTestCase {
    
    JavaI18nFinder instance;
    private static String source;
    
    public JavaI18nFinderTest() {
        super("JavaI18nFinder test");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fo = fs.getRoot().createData("JFrame.java");
        
        source = convertStreamToString(getClass().getResourceAsStream("resources/JFrame.txt"));
        
        writeFile(source, fo);
        DataObject sourceDataObject = DataObject.find(fo);
        
        EditorCookie editorCookie = sourceDataObject.getCookie(EditorCookie.class);        
        if (editorCookie == null) {
            throw new IllegalArgumentException("I18N: Illegal data object type"+ sourceDataObject); // NOI18N
        }        
        StyledDocument document = editorCookie.getDocument();
        while(document == null) {
            document = editorCookie.openDocument();
        }        
                        
        instance = new JavaI18nFinder(document);
    }
    
    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        is.close();
        return sb.toString();
    }
    
    private void writeFile(String content, FileObject file) throws Exception {
        OutputStream os = file.getOutputStream();
        os.write(content.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of findAllHardCodedStrings method, of class JavaI18nFinder.
     */
    @Test
    public void testBug33759() {
        HardCodedString[] result = instance.findAllHardCodedStrings();
        assertNotNull(result);
        
        for (int i = 0; i < result.length; i++) {
            HardCodedString hardCodedString = result[i];
            HardCodedString hardCodedString2 = instance.modifyHCStringText(hardCodedString);
            if (hardCodedString2 != null) {
                assertEquals(hardCodedString.getStartPosition(), hardCodedString2.getStartPosition());
                assertEquals(hardCodedString.getEndPosition(), hardCodedString2.getEndPosition());
                
                String expectedText = source.substring(hardCodedString2.getStartPosition().getOffset(), hardCodedString2.getEndPosition().getOffset());
                String actualText = "\"".concat(hardCodedString2.getText());
                actualText = (expectedText.endsWith("\"")) ? actualText.concat("\"") : actualText;                
                assertEquals(actualText, expectedText);
            }
        }
    }
}
