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
package org.netbeans.modules.java.lsp.server.debugging.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.CheckNodeModel;
import org.netbeans.spi.viewmodel.CheckNodeModelFilter;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.DnDNodeModel;
import org.netbeans.spi.viewmodel.DnDNodeModelFilter;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.ReorderableTreeModel;
import org.netbeans.spi.viewmodel.ReorderableTreeModelFilter;
import org.netbeans.spi.viewmodel.TableHTMLModel;
import org.netbeans.spi.viewmodel.TableHTMLModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TablePropertyEditorsModel;
import org.netbeans.spi.viewmodel.TablePropertyEditorsModelFilter;
import org.netbeans.spi.viewmodel.TableRendererModel;
import org.netbeans.spi.viewmodel.TableRendererModelFilter;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;

final class ViewModel {

    public static final String LOCALS_VIEW_NAME = "LocalsView";

    private static final Class[] TREE_MODELS = { TreeModel.class, ReorderableTreeModel.class };
    private static final Class[] TREE_MODEL_FILTERS = { TreeModelFilter.class, ReorderableTreeModelFilter.class };

    private static final Class[] NODE_MODELS = { NodeModel.class, CheckNodeModel.class, DnDNodeModel.class, ExtendedNodeModel.class };
    private static final Class[] NODE_MODEL_FILTERS = { NodeModelFilter.class, CheckNodeModelFilter.class, DnDNodeModelFilter.class, ExtendedNodeModelFilter.class };

    private static final Class[] TABLE_MODELS = { TableModel.class, TableHTMLModel.class };

    private final String viewType;

    private SessionProvider providerToDisplay;

    private final String propertiesHelpID = null;

    private ViewModel(String viewType) {
        this.viewType = viewType;
    }

