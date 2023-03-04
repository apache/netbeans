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

package org.netbeans.modules.editor.fold;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import junit.framework.TestCase;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

/**
 *
 * @author mmetelka
 */
public class NestedFoldManagerTest extends TestCase {
    
    static final int FOLD_START_OFFSET_OUTER = 5;
    static final int FOLD_END_OFFSET_OUTER = 10;
    static final int FOLD_START_OFFSET_INNER = 6;
    static final int FOLD_END_OFFSET_INNER = 8;
    
    public NestedFoldManagerTest(String testName) {
        super(testName);
    }
    
    public void test() throws BadLocationException {
        test(true);
        test(false);
    }

    /**
     * Test the creation of several folds.
     */
    public void test(boolean outerFirst) throws BadLocationException {
        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new NestedFoldManagerFactory(outerFirst));
        AbstractDocument doc = env.getDocument();
        doc.insertString(0, "1234567890", null);

        FoldHierarchy hierarchy = env.getHierarchy();
        doc.readLock();
        try {
            hierarchy.lock();
            try {
                Fold rootFold = hierarchy.getRootFold();
                int foldCount = rootFold.getFoldCount();
                int expectedFoldCount = 1;
                assertTrue("Incorrect fold count " + foldCount, // NOI18N
                    (foldCount == expectedFoldCount)
                );
                
                Fold fold = rootFold.getFold(0);
                FoldType foldType = fold.getType();
                int foldStartOffset = fold.getStartOffset();
                int foldEndOffset = fold.getEndOffset();
                assertTrue("Incorrect fold type " + foldType, // NOI18N
                    (foldType == AbstractFoldManager.REGULAR_FOLD_TYPE));
                assertTrue("Incorrect fold start offset " + foldStartOffset, // NOI18N
                    (foldStartOffset == FOLD_START_OFFSET_OUTER));
                assertTrue("Incorrect fold end offset " + foldEndOffset, // NOI18N
                    (foldEndOffset == FOLD_END_OFFSET_OUTER));
                
                // Test inner fold
                Fold outerFold = fold;
                foldCount = outerFold.getFoldCount();
                expectedFoldCount = 1;
                assertTrue("Incorrect fold count " + foldCount, // NOI18N
                    (foldCount == expectedFoldCount)
                );
                
                fold = outerFold.getFold(0);
                assertTrue("Folds must differ", (fold != outerFold)); // NOI18N
                foldType = fold.getType();
                foldStartOffset = fold.getStartOffset();
                foldEndOffset = fold.getEndOffset();
                assertTrue("Incorrect fold type " + foldType, // NOI18N
                    (foldType == AbstractFoldManager.REGULAR_FOLD_TYPE));
                assertTrue("Incorrect fold start offset " + foldStartOffset, // NOI18N
                    (foldStartOffset == FOLD_START_OFFSET_INNER));
                assertTrue("Incorrect fold end offset " + foldEndOffset, // NOI18N
                    (foldEndOffset == FOLD_END_OFFSET_INNER));

            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    
    final class NestedFoldManager extends AbstractFoldManager {
        
        private boolean outerFirst;
        
        NestedFoldManager(boolean outerFirst) {
            this.outerFirst = outerFirst;
        }
        
        private void addOuter(FoldHierarchyTransaction transaction) throws BadLocationException {
            getOperation().addToHierarchy(
                REGULAR_FOLD_TYPE,
                null,
                false,
                FOLD_START_OFFSET_OUTER, FOLD_END_OFFSET_OUTER, 1, 1,
                null,
                transaction
            );
        }            
        
        private void addInner(FoldHierarchyTransaction transaction) throws BadLocationException{
            getOperation().addToHierarchy(
                REGULAR_FOLD_TYPE,
                null,
                false,
                FOLD_START_OFFSET_INNER, FOLD_END_OFFSET_INNER, 1, 1,
                null,
                transaction
            );
        }            
        
        public void initFolds(FoldHierarchyTransaction transaction) {
            try {
                if (outerFirst) {
                    addOuter(transaction);
                }
                addInner(transaction);
                if (!outerFirst) {
                    addOuter(transaction);
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
                fail();
            }
        }
        
    }

    public final class NestedFoldManagerFactory implements FoldManagerFactory {
        
        private boolean outerFirst;
        
        NestedFoldManagerFactory(boolean outerFirst) {
            this.outerFirst = outerFirst;
        }
        
        public FoldManager createFoldManager() {
            return new NestedFoldManager(outerFirst);
        }
        
    }
    
}
