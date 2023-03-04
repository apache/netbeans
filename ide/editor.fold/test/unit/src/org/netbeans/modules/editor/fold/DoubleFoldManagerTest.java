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
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;

/**
 * Test adding fold and remove overlap when newly added fold has higher priority
 * so it overwrites existing folds.
 *
 * @author Marek Slama
 */
public class DoubleFoldManagerTest extends NbTestCase {
    
    public DoubleFoldManagerTest(String testName) {
        super(testName);
    }
    
    /**
     * Test the creation of several folds.
     */
    public void test() throws BadLocationException {
        FoldHierarchyTestEnv env = 
                new FoldHierarchyTestEnv(new FoldManagerFactory[] {new Simple1FoldManagerFactory(), new Simple2FoldManagerFactory()});
        AbstractDocument doc = env.getDocument();
        doc.insertString(0, "1234567890", null);
        doc.insertString(0, "1234567890", null);
        doc.insertString(0, "1234567890", null);
        doc.insertString(0, "1234567890", null);
        doc.insertString(0, "1234567890", null);
        doc.insertString(0, "1234567890", null);
        doc.insertString(0, "1234567890", null);
        FoldHierarchy hierarchy = env.getHierarchy();

        Simple1FoldManager man1 = Simple1FoldManager.getLast();
        Simple2FoldManager man2 = Simple2FoldManager.getLast();
        
        doc.readLock();
        try {
            hierarchy.lock();
            try {
                FoldHierarchyTransaction transaction = man2.getOperation().openTransaction();
                try {
                    Fold fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        1, 60, 1, 1, null, transaction
                    );
                    fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        3, 25, 1, 1, null, transaction
                    );
                    fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        28, 55, 1, 1, null, transaction
                    );
                    fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        31, 50, 1, 1, null, transaction
                    );
                    /*fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        33, 40, 1, 1, null, transaction
                    );*/
                    fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        33, 35, 1, 1, null, transaction
                    );
                    fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        37, 40, 1, 1, null, transaction
                    );
                    /*fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        12, 15, 1, 1, null, transaction
                    );*/
                    /*fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        31, 45, 1, 1, null, transaction
                    );*/
                    /*fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        11, 15, 1, 1, null, transaction
                    );
                    fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        12, 14, 1, 1, null, transaction
                    );
                    fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        16, 20, 1, 1, null, transaction
                    );
                    fold = man2.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        17, 19, 1, 1, null, transaction
                    );*/
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    fail();
                } finally {
                    transaction.commit();
                }
                //ERROR This fails !!!!!
                /*transaction = man1.getOperation().openTransaction();
                try {
                    Fold fold = man1.getOperation().addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        38, 70, 1, 1, null, transaction
                    );

                    assertTrue(man1.getOperation().owns(fold));

                } catch (BadLocationException e) {
                    e.printStackTrace();
                    fail();
                } finally {
                    transaction.commit();
                }*/
                
                Fold rootFold = hierarchy.getRootFold();
                int foldCount = rootFold.getFoldCount();

                /*Fold fold = rootFold.getFold(0);
                System.out.println("test fold:" + fold);
                FoldType foldType = fold.getType();
                int foldStartOffset = fold.getStartOffset();
                int foldEndOffset = fold.getEndOffset();
                assertTrue("Incorrect fold type " + foldType, // NOI18N
                    (foldType == AbstractFoldManager.REGULAR_FOLD_TYPE));
                assertTrue("Incorrect fold start offset " + foldStartOffset, // NOI18N
                    (foldStartOffset == FOLD_START_OFFSET_1));
                assertTrue("Incorrect fold end offset " + foldEndOffset, // NOI18N
                    (foldEndOffset == FOLD_END_OFFSET_1));*/
                
            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    
    static final class Simple1FoldManager extends AbstractFoldManager {
        private static Simple1FoldManager manager;

        public Simple1FoldManager () {
            manager = this;
        }

        public static Simple1FoldManager getLast () {
            return manager;
        }

        @Override
        public FoldOperation getOperation() {
            return super.getOperation();
        }
        
        @Override
        public void initFolds(FoldHierarchyTransaction transaction) {
            //System.out.println("-- Simple1FoldManager.initFolds ENTER");
            /*try {
                Fold fold = getOperation().addToHierarchy(
                    REGULAR_FOLD_TYPE,
                    "...", // non-null to properly count fold's size (non-null desc gets set) // NOI18N
                    false,
                    FOLD_START_OFFSET_1, FOLD_END_OFFSET_1, 1, 1,
                    null,
                    transaction
                );

                assertTrue(getOperation().owns(fold));

            } catch (BadLocationException e) {
                e.printStackTrace();
                fail();
            }*/
        }
        
    }

    public final class Simple1FoldManagerFactory implements FoldManagerFactory {
        
        public FoldManager createFoldManager() {
            return new Simple1FoldManager();
        }
        
    }
    
    static final class Simple2FoldManager extends AbstractFoldManager {
        private static Simple2FoldManager manager;
        
        public Simple2FoldManager () {
            manager = this;
        }
        
        public static Simple2FoldManager getLast () {
            return manager;
        }
        
        @Override
        public FoldOperation getOperation() {
            return super.getOperation();
        }
        
        @Override
        public void initFolds(FoldHierarchyTransaction transaction) {
            //System.out.println("-- Simple2FoldManager.initFolds ENTER");
            /*try {
                Fold fold = getOperation().addToHierarchy(
                    REGULAR_FOLD_TYPE,
                    "...", // non-null to properly count fold's size (non-null desc gets set) // NOI18N
                    false,
                    FOLD_START_OFFSET_2, FOLD_END_OFFSET_2, 1, 1,
                    null,
                    transaction
                );

                assertTrue(getOperation().owns(fold));

            } catch (BadLocationException e) {
                e.printStackTrace();
                fail();
            }*/
        }

    }

    public final class Simple2FoldManagerFactory implements FoldManagerFactory {

        public FoldManager createFoldManager() {
            return new Simple2FoldManager();
        }

    }

    private final class FoldMemoryFilter implements MemoryFilter {
        
        private Fold fold;
        
        FoldMemoryFilter(Fold fold) {
            this.fold = fold;
        }
        
        public boolean reject(Object o) {
            return (o == fold.getType())
                || (o == fold.getDescription()) // requires non-null description during construction
                || (o == fold.getParent())
                || (o instanceof FoldOperationImpl)
                || (o instanceof Position);
            
            // Will count possible FoldChildren and ExtraInfo
        }

    }

}
