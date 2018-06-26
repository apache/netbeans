/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            if (parserResult != null && parserResult instanceof PHPParseResult) {
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
