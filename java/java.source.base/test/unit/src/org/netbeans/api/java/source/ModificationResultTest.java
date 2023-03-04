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

package org.netbeans.api.java.source;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.parsing.api.Source;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;

/**
 *
 * @author Jan Lahoda
 */
public class ModificationResultTest extends NbTestCase {
    
    /** Creates a new instance of ModificationResultTest */
    public ModificationResultTest(String name) {
        super(name);
    }
    
    private FileObject testFile;
    private CloneableEditorSupport ces;
    
    private void prepareTest(String code) throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject root = fs.getRoot();
        testFile = FileUtil.createData(root, "test/test.java");
        
        TestUtilities.copyStringToFile(testFile, code);
        
        DataObject od = DataObject.find(testFile);
        
        ces = (CloneableEditorSupport) od.getCookie(EditorCookie.class);
    }
    
    private ModificationResult prepareInsertResult() throws Exception {
        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, start1, start1, "", "new-test1\n", Source.create(testFile));
        PositionRef start2 = ces.createPositionRef(10, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, start2, start2, "", "new-test2\n", Source.create(testFile));
        
        ModificationResult result = new ModificationResult();
        
        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));
        
        return result;
    }
    
    private ModificationResult prepareInsertResultFiltered() throws Exception {
        PositionRef start1 = ces.createPositionRef(4, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, start1, start1, "", "new-test1\n", Source.create(testFile));
        PositionRef start2 = ces.createPositionRef(8, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, start2, start2, "", "new-test2\n", Source.create(testFile));

        ModificationResult result = new ModificationResult();

        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));

        return result;
    }

    private void performTestToFile(String creator) throws Exception {
        prepareTest("test\ntest\ntest\n");
        
        Method m = ModificationResultTest.class.getDeclaredMethod(creator);
        
        ModificationResult result = (ModificationResult) m.invoke(this);
        
        result.commit();
        
        Document doc = ces.openDocument();
        
        ref(doc.getText(0, doc.getLength()));
        
        compareReferenceFiles();
    }
    
    private void performTestToDocument(String creator) throws Exception {
        prepareTest("test\ntest\ntest\n");
        
        Document doc = ces.openDocument();
        
        Method m = ModificationResultTest.class.getDeclaredMethod(creator);
        
        ModificationResult result = (ModificationResult) m.invoke(this);
        
        result.commit();
        
        ref(doc.getText(0, doc.getLength()));
        
        compareReferenceFiles();
    }
    
    private void performTestToGuardedDocument(String creator) throws Exception {
        prepareTest("test\ntest\ntest\n");
        
        StyledDocument doc = ces.openDocument();
        
        NbDocument.markGuarded(doc, 4, 6);
        
        Method m = ModificationResultTest.class.getDeclaredMethod(creator);
        
        ModificationResult result = (ModificationResult) m.invoke(this);
        
        for (FileObject fo : result.getModifiedFileObjects()) {
            for (ModificationResult.Difference diff : result.getDifferences(fo)) {
                diff.setCommitToGuards(true);
            }
        }

        
        result.commit();
        
        ref(doc.getText(0, doc.getLength()));
        
        compareReferenceFiles();
    }
    
    private void performTestToFileNoDocumentOpen(String creator) throws Exception {
        prepareTest("test\ntest\ntest\n");

        Method m = ModificationResultTest.class.getDeclaredMethod(creator);

        ModificationResult result = (ModificationResult) m.invoke(this);

        result.commit();

        ref(testFile.asText());

        compareReferenceFiles();
    }

    private void performTestToResultingSource(String creator) throws Exception {
        prepareTest("test\ntest\ntest\n");

        Method m = ModificationResultTest.class.getDeclaredMethod(creator);

        ModificationResult result = (ModificationResult) m.invoke(this);

        ref(result.getResultingSource(testFile));

        compareReferenceFiles();
    }

    private ModificationResult prepareRemoveResult() throws Exception {
        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        PositionRef end1 = ces.createPositionRef(9, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.REMOVE, start1, end1, "test", "", Source.create(testFile));
        PositionRef start2 = ces.createPositionRef(11, Bias.Forward);
        PositionRef end2 = ces.createPositionRef(12, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.REMOVE, start2, end2, "e", "", Source.create(testFile));
        
        ModificationResult result = new ModificationResult();
        
        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));
        
        return result;
    }
    
    private ModificationResult prepareModificationResult1() throws Exception {
        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        PositionRef end1 = ces.createPositionRef(9, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start1, end1, "test", "ab", Source.create(testFile));
        PositionRef start2 = ces.createPositionRef(11, Bias.Forward);
        PositionRef end2 = ces.createPositionRef(13, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start2, end2, "es", "a", Source.create(testFile));
        
        ModificationResult result = new ModificationResult();
        
        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));
        
        return result;
    }
    
    private ModificationResult prepareModificationResult2() throws Exception {
        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        PositionRef end1 = ces.createPositionRef(9, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start1, end1, "test", "abcde", Source.create(testFile));
        PositionRef start2 = ces.createPositionRef(11, Bias.Forward);
        PositionRef end2 = ces.createPositionRef(13, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start2, end2, "es", "a", Source.create(testFile));
        
        ModificationResult result = new ModificationResult();
        
        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));
        
        return result;
    }
    
    public void testInsertToFile() throws Exception {
        performTestToFile("prepareInsertResult");
    }
    
    public void testInsertToDocument() throws Exception {
        performTestToDocument("prepareInsertResult");
    }
    
    public void testInsertToGuardedDocument() throws Exception {
        performTestToGuardedDocument("prepareInsertResult");
    }
    
    public void testRemoveFromFile() throws Exception {
        performTestToFile("prepareRemoveResult");
    }
    
    public void testRemoveFromDocument() throws Exception {
        performTestToDocument("prepareRemoveResult");
    }
    
    public void testModification1ToFile() throws Exception {
        performTestToFile("prepareModificationResult1");
    }
    
    public void testModification1ToDocument() throws Exception {
        performTestToDocument("prepareModificationResult1");
    }
    
    public void testModification2ToFile() throws Exception {
        performTestToFile("prepareModificationResult2");
    }
    
    public void testModification2ToDocument() throws Exception {
        performTestToDocument("prepareModificationResult2");
    }
    
    public void testCRLF1() throws Exception {
        prepareTest("test\r\ntest\r\ntest\r\n");
        
        Method m = ModificationResultTest.class.getDeclaredMethod("prepareModificationResult2");
        
        ModificationResult result = (ModificationResult) m.invoke(this);
        
        result.commit();
        
        assertEquals("test\r\nabcde\r\ntat\r\n", testFile.asText());
    }
    
    public void test152941() throws Exception {
        prepareTest("test\ntest\ntest\n");

        ModificationResult result = new ModificationResult();

        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();

        try {
            result.getResultingSource(testFile);
            fail("No exception");
        } catch (IllegalArgumentException ex) {
            //correct exception
        }
    }

    public void testCRLF197538() throws Exception {
        prepareTest("test\r\ntest\r\ntest\r\n");

        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        PositionRef end1 = ces.createPositionRef(9, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start1, end1, "test", "abcde", Source.create(testFile));
        PositionRef start2 = ces.createPositionRef(10, Bias.Forward);
        PositionRef end2 = ces.createPositionRef(13, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start2, end2, "tes", "a", Source.create(testFile));

        ModificationResult result = new ModificationResult();

        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));

        result.commit();

        assertEquals("test\r\nabcde\r\nat\r\n", testFile.asText());
    }

    public void testFilteringCommitToFile189203a() throws Exception {
        TestUtil.setJavaFileFilter(new JavaFileFilterImplementationImpl());
        try {
            performTestToFileNoDocumentOpen("prepareInsertResultFiltered");
        } finally {
            TestUtil.setJavaFileFilter(null);
        }
    }

    public void testFilteringCommitToFile189203b() throws Exception {
        TestUtil.setJavaFileFilter(new JavaFileFilterImplementationImpl());
        try {
            performTestToResultingSource("prepareInsertResultFiltered");
        } finally {
            TestUtil.setJavaFileFilter(null);
        }
    }

    private static final class JavaFileFilterImplementationImpl implements JavaFileFilterImplementation {

        @Override
        public Reader filterReader(final Reader r) {
            try {
                char[] data = readFully(r);
                int j = 0;
                for (int i = 1; i < data.length; i++) {
                    if (data[i - 1] != '\n') {
                        data[j++] = data[i];
                    }
                }
                return new CharArrayReader(data, 0, j) {
                    @Override
                    public void close() {
                        super.close();
                        try {
                            r.close();
                        } catch (IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                };
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public CharSequence filterCharSequence(CharSequence charSequence) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Writer filterWriter(final Writer w) {
            return new CharArrayWriter() {
                @Override
                public void close() {
                    try {
                        super.close();
                        for (String line : new String(toCharArray()).split("\n")) {
                            w.write("t" + line);
                            w.write(System.getProperty("line.separator"));
                        }
                        w.close();
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            };
        }

        @Override
        public void addChangeListener(ChangeListener listener) {}

        @Override
        public void removeChangeListener(ChangeListener listener) {}

    }

    private static char[] readFully(Reader reader) throws IOException {
        CharArrayWriter baos = new CharArrayWriter();
        int r;

        while ((r = reader.read()) != (-1)) {
            baos.append((char) r);
        }

        return baos.toCharArray();
    }

}
