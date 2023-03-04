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
package org.netbeans.modules.java.debug;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class TreeNavigatorProviderImpl implements NavigatorPanel {
    
    private JComponent panel;
    private final ExplorerManager manager = new ExplorerManager();
    
    /**
     * Default constructor for layer instance.
     */
    public TreeNavigatorProviderImpl() {
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    setHighlights(TreeNavigatorJavaSourceFactory.getInstance().getFile(), manager);
                }
            }
        });
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(TreeNavigatorProviderImpl.class, "NM_Trees");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(TreeNavigatorProviderImpl.class, "SD_Trees");
    }
    
    public JComponent getComponent() {
        if (panel == null) {
            final BeanTreeView view = new BeanTreeView();
            view.setRootVisible(true);
            view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            class Panel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
                // Make sure action context works correctly:
                private final Lookup lookup = ExplorerUtils.createLookup(manager, new ActionMap());
                {
                    setLayout(new BorderLayout());
                    add(view, BorderLayout.CENTER);
                }
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
                public Lookup getLookup() {
                    return lookup;
                }
            }
            panel = new Panel();
        }
        return panel;
    }
    
    public Lookup getLookup() {
        return null;
    }

    public void panelActivated(Lookup context) {
        TreeNavigatorJavaSourceFactory.getInstance().setLookup(context, new TaskImpl());
        TreeNavigatorJavaSourceFactory.CaretAwareFactoryImpl.getInstance().setTask(new SelectingTaskImpl());
    }

    public void panelDeactivated() {
        TreeNavigatorJavaSourceFactory.getInstance().setLookup(Lookup.EMPTY, null);
        TreeNavigatorJavaSourceFactory.CaretAwareFactoryImpl.getInstance().setTask(null);
        setHighlights(null, null);
    }

    static OffsetsBag getBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(TreeNavigatorProviderImpl.class);
                
        if (bag == null) {
            doc.putProperty(TreeNavigatorProviderImpl.class, bag = new OffsetsBag(doc));
        }
        
        return bag;
    }

    private static Reference<FileObject> lastFile = null;
    static synchronized void setHighlights(FileObject file, ExplorerManager manager) {
        FileObject last = lastFile != null ? lastFile.get() : null;
        Document lastDoc = documentFor(last);

        if (lastDoc != null) {
            getBag(lastDoc).clear();
        }

        Document doc = documentFor(file);

        if (doc == null) {
            return;
        }

        OffsetsBag bag = new OffsetsBag(doc, true);

        for (Node n : manager.getSelectedNodes()) {
            if (n instanceof OffsetProvider) {
                OffsetProvider p = (OffsetProvider) n;
                final int start = p.getStart();
                final int end = p.getEnd();
                final int pref = p.getPreferredPosition();

                if (start >= 0 && end >= 0) {
                    bag.addHighlight(start, end, HIGHLIGHT);
                }

                if (pref >= 0) {
                    bag.addHighlight(pref, pref+1, HIGHLIGHT_PREF);
                }
            }
        }

        getBag(doc).setHighlights(bag);

        if (last != file) {
            lastFile = new WeakReference<>(file);
        }
    }

    private static Document documentFor(FileObject file) {
        if (file == null) {
            return null;
        }

        EditorCookie ec = file.getLookup().lookup(EditorCookie.class);

        if (ec == null) {
            return null;
        }

        return ec.getDocument();
    }

    private static final AttributeSet HIGHLIGHT = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(220, 220, 220));
    private static final AttributeSet HIGHLIGHT_PREF = AttributesUtilities.createImmutable(StyleConstants.Underline, new Color(30, 255, 0));
    
    private final class TaskImpl implements CancellableTask<CompilationInfo> {

        private final AtomicBoolean cancel = new AtomicBoolean();

        public void cancel() {
            cancel.set(true);
        }

        public void run(CompilationInfo info) {
            cancel.set(false);
            
            Node tree = TreeNode.getTree(info, new TreePath(info.getCompilationUnit()), cancel);

            if (!cancel.get()) {
                manager.setRootContext(tree);
            }
        }
        
    }
    
    private final class SelectingTaskImpl implements CancellableTask<CompilationInfo> {

        public void cancel() {
        }

        public void run(CompilationInfo info) throws PropertyVetoException {
            int pos = CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject());
            TreePath tp = plainPathFor(info, pos);
            Node toSelect = tp != null ? TreeNode.findNode(manager.getRootContext(), tp) : null;

            if (toSelect != null) {
                manager.setExploredContext(toSelect);
                manager.setSelectedNodes(new Node[] {toSelect});
            } else {
                manager.setSelectedNodes(new Node[] {});
            }
        }

    }

    private static TreePath plainPathFor(final CompilationInfo info, int pos) {
        //TODO: TreeUtilities.pathFor handles error trees in a strange way - not sure if intentional, but unusable for the tree navigator
        class Result extends Error {
            TreePath path;
            Result(TreePath path) {
                this.path = path;
            }
        }

        class PathFinder extends ErrorAwareTreePathScanner<Void,Void> {
            private int pos;
            private SourcePositions sourcePositions;

            private PathFinder(int pos, SourcePositions sourcePositions) {
                this.pos = pos;
                this.sourcePositions = sourcePositions;
            }

            @Override
            public Void scan(Tree tree, Void p) {
                if (tree != null) {
                    if (sourcePositions.getStartPosition(getCurrentPath().getCompilationUnit(), tree) < pos && sourcePositions.getEndPosition(getCurrentPath().getCompilationUnit(), tree) >= pos) {
                        super.scan(tree, p);
                        throw new Result(new TreePath(getCurrentPath(), tree));
                    }
                }
                return null;
            }

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                int[] span = info.getTreeUtilities().findNameSpan(node);

                if (span != null && span[0] <= pos && pos < span[1]) {
                    throw new Result(getCurrentPath());
                }

                return super.visitVariable(node, p);
            }

            @Override
            public Void visitMethod(MethodTree node, Void p) {
                int[] span = info.getTreeUtilities().findNameSpan(node);

                if (span != null && span[0] <= pos && pos < span[1]) {
                    throw new Result(getCurrentPath());
                }

                return super.visitMethod(node, p);
            }
        }

        try {
            new PathFinder(pos, info.getTrees().getSourcePositions()).scan(new TreePath(info.getCompilationUnit()), null);
            return null;
        } catch (Result result) {
            return result.path;
        }
    }
}
