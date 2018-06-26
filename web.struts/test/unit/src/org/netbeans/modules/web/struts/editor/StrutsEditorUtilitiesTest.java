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
