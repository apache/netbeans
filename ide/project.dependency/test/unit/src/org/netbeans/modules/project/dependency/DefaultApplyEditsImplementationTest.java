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
package org.netbeans.modules.project.dependency;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.lsp.ResourceModificationException;
import org.netbeans.api.lsp.ResourceOperation;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Union2;

/**
 *
 * @author sdedic
 */
public class DefaultApplyEditsImplementationTest extends NbTestCase {
    private FileObject workDir;
    
    public DefaultApplyEditsImplementationTest(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        workDir = FileUtil.toFileObject(getWorkDir());
    }
    
    /**
     * Checks that an empty list of edits or an empty WorkspaceEdit succeeds and makes
     * no change.
     * 
     * @throws Exception 
     */
    public void testNoOperation() throws Exception {
        WorkspaceEdit.applyEdits(Collections.emptyList(), false);
    }
    
    FileObject document;
    
    
    String makeSimpleEdit(boolean save) throws Exception {
        document = FileUtil.copyFile(FileUtil.toFileObject(getDataDir()).getFileObject("ProjectArtifactImplementation.java"), workDir, "Document1");
        String text = document.asText();
        URI fileUri = document.toURI();
        
        int index = text.indexOf("*/") + 2;
        
        TextDocumentEdit edit = new TextDocumentEdit(fileUri.toString(), Arrays.asList(
            new TextEdit(0, index, null)
        ));
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Collections.singletonList(Union2.createFirst((edit))));
        
        WorkspaceEdit.applyEdits(Collections.singletonList(wkspEdit), save).get();
        
        EditorCookie cake = document.getLookup().lookup(EditorCookie.class);
        assertNotNull(cake);
        String modified = cake.getDocument().getText(0, cake.getDocument().getLength());
        
