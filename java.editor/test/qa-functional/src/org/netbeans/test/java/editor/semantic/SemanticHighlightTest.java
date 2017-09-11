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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.test.java.editor.semantic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.test.java.editor.lib.JavaEditorTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jiri Prox
 */
public class SemanticHighlightTest extends JavaEditorTestCase {

    private String curPackage;

    private String testClass;

    private EditorOperator oper;

    private final String projectName = "java_editor_test";

    public SemanticHighlightTest(String testMethodName) {
        super(testMethodName);
        curPackage = getClass().getPackage().getName();
    }

    public void testSemantic() throws IOException {
        checkCurrentColorSettings();
        
    }

    private void checkCurrentColorSettings() throws IOException {
	String path  = "/projects/"+projectName+"/src/"+curPackage.replace('.','/')+"/"+testClass+".java";	
	File testFile = new File(getDataDir(),path);
	FileObject fo = FileUtil.toFileObject(testFile);
	DataObject d = DataObject.find(fo);
	final EditorCookie ec = (EditorCookie)d.getCookie(EditorCookie.class);
	ec.open();
	StyledDocument doc = ec.openDocument();
        //wait for semantic higlight initialization
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
        dumpColorsForDocument(doc);

    }

    public void dumpColorsForDocument(StyledDocument doc) {
        try {
            Class clazz = Class.forName("org.netbeans.modules.java.editor.semantic.LexerBasedHighlightLayer");
            assertNotNull("Color layer class was not found");
            Method method = clazz.getMethod("getLayer", Class.class, Document.class);
            Object invoke = method.invoke(null, Class.forName("org.netbeans.modules.java.editor.semantic.SemanticHighlighter"), doc);
            AbstractHighlightsContainer container = (AbstractHighlightsContainer) invoke;
            HighlightsSequence hs = container.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
            while(hs.moveNext()) {
                AttributeSet as  = hs.getAttributes();
                Enumeration en = as.getAttributeNames();//produces elements in random order!
                getRef().println(hs.getStartOffset()+ " "+hs.getEndOffset());                
                ArrayList<String> tmpEnumContent = new ArrayList<String>();
                while(en.hasMoreElements()) {
                    Object s = en.nextElement();
                    String attrValue = as.getAttribute(s).toString();
                    if(s.toString().equals("tooltip")) {  // trim @hashcode if attribute value is instance of class
                        int pos = attrValue.lastIndexOf('@');
                        attrValue = attrValue.substring(0,pos);
                    }
                    tmpEnumContent.add("    "+s+" "+attrValue);
                }
                Collections.sort(tmpEnumContent); //sort the output
                Iterator<String> it = tmpEnumContent.iterator();
                while(it.hasNext()) {
                    String s = it.next();
                    getRef().println(s);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openProject(projectName);
        //sets the testClass name to current test name (ie. for proper goldenfile)
        testClass = getName();        
        openSourceFile(curPackage, testClass);
        oper = new EditorOperator(testClass);
    }

    @Override
    protected void tearDown() throws Exception {
        compareGoldenFile();
        super.tearDown();
    }

    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SemanticHighlightTest.class)
                .addTest("testSemantic")
                .enableModules(".*")
                .clusters(".*"));
    }
}
