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
import java.awt.EventQueue;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.spi.lsp.StructureProvider;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages(value = {
    "# {0} - name of the file",
    "CTL_StructureForFile=Structure for {0}",
    "# {0} - mime type",
    "CTL_StructureForMimeType=Structure for {0}"
})
final class LspStructureNavigatorPanel extends JPanel implements NavigatorPanel, ExplorerManager.Provider {

    private static final Logger LOG = Logger.getLogger(LspStructureNavigatorPanel.class.getName());
    static final LspStructureNavigatorPanel INSTANCE = new LspStructureNavigatorPanel();
    private final ExplorerManager em;
    private FileObject fo;

    private LspStructureNavigatorPanel() {
        this.em = new ExplorerManager();
        setLayout(new BorderLayout());
        final BeanTreeView view = new BeanTreeView();
        view.setRootVisible(false);
        add(BorderLayout.CENTER, view);
    }

    @Override
    public String getDisplayName() {
        return Bundle.CTL_StructureForFile(fo.getNameExt());
    }

    @Override
    public String getDisplayHint() {
        return Bundle.CTL_StructureForMimeType(fo.getMIMEType());
    }

    @Override
    public JComponent getComponent() {
        assert EventQueue.isDispatchThread();
        return this;
    }

    @Override
    public void panelActivated(Lookup context) {
        assert EventQueue.isDispatchThread();
        fo = context.lookup(FileObject.class);
        if (fo != null) {
            LOG.log(Level.INFO, "panelActivated: {0}", fo);
            EditorCookie ec = context.lookup(EditorCookie.class);
            if (ec != null) {
                StyledDocument doc = ec.getDocument();
                if (doc != null) {
                    for (StructureProvider sp : MimeLookup.getLookup(fo.getMIMEType()).lookupAll(StructureProvider.class)) {
                        Children ch = StructureElementChildren.childrenFor(sp.getStructure(doc));
                        em.setRootContext(new AbstractNode(ch));
                        return;
                    }
                }
            }
        }
        em.setRootContext(Node.EMPTY);
    }

    @Override
    public void panelDeactivated() {
        assert EventQueue.isDispatchThread();
        LOG.log(Level.INFO, "panelDeactivated: {0}", fo);
        fo = null;
    }

    @Override
    public Lookup getLookup() {
        return null;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    private static final class StructureElementChildren extends Children.Keys<StructureElement> {

        static Children childrenFor(Collection<? extends StructureElement> elements) {
            if (elements == null) {
                return Children.LEAF;
            } else {
                StructureElementChildren ch = new StructureElementChildren();
                ch.setKeys(elements);
                return ch;
            }
        }

        @Override
        protected Node[] createNodes(StructureElement key) {
            return new Node[]{new StructureElementNode(key)};
        }
    }

    private static final class StructureElementNode extends AbstractNode {

        StructureElementNode(StructureElement e) {
            super(StructureElementChildren.childrenFor(e.getChildren()));
            setName(e.getName());
            setShortDescription(e.getDetail());
        }
    }

}
