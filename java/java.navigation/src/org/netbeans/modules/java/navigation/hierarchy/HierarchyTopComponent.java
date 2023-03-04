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
package org.netbeans.modules.java.navigation.hierarchy;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.navigation.JavadocTopComponent;
import org.netbeans.modules.java.navigation.NoBorderToolBar;
import org.netbeans.modules.java.navigation.base.HistorySupport;
import org.netbeans.modules.java.navigation.base.Resolvers;
import org.netbeans.modules.java.navigation.base.SelectJavadocTask;
import org.netbeans.modules.java.navigation.base.TapPanel;
import org.netbeans.modules.java.navigation.base.Utils;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 * @author Tomas Zezula
 */
@ConvertAsProperties(
    dtd = "-//org.netbeans.modules.java.navigation.hierarchy//Hierarchy//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "JavaHierarchyTopComponent",
iconBase="org/netbeans/modules/java/navigation/resources/hierarchy_window.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
@Messages({
    "CTL_HierarchyTopComponent=Hierarchy",
    "HINT_HierarchyTopComponent=This is a Hierarchy window"
})
public final class HierarchyTopComponent extends TopComponent implements ExplorerManager.Provider, ActionListener, PropertyChangeListener, ListDataListener {

    private static final int NOW = 0;
    private static final int JDOC_TIME = 500;
    private static final int COMBO_HEIGHT = 20;
    private static final int MIN_HISTORY_WIDTH = 50;
    private static final int MIN_TYPE_WIDTH = 100;
    private static final Logger LOG = Logger.getLogger(HierarchyTopComponent.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(HierarchyTopComponent.class);
    @StaticResource
    private static final String REFRESH_ICON = "org/netbeans/modules/java/navigation/resources/hierarchy_refresh.png";  //NOI18N
    @StaticResource
    private static final String JDOC_ICON = "org/netbeans/modules/java/navigation/resources/javadoc_open.png";          //NOI18N
    private static final String NON_ACTIVE_CONTENT = "non-active-content";  //NOI18N
    private static final String ACTIVE_CONTENT = "active-content";  //NOI18N
    private static final String PROP_LOWER_TOOLBAR_EXPANDED = "filtersPanelTap.expanded"; //NOI18N
    
    private static HierarchyTopComponent instance;

    private final SelectJavadocTask jdocFinder;
    private final RequestProcessor.Task jdocTask;
    private final ExplorerManager explorerManager;
    private final InstanceContent selectedNodes;
    private final Lookup lookup;
    private final Container contentView;
    private final JLabel nonActiveInfo;
    private final BeanTreeView btw;
    private final TapPanel lowerToolBar;
    private final JComboBox viewTypeCombo;
    private final JComboBox historyCombo;
    private final JButton refreshButton;
    private final JButton jdocButton;
    private final HierarchyFilters filters;
    private final RootChildren rootChildren;
    private final HistorySupport history;

    @NbBundle.Messages({
        "TXT_NonActiveContent=<No View Available - Refresh Manually>",
        "TXT_InspectHierarchyHistory=<empty>",
        "TOOLTIP_RefreshContent=Refresh for entity under cursor",
        "TOOLTIP_OpenJDoc=Open Javadoc Window",
        "TOOLTIP_ViewHierarchyType=Hierachy View Type",
        "TOOLTIP_InspectHierarchyHistory=Inspect Hierarchy History"
    })
    public HierarchyTopComponent() {
        history = HistorySupport.getInstnace(this.getClass());
        jdocFinder = SelectJavadocTask.create(this);
        jdocTask = RP.create(jdocFinder);
        explorerManager = new ExplorerManager();
        rootChildren = new RootChildren();
        filters = new HierarchyFilters();
        explorerManager.setRootContext(Nodes.rootNode(rootChildren, filters));
        selectedNodes  = new InstanceContent();
        lookup = new AbstractLookup(selectedNodes);
        explorerManager.addPropertyChangeListener(this);
        initComponents();
        setName(Bundle.CTL_HierarchyTopComponent());
        setToolTipText(Bundle.HINT_HierarchyTopComponent());        
        viewTypeCombo = new JComboBox(new DefaultComboBoxModel(ViewType.values()));
        viewTypeCombo.setMinimumSize(new Dimension(MIN_TYPE_WIDTH,COMBO_HEIGHT));
        viewTypeCombo.addActionListener(this);
        viewTypeCombo.setToolTipText(Bundle.TOOLTIP_ViewHierarchyType());
        historyCombo = new JComboBox(HistorySupport.createModel(history, Bundle.TXT_InspectHierarchyHistory()));
        historyCombo.setMinimumSize(new Dimension(MIN_HISTORY_WIDTH,COMBO_HEIGHT));
        historyCombo.setRenderer(HistorySupport.createRenderer(history));
        historyCombo.addActionListener(this);
        historyCombo.setEnabled(false);
        historyCombo.getModel().addListDataListener(this);
        historyCombo.setToolTipText(Bundle.TOOLTIP_InspectHierarchyHistory());
        refreshButton = new JButton(ImageUtilities.loadImageIcon(REFRESH_ICON, true));
        refreshButton.addActionListener(this);
        refreshButton.setToolTipText(Bundle.TOOLTIP_RefreshContent());
        jdocButton = new JButton(ImageUtilities.loadImageIcon(JDOC_ICON, true));
        jdocButton.addActionListener(this);
        jdocButton.setToolTipText(Bundle.TOOLTIP_OpenJDoc());
        final Box upperToolBar = new MainToolBar(
            constrainedComponent(viewTypeCombo, GridBagConstraints.HORIZONTAL, 1.0, new Insets(0,0,0,0)),
            constrainedComponent(historyCombo, GridBagConstraints.HORIZONTAL, 1.5, new Insets(0,3,0,0)),
            constrainedComponent(refreshButton, GridBagConstraints.NONE, 0.0, new Insets(0,3,0,0)),
            constrainedComponent(jdocButton, GridBagConstraints.NONE, 0.0, new Insets(0,3,0,3)));
        add(decorateAsUpperPanel(upperToolBar), BorderLayout.NORTH);
        contentView = new JPanel();
        contentView.setLayout(new CardLayout());
        JPanel nonActiveContent = Utils.updateBackground(new JPanel());
        nonActiveContent.setLayout(new BorderLayout());
        nonActiveInfo = new JLabel(Bundle.TXT_NonActiveContent());
        nonActiveInfo.setEnabled(false);
        nonActiveInfo.setHorizontalAlignment(SwingConstants.CENTER);
        nonActiveContent.add(nonActiveInfo, BorderLayout.CENTER);
        btw = createBeanTreeView();
        contentView.add(nonActiveContent, NON_ACTIVE_CONTENT);
        contentView.add(btw, ACTIVE_CONTENT);
        add(contentView,BorderLayout.CENTER);
        lowerToolBar = new TapPanel();
        lowerToolBar.setOrientation(TapPanel.DOWN);
        final JComponent lowerButtons = filters.getComponent();
        lowerButtons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        lowerToolBar.add(lowerButtons);
        final boolean expanded = NbPreferences.forModule(HierarchyTopComponent.class).
                getBoolean(PROP_LOWER_TOOLBAR_EXPANDED, true); //NOI18N
        lowerToolBar.setExpanded(expanded);
        lowerToolBar.addPropertyChangeListener(this);
        add(Utils.updateBackground(lowerToolBar), BorderLayout.SOUTH);
    }

    public void setContext(
            @NonNull final JavaSource js,
            @NonNull final JTextComponent tc) {        
        final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver =
                Resolvers.createEditorResolver(
                js,
                tc.getCaret().getDot());
        schedule(resolver);
    }

    public void setContext (@NonNull final JavaSource js) {
        final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver =
                Resolvers.createFileResolver(js);
        schedule(resolver);

    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (refreshButton == e.getSource()) {
            final JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
            if (lastFocusedComponent != null) {
                final JavaSource js = JavaSource.forDocument(Utilities.getDocument(lastFocusedComponent));
                if (js != null) {
                    setContext(js, lastFocusedComponent);
                }
            }
        } else if (jdocButton == e.getSource()) {
            final TopComponent win = JavadocTopComponent.findInstance();
            if (win != null && !win.isShowing()) {
                win.open();
                win.requestVisible();
                jdocTask.schedule(NOW);
            }
        } else if (historyCombo == e.getSource()) {
            refresh();
        } else if (viewTypeCombo == e.getSource()) {
            refresh();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            final Node[] oldNodes = (Node[])evt.getOldValue();
            final Node[] newNodes = (Node[])evt.getNewValue();
            for (Node n: oldNodes) {
                selectedNodes.remove(n);
                selectedNodes.remove(n, NodeToFileObjectConvertor.INSTANCE);
            }            
            for (Node n : newNodes) {
                selectedNodes.add(n);
                selectedNodes.add(n, NodeToFileObjectConvertor.INSTANCE);
            }
            if (newNodes.length > 0 && JavadocTopComponent.shouldUpdate()) {
                jdocFinder.cancel();
                jdocTask.schedule(JDOC_TIME);
            }
        } else if (TapPanel.EXPANDED_PROPERTY.equals(evt.getPropertyName())) {
            NbPreferences.forModule(HierarchyTopComponent.class).putBoolean(
                    PROP_LOWER_TOOLBAR_EXPANDED,
                    lowerToolBar.isExpanded());
        }
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        enableHistory();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        enableHistory();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        enableHistory();
    }


    @Override
    protected void componentActivated() {
        super.componentActivated();
        if (JavadocTopComponent.shouldUpdate() && getLookup().lookup(Node.class) != null) {
            jdocFinder.cancel();
            jdocTask.schedule(NOW);
        }
    }

    private void enableHistory() {
        if (!history.getHistory().isEmpty()) {
            historyCombo.setEnabled(true);
        }
    }

    @NonNull
    private static Pair<JComponent, GridBagConstraints> constrainedComponent(
            @NonNull final JComponent component,
            final int fill,
            final double weightx,
            @NonNull final Insets insets) {
        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = weightx;
        c.weighty = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = fill;
        c.insets = insets;
        return Pair.<JComponent,GridBagConstraints>of(component,c);
    }


    @NonNull
    private static BeanTreeView createBeanTreeView() {
        final BeanTreeView btw = new BeanTreeView();
        btw.setRootVisible(false);
        return btw;
    }
    

    @NonNull
    private static JPanel decorateAsUpperPanel(@NonNull final JComponent comp) {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        wrapper.add(comp,c);
        wrapper.setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0,
                    UIManager.getColor("NbSplitPane.background")));//NOI18N
        return Utils.updateBackground(wrapper);
    }

    private void showBusy() {
        assert SwingUtilities.isEventDispatchThread();
        ((CardLayout)contentView.getLayout()).show(contentView, ACTIVE_CONTENT);
        rootChildren.set(Nodes.waitNode());
        btw.requestFocus();
    }

    private void schedule(@NonNull final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver) {
        showBusy();
        assert resolver != null;
        final RunnableFuture<Pair<URI,ElementHandle<TypeElement>>> becomesType = new FutureTask<Pair<URI,ElementHandle<TypeElement>>>(resolver);
        jdocTask.cancel();
        jdocFinder.cancel();
        RP.execute(becomesType);
        Object selItem = viewTypeCombo.getSelectedItem();
        if (!(selItem instanceof ViewType)) {
            selItem = ViewType.SUPER_TYPE;
        }
        final Runnable refreshTask = new RefreshTask(becomesType,(ViewType)selItem);
        jdocTask.cancel();
        jdocFinder.cancel();
        RP.execute(refreshTask);
    }

    private void refresh() {
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

    public static synchronized HierarchyTopComponent findDefault() {

        HierarchyTopComponent component = instance;

        if (component == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("JavaHierarchyTopComponent"); //NOI18N
            if (tc instanceof HierarchyTopComponent) {
                component = instance = (HierarchyTopComponent) tc;
            } else {
                component = instance = new HierarchyTopComponent();
            }
        }
        return component;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @NbBundle.Messages({
        "LBL_SuperTypeView=Supertypes",
        "LBL_SubTypeView=Subtypes"})
    private static enum ViewType {
                       
        SUPER_TYPE(Bundle.LBL_SuperTypeView()),
        SUB_TYPE(Bundle.LBL_SubTypeView());

        private final String displayName;

        private ViewType(@NonNull final String displayName) {
            assert displayName != null;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }


    private final class RefreshTask implements Runnable {

        private final Future<Pair<URI,ElementHandle<TypeElement>>> toShow;
        private final ViewType viewType;

        RefreshTask(
            @NonNull final Future<Pair<URI,ElementHandle<TypeElement>>> toShow,
            @NonNull final ViewType viewType) {
            assert toShow != null;
            assert viewType != null;
            this.toShow = toShow;
            this.viewType = viewType;
        }

        @Override
        @NbBundle.Messages({
        "ERR_Cannot_Resolve_File=Cannot resolve type: {0}.",
        "ERR_Not_Declared_Type=Not a declared type.",
        "WARN_Object=<html>The subtypes of java.lang.Object are not supported.",
        "INFO_SubClassesComputation=Computing Subtypes of {0}"})
        public void run() {
            try {
                final Pair<URI,ElementHandle<TypeElement>> pair = toShow.get();
                if (pair != null) {
                    if (viewType == ViewType.SUB_TYPE &&
                        Object.class.getName().equals(pair.second().getQualifiedName())) {
                        nonActiveInfo.setText(Bundle.WARN_Object());
                        ((CardLayout)contentView.getLayout()).show(contentView, NON_ACTIVE_CONTENT);
                    } else {
                        final FileObject file = URLMapper.findFileObject(pair.first().toURL());
                        JavaSource js;
                        if (file != null && (js=JavaSource.forFileObject(file)) != null) {
                            LOG.log(Level.FINE, "Showing hierarchy for: {0}", pair.second().getQualifiedName());  //NOI18N
                            history.addToHistory(pair);
                            js.runUserActionTask(new Task<CompilationController>() {
                                @Override
                                public void run(CompilationController cc) throws Exception {
                                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                                    final TypeElement te = pair.second().resolve(cc);
                                    if (te != null) {
                                        final Node root;
                                        if (viewType == ViewType.SUPER_TYPE) {
                                         root = Nodes.superTypeHierarchy(
                                                (DeclaredType)te.asType(),
                                                cc.getClasspathInfo(),
                                                filters);
                                        } else {
                                            final AtomicBoolean cancel = new AtomicBoolean();
                                            final ProgressHandle  progress = ProgressHandleFactory.createHandle(
                                                Bundle.INFO_SubClassesComputation(te.getQualifiedName()),
                                                new Cancellable() {
                                                    @Override
                                                    public boolean cancel() {
                                                        cancel.set(true);
                                                        return true;
                                                    }
                                                });
                                            progress.start();
                                            try {
                                                Node subTypes = Nodes.subTypeHierarchy(te, cc, filters, cancel);
                                                root = subTypes != null ? subTypes : /*XXX:*/new AbstractNode(Children.LEAF);
                                            } finally {
                                                progress.finish();
                                            }
                                        }
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                historyCombo.getModel().setSelectedItem(pair);
                                                rootChildren.set(root);
                                                btw.expandAll();
                                            }
                                        });
                                    }
                                }
                            }, true);
                        } else {
                            rootChildren.set(null);
                            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Cannot_Resolve_File(pair.second().getQualifiedName()));
                        }
                    }
                } else {
                    rootChildren.set(null);
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
    }

    private static final class NodeToFileObjectConvertor implements InstanceContent.Convertor<Node,FileObject> {

        public static final NodeToFileObjectConvertor INSTANCE =
                new NodeToFileObjectConvertor();

        private NodeToFileObjectConvertor() {}

        @Override
        public FileObject convert(Node obj) {
            return obj.getLookup().lookup(FileObject.class);
        }

        @Override
        public Class<? extends FileObject> type(Node obj) {
            return FileObject.class;
        }

        @Override
        public String id(Node obj) {
            return obj.toString();
        }

        @Override
        public String displayName(Node obj) {
            return obj.getDisplayName();
        }

    }
    
    private static final class MainToolBar extends Box {
        MainToolBar(@NonNull final Pair<JComponent,GridBagConstraints>... components) {
            super(BoxLayout.X_AXIS);
            setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 5));
            final JToolBar toolbar = new NoBorderToolBar(JToolBar.HORIZONTAL);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            toolbar.setLayout(new GridBagLayout());
            for (Pair<JComponent,GridBagConstraints> p : components) {
                toolbar.add(p.first(),p.second());
            }
            add (toolbar);
        }
    }

    private static final class RootChildren extends Children.Array {
        
        void set (Node node) {
            remove(getNodes(true));
            if (node != null) {
                add(new Node[] {node});
            }
        }
    }
}
