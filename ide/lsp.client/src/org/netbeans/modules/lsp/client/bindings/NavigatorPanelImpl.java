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
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
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
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class NavigatorPanelImpl extends Children.Keys<Either<SymbolInformation, DocumentSymbol>> implements NavigatorPanel, BackgroundTask, LookupListener {

    private static final NavigatorPanelImpl INSTANCE = new NavigatorPanelImpl();

    private final ExplorerManager manager;
    private JComponent view;
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
            class View extends JPanel implements ExplorerManager.Provider {

                public View() {
                    setLayout(new BorderLayout());
                    BeanTreeView btv = new BeanTreeView();
                    add(btv, BorderLayout.CENTER);

                    btv.setRootVisible(false);
                }

                @Override
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
            }
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
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            System.err.println("!!!");
        }
    }

    @Override
    protected Node[] createNodes(Either<SymbolInformation, DocumentSymbol> sym) {
        return new Node[] {new NodeImpl(sym)};
    }

    @Override
    public void resultChanged(LookupEvent arg0) {
        updateFile();
    }

    private static final class NodeImpl extends AbstractNode {

        private static Children createChildren(Either<SymbolInformation, DocumentSymbol> sym) {
            if (sym.isLeft()) {
                return LEAF;
            }
            return createChildren(sym.getRight());
        }

        private static Children createChildren(DocumentSymbol sym) {
            if (sym.getChildren().isEmpty()) {
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
                        new NodeImpl(sym)
                    };
                }

                @Override
                protected void removeNotify() {
                    setKeys(Collections.emptyList());
                }

            };
        }

        public NodeImpl(Either<SymbolInformation, DocumentSymbol> symbol) {
            super(createChildren(symbol));
            if (symbol.isLeft()) {
                setDisplayName(symbol.getLeft().getName());
                setIconBaseWithExtension(Icons.getSymbolIconBase(symbol.getLeft().getKind()));
            } else {
                setDisplayName(symbol.getRight().getName());
                setIconBaseWithExtension(Icons.getSymbolIconBase(symbol.getRight().getKind()));
            }
        }

        public NodeImpl(DocumentSymbol symbol) {
            super(createChildren(symbol));
            setDisplayName(symbol.getName());
            setIconBaseWithExtension(Icons.getSymbolIconBase(symbol.getKind()));
        }

    }

    @ServiceProvider(service=DynamicRegistration.class)
    public static final class DynamicRegistrationImpl implements DynamicRegistration {

        @Override
        public Collection<? extends NavigatorPanel> panelsFor(URI uri) {
            try {
                FileObject file = URLMapper.findFileObject(uri.toURL());
                if (file != null) {
                    return LSPBindings.getBindings(file) != null ? Collections.singletonList(INSTANCE) : Collections.emptyList();
                } else {
                    return Collections.emptyList();
                }
            } catch (MalformedURLException ex) {
                //ignore
                return Collections.emptyList();
            }
        }

    }
}
