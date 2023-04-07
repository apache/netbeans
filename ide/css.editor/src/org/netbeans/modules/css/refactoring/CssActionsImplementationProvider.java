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
package org.netbeans.modules.css.refactoring;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

import static org.netbeans.modules.css.lib.api.NodeType.cssClass;
import static org.netbeans.modules.css.lib.api.NodeType.cssId;
import static org.netbeans.modules.css.lib.api.NodeType.elementName;
import static org.netbeans.modules.css.lib.api.NodeType.hexColor;
import static org.netbeans.modules.css.lib.api.NodeType.resourceIdentifier;
import static org.netbeans.modules.css.lib.api.NodeType.term;

/**
 * Folder rename refactoring is enabled by the
 * org.netbeans.modules.web.common.ui.refactoring.FolderActionsImplementationProvider
 *
 * The css refactoring just provides the rename plugin which handles css links
 * possibly affected by the folder rename.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = ActionsImplementationProvider.class, position = 1050)
public class CssActionsImplementationProvider extends ActionsImplementationProvider {

    private static final RequestProcessor RP = new RequestProcessor(CssActionsImplementationProvider.class);
    private static final Logger LOG = Logger.getLogger(CssActionsImplementationProvider.class.getName());

    @Override
    public boolean canRename(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        //we are able to rename only one node selection [at least for now ;-) ]
        if (nodes.size() != 1) {
            return false;
        }

        //check if the file is a file with .css extension or represents
        //an opened file which code embeds a css content on the caret position
        Node node = nodes.iterator().next();
        return isCssFile(node) || isRefactorableEditorElement(node); 

    }

    @Override
    public void doRename(Lookup selectedNodes) {
        EditorCookie ec = selectedNodes.lookup(EditorCookie.class);
        Runnable task;
        if (isFromEditor(ec)) {
            //editor refactoring
            task = new TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(String elementName, Object... lookupContent) {
                    return new CssRenameRefactoringUI(elementName, lookupContent);
                }
            };
        } else {
            //file or folder refactoring
            Collection<? extends Node> nodes = selectedNodes.lookupAll(Node.class);
            assert nodes.size() == 1;
            Node currentNode = nodes.iterator().next();
            task = new NodeToFileTask(currentNode) {
                @Override
                protected RefactoringUI createRefactoringUI(String elementName, Object... lookupContent) {
                    return new CssRenameRefactoringUI(elementName, lookupContent);
                }
            };
        }
        RP.post(task);
    }

    @Override
    public boolean canFindUsages(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        //we are able to rename only one node selection [at least for now ;-) ]
        if (nodes.size() != 1) {
            return false;
        }

        //check if the file is a file with .css extension or represents
        //an opened file which code embeds a css content on the caret position
        Node node = nodes.iterator().next();
        return isCssFile(node) || isRefactorableEditorElement(node);
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        Runnable task;
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec) {
                //editor element context
                @Override
                protected RefactoringUI createRefactoringUI(String elementName, Object... lookupContent) {
                    return new WhereUsedUI(lookupContent);
                }
            };
        } else {
            //file context
            Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
            assert nodes.size() == 1;
            Node currentNode = nodes.iterator().next();
            task = new NodeToFileTask(currentNode) {
                @Override
                protected RefactoringUI createRefactoringUI(String elementName, Object... lookupContent) {
                    return new WhereUsedUI(lookupContent);
                }
            };
        }

        RP.post(task);
    }

    private static boolean isCssFile(Node node) {
        //for the one thing check if the node represents a css file itself
        FileObject fo = getFileObjectFromNode(node);
        if (fo == null) {
            return false;
        }
        return CssLanguage.CSS_MIME_TYPE.equals(fo.getMIMEType());
    }

    /*
    * We can't access the parser here as we may (or always are?) be called
     * in the EDT.
     */
    private static boolean isRefactorableEditorElement(final Node node) {
        final AtomicBoolean result = new AtomicBoolean(false);
        Mutex.EVENT.readAccess(() -> {
            EditorCookie ec = getEditorCookie(node);
            if (isFromEditor(ec)) {
                //check if there's css code at the offset
                final StyledDocument document = ec.getDocument();
                JEditorPane pane = ec.getOpenedPanes()[0];
                final int caret = pane.getCaretPosition();
                document.render(() -> {
                    TokenSequence<?> ts = LexerUtils.getTokenSequence(document, caret, CssTokenId.language(), false);
                    result.set(ts != null);
                });
            }
        });

        return result.get();
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

    private static EditorCookie getEditorCookie(Node node) {
        return node.getLookup().lookup(EditorCookie.class);
    }

    private abstract static class NodeToFileTask implements Runnable {

        private final Node node;
        private FileObject fileObject;

        public NodeToFileTask(Node node) {
            this.node = node;
        }

        //runs in RequestProcessor
        @Override
        public void run() {
            DataObject dobj = node.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                fileObject = dobj.getPrimaryFile();

                //switch to EDT
                EventQueue.invokeLater(() -> {
                    UI.openRefactoringUI(createRefactoringUI(fileObject.getName(), fileObject));
                });
            }

        }

        protected abstract RefactoringUI createRefactoringUI(String elementName, Object... lookupContent);
    }

    private abstract static class TextComponentTask extends UserTask implements Runnable {

        private final Document document;
        private final int caretOffset;
        private RefactoringUI ui;

        public TextComponentTask(EditorCookie ec) {
            JTextComponent textC = ec.getOpenedPanes()[0];
            this.document = textC.getDocument();
            this.caretOffset = textC.getCaretPosition();
        }

        @Override
        public void run(ResultIterator ri) throws ParseException {
            ResultIterator cssri = WebUtils.getResultIterator(ri, CssLanguage.CSS_MIME_TYPE);

            if (cssri != null) {
                CssParserResult result = (CssParserResult) cssri.getParserResult();
                if (result.getParseTree() != null) {
                    //the parser result seems to be quite ok,
                    //in case of serious parse issue the parse root is null
                    CssRefactoringInfo context = createRefactoringInfo(result, caretOffset);
                    ui = context != null ? createRefactoringUI(context.getElementName(), context) : null;
                }
            }
        }

        //runs in RequestProcessor
        @Override
        public final void run() {
            try {
                Source source = Source.create(document);
                ParserManager.parse(Collections.singleton(source), this);
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
                return;
            }

            //switch to EDT
            EventQueue.invokeLater(() -> {
                TopComponent activetc = TopComponent.getRegistry().getActivated();

                if (ui != null) {
                    UI.openRefactoringUI(ui, activetc);
                } else {
                    JOptionPane.showMessageDialog(null, NbBundle.getMessage(CssActionsImplementationProvider.class, "ERR_CannotRefactorLoc"));//NOI18N
                }
            });
        }

        protected abstract RefactoringUI createRefactoringUI(String elementName, Object... lookupContent);
    }

    private static CssRefactoringInfo createRefactoringInfo(CssParserResult result, int caretOffset) {
        org.netbeans.modules.css.lib.api.Node root = result.getParseTree();
        int astOffset = result.getSnapshot().getEmbeddedOffset(caretOffset);
        org.netbeans.modules.css.lib.api.Node leaf = NodeUtil.findNodeAtOffset(root, astOffset);
        if (leaf != null) {
            //we found token node, use its encolosing node - parent
            leaf = leaf.parent();
        } else {
            return null;
        }

        CssRefactoringInfo.Type type = null;
        switch (leaf.type()) {
            case elementName:
                type = CssRefactoringInfo.Type.ELEMENT;
                break;
            case cssClass:
                type = CssRefactoringInfo.Type.CLASS;
                break;
            case cssId:
                type = CssRefactoringInfo.Type.ID;
                break;
            case hexColor:
                type = CssRefactoringInfo.Type.HEX_COLOR;
                break;
            case resourceIdentifier:
                type = CssRefactoringInfo.Type.RESOURCE_IDENTIFIER;
                break;
            case term:
                if (NodeUtil.getChildTokenNode(leaf, CssTokenId.URI) != null) {
                    type = CssRefactoringInfo.Type.URI;
                }
        }

        if (type == null) {
            return null;
        }

        String name = null;
        switch (leaf.type()) {
            case resourceIdentifier:
                org.netbeans.modules.css.lib.api.Node string = NodeUtil.getChildTokenNode(leaf, CssTokenId.STRING);
                if (string != null) {
                    name = WebUtils.unquotedValue(string.unescapedImage());
                    //w/o extension!
                    int dotIndex = name.lastIndexOf('.');
                    if (dotIndex != -1) {
                        name = name.substring(0, dotIndex);
                    }
                }
                break;
            case term:
                org.netbeans.modules.css.lib.api.Node uri = NodeUtil.getChildTokenNode(leaf, CssTokenId.URI);
                if (uri != null) {
                    Matcher m = Css3Utils.URI_PATTERN.matcher(uri.unescapedImage());
                    if (m.matches()) {
                        int groupIndex = 1;
                        String content = m.group(groupIndex);
                        name = WebUtils.unquotedValue(content);
                        //w/o extension!
                        int dotIndex = name.lastIndexOf('.');
                        if (dotIndex != -1) {
                            name = name.substring(0, dotIndex);
                        }
                    }
                }
                break;
            case cssClass:
            case cssId:
                name = leaf.unescapedImage().trim().substring(1);
                break;
            default:
                name = leaf.unescapedImage().trim();
        }

        if (name == null) {
            return null;
        }

        FileObject fileObject = result.getSnapshot().getSource().getFileObject();

        return new CssRefactoringInfo(fileObject, name, type);
    }
}
