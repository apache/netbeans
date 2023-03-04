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

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.DocumentTesting;
import org.netbeans.lib.editor.util.random.EditorPaneTesting;
import org.netbeans.lib.editor.util.random.RandomTestContainer;

/**
 *
 * @author mmetelka
 */
public class RandomFoldManagerTest extends NbTestCase {
    
    private static final int OP_COUNT = 10000;

    private static final boolean LOG_OP_AND_DOC = false;

    private static final Level LOG_LEVEL = Level.FINE;

    public RandomFoldManagerTest(String testName) {
        super(testName);
        Filter filter = new Filter();
        filter.setIncludes(new Filter.IncludeExclude[]{new Filter.IncludeExclude("testAddRemoveFolds", "")});
//        setFilter(filter);
    }

    private static void loggingOn() {
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(LOG_LEVEL);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(LOG_LEVEL);
        // FINEST throws ISE for integrity error
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINEST);
        // Check gap-storage correctness
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorBoxViewChildren").setLevel(Level.FINE);
    }

    private RandomTestContainer createContainer() throws Exception {
        final RandomTestContainer fcontainer = new RandomTestContainer();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JEditorPane pane = new JEditorPane();
                pane.setDocument(new BaseDocument(false, "text/plain"));
                fcontainer.putProperty(JEditorPane.class, pane);
            }
        });
        RandomTestContainer container = EditorPaneTesting.initContainer(fcontainer, null);
        DocumentTesting.initContainer(container);
        DocumentTesting.initUndoManager(container);
        FoldManagerTesting.initContainer(container);
        container.setName(this.getName());
        container.setLogOp(LOG_OP_AND_DOC);
        DocumentTesting.setLogDoc(container, LOG_OP_AND_DOC);
        return container;
    }

    public void testAddRemoveFolds() throws Exception {
        loggingOn();
        RandomTestContainer container = createContainer();
        JEditorPane pane = container.getInstance(JEditorPane.class);
        Document doc = pane.getDocument();
        RandomTestContainer.Context context = container.context();
        DocumentTesting.insert(context, 0, "abc\ndef\nxyz");

        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(OP_COUNT);
        round.setRatio(DocumentTesting.INSERT_CHAR, 5);
        round.setRatio(DocumentTesting.INSERT_TEXT, 3);
        round.setRatio(DocumentTesting.REMOVE_CHAR, 3);
        round.setRatio(DocumentTesting.REMOVE_TEXT, 1);
        round.setRatio(DocumentTesting.UNDO, 1);
        round.setRatio(DocumentTesting.REDO, 1);

        round.setRatio(FoldManagerTesting.ADD_FOLDS, 10);
        round.setRatio(FoldManagerTesting.REMOVE_FOLDS, 10);

        container.run(1273153097712L); // failed op=1222: IllegalArgumentException: affectedEndOffset=300 < affectedStartOffset=301
        container.run(0L);
    }

}
