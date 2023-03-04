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
 * Unit tests for the HyperlinkProvider for pom.xml within a single project.
 */
public class HyperlinkProviderImplTestForSinglePomProjects extends NbTestCase {
    
    /* Maven Repository URL */
    private static final int START_URL_VALUE = 577;
    private static final int MIDDLE_URL_VALUE = 595;
    private static final int END_URL_VALUE = 611;
    
    /* dependency - org.apache.commons:commons-lang3:3.7 */
    private static final int START_DEPENDENCY_GROUP_ID_VALUE = 849;
    private static final int MIDDLE_DEPENDENCY_GROUP_ID_VALUE = 857;
    private static final int END_DEPENDENCY_GROUP_ID_VALUE = 866;
    
    private static final int START_DEPENDENCY_ARTIFACT_ID_VALUE = 902;
    private static final int MIDDLE_DEPENDENCY_ARTIFACT_ID_VALUE = 909;
    private static final int END_DEPENDENCY_ARTIFACT_ID_VALUE = 914;
    
    private static final int START_DEPENDENCY_VERSION_ID_VALUE = 950;
    private static final int MIDDLE_DEPENDENCY_VERSION_ID_VALUE = 951;
    private static final int END_DEPENDENCY_VERSION_ID_VALUE = 952;
    
    /* dependency - org.mockito:mockito-core:1.10.9 - uses property for version: mockito.version*/
    private static final int START_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE = 1028;
    private static final int MIDDLE_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE = 1034;
    private static final int END_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE = 1038;
    
    private static final int START_VERSION_PROPERTY_DEPENDENCY_ARTIFACT_ID_VALUE = 1074;
    private static final int MIDDLE_VERSION_PROPERTY_DEPENDENCY_ARTIFACT_ID_VALUE = 1079;
    private static final int END_VERSION_PROPERTY_DEPENDENCY_ARTIFACT_ID_VALUE = 1085;
    
    private static final int START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE = 1123;
    private static final int MIDDLE_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE = 1131;
    private static final int END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE = 1138;
    
    /* plugin - maven-compiler-plugin:org.apache.maven.plugins:3.1 */
    private static final int START_PLUGIN_ARTIFACT_ID_VALUE = 1271;
    private static final int MIDDLE_PLUGIN_ARTIFACT_ID_VALUE = 1281;
    private static final int END_PLUGIN_ARTIFACT_ID_VALUE = 1292;
    
    private static final int START_PLUGIN_GROUP_ID_VALUE = 1331;
    private static final int MIDDLE_PLUGIN_GROUP_ID_VALUE = 1342;
    private static final int END_PLUGIN_GROUP_ID_VALUE = 1366;
    
