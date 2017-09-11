/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class, position = 10) //NOI18N
})
public final class WaitScanFinishedCompletionProvider implements CompletionProvider {
    
    private static final RequestProcessor RP = new RequestProcessor(
                WaitScanFinishedCompletionProvider.class.getName(),
                1,
                false,
                false);
    
    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        return (queryType & COMPLETION_QUERY_TYPE) != 0 && IndexingManager.getDefault().isIndexing() ? new AsyncCompletionTask(new Query(), component) : null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    @NbBundle.Messages("LBL_ScanningInProgress=Scanning in progress...")
    private static final class Query extends AsyncCompletionQuery {
        
        private final AtomicReference<RequestProcessor.Task> task = new AtomicReference<RequestProcessor.Task>();

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                final Source source = Source.create(doc);
                if (source != null) {
                    resultSet.setWaitText(Bundle.LBL_ScanningInProgress());
                    resultSet.addItem(new Item());
                    if (task.get() == null) {
                        final RequestProcessor.Task newTask = RP.create(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Future<Void> f = ParserManager.parseWhenScanFinished(Collections.singletonList(source), new UserTask() {

                                        @Override
                                        public void run(ResultIterator resultIterator) throws Exception {
                                        }
                                    });
                                    if (!f.isDone()) {
                                        while (true) {
                                            if (isTaskCancelled()) {
                                                task.set(null);
                                                f.cancel(false);
                                                break;
                                            }
                                            try {
                                                f.get(250, TimeUnit.MILLISECONDS);
                                                task.set(null);
                                                Completion.get().hideCompletion();
                                                Completion.get().showCompletion();
                                                break;
                                            } catch (TimeoutException te) {
                                                // retry
                                            }
                                        }
                                    }
                                } catch (InterruptedException ie) {
                                } catch (ExecutionException ee) {
                                } catch (ParseException pe) {
                                }
                            }
                        });
                        if (task.compareAndSet(null, newTask)) {
                            newTask.schedule(0);
                        }
                    }
                }
            } finally {
                resultSet.finish();
            }
        }
    }

    @NbBundle.Messages("LBL_IncompleteResults=Searching for suggestions...")
    private static final class Item implements CompletionItem {

        @Override
        public void defaultAction(JTextComponent component) {
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(Bundle.LBL_IncompleteResults(), null, g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(null, Bundle.LBL_IncompleteResults(), null, g, defaultFont, defaultColor, width, height, selected);
            g.setColor(Color.gray);
            g.drawLine(0, 0, g.getClipBounds().width, 0);
            g.setColor(defaultColor);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
        }

        @Override
        public CompletionTask createToolTipTask() {
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        public int getSortPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public CharSequence getSortText() {
            return null;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return null;
        }
    }
}
