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
package org.netbeans.modules.editor.fold;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldingSupport;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.fold.ContentReader;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Exceptions;

/**
 *
 * @author
 * sdedic
 */
public class FoldContentReaderTest extends NbTestCase implements FoldManagerFactory {
    private TestFM fm = new TestFM();
    private FoldHierarchyTestEnv env;
    private FoldHierarchy h;
    private Document d;
    
    @Override
    public FoldManager createFoldManager() {
        return fm;
    }

    @Override
    protected void setUp() throws Exception {
        InputStream istm = getClass().getResourceAsStream("FoldContentReader.txt");
        env = new FoldHierarchyTestEnv(this);
        env.getPane().getEditorKit().read(istm, env.getDocument(), 0);
        d = env.getDocument();
        istm.close();
        h = env.getHierarchy();
    }
    
    public FoldContentReaderTest(String name) {
        super(name);
    }
    
    /**
     * Checks that 'stop' pattern will stop searching even though there's some text following.
     * @throws Exception 
     */
    public void testEmptyJavadocDescription() throws Exception {
        Fold f = fm.foldMap.get("A");
        assertNotNull(f);
        
        ContentReader r = javaReader();
        CharSequence content = r.read(d, f, f.getType().getTemplate());
        assertNull(content);
    }
    
    /**
     * Gets the 1st line of text, not more.
     * @throws Exception 
     */
    public void testJavadocFullLine() throws Exception {
        Fold f = fm.foldMap.get("B");
        assertNotNull(f);
        
        ContentReader r = javaReader();
        CharSequence content = r.read(d, f, f.getType().getTemplate());
        assertEquals(" First line alone", content.toString());
    }
    
    /**
     * Checks that 'terminator' ends reading in the middle of the line
     */
    public void testJavadocPartLine() throws Exception {
        Fold f = fm.foldMap.get("C");
        assertNotNull(f);
        
        ContentReader r = FoldingSupport.contentReader("*", "\\.", "@", "^");
        CharSequence content = r.read(d, f, f.getType().getTemplate());
        assertEquals("^First line end", content.toString());
    }
    
    /**
     * Reading of a simple tag
     */
    public void testTagAlone() throws Exception {
        Fold f = fm.foldMap.get("D");
        assertNotNull(f);
        
        ContentReader r = FoldingSupport.contentReader("<", "[\\s/>]", null, "");
        CharSequence content = r.read(d, f, f.getType().getTemplate());
        assertEquals("tag", content.toString());
    }
    
    /**
     * Checks that null prefix will prepend a space
     */
    public void testTagAloneNullPrefix() throws Exception {
        Fold f = fm.foldMap.get("D");
        assertNotNull(f);
        
        ContentReader r = FoldingSupport.contentReader("<", "[\\s/>]", null, null);
        CharSequence content = r.read(d, f, f.getType().getTemplate());
        assertEquals(" tag", content.toString());
    }
    
    /**
     * Checks that empty lines are skipped even though they contain whitespaces
     */
    public void testJavadocSkipEmptyLines() throws Exception {
        Fold f = fm.foldMap.get("H");
        assertNotNull(f);
        
        ContentReader r = FoldingSupport.contentReader("*", "\\.", "@", "^");
        CharSequence content = r.read(d, f, f.getType().getTemplate());
        assertEquals("^Empty line left out", content.toString());
    }
    
    private static ContentReader javaReader() {
        return FoldingSupport.contentReader("*", "\\.", "@", " ");
    }
    
    /**
     * Reading of a tag with attributes
     */
    public void testTagWithAttributes() throws Exception {
        Fold f = fm.foldMap.get("E");
        assertNotNull(f);
        
        ContentReader r = FoldingSupport.contentReader("<", "[\\s/>]", null, "");
        CharSequence content = r.read(d, f, f.getType().getTemplate());
        assertEquals("tag-with-attributes", content.toString());
    }
    
    /**
     * Reading of a tag that contains a newline (only tag name should be read)
     */
    public void testTagWithNewline() throws Exception {
        Fold f = fm.foldMap.get("G");
        assertNotNull(f);
        
        ContentReader r = FoldingSupport.contentReader("<", "[\\s/>]", null, "%");
        CharSequence content = r.read(d, f, f.getType().getTemplate());
        assertEquals("%tag-with-newline", content.toString());
    }
    
    public void testJavaOneLinerWithoutDot() throws Exception {
        Fold f = fm.foldMap.get("I");
        assertNotNull(f);
        
        CharSequence content = javaReader().read(d, f, f.getType().getTemplate());
        assertEquals(" One liner without dot", content.toString());
    }
    
    public void testJavaOneLinerWithDot() throws Exception {
        Fold f = fm.foldMap.get("J");
        assertNotNull(f);
        
        CharSequence content = javaReader().read(d, f, f.getType().getTemplate());
        assertEquals(" One liner with dot", content.toString());
    }

    private static final FoldType BLOCK_COMMENT = FoldType.COMMENT.derive("comment", "Comment", 
            new FoldTemplate(3, 2, "/**...*/"));
    
    private static class TestFM implements FoldManager {
        private FoldOperation oper;
        
        Map<String, Fold> foldMap = new HashMap<String, Fold>();
        
        @Override
        public void init(FoldOperation operation) {
            this.oper = operation;
        }

        @Override
        public void initFolds(FoldHierarchyTransaction transaction) {
            Document d = oper.getHierarchy().getComponent().getDocument();
            String s;
            try {
                 s = d.getText(0, d.getLength());
                // first find /** comments.
                for (int idx = s.indexOf("/**"); idx != -1; idx = s.indexOf("/**", idx)) {
                    int idx2 = s.indexOf("*/", idx + 3) + 2;
                    Fold f = oper.addToHierarchy(BLOCK_COMMENT, idx, idx2, null, null, null, null, transaction);
                    String id = s.substring(idx -1, idx);
                    foldMap.put(id, f);

                    idx = idx2;
                }
                
                // now search for <> tags
                for (int idx = s.indexOf('<'); idx != -1; idx = s.indexOf('<', idx)) {
                    int idx2 = s.indexOf('>', idx + 1);
                    Fold f = oper.addToHierarchy(FoldType.TAG, idx, idx2 + 1, null, null, null, null, transaction);
                    String id = s.substring(idx -1, idx);
                    foldMap.put(id, f);

                    idx = idx2;
                }
            } catch (BadLocationException ex) {
                fail("Unexpected exception");
                return;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        }

        @Override
        public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        }

        @Override
        public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        }

        @Override
        public void removeEmptyNotify(Fold epmtyFold) {
        }

        @Override
        public void removeDamagedNotify(Fold damagedFold) {
        }

        @Override
        public void expandNotify(Fold expandedFold) {
        }

        @Override
        public void release() {
        }
        
    } 
}
