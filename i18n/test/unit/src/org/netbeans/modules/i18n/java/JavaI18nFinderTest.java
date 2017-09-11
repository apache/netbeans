/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.i18n.java;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        os.write(content.getBytes("UTF-8"));
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
