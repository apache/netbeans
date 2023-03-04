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
package org.netbeans.api.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.lsp.HyperlinkLocationProvider;
import org.netbeans.spi.lsp.HyperlinkTypeDefLocationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
public class HyperlinkLocationTest extends NbTestCase {

    private FileObject srcFile;

    public HyperlinkLocationTest(String name) {
        super(name);
    }

    @Override
    public void setUp () throws Exception {
        super.setUp();
        clearWorkDir();
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        srcFile = FileUtil.createData(wd, "bar.test");
        MockMimeLookup.setInstances (MimePath.get ("text/foo"), new FooLocationProvider());
    }

    public void testHyperlinkResolve() {
        Document doc = createDocument("text/foo", "");
        int offset = 0;
        // @start region="testHyperlinkResolve"

        // Resolve a hyperlink at the given document offset...
        CompletableFuture<List<HyperlinkLocation>> future = HyperlinkLocation.resolve(doc, offset);

        // ...and get its target location(s)
        List<HyperlinkLocation> locations = future.getNow(null);
        assertNotNull(locations);
        assertEquals(1, locations.size());
        HyperlinkLocation location = locations.get(0);

        // get location's file object
        FileObject fileObject = location.getFileObject();
        assertEquals(srcFile, fileObject);

        // get location's start offset
        int startOffset = location.getStartOffset();
        assertEquals(10, startOffset);

        // get location's end offset
        int endOffset = location.getEndOffset();
        assertEquals(20, endOffset);
        // @end region="testHyperlinkResolve"
    }

    public void testHyperlinkTypeDefResolve() {
        Document doc = createDocument("text/foo", "");
        int offset = 0;
        // @start region="testHyperlinkTypeDefResolve"

        // Resolve a hyperlink at the given document offset...
        CompletableFuture<List<HyperlinkLocation>> future = HyperlinkLocation.resolveTypeDefinition(doc, offset);

        // ...and get its target type definition location(s)
        List<HyperlinkLocation> locations = future.getNow(null);
        assertNotNull(locations);
        assertEquals(1, locations.size());
        HyperlinkLocation location = locations.get(0);

        // get location's file object
        FileObject fileObject = location.getFileObject();
        assertEquals(srcFile, fileObject);

        // get location's start offset
        int startOffset = location.getStartOffset();
        assertEquals(10, startOffset);

        // get location's end offset
        int endOffset = location.getEndOffset();
        assertEquals(20, endOffset);
        // @end region="testHyperlinkTypeDefResolve"
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

    private class FooLocationProvider implements HyperlinkLocationProvider, HyperlinkTypeDefLocationProvider {

        @Override
        public CompletableFuture<HyperlinkLocation> getHyperlinkLocation(Document doc, int offset) {
            return CompletableFuture.completedFuture(HyperlinkLocationProvider.createHyperlinkLocation(srcFile, 10, 20));
        }

        @Override
        public CompletableFuture<HyperlinkLocation> getHyperlinkTypeDefLocation(Document doc, int offset) {
            return CompletableFuture.completedFuture(HyperlinkLocationProvider.createHyperlinkLocation(srcFile, 10, 20));
        }
    }
}
