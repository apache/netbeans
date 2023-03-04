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

package org.netbeans.modules.web.jsf.editor;

import org.netbeans.modules.web.jsf.api.editor.JSFEditorUtilities;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author petr
 */
public class JSFEditorUtilitiesTest extends NbTestCase {
    
    File testDir;
    FileObject testDirFO;
    
    public JSFEditorUtilitiesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testDir = new File (this.getDataDir().getPath());
        assertTrue("have a dir " + testDir, testDir.isDirectory());
        testDirFO = FileUtil.toFileObject(testDir);
        assertNotNull("testDirFO is null", testDirFO);
    }
    
    /**
     * Test of getNavigationRuleDefinition method, of class org.netbeans.modules.web.jsf.editor.JSFEditorUtilities.
     */
    public void testGetNavigationRuleDefinition() {
        //System.out.println("getNavigationRuleDefinition");
        BaseDocument doc = createBaseDocument(new File(testDir, "faces-config1.xml"));
        String ruleName = "/searchSciname.jsp";
        int[] expResult = new int[]{1541,1964};
        int[] result = JSFEditorUtilities.getNavigationRuleDefinition(doc, ruleName);
        //System.out.println("result: " + result[0] + " | " + result [1]);
        assertEquals(expResult[0], result[0]);
        assertEquals(expResult[1], result[1]);
    }

    private BaseDocument createBaseDocument(File file){
        BaseDocument doc = new BaseDocument(false, "text/xml");
        StringBuffer buffer = new StringBuffer();
        try {
            FileReader reader = new FileReader (file);
            char[] buf = new char [100];
            int count = -1;
            while ((count = reader.read(buf)) != -1){
                buffer.append(buf, 0, count);
            } 
            reader.close();
            doc.insertString(0, buffer.toString(), null); 
            return doc;
        } catch (IOException ex) {
            fail("Exception occured during createBaseDocument: " + ex.toString());
        }
        catch (BadLocationException ex) {
            fail("Exception occured during createBaseDocument: " + ex.toString());
        } 
        return null;
    }
    
}
