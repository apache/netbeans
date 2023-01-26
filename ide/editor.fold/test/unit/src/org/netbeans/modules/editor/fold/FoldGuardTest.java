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
package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;

/**
 * This testcase asserts some behaviour of the fold's guarded area before refactoring the implementation.
 * The old implementation used Position to mark 'guarded' area of the fold's interior, and determined 'damaged'
 * status based on that Position's distance to start/end of the fold. The new implementation just computes
 * effect of insertUpdate or removeUpdate to the folds, does not need Position data.
 *
 * @author sdedic
 */
public class FoldGuardTest extends NbTestCase implements FoldHierarchyListener {

    public FoldGuardTest(String name) {
        super(name);
    }
    
    private FoldHierarchy hierarchy;
    
    private TestFoldManager manager;
    
    private AbstractDocument doc;
    
    private Fold foldEmpty;
    
    private Fold foldDamaged;
    
    private Fold foldExpanded;
    
    private Fold changedFold;
    
    private Fold removedFold;
    
    private Fold fold35;
    
    private Fold fold90;
    
    private Fold fold102;
    
    public void setUp() throws Exception {
        super.setUp();
        manager = new TestFoldManager();
        
        class FMF implements FoldManagerFactory {
            @Override
            public FoldManager createFoldManager() {
                return manager;
            }
        }

        final FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(
                new FMF()
        );
        doc = env.getDocument();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("12345678901234567890");
        }
        doc.insertString(0, sb.toString(), null);
        
        hierarchy = env.getHierarchy();
        fold35 = FoldUtilities.findOffsetFold(hierarchy, 35);
        fold90 = FoldUtilities.findOffsetFold(hierarchy, 90);
        fold102 = FoldUtilities.findOffsetFold(hierarchy, 102);
        hierarchy.collapse(fold35);
        hierarchy.collapse(fold90);

