/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.document;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Miloslav Metelka
 */
public class StableCompoundEditTest extends NbTestCase {
    
    public StableCompoundEditTest(String testName) {
        super(testName);
        List<String> includes = new ArrayList<String>();
//        includes.add("testSimpleUndo");
//        includes.add("testSimplePositionSharingMods");
//        includes.add("testEndPosition");
//        includes.add("testRandomMods");
//        includes.add("testRemoveAtZero");
//        includes.add("testBackwardBiasPositionsSimple");
//        includes.add("testBackwardBiasPositions");
//        includes.add("testRemoveSimple");
//        filterTests(includes);
    }
    
    private void filterTests(List<String> includeTestNames) {
        List<Filter.IncludeExclude> includeTests = new ArrayList<Filter.IncludeExclude>();
        for (String testName : includeTestNames) {
            includeTests.add(new Filter.IncludeExclude(testName, ""));
        }
        Filter filter = new Filter();
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[includeTests.size()]));
        setFilter(filter);
    }

    @Override
    protected Level logLevel() {
//        return Level.FINEST;
//        return Level.FINE;
//        return Level.INFO;
        return null;
    }
    
    public void testBasicUndoRedo() throws Exception {
        StableCompoundEdit cEdit = new StableCompoundEdit();
        TestEdit e0 = new TestEdit();
        TestEdit e1 = new TestEdit();
        cEdit.addEdit(e0);
        cEdit.addEdit(e1);
        NbTestCase.assertFalse("Not ended yet", cEdit.canUndo());
        NbTestCase.assertFalse("Not ended yet", cEdit.canRedo());
        cEdit.end();
        NbTestCase.assertTrue("Expected undoable", cEdit.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", cEdit.canRedo());
        
        cEdit.undo();
        NbTestCase.assertFalse("Expected non-undoable", cEdit.canUndo());
        NbTestCase.assertTrue("Expected redoable", cEdit.canRedo());
        NbTestCase.assertFalse("Expected non-undoable", e0.canUndo());
        NbTestCase.assertTrue("Expected redoable", e0.canRedo());
        NbTestCase.assertFalse("Expected non-undoable", e1.canUndo());
        NbTestCase.assertTrue("Expected redoable", e1.canRedo());

        cEdit.redo();
        NbTestCase.assertTrue("Expected undoable", cEdit.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", cEdit.canRedo());
        NbTestCase.assertTrue("Expected undoable", e0.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", e0.canRedo());
        NbTestCase.assertTrue("Expected undoable", e1.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", e1.canRedo());
        
        cEdit.die();
    }

    public void testBasicFailUndo0() throws Exception {
        StableCompoundEdit cEdit = new StableCompoundEdit();
        TestEdit e0 = new TestEdit(true);
        TestEdit e1 = new TestEdit();
        cEdit.addEdit(e0);
        cEdit.addEdit(e1);
        NbTestCase.assertFalse("Not ended yet", cEdit.canUndo());
        NbTestCase.assertFalse("Not ended yet", cEdit.canRedo());
        cEdit.end();
        NbTestCase.assertTrue("Expected undoable", cEdit.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", cEdit.canRedo());
        
        try {
            cEdit.undo();
            fail("Was expecting CannotUndoException exception.");
        } catch (CannotUndoException ex) {
            // Expected
        }
        NbTestCase.assertTrue("Expected undoable", cEdit.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", cEdit.canRedo());
        NbTestCase.assertTrue("Expected undoable", e0.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", e0.canRedo());
        NbTestCase.assertTrue("Expected undoable", e1.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", e1.canRedo());
        
    }

    public void testBasicFailUndo1() throws Exception {
        StableCompoundEdit cEdit = new StableCompoundEdit();
        TestEdit e0 = new TestEdit();
        TestEdit e1 = new TestEdit(true);
        cEdit.addEdit(e0);
        cEdit.addEdit(e1);
        NbTestCase.assertFalse("Not ended yet", cEdit.canUndo());
        NbTestCase.assertFalse("Not ended yet", cEdit.canRedo());
        cEdit.end();
        NbTestCase.assertTrue("Expected undoable", cEdit.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", cEdit.canRedo());
        
        try {
            cEdit.undo();
            fail("Was expecting CannotUndoException exception.");
        } catch (CannotUndoException ex) {
            // Expected
        }
        NbTestCase.assertTrue("Expected undoable", cEdit.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", cEdit.canRedo());
        NbTestCase.assertTrue("Expected undoable", e0.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", e0.canRedo());
        NbTestCase.assertTrue("Expected undoable", e1.canUndo());
        NbTestCase.assertFalse("Expected non-redoable", e1.canRedo());
        
    }

    private static final class TestEdit extends AbstractUndoableEdit {
        
        private final boolean fireException;

        TestEdit() {
            this(false);
        }
        
        TestEdit(boolean fireException) {
            this.fireException = fireException;
        }

        @Override
        public void undo() throws CannotUndoException {
            if (fireException) {
                throw new CannotUndoException();
            }
            super.undo();
        }

        @Override
        public void redo() throws CannotRedoException {
            if (fireException) {
                throw new CannotRedoException();
            }
            super.redo();
        }
        
    }
}
