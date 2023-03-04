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
package org.netbeans.modules.css.prep.editor.refactoring;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.css.lib.api.CssParserFactory;
import org.netbeans.modules.css.lib.api.CssParserResult;
import static org.netbeans.modules.css.lib.api.NodeType.cp_mixin_name;
import static org.netbeans.modules.css.lib.api.NodeType.cp_variable;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.prep.editor.less.LessLanguage;
import org.netbeans.modules.css.prep.editor.scss.ScssLanguage;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Folder rename refactoring is enabled by the
 * org.netbeans.modules.web.common.ui.refactoring.FolderActionsImplementationProvider
 *
 * The css refactoring just provides the rename plugin which handles css links
 * possibly affected by the folder rename.
 *
 * Notice this ActionsImplementationProvider needs to be registered BEFORE the
 * CssActionsImplementationProvider from css.editor module! As we can't regularly parse
 * the file in canRename/FindUsages method as it runs in EDT, there's no
 * reliable way how to ensure the the correct provider is active. The workaround
 * is not to do the parsing in CssActionsImplementationProvider at all as the parser
 * result for pure css code needs to be obtained from EmbeddingProviders (via the
 * parsing infrastructure) but do some css parsing for sass/less files w/o 
 * the parsing infrastructure as we are always the top level language and hence
 * do not need the EmbeddingProviders. This way the CPActionsImplementationProvider
 * first checks if the caret is on a SASS/LESS content and if not fallbacks to 
 * the pure CssActionsImplementationProvider.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = ActionsImplementationProvider.class, position = 1033)
public class CPActionsImplementationProvider extends ActionsImplementationProvider {

    private static final Logger LOG = Logger.getLogger(CPActionsImplementationProvider.class.getName());

    @Override
    public boolean canRename(Lookup lookup) {
        return canRefactor(lookup);
    }

    @Override
    public void doRename(Lookup selectedNodes) {
        EditorCookie ec = selectedNodes.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            //editor refactoring
            new TextComponentTask(ec) {

                @Override
                protected RefactoringUI createRefactoringUI(RefactoringElementContext context) {
                    return new CPRenameRefactoringUI(context);
                }
            }.run();
        } else {
            //file or folder refactoring - not supported
        }
    }

    @Override
    public boolean canFindUsages(Lookup lookup) {
        return canRefactor(lookup);
    }

    private static boolean canRefactor(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node node = nodes.iterator().next();

        //can refactor only in less/sass files
        FileObject file = getFileObjectFromNode(node);
        if (file != null) {
            String mimeType = file.getMIMEType();
            if (LessLanguage.getLanguageInstance().mimeType().equals(mimeType) || ScssLanguage.getLanguageInstance().mimeType().equals(mimeType)) {
                return isRefactorableEditorElement(node);
            }
        }
        return false;
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            new TextComponentTask(ec) {

                //editor element context
                @Override
                protected RefactoringUI createRefactoringUI(RefactoringElementContext context) {
                    return new WhereUsedUI(context);
                }
            }.run();
        } else {
            //file context - not supported
        }
    }

    private static boolean isRefactorableEditorElement(final Node node) {
        class Context {

            public int caret;
            public Document document;
        }
        final Context context = Mutex.EVENT.readAccess(new Mutex.Action<Context>() {

            @Override
            public Context run() {
                EditorCookie ec = node.getLookup().lookup(EditorCookie.class);
                if (isFromEditor(ec)) {
                    Context context = new Context();
                    context.document = ec.getDocument();
                    JEditorPane pane = ec.getOpenedPanes()[0];
                    context.caret = pane.getCaretPosition();
                    return context;
                } else {
                    return null;
                }
            }
        });
        if (context == null) {
            return false;
        }
        Source source = Source.create(context.document);
        Snapshot snapshot = source.createSnapshot();
        //we can't do the parsing via the parsing infrastructure as this may block the EDT for long time
        Parser cssParser = CssParserFactory.getDefault().createParser(Collections.singleton(snapshot));
        try {
            cssParser.parse(snapshot, null, null);
            CssParserResult result = (CssParserResult) cssParser.getResult(null);
            org.netbeans.modules.css.lib.api.Node leaf = NodeUtil.findNonTokenNodeAtOffset(result.getParseTree(), context.caret);
            if (leaf != null) {
                switch (leaf.type()) {
                    case cp_variable:
                    case cp_mixin_name:
                        return true;
                    default:
                        return false;
                }
            }
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private static FileObject getFileObjectFromNode(Node node) {
        DataObject dobj = node.getLookup().lookup(DataObject.class);
        return dobj != null ? dobj.getPrimaryFile() : null;
    }

    private static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }

    private abstract static class TextComponentTask extends UserTask implements Runnable {

        private final Document document;
        private final int caretOffset;
        private final int selectionStart;
        private final int selectionEnd;
        private RefactoringUI ui;

        public TextComponentTask(EditorCookie ec) {
            JTextComponent textC = ec.getOpenedPanes()[0];
            this.document = textC.getDocument();
            this.caretOffset = textC.getCaretPosition();
            this.selectionStart = textC.getSelectionStart();
            this.selectionEnd = textC.getSelectionEnd();
        }

        @Override
        public void run(ResultIterator ri) throws ParseException {
            ResultIterator cssri = WebUtils.getResultIterator(ri, "text/css");

            if (cssri != null) {
                CssParserResult result = (CssParserResult) cssri.getParserResult();
                if (result.getParseTree() != null) {
                    //the parser result seems to be quite ok,
                    //in case of serious parse issue the parse root is null
                    RefactoringElementContext context = new RefactoringElementContext(result, caretOffset, selectionStart, selectionEnd);
                    ui = context.isRefactoringAllowed() ? createRefactoringUI(context) : null;
                }
            }
        }

        @Override
        public final void run() {
            try {
                Source source = Source.create(document);
                ParserManager.parse(Collections.singleton(source), this);
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
                return;
            }

            TopComponent activetc = TopComponent.getRegistry().getActivated();

            if (ui != null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null, NbBundle.getMessage(CPActionsImplementationProvider.class, "ERR_CannotRefactorLoc"));//NOI18N
            }
        }

        protected abstract RefactoringUI createRefactoringUI(RefactoringElementContext context);
    }
}