        hierarchy.addFoldHierarchyListener(this);
    }

    @Override
    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        assertEquals(0, evt.getAddedFoldCount());
        if (evt.getRemovedFoldCount() == 1) {
            this.removedFold = evt.getRemovedFold(0);
        }
        if (evt.getFoldStateChangeCount() == 1) {
            this.changedFold = evt.getFoldStateChange(0).getFold();
        }
    }
    
    
    /**
     * Checks deletion of content before the fold, and part of the start guarded area
     * @throws Exception 
     */
    public void testDeleteBeforeAndStart() throws Exception {
        doc.remove(25, 9);
        assertSame(fold35, foldDamaged);
        assertSame(fold35, removedFold);
        assertNull(changedFold);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertTrue(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }

    /**
     * Content that fully contains the fold is deleted.
     */
    public void testDeleteWholeFold() throws Exception {
        doc.remove(25, (75 - 25));

        assertNull(foldDamaged);
        assertSame(fold35, removedFold);
        assertNull(changedFold);
        assertSame(fold35, foldEmpty);
        assertNull(foldExpanded);

        assertTrue(manager.operation.isStartDamaged(fold35));
        assertTrue(manager.operation.isEndDamaged(fold35));
}
    
    /**
     * Part of the start guarded area + part of interior is deleted. The fold will be damaged
     */
    public void testDeleteStartAndInterior() throws Exception {
        doc.remove(32, 6);
        
        assertSame(fold35, foldDamaged);
        assertSame(fold35, removedFold);
        
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        assertNull(changedFold);
        
        assertTrue(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }

    /**
     * Part of the end guarded area + part of interior is deleted
     */
    public void testDeleteEndAndInterior() throws Exception {
        doc.remove(55, 8);

        assertSame(fold35, foldDamaged);
        assertSame(fold35, removedFold);
        
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        assertNull(changedFold);

        assertFalse(manager.operation.isStartDamaged(fold35));
        assertTrue(manager.operation.isEndDamaged(fold35));
    }
    
    public void testDeleteStartInteriorEnd() throws Exception {
        doc.remove(32, (58 - 32));
        
        assertSame(fold35, foldDamaged);
        assertSame(fold35, removedFold);
        
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        assertNull(changedFold);

        assertTrue(manager.operation.isStartDamaged(fold35));
        assertTrue(manager.operation.isEndDamaged(fold35));
    }
    
    public void testDeleteWholeInterior() throws Exception {
        doc.remove(35, (55 - 35));
        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertNull(removedFold);
        assertNull(changedFold);

        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }
    
    public void testRemoveExactFold() throws Exception {
        doc.remove(30, 30);

        assertNull(foldDamaged);
        assertSame(fold35, removedFold);
        assertNull(changedFold);
        assertSame(fold35, foldEmpty);
        assertNull(foldExpanded);

        assertTrue(manager.operation.isStartDamaged(fold35));
        assertTrue(manager.operation.isEndDamaged(fold35));
    }
    
    public void testInsertBeforeStart() throws Exception {
        doc.insertString(25, "ahoj", null);

        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertNull(removedFold);
        assertNull(changedFold);
        
        assertEquals(34, fold35.getStartOffset());
        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }
    
    public void testInsertAtStart() throws Exception {
        doc.insertString(30, "ahoj", null);

        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertNull(removedFold);
        assertNull(changedFold);
        
        assertEquals(34, fold35.getStartOffset());
        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }

    public void testInsertInStartArea() throws Exception {
        doc.insertString(31, "ahoj", null);

        assertSame(fold35, foldDamaged);
        assertSame(fold35, removedFold);
        
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertNull(changedFold);
        assertTrue(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }
    
    public void testInsertAtInterior() throws Exception {
        doc.insertString(35, "ahoj", null);
        
        assertNull(foldDamaged);
        assertNull(removedFold);
        
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertNull(changedFold);
        
        assertEquals(35, fold35.getGuardedStart());
        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }
    
    public void testInsertIntoEndArea() throws Exception {
        doc.insertString(56, "ahoj", null);

        assertSame(fold35, foldDamaged);
        assertSame(fold35, removedFold);
        
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        assertFalse(manager.operation.isStartDamaged(fold35));
        assertTrue(manager.operation.isEndDamaged(fold35));
    }
    
    public void testInsertJustBeforeGuardedEnd() throws Exception {
        doc.insertString(55, "ahoj", null);

        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertNull(changedFold);
        assertNull(removedFold);
        
        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }
    
    public void testInsertJustBeforeEnd() throws Exception {
        doc.insertString(60, "ahoj", null);

        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertSame(fold35, changedFold);
        assertNull(removedFold);
        
    }
    public void testDeleteInBareFold() throws Exception {
        doc.remove(95, 4);

        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertNull(changedFold);
        assertNull(removedFold);

        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }
    
    public void testDeleteBareSuffix() throws Exception {
        doc.remove(95, 5);

        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertSame(fold90, changedFold);
        assertNull(removedFold);

        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }
    
    public void testDeleteBareSuffixAndBeyond() throws Exception {
        doc.remove(95, 8);

        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertSame(fold90, changedFold);
        assertNull(removedFold);

        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }
    
    public void testDeleteBareSuffixAndBeyondIncludingNext() throws Exception {
        doc.remove(95, 110 - 95);

        assertNull(foldDamaged);
        assertSame(fold102, foldEmpty);
        assertNull(foldExpanded);
        
        assertSame(fold90, changedFold);
        assertSame(fold102, removedFold);
    }

    public void testDeleteBarePrefix() throws Exception {
        doc.remove(90, 5);

        assertNull(foldDamaged);
        assertNull(foldEmpty);
        assertNull(foldExpanded);
        
        assertSame(fold90, changedFold);
        assertNull(removedFold);

        assertFalse(manager.operation.isStartDamaged(fold35));
        assertFalse(manager.operation.isEndDamaged(fold35));
    }

    private static final int[][] foldRanges = {
        { 10, 5, 150, 5 },
            { 30, 5, 60, 5 },
            { 90, 0, 100, 0 },
            { 102, 0, 105, 0 },
    };
    
    private class TestFoldManager implements FoldManager {
        FoldOperation operation;
        Collection<Fold> initFolds = new ArrayList<Fold>(6);
        int[][] ranges;
        FoldType type;
        
        public TestFoldManager() {
            ranges = foldRanges;
            type = FoldType.MEMBER;
        }
        
        public TestFoldManager(int[][] ranges, FoldType type) {
            this.ranges = ranges;
            this.type = type;
        }
        
        
        @Override
        public void init(FoldOperation operation) {
            this.operation = operation;
        }

        public void initFolds(FoldHierarchyTransaction transaction)  {
            for (int i = 0; i < ranges.length; i++) {
                int[] range = ranges[i];
                try {
                    FoldTemplate templ = new FoldTemplate(range[1], range[3], type.getTemplate().getDescription());
                    initFolds.add(operation.addToHierarchy(
                            type, 
                            range[0], 
                            range[2], 
                            null, 
                            templ, 
                            null, 
                            null, transaction
                    ));
                } catch (BadLocationException ex) {
                }
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
            foldEmpty = epmtyFold;
        }

        @Override
        public void removeDamagedNotify(Fold damagedFold) {
            foldDamaged = damagedFold;
        }

        @Override
        public void expandNotify(Fold expandedFold) {
            foldExpanded = expandedFold;
        }

        @Override
        public void release() {
            // purposely ignore
        }
    }   
}
