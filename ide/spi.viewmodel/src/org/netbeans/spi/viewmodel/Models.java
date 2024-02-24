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

package org.netbeans.spi.viewmodel;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.netbeans.modules.viewmodel.AsynchronousModel;
import org.netbeans.modules.viewmodel.DefaultTreeExpansionManager;
import org.netbeans.modules.viewmodel.HyperCompoundModel;
import org.netbeans.modules.viewmodel.ModelRootChangeListener;
import org.netbeans.modules.viewmodel.OutlineTable;
import org.netbeans.modules.viewmodel.TreeModelNode.ActionOnPresetNodes;
import org.netbeans.modules.viewmodel.TreeModelNode.DisableableAction;
import org.netbeans.modules.viewmodel.TreeModelRoot;

import org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL;
import org.openide.awt.Actions;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;


/**
 * Contains various utility methods for various models.
 *
 * @author   Jan Jancura
 */
public final class Models {

    /** Cached default implementations of expansion models. */
    private static final WeakHashMap<Object, DefaultTreeExpansionModel> defaultExpansionModels = new WeakHashMap<Object, DefaultTreeExpansionModel>();
    
    /**
     * Empty model - returns default root node with no children.
     */
    public static CompoundModel EMPTY_MODEL = createCompoundModel 
        (new ArrayList ());
    
    
    public static int MULTISELECTION_TYPE_EXACTLY_ONE = 1;
    public static int MULTISELECTION_TYPE_ALL = 2;
    public static int MULTISELECTION_TYPE_ANY = 3;

    private static final int DEFAULT_DRAG_DROP_ALLOWED_ACTIONS = DnDConstants.ACTION_NONE;
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.models") != null;
    
    
    /**
     * Creates a new instance of TreeTableView
     * for given {@link org.netbeans.spi.viewmodel.Models.CompoundModel}.
     *
     * @param compoundModel a compound model instance
     *
     * @return new instance of complete model view
     */
    public static JComponent createView (
        CompoundModel compoundModel
    ) {
        /*if (compoundModel.isTree()) {
            OutlineTree ot = new OutlineTree();
            ot.setModel (compoundModel);
            return ot;
        } else {*/
        OutlineTable ot = new OutlineTable ();
        if (compoundModel != null && compoundModel.isHyperModel()) {
            ot.setModel(compoundModel.createHyperModel(), compoundModel.getTreeNodeDisplayFormat());
        } else {
            ot.setModel (compoundModel, (compoundModel != null) ? compoundModel.getTreeNodeDisplayFormat() : null);
        }
        return ot;
        //}
    }
    
    /**
     * Creates a root node of the nodes tree structure
     * for given {@link org.netbeans.spi.viewmodel.Models.CompoundModel}.
     *
     * @param compoundModel a compound model instance
     * @param treeView The tree view component where nodes are going to be displayed.
     *
     * @return new instance root node
     * @since 1.15
     */
    public static Node createNodes (
        CompoundModel compoundModel,
        TreeView treeView
    ) {
        if (compoundModel != null && compoundModel.isHyperModel()) {
            return new TreeModelRoot (compoundModel.createHyperModel(), treeView).getRootNode();
        } else {
            return new TreeModelRoot (compoundModel, treeView).getRootNode();
        }
    }
    
