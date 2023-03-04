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
package org.netbeans.modules.refactoring.php;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radek Matous
 */
@NbBundle.Messages("ERR_CannotRefactorLoc=Can't refactor here")
public abstract class RefactoringTask extends UserTask implements Runnable {
    private static final RequestProcessor RP = new RequestProcessor(RefactoringTask.class);
    private static final Logger LOG = Logger.getLogger(RefactoringTask.class.getName());
    RefactoringUIHolder uiHolder = RefactoringUIHolder.NONE;

    protected void fetchRefactoringUI(Source source, UserTask userTask) {
        Future<?> futureTask = RP.submit(new ParsingTask(source, userTask));
        boolean parsingInProgress = false;
        try {
            futureTask.get(2500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (TimeoutException ex) {
            futureTask.cancel(true);
            parsingInProgress = true;
        }
        uiHolder.processUI(parsingInProgress);
    }

    private static final class ParsingTask implements Runnable, Cancellable {
        private final Source source;
        private final UserTask userTask;
        private volatile boolean cancelled;
        private volatile Future<Void> future;

        private ParsingTask(Source source, UserTask userTask) {
            this.source = source;
            this.userTask = userTask;
        }

        @Override
        public void run() {
            try {
                if (!cancelled) {
                    future = ParserManager.parseWhenScanFinished(Collections.singleton(source), userTask);
                }
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }

        @Override
        public boolean cancel() {
            cancelled = true;
            if (future != null) {
                future.cancel(true);
            }
            return true;
        }
    }

    abstract static class NodeToFileTask extends RefactoringTask {

        private final Node node;
        private FileObject fileObject;

        public NodeToFileTask(Node node) {
            this.node = node;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Result parserResult = resultIterator.getParserResult();
            if (parserResult instanceof PHPParseResult) {
                Program root = RefactoringUtils.getRoot((PHPParseResult) parserResult);
                if (root != null) {
                    uiHolder = createRefactoringUIHolder((PHPParseResult) parserResult);
                    return;
                }
            }
            // TODO How do I add some kind of error message?
            RefactoringTask.LOG.log(Level.FINE, "FAILURE - can't refactor uncompileable sources");
        }

        @Override
        public void run() {
            DataObject dobj = node.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                fileObject = dobj.getPrimaryFile();
                if (fileObject.isFolder()) {
                    JOptionPane.showMessageDialog(null, Bundle.ERR_CannotRefactorLoc());
                } else {
                    fetchRefactoringUI(Source.create(fileObject), this);
                }
            }
        }

        protected abstract RefactoringUIHolder createRefactoringUIHolder(final PHPParseResult info);
    }

    abstract static class TextComponentTask extends RefactoringTask {

        private final JTextComponent textC;
        private final int caret;
        private final Document document;

        public TextComponentTask(final EditorCookie ec) {
            this.textC = ec.getOpenedPanes()[0];
            this.document = textC.getDocument();
            this.caret = textC.getCaretPosition();
            assert caret != -1;
        }

        @Override
        public void run() {
            fetchRefactoringUI(Source.create(document), this);
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Result parserResult = resultIterator.getParserResult();
            if (parserResult instanceof PHPParseResult) {
                Program root = RefactoringUtils.getRoot((PHPParseResult) parserResult);
                if (root != null) {
                    uiHolder = createRefactoringUI((PHPParseResult) parserResult, caret);
                    return;
                }
            }
            // TODO How do I add some kind of error message?
            RefactoringTask.LOG.log(Level.FINE, "FAILURE - can't refactor uncompileable sources");
        }

        protected abstract RefactoringUIHolder createRefactoringUI(final PHPParseResult info, final int offset);
    }

    @NbBundle.Messages({
        "ERR_ParsingInProgress=Can't refactor - parsing in progress.",
        "ERR_ElementNotInUsersFile=Can't refactor - element is on Include Path or in Signature File"
    })
    interface RefactoringUIHolder {
        RefactoringUIHolder NONE = new RefactoringUIHolder() {

            @Override
            public void processUI(boolean parsingInProgress) {
                if (parsingInProgress) {
                    JOptionPane.showMessageDialog(null, Bundle.ERR_ParsingInProgress());
                } else {
                    JOptionPane.showMessageDialog(null, Bundle.ERR_CannotRefactorLoc());
                }
            }
        };

        RefactoringUIHolder NOT_USERS_FILE = new RefactoringUIHolder() {

            @Override
            public void processUI(boolean parsingInProgress) {
                JOptionPane.showMessageDialog(null, Bundle.ERR_ElementNotInUsersFile());
            }
        };

        void processUI(boolean parsingInProgress);
    }

}
