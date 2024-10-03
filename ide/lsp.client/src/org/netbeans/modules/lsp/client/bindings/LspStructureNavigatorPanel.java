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

import java.util.Collection;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.text.StyledDocument;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.lsp.StructureProvider;
import org.openide.awt.Actions;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

@NbBundle.Messages(value = {
    "# {0} - name of the file",
    "CTL_StructureForFile=Structure for {0}",
    "# {0} - mime type",
    "CTL_StructureForMimeType=Structure for {0}"
})
final class LspStructureNavigatorPanel extends AbstractNavigatorPanel<StructureElement> {
    static final LspStructureNavigatorPanel INSTANCE = new LspStructureNavigatorPanel();
    private static final RequestProcessor BACKGROUND = new RequestProcessor(LspStructureNavigatorPanel.class);
    private RequestProcessor.Task reparseTask;

    @Override
    void addBackgroundTask(FileObject fo) {
        removeBackgroundTask(fo);
        reparseTask = BACKGROUND.post(() -> refreshStructure(fo));
    }

    @Override
    void removeBackgroundTask(FileObject fo) {
        if (reparseTask != null) {
            reparseTask.cancel();
        }
    }

    void refreshStructure(FileObject fo) {
        LOG.log(Level.INFO, "panelActivated: {0}", fo);
        EditorCookie ec = Utils.lookupForFile(fo, EditorCookie.class);
        if (ec != null) {
            StyledDocument doc = ec.getDocument();
            if (doc != null) {
                for (StructureProvider sp : MimeLookup.getLookup(fo.getMIMEType()).lookupAll(StructureProvider.class)) {
                    setKeys(sp.getStructure(doc));
                    return;
                }
            }
        }
    }

    @Override
    Node[] createNodes(FileObject fo, StructureElement key) {
        return new Node[]{new StructureElementNode(key, fo)};
    }
    private static final class StructureElementChildren extends Children.Keys<StructureElement> {

        private final FileObject fo;

        StructureElementChildren(FileObject fo) {
            this.fo = fo;
        }

        static Children childrenFor(Collection<? extends StructureElement> elements, FileObject fo) {
            if (elements == null) {
                return Children.LEAF;
            } else {
                StructureElementChildren ch = new StructureElementChildren(fo);
                ch.setKeys(elements);
                return ch;
            }
        }

        @Override
        protected Node[] createNodes(StructureElement key) {
            return new Node[]{new StructureElementNode(key, fo)};
        }
    }

    private static final class StructureElementNode extends AbstractNode {

        private final FileObject fo;

        StructureElementNode(StructureElement e, FileObject fo) {
            this(e, fo, new InstanceContent());
        }

        private StructureElementNode(StructureElement e, FileObject fo, InstanceContent ic) {
            super(StructureElementChildren.childrenFor(e.getChildren(), fo), new AbstractLookup(ic));
            this.fo = fo;
            setName(e.getName());
            setShortDescription(e.getDetail());
            setIconBaseWithExtension(Icons.getSymbolIconBase(e.getKind()));
            Openable open = () -> {
                EditorCookie ec = Utils.lookupForFile(fo, EditorCookie.class);
                if (ec != null) {
                    StyledDocument doc = ec.getDocument();
                    if (doc != null) {
                        int lineNumber = NbDocument.findLineNumber(doc, e.getSelectionStartOffset());
                        LineCookie lines = Utils.lookupForFile(fo, LineCookie.class);
                        if (lines != null) {
                            Line at = lines.getLineSet().getOriginal(lineNumber);
                            if (at != null) {
                                at.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            }
                        }
                    }
                }
            };
            ic.add(open);
        }

        @Override
        public Action getPreferredAction() {
            return Actions.forID("System", "org.openide.actions.OpenAction");
        }
    }

}