    /**
     * Set given models to given view instance.
     *
     * @param view a view instance - must be an instance created by {@link #createView} method.
     * @param compoundModel a compound model instance
     */
    public static void setModelsToView (
        final JComponent view,
        final CompoundModel compoundModel
    ) {
        if (!(view instanceof OutlineTable)) {
            throw new IllegalArgumentException("Expecting an instance of "+OutlineTable.class.getName()+", which can be obtained from Models.createView(). view = "+view);
        }
        if (verbose) {
            System.out.println (compoundModel);
        }
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                if (compoundModel != null && compoundModel.isHyperModel()) {
                    ((OutlineTable) view).setModel (compoundModel.createHyperModel(), compoundModel.getTreeNodeDisplayFormat());
                } else {
                    if (compoundModel != null) {
                        ((OutlineTable) view).setModel (compoundModel, compoundModel.getTreeNodeDisplayFormat());
                    } else {
                        ((OutlineTable) view).setModel (compoundModel);
                    }
                }
            }
        });
    }
    
    /**
     * Creates one {@link CompoundModel} from given list of models.
     * <p>
     * If this list does not include any instance of TreeModel or
     * TreeExpansionModel, a default implementation of these models is created
     * based on the instance of this list. Thus you get one implementation
     * per provided instance of the models list.
     * 
     * @param models a list of models
     * @return {@link CompoundModel} encapsulating given list of models
     */
    public static CompoundModel createCompoundModel (List models) {
        return createCompoundModel(models, null);
    }
    
    /**
     * Creates one {@link CompoundModel} from given list of models.
     * <p>
     * If this list does not include any instance of TreeModel or
     * TreeExpansionModel, a default implementation of these models is created
     * based on the instance of this list. Thus you get one implementation
     * per provided instance of the models list.
     * 
     * @param models a list of models
     * @param propertiesHelpID The help ID, which is set for the properties
     *        sheets created from this model.
     * @return {@link CompoundModel} encapsulating given list of models
     * @since 1.7
     */
    // TODO: Add createCompoundModel(List models, String propertiesHelpID, RequestProcessor rp)
    // Or instead of RP use some interface that could provide the desired thread to run in (current, AWT, RP thread,...)
    public static CompoundModel createCompoundModel (List models, String propertiesHelpID) {
        if (models.size() > 1 && models.get(0) instanceof CompoundModel && models.get(1) instanceof CompoundModel) {
            // Hypermodel
            ArrayList<CompoundModel> subModels = new ArrayList<CompoundModel>();
            CompoundModel mainModel = null;
            TreeModelFilter treeFilter = null;
            for (Object o : models) {
                if (o instanceof CompoundModel) {
                    if (subModels.contains((CompoundModel) o)) {
                        mainModel = (CompoundModel) o;
                        continue;
                    }
                    subModels.add((CompoundModel) o);
                } else if (o instanceof TreeModelFilter) {
                    treeFilter = (TreeModelFilter) o;
                }
            }
            if (mainModel == null) {
                mainModel = subModels.get(0);
            }
            return new CompoundModel(mainModel, subModels.toArray(new CompoundModel[]{}), treeFilter, propertiesHelpID);
        }

        MessageFormat treeNodeDisplayFormat = null;
        if (models.size() > 0 && models.get(models.size() - 1) instanceof MessageFormat) {
            treeNodeDisplayFormat = (MessageFormat) models.remove(models.size() - 1);
        }
        ModelLists ml = new ModelLists();
        List<? extends Model>           otherModels;
        
        // Either the list contains 10 lists of individual models + one list of mixed models
        //  + optional TreeExpansionModelFilter(s) + optional AsynchronousModelFilter(s)
        // ; or the models directly
        boolean hasLists = false;
        int modelsSize = models.size();
        if (11 <= modelsSize && modelsSize <= 18) {
            Iterator it = models.iterator ();
            boolean failure = false;
            while (it.hasNext ()) {
                Object model = it.next();
                if (!(model instanceof List)) {
                    failure = true;
                    break;
                }
            }
            if (!it.hasNext() && !failure) { // All elements are lists
                hasLists = true;
            }
        }
        if (hasLists) { // We have 11 or 12 lists of individual models + optional RP
            ml.treeModels =            (List<TreeModel>)       models.get(0);
            ml.treeModelFilters =      (List<TreeModelFilter>) models.get(1);
            revertOrder(ml.treeModelFilters);
            ml.treeExpansionModels =   (List<TreeExpansionModel>) models.get(2);
            ml.nodeModels =            (List<NodeModel>) models.get(3);
            ml.nodeModelFilters =      (List<NodeModelFilter>) models.get(4);
            revertOrder(ml.nodeModelFilters);
            ml.tableModels =           (List<TableModel>) models.get(5);
            ml.tableModelFilters =     (List<TableModelFilter>) models.get(6);
            revertOrder(ml.tableModelFilters);
            ml.nodeActionsProviders =  (List<NodeActionsProvider>) models.get(7);
            ml.nodeActionsProviderFilters = (List<NodeActionsProviderFilter>) models.get(8);
            revertOrder(ml.nodeActionsProviderFilters);
            ml.columnModels =          (List<ColumnModel>) models.get(9);
            otherModels =           (List<? extends Model>) models.get(10);
            if (modelsSize > 11) { // TreeExpansionModelFilter
                ml.treeExpansionModelFilters = (List<TreeExpansionModelFilter>) models.get(11);
                //if (modelsSize > 12) { // AsynchronousModel
                //    ml.asynchModels = (List<AsynchronousModel>) models.get(12);
                    if (modelsSize > 12) { // AsynchronousModelFilter
                        ml.asynchModelFilters = (List<AsynchronousModelFilter>) models.get(12);
                        if (modelsSize > 13) {
                            ml.tableRendererModels = (List<TableRendererModel>) models.get(13);
                            if (modelsSize > 14) {
                                ml.tableRendererModelFilters = (List<TableRendererModelFilter>) models.get(14);
                                //if (modelsSize > 15) {
                                //    ml.tableHtmlModels = (List<TableHTMLModel>) models.get(15);
                                    if (modelsSize > 15) {
                                        ml.tableHtmlModelFilters = (List<TableHTMLModelFilter>) models.get(15);
                                        if (modelsSize > 16) {
                                            ml.tablePropertyEditorsModels = (List<TablePropertyEditorsModel>) models.get(16);
                                            if (modelsSize > 17) {
                                                ml.tablePropertyEditorsModelFilters = (List<TablePropertyEditorsModelFilter>) models.get(17);
                                            }
                                        }
                                    }
                                //}
                            }
                        }
                    }
                //}
            }
            //treeExpansionModelFilters = (models.size() > 11) ? (List<TreeExpansionModelFilter>) models.get(11) : (List<TreeExpansionModelFilter>) Collections.EMPTY_LIST;
        } else { // We have the models, need to find out what they implement
            otherModels =          (List<? extends Model>) models;
        }

        ml.addOtherModels(otherModels);
        if (ml.treeExpansionModels.isEmpty()) {
            DefaultTreeExpansionModel defaultExpansionModel;
            synchronized (defaultExpansionModels) {
                defaultExpansionModel = defaultExpansionModels.get(models);
                if (defaultExpansionModel != null) {
                    defaultExpansionModel = defaultExpansionModel.cloneForNewModel();
                } else {
                    defaultExpansionModel = new DefaultTreeExpansionModel();
                }
                defaultExpansionModels.put(models, defaultExpansionModel);
            }
            ml.treeExpansionModels = Collections.singletonList((TreeExpansionModel) defaultExpansionModel);
        }
        /*
        System.out.println("ALL MODELS = "+models+"\n");
        System.out.println("Tree Models = "+ml.treeModels);
        System.out.println("Tree Model Filters = "+ml.treeModelFilters);
        System.out.println("Tree Expans Models = "+ml.treeExpansionModels);
        System.out.println("Node Models = "+ml.nodeModels);
        System.out.println("Node Model Filters = "+ml.nodeModelFilters);
        System.out.println("Table Models = "+ml.tableModels);
        System.out.println("Table Model Filters = "+ml.tableModelFilters);
        System.out.println("Node Action Providers = "+ml.nodeActionsProviders);
        System.out.println("Node Action Provider Filters = "+ml.nodeActionsProviderFilters);
        System.out.println("Column Models = "+ml.columnModels);
         */
        CompoundModel cm = createCompoundModel(ml, propertiesHelpID);
        cm.setTreeNodeDisplayFormat(treeNodeDisplayFormat);
        return cm;
    }

    private  static CompoundModel createCompoundModel (ModelLists ml, String propertiesHelpID) {
        if (ml.treeModels.isEmpty ()) {
            TreeModel etm = new EmptyTreeModel();
            ml.treeModels = Collections.singletonList(etm);
        }
        DefaultTreeExpansionModel defaultExpansionModel = null;
        if (ml.treeExpansionModels.isEmpty()) {
            synchronized (defaultExpansionModels) {
                defaultExpansionModel = defaultExpansionModels.get(ml);
                if (defaultExpansionModel != null) {
                    defaultExpansionModel = defaultExpansionModel.cloneForNewModel();
                } else {
                    defaultExpansionModel = new DefaultTreeExpansionModel();
                }
                defaultExpansionModels.put(ml, defaultExpansionModel);
            }
            ml.treeExpansionModels = Collections.singletonList((TreeExpansionModel) defaultExpansionModel);
        } else if (ml.treeExpansionModels.size() == 1) {
            if (ml.treeExpansionModels.get(0) instanceof DefaultTreeExpansionModel) {
                defaultExpansionModel = (DefaultTreeExpansionModel) ml.treeExpansionModels.get(0);
            }
        }
        /*if (ml.asynchModels.isEmpty()) {
            ml.asynchModels = Collections.singletonList((AsynchronousModel) new DefaultAsynchronousModel());
        }*/
        
        CompoundModel cm;
        if (ml.columnModels == null && ml.tableModels == null && ml.tableModelFilters == null &&
            ml.tableRendererModels == null && ml.tableRendererModelFilters == null) {
            
            cm = new CompoundModel (
            createCompoundTreeModel (
                new DelegatingTreeModel (ml.treeModels),
                ml.treeModelFilters
            ),
            createCompoundTreeExpansionModel(
                new DelegatingTreeExpansionModel (ml.treeExpansionModels),
                ml.treeExpansionModelFilters
            ),
            createCompoundNodeModel (
                new DelegatingNodeModel (ml.nodeModels),
                ml.nodeModelFilters
            ),
            createCompoundNodeActionsProvider (
                new DelegatingNodeActionsProvider (ml.nodeActionsProviders),
                ml.nodeActionsProviderFilters
            ),
            null, null,
            createCompoundAsynchronousModel (
                new DefaultAsynchronousModel(),//new DelegatingAsynchronousModel (ml.asynchModels),
                ml.asynchModelFilters
            ),
            null, null,
            propertiesHelpID
        );
        } else {
            cm = new CompoundModel (
            createCompoundTreeModel (
                new DelegatingTreeModel (ml.treeModels),
                ml.treeModelFilters
            ),
            createCompoundTreeExpansionModel(
                new DelegatingTreeExpansionModel (ml.treeExpansionModels),
                ml.treeExpansionModelFilters
            ),
            createCompoundNodeModel (
                new DelegatingNodeModel (ml.nodeModels),
                ml.nodeModelFilters
            ),
            createCompoundNodeActionsProvider (
                new DelegatingNodeActionsProvider (ml.nodeActionsProviders),
                ml.nodeActionsProviderFilters
            ),
            ml.columnModels,
            createCompoundTableModel (
                new DelegatingTableModel (ml.tableModels),
                ml.tableModelFilters,
                ml.tableHtmlModelFilters
            ),
            createCompoundAsynchronousModel (
                new DefaultAsynchronousModel(),//new DelegatingAsynchronousModel (ml.asynchModels),
                ml.asynchModelFilters
            ),
            createCompoundTableRendererModel (
                new DelegatingTableRendererModel(ml.tableRendererModels),
                ml.tableRendererModelFilters
            ),
            /*createCompoundTableHTMLModel (
                new DelegatingTableHTMLModel(ml.tableHtmlModels),
                ml.tableHtmlModelFilters
            ),*/
            createCompoundTablePropertyEditorModel (
                new DelegatingTablePropertyEditorsModel(ml.tablePropertyEditorsModels),
                ml.tablePropertyEditorsModelFilters
            ),
            propertiesHelpID
        );
        }
        if (defaultExpansionModel != null) {
            defaultExpansionModel.setCompoundModel(cm);
        }
        return cm;
    }
    
    private static <T> void revertOrder(List<T> filters) {
        int n = filters.size();
        // [TODO] do not remove the following line, prevents null to be returned by filters.remove(i);
        // needs deeper investigation why it can occure
        filters.toString();
        for (int i = 0; i < n; ) {
            T filter = filters.remove(i);
            boolean first = filter.getClass ().getName ().endsWith ("First");
            if (first) { // The "First" should be the last one in this list
                filters.add(filter);
                n--;
            } else {
                filters.add(0, filter);
                i++;
            }
        }
    }
    
    
    /**
     * Returns {@link javax.swing.Action} for given parameters.
     *
     * @param displayName a display name for action
     * @param performer a performer for action
     * @param multiselectionType The type of the multi selection - one of the
     *        MULTISELECTION_TYPE_* constants.
     *
     * @return a new instance of {@link javax.swing.Action} for given parameters
     */
    public static Action createAction (
        String displayName, 
        ActionPerformer performer,
        int multiselectionType
    ) {
        return new ActionSupport (
            displayName, 
            performer, 
            multiselectionType
        );
    }
    
    /**
     * Returns implementation of tree view features for given view.
     *
     * @param view a view created by this Models class
     * @throws UnsupportedOperationException in the case that given 
     *        view is not tree view
     * @return implementation of tree view features
     */
    public static TreeFeatures treeFeatures (JComponent view) 
    throws UnsupportedOperationException {
        return new DefaultTreeFeatures (view);
    }
    
    
    // private methods .........................................................
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
     * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
     * 
     * @param originalTreeModel a original tree model
     * @param treeModelFilter a list of tree model filters
     *
     * @returns compund tree model
     */
    private static ReorderableTreeModel createCompoundTreeModel (
        ReorderableTreeModel originalTreeModel,
        List treeModelFilters
    ) {
        ReorderableTreeModel tm = originalTreeModel;
        int i, k = treeModelFilters.size ();
        for (i = 0; i < k; i++) {
            tm = new CompoundTreeModel (
                tm,
                (TreeModelFilter) treeModelFilters.get (i)
            );
        }
        return tm;
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.NodeModel} for given NodeModel and
     * {@link org.netbeans.spi.viewmodel.NodeModelFilter}.
     * 
     * @param originalNodeModel a original node model
     * @param nodeModelFilters a list of node model filters
     *
     * @returns compound tree model
     */
    private static SuperNodeModel createCompoundNodeModel (
        SuperNodeModel originalNodeModel,
        List treeNodeModelFilters
    ) {
        SuperNodeModel nm = originalNodeModel;
        int i, k = treeNodeModelFilters.size ();
        for (i = 0; i < k; i++) {
            nm = new CompoundNodeModel (
                nm,
                (NodeModelFilter) treeNodeModelFilters.get (i)
            );
        }
        return nm;
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TableModel} for given TableModel and
     * {@link org.netbeans.spi.viewmodel.TableModelFilter}.
     * 
     * @param originalTableModel a original table model
     * @param tableModelFilters a list of table model filters
     *
     * @returns compound table model
     */
    private static TableHTMLModel createCompoundTableModel (
        TableHTMLModel originalTableModel,
        List tableModelFilters,
        List tableHtmlModelFilters
    ) {
        TableHTMLModel tm = originalTableModel;
        int i, k = tableModelFilters.size ();
        for (i = 0; i < k; i++) {
            tm = new CompoundTableModel (
                tm,
                (TableModelFilter) tableModelFilters.get (i)
            );
        }
        k = tableHtmlModelFilters.size ();
        for (i = 0; i < k; i++) {
            tm = new CompoundTableModel (
                tm,
                (TableHTMLModelFilter) tableHtmlModelFilters.get (i)
            );
        }
        return tm;
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TableModel} for given TableModel and
     * {@link org.netbeans.spi.viewmodel.TableModelFilter}.
     *
     * @param originalTableModel a original table model
     * @param tableModelFilters a list of table model filters
     *
     * @returns compound table model
     */
    private static TableRendererModel createCompoundTableRendererModel (
        TableRendererModel originalTableModel,
        List tableModelFilters
    ) {
        TableRendererModel tm = originalTableModel;
        int i, k = tableModelFilters.size ();
        for (i = 0; i < k; i++) {
            tm = new CompoundTableRendererModel (
                tm,
                (TableRendererModelFilter) tableModelFilters.get (i)
            );
        }
        return tm;
    }
    
    private static TablePropertyEditorsModel createCompoundTablePropertyEditorModel (
        TablePropertyEditorsModel originalTableModel,
        List tableModelFilters
    ) {
        TablePropertyEditorsModel tm = originalTableModel;
        int i, k = tableModelFilters.size ();
        for (i = 0; i < k; i++) {
            tm = new CompoundTablePropertyEditorsModel (
                tm,
                (TablePropertyEditorsModelFilter) tableModelFilters.get (i)
            );
        }
        return tm;
    }

    /**
     * Creates {@link org.netbeans.spi.viewmodel.NodeActionsProvider} for given NodeActionsProvider and
     * {@link org.netbeans.spi.viewmodel.NodeActionsProviderFilter}.
     * 
     * @param originalNodeActionsProvider a original node actions provider
     * @param nodeActionsProviderFilters a list of node actions provider filters
     *
     * @returns compound node actions provider
     */
    private static NodeActionsProvider createCompoundNodeActionsProvider (
        NodeActionsProvider originalNodeActionsProvider,
        List nodeActionsProviderFilters
    ) {
        NodeActionsProvider nap = originalNodeActionsProvider;
        int i, k = nodeActionsProviderFilters.size ();
        for (i = 0; i < k; i++) {
            nap = new CompoundNodeActionsProvider (
                nap,
                (NodeActionsProviderFilter) nodeActionsProviderFilters.get (i)
            );
        }
        return nap;
    }
    
    private static TreeExpansionModel createCompoundTreeExpansionModel (
            TreeExpansionModel expansionModel,
            List<TreeExpansionModelFilter> filters
    ) {
        for (TreeExpansionModelFilter filter : filters) {
            expansionModel = new CompoundTreeExpansionModel (expansionModel, filter);
        }
        return expansionModel;
    }

    private static AsynchronousModel createCompoundAsynchronousModel (
            AsynchronousModel asynchModel,
            List<AsynchronousModelFilter> filters
    ) {
        for (AsynchronousModelFilter filter : filters) {
            asynchModel = new CompoundAsynchronousModel (asynchModel, filter);
        }
        return asynchModel;
    }
    
    
    // innerclasses ............................................................
    
    /**
     * @author   Jan Jancura
     */
    private static class ActionSupport extends AbstractAction implements DisableableAction,
                                                                         ActionOnPresetNodes {

        private ActionPerformer     performer;
        private int                 multiselectionType;
        private String              displayName;
        private PrivilegedAction    enabledTest;
        private List<Node>          presetNodes;

 
        ActionSupport (
            String displayName, 
            ActionPerformer performer,
            int multiselectionType
        ) {
            super (displayName);
            this.performer = performer;
            this.displayName = displayName;
            this.multiselectionType = multiselectionType;
        }
        
        @Override
        public void addNode(Node n) {
            if (SwingUtilities.isEventDispatchThread()) {
                if (presetNodes == null) {
                    presetNodes = new LinkedList<Node>();
                    SwingUtilities.invokeLater(new PresetNodesCleaner());
                }
                presetNodes.add(n);
            }
        }
        
        private Node[] getActiveNodes(ActionEvent e) {
            Node[] nodes = null;
            if (e != null) {
                nodes = getOutlineViewSelection(e);
            } else if (presetNodes != null) {
                nodes = presetNodes.toArray(new Node[] {});
            }
            if (nodes == null) {
                nodes = TopComponent.getRegistry().getActivatedNodes();
            }
            return nodes;
        }
        
        private Node[] getOutlineViewSelection(ActionEvent e) {
            Object source = e.getSource();
            if (source instanceof Component) {
                Component c = (Component) source;
                JPopupMenu popupMenu = null;
                while (c != null) {
                    if (c instanceof JPopupMenu) {
                        popupMenu = (JPopupMenu) c;
                        break;
                    }
                    c = c.getParent();
                }
                if (popupMenu != null) {
                    c = popupMenu.getInvoker();
                    while (c != null) {
                        if (c instanceof OutlineView || c instanceof TreeView) {
                            ExplorerManager manager = ExplorerManager.find(c);
                            return manager.getSelectedNodes();
                        }
                        c = c.getParent();
                    }
                }
            }
            return null;
        }
        
        @Override
        public boolean isEnabled () {
            if (enabledTest != null) {
                if (Boolean.FALSE.equals(enabledTest.run())) {
                    return false;
                }
            }
            boolean any = multiselectionType == MULTISELECTION_TYPE_ANY;
            Node[] ns = getActiveNodes(null);
            if (multiselectionType == MULTISELECTION_TYPE_EXACTLY_ONE) {
                if (ns.length != 1) {
                    return false;
                }
                return performer.isEnabled (
                    ns[0].getLookup().lookup(Object.class)
                );
            }
            int i, k = ns.length;
            if (k == 0) {
                if (!performer.isEnabled(TreeModel.ROOT)) {
                    return false;
                }
            } else {
                for (i = 0; i < k; i++) {
                    if (!performer.isEnabled(ns[i].getLookup().lookup(Object.class))) {
                        if (!any) {
                            return false;
                        }
                    } else if (any) {
                        return true;
                    }
                }
                if (any) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            //System.err.println("Models.ActionSupport.actionPerformed("+e+")");
            Node[] ns = getActiveNodes(e);
            int i, k = ns.length;
            IdentityHashMap<Action, ArrayList<Object>> h = new IdentityHashMap<Action, ArrayList<Object>>();
            for (i = 0; i < k; i++) {
                Object node = ns[i].getLookup().lookup(Object.class);
                Action[] as = ns [i].getActions (false);
                int j, jj = as.length;
                for (j = 0; j < jj; j++) {
                    if (equals (as [j])) {
                        ArrayList<Object> l = h.get (as [j]);
                        if (l == null) {
                            l = new ArrayList<Object>();
                            h.put (as [j], l);
                        }
                        l.add (node);
                    }
                }
            }
            //System.err.println("  k = "+k);
            if (k == 0) {
                if (multiselectionType != MULTISELECTION_TYPE_EXACTLY_ONE) {
                    performer.perform(new Object[]{});
                }
            } else {
                //System.err.println("  h = "+h);
                Iterator<Action> it = h.keySet ().iterator ();
                while (it.hasNext ()) {
                    ActionSupport a = (ActionSupport) it.next ();
                    //System.err.println("  "+a.performer+".perform("+((ArrayList) h.get (a)));
                    a.performer.perform (
                        ((ArrayList) h.get (a)).toArray ()
                    );
                }
            }
        }
        
        @Override
        public int hashCode () {
            return displayName.hashCode ();
        }
        
        @Override
        public boolean equals (Object o) {
            return (o instanceof ActionSupport) && 
                displayName.equals (((ActionSupport) o).displayName);
        }

        @Override
        public Action createDisableable(PrivilegedAction enabledTest) {
            ActionSupport a = new ActionSupport(displayName, performer, multiselectionType);
            a.enabledTest = enabledTest;
            Object[] keys = getKeys();
            if (keys != null) {
                for (Object k : keys) {
                    a.putValue((String) k, getValue((String) k));
                }
            }
            return a;
        }
        
        private class PresetNodesCleaner implements Runnable {

            @Override
            public void run() {
                presetNodes = null;
            }
            
        }
    }

    /**
     * Support interface for 
     * {@link #createAction(String,Models.ActionPerformer,int)} method.
     */
    public static interface ActionPerformer {

        /**
         * Returns enabled property state for given node.
         *
         * @param node the node the action should be applied to
         * @return enabled property state for given node
         *
         * @see #createAction(String,Models.ActionPerformer,int)
         */
        public boolean isEnabled (Object node);

        /**
         * Called when action <code>action</code> is performed for 
         * nodes.
         *
         * @param nodes nodes the action should be applied to
         *
         * @see #createAction(String,Models.ActionPerformer,int)
         */
        public void perform (Object[] nodes);
    }

    /**
     * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
     * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
     * 
     * @author   Jan Jancura
     */
    private static final class CompoundTreeModel implements ReorderableTreeModel, ModelListener {


        private ReorderableTreeModel model;
        private TreeModelFilter filter;
        
        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();

        
        /**
         * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
         * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
         */
        CompoundTreeModel (ReorderableTreeModel model, TreeModelFilter filter) {
            this.model = model;
            this.filter = filter;
        }

        /** 
         * Returns the root node of the tree or null, if the tree is empty.
         *
         * @return the root node of the tree or null
         */
        @Override
        public Object getRoot () {
            return filter.getRoot (model);
        }

        /** 
         * Returns children for given parent on given indexes.
         *
         * @param   parent a parent of returned nodes
         * @throws  NoInformationException if the set of children can not be 
         *          resolved
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  children for given parent on given indexes
         */
        @Override
        public Object[] getChildren (Object parent, int from, int to) 
            throws UnknownTypeException {

            return filter.getChildren (model, parent, from, to);
        }
    
        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  NoInformationException if the set of children can not be 
         *          resolved
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        @Override
        public int getChildrenCount (Object node) throws UnknownTypeException {
            return filter.getChildrenCount (model, node);
        }

        /**
         * Returns true if node is leaf.
         * 
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         * @return  true if node is leaf
         */
        @Override
        public boolean isLeaf (Object node) throws UnknownTypeException {
            return filter.isLeaf (model, node);
        }

        @Override
        public boolean canReorder(Object parent) throws UnknownTypeException {
            if (filter instanceof ReorderableTreeModelFilter) {
                return ((ReorderableTreeModelFilter) filter).canReorder(model, parent);
            } else {
                return model.canReorder(parent);
            }
        }

        @Override
        public void reorder(Object parent, int[] perm) throws UnknownTypeException {
            if (filter instanceof ReorderableTreeModelFilter) {
                ((ReorderableTreeModelFilter) filter).reorder(model, parent, perm);
            } else {
                model.reorder(parent, perm);
            }
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.isEmpty()) {
                    filter.addModelListener (this);
                    model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                removeLonelyModelRootChangelisteners(modelListeners);
                if (modelListeners.isEmpty()) {
                    filter.removeModelListener (this);
                    model.removeModelListener (this);
                }
            }
        }

        @Override
        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged &&
                    (event.getSource() instanceof NodeModel || event.getSource() instanceof NodeModelFilter)) {
                // CompoundNodeModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TableValueChanged &&
                    (event.getSource() instanceof TableModel || event.getSource() instanceof TableModelFilter)) {
                // CompoundTableModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }
        
        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            if (model instanceof CompoundTreeModel) {
                return n + filter + "\n" +
                    ((CompoundTreeModel) model).toString (n + "  ");
            }
            return n + filter + "\n" + 
                   n + "  " + model;
        }

    }
    
    private static void removeLonelyModelRootChangelisteners(Collection<ModelListener> listeners) {
        if (listeners.isEmpty()) {
            return ;
        }
        for (ModelListener ml : listeners) {
            if (!(ml instanceof ModelRootChangeListener)) {
                return ;
            }
        }
        listeners.clear();
    }

    private static ModelEvent translateEvent(ModelEvent event, Object newSource) {
        ModelEvent newEvent;
        if (event instanceof ModelEvent.NodeChanged) {
            newEvent = new ModelEvent.NodeChanged(newSource,
                    ((ModelEvent.NodeChanged) event).getNode(),
                    ((ModelEvent.NodeChanged) event).getChange());
        } else if (event instanceof ModelEvent.TableValueChanged) {
            newEvent = new ModelEvent.TableValueChanged(newSource,
                    ((ModelEvent.TableValueChanged) event).getNode(),
                    ((ModelEvent.TableValueChanged) event).getColumnID(),
                    ((ModelEvent.TableValueChanged) event).getChange());
        } else if (event instanceof ModelEvent.TreeChanged) {
            newEvent = new ModelEvent.TreeChanged(newSource);
        } else {
            newEvent = event;
        }
        return newEvent;
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
     * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
     * 
     * @author   Jan Jancura
     */
    private static final class CompoundNodeModel implements SuperNodeModel,
                                                            ModelListener {


        private SuperNodeModel model;
        private NodeModelFilter filter;
        private CheckNodeModelFilter cfilter;
        private DnDNodeModelFilter dndfilter;

        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();


        /**
         * Creates {@link org.netbeans.spi.viewmodel.TreeModel} for given TreeModel and
         * {@link org.netbeans.spi.viewmodel.TreeModelFilter}.
         */
        CompoundNodeModel (SuperNodeModel model, NodeModelFilter filter) {
            this.model = model;
            this.filter = filter;
            if (filter instanceof CheckNodeModelFilter) {
                this.cfilter = (CheckNodeModelFilter) filter;
            }
            if (filter instanceof DnDNodeModelFilter) {
                this.dndfilter = (DnDNodeModelFilter) filter;
            }
        }
    
        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        @Override
        public String getDisplayName (Object node) 
        throws UnknownTypeException {
            return filter.getDisplayName (model, node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        @Override
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            return filter.getIconBase (model, node);
        }

        /**
         * Returns tool tip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tool tip for given node type
         * @return  tool tip for given node
         */
        @Override
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            return filter.getShortDescription (model, node);
        }


        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.isEmpty()) {
                    filter.addModelListener (this);
                    model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                removeLonelyModelRootChangelisteners(modelListeners);
                if (modelListeners.isEmpty()) {
                    filter.removeModelListener (this);
                    model.removeModelListener (this);
                }
            }
        }

        @Override
        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.TableValueChanged &&
                    (event.getSource() instanceof TableModel || event.getSource() instanceof TableModelFilter)) {
                // CompoundTableModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TreeChanged &&
                    (event.getSource() instanceof TreeModel || event.getSource() instanceof TreeModelFilter)) {
                // CompoundTreeModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }
        
        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            if (model instanceof CompoundNodeModel) {
                return n + filter + "\n" +
                    ((CompoundNodeModel) model).toString (n + "  ");
            }
            if (model instanceof DelegatingNodeModel) {
                return n + filter + "\n" +
                    ((DelegatingNodeModel) model).toString (n + "  ");
            }
            return n + filter + "\n" + 
                   n + "  " + model;
        }
    
        @Override
        public boolean canRename(Object node) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).canRename(model, node);
            } else {
                return model.canRename(node);
            }
        }

        @Override
        public boolean canCopy(Object node) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).canCopy(model, node);
            } else {
                return model.canCopy(node);
            }
        }

        @Override
        public boolean canCut(Object node) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).canCut(model, node);
            } else {
                return model.canCut(node);
            }
        }

        @Override
        public Transferable clipboardCopy(Object node) throws IOException,
                                                              UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).clipboardCopy(model, node);
            } else {
                return model.clipboardCopy(node);
            }
        }

        @Override
        public Transferable clipboardCut(Object node) throws IOException,
                                                             UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).clipboardCut(model, node);
            } else {
                return model.clipboardCut(node);
            }
        }

        @Override
        public int getAllowedDragActions() {
            if (dndfilter != null) {
                return dndfilter.getAllowedDragActions(model);
            } else {
                return model.getAllowedDragActions();
            }
        }

        @Override
        public int getAllowedDropActions(Transferable t) {
            if (dndfilter != null) {
                return dndfilter.getAllowedDropActions(model, t);
            } else {
                return model.getAllowedDropActions(t);
            }
        }

        @Override
        public Transferable drag(Object node) throws IOException,
                                                     UnknownTypeException {
            if (dndfilter != null) {
                return dndfilter.drag(model, node);
            } else {
                return model.drag(node);
            }
        }

        @Override
        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).getPasteTypes(model, node, t);
            } else {
                return model.getPasteTypes(node, t);
            }
        }

        @Override
        public PasteType getDropType(Object node, Transferable t, int action,
                                     int index) throws UnknownTypeException {
            if (dndfilter != null) {
                return dndfilter.getDropType(model, node, t, action, index);
            } else {
                return model.getDropType(node, t, action, index);
            }
        }

        @Override
        public void setName(Object node, String name) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                ((ExtendedNodeModelFilter) filter).setName(model, node, name);
            } else {
                model.setName(node, name);
            }
        }

        @Override
        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            if (filter instanceof ExtendedNodeModelFilter) {
                return ((ExtendedNodeModelFilter) filter).getIconBaseWithExtension(model, node);
            } else {
                String base;
                try {
                    base = filter.getIconBase(model, node);
                    if (base != null) {
                        base += ".gif";
                    }
                } catch (Exception utex) {
                    // The filter can not process the icon base filtering
                    // Perhaps it needs to be upgraded to ExtendedNodeModelFilter
                    Logger.getLogger(Models.class.getName()).log(Level.CONFIG,
                            "The filter "+filter+" does not perform icon base filtering for "+node+".\n"+
                            "If this is a problem, it should be upgraded to "+
                            "ExtendedNodeModelFilter and getIconBaseWithExtension() implemented.",
                            utex);
                    base = model.getIconBaseWithExtension(node);
                }
                return base;
            }
        }

        @Override
        public boolean isCheckable(Object node) throws UnknownTypeException {
            if (cfilter != null) {
                return cfilter.isCheckable(model, node);
            } else {
                return model.isCheckable(node);
            }
        }

        @Override
        public boolean isCheckEnabled(Object node) throws UnknownTypeException {
            if (cfilter != null) {
                return cfilter.isCheckEnabled(model, node);
            } else {
                return model.isCheckEnabled(node);
            }
        }

        @Override
        public Boolean isSelected(Object node) throws UnknownTypeException {
            if (cfilter != null) {
                return cfilter.isSelected(model, node);
            } else {
                return model.isSelected(node);
            }
        }

        @Override
        public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
            if (cfilter != null) {
                cfilter.setSelected(model, node, selected);
            } else {
                model.setSelected(node, selected);
            }
        }

    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.TableModel} for given TableModel and
     * {@link org.netbeans.spi.viewmodel.TableModelFilter}.
     * 
     * @author   Jan Jancura
     */
    private static final class CompoundTableModel implements TableHTMLModel, ModelListener {


        private TableHTMLModel model;
        private TableModelFilter filter;
        private TableHTMLModelFilter htmlFilter;

        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();


        /**
         * Creates {@link org.netbeans.spi.viewmodel.TableModel} for given TableModel and
         * {@link org.netbeans.spi.viewmodel.TableModelFilter}.
         */
        CompoundTableModel (TableHTMLModel model, TableModelFilter filter) {
            this.model = model;
            this.filter = filter;
        }
    
        CompoundTableModel (TableHTMLModel model, TableHTMLModelFilter htmlFilter) {
            this.model = model;
            this.htmlFilter = htmlFilter;
        }
    
        /**
         * Returns value to be displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return value of variable representing given position in tree table.
         */
        @Override
        public Object getValueAt (Object node, String columnID) throws 
        UnknownTypeException {
            if (filter != null) {
                return filter.getValueAt (model, node, columnID);
            } else {
                return model.getValueAt(node, columnID);
            }
        }

        @Override
        public boolean hasHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
            if (htmlFilter != null) {
                return htmlFilter.hasHTMLValueAt(model, node, columnID);
            } else {
                return model.hasHTMLValueAt(node, columnID);
            }
        }

        @Override
        public String getHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
            if (htmlFilter != null) {
                return htmlFilter.getHTMLValueAt(model, node, columnID);
            } else {
                return model.getHTMLValueAt(node, columnID);
            }
        }

        /**
         * Returns true if value displayed in column <code>columnID</code>
         * and row <code>node</code> is read only. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return true if variable on given position is read only
         */
        @Override
        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            if (filter != null) {
                return filter.isReadOnly (model, node, columnID);
            } else {
                return model.isReadOnly(node, columnID);
            }
        }

        /**
         * Changes a value displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @param value a new value of variable on given position
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         */
        @Override
        public void setValueAt (Object node, String columnID, Object value) 
        throws UnknownTypeException {
            if (filter != null) {
                filter.setValueAt (model, node, columnID, value);
            } else {
                model.setValueAt(node, columnID, value);
            }
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.isEmpty()) {
                    if (filter != null) {
                        filter.addModelListener (this);
                    }
                    model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                removeLonelyModelRootChangelisteners(modelListeners);
                if (modelListeners.isEmpty()) {
                    if (filter != null) {
                        filter.removeModelListener (this);
                    }
                    model.removeModelListener (this);
                }
            }
        }

        @Override
        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged && (event.getSource() instanceof NodeModel || event.getSource() instanceof NodeModelFilter)) {
                // CompoundNodeModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TreeChanged &&
                    (event.getSource() instanceof TreeModel || event.getSource() instanceof TreeModelFilter)) {
                // CompoundTreeModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }
        
        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            Model theFilter = (filter != null) ? filter : htmlFilter;
            if (model instanceof CompoundTableModel) {
                return n + theFilter + "\n" +
                    ((CompoundTableModel) model).toString (n + "  ");
            }
            if (model instanceof DelegatingTableModel) {
                return n + theFilter + "\n" +
                    ((DelegatingTableModel) model).toString (n + "  ");
            }
            return n + theFilter + "\n" + 
                   n + "  " + model;
        }
    }

    /**
     * Creates {@link org.netbeans.spi.viewmodel.TableRendererModel} for given TableRendererModel and
     * {@link org.netbeans.spi.viewmodel.TableRendererModelFilter}.
     */
    private static final class CompoundTableRendererModel implements TableRendererModel, ModelListener {


        private TableRendererModel model;
        private TableRendererModelFilter filter;

        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();


        /**
         * Creates {@link org.netbeans.spi.viewmodel.TableRendererModel} for given TableRendererModel and
         * {@link org.netbeans.spi.viewmodel.TableRendererModelFilter}.
         */
        CompoundTableRendererModel (TableRendererModel model, TableRendererModelFilter filter) {
            this.model = model;
            this.filter = filter;
        }

        @Override
        public boolean canRenderCell(Object node, String columnID) throws UnknownTypeException {
            return filter.canRenderCell(model, node, columnID);
        }

        @Override
        public TableCellRenderer getCellRenderer(Object node, String columnID) throws UnknownTypeException {
            return filter.getCellRenderer(model, node, columnID);
        }

        @Override
        public boolean canEditCell(Object node, String columnID) throws UnknownTypeException {
            return filter.canEditCell(model, node, columnID);
        }

        @Override
        public TableCellEditor getCellEditor(Object node, String columnID) throws UnknownTypeException {
            return filter.getCellEditor(model, node, columnID);
        }
        
        /**
         * Registers given listener.
         *
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.isEmpty()) {
                    filter.addModelListener (this);
                    model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /**
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                removeLonelyModelRootChangelisteners(modelListeners);
                if (modelListeners.isEmpty()) {
                    filter.removeModelListener (this);
                    model.removeModelListener (this);
                }
            }
        }

        @Override
        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged && (event.getSource() instanceof NodeModel || event.getSource() instanceof NodeModelFilter)) {
                // CompoundNodeModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TreeChanged &&
                    (event.getSource() instanceof TreeModel || event.getSource() instanceof TreeModelFilter)) {
                // CompoundTreeModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }

        public String toString (String n) {
            if (model instanceof CompoundTableRendererModel) {
                return n + filter + "\n" +
                    ((CompoundTableRendererModel) model).toString (n + "  ");
            }
            if (model instanceof DelegatingTableRendererModel) {
                return n + filter + "\n" +
                    ((DelegatingTableRendererModel) model).toString (n + "  ");
            }
            return n + filter + "\n" +
                   n + "  " + model;
        }

    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.TreeModel}
     * from given list of TreeModels. DelegatingTreeModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    private static final class DelegatingTreeModel implements ReorderableTreeModel {

        private TreeModel[] models;
        private HashMap<String, TreeModel> classNameToModel = new HashMap<String, TreeModel>();


        /**
         * Creates new instance of DelegatingTreeModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTreeModel (List<TreeModel> models) {
            this (convert (models));
        }

        private static TreeModel[] convert (List<TreeModel> l) {
            TreeModel[] models = new TreeModel [l.size ()];
                return l.toArray (models);
            }

        /**
         * Creates new instance of DelegatingTreeModel for given array of 
         * TableModels.
         *
         * @param models a array of TreeModel
         */
        private DelegatingTreeModel (TreeModel[] models) {
            this.models = models;        
        }
        
        /** 
         * Returns the root node of the tree or null, if the tree is empty.
         *
         * @return the root node of the tree or null
         */
        @Override
        public Object getRoot () {
            return models [0].getRoot ();
        }

        /** 
         * Returns children for given parent on given indexes.
         *
         * @param   parent a parent of returned nodes
         * @param   from a start index
         * @param   to a end index
         *
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  children for given parent on given indexes
         */
        @Override
        public Object[] getChildren (Object node, int from, int to)
        throws UnknownTypeException {
            TreeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.getChildren (node, from, to);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    Object[] v = models [i].getChildren (node, from, to);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return v;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         * @since 1.1
         */
        @Override
        public int getChildrenCount (Object node) 
        throws UnknownTypeException {
            TreeModel model = (TreeModel) classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.getChildrenCount (node);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    int result = models [i].getChildrenCount (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return result;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    

        /**
         * Returns true if node is leaf.
         * 
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         * @return  true if node is leaf
         */
        @Override
        public boolean isLeaf (Object node) throws UnknownTypeException {
            TreeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.isLeaf (node);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    boolean result = models [i].isLeaf (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return result;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        @Override
        public boolean canReorder(Object parent) throws UnknownTypeException {
            UnknownTypeException uex = null;
            TreeModel model = classNameToModel.get (
                parent.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ReorderableTreeModel) {
                    try {
                        return ((ReorderableTreeModel) model).canReorder (parent);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                }
            }
            int i, k = models.length;
            boolean isIndexed = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ReorderableTreeModel) {
                    try {
                        boolean cr = ((ReorderableTreeModel) models [i]).canReorder (parent);
                        //classNameToModel.put (parent.getClass ().getName (), models [i]);
                        return cr;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isIndexed = true;
                }
            }
            if (!isIndexed) {
                return false;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (parent);
            }
        }

        @Override
        public void reorder(Object parent, int[] perm) throws UnknownTypeException {
            TreeModel model = (TreeModel) classNameToModel.get (
                parent.getClass ().getName ()
            );
            if (model instanceof ReorderableTreeModel) {
                try {
                    ((ReorderableTreeModel) model).reorder(parent, perm);
                    return ;
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ReorderableTreeModel) {
                    try {
                        ((ReorderableTreeModel) models[i]).reorder(parent, perm);
                        //classNameToModel.put (parent.getClass ().getName (), models [i]);
                        return ;
                    } catch (UnknownTypeException e) {
                    }
                }
            }
            throw new UnknownTypeException (parent);
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].addModelListener (l);
            }
        }

        /**
         * Registers given listener.
         *
         * @param l the listener to add
         */
        void addModelListener (ModelListener l, Set<Model> modelsListenersAddedTo) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                TreeModel m = models [i];
                if (!modelsListenersAddedTo.add(m)) {
                    continue;
                }
                if (m instanceof DelegatingTreeModel) {
                    ((DelegatingTreeModel) m).addModelListener(l, modelsListenersAddedTo);
                } else {
                    m.addModelListener (l);
                }
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].removeModelListener (l);
            }
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) {
                return "";
            }
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
    }
    
    private static final class CompoundTablePropertyEditorsModel implements TablePropertyEditorsModel {
        
        private TablePropertyEditorsModel model;
        private TablePropertyEditorsModelFilter filter;
        
        CompoundTablePropertyEditorsModel(TablePropertyEditorsModel model, TablePropertyEditorsModelFilter filter) {
            this.model = model;
            this.filter = filter;
        }

        @Override
        public PropertyEditor getPropertyEditor(Object node, String columnID) throws UnknownTypeException {
            return filter.getPropertyEditor(model, node, columnID);
        }
        
        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }

        public String toString (String n) {
            if (model instanceof CompoundTablePropertyEditorsModel) {
                return n + filter + "\n" +
                    ((CompoundTablePropertyEditorsModel) model).toString (n + "  ");
            }
            if (model instanceof DelegatingTablePropertyEditorsModel) {
                return n + filter + "\n" +
                    ((DelegatingTablePropertyEditorsModel) model).toString (n + "  ");
            }
            return n + filter + "\n" +
                   n + "  " + model;
        }
    }
    
    /**
     * Creates {@link org.netbeans.spi.viewmodel.NodeActionsProvider} 
     * for given NodeActionsProvider and
     * {@link org.netbeans.spi.viewmodel.NodeActionsProviderFilter}.
     * 
     * @author   Jan Jancura
     */
    private static final class CompoundNodeActionsProvider 
    implements NodeActionsProvider {


        private NodeActionsProvider model;
        private NodeActionsProviderFilter filter;


        /**
         * Creates {@link org.netbeans.spi.viewmodel.NodeActionsProvider} 
         * for given NodeActionsProvider and
         * {@link org.netbeans.spi.viewmodel.NodeActionsProviderFilter}.
         */
        CompoundNodeActionsProvider (
            NodeActionsProvider model, 
            NodeActionsProviderFilter filter
        ) {
            this.model = model;
            this.filter = filter;
        }
    
        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider 
         *          implementation is not able to resolve actions 
         *          for given node type
         * @return  display name for given node
         */
        @Override
        public void performDefaultAction (Object node) 
        throws UnknownTypeException {
            filter.performDefaultAction (model, node);
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        @Override
        public Action[] getActions (Object node) 
        throws UnknownTypeException {
            return filter.getActions (model, node);
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            if (model instanceof CompoundNodeActionsProvider) {
                return n + filter + "\n" +
                    ((CompoundNodeActionsProvider) model).toString (n + "  ");
            }
            if (model instanceof DelegatingNodeActionsProvider) {
                return n + filter + "\n" +
                    ((DelegatingNodeActionsProvider) model).toString (n + "  ");
            }
            return n + filter + "\n" + 
                   n + "  " + model;
        }
    }
    
    private static final class CompoundTreeExpansionModel implements TreeExpansionModel, ModelListener {
        
        private TreeExpansionModel expansionModel;
        private TreeExpansionModelFilter expansionFilter;
        
        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();
        
        CompoundTreeExpansionModel(TreeExpansionModel expansionModel, TreeExpansionModelFilter expansionFilter) {
            this.expansionModel = expansionModel;
            this.expansionFilter = expansionFilter;
        }

        @Override
        public boolean isExpanded(Object node) throws UnknownTypeException {
            return expansionFilter.isExpanded(expansionModel, node);
        }

        @Override
        public void nodeExpanded(Object node) {
            expansionModel.nodeExpanded(node);
            expansionFilter.nodeExpanded(node);
        }

        @Override
        public void nodeCollapsed(Object node) {
            expansionModel.nodeCollapsed(node);
            expansionFilter.nodeCollapsed(node);
        }
        
        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            synchronized (modelListeners) {
                if (modelListeners.isEmpty()) {
                    expansionFilter.addModelListener (this);
                    //model.addModelListener (this);
                }
                modelListeners.add(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            synchronized (modelListeners) {
                modelListeners.remove(l);
                removeLonelyModelRootChangelisteners(modelListeners);
                if (modelListeners.isEmpty()) {
                    expansionFilter.removeModelListener (this);
                    //model.removeModelListener (this);
                }
            }
        }

        @Override
        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged && (event.getSource() instanceof NodeModel || event.getSource() instanceof NodeModelFilter)) {
                // CompoundNodeModel.modelChanged() takes this.
                return ;
            }
            if (event instanceof ModelEvent.TableValueChanged &&
                    (event.getSource() instanceof TableModel || event.getSource() instanceof TableModelFilter)) {
                // CompoundTableModel.modelChanged() takes this.
                return ;
            }
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }
        
    }

    private static final class  CompoundAsynchronousModel implements AsynchronousModel {
        private AsynchronousModel asynchModel;
        private AsynchronousModelFilter asynchModelFilter;

        CompoundAsynchronousModel(AsynchronousModel asynchModel, AsynchronousModelFilter asynchModelFilter) {
            this.asynchModel = asynchModel;
            this.asynchModelFilter = asynchModelFilter;
        }

        @Override
        public Executor asynchronous(CALL asynchCall, Object node) throws UnknownTypeException {
            return asynchModelFilter.asynchronous(asynchModel.asynchronous(asynchCall, node), asynchCall, node);
        }

    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.TableModel}
     * from given list of TableModels. DelegatingTableModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    private static final class DelegatingTableModel implements TableModel, TableHTMLModel {

        private TableModel[] models;
        private HashMap<String, TableModel> classNameToModel = new HashMap<String, TableModel>();


        /**
         * Creates new instance of DelegatingTableModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTableModel (List<TableModel> models) {
            this (convert (models));
        }

        private static TableModel[] convert (List<TableModel> l) {
            TableModel[] models = new TableModel [l.size ()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingTableModel for given array of 
         * TableModels.
         *
         * @param models a array of TableModels
         */
        DelegatingTableModel (TableModel[] models) {
            this.models = models;        
        }

        /**
         * Returns value to be displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return value of variable representing given position in tree table.
         */
        @Override
        public Object getValueAt (Object node, String columnID)
        throws UnknownTypeException {
            TableModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null)  {
                try {
                    return model.getValueAt (node, columnID);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    Object v = models [i].getValueAt (node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return v;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    

        /**
         * Returns true if value displayed in column <code>columnID</code>
         * and row <code>node</code> is read only. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return true if variable on given position is read only
         */
        @Override
        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            TableModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.isReadOnly (node, columnID);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    boolean ro = models [i].isReadOnly (node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return ro;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /**
         * Changes a value displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@TreeModel#getChildren}.
         *
         * @param node a object returned from {@TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @param value a new value of variable on given position
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         */
        @Override
        public void setValueAt (Object node, String columnID, Object value)
        throws UnknownTypeException {
            TableModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    model.setValueAt (node, columnID, value);
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    models [i].setValueAt (node, columnID, value);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].addModelListener (l);
            }
        }

        /**
         * Registers given listener.
         *
         * @param l the listener to add
         */
        void addModelListener (ModelListener l, Set<Model> modelsListenersAddedTo) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                TableModel m = models [i];
                if (!modelsListenersAddedTo.add(m)) {
                    continue;
                }
                if (m instanceof DelegatingTableModel) {
                    ((DelegatingTableModel) m).addModelListener(l, modelsListenersAddedTo);
                } else {
                    m.addModelListener (l);
                }
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].removeModelListener (l);
            }
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) {
                return "";
            }
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
        
        // HTML extension:

        private boolean defaultHasHTMLValueAt() {
            return false;
        }
        
        @Override
        public boolean hasHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
            UnknownTypeException uex = null;
            TableModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof TableHTMLModel) {
                    try {
                        return ((TableHTMLModel) model).hasHTMLValueAt(node, columnID);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultHasHTMLValueAt();
                }
            }
            int i, k = models.length;
            boolean isHTML = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof TableHTMLModel) {
                    try {
                        boolean has = ((TableHTMLModel) models [i]).hasHTMLValueAt(node, columnID);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return has;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isHTML = true;
                }
            }
            if (!isHTML) {
                return defaultHasHTMLValueAt();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public String getHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
            UnknownTypeException uex = null;
            TableModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof TableHTMLModel) {
                    try {
                        return ((TableHTMLModel) model).getHTMLValueAt(node, columnID);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return null;
                }
            }
            int i, k = models.length;
            boolean isHTML = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof TableHTMLModel) {
                    try {
                        String htmlValue = ((TableHTMLModel) models [i]).getHTMLValueAt(node, columnID);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return htmlValue;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isHTML = true;
                }
            }
            if (!isHTML) {
                return null;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }
    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.TableRendererModel}
     * from given list of TableRendererModel. DelegatingTableRendererModel asks all underlaying
     * models for each concrete parameter, and returns first returned value.
     */
    private static final class DelegatingTableRendererModel implements TableRendererModel {

        private TableRendererModel[] models;
        private HashMap<String, TableRendererModel> classNameToModel = new HashMap<String, TableRendererModel>();


        /**
         * Creates new instance of DelegatingTableModel for given list of
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTableRendererModel (List<TableRendererModel> models) {
            this (convert (models));
        }

        private static TableRendererModel[] convert (List<TableRendererModel> l) {
            TableRendererModel[] models = new TableRendererModel [l.size ()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingTableModel for given array of
         * TableModels.
         *
         * @param models a array of TableModels
         */
        DelegatingTableRendererModel (TableRendererModel[] models) {
            this.models = models;
        }

        @Override
        public boolean canRenderCell(Object node, String columnID) throws UnknownTypeException {
            TableRendererModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.canRenderCell (node, columnID);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    boolean cr = models [i].canRenderCell (node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return cr;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        @Override
        public TableCellRenderer getCellRenderer(Object node, String columnID) throws UnknownTypeException {
            TableRendererModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.getCellRenderer (node, columnID);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    TableCellRenderer cr = models [i].getCellRenderer (node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return cr;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        @Override
        public boolean canEditCell(Object node, String columnID) throws UnknownTypeException {
            TableRendererModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.canEditCell (node, columnID);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    boolean ce = models [i].canEditCell (node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return ce;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        @Override
        public TableCellEditor getCellEditor(Object node, String columnID) throws UnknownTypeException {
            TableRendererModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.getCellEditor (node, columnID);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    TableCellEditor ce = models [i].getCellEditor (node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return ce;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }
        
        /**
         * Registers given listener.
         *
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].addModelListener (l);
            }
        }

        /**
         * Registers given listener.
         *
         * @param l the listener to add
         */
        void addModelListener (ModelListener l, Set<Model> modelsListenersAddedTo) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                TableRendererModel m = models [i];
                if (!modelsListenersAddedTo.add(m)) {
                    continue;
                }
                if (m instanceof DelegatingTableRendererModel) {
                    ((DelegatingTableRendererModel) m).addModelListener(l, modelsListenersAddedTo);
                } else {
                    m.addModelListener (l);
                }
            }
        }

        /**
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].removeModelListener (l);
            }
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }

        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) {
                return "";
            }
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }

    }
    
    private static final class DelegatingTablePropertyEditorsModel implements TablePropertyEditorsModel {
        
        private TablePropertyEditorsModel[] models;
        private HashMap<String, TablePropertyEditorsModel> classNameToModel = new HashMap<String, TablePropertyEditorsModel>();
        
        DelegatingTablePropertyEditorsModel(List<TablePropertyEditorsModel> models) {
            this(convert(models));
        }
        
        private static TablePropertyEditorsModel[] convert(List<TablePropertyEditorsModel> l) {
            TablePropertyEditorsModel[] models = new TablePropertyEditorsModel[l.size()];
            return l.toArray(models);
        }
        
        DelegatingTablePropertyEditorsModel(TablePropertyEditorsModel[] models) {
            this.models = models;
        }

        @Override
        public PropertyEditor getPropertyEditor(Object node, String columnID) throws UnknownTypeException {
            UnknownTypeException utex = null;
            TablePropertyEditorsModel model = classNameToModel.get(
                    node.getClass().getName()
            );
            if (model != null) {
                try {
                    return model.getPropertyEditor(node, columnID);
                } catch (UnknownTypeException e) {
                    utex = e;
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    PropertyEditor pe = models [i].getPropertyEditor(node, columnID);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return pe;
                } catch (UnknownTypeException e) {
                    utex = e;
                }
            }
            if (k == 0) {
                return null;
            }
            if (utex != null) {
                throw utex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }

        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) {
                return "";
            }
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.TableModel}
     * from given list of TableModels. DelegatingTableModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    private static final class DelegatingTreeExpansionModel implements TreeExpansionModel {

        private TreeExpansionModel[] models;
        private HashMap<String, TreeExpansionModel> classNameToModel = new HashMap<String, TreeExpansionModel>();


        /**
         * Creates new instance of DelegatingTableModel for given list of 
         * TableModels.
         *
         * @param models a list of TableModels
         */
        DelegatingTreeExpansionModel (List<TreeExpansionModel> models) {
            this (convert (models));
        }

        private static TreeExpansionModel[] convert (List<TreeExpansionModel> l) {
            TreeExpansionModel[] models = new TreeExpansionModel [l.size()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingTableModel for given array of 
         * TableModels.
         *
         * @param models a array of TableModels
         */
        private DelegatingTreeExpansionModel (TreeExpansionModel[] models) {
            this.models = models;        
        }

        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        @Override
        public boolean isExpanded (Object node) 
        throws UnknownTypeException {
            TreeExpansionModel model = 
                classNameToModel.get (
                    node.getClass ().getName ()
                );
            if (model != null) {
                try {
                    return model.isExpanded (node);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    boolean result = models [i].isExpanded (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return result;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }    


        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        @Override
        public void nodeExpanded (Object node) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].nodeExpanded (node);
            }
        }    

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        @Override
        public void nodeCollapsed (Object node) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].nodeCollapsed (node);
            }
        }    

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) {
                return "";
            }
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
    }

    private static class DefaultTreeExpansionModel implements TreeExpansionModel {
        
        private Reference<CompoundModel> cmRef;
        private CompoundModel oldCM;
        
        public DefaultTreeExpansionModel() {
        }
        
        private DefaultTreeExpansionModel(CompoundModel oldCM) {
            this.oldCM = oldCM;
        }
        
        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        @Override
        public boolean isExpanded (Object node) 
        throws UnknownTypeException {
            CompoundModel cm = cmRef.get();
            if (cm == null) {
                return false;
            }
            return DefaultTreeExpansionManager.get(cm).isExpanded(node);
        }

        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        @Override
        public void nodeExpanded (Object node) {
            CompoundModel cm = cmRef.get();
            if (cm == null) {
                return ;
            }
            DefaultTreeExpansionManager.get(cm).setExpanded(node);
        }

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        @Override
        public void nodeCollapsed (Object node) {
            CompoundModel cm = cmRef.get();
            if (cm == null) {
                return ;
            }
            DefaultTreeExpansionManager.get(cm).setCollapsed(node);
        }

        private void setCompoundModel(CompoundModel cm) {
            if (oldCM != null) {
                DefaultTreeExpansionManager.copyExpansions(oldCM, cm);
                oldCM = null;
            }
            cmRef = new WeakReference<CompoundModel>(cm);
        }
        
        private DefaultTreeExpansionModel cloneForNewModel() {
            return new DefaultTreeExpansionModel(cmRef.get());
        }

    }

    private static final class DefaultAsynchronousModel implements AsynchronousModel {

        @Override
        public Executor asynchronous(CALL asynchCall, Object node) {
            if (asynchCall.equals(CALL.CHILDREN) || asynchCall.equals(CALL.VALUE)) {
                // For backward compatibility
                return AsynchronousModelFilter.DEFAULT;
            } else {
                return AsynchronousModelFilter.CURRENT_THREAD;
            }
        }
        
    }

    /**
     * Creates one {@link org.netbeans.spi.viewmodel.NodeModel}
     * from given list of NodeModels. DelegatingNodeModel asks all underlaying 
     * models for each concrete parameter, and returns first returned value.
     *
     * @author   Jan Jancura
     */
    private static final class DelegatingNodeModel implements SuperNodeModel {

        private NodeModel[] models;
        private HashMap<String, NodeModel> classNameToModel = new HashMap<String, NodeModel>();


        /**
         * Creates new instance of DelegatingNodeModel for given list of 
         * NodeModels.
         *
         * @param models a list of NodeModels
         */
        DelegatingNodeModel (
            List<NodeModel> models
        ) {
            this (convert (models));
        }

        private static NodeModel[] convert (List<NodeModel> l) {
            NodeModel[] models = new NodeModel [l.size ()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingNodeModel for given array of 
         * NodeModels.
         *
         * @param models a array of NodeModels
         */
        DelegatingNodeModel (
            NodeModel[] models
        ) {
            this.models = models;

        }
        
        NodeModel[] getModels() {
            return models;
        }

        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        @Override
        public String getDisplayName (Object node) 
        throws UnknownTypeException {
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.getDisplayName (node);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    String dn = models [i].getDisplayName (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return dn;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /**
         * Returns tool tip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tool tip for given node type
         * @return  tool tip for given node
         */
        @Override
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.getShortDescription (node);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    String dn = models [i].getShortDescription (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return dn;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        @Override
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.getIconBase (node);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    String dn = models [i].getIconBase (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return dn;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].addModelListener (l);
            }
        }

        /**
         * Registers given listener.
         *
         * @param l the listener to add
         */
        void addModelListener (ModelListener l, Set<Model> modelsListenersAddedTo) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                NodeModel m = models [i];
                if (!modelsListenersAddedTo.add(m)) {
                    continue;
                }
                if (m instanceof DelegatingNodeModel) {
                    ((DelegatingNodeModel) m).addModelListener(l, modelsListenersAddedTo);
                } else {
                    m.addModelListener (l);
                }
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                models [i].removeModelListener (l);
            }
        }

        @Override
        public String toString () {
            return toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) {
                return "";
            }
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
        
        // Extensions:
        
        private boolean defaultCanRename() {
            return false;
        }
        
        private boolean defaultCanCopy() {
            return false;
        }
    
        private boolean defaultCanCut() {
            return false;
        }
    
        private Transferable defaultClipboardCopy() throws IOException {
            return null;
        }

        private Transferable defaultClipboardCut() throws IOException {
            return null;
        }

        private Transferable defaultDrag() throws IOException {
            return null;
        }

        private PasteType[] defaultGetPasteTypes(Transferable t) {
            return null;
        }

        private PasteType defaultGetDropType(Transferable t, int action,
                                            int index) {
            return null;
        }

        private void defaultSetName(String name) {
            // nothing
        }

        @Override
        public boolean canRename(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).canRename (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultCanRename();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        boolean cr = ((ExtendedNodeModel) models [i]).canRename (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return cr;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultCanRename();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public boolean canCopy(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).canCopy (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultCanCopy();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        boolean cr = ((ExtendedNodeModel) models [i]).canCopy (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return cr;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultCanCopy();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public boolean canCut(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).canCut (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultCanCut();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        boolean cr = ((ExtendedNodeModel) models [i]).canCut (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return cr;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultCanCut();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public Transferable clipboardCopy(Object node) throws IOException,
                                                              UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).clipboardCopy (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultClipboardCopy();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        Transferable t = ((ExtendedNodeModel) models [i]).clipboardCopy (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return t;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultClipboardCopy();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public Transferable clipboardCut(Object node) throws IOException,
                                                             UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).clipboardCut (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultClipboardCut();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        Transferable t = ((ExtendedNodeModel) models [i]).clipboardCut (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return t;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultClipboardCut();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public int getAllowedDragActions() {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof DnDNodeModel) {
                    return ((DnDNodeModel) models[i]).getAllowedDragActions();
                }
            }
            return DEFAULT_DRAG_DROP_ALLOWED_ACTIONS;
        }

        @Override
        public int getAllowedDropActions(Transferable t) {
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof DnDNodeModel) {
                    return ((DnDNodeModel) models[i]).getAllowedDropActions(t);
                }
            }
            return DEFAULT_DRAG_DROP_ALLOWED_ACTIONS;
        }

        @Override
        public Transferable drag(Object node) throws IOException,
                                                     UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = (NodeModel) classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof DnDNodeModel) {
                    try {
                        return ((DnDNodeModel) model).drag (node);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultDrag();
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof DnDNodeModel) {
                    try {
                        Transferable t = ((DnDNodeModel) models [i]).drag (node);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return t;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultDrag();
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        return ((ExtendedNodeModel) model).getPasteTypes (node, t);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultGetPasteTypes(t);
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        PasteType[] p = ((ExtendedNodeModel) models [i]).getPasteTypes (node, t);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return p;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultGetPasteTypes(t);
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public PasteType getDropType(Object node, Transferable t, int action,
                                     int index) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = (NodeModel) classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof DnDNodeModel) {
                    try {
                        return ((DnDNodeModel) model).getDropType (node, t, action, index);
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    return defaultGetDropType(t, action, index);
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof DnDNodeModel) {
                    try {
                        PasteType p = ((DnDNodeModel) models [i]).getDropType (node, t, action, index);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return p;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                return defaultGetDropType(t, action, index);
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public void setName(Object node, String name) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                if (model instanceof ExtendedNodeModel) {
                    try {
                        ((ExtendedNodeModel) model).setName (node, name);
                        return ;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                } else {
                    defaultSetName(name);
                    return ;
                }
            }
            int i, k = models.length;
            boolean isExtended = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof ExtendedNodeModel) {
                    try {
                        ((ExtendedNodeModel) models [i]).setName (node, name);
                        classNameToModel.put (node.getClass ().getName (), models [i]);
                        return ;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isExtended = true;
                }
            }
            if (!isExtended) {
                defaultSetName(name);
                return ;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            NodeModel model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    if (model instanceof ExtendedNodeModel) {
                        return ((ExtendedNodeModel) model).getIconBaseWithExtension (node);
                    } else {
                        String base = model.getIconBase(node);
                        if (base != null) {
                            return base + ".gif";
                        } else {
                            return null;
                        }
                    }
                } catch (UnknownTypeException e) {
                    uex = e;
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    String ib;
                    if (models[i] instanceof ExtendedNodeModel) {
                        ib = ((ExtendedNodeModel) models [i]).getIconBaseWithExtension (node);
                    } else {
                        String base = models[i].getIconBase(node);
                        if (base != null) {
                            ib = base + ".gif";
                        } else {
                            ib = null;
                        }
                    }
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return ib;
                } catch (UnknownTypeException e) {
                    uex = e;
                }
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
            
        }

        @Override
        public boolean isCheckable(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            int i, k = models.length;
            boolean isChecked = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof CheckNodeModel) {
                    try {
                        Boolean checkable = ((CheckNodeModel) models [i]).isCheckable(node);
                        return checkable;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isChecked = true;
                }
            }
            if (!isChecked) {
                return false;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public boolean isCheckEnabled(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof CheckNodeModel) {
                    try {
                        boolean checkEnabled = ((CheckNodeModel) models [i]).isCheckEnabled(node);
                        return checkEnabled;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                }
            }
            if (uex != null) {
                throw uex;
            } else {
                return true;
            }
        }

        @Override
        public Boolean isSelected(Object node) throws UnknownTypeException {
            UnknownTypeException uex = null;
            int i, k = models.length;
            boolean isChecked = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof CheckNodeModel) {
                    try {
                        Boolean selected = ((CheckNodeModel) models [i]).isSelected(node);
                        return selected;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isChecked = true;
                }
            }
            if (!isChecked) {
                return false;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

        @Override
        public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
            UnknownTypeException uex = null;
            int i, k = models.length;
            boolean isChecked = false;
            for (i = 0; i < k; i++) {
                if (models[i] instanceof CheckNodeModel) {
                    try {
                        ((CheckNodeModel) models [i]).setSelected (node, selected);
                        return ;
                    } catch (UnknownTypeException e) {
                        uex = e;
                    }
                    isChecked = true;
                }
            }
            if (!isChecked) {
                Exceptions.printStackTrace(new IllegalStateException("Can not set selected state to model "+this));
                return ;
            }
            if (uex != null) {
                throw uex;
            } else {
                throw new UnknownTypeException (node);
            }
        }

    }

    /**
     * Empty implementation of {@link org.netbeans.spi.viewmodel.TreeModel}.
     *
     * @author   Jan Jancura
     */
    private static final class EmptyTreeModel implements TreeModel {

        /** 
         * Returns {@link org.netbeans.spi.viewmodel.TreeModel#ROOT}.
         *
         * @return {@link org.netbeans.spi.viewmodel.TreeModel#ROOT}
         */
        @Override
        public Object getRoot () {
            return ROOT;
        }

        /** 
         * Returns empty array.
         *
         * @return empty array
         */
        @Override
        public Object[] getChildren (Object parent, int from, int to) {
            return new Object [0];
        }
    
        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        @Override
        public int getChildrenCount (Object node) {
            return 0;
        }

        /**
         * Returns false.
         *
         * @return false
         */
        @Override
        public boolean isLeaf (Object node) {
            return false;
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be added
         */
        @Override
        public void addModelListener (ModelListener l) {
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be removed
         */
        @Override
        public void removeModelListener (ModelListener l) {
        }
    }

    /**
     * Empty implementation of {@link org.netbeans.spi.viewmodel.NodeModel}.
     *
     * @author   Jan Jancura
     */
    private static final class EmptyNodeModel implements NodeModel {

        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        @Override
        public String getDisplayName (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        @Override
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Returns tool tip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tool tip for given node type
         * @return  tool tip for given node
         */
        @Override
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be added
         */
        @Override
        public void addModelListener (ModelListener l) {
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be removed
         */
        @Override
        public void removeModelListener (ModelListener l) {
        }
    }

    /**
     * Empty implemntation of {@link org.netbeans.spi.viewmodel.TableModel}.
     *
     * @author   Jan Jancura
     */
    private static final class EmptyTableModel implements TableModel {
 
        /**
         * Returns value to be displayed in column <code>columnID</code>
         * and row identified by <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@link org.netbeans.spi.viewmodel.TreeModel#getChildren}.
         *
         * @param node a object returned from 
         *         {@link org.netbeans.spi.viewmodel.TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return value of variable representing given position in tree table.
         */
        @Override
        public Object getValueAt (Object node, String columnID) throws 
        UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Returns true if value displayed in column <code>columnID</code>
         * and row <code>node</code> is read only. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@link TreeModel#getChildren}.
         *
         * @param node a object returned from {@link TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         *
         * @return true if variable on given position is read only
         */
        @Override
        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Changes a value displayed in column <code>columnID</code>
         * and row <code>node</code>. Column ID is defined in by 
         * {@link ColumnModel#getID}, and rows are defined by values returned from 
         * {@link TreeModel#getChildren}.
         *
         * @param node a object returned from {@link TreeModel#getChildren} for this row
         * @param columnID a id of column defined by {@link ColumnModel#getID}
         * @param value a new value of variable on given position
         * @throws UnknownTypeException if there is no TableModel defined for given
         *         parameter type
         */
        @Override
        public void setValueAt (Object node, String columnID, Object value) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }
        
        /** 
         * Do nothing.
         *
         * @param l the listener to be added
         */
        @Override
        public void addModelListener (ModelListener l) {
        }

        /** 
         * Do nothing.
         *
         * @param l the listener to be removed
         */
        @Override
        public void removeModelListener (ModelListener l) {
        }
    }

    /**
     * Empty implementation of {@link org.netbeans.spi.viewmodel.TableModel}.
     *
     * @author   Jan Jancura
     */
    private static final class EmptyNodeActionsProvider implements 
    NodeActionsProvider {
    
        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        @Override
        public void performDefaultAction (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        @Override
        public Action[] getActions (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }
    }
    
    /**
     * Creates one {@link org.netbeans.spi.viewmodel.NodeActionsProvider}
     * from given list of NodeActionsProviders. DelegatingNodeActionsProvider asks all 
     * underlaying models for each concrete parameter, and returns first 
     * returned value.
     *
     * @author   Jan Jancura
     */
    static final class DelegatingNodeActionsProvider implements NodeActionsProvider {

        private NodeActionsProvider[] models;
        private HashMap<String, NodeActionsProvider> classNameToModel = new HashMap<String, NodeActionsProvider>();


        /**
         * Creates new instance of DelegatingNodeActionsProvider for given list of 
         * NodeActionsProvider.
         *
         * @param models a list of NodeActionsProvider
         */
        public DelegatingNodeActionsProvider (
            List<NodeActionsProvider> models
        ) {
            this (convert (models));
        }

        private static NodeActionsProvider[] convert (List<NodeActionsProvider> l) {
            NodeActionsProvider[] models = new NodeActionsProvider [l.size ()];
            return l.toArray (models);
        }

        /**
         * Creates new instance of DelegatingNodeActionsProvider for given array of 
         * NodeActionsProvider.
         *
         * @param models a array of NodeActionsProvider
         */
        public DelegatingNodeActionsProvider (NodeActionsProvider[] models) {
            this.models = models;
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        @Override
        public Action[] getActions (Object node) 
        throws UnknownTypeException {
            NodeActionsProvider model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    return model.getActions (node);
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    Action[] dn = models [i].getActions (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return dn;
                } catch (UnknownTypeException e) {
                }
            }
            if (k == 0) {
                return new Action[] {};
            } else {
                throw new UnknownTypeException (node);
            }
        }

        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        @Override
        public void performDefaultAction (Object node) throws UnknownTypeException {
            NodeActionsProvider model = classNameToModel.get (
                node.getClass ().getName ()
            );
            if (model != null) {
                try {
                    model.performDefaultAction (node);
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            int i, k = models.length;
            for (i = 0; i < k; i++) {
                try {
                    models [i].performDefaultAction (node);
                    classNameToModel.put (node.getClass ().getName (), models [i]);
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException (node);
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + toString ("    ");
        }
        
        public String toString (String n) {
            int i, k = models.length - 1;
            if (k == -1) {
                return "";
            }
            StringBuffer sb = new StringBuffer ();
            for (i = 0; i < k; i++) {
                sb.append (n);
                sb.append (models [i]);
                sb.append ('\n');
            }
            sb.append (n);
            sb.append (models [i]);
            return new String (sb);
        }
    }
    
    /**
     * Tree expansion control.
     * @since 1.15
     */
    public abstract static class TreeFeatures {
        
        /**
         * Returns <code>true</code> if given node is expanded.
         *
         * @param node a node to be checked
         * @return <code>true</code> if given node is expanded
         */
        public abstract boolean isExpanded (Object node);
        
        /**
         * Expands given list of nodes.
         *
         * @param node a list of nodes to be expanded
         */
        public abstract void expandNode (Object node);
        
        /**
         * Collapses given node.
         *
         * @param node a node to be expanded
         */
        public abstract void collapseNode (Object node);
        
    }
    
    /**
     * Implements set of tree view features.
     */
    private static final class DefaultTreeFeatures extends TreeFeatures {
        
        private JComponent view;
        
        private DefaultTreeFeatures (JComponent view) {
            this.view = view;
        }
        
        /**
         * Returns <code>true</code> if given node is expanded.
         *
         * @param node a node to be checked
         * @return <code>true</code> if given node is expanded
         */
        @Override
        public boolean isExpanded (
            Object node
        ) {
            return ((OutlineTable) view).isExpanded (node);
        }

        /**
         * Expands given list of nodes.
         *
         * @param node a list of nodes to be expanded
         */
        @Override
        public void expandNode (
            Object node
        ) {
            ((OutlineTable) view).expandNode (node);
        }

        /**
         * Collapses given node.
         *
         * @param node a node to be expanded
         */
        @Override
        public void collapseNode (
            Object node
        ) {
            ((OutlineTable) view).collapseNode (node);
        }
    }

    /**
     * This model encapsulates all currently supported models.
     *
     * @see Models#createCompoundModel
     * @author   Jan Jancura
     */
    public static final class CompoundModel implements ReorderableTreeModel,
                                                       ExtendedNodeModel,
                                                       CheckNodeModel,
                                                       DnDNodeModel,
                                                       NodeActionsProvider,
                                                       TableHTMLModel,
                                                       TreeExpansionModel,
                                                       TableRendererModel,
                                                       TablePropertyEditorsModel {

        private ReorderableTreeModel treeModel;
        private ExtendedNodeModel nodeModel;
        private CheckNodeModel cnodeModel;
        private DnDNodeModel    dndNodeModel;
        private NodeActionsProvider nodeActionsProvider;
        private ColumnModel[]   columnModels;
        private TableHTMLModel  tableModel;
        private TableRendererModel tableRendererModel;
        private TablePropertyEditorsModel tablePropertyEditorsModel;
        private TreeExpansionModel treeExpansionModel;
        private AsynchronousModel asynchModel;

        private CompoundModel   mainSubModel;
        private CompoundModel[] subModels;
        private TreeModelFilter subModelsFilter;
        private MessageFormat treeNodeDisplayFormat;
        
        // <RAVE>
        // New field, setter/getter for propertiesHelpID, which is used
        // for property sheet help
        private String propertiesHelpID = null;
        // </RAVE>
        
        // init ....................................................................

        /**
         * Creates a new instance of {@link CompoundModel} for given models.
         *
         * @param treeModel a tree model to delegate on
         * @param nodeModel a node model to delegate on
         * @param nodeActionsProvider a node actions provider to delegate on
         * @param nodeActionsProvider a columns model to delegate on
         */
        private CompoundModel (
            ReorderableTreeModel treeModel,
            TreeExpansionModel treeExpansionModel,
            ExtendedNodeModel nodeModel, 
            NodeActionsProvider nodeActionsProvider,
            List<ColumnModel> columnModels,
            TableHTMLModel tableModel,
            AsynchronousModel asynchModel,
            TableRendererModel tableRendererModel,
            TablePropertyEditorsModel tablePropertyEditorsModel,
            String propertiesHelpID
        ) {
            if (treeModel == null || nodeModel == null || tableModel == null ||
                nodeActionsProvider == null) {
                
                throw new NullPointerException ();
            }
            /*if (columnModels == null && tableModel == null && tableRendererModel == null) {
                isTree = true;
            } else {
                if (tableModel == null) throw new NullPointerException ();
            }*/

            this.treeModel = treeModel;
            this.treeExpansionModel = treeExpansionModel;
            this.nodeModel = nodeModel;
            if (nodeModel instanceof CheckNodeModel) {
                this.cnodeModel = (CheckNodeModel) nodeModel;
            }
            if (nodeModel instanceof DnDNodeModel) {
                this.dndNodeModel = (DnDNodeModel) nodeModel;
            }
            this.tableModel = tableModel;
            this.tableRendererModel = tableRendererModel;
            this.tablePropertyEditorsModel = tablePropertyEditorsModel;
            this.nodeActionsProvider = nodeActionsProvider;
            this.columnModels = columnModels.toArray(new ColumnModel[0]);
            this.asynchModel = asynchModel;
            this.propertiesHelpID = propertiesHelpID;
        }

        private CompoundModel(CompoundModel mainSubModel,
                              CompoundModel[] models,
                              TreeModelFilter treeFilter,
                              String propertiesHelpID) {
            this.mainSubModel = mainSubModel;
            this.subModels = models;
            this.subModelsFilter = treeFilter;
            this.propertiesHelpID = propertiesHelpID;
        }
        
        void setTreeNodeDisplayFormat(MessageFormat treeNodeDisplayFormat) {
            this.treeNodeDisplayFormat = treeNodeDisplayFormat;
        }
        
        MessageFormat getTreeNodeDisplayFormat() {
            if (isHyperModel()) {
                return mainSubModel.getTreeNodeDisplayFormat();
            } else {
                return treeNodeDisplayFormat;
            }
        }
        
        /*boolean isTree() {
            return columnModels == null;
        }*/

        /*CompoundModel[] getSubModels() {
            return subModels;
        }

        TreeModelFilter getSubModelsFilter() {
            return subModelsFilter;
        }*/

        boolean isHyperModel() {
            return subModels != null;
        }

        HyperCompoundModel createHyperModel() {
            if (!isHyperModel()) {
                throw new IllegalStateException();
            }
            return new HyperCompoundModel(mainSubModel, subModels, subModelsFilter);
        }

        // <RAVE>
        /**
         * Get a help ID for this model.
         * @return The help ID defined for the properties sheets,
         *         or <code>null</code>.
         * @since 1.7
         */
        public String getHelpId() {
            return propertiesHelpID;
        }
        // </RAVE>

        // TreeModel ...............................................................

        /** 
         * Returns the root node of the tree or null, if the tree is empty.
         *
         * @return the root node of the tree or null
         */
        @Override
        public Object getRoot () {
            return treeModel.getRoot ();
        }

        /** 
         * Returns children for given parent on given indexes.
         *
         * @param   parent a parent of returned nodes
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  children for given parent on given indexes
         */
        @Override
        public Object[] getChildren (Object parent, int from, int to) 
        throws UnknownTypeException {
            Object[] ch = treeModel.getChildren (parent, from, to);
            //System.err.println("Children for node '"+parent+"' are '"+java.util.Arrays.asList(ch)+"'");
            //System.err.println("Model = "+this);
            return ch;
        }

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        @Override
        public int getChildrenCount (Object node) throws UnknownTypeException {
            return treeModel.getChildrenCount (node);
        }

        /**
         * Returns true if node is leaf.
         * 
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         * @return  true if node is leaf
         */
        @Override
        public boolean isLeaf (Object node) throws UnknownTypeException {
            return treeModel.isLeaf (node);
        }

        // ReorderableTreeModel ...............................................................

        @Override
        public boolean canReorder(Object parent) throws UnknownTypeException {
            return treeModel.canReorder(parent);
        }

        @Override
        public void reorder(Object parent, int[] perm) throws UnknownTypeException {
            treeModel.reorder(parent, perm);
        }

        // NodeModel ...............................................................

        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        @Override
        public String getDisplayName (Object node) throws UnknownTypeException {
            if (nodeModel instanceof DelegatingNodeModel) {
                NodeModel[] subModels = ((DelegatingNodeModel) nodeModel).getModels();
                if (subModels.length == 0) {
                    if (TreeModel.ROOT.equals(node)) {
                        ColumnModel[] columns = getColumns();
                        for (ColumnModel cm : columns) {
                            if (cm.getType() == null) {
                                return Actions.cutAmpersand(cm.getDisplayName());
                            }
                        }
                    }
                    return ""; // Nothing when there are no models
                }
            }
            String dn = nodeModel.getDisplayName (node);
            //System.err.println("DisplayName for node '"+node+"' is: '"+dn+"'");
            //System.err.println("Model = "+this);
            return dn;
        }

        /**
         * Returns tool tip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tool tip for given node type
         * @return  tool tip for given node
         */
        @Override
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            if (nodeModel instanceof DelegatingNodeModel) {
                NodeModel[] subModels = ((DelegatingNodeModel) nodeModel).getModels();
                if (subModels.length == 0) {
                    if (TreeModel.ROOT.equals(node)) {
                        ColumnModel[] columns = getColumns();
                        for (ColumnModel cm : columns) {
                            if (cm.getType() == null) {
                                return cm.getShortDescription();
                            }
                        }
                    }
                }
            }
            return nodeModel.getShortDescription (node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        @Override
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            if (nodeModel instanceof DelegatingNodeModel) {
                NodeModel[] subModels = ((DelegatingNodeModel) nodeModel).getModels();
                if (subModels.length == 0) {
                    return null; // Nothing when there are no models
                }
            }
            String ib = nodeModel.getIconBase (node);
            //System.err.println("IconBase for node '"+node+"' is '"+ib+"'");
            //System.err.println("Model = "+this);
            return ib;
        }


        // NodeActionsProvider .....................................................

        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         */
        @Override
        public void performDefaultAction (Object node) throws UnknownTypeException {
            nodeActionsProvider.performDefaultAction (node);
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        @Override
        public Action[] getActions (Object node) throws UnknownTypeException {
            return nodeActionsProvider.getActions (node);
        }


        // ColumnsModel ............................................................

        /**
         * Returns sorted array of 
         * {@link org.netbeans.spi.viewmodel.ColumnModel}s.
         *
         * @return sorted array of ColumnModels
         */
        public ColumnModel[] getColumns () {
            return columnModels;
        }


        // TableModel ..............................................................

        @Override
        public Object getValueAt (Object node, String columnID) throws 
        UnknownTypeException {
            return tableModel.getValueAt (node, columnID);
        }

        @Override
        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            return tableModel.isReadOnly (node, columnID);
        }

        @Override
        public void setValueAt (Object node, String columnID, Object value) throws 
        UnknownTypeException {
            tableModel.setValueAt (node, columnID, value);
        }


        // TreeExpansionModel ......................................................

        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        @Override
        public boolean isExpanded (Object node) throws UnknownTypeException {
            if (treeExpansionModel == null) {
                return false;
            }
            return treeExpansionModel.isExpanded (node);
        }

        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        @Override
        public void nodeExpanded (Object node) {
            if (treeExpansionModel != null) {
                treeExpansionModel.nodeExpanded (node);
            }
        }

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        @Override
        public void nodeCollapsed (Object node) {
            if (treeExpansionModel != null) {
                treeExpansionModel.nodeCollapsed (node);
            }
        }


        // listeners ...............................................................

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        @Override
        public void addModelListener (ModelListener l) {
            Set<Model> modelsListenersAddedTo = new HashSet<Model>();
            if (treeModel instanceof DelegatingTreeModel) {
                ((DelegatingTreeModel) treeModel).addModelListener(l, modelsListenersAddedTo);
            } else {
                treeModel.addModelListener (l);
                modelsListenersAddedTo.add(treeModel);
            }
            if (nodeModel != treeModel && !modelsListenersAddedTo.contains(nodeModel)) {
                if (nodeModel instanceof DelegatingNodeModel) {
                    ((DelegatingNodeModel) nodeModel).addModelListener(l, modelsListenersAddedTo);
                } else {
                    nodeModel.addModelListener (l);
                }
            }
            if (tableModel != treeModel && tableModel != nodeModel && !modelsListenersAddedTo.contains(tableModel)) {
                if (tableModel instanceof DelegatingTableModel) {
                    ((DelegatingTableModel) tableModel).addModelListener(l, modelsListenersAddedTo);
                } else {
                    tableModel.addModelListener (l);
                }
            }
            if (treeExpansionModel instanceof CompoundTreeExpansionModel) {
                ((CompoundTreeExpansionModel) treeExpansionModel).addModelListener(l);
            }
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        @Override
        public void removeModelListener (ModelListener l) {
            treeModel.removeModelListener (l);
            if (nodeModel != treeModel) {
                nodeModel.removeModelListener (l);
            }
            if (tableModel != treeModel && tableModel != nodeModel) {
                tableModel.removeModelListener (l);
            }
            if (treeExpansionModel instanceof CompoundTreeExpansionModel) {
                ((CompoundTreeExpansionModel) treeExpansionModel).removeModelListener(l);
            }
        }

        @Override
        public String toString () {
            /*String str = super.toString () +
                   "\n  TreeModel = " + treeModel +
                   "\n  NodeModel = " + nodeModel +
                   "\n  TableModel = " + tableModel +
                   "\n  NodeActionsProvider = " + nodeActionsProvider +
                   "\n  ColumnsModel = " + java.util.Arrays.asList(columnModels);
            if (str.indexOf("WatchesTableModel") > 0) {
                return "CompoundModel [WATCHES]";
            }
            if (str.indexOf("EvaluatorTableModel") > 0) {
                return "CompoundModel [EVALUATOR]";
            }
            if (str.indexOf("VariablesTableModel") > 0) {
                return "CompoundModel [VARIABLES]";
            }*/
            return super.toString () + 
                   "\n  TreeModel = " + treeModel +
                   "\n  NodeModel = " + nodeModel +
                   "\n  TableModel = " + tableModel +
                   "\n  NodeActionsProvider = " + nodeActionsProvider +
                   "\n  ColumnsModel = " + java.util.Arrays.asList(columnModels);
        }
        
        // ExtendedNodeModel
    
        @Override
        public boolean canRename(Object node) throws UnknownTypeException {
            return nodeModel.canRename(node);
        }

        @Override
        public boolean canCopy(Object node) throws UnknownTypeException {
            return nodeModel.canCopy(node);
        }

        @Override
        public boolean canCut(Object node) throws UnknownTypeException {
            return nodeModel.canCut(node);
        }

        @Override
        public Transferable clipboardCopy(Object node) throws IOException,
                                                              UnknownTypeException {
            return nodeModel.clipboardCopy(node);
        }

        @Override
        public Transferable clipboardCut(Object node) throws IOException,
                                                             UnknownTypeException {
            return nodeModel.clipboardCut(node);
        }

        @Override
        public int getAllowedDragActions() {
            if (dndNodeModel != null) {
                return dndNodeModel.getAllowedDragActions();
            } else {
                return DEFAULT_DRAG_DROP_ALLOWED_ACTIONS;
            }
        }

        @Override
        public int getAllowedDropActions(Transferable t) {
            if (dndNodeModel != null) {
                return dndNodeModel.getAllowedDropActions(t);
            } else {
                return DEFAULT_DRAG_DROP_ALLOWED_ACTIONS;
            }
        }

        @Override
        public Transferable drag(Object node) throws IOException,
                                                     UnknownTypeException {
            if (dndNodeModel != null) {
                return dndNodeModel.drag(node);
            } else {
                return null;
            }
        }

        @Override
        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            return nodeModel.getPasteTypes(node, t);
        }

        @Override
        public PasteType getDropType(Object node, Transferable t, int action,
                                     int index) throws UnknownTypeException {
            if (dndNodeModel != null) {
                return dndNodeModel.getDropType(node, t, action, index);
            } else {
                return null;
            }
        }

        @Override
        public void setName(Object node, String name) throws UnknownTypeException {
            nodeModel.setName(node, name);
        }

        @Override
        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            String ib = nodeModel.getIconBaseWithExtension(node);
            //System.err.println("IconBase for node '"+node+"' is '"+ib+"'");
            //System.err.println("Model = "+this);
            return ib;
        }

        @Override
        public boolean isCheckable(Object node) throws UnknownTypeException {
            if (cnodeModel != null) {
                return cnodeModel.isCheckable(node);
            } else {
                return false;
            }
        }

        @Override
        public boolean isCheckEnabled(Object node) throws UnknownTypeException {
            if (cnodeModel != null) {
                return cnodeModel.isCheckEnabled(node);
            } else {
                return true;
            }
        }

        @Override
        public Boolean isSelected(Object node) throws UnknownTypeException {
            if (cnodeModel != null) {
                return cnodeModel.isSelected(node);
            } else {
                return false;
            }
        }

        @Override
        public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
            if (cnodeModel != null) {
                cnodeModel.setSelected(node, selected);
            } else {
                Exceptions.printStackTrace(new IllegalStateException("Can not set selected state to model "+nodeModel));
            }
        }

        // AsynchronousModel

        public Executor asynchronous(CALL asynchCall, Object node) throws UnknownTypeException {
            return asynchModel.asynchronous(asynchCall, node);
        }

        // TableRendererModel

        @Override
        public boolean canRenderCell(Object node, String columnID) throws UnknownTypeException {
            if (tableRendererModel != null) {
                return tableRendererModel.canRenderCell(node, columnID);
            } else {
                return false;
            }
        }

        @Override
        public TableCellRenderer getCellRenderer(Object node, String columnID) throws UnknownTypeException {
            if (tableRendererModel != null) {
                return tableRendererModel.getCellRenderer(node, columnID);
            } else {
                return null;
            }
        }

        @Override
        public boolean canEditCell(Object node, String columnID) throws UnknownTypeException {
            if (tableRendererModel != null) {
                return tableRendererModel.canEditCell(node, columnID);
            } else {
                return false;
            }
        }

        @Override
        public TableCellEditor getCellEditor(Object node, String columnID) throws UnknownTypeException {
            if (tableRendererModel != null) {
                return tableRendererModel.getCellEditor(node, columnID);
            } else {
                return null;
            }
        }
        
        // TableHTMLModel

        @Override
        public boolean hasHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
            return tableModel.hasHTMLValueAt(node, columnID);
        }

        @Override
        public String getHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
            return tableModel.getHTMLValueAt(node, columnID);
        }
        
        // TablePropertyEditorsModel

        @Override
        public PropertyEditor getPropertyEditor(Object node, String columnID) throws UnknownTypeException {
            if (tablePropertyEditorsModel != null) {
                return tablePropertyEditorsModel.getPropertyEditor(node, columnID);
            } else {
                return null;
            }
        }
        
        

    }

    private static final class ModelLists extends Object {

        public List<TreeModel>                 treeModels = Collections.emptyList();
        public List<TreeModelFilter>           treeModelFilters = Collections.emptyList();
        public List<TreeExpansionModel>        treeExpansionModels = Collections.emptyList();
        public List<TreeExpansionModelFilter>  treeExpansionModelFilters = Collections.emptyList();
        public List<NodeModel>                 nodeModels = Collections.emptyList();
        public List<NodeModelFilter>           nodeModelFilters = Collections.emptyList();
        public List<TableModel>                tableModels = Collections.emptyList();
        public List<TableModelFilter>          tableModelFilters = Collections.emptyList();
        public List<NodeActionsProvider>       nodeActionsProviders = Collections.emptyList();
        public List<NodeActionsProviderFilter> nodeActionsProviderFilters = Collections.emptyList();
        public List<ColumnModel>               columnModels = Collections.emptyList();
        //public List<AsynchronousModel>         asynchModels = Collections.emptyList();
        public List<AsynchronousModelFilter>   asynchModelFilters = Collections.emptyList();
        public List<TableRendererModel>        tableRendererModels = Collections.emptyList();
        public List<TableRendererModelFilter>  tableRendererModelFilters = Collections.emptyList();
        public List<TableHTMLModel>            tableHtmlModels = Collections.emptyList();
        public List<TableHTMLModelFilter>      tableHtmlModelFilters = Collections.emptyList();
        public List<TablePropertyEditorsModel> tablePropertyEditorsModels = Collections.emptyList();
        public List<TablePropertyEditorsModelFilter> tablePropertyEditorsModelFilters = Collections.emptyList();

        public void addOtherModels(List<? extends Model> otherModels) {
            Iterator it = otherModels.iterator ();
            while (it.hasNext ()) {
                Object model = it.next ();
                boolean first = model.getClass ().getName ().endsWith ("First");
                if (model instanceof TreeModel && !treeModels.contains((TreeModel) model)) {
                    treeModels = new ArrayList<TreeModel>(treeModels);
                    treeModels.add((TreeModel) model);
                }
                if (model instanceof TreeModelFilter && !treeModelFilters.contains((TreeModelFilter) model)) {
                    treeModelFilters = new ArrayList<TreeModelFilter>(treeModelFilters);
                    if (first) {
                        treeModelFilters.add((TreeModelFilter) model);
                    } else {
                        treeModelFilters.add(0, (TreeModelFilter) model);
                    }
                }
                if (model instanceof TreeExpansionModel && !treeExpansionModels.contains((TreeExpansionModel) model)) {
                    treeExpansionModels = new ArrayList<TreeExpansionModel>(treeExpansionModels);
                    treeExpansionModels.add((TreeExpansionModel) model);
                }
                if (model instanceof TreeExpansionModelFilter && !treeExpansionModelFilters.contains((TreeExpansionModelFilter) model)) {
                    treeExpansionModelFilters = new ArrayList<TreeExpansionModelFilter>(treeExpansionModelFilters);
                    if (first) {
                        treeExpansionModelFilters.add((TreeExpansionModelFilter) model);
                    } else {
                        treeExpansionModelFilters.add(0, (TreeExpansionModelFilter) model);
                    }
                }
                if (model instanceof NodeModel && !nodeModels.contains((NodeModel) model)) {
                    nodeModels = new ArrayList<NodeModel>(nodeModels);
                    nodeModels.add((NodeModel) model);
                }
                if (model instanceof NodeModelFilter && !nodeModelFilters.contains((NodeModelFilter) model)) {
                    nodeModelFilters = new ArrayList<NodeModelFilter>(nodeModelFilters);
                    if (first) {
                        nodeModelFilters.add((NodeModelFilter) model);
                    } else {
                        nodeModelFilters.add(0, (NodeModelFilter) model);
                    }
                }
                if (model instanceof TableModel && !tableModels.contains((TableModel) model)) {
                    tableModels = new ArrayList<TableModel>(tableModels);
                    tableModels.add((TableModel) model);
                }
                if (model instanceof TableModelFilter && !tableModelFilters.contains((TableModelFilter) model)) {
                    tableModelFilters = new ArrayList<TableModelFilter>(tableModelFilters);
                    if (first) {
                        tableModelFilters.add((TableModelFilter) model);
                    } else {
                        tableModelFilters.add(0, (TableModelFilter) model);
                    }
                }
                if (model instanceof TableRendererModel && !tableRendererModels.contains((TableRendererModel) model)) {
                    tableRendererModels = new ArrayList<TableRendererModel>(tableRendererModels);
                    tableRendererModels.add((TableRendererModel) model);
                }
                if (model instanceof TableRendererModelFilter && !tableRendererModelFilters.contains((TableRendererModelFilter) model)) {
                    tableRendererModelFilters = new ArrayList<TableRendererModelFilter>(tableRendererModelFilters);
                    if (first) {
                        tableRendererModelFilters.add((TableRendererModelFilter) model);
                    } else {
                        tableRendererModelFilters.add(0, (TableRendererModelFilter) model);
                    }
                }
                if (model instanceof TableHTMLModel && !tableHtmlModels.contains((TableHTMLModel) model)) {
                    tableHtmlModels = new ArrayList<TableHTMLModel>(tableHtmlModels);
                    tableHtmlModels.add((TableHTMLModel) model);
                }
                if (model instanceof TableHTMLModelFilter && !tableHtmlModelFilters.contains((TableHTMLModelFilter) model)) {
                    tableHtmlModelFilters = new ArrayList<TableHTMLModelFilter>(tableHtmlModelFilters);
                    if (first) {
                        tableHtmlModelFilters.add((TableHTMLModelFilter) model);
                    } else {
                        tableHtmlModelFilters.add(0, (TableHTMLModelFilter) model);
                    }
                }
                if (model instanceof TablePropertyEditorsModel && !tablePropertyEditorsModels.contains((TablePropertyEditorsModel) model)) {
                    tablePropertyEditorsModels = new ArrayList<TablePropertyEditorsModel>(tablePropertyEditorsModels);
                    tablePropertyEditorsModels.add((TablePropertyEditorsModel) model);
                }
                if (model instanceof TablePropertyEditorsModelFilter && !tablePropertyEditorsModelFilters.contains((TablePropertyEditorsModelFilter) model)) {
                    tablePropertyEditorsModelFilters = new ArrayList<TablePropertyEditorsModelFilter>(tablePropertyEditorsModelFilters);
                    if (first) {
                        tablePropertyEditorsModelFilters.add((TablePropertyEditorsModelFilter) model);
                    } else {
                        tablePropertyEditorsModelFilters.add(0, (TablePropertyEditorsModelFilter) model);
                    }
                }
                if (model instanceof NodeActionsProvider && !nodeActionsProviders.contains((NodeActionsProvider) model)) {
                    nodeActionsProviders = new ArrayList<NodeActionsProvider>(nodeActionsProviders);
                    nodeActionsProviders.add((NodeActionsProvider) model);
                }
                if (model instanceof NodeActionsProviderFilter && !nodeActionsProviderFilters.contains((NodeActionsProviderFilter) model)) {
                    nodeActionsProviderFilters = new ArrayList<NodeActionsProviderFilter>(nodeActionsProviderFilters);
                    if (first) {
                        nodeActionsProviderFilters.add((NodeActionsProviderFilter) model);
                    } else {
                        nodeActionsProviderFilters.add(0, (NodeActionsProviderFilter) model);
                    }
                }
                /*if (model instanceof AsynchronousModel) {
                    asynchModels = new ArrayList<AsynchronousModel>(asynchModels);
                    asynchModels.add((AsynchronousModel) model);
                }*/
                if (model instanceof AsynchronousModelFilter && !asynchModelFilters.contains((AsynchronousModelFilter) model)) {
                    asynchModelFilters = new ArrayList<AsynchronousModelFilter>(asynchModelFilters);
                    if (first) {
                        asynchModelFilters.add((AsynchronousModelFilter) model);
                    } else {
                        asynchModelFilters.add(0, (AsynchronousModelFilter) model);
                    }
                }

                if (model instanceof ColumnModel && !columnModels.contains((ColumnModel) model)) {
                    columnModels = new ArrayList<ColumnModel>(columnModels);
                    columnModels.add((ColumnModel) model);
                }
            }
        }
    }

}
