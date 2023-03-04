package org.netbeans.api.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.lsp.StructureProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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

/**
 *
 * @author Petr Pisl
 */
public class StructureElementTest extends NbTestCase {
    
    private FileObject srcFile;

    public StructureElementTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        srcFile = FileUtil.createData(workDir, "mytest.test");
        MockMimeLookup.setInstances(MimePath.get("text/haha"), new HahaStructureProvider());
    }
    
    
    public void testStructureElements() {
        Document doc = createDocument("text/haha", "");
        
        StructureProvider structureProvider = MimeLookup.getLookup(DocumentUtilities.getMimeType(doc)).lookup(StructureProvider.class);
        
        List<StructureElement> structure = structureProvider.getStructure(doc);
        
        assertNotNull(structure);
        assertEquals(1, structure.size());
        
        // root
        StructureElement root = structure.get(0);
        assertEquals("Root1", root.getName());
        assertEquals(StructureElement.Kind.Class, root.getKind());
        assertEquals(10, root.getSelectionStartOffset());
        assertEquals(50, root.getSelectionEndOffset());
        assertEquals(5, root.getExpandedStartOffset());
        assertEquals(51, root.getExpandedEndOffset());
        assertEquals("detail", root.getDetail());
        assertNull(root.getTags());
        
        // children
        structure = root.getChildren();
        assertNotNull(structure);
        assertEquals(2, structure.size());
        
        StructureElement child1 = structure.get(0);
        assertEquals("field1", child1.getName());
        assertEquals(StructureElement.Kind.Field, child1.getKind());
        assertEquals(20, child1.getSelectionStartOffset());
        assertEquals(25, child1.getSelectionEndOffset());
        assertEquals(15, child1.getExpandedStartOffset());
        assertEquals(25, child1.getExpandedEndOffset());
        assertNull(child1.getDetail());
        assertNull(child1.getChildren());
        assertNotNull(child1.getTags());
        assertTrue(child1.getTags().contains(StructureElement.Tag.Deprecated));
        
        StructureElement child2 = structure.get(1);
        assertEquals("method1", child2.getName());
        assertEquals(StructureElement.Kind.Method, child2.getKind());
        assertEquals(30, child2.getSelectionStartOffset());
        assertEquals(45, child2.getSelectionEndOffset());
        assertEquals(26, child2.getExpandedStartOffset());
        assertEquals(46, child2.getExpandedEndOffset());
        assertEquals(": int", child2.getDetail());
        assertNull(child2.getChildren());
        assertNull(child2.getTags());
    }
    
    private Document createDocument(String mimeType, String contents) {
        Document doc = new DefaultStyledDocument();
        doc.putProperty("mimeType", mimeType);
        try {
            doc.insertString(0, contents, null);
            return doc;
        } catch (BadLocationException ble) {
            throw new IllegalStateException(ble);
        }
    }
    
    private class HahaStructureProvider implements StructureProvider {
        List<StructureElement> elements;

        public HahaStructureProvider() {
            elements = new ArrayList<>();
            addStructure();
        }
        
        @Override
        public List<StructureElement> getStructure(Document doc) {
            return elements;
        }
        
        private void addStructure() {
           Builder root = StructureProvider.newBuilder("Root1", StructureElement.Kind.Class);
           root.detail("detail");
           root.selectionStartOffset(10).selectionEndOffset(50);
           root.expandedStartOffset(5).expandedEndOffset(51);
           
           Builder child1 = StructureProvider.newBuilder("field1", StructureElement.Kind.Field);
           child1.selectionStartOffset(20).selectionEndOffset(25);
           child1.expandedStartOffset(15).expandedEndOffset(25);
           child1.addTag(StructureElement.Tag.Deprecated);
           root.children(child1.build());
           
           Builder child2 = StructureProvider.newBuilder("method1", StructureElement.Kind.Method);
           child2.selectionStartOffset(30).selectionEndOffset(45);
           child2.expandedStartOffset(26).expandedEndOffset(46);
           child2.detail(": int");
           root.children(child2.build());
           elements.add(root.build());
        }
        
    } 
    
    
}
