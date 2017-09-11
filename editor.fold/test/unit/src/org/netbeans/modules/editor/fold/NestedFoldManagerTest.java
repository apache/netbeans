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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
