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

package org.netbeans.modules.viewmodel;

import java.awt.datatransfer.Transferable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.CompoundModel;
import org.netbeans.spi.viewmodel.Models.TreeFeatures;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Martin Entlicher
 */
public class TreeModelHyperNode extends TreeModelNode {

    private HyperCompoundModel model;
    
    public TreeModelHyperNode(
        final HyperCompoundModel model,
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        super(
            model.getMain(),
            model.getColumns(),
            null,
            createChildren(model, treeModelRoot, object),
            treeModelRoot,
            object
        );
        this.model = model;
    }

    private static Children createChildren (
        HyperCompoundModel model,
        TreeModelRoot treeModelRoot,
        Object object
    ) {
        if (object == null) {
            throw new NullPointerException ();
        }
        return new HyperModelChildren (model, treeModelRoot, object);
    }

    @Override
    protected void refreshTheChildren(Set<Models.CompoundModel> models, TreeModelChildren.RefreshingInfo refreshInfo) {
        //System.err.println("HYPER node: refreshTheChildren("+model+", "+refreshInfo+")");
        //Thread.dumpStack();
        Children ch = getChildren();
        if (ch instanceof TreeModelChildren) {
            HyperModelChildren hch = (HyperModelChildren) ch;
            //hch.cleanCachedChildren(model);
            hch.refreshChildren(hch.new HyperRefreshingInfo(refreshInfo, models));
        } else {
            setChildren(new HyperModelChildren (this.model, treeModelRoot, object));
        }
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        //System.err.println("\nTreeModelHyperNode.getDropType("+model+", \n"+action+", "+index+")");
        if (index < 0) {
            // Drop to an area outside models. Try to find some that accepts it:
            PasteType p;
            CompoundModel mm = model.getMain();
            try {
                p = mm.getDropType(object, t, action, index);
            } catch (UnknownTypeException e) {
                p = null;
            }
            if (p == null) {
                for (CompoundModel m : model.getModels()) {
                    if (m != mm) {
                        try {
                            p = m.getDropType(object, t, action, index);
                            if (p != null) break;
                        } catch (UnknownTypeException ex) {
                        }
                    }
                }
            }
            //System.err.println("  PasteType = "+p+"\n");
            return p;
        } else {
            // Drop between nodes of some model.
            PasteType p = null;
            HyperModelChildren hch = (HyperModelChildren) getChildren();
            int index1 = (index > 0) ? index - 1 : index; // node above
            int[] modelIndexPtr = new int[] { -1 };
            CompoundModel cm1 = hch.getRootModelByIndex(index1, modelIndexPtr);
            if (cm1 != null) {
                try {
                    if (index1 < index) modelIndexPtr[0]++;
                    //System.err.println("\nTreeModelHyperNode.getDropType("+cm1+", \n"+action+", "+modelIndexPtr[0]+")");
                    p = cm1.getDropType(object, t, action, modelIndexPtr[0]);
                } catch (UnknownTypeException e) {
                }
            }
            if (p == null && index1 < index) { // node below
                CompoundModel cm2 = hch.getRootModelByIndex(index, modelIndexPtr);
                if (cm2 != null && cm2 != cm1) {
                    try {
                        //System.err.println("\nTreeModelHyperNode.getDropType("+cm2+", \n"+action+", "+modelIndexPtr[0]+")");
                        p = cm2.getDropType(object, t, action, modelIndexPtr[0]);
                    } catch (UnknownTypeException e) {
                    }
                }
            }
            //System.err.println("  PasteType = "+p+"\n");
            return p;
        }
    }

    private static final class HyperModelChildren extends TreeModelChildren {
        
        private HyperCompoundModel model;
        private final java.util.Map<Object, Models.CompoundModel> rootModelsByChildren = new HashMap<Object, Models.CompoundModel>();
        private final java.util.Map<Models.CompoundModel, Object[]> rootChildrenByModels = new HashMap<Models.CompoundModel, Object[]>();
        private final int[] rootModelIndexes; // Children indexes of root models. First is 0.

        public HyperModelChildren (
            HyperCompoundModel model,
            TreeModelRoot   treeModelRoot,
            Object          object
        ) {
            super(null, model.getColumns(), treeModelRoot, object);
            this.model = model;
            this.rootModelIndexes = new int[model.getModels().length + 1];
        }

        // TODO: Run children of individual models according to individual asynchronous specifications
        @Override
        protected Executor getModelAsynchronous() {
            Executor exec = null;
            for (Models.CompoundModel m : model.getModels()) {
                try {
                    Executor e = m.asynchronous(CALL.CHILDREN, object);
                    if (exec == null) {
                        exec = e;
                    } else {
                        if (e != AsynchronousModelFilter.CURRENT_THREAD) {
                            exec = e;
                        }
                    }
                } catch (UnknownTypeException ex) {
                    Exceptions.printStackTrace(Exceptions.attachMessage(ex, "model = "+model+", object = "+object));
                }
            }
            if (exec == null) {
                exec = AsynchronousModelFilter.CURRENT_THREAD;
            }
            return exec;
        }

        CompoundModel getRootModelByIndex(int index, int[] modelIndexPtr) {
            for (int i = 1; i < rootModelIndexes.length; i++) {
                if (rootModelIndexes[i] > index) {
                    modelIndexPtr[0] = index - rootModelIndexes[i - 1];
                    return model.getModels()[i - 1];
                }
            }
            return null;
        }

