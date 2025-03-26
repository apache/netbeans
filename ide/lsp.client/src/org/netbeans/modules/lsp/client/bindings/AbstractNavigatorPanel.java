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
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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

abstract class AbstractNavigatorPanel<K> extends Children.Keys<K> implements NavigatorPanel, LookupListener {
    static final Logger LOG = Logger.getLogger(AbstractNavigatorPanel.class.getName());

    private final ExplorerManager manager;
    private View view;
    private Lookup.Result<FileObject> result;
    private FileObject file;
    private String displayName;

    @Messages("DN_Symbols=Symbols")
    public AbstractNavigatorPanel() {
        manager = new ExplorerManager();
        manager.setRootContext(new AbstractNode(this));
        this.displayName = Bundle.DN_Symbols();
    }

    abstract void addBackgroundTask(FileObject fo);
    abstract void removeBackgroundTask(FileObject fo);
    abstract Node[] createNodes(FileObject fo, K sym);

    @Override
    protected final Node[] createNodes(K key) {
        return createNodes(file, key);
    }

    final boolean isCurrentFile(FileObject fo) {
        return Objects.equals(file, fo);
    }

    final void expandAll() {
        SwingUtilities.invokeLater(view::expandAll);
    }

    @Override
    public synchronized final String getDisplayName() {
        return displayName;
    }

    public synchronized final void setDisplayName(String displayName) {
        if (displayName == null) {
            displayName = Bundle.DN_Symbols();
        }
        if (!Objects.equals(this.displayName, displayName)) {
            this.displayName = displayName;
        }
    }
    @Override
    public final String getDisplayHint() {
        return "symbols";
    }

    @Override
    public final JComponent getComponent() {
        if (view == null) {
            view = new View();
        }
        return view;
    }

    @Override
    public final void panelActivated(Lookup context) {
        result = context.lookupResult(FileObject.class);
        result.addLookupListener(this);
        updateFile();
    }

    @Override
    public final void panelDeactivated() {
        result.removeLookupListener(this);
        result = null;
        updateFile();
    }

    private void updateFile() {
        if (file != null) {
            removeBackgroundTask(file);
            setKeys(Collections.emptyList());
            file = null;
        }
        Collection<? extends FileObject> files = result != null ? result.allInstances() : Collections.emptyList();
        file = files.isEmpty() ? null : files.iterator().next();
        if (file != null) {
            addBackgroundTask(file);
        }
    }

    @Override
    public final Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public final void resultChanged(LookupEvent arg0) {
        updateFile();
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
