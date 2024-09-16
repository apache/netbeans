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
package org.netbeans.modules.lsp.client.bindings;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.LSPBindings.BackgroundTask;
import org.netbeans.modules.lsp.client.Utils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author lahvac
 */
public class NavigatorPanelImpl extends AbstractNavigatorPanel<Either<SymbolInformation, DocumentSymbol>> implements BackgroundTask {
    static final NavigatorPanelImpl INSTANCE = new NavigatorPanelImpl();

    public NavigatorPanelImpl() {
    }

    @Override
    void addBackgroundTask(FileObject fo) {
        LSPBindings.addBackgroundTask(fo, this);
    }

    @Override
    void removeBackgroundTask(FileObject fo) {
        LSPBindings.removeBackgroundTask(fo, this);
    }

    @Override
    public void run(LSPBindings bindings, FileObject file) {
        if (isCurrentFile(file)) {
            try {
                String uri = Utils.toURI(file);
                List<Either<SymbolInformation, DocumentSymbol>> symbols = bindings.getTextDocumentService().documentSymbol(new DocumentSymbolParams(new TextDocumentIdentifier(uri))).get();

                setKeys(symbols);
                expandAll();
            } catch (ExecutionException ex) {
                LOG.log(Level.FINE, null, ex);
                setKeys(Collections.emptyList());
            } catch (InterruptedException ex) {
                //try again:
                LSPBindings.addBackgroundTask(file, this);
            }
        } else {
            //ignore, should be called with the other file eventually.
        }
    }

    @Override
    protected Node[] createNodes(FileObject currentFile, Either<SymbolInformation, DocumentSymbol> sym) {
        return new Node[] {new NodeImpl(Utils.toURI(currentFile), sym)};
    }

    private static final class NodeImpl extends AbstractNode {

        private static Children createChildren(String currentFileUri, Either<SymbolInformation, DocumentSymbol> sym) {
            if (sym.isLeft()) {
                return LEAF;
            }
            return createChildren(currentFileUri, sym.getRight());
        }

        private static Children createChildren(String currentFileUri, DocumentSymbol sym) {
            if (sym.getChildren() == null || sym.getChildren().isEmpty()) {
                return LEAF;
            }
            return new Keys<DocumentSymbol>() {
                @Override
                protected void addNotify() {
                    setKeys(sym.getChildren());
                }

                @Override
                protected Node[] createNodes(DocumentSymbol sym) {
                    return new Node[] {
                        new NodeImpl(currentFileUri, sym)
                    };
                }

                @Override
                protected void removeNotify() {
                    setKeys(Collections.emptyList());
                }

            };
        }

        private static Action createOpenAction(String uri, Range range) {
            return new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    Utils.open(uri, range);
                }
            };
        }

        private final Action open;

        public NodeImpl(String currentFileUri, Either<SymbolInformation, DocumentSymbol> symbol) {
            super(createChildren(currentFileUri, symbol));
            if (symbol.isLeft()) {
                setDisplayName(symbol.getLeft().getName());
                setIconBaseWithExtension(Icons.getSymbolIconBase(symbol.getLeft().getKind()));
                this.open = createOpenAction(symbol.getLeft().getLocation().getUri(), symbol.getLeft().getLocation().getRange());
            } else {
                setDisplayName(symbol.getRight().getName());
                setIconBaseWithExtension(Icons.getSymbolIconBase(symbol.getRight().getKind()));
                this.open = createOpenAction(currentFileUri, symbol.getRight().getRange());
            }
        }

        public NodeImpl(String currentFileUri, DocumentSymbol symbol) {
            super(createChildren(currentFileUri, symbol));
            setDisplayName(symbol.getName());
            setIconBaseWithExtension(Icons.getSymbolIconBase(symbol.getKind()));
            this.open = createOpenAction(currentFileUri, symbol.getRange());
        }

        @Override
        public Action getPreferredAction() {
            return open;
        }

    }
}
