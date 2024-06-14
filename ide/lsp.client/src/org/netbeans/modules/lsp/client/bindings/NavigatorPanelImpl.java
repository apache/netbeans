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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.LSPBindings.BackgroundTask;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class NavigatorPanelImpl extends Children.Keys<Either<SymbolInformation, DocumentSymbol>> implements NavigatorPanel, BackgroundTask, LookupListener {

    private static final Logger LOG = Logger.getLogger(NavigatorPanelImpl.class.getName());
    static final NavigatorPanelImpl INSTANCE = new NavigatorPanelImpl();

    private final ExplorerManager manager;
    private View view;
    private Lookup.Result<FileObject> result;
    private FileObject file;

    public NavigatorPanelImpl() {
        manager = new ExplorerManager();
        manager.setRootContext(new AbstractNode(this));
    }

    @Override
    @Messages("DN_Symbols=Symbols")
    public String getDisplayName() {
        return Bundle.DN_Symbols();
    }

    @Override
    public String getDisplayHint() {
        return "symbols";
    }

    @Override
    public JComponent getComponent() {
        if (view == null) {
            view = new View();
        }
        return view;
    }

    @Override
    public void panelActivated(Lookup context) {
        result = context.lookupResult(FileObject.class);
        result.addLookupListener(this);
        updateFile();
    }

    @Override
    public void panelDeactivated() {
        result.removeLookupListener(this);
        result = null;
        updateFile();
    }

    private void updateFile() {
        if (file != null) {
            LSPBindings.removeBackgroundTask(file, this);
            setKeys(Collections.emptyList());
            file = null;
        }
        Collection<? extends FileObject> files = result != null ? result.allInstances() : Collections.emptyList();
        file = files.isEmpty() ? null : files.iterator().next();
        if (file != null) {
            LSPBindings.addBackgroundTask(file, this);
        }
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public void run(LSPBindings bindings, FileObject file) {
        if (file.equals(this.file)) {
            try {
                String uri = Utils.toURI(file);
                List<Either<SymbolInformation, DocumentSymbol>> symbols = bindings.getTextDocumentService().documentSymbol(new DocumentSymbolParams(new TextDocumentIdentifier(uri))).get();

                setKeys(symbols);
                
                SwingUtilities.invokeLater(() -> view.expandAll());
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
    protected Node[] createNodes(Either<SymbolInformation, DocumentSymbol> sym) {
        return new Node[] {new NodeImpl(Utils.toURI(file), sym)};
    }

    @Override
    public void resultChanged(LookupEvent arg0) {
        updateFile();
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

    private class View extends JPanel implements ExplorerManager.Provider {

        private final BeanTreeView internalView;

        public View() {
            setLayout(new BorderLayout());
            this.internalView = new BeanTreeView();
            add(internalView, BorderLayout.CENTER);

            internalView.setRootVisible(false);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        public void expandAll() {
            boolean scrollsOnExpand = internalView.getScrollsOnExpand();
            internalView.setScrollsOnExpand(false);
            internalView.expandAll();
            internalView.setScrollsOnExpand(scrollsOnExpand);
        }
    }

}
