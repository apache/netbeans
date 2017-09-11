/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.fold;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;

/**
 *
 * @author sdedic
 */
public class FoldOperationTest extends NbTestCase {

    public FoldOperationTest(String name) {
        super(name);
    }

    /*
     * update* tests use a simple fold manager, which defines several folds, some of them nested.
     * A[0,1]
     * B[2,7]
     *  C[3,4]
     *  D[5,6]
     * E[8,9]
     */
    
    private static int[][] foldRanges = new int[][] {
        {0, 1},
        {3,12},
            {5, 6},
            {9, 10},
        {14, 16}
    };
    
    /**
     * Checks that no changes (update with the same infos) will not fire events, remove or add any folds.
     * 
     * @throws Exception 
     */
    public void testUpdateNoChanges() throws Exception {
        final TestFoldManager[] mgr = new TestFoldManager[1];
        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new FoldManagerFactory() {
            @Override
            public FoldManager createFoldManager() {
                return mgr[0] = new TestFoldManager();
            }
        });
        AbstractDocument doc = env.getDocument();
        doc.insertString(0, "12345678901234567890", null);

        FoldHierarchy hierarchy = env.getHierarchy();
        
        Collection<FoldInfo> infos = new ArrayList<FoldInfo>(10);
        infos.add(FoldInfo.range(0, 1, FoldType.MEMBER));
        infos.add(FoldInfo.range(9, 10, FoldType.MEMBER));
        infos.add(FoldInfo.range(14, 16, FoldType.MEMBER));
        // not sorted, check :)
        infos.add(FoldInfo.range(3, 12, FoldType.MEMBER));
        infos.add(FoldInfo.range(5, 6, FoldType.MEMBER));
        
