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

package org.netbeans.modules.java.navigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.actions.FilterSubmenuAction;
import org.netbeans.modules.java.navigation.actions.SortActions;
import org.netbeans.modules.java.navigation.base.FiltersManager;
import org.netbeans.modules.java.navigation.base.HistorySupport;
import org.netbeans.modules.java.navigation.base.Resolvers;
import org.netbeans.modules.java.navigation.base.SelectJavadocTask;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.java.navigation.base.TapPanel;
import org.netbeans.modules.java.navigation.base.Utils;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbPreferences;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 *
 * @author  phrebejk
 */
@SuppressWarnings("ClassWithMultipleLoggers")
public class ClassMemberPanelUI extends javax.swing.JPanel
        implements ExplorerManager.Provider, Lookup.Provider, FiltersManager.FilterChangeListener, PropertyChangeListener {

    private static final String JDOC_ICON = "org/netbeans/modules/java/navigation/resources/javadoc_open.png";          //NOI18N
    private static final String CMD_JDOC = "jdoc";  //NOI18N
    private static final String CMD_HISTORY = "history";    //NOI18N
    private static final int MIN_HISTORY_WIDTH = 50;
    private static final int HISTORY_HEIGHT = 20;
    private static final ThreadLocal<Boolean> ignoreJavaDoc = new ThreadLocal<>();

    private final ExplorerManager manager = new ExplorerManager();
    private final MyBeanTreeView elementView;
    private final TapPanel filtersPanel;
    private final InstanceContent selectedNodes = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(selectedNodes);
    private final ClassMemberFilters filters;
    private final AtomicReference<State> state = new AtomicReference<State>();    
    private final Action[] actions; // General actions for the panel
    private final SelectJavadocTask jdocFinder;
    private final RequestProcessor.Task watcherTask = WATCHER_RP.create(new Runnable() {
        @Override
        public void run() {
            final State current = state.get();
            if (current != State.DONE) {
                LOG.log(
                    Level.WARNING,
                    "No scheduled navigator update in {0}ms, current state: {1}",   //NOI18N
                    new Object[]{
                        WATCHER_TIME,
                        state.get()
                    });
            }
        }
    });
    private final RequestProcessor.Task jdocTask;
    private final HistorySupport history;
    private long lastShowWaitNodeTime = -1;
    //@GuardedBy this
    private Toolbar toolbar;
    private volatile boolean auto;

    private static final int JDOC_TIME = 500;
    private static final Logger LOG = Logger.getLogger(ClassMemberPanelUI.class.getName()); //NOI18N
    private static final Logger PERF_LOG = Logger.getLogger(ClassMemberPanelUI.class.getName() + ".perf"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ClassMemberPanelUI.class.getName(), 1);
    private static final RequestProcessor WATCHER_RP = new RequestProcessor(ClassMemberPanelUI.class.getName() + ".watcher", 1, false, false);  //NOI18N
    private static final int WATCHER_TIME = 30000;
    private static final String INHERITED_COLOR_KEY = "nb.navigator.inherited.color";   //NOI18N
    private static final String TYPE_COLOR_KEY = "nb.navigator.type.color";    //NOI18N
    private static final Color DEFAULT_TYPE_COLOR = new Color(0x70,0x70,0x70); //NOI18N
    private static final Color DEFAULT_INHERITED_COLOR = new Color(0x7D,0x69, 0x4A);    //NOI18N

    private final Color inheritedColor;
    private final Color typeColor;
    
    
    /** Creates new form ClassMemberPanelUi */
    public ClassMemberPanelUI() {
        inheritedColor = UIManager.getColor(INHERITED_COLOR_KEY);
        typeColor = UIManager.getColor(TYPE_COLOR_KEY);
        history = HistorySupport.getInstnace(this.getClass());
        jdocFinder = SelectJavadocTask.create(this);
        jdocTask = RP.create(jdocFinder);
        initComponents();
        manager.addPropertyChangeListener(this);
        
        // Tree view of the elements
        elementView = createBeanTreeView();        
        add(elementView, BorderLayout.CENTER);
               
        // filters
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(ClassMemberPanelUI.class, "TIP_TapPanel", keyText)); //NOI18N
        
        filters = new ClassMemberFilters( this );
        filters.getFiltersManager().hookChangeListener(this);
        JComponent buttons = filters.getComponent();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        Utils.updateBackground(filtersPanel);
        
        actions = new Action[] {            
            SortActions.createSortByNameAction(filters),
            SortActions.createSortBySourceAction(filters),
            null,
            new FilterSubmenuAction(filters.getFiltersManager())
        };
        
        add(filtersPanel, BorderLayout.SOUTH);        

        boolean expanded = NbPreferences.forModule(ClassMemberPanelUI.class).getBoolean("filtersPanelTap.expanded", true); //NOI18N
        filtersPanel.setExpanded(expanded);
        filtersPanel.addPropertyChangeListener(this);
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

    @Override
    public org.openide.util.Lookup getLookup() {
        // XXX Check for chenge of FileObject
        return lookup;
    }
    
    public org.netbeans.modules.java.navigation.ElementScanningTask getTask() {        
        return new ElementScanningTask(this);        
    }

    @NonNull
    String getInheritedColor() {
        return getHtmlColor(
           inheritedColor == null ?
           DEFAULT_INHERITED_COLOR :
           inheritedColor);
    }

    @NonNull
    String getTypeColor() {
        return getHtmlColor(
            typeColor == null ?
            DEFAULT_TYPE_COLOR :
            typeColor);
    }

    @NonNull
    private static String getHtmlColor(@NonNull final Color c) {
        final int r = c.getRed();
        final int g = c.getGreen();
        final int b = c.getBlue();
        final StringBuilder result = new StringBuilder();
        result.append ("#");        //NOI18N
        final String rs = Integer.toHexString (r);
        final String gs = Integer.toHexString (g);
        final String bs = Integer.toHexString (b);
        if (r < 0x10)
            result.append('0');
        result.append(rs);
        if (g < 0x10)
            result.append ('0');
        result.append(gs);
        if (b < 0x10)
            result.append ('0');
        result.append(bs);
        return result.toString();
    }
    
    
    void showWaitNode() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
               elementView.setRootVisible(true);
               manager.setRootContext(ElementNode.getWaitNode());
               lastShowWaitNodeTime = System.currentTimeMillis();
               scheduled();
            } 
        });
    }
    
    void clearNodes(final boolean resetAutoRefresh) {
        ClassMemberPanel.compareAndSetLastUsedFile(null);
        if (resetAutoRefresh) {
            auto = false;
        }
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
               elementView.setRootVisible(false);
               manager.setRootContext(new AbstractNode(Children.LEAF));
            } 
        });
    }
    
    private void scheduled() {
        state.set(State.SCHEDULED);
        boolean ae = false;
        assert ae = true;
        if (ae) {
            watcherTask.schedule(WATCHER_TIME);
        }
    }
    
    void start() {
        state.set(State.INVOKED);
    }
    
    private void done() {
        state.set(State.DONE);
        boolean ae = false;
        assert ae = true;
        if (ae) {
            watcherTask.cancel();
        }
    }
    
    public void selectNode(final Pair<ElementHandle<Element>,TreePathHandle> pattern ) {
        final ElementNode root = getRootNode();
        if ( root == null ) {
            return;
        }
        final ElementNode node = root.stream()
                .filter((n)-> {
                    final Description d = n.getDescription();
                    boolean match = true;
                    if (pattern.first() != null) {
                        match &= pattern.first().equals(d.getElementHandle());
                    }
                    if (pattern.second() != null) {
                        match &= pattern.second().equals(d.getTreePathHandle());
                    }
                    return match;
                })
                .findFirst()
                .orElse(null);
        ignoreJavaDoc.set(true);
        try {
            manager.setSelectedNodes(new Node[]{ node == null ? root : node });
        } catch (PropertyVetoException propertyVetoException) {
            Exceptions.printStackTrace(propertyVetoException);
        } finally {
            ignoreJavaDoc.remove();
        }
    }
    
    public void setContext(
            @NonNull final JavaSource js,
            @NullAllowed JTextComponent target) {
        final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver =
                target == null ?
                Resolvers.createFileResolver(js) :
                Resolvers.createEditorResolver(
                    js,
                    target.getCaret().getDot());
        schedule(resolver);
    }

    synchronized JComponent getToolbar() {
        if (toolbar == null) {
            toolbar = new Toolbar();
        }
        return toolbar;
    }

    void refresh() {
        RP.execute(new Runnable() {
            @Override
            public void run() {
                ElementNode rootNode = getRootNode();
                if (rootNode != null) {
                    final FileObject fo = rootNode.getDescription().fileObject;
                    if (fo != null) {
                        final JavaSource js = JavaSource.forFileObject(fo);
                        if (js != null) {
                            try {
                                js.runUserActionTask(new Task<CompilationController>() {
                                    @Override
                                    public void run(CompilationController parameter) throws Exception {
                                        parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                        getTask().runImpl(parameter, true);
                                    }
                                }, true);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        });
    }

    void refresh(
            @NonNull final Description description,
            final boolean userAction) {
        auto = !userAction;
        final ElementNode rootNode = getRootNode();
        
        if ( rootNode != null && rootNode.getDescription().fileObject.equals( description.fileObject) ) {
            // update
            //System.out.println("UPDATE ======" + description.fileObject.getName() );
            jdocTask.cancel();
            jdocFinder.cancel();
            RP.post(new Runnable() {
                public void run() {
                    rootNode.updateRecursively( description );
                    if (!userAction) {
                        toolbar.setAuto();
                    }
                    done();
                }
            } );            
        } else {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    elementView.setRootVisible(false);        
                    manager.setRootContext(new ElementNode( description ) );
                    if (!userAction) {
                        toolbar.setAuto();
                    }
                    done();
                    boolean scrollOnExpand = getScrollOnExpand();
                    setScrollOnExpand( false );
                    elementView.setAutoWaitCursor(false);
                    elementView.expandAll();
                    elementView.setAutoWaitCursor(true);
                    setScrollOnExpand( scrollOnExpand );

                    if (PERF_LOG.isLoggable(Level.FINE)) {
                        final long tm2 = System.currentTimeMillis();
                        final long tm1 = lastShowWaitNodeTime;
                        if (tm1 != -1) {
                            lastShowWaitNodeTime = -1;
                            PERF_LOG.log(Level.FINE,
                                String.format("ClassMemberPanelUI refresh took: %d ms", (tm2 - tm1)),
                                new Object[] { description.getFileObject().getName(), (tm2 - tm1) });
                        }
                    }
                }
            } );
            
        }
    }

    boolean isAutomaticRefresh() {
        return auto;
    }
    
    public void sort() {
        ElementNode root = getRootNode();
        if( null != root )
            root.refreshRecursively();
    }
    
    public ClassMemberFilters getFilters() {
        return filters;
    }
    
    public void expandNode( Node n ) {
        elementView.expandNode(n);
    }
    
    public Action[] getActions() {
        return actions;
    }
    
    public FileObject getFileObject() {
        final ElementNode root = getRootNode();
        if (root != null) {
            return root.getDescription().fileObject;
        }
        else {
            return null;
        }        
    }
    
    // FilterChangeListener ----------------------------------------------------
    
    public void filterStateChanged(ChangeEvent e) {
        ElementNode root = getRootNode();
        
        if ( root != null ) {
            root.refreshRecursively();
        }
    }
    
    boolean getScrollOnExpand() {
        return null == elementView ? true : elementView.getScrollOnExpand();
    }
    
    void setScrollOnExpand( boolean scroll ) {
        if( null != elementView )
            elementView.setScrollOnExpand( scroll );
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
        return new MyBeanTreeView();
    }

    private void scheduleJavadocRefresh(final int time) {
        jdocFinder.cancel();
        jdocTask.schedule(time);
    }

    private void schedule(@NonNull final Callable<Pair<URI, ElementHandle<TypeElement>>> resolver) {
        showWaitNode();
        final Future<Pair<URI, ElementHandle<TypeElement>>> becomesHandle = RP.submit(resolver);
        final RefreshTask refresh = new RefreshTask(becomesHandle);
        RP.execute(refresh);
    }
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private ElementJavadoc getJavaDocFor(
            @NonNull final ElementNode node,
            @NullAllowed final Callable<Boolean> cancel) {
        ElementNode root = getRootNode();
        if ( root == null ) {
            return null;
        }
        final ElementHandle<? extends Element> eh = node.getDescription().getElementHandle();
        if (eh == null) {
            return null;
        }
        final JavaSource js = JavaSource.forFileObject( root.getDescription().fileObject );
        if (js == null) {
            return null;
        }
        final JavaDocCalculator calculator = new JavaDocCalculator(eh, cancel);
        try {
            js.runUserActionTask( calculator, true );
        } catch( IOException ioE ) {
            Exceptions.printStackTrace( ioE );
            return null;
        }
        return calculator.doc;
    }

    private static class JavaDocCalculator implements Task<CompilationController> {

        private final ElementHandle<? extends Element> handle;
        private final Callable<Boolean> cancel;
        private ElementJavadoc doc;
        
        public JavaDocCalculator(
                @NonNull final ElementHandle<? extends Element> handle,
                @NullAllowed final Callable<Boolean> cancel) {
            this.handle = handle;
            this.cancel = cancel;
        }


        @Override
        public void run(CompilationController cc) throws Exception {
            cc.toPhase( JavaSource.Phase.UP_TO_DATE );
            
            Element e = handle.resolve( cc );
            doc = ElementJavadoc.create(cc, e, cancel );
        }
    };
        
    private class MyBeanTreeView extends BeanTreeView implements ToolTipManagerEx.ToolTipProvider {

        private final ToolTipManagerEx toolTipManager;

        public MyBeanTreeView() {
            toolTipManager = new ToolTipManagerEx( this );
        }

        public boolean getScrollOnExpand() {
            return tree.getScrollsOnExpand();
}
        
        public void setScrollOnExpand( boolean scroll ) {
            this.tree.setScrollsOnExpand( scroll );
        }

        @Override
        public JComponent getComponent() {
            return tree;
        }

        @Override
        public String getToolTipText(@NonNull Node node) {
            ElementJavadoc doc = getDocumentation(node);
            return null == doc ? null : doc.getText();
        }


        @CheckForNull
        @Override
        public Node findNode(@NonNull final Point loc) {
            final TreePath path = tree.getPathForLocation( loc.x, loc.y );
            if( null == path ) {
                return null;
            }
            final Node node = Visualizer.findNode( path.getLastPathComponent());
            if (!(node instanceof ElementNode)) {
                return null;
            }
            final ElementNode enode = (ElementNode) node;
            final ElementNode.Description desc = enode.getDescription();
            //Other and module do not have javadoc
            return desc.kind != ElementKind.OTHER
                && desc.kind != ElementKind.MODULE ?
                    node :
                    null;
        }


        @CheckForNull
        private ElementJavadoc getDocumentation(@NullAllowed final Node node) {
            if( node instanceof ElementNode ) {
                return getJavaDocFor((ElementNode)node, toolTipManager);
            }
            return null;
        }

        @Override
        public Rectangle getToolTipSourceBounds(Point loc) {
            ElementNode root = getRootNode();
            if ( root == null ) {
                return null;
            }
            TreePath path = tree.getPathForLocation( loc.x, loc.y );
            return null == path ? null : tree.getPathBounds( path );
        }

        @Override
        public Point getToolTipLocation( Point mouseLocation, Dimension tipSize ) {
            Point screenLocation = getLocationOnScreen();
            Rectangle sBounds = getGraphicsConfiguration().getBounds();
            Dimension compSize = getSize();
            Point res = new Point();
            Rectangle tooltipSrcRect = getToolTipSourceBounds( mouseLocation );
            //May be null, prevent the NPE, nothing will be shown anyway.
            if (tooltipSrcRect == null) {
                tooltipSrcRect = new Rectangle();
            }

            Point viewPosition = getViewport().getViewPosition();
            screenLocation.x -= viewPosition.x;
            screenLocation.y -= viewPosition.y;

            //first try bottom right
            res.x = screenLocation.x + compSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y+tooltipSrcRect.height;

            if( res.x + tipSize.width <= sBounds.x+sBounds.width
                    && res.y + tipSize.height <= sBounds.y+sBounds.height ) {
                return res;
            }

            //upper right
            res.x = screenLocation.x + compSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y - tipSize.height;

            if( res.x + tipSize.width <= sBounds.x+sBounds.width
                    && res.y >= sBounds.y ) {
                return res;
            }

            //lower left
            res.x = screenLocation.x - tipSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y;

            if( res.x >= sBounds.x
                    && res.y + tipSize.height <= sBounds.y+sBounds.height ) {
                return res;
            }

            //upper left
            res.x = screenLocation.x - tipSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height - tipSize.height;

            if( res.x >= sBounds.x && res.y >= sBounds.y ) {
                return res;
            }

            //give up (who's got such a small display anyway?)
            res.x = screenLocation.x + tooltipSrcRect.x;
            if( sBounds.y + sBounds.height - (screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height) 
                > screenLocation.y + tooltipSrcRect.y - sBounds.y ) {
                res.y = screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height;
            } else {
                res.y = screenLocation.y + tooltipSrcRect.y - tipSize.height;
            }

            return res;
        }

        @Override
        public void invokeUserAction(final MouseEvent me) {
            Mutex.EVENT.readAccess( new Runnable() {
                @Override
                public void run() {
                    if( null != me ) {
                        final Node node = findNode(me.getPoint());
                        if (node != null) {
                            final ElementJavadoc doc = getDocumentation(node);
                            final ElementNode root = getRootNode();
                            final FileObject owner = root == null ? null : root.getDescription().fileObject;
                            JavadocTopComponent tc = JavadocTopComponent.findInstance();
                            if( null != tc ) {
                                tc.open();
                                tc.setJavadoc(owner,  doc);
                                tc.requestActive();
                            }
                        }
                    }
                }
            });
        }

        //#123940 start
        private boolean inHierarchy;
        private boolean doExpandAll;
        
        @Override
        public void addNotify() {
            super.addNotify();
            
            inHierarchy = true;
            
            if (doExpandAll) {
                super.expandAll();
                doExpandAll = false;
            }
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            inHierarchy = false;
            this.toolTipManager.hideTipWindow();
        }

        @Override
        public void expandAll() {
            super.expandAll();
            
            if (!inHierarchy) {
                doExpandAll = true;
            }
        }
        //#123940 end
        
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            final Node[] oldNodes = (Node[]) evt.getOldValue();
            final Node[] newNodes = (Node[]) evt.getNewValue();
            for (Node n : oldNodes) {
                selectedNodes.remove(n);
            }
            for (Node n : newNodes) {
                selectedNodes.add(n);
            }
            final boolean javadocDone = ignoreJavaDoc.get() == Boolean.TRUE;
            RP.execute(() -> {
                if (newNodes.length > 0 && !javadocDone && JavadocTopComponent.shouldUpdate()) {
                    scheduleJavadocRefresh(JDOC_TIME);
                }
            });
        } else if (TapPanel.EXPANDED_PROPERTY.equals(evt.getPropertyName())) {
            NbPreferences.forModule(ClassMemberPanelUI.class)
                    .putBoolean("filtersPanelTap.expanded", filtersPanel.isExpanded());
        }
    }
    
    private enum State {
        SCHEDULED,
        INVOKED,
        DONE
    }

    private class RefreshTask implements Runnable, Task<CompilationController> {

        private final Future<Pair<URI,ElementHandle<TypeElement>>> becomesHandle;

        RefreshTask(@NonNull final Future<Pair<URI,ElementHandle<TypeElement>>> becomesHandle) {
            assert becomesHandle != null;
            this.becomesHandle = becomesHandle;
        }

        @Override
        @NbBundle.Messages({
        "ERR_Cannot_Resolve_File=Cannot resolve type: {0}.",
        "ERR_Not_Declared_Type=Not a declared type."})
        public void run() {
            try {
                final Pair<URI,ElementHandle<TypeElement>> handlePair = becomesHandle.get();
                if (handlePair != null) {
                    final FileObject target = URLMapper.findFileObject(handlePair.first().toURL());
                    if (target != null) {
                        final JavaSource targetJs = JavaSource.forFileObject(target);
                        if (targetJs != null) {
                            history.addToHistory(handlePair);
                            targetJs.runUserActionTask(this, true);
                            ((Toolbar)getToolbar()).select(handlePair);
                        } else {
                            clearNodes(true);
                            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Cannot_Resolve_File(
                                handlePair.second().getQualifiedName()));
                        }
                    } else {
                        clearNodes(true);
                        StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Cannot_Resolve_File(
                                handlePair.second().getQualifiedName()));
                    }
                } else {
                    clearNodes(true);
                    StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Not_Declared_Type());
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void run(@NonNull final CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            getTask().runImpl(cc, true);
        }

    }

    private class Toolbar extends JPanel implements ActionListener, ListDataListener {

        private final JComboBox historyCombo;
        private boolean ignoreEvents;

        @NbBundle.Messages({        
        "TXT_InspectMembersHistoryEmpty=<empty>",
        "TXT_InspectMembersHistoryAuto=<auto>",
        "TOOLTIP_OpenJDoc=Open Javadoc Window",
        "TOOLTIP_InspectMembersHistory=Inspect Members History"
        })
        Toolbar() {
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            final JToolBar toolbar = new NoBorderToolBar(JToolBar.HORIZONTAL);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            historyCombo = new JComboBox(HistorySupport.createModel(history, Bundle.TXT_InspectMembersHistoryEmpty()));
            historyCombo.setMinimumSize(new Dimension(MIN_HISTORY_WIDTH,HISTORY_HEIGHT));
            historyCombo.setRenderer(HistorySupport.createRenderer(history));
            historyCombo.setActionCommand(CMD_HISTORY);
            historyCombo.addActionListener(this);
            historyCombo.getModel().addListDataListener(this);
            historyCombo.setEnabled(false);
            historyCombo.setToolTipText(Bundle.TOOLTIP_InspectMembersHistory());
            final JButton jdocButton = new JButton(ImageUtilities.loadImageIcon(JDOC_ICON, true));
            jdocButton.setActionCommand(CMD_JDOC);
            jdocButton.addActionListener(this);
            jdocButton.setFocusable(false);
            jdocButton.setToolTipText(Bundle.TOOLTIP_OpenJDoc());
            toolbar.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.insets = new Insets(0, 0, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 0;
            toolbar.add(historyCombo, c);

            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = 0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.insets = new Insets(0, 0, 0, 0);
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.weighty = 0;
            toolbar.add(jdocButton, c);

            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.insets = new Insets(0, 0, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 0;
            add(toolbar,c);
            Utils.updateBackground(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert SwingUtilities.isEventDispatchThread();
            if (CMD_JDOC.equals(e.getActionCommand())) {
                final TopComponent win = JavadocTopComponent.findInstance();
                if (win != null && !win.isShowing()) {
                    win.open();
                    win.requestVisible();
                    scheduleJavadocRefresh(0);
                }
            } else if (!ignoreEvents && CMD_HISTORY.equals(e.getActionCommand())) {
                final Object selItem = historyCombo.getSelectedItem();
                if (selItem instanceof Pair) {
                    final Pair<URI,ElementHandle<TypeElement>> pair = (Pair<URI,ElementHandle<TypeElement>>)selItem;
                    schedule(new Callable<Pair<URI, ElementHandle<TypeElement>>>() {
                        @Override
                        public Pair<URI, ElementHandle<TypeElement>> call() throws Exception {
                            return pair;
                        }
                    });
                }
            }
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            showHistory();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            showHistory();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            showHistory();
        }

        void select(@NonNull final Pair<URI,ElementHandle<TypeElement>> pair) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    ignoreEvents = true;
                    try {
                        historyCombo.getModel().setSelectedItem(pair);
                    } finally {
                        ignoreEvents = false;
                    }
                }
            });
        }

        void setAuto() {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    if (historyCombo.isEnabled()) {
                        historyCombo.getModel().setSelectedItem(Bundle.TXT_InspectMembersHistoryAuto());
                    }
                }
            });
        }

        private void showHistory() {
            if (!history.getHistory().isEmpty()) {
                historyCombo.setEnabled(true);
            }
        }        

    }
}
