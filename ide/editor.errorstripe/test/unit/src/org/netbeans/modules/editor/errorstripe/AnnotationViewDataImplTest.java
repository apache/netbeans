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
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.errorstripe.caret.CaretMarkProviderCreator;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.modules.editor.options.AnnotationTypeProcessor;
import org.netbeans.modules.editor.plain.PlainKit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationViewDataImplTest extends NbTestCase {
    
    public AnnotationViewDataImplTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        UnitUtilities.prepareTest(new String[] {"/org/netbeans/modules/editor/resources/annotations-test-layer.xml",
                                                "/org/netbeans/modules/editor/plain/resources/layer.xml",
                                                "/org/netbeans/modules/editor/errorstripe/test-layer.xml"},
                                  new Object[0]);
                
        AnnotationTypes.getTypes().registerLoader(new AnnotationsLoader());
        CaretMarkProviderCreator.switchOff = true;
    }

    public void testGetMainMarkForBlock() throws /*BadLocation*/Exception {
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        BaseDocument bd = (BaseDocument) editor.getDocument();

        bd.insertString(0, "\n\n\n\n\n\n\n\n\n\n", null);
        
        TestMark mark1 = new TestMark(Status.STATUS_ERROR, null, null, new int[] {2, 2});
        TestMark mark2 = new TestMark(Status.STATUS_OK, null, null, new int[] {2, 2});
        TestMark mark3 = new TestMark(Status.STATUS_WARNING, null, null, new int[] {2, 4});
        
        AnnotationDesc test1 = new TestAnnotationDesc(bd, bd.createPosition(7), "test-annotation-1");
        AnnotationDesc test2 = new TestAnnotationDesc(bd, bd.createPosition(8), "test-annotation-2");
        AnnotationDesc test3 = new TestAnnotationDesc(bd, bd.createPosition(8), "test-annotation-8");
        AnnotationDesc test4 = new TestAnnotationDesc(bd, bd.createPosition(9), "test-annotation-8");
        
        bd.getAnnotations().addAnnotation(test1);
        bd.getAnnotations().addAnnotation(test2);
        bd.getAnnotations().addAnnotation(test3);
        bd.getAnnotations().addAnnotation(test4);
        
        List marks1 = Arrays.asList(new Mark[]{mark1, mark2, mark3});
        List marks2 = Arrays.asList(new Mark[]{mark1, mark3});
        List marks3 = Arrays.asList(new Mark[]{mark2, mark3});
        List marks4 = Arrays.asList(new Mark[]{mark1, mark2});
        List marks5 = Arrays.asList(new Mark[]{mark3});
        
        TestMarkProvider provider = new TestMarkProvider(marks1, UpToDateStatus.UP_TO_DATE_OK);
        TestMarkProviderCreator creator = TestMarkProviderCreator.getDefault();
        
        creator.setProvider(provider);
        
        AnnotationView aView = new AnnotationView(editor);
        AnnotationViewDataImpl data = (AnnotationViewDataImpl) aView.getData();
        
        assertEquals(mark1, data.getMainMarkForBlock(2, 2));
        assertEquals(mark1, data.getMainMarkForBlock(2, 3));
        assertEquals(mark1, data.getMainMarkForBlock(2, 4));
        assertEquals(mark1, data.getMainMarkForBlock(2, 6));
        assertEquals(mark3, data.getMainMarkForBlock(3, 6));
        assertEquals(mark3, data.getMainMarkForBlock(3, 3));
        assertEquals(null, data.getMainMarkForBlock(6, 6));
        assertEquals(Status.STATUS_ERROR, data.getMainMarkForBlock(7, 7).getStatus());
        assertEquals(Status.STATUS_WARNING, data.getMainMarkForBlock(8, 8).getStatus());
        bd.getAnnotations().activateNextAnnotation(8);
        assertEquals(Status.STATUS_WARNING, data.getMainMarkForBlock(8, 8).getStatus());
        bd.getAnnotations().activateNextAnnotation(8);
        assertEquals(Status.STATUS_WARNING, data.getMainMarkForBlock(8, 8).getStatus());
        assertNull(data.getMainMarkForBlock(9, 9));
        assertEquals(Status.STATUS_ERROR, data.getMainMarkForBlock(7, 9).getStatus());
        
        provider.setMarks(marks2);
        
        bd.getAnnotations().removeAnnotation(test3);
        
        assertEquals(mark1, data.getMainMarkForBlock(2, 2));
        assertEquals(mark1, data.getMainMarkForBlock(2, 3));
        assertEquals(mark1, data.getMainMarkForBlock(2, 4));
        assertEquals(mark1, data.getMainMarkForBlock(2, 6));
        assertEquals(mark3, data.getMainMarkForBlock(3, 6));
        assertEquals(mark3, data.getMainMarkForBlock(3, 3));
        assertEquals(null, data.getMainMarkForBlock(6, 6));

        assertEquals(Status.STATUS_ERROR, data.getMainMarkForBlock(7, 7).getStatus());
        assertEquals(Status.STATUS_WARNING, data.getMainMarkForBlock(8, 8).getStatus());
        assertNull(data.getMainMarkForBlock(9, 9));
        assertEquals(Status.STATUS_ERROR, data.getMainMarkForBlock(7, 9).getStatus());
        
        provider.setMarks(marks3);
        
        assertEquals(mark3, data.getMainMarkForBlock(2, 2));
        assertEquals(mark3, data.getMainMarkForBlock(2, 3));
        assertEquals(mark3, data.getMainMarkForBlock(2, 4));
        assertEquals(mark3, data.getMainMarkForBlock(2, 6));
        assertEquals(mark3, data.getMainMarkForBlock(3, 6));
        assertEquals(mark3, data.getMainMarkForBlock(3, 3));
        assertEquals(null, data.getMainMarkForBlock(6, 6));
        
        provider.setMarks(marks4);
        
        assertEquals(mark1, data.getMainMarkForBlock(2, 2));
        assertEquals(mark1, data.getMainMarkForBlock(2, 3));
        assertEquals(mark1, data.getMainMarkForBlock(2, 4));
        assertEquals(mark1, data.getMainMarkForBlock(2, 6));
        assertEquals(null, data.getMainMarkForBlock(3, 6));
        assertEquals(null, data.getMainMarkForBlock(3, 3));
        assertEquals(null, data.getMainMarkForBlock(6, 6));
        
        provider.setMarks(marks5);
        
        assertEquals(mark3, data.getMainMarkForBlock(2, 2));
        assertEquals(mark3, data.getMainMarkForBlock(2, 3));
        assertEquals(mark3, data.getMainMarkForBlock(2, 4));
        assertEquals(mark3, data.getMainMarkForBlock(2, 6));
        assertEquals(mark3, data.getMainMarkForBlock(3, 6));
        assertEquals(mark3, data.getMainMarkForBlock(3, 3));
        assertEquals(null, data.getMainMarkForBlock(6, 6));
    }
    
    public void testComputeTotalStatus() throws Exception {
        JFrame f = new JFrame();
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        AnnotationView aView = new AnnotationView(editor);
        AnnotationViewDataImpl data = (AnnotationViewDataImpl) aView.getData();
        
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(new JScrollPane(editor), BorderLayout.CENTER);
        f.getContentPane().add(aView, BorderLayout.EAST);
        
        f.setSize(500, 500);
        
        f.setVisible(true);

        BaseDocument bd = (BaseDocument) editor.getDocument();
        
        bd.insertString(0, "\n\n\n\n\n\n\n\n\n\n", null);
        
        Position start = bd.createPosition(Utilities.getRowStartFromLineOffset(bd, 2));
        
        AnnotationDesc a1 = new AnnotationTestUtilities.TestAnnotationDesc1(bd, start);
        AnnotationDesc a2 = new AnnotationTestUtilities.TestAnnotationDesc2(bd, start);
        
        bd.getAnnotations().addAnnotation(a1);
        bd.getAnnotations().addAnnotation(a2);
        
        assertEquals(Status.STATUS_ERROR, data.computeTotalStatus());
        
        bd.getAnnotations().activateNextAnnotation(2);
        
        assertEquals(Status.STATUS_ERROR, data.computeTotalStatus());
        
        f.setVisible(false);
    }
    
    public void testMarkUpdates() {
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        TestMark mark1 = new TestMark(Status.STATUS_ERROR, null, null, new int[] {2, 2});
        TestMark mark2 = new TestMark(Status.STATUS_OK, null, null, new int[] {2, 2});
        TestMark mark3 = new TestMark(Status.STATUS_OK, null, null, new int[] {4, 6});
        
        List<Mark> marks = Arrays.asList(new Mark[]{mark1, mark2});
        List<Mark> marksOnlyFirst = Arrays.asList(new Mark[]{mark1});
        List<Mark> marksOnlySecond = Arrays.asList(new Mark[]{mark2});
        List<Mark> marksFirstAndThird = Arrays.asList(new Mark[]{mark1, mark3});
        
        TestMarkProvider provider = new TestMarkProvider(marks, UpToDateStatus.UP_TO_DATE_OK);
        TestMarkProviderCreator creator = TestMarkProviderCreator.getDefault();
        
        creator.setProvider(provider);
        
        AnnotationView aView = new AnnotationView(editor);
        AnnotationViewDataImpl data = (AnnotationViewDataImpl) aView.getData();
        
        Collection<Mark> mergedMarks;
        SortedMap<Integer, List<Mark>> map;
        
        mergedMarks = data.getMergedMarks();
        
        assertEquals(new LinkedHashSet<Mark>(marks), mergedMarks);
        
        map = data.getMarkMap();
        
        assertEquals(1, map.size());
        assertEquals(marks, map.get(map.firstKey()));
        
        provider.setMarks(marksOnlyFirst);
        
        mergedMarks = data.getMergedMarks();
        
        assertEquals(new LinkedHashSet<Mark>(marksOnlyFirst), mergedMarks);
        
        map = data.getMarkMap();
        
        assertEquals(1, map.size());
        assertEquals(marksOnlyFirst, map.get(map.firstKey()));
        
        provider.setMarks(marksFirstAndThird);
        
        mergedMarks = data.getMergedMarks();
        
        assertEquals(new LinkedHashSet<Mark>(marksFirstAndThird), mergedMarks);
        
        map = data.getMarkMap();
        
        assertEquals(4, map.size());
        assertEquals(new HashSet(Arrays.asList(new Integer[] {new Integer(2), new Integer(4), new Integer(5), new Integer(6)})), map.keySet());
        assertEquals(Arrays.asList(new Mark[] {mark1}), map.get(new Integer(2)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(4)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(5)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(6)));
        
        provider.setMarks(Collections.EMPTY_LIST);
        
        mergedMarks = data.getMergedMarks();
        
        assertEquals(Collections.emptySet(), mergedMarks);
        
        map = data.getMarkMap();
        
        assertEquals(0, map.size());
        
        provider.setMarks(marksFirstAndThird);
        
        mergedMarks = data.getMergedMarks();
        
        assertEquals(new LinkedHashSet<Mark>(marksFirstAndThird), mergedMarks);
        
        map = data.getMarkMap();
        
        assertEquals(4, map.size());
        assertEquals(new HashSet(Arrays.asList(new Integer[] {new Integer(2), new Integer(4), new Integer(5), new Integer(6)})), map.keySet());
        assertEquals(Arrays.asList(new Mark[] {mark1}), map.get(new Integer(2)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(4)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(5)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(6)));
    }

    public void testMarkPriorities() throws Exception {
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        BaseDocument bd = (BaseDocument) editor.getDocument();

        bd.insertString(0, "\n\n\n\n\n\n\n\n\n\n", null);
        
        //test marks:
        TestMark mark1 = new TestMark(Status.STATUS_OK, null, null, new int[] {2, 2}, 99);
        TestMark mark2 = new TestMark(Status.STATUS_OK, null, null, new int[] {2, 2}, 10);
        TestMark mark3 = new TestMark(Status.STATUS_OK, null, null, new int[] {3, 4}, 5);
        
        TestMark mark4 = new TestMark(Status.STATUS_ERROR, null, null, new int[] {2, 2}, 1000);
        TestMark mark5 = new TestMark(Status.STATUS_ERROR, null, null, new int[] {2, 2}, 100);
        TestMark mark6 = new TestMark(Status.STATUS_ERROR, null, null, new int[] {3, 4}, 50);
        
        List marks1 = Arrays.asList(new Mark[]{mark1, mark2, mark3});
        List marks2 = Arrays.asList(new Mark[]{mark2, mark1, mark3});
        List marks3 = Arrays.asList(new Mark[]{mark1, mark2, mark3, mark4, mark5, mark6});
        
        TestMarkProvider provider = new TestMarkProvider(marks1, UpToDateStatus.UP_TO_DATE_OK);
        TestMarkProviderCreator creator = TestMarkProviderCreator.getDefault();
        
        creator.setProvider(provider);
        
        AnnotationView aView = new AnnotationView(editor);
        AnnotationViewDataImpl data = (AnnotationViewDataImpl) aView.getData();
        
        assertEquals(mark2, data.getMainMarkForBlock(2, 2));
        assertEquals(mark3, data.getMainMarkForBlock(2, 3));
        assertEquals(mark3, data.getMainMarkForBlock(3, 4));
        
        assertEquals(null, data.getMainMarkForBlock(6, 6));
        
        provider.setMarks(marks2);
        
        assertEquals(mark2, data.getMainMarkForBlock(2, 2));
        assertEquals(mark3, data.getMainMarkForBlock(2, 3));
        assertEquals(mark3, data.getMainMarkForBlock(3, 4));
        
        assertEquals(null, data.getMainMarkForBlock(6, 6));
        
        provider.setMarks(marks3);
        
        assertEquals(mark5, data.getMainMarkForBlock(2, 2));
        assertEquals(mark6, data.getMainMarkForBlock(2, 3));
        assertEquals(mark6, data.getMainMarkForBlock(3, 4));
        
        assertEquals(null, data.getMainMarkForBlock(6, 6));
        
        provider.setMarks(Collections.EMPTY_LIST);
        
        //test annotations:
        AnnotationDesc test1 = new TestAnnotationDesc(bd, bd.createPosition(2), "test-annotation-priority-1");
        AnnotationDesc test2 = new TestAnnotationDesc(bd, bd.createPosition(2), "test-annotation-priority-2");
        AnnotationDesc test3 = new TestAnnotationDesc(bd, bd.createPosition(2), "test-annotation-priority-3");
        AnnotationDesc test4 = new TestAnnotationDesc(bd, bd.createPosition(2), "test-annotation-priority-4");
        
        bd.getAnnotations().addAnnotation(test1);
        bd.getAnnotations().addAnnotation(test2);
        
        assertEquals(test2, ((AnnotationMark) data.getMainMarkForBlock(2, 2)).getAnnotationDesc());
        
        bd.getAnnotations().activateNextAnnotation(2);
        
        assertEquals(test2, ((AnnotationMark) data.getMainMarkForBlock(2, 2)).getAnnotationDesc());
        
        bd.getAnnotations().activateNextAnnotation(2);
        
        bd.getAnnotations().addAnnotation(test3);
        bd.getAnnotations().addAnnotation(test4);
        
        assertEquals(test4, ((AnnotationMark) data.getMainMarkForBlock(2, 2)).getAnnotationDesc());
        
        bd.getAnnotations().activateNextAnnotation(2);
        
        assertEquals(test4, ((AnnotationMark) data.getMainMarkForBlock(2, 2)).getAnnotationDesc());
        
        bd.getAnnotations().activateNextAnnotation(2);
        
        assertEquals(test4, ((AnnotationMark) data.getMainMarkForBlock(2, 2)).getAnnotationDesc());
        
        //test interaction between annotations and marks:
        List marks4 = Arrays.asList(new Mark[]{mark1});
        
        provider.setMarks(marks4);
        
        bd.getAnnotations().removeAnnotation(test2);
        bd.getAnnotations().removeAnnotation(test3);
        bd.getAnnotations().removeAnnotation(test4);
        
        assertEquals(mark1, data.getMainMarkForBlock(2, 2));
        
        bd.getAnnotations().addAnnotation(test2);
        
        assertEquals(test2, ((AnnotationMark) data.getMainMarkForBlock(2, 2)).getAnnotationDesc());
    }
    
    protected @Override boolean runInEQ() {
        return true;
    }
    
    static class TestAnnotationDesc extends AnnotationDesc {
        
        private BaseDocument doc;
        private Position position;
        private String   type;
        
        public TestAnnotationDesc (BaseDocument bd, Position position, String type) {
            super(position.getOffset(), -1);
            this.doc      = bd;
            this.position = position;
            this.type = type;
        }
        
        public String getShortDescription() {
            return getAnnotationType();
        }

        public String getAnnotationType() {
            return type;
        }

        public int getOffset() {
            return position.getOffset();
        }

        public int getLine() {
            try {
                return Utilities.getLineOffset(doc, getOffset());
            } catch (BadLocationException e) {
                IllegalStateException exc = new IllegalStateException();
                
                exc.initCause(e);
                
                throw exc;
            }
        }
        
    }
    
    static final class AnnotationsLoader implements AnnotationTypes.Loader {

        public void loadTypes() {
            try {
                Map typesInstances = new HashMap();
                FileObject typesFolder = FileUtil.getConfigFile("Editors/AnnotationTypes");
                FileObject[] types = typesFolder.getChildren();
                
                for (int cntr = 0; cntr < types.length; cntr++) {
                    AnnotationTypeProcessor proc = new AnnotationTypeProcessor();
                    
                    System.err.println("CCC:" + types[cntr].getNameExt());
                    if (types[cntr].getName().startsWith("testAnnotation") && "xml".equals(types[cntr].getExt())) {
                        proc.attachTo(types[cntr]);
                        AnnotationType type = (AnnotationType) proc.instanceCreate();
                        typesInstances.put(type.getName(), type);
                    }
                }
                
                AnnotationTypes.getTypes().setTypes(typesInstances);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void loadSettings() {
        }

        public void saveType(AnnotationType type) {
        }

        public void saveSetting(String settingName, Object value) {
        }

    }
    
}