        @Override
        protected Object[] getModelChildren(RefreshingInfo refreshInfo) throws UnknownTypeException {
            if (refreshInfo instanceof HyperRefreshingInfo) {
                HyperRefreshingInfo hri = (HyperRefreshingInfo) refreshInfo;
                for (Models.CompoundModel m : hri.getRefreshedModels()) {
                    cleanCachedChildren(m);
                }
            }
            Object[] ch = null;
            TreeModelFilter tf = model.getTreeFilter();
            int i = 0, totalCount = 0;
            for (Models.CompoundModel m : model.getModels()) {
                Object[] mch;
                synchronized (rootChildrenByModels) {
                    mch = rootChildrenByModels.get(m);
                }
                if (mch == null) {
                    if (tf != null) {
                        int count = tf.getChildrenCount (m, object);
                        mch = tf.getChildren (
                            m,
                            object,
                            0,
                            count
                        );
                        if (mch == null) {
                            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+tf+"\nreturned null children for parent '"+object+"'");
                            mch = new Object[] {};
                        }
                    } else {
                        int count = m.getChildrenCount (object);
                        mch = m.getChildren (
                            object,
                            0,
                            count
                        );
                        if (mch == null) {
                            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+m+"\nreturned null children for parent '"+object+"'");
                            mch = new Object[] {};
                        }
                    }
                    synchronized (rootModelsByChildren) {
                        for (Object o : mch) {
                            rootModelsByChildren.put(o, m);
                        }
                    }
                    synchronized (rootChildrenByModels) {
                        rootChildrenByModels.put(m, mch);
                    }
                }
                rootModelIndexes[i++] = totalCount;
                totalCount += mch.length;
                if (ch == null) {
                    ch = mch;
                } else {
                    int chl = ch.length;
                    Object[] nch = new Object[chl + mch.length];
                    System.arraycopy(ch, 0, nch, 0, chl);
                    System.arraycopy(mch, 0, nch, chl, mch.length);
                    ch = nch;
                }
            }
            rootModelIndexes[i] = totalCount;
            return ch;
        }

        private void cleanCachedChildren(Models.CompoundModel model) {
            Object[] children;
            synchronized (rootChildrenByModels) {
                children = rootChildrenByModels.remove(model);
            }
            if (children != null) {
                synchronized (rootModelsByChildren) {
                    for (Object ch : children) {
                        rootModelsByChildren.remove(ch);
                    }
                }
            }
        }

        @Override
        protected void expandIfSetToExpanded(Object child) {
            Models.CompoundModel model;
            synchronized (rootModelsByChildren) {
                model = rootModelsByChildren.get(child);
            }
            if (model == null) return ;
            try {
                DefaultTreeExpansionManager.get(model).setChildrenToActOn(getTreeDepth());
                if (model.isExpanded (child)) {
                    TreeFeatures treeTable = treeModelRoot.getTreeFeatures ();
                    if (treeTable != null && treeTable.isExpanded(object)) {
                        // Expand the child only if the parent is expanded
                        treeTable.expandNode (child);
                    }
                }
            } catch (UnknownTypeException ex) {
            }
        }

        @Override
        public Node[] createNodes (Object object) {
            if (object == WAIT_KEY) {
                return super.createNodes(object);
            }
            if (object instanceof Exception)
                return new Node[] {
                    new ExceptionNode ((Exception) object)
                };
            Models.CompoundModel m;
            synchronized (rootModelsByChildren) {
                m = rootModelsByChildren.get(object);
            }
            if (m == null) {
                //System.err.println("\n\n\n\n!!! NO NODE for object "+object+"!!!\n\n\n");
                return new Node[] {};
            }
            TreeModelNode tmn = new TreeModelNode (
                m,
                createHyperColumns(model.getColumns(), m.getColumns()),
                treeModelRoot,
                object
            );
            //System.err.println("created node for ("+object+") = "+tmn);
            objectToNode.put (object, new WeakReference<TreeModelNode>(tmn));
            return new Node[] {tmn};
        }

        private static ColumnModel[] createHyperColumns(ColumnModel[] mainColumns, ColumnModel[] columns) {
            int n = Math.min(mainColumns.length, columns.length);
            ColumnModel[] hColumns = new ColumnModel[n];
            for (int i = 0; i < n; i++) {
                hColumns[i] = new HyperColumnModel(mainColumns[i], columns[i]);
            }
            return hColumns;
        }

        public class HyperRefreshingInfo extends RefreshingInfo {

            private Set<Models.CompoundModel> models;

            public HyperRefreshingInfo(RefreshingInfo ri, Set<Models.CompoundModel> models) {
                super(ri.refreshSubNodes);
                this.models = models;
            }

            @Override
            public RefreshingInfo mergeWith(RefreshingInfo rinfo) {
                if (rinfo instanceof HyperRefreshingInfo) {
                    try {
                        this.models.addAll(((HyperRefreshingInfo) rinfo).models);
                    } catch (UnsupportedOperationException uoex) {
                        // add not supported, probably a non-modifiable set
                        this.models = new HashSet<CompoundModel>(models);
                        this.models.addAll(((HyperRefreshingInfo) rinfo).models);
                    }
                }
                this.refreshSubNodes = this.refreshSubNodes || rinfo.refreshSubNodes;
                return this;
            }

            public Set<Models.CompoundModel> getRefreshedModels() {
                return models;
            }

            @Override
            public boolean isRefreshSubNodes(Object child) {
                //System.err.println("isRefreshSubNodes("+child+") = "+(models.contains(rootModelsByChildren.get(child))));
                //System.err.println("  child's model    = "+rootModelsByChildren.get(child));
                //System.err.println("  refreshing models = "+models);
                return super.isRefreshSubNodes(child) && models.contains(rootModelsByChildren.get(child));
            }

        }
    }

}
