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

import java.util.Collections;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.fold.FoldHierarchyMonitor;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

/**
 *
 * @author mmetelka
 */
public class SimpleFoldManagerTest extends NbTestCase {
    
    private static final int MAX_FOLD_MEMORY_SIZE = 64;
    
    static final int FOLD_START_OFFSET_1 = 5;
    static final int FOLD_END_OFFSET_1 = 10;
    
    public SimpleFoldManagerTest(String testName) {
        super(testName);
    }
    
    /**
     * Test the creation of several folds.
     */
    public void test() throws Exception {
        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new SimpleFoldManagerFactory());
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
                    (foldStartOffset == FOLD_START_OFFSET_1));
                assertTrue("Incorrect fold end offset " + foldEndOffset, // NOI18N
                    (foldEndOffset == FOLD_END_OFFSET_1));
                
                // Check fold size
                assertSize("Size of the fold " , Collections.singleton(fold), // NOI18N
                    MAX_FOLD_MEMORY_SIZE, new FoldMemoryFilter(fold));
                
            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    /**
     * Checks that FoldHierarchyMonitor is pinged during hierarchy creation.
     * No provider is registered, so the hierarchy should report active == false.
     */
    public void testFoldHierarchyMonitorNoProviders() throws Exception {
        MockMimeLookup.setInstances(MimePath.parse(""), new FHM());

        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new FoldManagerFactory[0]);
        
        env.getHierarchy();
        
        assertTrue("Folds not attached", attached);
        assertFalse("Unexpected provider", active);
    }
    
    private boolean attached;
    private boolean active;
    
    private class FHM implements FoldHierarchyMonitor {

        @Override
        public void foldsAttached(FoldHierarchy h) {
            attached = true;
            active = h.isActive();
        }
    }
    
    /**
     * A provider is registered for the hierarchy. 
     * Active == true should be reported to FoldHierarchyMonitor.
     */
    public void testFoldHierararchyMonitorWithProvider() throws Exception {
        MockMimeLookup.setInstances(MimePath.parse(""), new FHM());

        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new SimpleFoldManagerFactory());
        AbstractDocument doc = env.getDocument();
        doc.insertString(0, "1234567890", null);
        
        env.getHierarchy();
        
        assertTrue("Folds not attached", attached);
        assertTrue("Hierarchy must be active", active);
    }
    
    
    final class SimpleFoldManager extends AbstractFoldManager {
        
        public void initFolds(FoldHierarchyTransaction transaction) {
            try {
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
            }
        }
        
    }

    public final class SimpleFoldManagerFactory implements FoldManagerFactory {
        
        public FoldManager createFoldManager() {
            return new SimpleFoldManager();
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
