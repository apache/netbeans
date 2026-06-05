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

package org.netbeans.modules.editor.hints;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import static org.netbeans.modules.editor.hints.AnnotationHolder.*;

import org.netbeans.spi.editor.hints.Fix;

import static org.netbeans.spi.editor.hints.Severity.*;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationHolderTest extends NbTestCase {
    
    private FileObject file;
    private Document doc;
    private EditorCookie ec;
    
    /** Creates a new instance of AnnotationHolderTest */
    public AnnotationHolderTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(MimeDataProviderImpl.class);
        FileSystem fs = FileUtil.createMemoryFileSystem();
        
        file = fs.getRoot().createData("test.txt");
        
        writeIntoFile(file, "01234567890123456789\n  abcdefg  \n  hijklmnop");
        
        DataObject od = DataObject.find(file);
        
        ec = od.getCookie(EditorCookie.class);
        doc = ec.openDocument();
    }
    
    public void testMultiLineErrors() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(null, Severity.ERROR, "clank-diagnostics-error", "1", null, 
                ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()), file, new int[] {33 - 30, 55 - 30 - 1}, 
                new int[] {40 - 30, 58 - 30 -1});
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(null, Severity.WARNING, "clank-diagnostics-warning", "1", null, 
                ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()), file, new int[] {55 - 30 - 1, 69 - 30 - 2}, 
                new int[] {58 - 30 - 1, 75 - 30 - 2});
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        assertHighlights("",bag, new int[] {33 - 30, 40 - 30, 55 - 30 - 1, 58 - 30 - 1, 69 - 30 -2, 75 - 30 - 2}, 
                new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc), 
                    AnnotationHolder.getColoring(ERROR, doc), 
                    AnnotationHolder.getColoring(WARNING, doc)});
    }    
    
    public void testComputeHighlightsOneLayer1() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 3);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 5, 6);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 3, 5, 6}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc), AnnotationHolder.getColoring(ERROR, doc)});
    }
    
    public void testComputeHighlightsOneLayer2() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 7);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 5, 6);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 7}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc)});
    }
    
    public void testComputeHighlightsOneLayer3() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 3, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);

        assertHighlights("",bag, new int[] {1, 7}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc)});
    }
    
    public void testComputeHighlightsOneLayer4() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 1, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 7}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc)});
    }
    
    public void testComputeHighlightsTwoLayers1() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 3);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 5, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 3, 5, 7}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc), AnnotationHolder.getColoring(WARNING, doc)});
    }
    
    public void testComputeHighlightsTwoLayers2() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 7);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 3, 5);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 7}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc)});
    }
    
    public void testComputeHighlightsTwoLayers3() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 4, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {3, 5, /*6*/5, 7}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc), AnnotationHolder.getColoring(WARNING, doc)});
    }
    
    public void testComputeHighlightsTwoLayers4() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 4);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, /*2*/3, 3, 5}, new AttributeSet[] {AnnotationHolder.getColoring(WARNING, doc), AnnotationHolder.getColoring(ERROR, doc)});
    }
    
    public void testComputeHighlightsTwoLayers5() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, /*2*/3, 3, 5, /*6*/5, 7}, new AttributeSet[] {AnnotationHolder.getColoring(WARNING, doc), AnnotationHolder.getColoring(ERROR, doc), AnnotationHolder.getColoring(WARNING, doc)});
    }
    
    public void testNullSpan() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed3 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 7);
        
        ec.open();
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed1, ed3));
        
        ec.close();
    }
    
    public void testMultilineHighlights() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 47 - 30, 72 - 30);
        OffsetsBag bag = new OffsetsBag(doc);
        
        List<ErrorDescription> errors = Arrays.asList(ed1);
        BaseDocument bdoc = (BaseDocument) doc;
        
        bag = new OffsetsBag(doc);
        AnnotationHolder.updateHighlightsOnLine(bag, bdoc, bdoc.createPosition(LineDocumentUtils.getLineStartFromIndex(bdoc, 0)), errors);
        
        assertHighlights("", bag, new int[] {47 - 30, 50 - 30}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc)});
        
        bag = new OffsetsBag(doc);
        AnnotationHolder.updateHighlightsOnLine(bag, bdoc, bdoc.createPosition(LineDocumentUtils.getLineStartFromIndex(bdoc, 1)), errors);
        
        assertHighlights("", bag, new int[] {53 - 30, 60 - 30}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc)});
        
        bag = new OffsetsBag(doc);
        AnnotationHolder.updateHighlightsOnLine(bag, bdoc, bdoc.createPosition(LineDocumentUtils.getLineStartFromIndex(bdoc, 2)), errors);
        
        assertHighlights("", bag, new int[] {65 - 30, 72 - 30}, new AttributeSet[] {AnnotationHolder.getColoring(ERROR, doc)});
    }
    
    public void testComputeSeverity() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, "2", file, 1, 7);
        ErrorDescription ed3 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 7);
        ErrorDescription ed4 = ErrorDescriptionFactory.createErrorDescription(Severity.VERIFIER, "2", file, 1, 7);
        
        ec.open();
        
        class AttacherImpl implements Attacher {
            private ParseErrorAnnotation annotation;
            public void attachAnnotation(Position line, ParseErrorAnnotation a, boolean synchronous) throws BadLocationException {
                if (line.getOffset() == 0) {
                    this.annotation = a;
                }
            }
            public void detachAnnotation(ParseErrorAnnotation a, boolean synchronous) {}
        }
        
        AttacherImpl impl = new AttacherImpl();
        
        AnnotationHolder.getInstance(file).attacher = impl;
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed1, ed2, ed3));
        
        assertEquals(Severity.ERROR, impl.annotation.getSeverity());
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed2, ed3));
        
        assertEquals(Severity.WARNING, impl.annotation.getSeverity());
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed2));
        
        assertEquals(Severity.HINT, impl.annotation.getSeverity());
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed2, ed4));
        
        assertEquals(Severity.VERIFIER, impl.annotation.getSeverity());
        
        ec.close();
    }
    
    public void testTypeIntoLine() throws Exception {
        performTypingTest(25, "a", new int[0]);
    }
    
    public void testTypeOnLineStart() throws Exception {
        performTypingTest(21, "a", new int[0]);
    }
    
    public void testTypeOnLineStartWithNewline() throws Exception {
        performTypingTest(21, "a\n", new int[0]);
    }
    
    public void testTypeOnLineStartWithNewlines() throws Exception {
        performTypingTest(21, "a\na\na\na\n", new int[0]);
    }
    
    public void testTypeNewline() throws Exception {
        performTypingTest(22, "asdasd\nasdfasdf", new int[] {23, 25}, new int[0]);
    }

    public void testType190393a() throws Exception {
        doc.remove(0, doc.getLength());
        doc.insertString(0, "1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne", null);
        performTypingTest(5, "x", new int[] {8, 9}, new int[] {9, 10});
    }

    public void testType190393b() throws Exception {
        doc.remove(0, doc.getLength());
        doc.insertString(0, "1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne", null);
        performTypingTest(4, 1, "", new int[] {8, 9}, new int[] {7, 8});
    }
    
    public void test205675() throws Exception {
        doc.remove(0, doc.getLength());
        doc.insertString(0, "a\nb\nc\nd\ne\n", null);
        
        ErrorDescription ed0 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "0", file, 0, 1);
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 2, 3);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 4, 5);
        AnnotationHolder ah = AnnotationHolder.getInstance(file);
        
        ah.setErrorDescriptions("test", Arrays.asList(ed0, ed1, ed2));
        
        assertEquals(Arrays.asList(ed0), ah.getErrorsGE(0));
        assertEquals(Arrays.asList(ed1), ah.getErrorsGE(1));
        assertEquals(Arrays.asList(ed1), ah.getErrorsGE(2));
        assertEquals(Arrays.asList(ed2), ah.getErrorsGE(3));
        assertEquals(Arrays.asList(ed2), ah.getErrorsGE(4));
        assertEquals(Arrays.asList(), ah.getErrorsGE(5));
    }
    
    private void performTypingTest(int index, String insertWhat, int[] highlightSpans) throws Exception {
        performTypingTest(index, insertWhat, new int[] {21, 32}, highlightSpans);
    }
    
    private void performTypingTest(int index, String insertWhat, int[] errorSpan, int[] highlightSpans) throws Exception {
        performTypingTest(index, 0, insertWhat, errorSpan, highlightSpans);
    }

    private void performTypingTest(int index, int remove, String insertWhat, int[] errorSpan, int[] highlightSpans) throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, errorSpan[0], errorSpan[1]);
        
        ec.open();
        
        //these tests currently ignore annotations:
        class AttacherImpl implements Attacher {
            public void attachAnnotation(Position line, ParseErrorAnnotation a, boolean synchronous) throws BadLocationException {}
            public void detachAnnotation(ParseErrorAnnotation a, boolean synchronous) {}
        }
        
        AnnotationHolder.getInstance(file).attacher = new AttacherImpl();
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed1));

        doc.remove(index, remove);
        doc.insertString(index, insertWhat, null);

        assertHighlights("highlights correct", AnnotationHolder.getBag(doc), highlightSpans, null);
        
        LifecycleManager.getDefault().saveAll();
        
        ec.close();
    }
    
    private void assertHighlights(String message, OffsetsBag bag, int[] spans, AttributeSet[] values) {
        HighlightsSequence hs = bag.getHighlights(0, Integer.MAX_VALUE);
        int index = 0;
        
        while (hs.moveNext()) {
            assertEquals(message, spans[2 * index], hs.getStartOffset());
            assertEquals(message, spans[2 * index + 1], hs.getEndOffset());
            if (values != null) {
                assertEquals(message, values[index], hs.getAttributes());
            }
            index++;
        }

        assertEquals(2 * index, spans.length);
    }
    
    @Override 
    protected boolean runInEQ() {
        return true;
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    public static final class MimeDataProviderImpl implements MimeDataProvider {

        @SuppressWarnings("deprecation")
        public Lookup getLookup(MimePath mimePath) {
            return Lookups.singleton(new DefaultEditorKit() {
                @Override
                public Document createDefaultDocument() {
                    return new GuardedDocument(this.getClass());
                }
            });
        }
        
    }
}