        doc.readLock();
        try {
            hierarchy.lock();
            TestFoldManager m = mgr[0];
            try {
                Collection<Fold> remove = new ArrayList<Fold>();
                Collection<FoldInfo> create = new ArrayList<FoldInfo>();
                
                final boolean[] changed = new boolean[1];
                
                hierarchy.addFoldHierarchyListener(new FoldHierarchyListener() {

                    @Override
                    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
                        changed[0] = true;
                    }
                });
                
                Map<FoldInfo, Fold> mapping = m.operation.update(infos, remove, create);
                
                assertEquals(m.initFolds.size(), mapping.size());
                assertTrue(m.initFolds.containsAll(mapping.values()));
                assertFalse(changed[0]);
                
            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    private List<FoldInfo> createDefaultInfos() {
        List<FoldInfo> infos = new ArrayList<FoldInfo>(10);
        infos.add(FoldInfo.range(0, 1, FoldType.MEMBER));
        infos.add(FoldInfo.range(3, 12, FoldType.MEMBER));
        infos.add(FoldInfo.range(5, 6, FoldType.MEMBER));
        infos.add(FoldInfo.range(9, 10, FoldType.MEMBER));
        infos.add(FoldInfo.range(14, 16, FoldType.MEMBER));
        return infos;
    }
    
    /**
     * Checks that folds are created beween two folds, encapsulating existing folds.
     * 
     * @throws Exception 
     */
    public void testUpdateCreateFold() throws Exception {
        final TestFoldManager[] mgr = new TestFoldManager[1];
        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new FoldManagerFactory() {
            @Override
            public FoldManager createFoldManager() {
                return mgr[0] = new TestFoldManager();
            }
        });
        AbstractDocument doc = env.getDocument();
        doc.insertString(0, "12345678901234567890", null);

        FoldHierarchy hierarchy = env.getHierarchy();
        
        List<FoldInfo> infos = createDefaultInfos();
        
        // add a new fold between #1 and #2
        infos.add(FoldInfo.range(2, 3, FoldType.MEMBER));
        
        // add a new fold at the end:
        infos.add(FoldInfo.range(19,20, FoldType.MEMBER));
        
        // add a fold, which encapsulates #2 - #5
        infos.add(FoldInfo.range(3, 18, FoldType.MEMBER));
        
        // add a fold, which encapsulates ##5
        infos.add(FoldInfo.range(13, 16, FoldType.MEMBER));
        
        doc.readLock();
        try {
            hierarchy.lock();
            TestFoldManager m = mgr[0];
            try {
                Collection<Fold> remove = new ArrayList<Fold>();
                Collection<FoldInfo> create = new ArrayList<FoldInfo>();
                
                final boolean[] changed = new boolean[1];
                
                hierarchy.addFoldHierarchyListener(new FoldHierarchyListener() {
                    @Override
                    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
                        changed[0] = true;
                    }
                });
                Map<FoldInfo, Fold> mapping = m.operation.update(infos, remove, create);
                
                // 3 folds added, no deleted:
                assertEquals(4, create.size());
                assertEquals(0, remove.size());
                
            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    /**
     * Checks that updated folds can SHIFT. If a fold should be replaced by a same-type fold, which 
     * fully contains the original one, or is fully contained by the original one, the original fold will
     * be updated rather than removed/created. This behaviour eliminates issues with collapsing imports.
     * 
     * @throws Exception 
     */
    public void testUpdateShiftFolds() throws Exception {
        final TestFoldManager[] mgr = new TestFoldManager[1];
        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new FoldManagerFactory() {
            @Override
            public FoldManager createFoldManager() {
                return mgr[0] = new TestFoldManager();
            }
        });
        AbstractDocument doc = env.getDocument();
        doc.insertString(0, "12345678901234567890", null);

        FoldHierarchy hierarchy = env.getHierarchy();
        
        List<FoldInfo> infos = createDefaultInfos();
        
        // Fold #3 is not shifted, as the new fold does not intersect with the old one. Fold will be replaced.
        FoldInfo cInfo;
        infos.add(cInfo = FoldInfo.range(4, 5, FoldType.MEMBER));
        
        // Fold #4 is extended backwards
        infos.add(FoldInfo.range(8, 10, FoldType.MEMBER));
        
        // Fold #5 is extended in both directions
        infos.add(FoldInfo.range(13, 17, FoldType.MEMBER));
        
        infos.remove(4); infos.remove(3); infos.remove(2);
        
        TestFoldManager m = mgr[0];
        final Fold fold8 = FoldUtilities.findNearestFold(hierarchy, 8);
        final Fold fold13 = FoldUtilities.findNearestFold(hierarchy, 13);
        
        class FHL implements FoldHierarchyListener {
            boolean changed;
            
            @Override
            public void foldHierarchyChanged(FoldHierarchyEvent evt) {
                changed = true;
                assertEquals(2, evt.getFoldStateChangeCount());
                for (int i = evt.getFoldStateChangeCount() - 1; i >= 0; i--) {
                    FoldStateChange chg = evt.getFoldStateChange(i);
                    if (chg.getFold() == fold8) {
                        assertEquals(9, chg.getOriginalStartOffset());
                        assertEquals(-1, chg.getOriginalEndOffset());
                    } else if (chg.getFold() == fold13) {
                        assertEquals(14, chg.getOriginalStartOffset());
                        assertEquals(16, chg.getOriginalEndOffset());
                    } else {
                        fail("Unexpected change");
                    }
                }
            }
        }
        
        FHL fhl = new FHL();
        doc.readLock();
        try {
            hierarchy.lock();
            try {
                Collection<Fold> remove = new ArrayList<Fold>();
                Collection<FoldInfo> create = new ArrayList<FoldInfo>();
                
                hierarchy.addFoldHierarchyListener(fhl);
                Map<FoldInfo, Fold> mapping = m.operation.update(infos, remove, create);
                
                // 3 folds added, no deleted:
                assertEquals(1, create.size());
                assertEquals(1, remove.size());
                
                assertSame(cInfo, create.iterator().next());
                
                Fold f = remove.iterator().next();
                // old fold
                assertEquals(5, f.getStartOffset());
                
                // new fold
                FoldInfo newInfo = create.iterator().next();
                assertSame(cInfo, newInfo);
                f = mapping.get(cInfo);
                assertEquals(4, f.getStartOffset());
            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    /**
     * Checks that a released operation will not update anything
     * 
     * @throws Exception 
     */
    public void testNoUpdateAfterRelease() throws Exception {
        final TestFoldManager[] mgr = new TestFoldManager[1];
        final FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new FoldManagerFactory() {
            @Override
            public FoldManager createFoldManager() {
                return mgr[0] = new TestFoldManager();
            }
        });
        AbstractDocument doc = env.getDocument();
        doc.insertString(0, "12345678901234567890", null);

        FoldHierarchy hierarchy = env.getHierarchy();
        List<FoldInfo> infos = createDefaultInfos();
        
        // add a new fold between #1 and #2
        infos.add(FoldInfo.range(2, 3, FoldType.MEMBER));
        
        // add a new fold at the end:
        infos.add(FoldInfo.range(19,20, FoldType.MEMBER));
        
        // add a fold, which encapsulates #2 - #5
        infos.add(FoldInfo.range(3, 18, FoldType.MEMBER));
        
        infos.remove(4); infos.remove(3); infos.remove(2);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JPanel outer = new JPanel();
                // force parent change 
                outer.add(env.getPane());
                env.getPane().setEditorKit(null);
            }
        });
        // listener is attached in between these events
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                // nothing, just wait for the delayed events to process.
            }
        });
        FoldHierarchyExecution.waitAllTasks();
        
        doc.readLock();
        try {
            hierarchy.lock();
            TestFoldManager m = mgr[0];
            try {
                
                Collection<Fold> remove = new ArrayList<Fold>();
                Collection<FoldInfo> create = new ArrayList<FoldInfo>();
                final boolean[] changed = new boolean[1];
                
                hierarchy.addFoldHierarchyListener(new FoldHierarchyListener() {
                    @Override
                    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
                        changed[0] = true;
                    }
                });
                Map<FoldInfo, Fold> mapping = m.operation.update(infos, remove, create);
                assertNull(mapping);
                // 3 folds added, no deleted:
                assertEquals(0, create.size());
                assertEquals(0, remove.size());
                assertFalse(changed[0]);
                
            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    
    /**
     * Checks that foldIterator() enumerates blocked folds, and blocked folds blocked by blocked folds
     * 
     * @throws Exception 
     */
    public void testFoldIterator() throws Exception {
        TestFoldManager baseFoldManager = new TestFoldManager(BASE_RANGE, FoldType.MEMBER);
        TestFoldManager overrideFoldManager = new TestFoldManager(OVERRIDE_RANGE, FoldType.NESTED);
        TestFoldManager uberFoldManager = new TestFoldManager(UBER_RANGE, FoldType.USER);
        
        class FMF implements FoldManagerFactory {
            private FoldManager fm;

            public FMF(FoldManager fm) {
                this.fm = fm;
            }

            @Override
            public FoldManager createFoldManager() {
                return fm;
            }
        }
        
        final FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(
                new FMF(uberFoldManager), new FMF(overrideFoldManager), new FMF(baseFoldManager)
        );
        AbstractDocument doc = env.getDocument();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("12345678901234567890");
        }
        doc.insertString(0, sb.toString(), null);
        
        FoldHierarchy hierarchy = env.getHierarchy();
        hierarchy.lock();
        // now update the uber-range, so that the new fold blocks the blocking fold:
        FoldOperation fo = uberFoldManager.operation;
        FoldHierarchyTransaction t = fo.openTransaction();
        fo.addToHierarchy(FoldType.USER, 110, 170, null, null, null, null, t);
        t.commit();
        
        // check that the hierarchy contains the proper overrides:
        Fold f = FoldUtilities.findOffsetFold(hierarchy, 2);
        assertNotNull(f);
        assertSame(FoldType.NESTED, f.getType());
        
        f = FoldUtilities.findOffsetFold(hierarchy, 122);
        assertNotNull(f);
        assertSame(FoldType.USER, f.getType());
        
        // enumerate all folds of the base iterator
        fo = baseFoldManager.operation;
        
        Iterator<Fold> folds = fo.foldIterator();
        for (int i = 0; i < BASE_RANGE.length; i++) {
            int[] rng = BASE_RANGE[i];
            f = folds.next();
            
            assertSame(FoldType.MEMBER, f.getType());
            assertEquals(rng[0], f.getStartOffset());
            assertEquals(rng[1], f.getEndOffset());
        }
        assertFalse(folds.hasNext());
    }
    
    //                                                 index          type       status prio         from    to
    private Pattern foldPattern = Pattern.compile("^\\s*(?:\\[\\d+\\]:)?\\s+\\[(\\S+)\\]\\s+([CE])(\\d)\\s+\\<(\\d+),(\\d+)\\>");
    
    private static final Map<String, FoldType> FOLD_TYPES = new HashMap<String, FoldType>();
    
    private static final FoldType FOLD_FUNCTION = FoldType.MEMBER.derive("function", "Function", FoldTemplate.DEFAULT);
    
    static {
        FOLD_TYPES.put(FoldType.CODE_BLOCK.code(), FoldType.CODE_BLOCK);
        FOLD_TYPES.put(FoldType.COMMENT.code(), FoldType.COMMENT);
        FOLD_TYPES.put(FoldType.DOCUMENTATION.code(), FoldType.DOCUMENTATION);
        FOLD_TYPES.put(FoldType.IMPORT.code(), FoldType.IMPORT);
        FOLD_TYPES.put(FoldType.INITIAL_COMMENT.code(), FoldType.INITIAL_COMMENT);
        FOLD_TYPES.put(FoldType.MEMBER.code(), FoldType.MEMBER);
        FOLD_TYPES.put(FoldType.NESTED.code(), FoldType.NESTED);
        FOLD_TYPES.put(FoldType.TAG.code(), FoldType.TAG);
        FOLD_TYPES.put(FOLD_FUNCTION.code(), FOLD_FUNCTION);
    }
    
    private int highestOffset = -1;
    
    private List<FoldInfo> readFoldData(String pathName) throws Exception {
        List<FoldInfo> result = new ArrayList<FoldInfo>();
        File f = new File(getDataDir(), pathName.replaceAll("/", Matcher.quoteReplacement(File.separator)));
        if (!f.exists()) {
            throw new IllegalArgumentException("Data file not found: " + pathName);
        }
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        
        while ((line = reader.readLine()) != null) {
            Matcher m = foldPattern.matcher(line);
            if (!m.find()) {
                continue;
            }
            
            String type = m.group(1);
            int from = Integer.parseInt(m.group(4));
            int to = Integer.parseInt(m.group(5));
            
            highestOffset = Math.max(highestOffset, to);
            if (FOLD_TYPES.get(type) == null) {
                System.err.println("invalid type: type");
            }
            FoldInfo fi = FoldInfo.range(from, to, FOLD_TYPES.get(type));
            result.add(fi);
        }
        reader.close();
        
        return result;
    }
    
    // this is only partly written, waiting for more info from issue #233455
    public void testNestedFolds() throws Exception {
        List<FoldInfo> infos = readFoldData("hierarchy/update-hierarchy.folds");
//        Collections.reverse(infos);
        class FMF implements FoldManagerFactory {
            private FoldManager fm;

            public FMF(FoldManager fm) {
                this.fm = fm;
            }

            @Override
            public FoldManager createFoldManager() {
                return fm;
            }
        }
        
        TestFoldManager baseFoldManager = new TestFoldManager(new int[][]{}, FoldType.MEMBER);
        final FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(
                new FMF(baseFoldManager) 
        );
        AbstractDocument doc = env.getDocument();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; sb.length() <= highestOffset; i++) {
            sb.append("12345678901234567890");
        }
        doc.insertString(0, sb.toString(), null);
        
        FoldHierarchy hierarchy = env.getHierarchy();
        hierarchy.lock();
        // now update the uber-range, so that the new fold blocks the blocking fold:
        FoldOperation fo = baseFoldManager.operation;
        fo.update(infos, null, null);
        hierarchy.unlock();
        
        String dump = hierarchy.toString();
        dump = dump.replaceAll("=\\d\\S+", "").replaceAll("@\\S+", "");
    }
    
    private static int[][] BASE_RANGE = {
        { 2, 4 },
        { 7, 100},
            { 10, 30 },
            { 40, 50 },
        { 120, 200 },
            { 130, 140 },
            { 150, 160 }
    };

    private static int[][] OVERRIDE_RANGE = {
        { 1, 3 },   
        { 7, 100},  
            { 8, 20 }, // intersect at the beginning
            { 45, 60 }, // intersect at the end
        { 120, 200 },   // block
            { 125, 142 }, // fully contain
            { 155, 157 }  // proper child
    };
    
    private static int[][] UBER_RANGE = {
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
                    initFolds.add(operation.addToHierarchy(
                            type, 
                            range[0], 
                            range[1], 
                            null, 
                            null, 
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
        }

        @Override
        public void removeDamagedNotify(Fold damagedFold) {
        }

        @Override
        public void expandNotify(Fold expandedFold) {
        }

        @Override
        public void release() {
            // purposely ignore
        }
    }   
}