    private Models.CompoundModel createModel(DebuggerEngine e) {
        List<? extends SessionProvider> sessionProviders;
        List[] treeModels = new List[TREE_MODELS.length];
        List[] treeModelFilters = new List[TREE_MODEL_FILTERS.length];
        List treeExpansionModels;
        List treeExpansionModelFilters;
        List[] nodeModels = new List[NODE_MODELS.length];
        List[] nodeModelFilters = new List[NODE_MODEL_FILTERS.length];
        List[] tableModels = new List[TABLE_MODELS.length];
        List tableModelFilters;
        List tableHTMLModelFilters;
        List nodeActionsProviders;
        List nodeActionsProviderFilters;
        List columnModels;
        List mm;
        List asynchModelFilters;
        List tableRenderers;
        List tableRendererFilters;
        List tablePropertyEditors;
        List tablePropertyEditorsFilters;
        List hyperModels;

        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        if (e == null) {
            sessionProviders = dm.lookup (viewType, SessionProvider.class);
        } else {
            sessionProviders = DebuggerManager.join(e, dm).lookup (viewType, SessionProvider.class);
        }
        if (!sessionProviders.contains(providerToDisplay)) {
            providerToDisplay = null;
        }
        if (e == null && providerToDisplay == null && sessionProviders.size() > 0) {
            providerToDisplay = sessionProviders.get(0);
        }
        ContextProvider cp;
        String viewPath;
        if (providerToDisplay != null) {
            e = null;
            cp = dm;
            viewPath = viewType + "/" + providerToDisplay.getTypeID();
        } else {
            cp = e != null ? DebuggerManager.join(e, dm) : dm;
            viewPath = viewType;
        }

        getMultiModels(cp, viewPath, treeModels, TREE_MODELS);
        getMultiModels(cp, viewPath, treeModelFilters, TREE_MODEL_FILTERS);
        treeExpansionModels =   cp.lookup (viewPath, TreeExpansionModel.class);
        treeExpansionModelFilters = cp.lookup (viewType, TreeExpansionModelFilter.class);
        getMultiModels(cp, viewPath, nodeModels, NODE_MODELS);
        getMultiModels(cp, viewPath, nodeModelFilters, NODE_MODEL_FILTERS);
        getMultiModels(cp, viewPath, tableModels, TABLE_MODELS);
        tableModelFilters =     cp.lookup (viewPath, TableModelFilter.class);
        tableHTMLModelFilters =     cp.lookup (viewPath, TableHTMLModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewPath, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewPath, NodeActionsProviderFilter.class);
        columnModels =          cp.lookup (viewPath, ColumnModel.class);
        mm =                    cp.lookup (viewPath, Model.class);
        asynchModelFilters =    cp.lookup (viewPath, AsynchronousModelFilter.class);
        tableRenderers =        cp.lookup (viewPath, TableRendererModel.class);
        tableRendererFilters =  cp.lookup (viewPath, TableRendererModelFilter.class);
        tablePropertyEditors =  cp.lookup (viewPath, TablePropertyEditorsModel.class);
        tablePropertyEditorsFilters = cp.lookup (viewPath, TablePropertyEditorsModelFilter.class);
        String searchPath = viewPath; // Try to find the AsynchronousModelFilter in upper folders...
        while (asynchModelFilters.isEmpty() && searchPath != null) {
            int i = searchPath.lastIndexOf('/');
            if (i > 0) {
                searchPath = searchPath.substring(0, i);
            } else {
                searchPath = null;
            }
            asynchModelFilters = cp.lookup (searchPath, AsynchronousModelFilter.class);
        }
        //rp = (e != null) ? e.lookupFirst(null, RequestProcessor.class) : null;

        hyperModels = null;

        List<Object> models = new ArrayList<>(11);
        if (mm == null) {
            // Destroyed
            return null;
        }
        synchronized (treeModels) {
            models.add(joinLists(treeModels));
        }
        synchronized (treeModelFilters) {
            models.add(joinLists(treeModelFilters));
        }
        synchronized (treeExpansionModels) {
            models.add(new ArrayList<Object>(treeExpansionModels));
        }
        synchronized (nodeModels) {
            models.add(joinLists(nodeModels));
        }
        synchronized (nodeModelFilters) {
            models.add(joinLists(nodeModelFilters));
        }
        synchronized (tableModels) {
            models.add(joinLists(tableModels));
        }
        synchronized (tableModelFilters) {
            models.add(new ArrayList<>(tableModelFilters));
        }
        synchronized (nodeActionsProviders) {
            models.add(new ArrayList<>(nodeActionsProviders));
        }
        synchronized (nodeActionsProviderFilters) {
            models.add(new ArrayList<>(nodeActionsProviderFilters));
        }
        synchronized (columnModels) {
            models.add(new ArrayList<>(columnModels));
        }
        synchronized (mm) {
            models.add(new ArrayList<>(mm));
        }
        synchronized (treeExpansionModelFilters) {
            models.add(new ArrayList<>(treeExpansionModelFilters));
        }
        synchronized (asynchModelFilters) {
            models.add(new ArrayList<>(asynchModelFilters));
        }
        synchronized (tableRenderers) {
            models.add(new ArrayList<>(tableRenderers));
        }
        synchronized (tableRendererFilters) {
            models.add(new ArrayList<>(tableRendererFilters));
        }
        synchronized (tableHTMLModelFilters) {
            models.add(new ArrayList<>(tableHTMLModelFilters));
        }
        synchronized (tablePropertyEditors) {
            models.add(new ArrayList<>(tablePropertyEditors));
        }
        synchronized (tablePropertyEditorsFilters) {
            models.add(new ArrayList<>(tablePropertyEditorsFilters));
        }

        boolean haveTreeModels = false;
        for (List tms : treeModels) {
            if (tms.size() > 0) {
                haveTreeModels = true;
                break;
            }
        }
        boolean haveNodeModels = false;
        for (List nms : nodeModels) {
            if (nms.size() > 0) {
                haveNodeModels = true;
                break;
            }
        }
        boolean haveTableModels = false;
        for (List tms : tableModels) {
            if (tms.size() > 0) {
                haveTableModels = true;
                break;
            }
        }
        final boolean haveModels = haveTreeModels || haveNodeModels || haveTableModels || hyperModels != null;
        final Models.CompoundModel newModel;
        if (hyperModels != null) {
            newModel = Models.createCompoundModel (hyperModels, propertiesHelpID);
        } else if (haveModels) {
            List<Object> theModels;
            //viewTreeDisplayFormat = createTreeDisplayFormat(viewPreferences, columnModels);
            //if (viewTreeDisplayFormat != null) {
            //    theModels = new ArrayList<>(models);
            //    theModels.add(viewTreeDisplayFormat);
            //} else {
                theModels = models;
            //}
            newModel = Models.createCompoundModel (theModels, propertiesHelpID);
        } else {
            newModel = null;
        }
        return newModel;
    }

    private static void getMultiModels(ContextProvider cp, String viewPath,
                                       List[] models, Class[] classTypes) {
        for (int i = 0; i < classTypes.length; i++) {
            models[i] = cp.lookup (viewPath, classTypes[i]);
        }
        //System.err.println("\ngetMultiModels("+viewPath+") = "+Arrays.asList(models)+"\n");
    }

    private static List joinLists(List[] modelLists) {
        List<Object> models = new ArrayList<>();
        for (List l : modelLists) {
            synchronized (l) {
                for (Object o : l) {
                    if (!models.contains(o)) {
                        models.add(o);
                    }
                }
            }
        }
        return models;
    }

    static final class Provider {

        private final ViewModel viewModel;
        private volatile Models.CompoundModel modelCached;
        private volatile String languageCached;

        Provider(String viewType) {
            this.viewModel = new ViewModel(viewType);
        }

        /**
         * Get a view model updated for the provided Session. Do not store the returned model
         * persistently. It's cached and updated according to the provided session's engine.
         */
        Models.CompoundModel getModel(Session session) {
            Models.CompoundModel model = modelCached;
            boolean refresh = !Objects.equals(session.getCurrentLanguage(), languageCached);
            if (refresh || model == null) {
                synchronized (this) {
                    if (refresh || (model = modelCached) == null) {
                        modelCached = model = viewModel.createModel(session.getCurrentEngine());
                        languageCached = session.getCurrentLanguage();
                    }
                }
            }
            return model;
        }
    }
}
