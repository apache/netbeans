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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * If this class name is changed, {@link LineBreakpointBeanInfo} class name must
 * be changed.
 *
 * @author ads
 */
public class LineBreakpoint extends AbstractBreakpoint {

    private static final Logger LOGGER = Logger.getLogger(LineBreakpoint.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(LineBreakpoint.class);
    public static final String PROP_URL = "url"; // NOI18N
    public static final String PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    public static final String PROP_CONDITION = "condition"; // NOI18N

    private final Line myLine;
    private final FileRemoveListener myListener;
    private FileChangeListener myWeakListener;
    private final String myFileUrl;
    private final Future<Boolean> isValidFuture;
    // @GuardedBy("this")
    private String condition;

    public LineBreakpoint(Line line) {
        myLine = line;
        myListener = new FileRemoveListener();
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        if (fileObject != null) {
            myWeakListener = WeakListeners.create(FileChangeListener.class, myListener, fileObject);
            fileObject.addFileChangeListener(myWeakListener);
            myFileUrl = fileObject.toURL().toString();
        } else {
            myFileUrl = ""; //NOI18N
        }
        isValidFuture = RP.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                final Boolean[] result = new Boolean[1];
                DataObject dataObject = DataEditorSupport.findDataObject(myLine);
                EditorCookie editorCookie = (EditorCookie) dataObject.getLookup().lookup(EditorCookie.class);
                final StyledDocument styledDocument = editorCookie.getDocument();
                if (styledDocument instanceof BaseDocument) {
                    try {
                        final BaseDocument baseDocument = (BaseDocument) styledDocument;
                        Source source = Source.create(baseDocument);
                        ParserManager.parse(Collections.singleton(source), new UserTask() {

                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                Parser.Result parserResult = resultIterator.getParserResult();
                                if (parserResult instanceof PHPParseResult) {
                                    PHPParseResult phpParserResult = (PHPParseResult) parserResult;
                                    int rowStart = LineDocumentUtils.getLineStartFromIndex(baseDocument, myLine.getLineNumber());
                                    int contentStart = LineDocumentUtils.getLineFirstNonWhitespace(baseDocument, rowStart);
                                    int contentEnd = LineDocumentUtils.getLineLastNonWhitespace(baseDocument, rowStart) + 1;
                                    StatementVisitor statementVisitor = new StatementVisitor(contentStart, contentEnd);
                                    statementVisitor.scan(phpParserResult.getProgram().getStatements());
                                    int properStatementOffset = statementVisitor.getProperStatementOffset();
                                    result[0] = properStatementOffset == contentStart;
                                }
                            }
                        });
                    } catch (ParseException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    }
                }
                return result[0];
            }
        });
    }

    public final void refreshValidity() {
        Boolean valid = isValid();
        if (valid == null) {
            return ;
        }
        if (!valid) {
            setValidity(VALIDITY.INVALID, null);
        } else if (getBreakpointId() == null) {
            // not submitted
            setValidity(VALIDITY.UNKNOWN, null);
        } else {
            // submitted
            setValidity(VALIDITY.VALID, null);
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "Intentional")
    @CheckForNull
    private Boolean isValid() {
        try {
            return isValidFuture.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.interrupted();
        } catch (ExecutionException | TimeoutException ex) {
            isValidFuture.cancel(true);
            LOGGER.log(Level.FINE, null, ex);
        } catch (CancellationException ex) {
            //noop
        }
        return null;
    }

    public Line getLine() {
        return myLine;
    }

    public String getFileUrl() {
        return myFileUrl;
    }

    public final synchronized String getCondition() {
        return condition;
    }

    public final synchronized void setCondition(String condition) {
        String oldCondition = this.condition;
        if ((condition != null && condition.equals(oldCondition))
                || (condition == null && oldCondition == null)) {
            return;
        }
        this.condition = condition;
        firePropertyChange(PROP_CONDITION, oldCondition, condition);
    }

    public final synchronized boolean isConditional() {
        return condition != null && !condition.isEmpty();
    }

    @Override
    public int isTemp() {
        return 0;
    }

    @Override
    public boolean isSessionRelated(DebugSession session) {
        SessionId id = session != null ? session.getSessionId() : null;
        if (id == null) {
            return false;
        }
        return id.getProject() != null;
    }

    @Override
    public void removed() {
        FileObject fileObject = getLine().getLookup().lookup(FileObject.class);
        if (fileObject != null) {
            fileObject.removeFileChangeListener(myWeakListener);
        }
    }

    @Override
    public GroupProperties getGroupProperties() {
        return new PhpGroupProperties();
    }

    void fireLineNumberChanged() {
        int lineNumber = getLine().getLineNumber();
        firePropertyChange(PROP_LINE_NUMBER, null, lineNumber);
    }

    //~ Inner classes

    private final class PhpGroupProperties extends GroupProperties {

        @Override
        public String getLanguage() {
            return "PHP"; // NOI18N
        }

        @NbBundle.Messages("LineBreakpoint.type=Line")
        @Override
        public String getType() {
            return Bundle.LineBreakpoint_type();
        }

        @Override
        public FileObject[] getFiles() {
            FileObject file = getFile();
            if (file != null) {
                return new FileObject[] {file};
            }
            return null;
        }

        @Override
        public Project[] getProjects() {
            FileObject file = getFile();
            if (file != null) {
                Project project = ProjectConvertors.getNonConvertorOwner(file);
                if (project != null) {
                    return new Project[] {project};
                }
            }
            return null;
        }

        @Override
        public DebuggerEngine[] getEngines() {
            // ???
            return null;
        }

        @Override
        public boolean isHidden() {
            return false;
        }

        @CheckForNull
        private FileObject getFile() {
            return myLine.getLookup().lookup(FileObject.class);
        }

    }

    private class FileRemoveListener extends FileChangeAdapter {

        @Override
        public void fileDeleted(FileEvent arg0) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(
                    LineBreakpoint.this);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            FileObject renamedFo = fe.getFile();
            firePropertyChange(PROP_URL, myFileUrl, renamedFo.toURL().toString());
        }

    }

    private static final class StatementVisitor extends DefaultVisitor {
        private int properStatementOffset;
        private final int contentStart;
        private final int contentEnd;

        private StatementVisitor(int contentStart, int contentEnd) {
            this.contentStart = contentStart;
            this.contentEnd = contentEnd;
        }

        @Override
        public void scan(ASTNode node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node != null) {
                OffsetRange nodeRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                if (node instanceof Statement && nodeRange.containsInclusive(contentStart) && nodeRange.containsInclusive(contentEnd)) {
                    properStatementOffset = node.getStartOffset();
                }
                super.scan(node);
            }
        }

        public int getProperStatementOffset() {
            return properStatementOffset;
        }

    }

}
