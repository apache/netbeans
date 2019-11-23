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
            Class<?> clazz = Class.forName("org.netbeans.modules.java.editor.semantic.LexerBasedHighlightLayer");
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
