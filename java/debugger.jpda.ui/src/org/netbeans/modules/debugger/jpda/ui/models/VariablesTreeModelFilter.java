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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;
import javax.swing.Action;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.views.VariablesViewButtons;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.jpda.VariablesFilter;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.CodeEvaluator.Result.DefaultHistoryItem;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;

/**
 * Filters some original tree of nodes (represented by {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types={ TreeModelFilter.class,
                                         ExtendedNodeModelFilter.class,
                                         TableModelFilter.class,
                                         NodeActionsProviderFilter.class },
                                 position=400),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types={ TreeModelFilter.class,
                                         ExtendedNodeModelFilter.class,
                                         TableModelFilter.class,
                                         NodeActionsProviderFilter.class },
                                 position=400),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types={ TreeModelFilter.class,
                                         ExtendedNodeModelFilter.class,
                                         TableModelFilter.class,
                                         NodeActionsProviderFilter.class },
                                 position=400),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types={ TreeModelFilter.class,
                                         ExtendedNodeModelFilter.class,
                                         TableModelFilter.class,
                                         NodeActionsProviderFilter.class },
                                 position=400)
})
public class VariablesTreeModelFilter implements TreeModelFilter, 
ExtendedNodeModelFilter, TableModelFilter, NodeActionsProviderFilter, Runnable {
    
    private ContextProvider lookupProvider;
    
    private final Collection modelListeners = new HashSet();
    
    private RequestProcessor evaluationRP;
    
    private RequestProcessor.Task evaluationTask;
    
    private final LinkedList evaluationQueue = new LinkedList();

    private final CodeEvaluator.Result<Variable, DefaultHistoryItem> result;
    private EvaluatorListener evalListener;
    private VariablesPreferenceChangeListener prefListener;
    private Preferences preferences = NbPreferences.forModule(VariablesViewButtons.class).node(VariablesViewButtons.PREFERENCES_NAME);
    
    public VariablesTreeModelFilter (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        evaluationRP = lookupProvider.lookupFirst(null, RequestProcessor.class);
        evalListener = new EvaluatorListener();
        //CodeEvaluatorUI.addResultListener(WeakListeners.propertyChange(evalListener, new ResultListenerRemoval()));
        result = CodeEvaluator.Result.get(lookupProvider.lookupFirst(null, DebuggerEngine.class));
        result.addListener(evalListener);
        prefListener = new VariablesPreferenceChangeListener();
        preferences.addPreferenceChangeListener(WeakListeners.create(
                PreferenceChangeListener.class, prefListener, preferences));
        Properties properties = Properties.getDefault().getProperties("debugger.options.JPDA"); // NOI18N
        properties.addPropertyChangeListener(WeakListeners.propertyChange(prefListener, properties));
    }

    /** 
     * Returns filtered root of hierarchy.
     *
     * @param   original the original tree model
     * @return  filtered root of hierarchy
     */
    @Override
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }
    
    public static boolean isEvaluated(Object o) {
        if (o instanceof Refreshable) {
            return ((Refreshable) o).isCurrent();
        }
        return true;
    }
    
    private static void waitToEvaluate(Object o) {
        if (o instanceof Refreshable) {
            // waits for the evaluation, the retrieval must already be initiated
            try {
                ((Refreshable) o).refresh();
            } catch (RefreshFailedException exc) {
                // Thrown when interrupted
                Thread.currentThread().interrupt();
            }
        }
        loadAllTypes(o); // Initialize all types, implemented interfaces and super classes
    }

    private static boolean hasAllTypes(Object o) {
        if (!(o instanceof ObjectVariable)) return true;
        ObjectVariable ov = (ObjectVariable) o;
        boolean hasAllInterfaces;
        try {
            java.lang.reflect.Method hasAllInterfacesMethod = ov.getClass().getMethod("hasAllTypes");
            hasAllInterfacesMethod.setAccessible(true);
            hasAllInterfaces = (Boolean) hasAllInterfacesMethod.invoke(ov);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            hasAllInterfaces = true;
        }
        return hasAllInterfaces;
        
    }

    private static void loadAllTypes(Object o) {
        if (!(o instanceof ObjectVariable)) return ;
        ObjectVariable ov = (ObjectVariable) o;
        // TODO: List<JPDAClassType> ov.loadAllTypes();
        try {
            java.lang.reflect.Method loadAllTypesMethod = ov.getClass().getMethod("loadAllTypes");
            loadAllTypesMethod.setAccessible(true);
            loadAllTypesMethod.invoke(ov);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static List<JPDAClassType> getAllInterfaces(Object o) {
        if (!(o instanceof ObjectVariable)) return null;
        ObjectVariable ov = (ObjectVariable) o;
        // TODO: List<JPDAClassType> ov.getAllInterfaces();
        List<JPDAClassType> allInterfaces;
        try {
            java.lang.reflect.Method allInterfacesMethod = ov.getClass().getMethod("getAllInterfaces");
            allInterfacesMethod.setAccessible(true);
            allInterfaces = (List<JPDAClassType>) allInterfacesMethod.invoke(ov);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            allInterfaces = null;
        }
        return allInterfaces;
    }
    
    private void postEvaluationMonitor(Object o, Runnable whenEvaluated) {
        //Logger.getLogger(VariablesTreeModelFilter.class.getName()).fine("postEvaluationMonitor("+o+", whenEvaluated="+(whenEvaluated != null)+")");
        //Logger.getLogger(VariablesTreeModelFilter.class.getName()).log(Level.FINE, "Called from ", new IllegalStateException("TEST POST EVAL MONITOR"));
        synchronized (evaluationQueue) {
            if (evaluationQueue.contains(o) &&
                evaluationQueue.contains(whenEvaluated)) return ;
            if (evaluationTask == null) {
                evaluationTask = evaluationRP.create(this);
            }
            evaluationQueue.add(o);
            evaluationQueue.add(whenEvaluated);
            evaluationTask.schedule(1);
        }
    }
    
    @Override
    public void run() {
        Object node;
        do {
            node = null;
            Runnable whenEvaluated = null;
            synchronized (evaluationQueue) {
                if (!evaluationQueue.isEmpty()) {
                    node = evaluationQueue.removeFirst();
                    whenEvaluated = (Runnable) evaluationQueue.removeFirst();
                }
            }
            if (node != null) {
                waitToEvaluate(node);
                if (whenEvaluated != null) {
                    whenEvaluated.run();
                } else {
                    fireModelChange(new ModelEvent.NodeChanged(this, node));
                    //System.out.println("FIRE "+node+" evaluated, ID = "+node.hashCode());
                }
            }
        } while (node != null);
        synchronized (evaluationQueue) {
            evaluationTask = null;
        }
    }
    
    /** 
     * Returns filtered children for given parent on given indexes.
     *
     * @param   original the original tree model
     * @param   parent a parent of returned nodes
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve children for given node type
     *
     * @return  children for given parent on given indexes
     */
    @Override
    public Object[] getChildren (
        final TreeModel   original, 
        final Object      parent, 
        final int         from, 
        final int         to
    ) throws UnknownTypeException {
        Object[] ch;
        VariablesFilter vf = getFilter (parent, true, new Runnable() {
            @Override
            public void run() {
                fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                                                           parent,
                                                           ModelEvent.NodeChanged.CHILDREN_MASK));
            }
        });
        if (vf == null) 
            ch = original.getChildren (parent, from, to);
        else
            ch = vf.getChildren (original, (Variable) parent, from, to);
        return ch;
    }
    
    /**
     * Returns number of filtered children for given node.
     * 
     * @param   original the original tree model
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    @Override
    public int getChildrenCount (
        final TreeModel   original, 
        final Object      parent
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (parent, true, new Runnable() {
            @Override
            public void run() {
                fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                                                           parent,
                                                           ModelEvent.NodeChanged.CHILDREN_MASK));
            }
        });
        int count;
        if (vf == null) {
            count = original.getChildrenCount (parent);
        } else {
            count = vf.getChildrenCount (original, (Variable) parent);
        }
        return count;
    }
    
    /**
     * Returns true if node is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     * @return  true if node is leaf
     */
    @Override
    public boolean isLeaf (
        final TreeModel original,
        final Object node
    ) throws UnknownTypeException {
        final boolean[] unfilteredIsLeaf = new boolean[] { false };
        VariablesFilter vf = getFilter (node, true, new Runnable() {
            @Override
            public void run() {
                VariablesFilter vf = getFilter (node, false, null);
                if (vf == null) return ;
                try {
                    boolean filteredIsLeaf = vf.isLeaf (original, (Variable) node);
                    if (filteredIsLeaf != unfilteredIsLeaf[0]) {
                        fireChildrenChange(node);
                    }
                } catch (UnknownTypeException utex) {
                }
            }
        });
        if (vf == null) {
            boolean isLeaf = original.isLeaf (node);
            unfilteredIsLeaf[0] = isLeaf;
            return isLeaf;
        }
        return vf.isLeaf (original, (Variable) node);
    }

    @Override
    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    @Override
    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    private void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    }

    void fireChildrenChange(Object row) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged (
                new ModelEvent.NodeChanged(this, row, ModelEvent.NodeChanged.CHILDREN_MASK)
            );
        }
    }
    
    // NodeModelFilter
    
    @Override
    public String getDisplayName (final NodeModel original, final Object node) 
    throws UnknownTypeException {
        final String[] unfilteredDisplayName = new String[] { null };
        VariablesFilter vf = getFilter (node, true, new Runnable() {
            @Override
            public void run() {
                VariablesFilter vf = getFilter (node, false, null);
                if (vf == null) return ;
                String filteredDisplayName;
                try {
                    filteredDisplayName = vf.getDisplayName (original, (Variable) node);
                } catch (UnknownTypeException utex) {
                    // still do fire
                    filteredDisplayName = utex.toString();
                }
                if (!filteredDisplayName.equals(unfilteredDisplayName[0])) {
                    fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                            node, ModelEvent.NodeChanged.DISPLAY_NAME_MASK));
                }
            }
        });
        if (vf == null) {
            String displayName = original.getDisplayName (node);
            unfilteredDisplayName[0] = displayName;
            return displayName;
        } else {
            return vf.getDisplayName (original, (Variable) node);
        }
    }
    
    @Override
    public String getIconBase (final NodeModel original, final Object node) 
    throws UnknownTypeException {
        Logger.getLogger(VariablesTreeModelFilter.class.getName()).log(Level.CONFIG,
                "Obsolete getIconBase() method was called!",
                new IllegalStateException("getIconBaseWithExtension() should be called!"));
        final String[] unfilteredIconBase = new String[] { null };
        VariablesFilter vf = getFilter (node, true, new Runnable() {
            @Override
            public void run() {
                VariablesFilter vf = getFilter (node, false, null);
                if (vf == null) return ;
                String filteredIconBase;
                try {
                    filteredIconBase = vf.getIconBase (original, (Variable) node);
                } catch (UnknownTypeException utex) {
                    // still do fire
                    filteredIconBase = utex.toString();
                }
                if (!filteredIconBase.equals(unfilteredIconBase[0])) {
                    fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                            node, ModelEvent.NodeChanged.ICON_MASK));
                }
            }
        });
        if (vf == null) {
            String iconBase = original.getIconBase (node);
            unfilteredIconBase[0] = iconBase;
            return iconBase;
        } else {
            return vf.getIconBase (original, (Variable) node);
        }
    }
    
    @Override
    public String getShortDescription (final NodeModel original, final Object node) 
    throws UnknownTypeException {
        final String[] unfilteredShortDescription = new String[] { null };
        VariablesFilter vf = getFilter (node, true, new Runnable() {
            @Override
            public void run() {
                VariablesFilter vf = getFilter (node, false, null);
                if (vf == null) return ;
                String filteredShortDescription;
                try {
                    filteredShortDescription = vf.getShortDescription (original, (Variable) node);
                } catch (UnknownTypeException utex) {
                    // still do fire
                    filteredShortDescription = utex.toString();
                }
                if (!filteredShortDescription.equals(unfilteredShortDescription[0])) {
                    fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                            node, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        if (vf == null) {
            return original.getShortDescription (node);
        } else {
            return vf.getShortDescription (original, (Variable) node);
        }
    }
    
    
    // NodeActionsProviderFilter
    
    @Override
    public Action[] getActions (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (vf == null) 
            return original.getActions (node);
        return vf.getActions (original, (Variable) node);
    }
    
    @Override
    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (vf == null) 
            original.performDefaultAction (node);
        else
            vf.performDefaultAction (original, (Variable) node);
    }
    
    
    // TableModelFilter
    
    @Override
    public Object getValueAt (
        TableModel original, 
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        Object value;
        VariablesFilter vf = getFilter (row, false, null);
        if (vf == null) {
            value = original.getValueAt (row, columnID);
        } else {
            value = vf.getValueAt (original, (Variable) row, columnID);
        }
        return value;
    }
    
    @Override
    public boolean isReadOnly (
        TableModel original, 
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (row, true, null);
        if (vf == null) 
            return original.isReadOnly (row, columnID);
        return vf.isReadOnly (original, (Variable) row, columnID);
    }
    
    @Override
    public void setValueAt (
        TableModel original, 
        Object row, 
        String columnID, 
        Object value
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (row, false, null);
        if (vf == null)
            original.setValueAt (row, columnID, value);
        else
            vf.setValueAt (original, (Variable) row, columnID, value);
    }
    
    
    // helper methods ..........................................................

    private final Object filtersLock = new Object();
    private HashMap typeToFilter;
    private HashMap ancestorToFilter;
    
    /**
     * @param o The object to get the filter for
     * @param checkEvaluated Whether we should check if the object was already evaluated
     * @param whenEvaluated If the object is not yet evaluated, <code>null</code>
     *                      will be returned and <code>whenEvaluated.run()<code>
     *                      will be executed when the object becomes evaluated.
     * @return The filter or <code>null</code>.
     */
    private VariablesFilter getFilter (Object o, boolean checkEvaluated, Runnable whenEvaluated) {
        Map typeToFilterL;
        Map ancestorToFilterL;
        synchronized (filtersLock) {
            if (typeToFilter == null) {
                typeToFilter = new HashMap ();
                ancestorToFilter = new HashMap ();
                List l = lookupProvider.lookup (null, VariablesFilter.class);
                int i, k = l.size ();
                for (i = 0; i < k; i++) {
                    VariablesFilter f = (VariablesFilter) l.get (i);
                    String[] types = f.getSupportedAncestors ();
                    int j, jj = types.length;
                    for (j = 0; j < jj; j++)
                        ancestorToFilter.put (types [j], f);
                    types = f.getSupportedTypes ();
                    jj = types.length;
                    for (j = 0; j < jj; j++)
                        typeToFilter.put (types [j], f);
                }
            }

            if (typeToFilter.isEmpty() && ancestorToFilter.isEmpty()) return null; // Optimization for corner case

            typeToFilterL = typeToFilter;
            ancestorToFilterL = ancestorToFilter;
        }

        if (!(o instanceof Variable)) return null;

        Variable v = (Variable) o;

        String type = null;
        if (checkEvaluated) {
            boolean evaluated = true;
            if (!hasAllTypes(v)) {
                evaluated = false;
            }
            if (v instanceof Refreshable) {
                synchronized (v) { // Do the test and retrieve of type in synch
                    evaluated = ((Refreshable) v).isCurrent();
                    if (evaluated) {
                        type = v.getType();
                    }
                }
            }
            if (!evaluated) {
                if (whenEvaluated != null) {
                    postEvaluationMonitor(o, whenEvaluated);
                }
                return null;
            }
        }
        if (type == null) {
            type = v.getType();
        }

        VariablesFilter vf = (VariablesFilter) typeToFilterL.get (type);
        if (vf != null) return vf;

        if (!(o instanceof ObjectVariable)) return null;
        ObjectVariable ov = (ObjectVariable) o;
        
        List<JPDAClassType> allInterfaces = getAllInterfaces(o);
        if (allInterfaces != null) {
            for (JPDAClassType ct : allInterfaces) {
                type = ct.getName();
                vf = (VariablesFilter) ancestorToFilterL.get (type);
                if (vf != null) return vf;
            }
        }
        // Consider ancestors as the type + it's ancestors
        while ((ov = ov.getSuper ()) != null) {
            type = null;
            // Check for evaluation before type is retrieved:
            if (checkEvaluated) {
                boolean evaluated;
                if (ov instanceof Refreshable) {
                    synchronized (ov) { // Do the test and retrieve of type in synch
                        evaluated = ((Refreshable) ov).isCurrent();
                        if (evaluated) {
                            type = ov.getType();
                        } else {
                            type = null;
                        }
                    }
                } else {
                    evaluated = true;
                    type = ov.getType();
                }
                if (!evaluated) {
                    return null;
                }
            } else {
                type = ov.getType();
            }
            if (type == null) {
                break;
            }
            vf = (VariablesFilter) ancestorToFilterL.get (type);
            if (vf != null) return vf;
        }
        return null;
    }

    @Override
    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (!(vf instanceof ExtendedNodeModelFilter))  {
            return original.canRename(node);
        } else {
            return ((ExtendedNodeModelFilter) vf).canRename(original, node);
        }
    }

    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (!(vf instanceof ExtendedNodeModelFilter))  {
            return original.canCopy(node);
        } else {
            return ((ExtendedNodeModelFilter) vf).canCopy(original, node);
        }
    }

    @Override
    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (!(vf instanceof ExtendedNodeModelFilter))  {
            return original.canCut(node);
        } else {
            return ((ExtendedNodeModelFilter) vf).canCut(original, node);
        }
    }

    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (!(vf instanceof ExtendedNodeModelFilter))  {
            return original.clipboardCopy(node);
        } else {
            return ((ExtendedNodeModelFilter) vf).clipboardCopy(original, node);
        }
    }

    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (!(vf instanceof ExtendedNodeModelFilter))  {
            return original.clipboardCut(node);
        } else {
            return ((ExtendedNodeModelFilter) vf).clipboardCut(original, node);
        }
    }

    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (!(vf instanceof ExtendedNodeModelFilter))  {
            return original.getPasteTypes(node, t);
        } else {
            return ((ExtendedNodeModelFilter) vf).getPasteTypes(original, node, t);
        }
    }

    @Override
    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (!(vf instanceof ExtendedNodeModelFilter))  {
            original.setName(node, name);
        } else {
            ((ExtendedNodeModelFilter) vf).setName(original, node, name);
        }
    }

    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (!(vf instanceof ExtendedNodeModelFilter))  {
            return original.getIconBaseWithExtension(node);
        } else {
            return ((ExtendedNodeModelFilter) vf).getIconBaseWithExtension(original, node);
        }
    }

    private class VariablesPreferenceChangeListener implements PreferenceChangeListener, PropertyChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (VariablesViewButtons.SHOW_VALUE_PROPERTY_EDITORS.equals(key)) {
                refresh();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("VariableFormatters".equals(evt.getPropertyName())) {
                synchronized (filtersLock) {
                    typeToFilter = null;
                    ancestorToFilter = null;
                }
                refresh();
            }
        }

        private void refresh() {
            try {
                fireModelChange(new ModelEvent.NodeChanged(this, TreeModel.ROOT));
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }

    }

    private class EvaluatorListener implements CodeEvaluator.Result.Listener<Variable> {

        @Override
        public void resultChanged(Variable o) {
            try {
                fireModelChange(new ModelEvent.NodeChanged(this, TreeModel.ROOT));
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }

    }
    
}