        assertEquals("Document not edited properly", text.substring(index), modified);
        return modified;
    }
    
    public void testOneSimpleTextEdit() throws Exception {
        makeSimpleEdit(false);
        assertNotNull("Document has to be modified", document.getLookup().lookup(Savable.class));
    }
    
    public void testOneSimpleTextEditWithSave() throws Exception {
        String modified = makeSimpleEdit(true);
        assertNull("Document has to be saved", document.getLookup().lookup(Savable.class));
        assertEquals("Modified content was saved", modified, document.asText());
    }
    

    public void testMultipleTextEdits() throws Exception {
        document = FileUtil.copyFile(FileUtil.toFileObject(getDataDir()).getFileObject("ProjectArtifactImplementation.java"), workDir, "Document1");
        String text = document.asText();
        URI fileUri = document.toURI();
        
        // replace all texts "Result" with "ResultX", each replacement will increase the find offset by 1 (longer by 1 char)
        List<TextEdit> edits = new ArrayList<>();
        int offset = 0;
        for (int i = text.indexOf("Result"); i != -1; i = text.indexOf("Result", i + 1)) {
            edits.add(new TextEdit(i + offset, i + offset + "Result".length(), "ResultX"));
            offset++;
        }
        TextDocumentEdit edit = new TextDocumentEdit(fileUri.toString(), edits);
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Collections.singletonList(Union2.createFirst((edit))));
        
        
        List<String> res = WorkspaceEdit.applyEdits(Collections.singletonList(wkspEdit), false).get();
        assertEquals(1, res.size());
        assertEquals(fileUri.toString(), res.get(0));
        
        EditorCookie cake = document.getLookup().lookup(EditorCookie.class);
        assertNotNull(cake);
        String modified = cake.getDocument().getText(0, cake.getDocument().getLength());
        
        String replaced = text.replace("Result", "ResultX");
        
        assertEquals("Document not edited properly", replaced, modified);
    }
    
    public void testSingleResourceOperation() throws Exception {
        URI newUri = workDir.toURI().resolve("NewResource.java");
        ResourceOperation.CreateFile cf = new ResourceOperation.CreateFile(newUri.toString());
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Collections.singletonList(Union2.createSecond(cf)));
        
        List<String> res = WorkspaceEdit.applyEdits(Collections.singletonList(wkspEdit), false).get();
        assertEquals(1, res.size());
        assertEquals(newUri.toString(), res.get(0));
        
        assertNotNull(workDir.getFileObject("NewResource.java"));
        assertEquals(0, workDir.getFileObject("NewResource.java").asText().length());
    }
    
    public void testCreationWithSubfolders() throws Exception {
        URI newUri = workDir.toURI().resolve("parent1/parent2/NewResource.java");
        ResourceOperation.CreateFile cf = new ResourceOperation.CreateFile(newUri.toString());
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Collections.singletonList(Union2.createSecond(cf)));
        
        List<String> res = WorkspaceEdit.applyEdits(Collections.singletonList(wkspEdit), false).get();
        assertEquals(1, res.size());
        assertEquals(newUri.toString(), res.get(0));
        
        assertNotNull(workDir.getFileObject("parent1/parent2/NewResource.java"));
        assertEquals(0, workDir.getFileObject("parent1/parent2/NewResource.java").asText().length());
    }
    
    public void testCreationFailsAlreadyExists() throws Exception {
        workDir.createData("Existing.txt");
        
        URI newUri = workDir.toURI().resolve("Existing.txt");
        ResourceOperation.CreateFile cf = new ResourceOperation.CreateFile(newUri.toString());
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Collections.singletonList(Union2.createSecond(cf)));
        
        try {
            WorkspaceEdit.applyEdits(Collections.singletonList(wkspEdit), false).get();
            fail("Cannot replace existing resource");
        } catch (ExecutionException ex) {
            Throwable t = ex.getCause();
            assertTrue(t instanceof ResourceModificationException);
            
            ResourceModificationException rme = (ResourceModificationException)t;
            assertEquals(0, rme.getAppliedEdits().size());
            assertSame(wkspEdit, rme.getFailedEdit());
            assertEquals(0, rme.getFailedOperationIndex());
            assertEquals(ResourceModificationException.UNSPECIFIED_EDIT, rme.getFailedEditIndex());
        }
    }
    
    public void testTextEditsWithResourceCreation() throws Exception {
        URI newUri = workDir.toURI().resolve("NewResource.java");
        ResourceOperation.CreateFile cf = new ResourceOperation.CreateFile(newUri.toString());
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Collections.singletonList(Union2.createSecond(cf)));
        
        String text = FileUtil.toFileObject(getDataDir()).getFileObject("ProjectArtifactImplementation.java").asText();
        TextDocumentEdit tde = new TextDocumentEdit(newUri.toString(), Collections.singletonList(
            new TextEdit(0, 0, text)
        ));
        WorkspaceEdit wkspEdit2 = new WorkspaceEdit(Collections.singletonList(Union2.createFirst(tde)));
        
        List<String> res = WorkspaceEdit.applyEdits(Arrays.asList(wkspEdit, wkspEdit2), false).get();
        assertEquals(1, res.size());
        assertEquals(newUri.toString(), res.get(0));
        
        FileObject f = workDir.getFileObject("NewResource.java");
        
        assertNotNull(f);
        // the edited resource was still not saved !
        assertEquals(0, f.asText().length());
        
        EditorCookie cake = f.getLookup().lookup(EditorCookie.class);
        String edited = cake.getDocument().getText(0, cake.getDocument().getLength());
        assertEquals("Resource must be edited after creation", edited, text);
    }
    
    public void testFailedSimpleEdit() throws Exception {        
        document = FileUtil.copyFile(FileUtil.toFileObject(getDataDir()).getFileObject("ProjectArtifactImplementation.java"), workDir, "Document1");
        String text = document.asText();
        
        
        TextDocumentEdit edit = new TextDocumentEdit(document.toURI().toString(), Collections.singletonList(
            // deliberately deletes past document length.
            new TextEdit(1000, text.length() +1 , "New content")
        ));
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Collections.singletonList(Union2.createFirst((edit))));
        try {
            WorkspaceEdit.applyEdits(Collections.singletonList(wkspEdit), false).get();
            fail("Deleting past document lenght should fail");
        } catch (ExecutionException ex) {
            ResourceModificationException rme = (ResourceModificationException)ex.getCause();

            assertEquals(0, rme.getAppliedEdits().size());
            assertSame(wkspEdit, rme.getFailedEdit());
            assertEquals(0, rme.getFailedOperationIndex());
            assertEquals("First edit operation fails", 0, rme.getFailedEditIndex());
        }
    }
    
    public void testFailedEditOfMissingResource() throws Exception {
        URI newUri = workDir.toURI().resolve("NewResource.java");

        String text = FileUtil.toFileObject(getDataDir()).getFileObject("ProjectArtifactImplementation.java").asText();
        TextDocumentEdit tde = new TextDocumentEdit(newUri.toString(), Collections.singletonList(
            new TextEdit(0, 0, text)
        ));
        WorkspaceEdit wkspEdit2 = new WorkspaceEdit(Collections.singletonList(Union2.createFirst(tde)));
        
        try {
            WorkspaceEdit.applyEdits(Arrays.asList(wkspEdit2), false).get();
            fail("Should fail on editing non-existing resource");
        } catch (ExecutionException ex) {
            ResourceModificationException rme = (ResourceModificationException)ex.getCause();

            assertEquals(0, rme.getAppliedEdits().size());
            assertSame(wkspEdit2, rme.getFailedEdit());
            assertEquals(0, rme.getFailedOperationIndex());
            assertEquals("Opening the resource fails", ResourceModificationException.BEFORE_FIRST_EDIT, rme.getFailedEditIndex());
        }
    }
    
    public void testFailedResourceCreation() throws Exception {
        document = FileUtil.copyFile(FileUtil.toFileObject(getDataDir()).getFileObject("ProjectArtifactImplementation.java"), workDir, "NewResource");

        ResourceOperation.CreateFile cf = new ResourceOperation.CreateFile(document.toURI().toString());
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Collections.singletonList(Union2.createSecond(cf)));
        
        try {
            WorkspaceEdit.applyEdits(Arrays.asList(wkspEdit), false).get();
        } catch (ExecutionException ex) {
            ResourceModificationException rme = (ResourceModificationException)ex.getCause();

            assertEquals(0, rme.getAppliedEdits().size());
            assertSame(wkspEdit, rme.getFailedEdit());
            assertEquals(0, rme.getFailedOperationIndex());
        }
    }
    
    public void testFailedTextEditAfterResourceCreation() throws Exception {
        URI newUri = workDir.toURI().resolve("NewResource.java");
        ResourceOperation.CreateFile cf = new ResourceOperation.CreateFile(newUri.toString());

        String text = FileUtil.toFileObject(getDataDir()).getFileObject("ProjectArtifactImplementation.java").asText();
        TextDocumentEdit tde = new TextDocumentEdit(newUri.toString(), Collections.singletonList(
            new TextEdit(10, 20, text)
        ));
        WorkspaceEdit wkspEdit = new WorkspaceEdit(Arrays.asList(Union2.createSecond(cf), Union2.createFirst(tde)));
        
        try {
            WorkspaceEdit.applyEdits(Arrays.asList(wkspEdit), false).get();
            fail("Should fail on editing non-existing resource");
        } catch (ExecutionException ex) {
            ResourceModificationException rme = (ResourceModificationException)ex.getCause();

            assertEquals(0, rme.getAppliedEdits().size());
            assertSame(wkspEdit, rme.getFailedEdit());
            assertEquals(1, rme.getFailedOperationIndex());
            assertEquals("First text edit fails", 0, rme.getFailedEditIndex());
        }
    }
}
