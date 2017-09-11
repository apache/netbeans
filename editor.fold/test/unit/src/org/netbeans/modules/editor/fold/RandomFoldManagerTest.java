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
