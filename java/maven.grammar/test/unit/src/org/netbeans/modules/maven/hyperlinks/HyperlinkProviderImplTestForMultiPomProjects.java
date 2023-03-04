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
package org.netbeans.modules.maven.hyperlinks;

import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.maven.grammar.POMDataObject;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;

/**
 * Additional Unit tests for the HyperlinkProvider for pom.xml files when used in a 
 * hierarchy of maven projects.
 */
public class HyperlinkProviderImplTestForMultiPomProjects extends NbTestCase {
    
    private static final int START_DEPENDENCY_GROUP_ID_VALUE = 538;
    private static final int MIDDLE_DEPENDENCY_GROUP_ID_VALUE = 547;
    private static final int END_DEPENDENCY_GROUP_ID_VALUE = 555;
    
    private static final int START_DEPENDENCY_ARTIFACT_ID_VALUE = 591;
    private static final int MIDDLE_DEPENDENCY_ARTIFACT_ID_VALUE = 599;
    private static final int END_DEPENDENCY_ARTIFACT_ID_VALUE = 603;
    
    private Document testDocument;
    private HyperlinkProviderImpl classUnderTest;
    
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public HyperlinkProviderImplTestForMultiPomProjects(String n) {
        super(n);
    }
    
    @BeforeClass
    @Override
    public void setUp() {
        classUnderTest = new HyperlinkProviderImpl();
    }
    
    @AfterClass
    @Override
    public void tearDown() throws IOException {
        classUnderTest = null;
    }
    
    private Document generateTestPomDocument() throws BadLocationException, Exception {
        if (testDocument == null) {
            POMDataObject docDataObject = getTestMavenProjectPom();
            EditorCookie cookie = docDataObject.getLookup().lookup(EditorCookie.class);
            
            testDocument = cookie.openDocument();
            testDocument.putProperty(Language.class, XMLTokenId.language());
        }
        
        return testDocument;
    }
    
    public void testHyperlinkPointMatchesMavenDependencyWhenNoVersionIsDefined() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_DEPENDENCY_GROUP_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, START_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, MIDDLE_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, END_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_DEPENDENCY_GROUP_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
        
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_DEPENDENCY_ARTIFACT_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, START_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, MIDDLE_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, END_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_DEPENDENCY_ARTIFACT_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    public void testHyperlinkSpanForMavenDependencyWhenNoVersionIsDefined() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        int[] hyperlinkSpan1 = classUnderTest.getHyperlinkSpan(doc, START_DEPENDENCY_GROUP_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan1);
        
        int[] hyperlinkSpan2 = classUnderTest.getHyperlinkSpan(doc, START_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_DEPENDENCY_GROUP_ID_VALUE, hyperlinkSpan2[0]); 
        assertEquals(END_DEPENDENCY_GROUP_ID_VALUE + 1, hyperlinkSpan2[1]); 
        
        int[] hyperlinkSpan3 = classUnderTest.getHyperlinkSpan(doc, MIDDLE_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_DEPENDENCY_GROUP_ID_VALUE, hyperlinkSpan3[0]); 
        assertEquals(END_DEPENDENCY_GROUP_ID_VALUE + 1, hyperlinkSpan3[1]);
        
        int[] hyperlinkSpan4 = classUnderTest.getHyperlinkSpan(doc, END_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_DEPENDENCY_GROUP_ID_VALUE, hyperlinkSpan4[0]); 
        assertEquals(END_DEPENDENCY_GROUP_ID_VALUE + 1, hyperlinkSpan4[1]); 

        int[] hyperlinkSpan5 = classUnderTest.getHyperlinkSpan(doc, END_DEPENDENCY_GROUP_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan5);
        
        int[] hyperlinkSpan6 = classUnderTest.getHyperlinkSpan(doc, START_DEPENDENCY_ARTIFACT_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan6);
        
        int[] hyperlinkSpan7 = classUnderTest.getHyperlinkSpan(doc, START_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_DEPENDENCY_ARTIFACT_ID_VALUE, hyperlinkSpan7[0]); 
        assertEquals(END_DEPENDENCY_ARTIFACT_ID_VALUE + 1, hyperlinkSpan7[1]); 
        
        int[] hyperlinkSpan8 = classUnderTest.getHyperlinkSpan(doc, MIDDLE_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_DEPENDENCY_ARTIFACT_ID_VALUE, hyperlinkSpan8[0]); 
        assertEquals(END_DEPENDENCY_ARTIFACT_ID_VALUE + 1, hyperlinkSpan8[1]);
        
        int[] hyperlinkSpan9 = classUnderTest.getHyperlinkSpan(doc, END_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_DEPENDENCY_ARTIFACT_ID_VALUE, hyperlinkSpan9[0]); 
        assertEquals(END_DEPENDENCY_ARTIFACT_ID_VALUE + 1, hyperlinkSpan9[1]); 

        int[] hyperlinkSpan10 = classUnderTest.getHyperlinkSpan(doc, END_DEPENDENCY_ARTIFACT_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan10);
    }
    
    public void testTooltipTextMatchesMavenDependencyWhenNoVersionIsDefined() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        assertEquals("Cannot resolve expression\nNavigates to definition.", classUnderTest.getTooltipText(doc, START_DEPENDENCY_GROUP_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertTrue(classUnderTest.getTooltipText(doc, START_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertTrue(classUnderTest.getTooltipText(doc, MIDDLE_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertTrue(classUnderTest.getTooltipText(doc, END_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertEquals("Cannot resolve expression\nNavigates to definition.", classUnderTest.getTooltipText(doc, END_DEPENDENCY_GROUP_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
        
        assertEquals("Cannot resolve expression\nNavigates to definition.", classUnderTest.getTooltipText(doc, START_DEPENDENCY_ARTIFACT_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertTrue(classUnderTest.getTooltipText(doc, START_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertTrue(classUnderTest.getTooltipText(doc, MIDDLE_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertTrue(classUnderTest.getTooltipText(doc, END_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertEquals("Cannot resolve expression\nNavigates to definition.", classUnderTest.getTooltipText(doc, END_DEPENDENCY_ARTIFACT_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    private POMDataObject getTestMavenProjectPom() throws Exception {
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(new File(getDataDir(),"test_projects"));
        FileObject pom = lfs.findResource("hyperlinks2/subModule/pom.xml");
        return (POMDataObject) DataObject.find(pom);
    }
}
