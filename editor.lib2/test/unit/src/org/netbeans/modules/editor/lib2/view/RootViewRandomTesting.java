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

package org.netbeans.modules.editor.lib2.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.random.DocumentTesting;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;
import org.netbeans.lib.editor.util.random.RandomText;

/**
 * Random testing support for fold managers.
 *
 * @author Miloslav Metelka
 */
public class RootViewRandomTesting {
    
    public static final String CREATE_PANE = "create-pane";
    
    public static final String RELEASE_PANE = "release-pane";

    public static RandomTestContainer createContainer() throws Exception {
        ViewUpdatesTesting.registerTestFactory();
        RandomTestContainer container = new RandomTestContainer();
        DocumentTesting.initContainer(container);
        DocumentTesting.initUndoManager(container);
        DocumentTesting.setSameThreadInvoke(container.context(), true);
        container.putProperty(PaneList.class, new PaneList());
        container.addOp(new PaneOp(CREATE_PANE));
        container.addOp(new PaneOp(RELEASE_PANE));
        container.addCheck(new MultiPaneCheck());
        return container;
    }
    
    static List<JEditorPane> getPaneList(Context context) {
        return context.getInstance(PaneList.class).paneList;
    }
    
    static JEditorPane addNewPane(Context context) {
        JEditorPane pane = ViewUpdatesTesting.createPane(DocumentTesting.getDocument(context));
        getPaneList(context).add(pane);
        return pane;
    }
    
    public static void initRandomText(RandomTestContainer container) throws Exception {
//        container.addOp(new Op());
        container.addCheck(new MultiPaneCheck());
        RandomText randomText = RandomText.join(
                RandomText.lowerCaseAZ(3),
                RandomText.spaceTabNewline(1),
                RandomText.phrase(" \n\n\n", 1),
                RandomText.phrase(" \t\tabcdef\t", 1)
        );
        container.putProperty(RandomText.class, randomText);
    }

    public static RandomTestContainer.Round addRound(RandomTestContainer container) throws Exception {
        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(100);
        round.setRatio(DocumentTesting.INSERT_CHAR, 5);
        round.setRatio(DocumentTesting.INSERT_TEXT, 3);
        round.setRatio(DocumentTesting.INSERT_PHRASE, 3);
        round.setRatio(DocumentTesting.REMOVE_CHAR, 3);
        round.setRatio(DocumentTesting.REMOVE_TEXT, 1);
        round.setRatio(DocumentTesting.UNDO, 1);
        round.setRatio(DocumentTesting.REDO, 1);
        round.setRatio(CREATE_PANE, 2);
        round.setRatio(RELEASE_PANE, 1);
        return round;
    }
    
    public static void checkIntegrity(Context context) {
        List<JEditorPane> paneList = getPaneList(context);
        int paneListSize = paneList.size();
        for (int i = 0; i < paneListSize; i++) {
            JEditorPane pane = paneList.get(i);
            DocumentView docView = DocumentView.get(pane);
            String err = docView.findTreeIntegrityError();
            int id = ViewUpdatesTesting.getId(pane);
            if (err != null) {
                throw new IllegalStateException("VH(" + id + ") integrity ERROR:\n" +
                        err + "\n" + docView.toStringDetailNeedsLock());
            }
            if (context.isLogOp()) {
                context.logOpBuilder().append("\nVH(").append(id).append("):\n").
                        append(pane).append("\n");
            }

        }
    }

    final static class PaneOp extends RandomTestContainer.Op {

        public PaneOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            List<JEditorPane> paneList = getPaneList(context);
            Random random = context.container().random();
            StringBuilder log = context.logOpBuilder();
            if (CREATE_PANE == name()) { // Just use ==
                if (log != null) {
                    log.append("CREATE_ROOT_VIEW");
                }
                boolean createBounded = !paneList.isEmpty();
                JEditorPane pane = ViewUpdatesTesting.createPane(DocumentTesting.getDocument(context));
                paneList.add(pane);
                if (createBounded) {
                    Document doc = pane.getDocument();
                    int startOffset = random.nextInt(doc.getLength());
                    int endOffset = startOffset + random.nextInt(doc.getLength() - startOffset) + 1;
                    ViewUpdatesTesting.setViewBounds(pane, startOffset, endOffset);
                    if (log != null) {
                        log.append("(").append(startOffset).append(",").append(endOffset).append(")");
                    }
                }
                if (log != null) {
                    log.append("\n");
                    context.logOp(log);
                }
                pane.modelToView(0);

            } else if (RELEASE_PANE == name()) { // Just use ==
                if (!paneList.isEmpty()) {
                    int index = random.nextInt(paneList.size());
                    paneList.remove(index);
                    if (log != null) {
                        log.append("DESTROY_ROOT_VIEW[" + index + "]\n");
                    }
                }
            }
        }

    }

    private static final class PaneList {
        
        List<JEditorPane> paneList = new ArrayList<JEditorPane>();

    }

    private static final class MultiPaneCheck extends RandomTestContainer.Check {

        @Override
        protected void check(Context context) throws Exception {
            checkIntegrity(context);
        }


    }

}
