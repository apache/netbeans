/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.csl.navigation;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.StructureScanner.Configuration;
import org.netbeans.modules.csl.navigation.actions.FilterSubmenuAction;
import org.netbeans.modules.csl.navigation.actions.SortActionSupport.SortByNameAction;
import org.netbeans.modules.csl.navigation.actions.SortActionSupport.SortBySourceAction;
import org.netbeans.modules.csl.navigation.base.FiltersManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.csl.navigation.base.TapPanel;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 *
 * @author  phrebejk
 */
public class ClassMemberPanelUI extends javax.swing.JPanel
        implements ExplorerManager.Provider, FiltersManager.FilterChangeListener {

    private static RequestProcessor RP = new RequestProcessor(ClassMemberPanelUI.class);

    private ExplorerManager manager = new ExplorerManager();
    private MyBeanTreeView elementView;
    private TapPanel filtersPanel;
    private JLabel filtersLbl;
    private Lookup lookup;
    private ClassMemberFilters filters;
    
    private Action[] actions = new Action[0]; // General actions for the panel
    
    private class UpdateFilterState implements Runnable {
        private final Language language;
        
        public UpdateFilterState(Language language) {
            this.language = language;
        }
        
            @Override
            public void run() {
                // See http://www.netbeans.org/issues/show_bug.cgi?id=128985
                // We don't want filters for all languages. Hardcoded for now.
                boolean includeFilters = true;
                if (language != null && language.getStructure() != null) {
                    StructureScanner scanner = language.getStructure();
                    Configuration configuration = scanner.getConfiguration();
                    if (configuration != null) {
                        includeFilters = configuration.isFilterable();
                    
                    List<Action> newActions = new ArrayList<Action>();
                    
                    if (configuration.isSortable()) {
                        newActions.add(new SortByNameAction(filters));
                        newActions.add(new SortBySourceAction(filters));
                    }
                    
                        if (!includeFilters) {
                            //issue #132883 workaround
                            filters.disableFiltering = true;
                    } else {
                        filters.disableFiltering = false;
                        if(! newActions.isEmpty()) {
                            newActions.add(null);
                            newActions.add(new FilterSubmenuAction(filters));
                        }
                        }
                    
                    actions = newActions.toArray(new Action[newActions.size()]);
                    }
                }
            final boolean finalIncludeFilters = includeFilters;
                    SwingUtilities.invokeLater(new Runnable() {
                @Override
                        public void run() {
                    filtersPanel.setVisible(finalIncludeFilters);
                }
            });
        }
    };
    
    /** Creates new form ClassMemberPanelUi */
    public ClassMemberPanelUI(final Language language) {
        
        initComponents();
        
        // Tree view of the elements
        elementView = createBeanTreeView();        
        add(elementView, BorderLayout.CENTER);
               
        filters = new ClassMemberFilters( this );
        filters.getInstance().hookChangeListener(this);

        // See http://www.netbeans.org/issues/show_bug.cgi?id=186407
        // Making the calls to getStructure() out of AWT EDT
        RP.post(new UpdateFilterState(language));
        
                            // filters
                            filtersPanel = new TapPanel();
                            filtersLbl = new JLabel(NbBundle.getMessage(ClassMemberPanelUI.class, "LBL_Filter")); //NOI18N
                            filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
                            filtersPanel.add(filtersLbl);
                            filtersPanel.setOrientation(TapPanel.DOWN);
                            // tooltip
                            KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
                            String keyText = Utilities.keyToString(toggleKey);
                            filtersPanel.setToolTipText(NbBundle.getMessage(ClassMemberPanelUI.class, "TIP_TapPanel", keyText));
                            filtersPanel.add(filters.getComponent());
                            add(filtersPanel, BorderLayout.SOUTH);  
        manager.setRootContext(ElementNode.getWaitNode());
        
        lookup = ExplorerUtils.createLookup(manager, getActionMap());       
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        elementView.requestFocusInWindow();
        return result;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        elementView.requestFocus();
    }

    public org.openide.util.Lookup getLookup() {
        // XXX Check for chenge of FileObject
        return lookup;
    }
    
    public org.netbeans.modules.csl.navigation.ElementScanningTask getTask() {
        
        return new ElementScanningTask() {
            public @Override int getPriority() {
                return 20000;
            }
            public @Override Class<? extends Scheduler> getSchedulerClass() {
                return CSLNavigatorScheduler.class;
            }
            @Override public void run(final ParserResult result, final SchedulerEvent event) {
                runWithCancelService(new Runnable() {
                    @Override
                    public void run() {
                        resume();
                        StructureItem root = computeStructureRoot(result.getSnapshot().getSource());
                        FileObject file = result.getSnapshot().getSource().getFileObject();

                        if (root != null && file != null) {
                            Document doc = result.getSnapshot().getSource().getDocument(false);
                            BaseDocument bd = doc instanceof BaseDocument ? (BaseDocument)doc : null;
                            refresh(root, file, bd);
                        }
                    }
                });
            }
        };
        
    }
    
    
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               elementView.setRootVisible(true);
               manager.setRootContext(ElementNode.getWaitNode());
            } 
        });
    }
    
    /**
     * For a FileObject/source, holds a position/offset of the caret; the position should be selected
     * after parse.
     */
    private Map<FileObject, Integer> positionRequests = new WeakHashMap<FileObject, Integer>();

    public void selectElementNode(final ParserResult info, final int offset) {
        final ElementNode root = getRootNode();
        if ( root == null ) {
            return;
        }
        FileObject rootFo = root.getFileObject();
        FileObject sourceFo = info.getSnapshot().getSource().getFileObject();
        if (sourceFo != null && !sourceFo.equals(rootFo)) {
            // switching files; refresh should be fired by periodic scheduler
            synchronized (this) {
                positionRequests.put(sourceFo, offset);
            }
        } else {
            doSelectNodes(info, null, offset);
        }
    }
    
    private void doSelectNodes(final ParserResult info, final BaseDocument bd, final int offset) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                doSelectNodes0(info, bd, offset);
            }
        });
    }
    
    private void doSelectNodes0(ParserResult info, BaseDocument bd, int offset) {
        ElementNode node;
        final ElementNode rootNode = getRootNode();
        if (info != null && rootNode != null) {
            node = rootNode.getMimeRootNodeForOffset(info, offset);
        } else if (bd != null && rootNode != null) {
            node = rootNode.getMimeRootNodeForOffset(bd, offset);
        } else {
            return;
        }
        Node[] selectedNodes = manager.getSelectedNodes();
        if (!(selectedNodes != null && selectedNodes.length == 1 && selectedNodes[0] == node)) {
            try {
                manager.setSelectedNodes(new Node[]{ node == null ? getRootNode() : node });
            } catch (PropertyVetoException propertyVetoException) {
                Exceptions.printStackTrace(propertyVetoException);
            }
        }
    }

    public void refresh( final StructureItem description, final FileObject fileObject, 
            final BaseDocument bd) {
        final ElementNode rootNode = getRootNode();
        
        if ( rootNode != null && rootNode.getFileObject().equals( fileObject) ) {
            // update
            //System.out.println("UPDATE ======" + description.fileObject.getName() );
            final Runnable r = new Runnable() {
                public void run() {
                    long startTime = System.currentTimeMillis();
                    rootNode.updateRecursively( description );
                    long endTime = System.currentTimeMillis();
                    Logger.getLogger("TIMER").log(Level.FINE, "Navigator Merge",
                            new Object[] {fileObject, endTime - startTime});
                }
            };
            RP.post(r);
        } 
        else {
            //System.out.println("REFRES =====" + description.fileObject.getName() );
            // New fileobject => refresh completely
            Runnable r = new Runnable() {

                public void run() {
                    long startTime = System.currentTimeMillis();
                    elementView.setRootVisible(false);
                    elementView.setAutoWaitCursor(false);
                    manager.setRootContext(new ElementNode( description, ClassMemberPanelUI.this, fileObject ) );

                    int expandDepth = -1;
                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(fileObject.getMIMEType());
                    if (language != null && language.getStructure() != null) {
                        StructureScanner scanner = language.getStructure();
                        Configuration configuration = scanner.getConfiguration();
                        if (configuration != null) {
                            expandDepth = configuration.getExpandDepth();
                        }
                    }
                    
                    new UpdateFilterState(language).run();

                    final boolean scrollOnExpand = elementView.getScrollOnExpand();
                    elementView.setScrollOnExpand( false );
                    // impl hack: Node expansion is synced by VisualizerNode to the AWT thread, possibly delayed
                    expandNodeByDefaultRecursively(manager.getRootContext(), 0, expandDepth);
                    // set soe back only after all pending expansion events are processed:
                    Mutex.EVENT.writeAccess(new Runnable() {
                        @Override
                        public void run() {
                            elementView.setScrollOnExpand( scrollOnExpand );
                        }
                    });
                    elementView.setAutoWaitCursor(true);
                    long endTime = System.currentTimeMillis();
                    Logger.getLogger("TIMER").log(Level.FINE, "Navigator Initialization",
                            new Object[] {fileObject, endTime - startTime});

                    final Integer offset;
                    synchronized (ClassMemberPanelUI.this) {
                        offset = positionRequests.remove(fileObject);    
                    }
                    if (offset != null) {
                        doSelectNodes(null, bd, offset);
                    }
                }

            };
            RP.post(r);
        }
    }
    
    public void sort() {
        refreshRootRecursively();
    }
    
    public ClassMemberFilters getFilters() {
        return filters;
    }
    
    public void expandNode( Node n ) {
        elementView.expandNode(n);
    }

    private void expandNodeByDefaultRecursively(Node node) {
        // using 0, -1 since we cannot quickly resolve currentDepth
        expandNodeByDefaultRecursively(node, 0, -1);
    }

    private void expandNodeByDefaultRecursively(Node node, int currentDepth, int maxDepth) {
        if (maxDepth >= 0  &&  currentDepth >= maxDepth) {
            return;
        }
        if (! expandNodeByDefault (node)) {
            return;
        }
        expandNode(node);
        for (Node subNode : node.getChildren().getNodes()) {
            expandNodeByDefaultRecursively(subNode, currentDepth + 1, maxDepth);
        }
    }

    private boolean expandNodeByDefault(Node node) {
        if (isExpandedByDefault(node)) {
            expandNode(node);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Expand specified nodes. The nodes in 'expand' are expanded, nodes in 'expandRec'
     * are expanded recursively, if their default state allows expansion.
     * <p/>
     * The processing is forked to a private thread to avoid waiting for completion.
     *
     * @param expand nodes to expand (unconditionally)
     * @param expandRec nodes to expand recursively if they are expanded by default.
     */
    void performExpansion(final Collection<Node> expand, final Collection<Node> expandRec) {
        Runnable r = new Runnable() {
           public void run() {
               for (Node n : expand) {
                   expandNode(n);
               }

               for (Node n : expandRec) {
                   expandNodeByDefaultRecursively(n);
               }
           }
        };

        RP.post(r);
    }

    boolean isExpandedByDefault(Node node) {
        if (node instanceof ElementNode) {
            StructureItem item = ((ElementNode) node).getDescription();
            if (item instanceof StructureItem.CollapsedDefault  &&  ((StructureItem.CollapsedDefault) item).isCollapsedByDefault()) {
                return false;
            }
        }
        return true;
    }

    public Action[] getActions() {
        return actions;
    }
    
    public FileObject getFileObject() {
        return getRootNode().getFileObject();
    }
    
    // FilterChangeListener ----------------------------------------------------
    
    public void filterStateChanged(ChangeEvent e) {
        refreshRootRecursively();
    }

    private void refreshRootRecursively() {
        final ElementNode root = getRootNode();

         if ( root != null ) {
            RP.post(new Runnable() {
                public void run() {
                    root.refreshRecursively();
                }
            });
         }
     }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    // Private methods ---------------------------------------------------------
    
    private ElementNode getRootNode() {
        
        Node n = manager.getRootContext();
         if ( n instanceof ElementNode ) {
            return (ElementNode)n;
        }
        else {
            return null;
        }
    }
    
    private MyBeanTreeView createBeanTreeView() {
//        ActionMap map = getActionMap();
//        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
//        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
//        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
//        map.put("delete", new DelegatingAction(ActionProvider.COMMAND_DELETE, ExplorerUtils.actionDelete(manager, true)));
//        
        
        MyBeanTreeView btv = new MyBeanTreeView();    // Add the BeanTreeView        
//      btv.setDragSource (true);        
//      btv.setRootVisible(false);        
//      associateLookup( ExplorerUtils.createLookup(manager, map) );        
        return btv;
        
    }
    
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    
    private class MyBeanTreeView extends BeanTreeView {
        public boolean getScrollOnExpand() {
            return tree.getScrollsOnExpand();
}
        
        public void setScrollOnExpand( boolean scroll ) {
            this.tree.setScrollsOnExpand( scroll );
        }
    }
}
