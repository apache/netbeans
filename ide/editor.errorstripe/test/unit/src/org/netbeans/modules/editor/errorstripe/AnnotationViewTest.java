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

package org.netbeans.modules.editor.errorstripe;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.errorstripe.caret.CaretMarkProviderCreator;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationViewTest extends NbTestCase {
    
    public AnnotationViewTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws Exception {
        UnitUtilities.prepareTest(new String[] {"/org/netbeans/modules/editor/resources/annotations-test-layer.xml",
                                                "/org/netbeans/modules/editor/plain/resources/layer.xml",
                                                "/org/netbeans/modules/editor/errorstripe/test-layer.xml"},
                                  new Object[0]);
        
        CaretMarkProviderCreator.switchOff = true;
    }

    public void testModelToView() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                double         pos   = aView.modelToView(2);
                
                assertEquals(aView.viewToModel(pos)[0], aView.viewToModel(aView.modelToView(aView.viewToModel(pos)[0]))[0]);
                
                assertEquals((-1.0), aView.modelToView(LineDocumentUtils.getLineIndex(document, document.getLength()) + 1), 0.0001d);
            }
        });
    }
    
    public void testViewToModelIsContinuous() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                int[] last = new int[] {-1, -1};
		int topOffset = aView.topOffset();
                
                for (double pos = 0; pos < aView.getUsableHeight(); pos = pos + 1) {
                    int[] current = aView.viewToModel(pos + topOffset);
                    
                    if (current == null)
                        continue;
                    assertTrue(last[0] <= current[0]);
                    assertTrue(last[1] <= current[1]);
                    
                    last = current;
                }
            }
        });
    }
    
    public void testGetAnnotationIsContinuous() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                Mark mark = null;
                boolean wasMark = false;
		int topOffset = aView.topOffset();
                
                for (double pos = 0; pos < aView.getUsableHeight(); pos = pos + 1) {
                    Mark newMark = aView.getMarkForPoint(pos + topOffset);
                    
                    if (newMark != null && mark!= null) {
                        assertTrue(newMark == mark);
                    }
                    
                    if (wasMark) {
                        assertNull("pos=" + pos + ", mark=" + mark + ", newMark=" + newMark, newMark);
                    }
                    
                    if (mark != null && newMark == null) {
                        wasMark = true;
                    }
                    
                    mark = newMark;
                }
            }
        });
    }
    
    public void testGetLinesSpanIsContinuous() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                int startLine = 1;
                int linesCount = Utilities.getRowCount(document);
                
                while (startLine < linesCount) {
                    int[] span = aView.getLinesSpan(startLine);
                    
                    assertTrue(startLine >= span[0]);
                    assertTrue(startLine <= span[1]);
                    
                    if (span[1] < linesCount) {
                        int[] newSpan = aView.getLinesSpan(span[1] + 1);
                        
                        assertEquals(newSpan[0], span[1] + 1);
                    }
                    
                    startLine = span[1] + 1;
                }
            }
        });
    }
    
    public void testMarkSensitiveStripe1() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                double position = aView.modelToView(6);
                double start    = position - AnnotationView.UPPER_HANDLE;
                double end      = position + AnnotationView.PIXELS_FOR_LINE + AnnotationView.LOWER_HANDLE - 1;
                
                for (double pos = start; pos <= end; pos++) {
                    Mark m = aView.getMarkForPoint(pos);
                    
                    assertNotNull("pos=" + pos + ", start=" + start + ", end=" + end + ", position=" + position, m);
                }
                
                Mark m1 = aView.getMarkForPoint(start - 1);
                
                assertNull("There is a mark at position: " + (start - 1), m1);
                
                Mark m2 = aView.getMarkForPoint(end   + 1);
                
                assertNull("There is a mark at position: " + (end + 1), m2);
            }
        });
    }
    
    private static String[] getContents() {
        StringBuffer largeBuffer = new StringBuffer(16384);
        
        for (int cntr = 0; cntr < 16300; cntr++) {
            largeBuffer.append('\n');
        }
        
        List contents = new ArrayList();
        String large = largeBuffer.toString();
        
        for (int lines = 7; lines < 300; lines++) {
            contents.add(large.substring(0, lines + 1));
        }
        
        contents.add(large);
        
        return (String[] ) contents.toArray(new String[0]);
    }
    
    private static void performTest(final Action action) throws Exception {
        JFrame f = new JFrame();
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        TestMark mark1 = new TestMark(Status.STATUS_ERROR, null, null, new int[] {6, 6});
        TestMark mark2 = new TestMark(Status.STATUS_OK, null, null, new int[] {6, 6});
        
        List marks = Arrays.asList(new Mark[]{mark1, mark2});
        
        TestMarkProvider provider = new TestMarkProvider(Collections.EMPTY_LIST, UpToDateStatus.UP_TO_DATE_OK);
        TestMarkProviderCreator creator = TestMarkProviderCreator.getDefault();
        
        creator.setProvider(provider);
        
        AnnotationView aView = new AnnotationView(editor);
        
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(new JScrollPane(editor), BorderLayout.CENTER);
        f.getContentPane().add(aView, BorderLayout.EAST);
        
        f.setSize(500, 500);
        
        f.setVisible(true);

        String[] contents = getContents();
        
        for (int index = 0; index < contents.length; index++) {
            BaseDocument bd = (BaseDocument) editor.getDocument();
            
            bd.insertString(0, contents[index], null);
            
            provider.setMarks(marks);
            
            action.test(aView, bd);
            
            provider.setMarks(Collections.EMPTY_LIST);
            
            bd.remove(0, bd.getLength());
        }
        
        f.setVisible(false);
    }
    
    private abstract static class Action {
        public abstract void test(AnnotationView aView, BaseDocument document) throws Exception;
    }

    
    public void testAnnotationViewFactory() {
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        assertNotNull(new AnnotationViewFactory().createSideBar(editor));
    }

    protected @Override boolean runInEQ() {
        return true;
    }
    
}