    private Document testDocument;
    private HyperlinkProviderImpl classUnderTest;
    
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public HyperlinkProviderImplTestForSinglePomProjects(String n) {
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
    
    public void testHyperlinkPointMatchesUsedMavenProperty() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, MIDDLE_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    public void testHyperlinkPointMatchesUrl() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_URL_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, START_URL_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, MIDDLE_URL_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, END_URL_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_URL_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    public void testHyperlinkPointMatchesMavenDependency() throws BadLocationException, Exception {
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
        
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_DEPENDENCY_VERSION_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, START_DEPENDENCY_VERSION_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, MIDDLE_DEPENDENCY_VERSION_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, END_DEPENDENCY_VERSION_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_DEPENDENCY_VERSION_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    public void testHyperlinkPointDoesntMatchMavenPlugin() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_PLUGIN_ARTIFACT_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_PLUGIN_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, MIDDLE_PLUGIN_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_PLUGIN_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_PLUGIN_ARTIFACT_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
        
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_PLUGIN_GROUP_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_PLUGIN_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, MIDDLE_PLUGIN_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_PLUGIN_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_PLUGIN_GROUP_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    public void testHyperlinkPointMatchesMavenDependencyWhenDependencyUsesVersionProperty() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, START_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, MIDDLE_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, END_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
        
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, START_VERSION_PROPERTY_DEPENDENCY_ARTIFACT_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, START_VERSION_PROPERTY_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, MIDDLE_VERSION_PROPERTY_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(true, classUnderTest.isHyperlinkPoint(doc, END_VERSION_PROPERTY_DEPENDENCY_ARTIFACT_ID_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals(false, classUnderTest.isHyperlinkPoint(doc, END_VERSION_PROPERTY_DEPENDENCY_ARTIFACT_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    public void testHyperlinkSpanForMavenProperty() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        int[] hyperlinkSpan1 = classUnderTest.getHyperlinkSpan(doc, START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE - 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan1);
        
        int[] hyperlinkSpan2 = classUnderTest.getHyperlinkSpan(doc, START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE - 2, hyperlinkSpan2[0]); 
        assertEquals(END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE + 1, hyperlinkSpan2[1]); 
        
        int[] hyperlinkSpan3 = classUnderTest.getHyperlinkSpan(doc, MIDDLE_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE - 2, hyperlinkSpan3[0]); 
        assertEquals(END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE + 1, hyperlinkSpan3[1]); 
        
        int[] hyperlinkSpan4 = classUnderTest.getHyperlinkSpan(doc, END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE - 2, hyperlinkSpan4[0]); 
        assertEquals(END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE + 1, hyperlinkSpan4[1]); 

        int[] hyperlinkSpan5 = classUnderTest.getHyperlinkSpan(doc, END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE + 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan5);
    }
    
    public void testHyperlinkSpanForUrl() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        int[] hyperlinkSpan1 = classUnderTest.getHyperlinkSpan(doc, START_URL_VALUE - 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan1);
        
        int[] hyperlinkSpan2 = classUnderTest.getHyperlinkSpan(doc, START_URL_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_URL_VALUE, hyperlinkSpan2[0]); 
        assertEquals(END_URL_VALUE + 1, hyperlinkSpan2[1]); 
        
        int[] hyperlinkSpan3 = classUnderTest.getHyperlinkSpan(doc, MIDDLE_URL_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_URL_VALUE, hyperlinkSpan3[0]); 
        assertEquals(END_URL_VALUE + 1, hyperlinkSpan3[1]);
        
        int[] hyperlinkSpan4 = classUnderTest.getHyperlinkSpan(doc, END_URL_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_URL_VALUE, hyperlinkSpan4[0]); 
        assertEquals(END_URL_VALUE + 1, hyperlinkSpan4[1]); 

        int[] hyperlinkSpan5 = classUnderTest.getHyperlinkSpan(doc, END_URL_VALUE + 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan5);
    }
    
    public void testHyperlinkSpanForMavenDependency() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        int[] hyperlinkSpan1 = classUnderTest.getHyperlinkSpan(doc, START_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan1);
        
        int[] hyperlinkSpan2 = classUnderTest.getHyperlinkSpan(doc, START_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, hyperlinkSpan2[0]); 
        assertEquals(END_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE + 1, hyperlinkSpan2[1]); 
        
        int[] hyperlinkSpan3 = classUnderTest.getHyperlinkSpan(doc, MIDDLE_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, hyperlinkSpan3[0]); 
        assertEquals(END_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE + 1, hyperlinkSpan3[1]);
        
        int[] hyperlinkSpan4 = classUnderTest.getHyperlinkSpan(doc, END_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, HyperlinkType.GO_TO_DECLARATION);
        assertEquals(START_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE, hyperlinkSpan4[0]); 
        assertEquals(END_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE + 1, hyperlinkSpan4[1]); 

        int[] hyperlinkSpan5 = classUnderTest.getHyperlinkSpan(doc, END_VERSION_PROPERTY_DEPENDENCY_GROUP_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION);
        assertNull(hyperlinkSpan5);
    }
    
    public void testTooltipTextMatchesUsedMavenProperty() throws BadLocationException, Exception {
        Document doc = generateTestPomDocument();
        assertEquals("Cannot resolve expression\nNavigates to definition.", classUnderTest.getTooltipText(doc, START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertEquals("mockito.version resolves to \'1.10.9\'\nNavigate to definition.", classUnderTest.getTooltipText(doc, START_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals("mockito.version resolves to \'1.10.9\'\nNavigate to definition.", classUnderTest.getTooltipText(doc, MIDDLE_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals("mockito.version resolves to \'1.10.9\'\nNavigate to definition.", classUnderTest.getTooltipText(doc, END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE, HyperlinkType.GO_TO_DECLARATION));
        assertEquals("Cannot resolve expression\nNavigates to definition.", classUnderTest.getTooltipText(doc, END_VERSION_PROPERTY_DEPENDENCY_VERSION_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    public void testTooltipTextMatchesMavenDependency() throws BadLocationException, Exception {
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
        
        assertEquals("Cannot resolve expression\nNavigates to definition.", classUnderTest.getTooltipText(doc, START_DEPENDENCY_VERSION_ID_VALUE - 1, HyperlinkType.GO_TO_DECLARATION));
        assertTrue(classUnderTest.getTooltipText(doc, START_DEPENDENCY_VERSION_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertTrue(classUnderTest.getTooltipText(doc, MIDDLE_DEPENDENCY_VERSION_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertTrue(classUnderTest.getTooltipText(doc, END_DEPENDENCY_VERSION_ID_VALUE, HyperlinkType.GO_TO_DECLARATION).endsWith("org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.pom"));
        assertEquals("Cannot resolve expression\nNavigates to definition.", classUnderTest.getTooltipText(doc, END_DEPENDENCY_VERSION_ID_VALUE + 1, HyperlinkType.GO_TO_DECLARATION));
    }
    
    private POMDataObject getTestMavenProjectPom() throws Exception {
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(new File(getDataDir(),"test_projects"));
        FileObject pom = lfs.findResource("hyperlinks/pom.xml");
        return (POMDataObject) DataObject.find(pom);
    }
}
