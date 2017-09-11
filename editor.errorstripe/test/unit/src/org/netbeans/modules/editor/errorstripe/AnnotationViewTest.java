/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                
                assertEquals((-1.0), aView.modelToView(Utilities.getLineOffset(document, document.getLength()) + 1), 0.0001d);
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
    
    private static abstract class Action {
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
