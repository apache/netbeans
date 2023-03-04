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

package org.netbeans.modules.web.struts.editor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author petr
 */
public class StrutsEditorUtilitiesTest extends NbTestCase {

    private File testDir;

    private FileObject testDirFO;

    public StrutsEditorUtilitiesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances(new MimeDataProvider() {

            public Lookup getLookup(MimePath mimePath) {
                if ("text/xml".equals(mimePath.getPath())) {
                    return Lookups.fixed(new XMLKit());
                }
                return null;
            }
        });

        testDir = new File (this.getDataDir().getPath());
        assertTrue("have a dir " + testDir, testDir.isDirectory());
        testDirFO = FileUtil.toFileObject(testDir);
        assertNotNull("testDirFO is null", testDirFO);
    }

    /** Test when the cursor is inside of declaration <action ..... />
     */
    public void testGetActionPath() {
        BaseDocument doc = createBaseDocument(new File(testDir, "struts-config.xml"));
        String path = null;
        String text = null;
        try {
            text = doc.getText(0, doc.getLength() - 1);
        } catch (BadLocationException ex) {
            fail(ex.toString());
        }
        int where;
        
        where = text.indexOf("/login");
        path = StrutsEditorUtilities.getActionPath(doc, where);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where+1);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where+6);
        assertEquals("/login", path);
        
        where = text.indexOf("action type=\"com");
        path = StrutsEditorUtilities.getActionPath(doc, where);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where-1);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where+7);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where+10);
        assertEquals("/login", path);
    }

    /** Test when the cursor is inside of declaration <form-bean ..... />
     */
    public void testGetActionFormBeanName() {
        BaseDocument doc = createBaseDocument(new File(testDir, "struts-config.xml"));
        String path = null;
        String text = null;
        try {
            text = doc.getText(0, doc.getLength() - 1);
        } catch (BadLocationException ex) {
            fail(ex.toString());
        }
        int where;
        
        where = text.indexOf("name=\"FirstBean\"");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+5);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+10);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+16);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where-1);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where - 20);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where-35);
        assertEquals("FirstBean", path);
        
        where = text.indexOf("initial=\"33\"");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+5);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+10);
        assertEquals("SecondBean", path);
        
        where = text.indexOf("name=\"name\"");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+5);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+10);
        assertEquals("SecondBean", path);
        
        where = text.indexOf("/form-bean>");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+5);
        assertEquals("SecondBean", path);
        
        where = text.indexOf("name=\"SecondBean\">");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+10);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+15);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+17);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+18);
        assertEquals("SecondBean", path);
    }
    
    private BaseDocument createBaseDocument(File file){
        BaseDocument doc = new BaseDocument(false, "text/xml");
        File strutsConfig = new File(testDir, "struts-config.xml");
        StringBuffer buffer = new StringBuffer();
        try {
            FileReader reader = new FileReader (strutsConfig);
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
