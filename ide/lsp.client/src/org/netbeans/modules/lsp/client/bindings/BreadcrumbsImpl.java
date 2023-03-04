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
package org.netbeans.modules.lsp.client.bindings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.actions.Openable;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.LSPBindings.BackgroundTask;
import org.netbeans.modules.lsp.client.Utils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 *
 * @author lahvac
 */
public class BreadcrumbsImpl implements BackgroundTask {

    private static final RequestProcessor WORKER = new RequestProcessor(BreadcrumbsImpl.class.getName(), 1, false, false);
    private final JTextComponent comp;
    private final Document doc;
    private volatile RootBreadcrumbsElementImpl rootElement;

    public BreadcrumbsImpl(JTextComponent comp) {
        this.comp = comp;
        this.doc = comp.getDocument();
        this.comp.addCaretListener(evt -> {
            update();
        });
    }

    @Override
    public void run(LSPBindings bindings, FileObject file) {
        try {
            //TODO: modified while the query is running?
            List<Either<SymbolInformation, DocumentSymbol>> symbols = bindings.getTextDocumentService().documentSymbol(new DocumentSymbolParams(new TextDocumentIdentifier(Utils.toURI(file)))).get();

            this.rootElement = new RootBreadcrumbsElementImpl(file, doc, symbols.stream().map(this::toDocumentSymbol).collect(Collectors.toList()));

            SwingUtilities.invokeLater(() -> update());
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private DocumentSymbol toDocumentSymbol(Either<SymbolInformation, DocumentSymbol> variants) {
        if (variants.isRight()) return variants.getRight();

        SymbolInformation left = variants.getLeft();

        return new DocumentSymbol(left.getName(), left.getKind(), left.getLocation().getRange(), left.getLocation().getRange(), null, Collections.emptyList());
    }

    private void update() {
        BreadcrumbsElement element = this.rootElement;

        if (element == null) {
            return ;
        }

        Caret caret = comp.getCaret();

        if (caret != null && (!element.getChildren().isEmpty())) {
            element = element.getChildren().get(0);

            int caretPos = caret.getDot();

            OUTER:
            while (true) {
                for (BreadcrumbsElement child : element.getChildren()) {
                    BreadcrumbsElementImpl impl = (BreadcrumbsElementImpl) child;
                    if (impl.startPos.getOffset() <= caretPos && caretPos <= impl.endPos.getOffset()) {
                        element = child;
                        continue OUTER;
                    }
                }
                break;
            }

            BreadcrumbsController.setBreadcrumbs(doc, element);
        }
    }

    private static final class RootBreadcrumbsElementImpl implements BreadcrumbsElement {
        private final List<BreadcrumbsElement> children;

        public RootBreadcrumbsElementImpl(FileObject file, Document doc, List<DocumentSymbol> symbols) {
            this.children = Collections.singletonList(new FileBreadcrumbsElementImpl(file, doc, this, symbols));
        }

        @Override
        public String getHtmlDisplayName() {
            return "";
        }

        @Override
        public Image getIcon(int type) {
            return BreadcrumbsController.NO_ICON;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return BreadcrumbsController.NO_ICON;
        }

        @Override
        public List<BreadcrumbsElement> getChildren() {
            return children;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public BreadcrumbsElement getParent() {
            return null;
        }
    }

    private static final class FileBreadcrumbsElementImpl implements BreadcrumbsElement {
        private final FileObject file;
        private final BreadcrumbsElement root;
        private final List<BreadcrumbsElement> children;

        public FileBreadcrumbsElementImpl(FileObject file, Document doc, BreadcrumbsElement root, List<DocumentSymbol> symbols) {
            this.file = file;
            this.root = root;
            this.children = BreadcrumbsElementImpl.create(this, symbols, file, doc);
        }

        @Override
        public String getHtmlDisplayName() {
            return escape(file.getNameExt());
        }

        @Override
        public Image getIcon(int type) {
            try {
                return DataObject.find(file).getNodeDelegate().getIcon(type);
            } catch (DataObjectNotFoundException ex) {
                return BreadcrumbsController.NO_ICON;
            }
        }

        @Override
        public Image getOpenedIcon(int type) {
            try {
                return DataObject.find(file).getNodeDelegate().getOpenedIcon(type);
            } catch (DataObjectNotFoundException ex) {
                return BreadcrumbsController.NO_ICON;
            }
        }

        @Override
        public List<BreadcrumbsElement> getChildren() {
            return children;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public BreadcrumbsElement getParent() {
            return root;
        }

    }

    private static final class BreadcrumbsElementImpl implements BreadcrumbsElement {

        private final BreadcrumbsElement parent;
        private final DocumentSymbol symbol;
        private final Position startPos;
        private final Position endPos;
        private final List<BreadcrumbsElement> children;
        private final Lookup lookup;

        public BreadcrumbsElementImpl(BreadcrumbsElement parent, FileObject file, Document doc, DocumentSymbol symbol) throws BadLocationException {
            this.parent = parent;
            this.symbol = symbol;
            this.startPos = doc.createPosition(Utils.getOffset(doc, symbol.getRange().getStart()));
            this.endPos = doc.createPosition(Utils.getOffset(doc, symbol.getRange().getEnd()));
            this.children = create(this, symbol.getChildren(), file, doc);
            this.lookup = Lookups.fixed(new Openable() {
                @Override
                public void open() {
                    Utils.open(Utils.toURI(file), symbol.getRange());
                }
            });
        }


        @Override
        public String getHtmlDisplayName() {
            return escape(symbol.getName());
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(Icons.getSymbolIconBase(symbol.getKind()));
        }


        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public List<BreadcrumbsElement> getChildren() {
            return children;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }

        @Override
        public BreadcrumbsElement getParent() {
            return parent;
        }

        public static List<BreadcrumbsElement> create(BreadcrumbsElement parent, List<DocumentSymbol> symbols, FileObject file, Document doc) {
            if (symbols == null) {
                return Collections.emptyList();
            }
            return symbols.stream()
                          .map(c -> create(parent, file, doc, c))
                          .filter(e -> e != null)
                          .sorted((be1, be2) -> ((BreadcrumbsElementImpl) be1).startPos.getOffset() - ((BreadcrumbsElementImpl) be2).endPos.getOffset())
                          .collect(Collectors.toList());
        }

        private static BreadcrumbsElement create(BreadcrumbsElement parent, FileObject file, Document doc, DocumentSymbol symbol) {
            try {
                return new BreadcrumbsElementImpl(parent, file, doc, symbol);
            } catch (BadLocationException ex) {
                return null;
            }
        }
    }

    static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (CharConversionException ex) {
            }
        }
        return null;
    }

    public static SideBarFactory createSideBarFactory() {
        SideBarFactory delegate = BreadcrumbsController.createSideBarFactory();
        return new SideBarFactory() {
            @Override
            public JComponent createSideBar(JTextComponent target) {
                return new LSPBreadcrumbPanel(delegate, target);
            }
        };
    }

    private static class LSPBreadcrumbPanel extends JPanel implements PropertyChangeListener, ChangeListener {

        private final JTextComponent component;
        private final JComponent sidebar;

        public LSPBreadcrumbPanel(SideBarFactory delegate, JTextComponent component) {
            this.component = component;
            this.sidebar = delegate.createSideBar(component);
            setLayout(new BorderLayout());
            add(sidebar, BorderLayout.CENTER);
            sidebar.addPropertyChangeListener(this);
            LSPBindings.addChangeListener(this);
            update();
        }

        private void update() {
            WORKER.post(() -> {
                FileObject file = NbEditorUtilities.getFileObject(component.getDocument());
                LSPBindings bindings = file != null ? LSPBindings.getBindings(file) : null;
                Runnable r;

                if (bindings != null && Utils.isEnabled(bindings.getInitResult().getCapabilities().getDocumentSymbolProvider())) {
                    r = () -> {
                        setPreferredSize(sidebar.getPreferredSize());
                        setMaximumSize(sidebar.getMaximumSize());
                        revalidate();
                    };
                } else {
                    r = () -> {
                        setPreferredSize(new Dimension(0,0));
                        setMaximumSize(new Dimension(0,0));
                        revalidate();
                    };
                }

                SwingUtilities.invokeLater(r);
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();

            if (propertyName == null || "preferredSize".equals(propertyName) || "maximumSize".equals(propertyName)) {
                update();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            update();
        }
    }
}
