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
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[0]));
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
