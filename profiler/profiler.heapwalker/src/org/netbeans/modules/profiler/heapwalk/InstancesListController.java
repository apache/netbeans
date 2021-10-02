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

package org.netbeans.modules.profiler.heapwalk;


import java.text.NumberFormat;
import org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.netbeans.modules.profiler.heapwalk.model.ChildrenComputer;
import org.netbeans.modules.profiler.heapwalk.model.ClassNode;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNodeFactory;
import org.netbeans.modules.profiler.heapwalk.ui.InstancesListControllerUI;
import org.openide.util.NbBundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.heapwalk.details.api.DetailsSupport;
import org.netbeans.modules.profiler.heapwalk.model.RootNode;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "InstancesListController_NoInstanceString=<No Instance>",
    "InstancesListController_InstancesNumberString=<{0} instances>"
})
public class InstancesListController extends AbstractController {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public interface InstancesListNode {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public TreePath getInstancePath(Instance instance);

        public String getName();

        public String getReachableSize();

        public String getRetainedSize();

        public String getSize();
        
        public String getID();
    }

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public class InstancesListClassNode extends ClassNode.RootNode implements InstancesListNode {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        String filterValue;
        boolean sortingOrder;
        int sortingColumn;
        private String size;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public InstancesListClassNode(JavaClass javaClass, String filterValue, int sortingColumn, boolean sortingOrder) {
            super(javaClass, "class", null); // NOI18N

            this.size = String.valueOf(javaClass.getAllInstancesSize());
            this.filterValue = filterValue;
            this.sortingColumn = sortingColumn;
            this.sortingOrder = sortingOrder;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public GCRoot getGCRoot(Instance instance) {
            return instancesController.getHeapFragmentWalker().getHeapFragment().getGCRoot(instance);
        }

        public TreePath getInstancePath(Instance instance) {
            TreePath instancePath = null;

            if (currentlyHasChildren()) {
                HeapWalkerNode[] children = getChildren();

                for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof InstancesListNode) {
                        instancePath = ((InstancesListNode) children[i]).getInstancePath(instance);

                        if (instancePath != null) {
                            break;
                        }
                    }
                }
            }

            return instancePath;
        }
        
        @Override
        public String getID() {
            return "0x"+Long.toHexString(getJavaClass().getJavaClassId());
        }

        public JavaClass getJavaClassByID(long javaclassId) {
            return instancesController.getHeapFragmentWalker().getHeapFragment().getJavaClassByID(javaclassId);
        }
        
        public String getDetails(Instance instance) {
            return DetailsSupport.getDetailsString(instance, instancesController.getHeapFragmentWalker().getHeapFragment());
        }
    
        public void repaintView() {
            getPanel().repaint();
        }

        public boolean isLeaf() {
            return false;
        }

        public String getReachableSize() {
            return "N/A"; // NOI18N
        }

        public String getRetainedSize() {
            return "N/A"; // NOI18N
        }

        public String getSize() {
            return size;
        }

        public void refreshView() {
            ((InstancesListControllerUI) getPanel()).refreshView();

            if (pathToSelect != null) {
                ((InstancesListControllerUI) getPanel()).selectPath(pathToSelect);
                pathToSelect = null;
            }
        }
        
        protected ChildrenComputer getChildrenComputer() {
            return new ChildrenComputer() {
                public HeapWalkerNode[] computeChildren() {
                    HeapWalkerNode[] children = null;

                    List instances = getJavaClass().getInstances();
                    List filteredInstances = getFilteredInstances(instances, filterValue);
                    List sortedFilteredInstances = null;

                    if (instanceToSelect != null) {
                        sortedFilteredInstances = getSortedInstances(filteredInstances, sortingColumn, sortingOrder);
                    }

                    if (filteredInstances.size() == 0) {
                        // Class has no instances
                        children = new HeapWalkerNode[1];
                        children[0] = HeapWalkerNodeFactory.createNoItemsNode(InstancesListClassNode.this);
                    } else if (filteredInstances.size() > HeapWalkerNodeFactory.ITEMS_COLLAPSE_THRESHOLD) {
                        int instanceToSelectIndex = -1;

                        if (instanceToSelect != null) {
                            if (instanceToSelect == INSTANCE_FIRST) {
                                instanceToSelectIndex = 0;
                            } else {
                                instanceToSelectIndex = sortedFilteredInstances.indexOf(instanceToSelect);
                            }
                        }

                        int childrenCount = filteredInstances.size();
                        BrowserUtils.GroupingInfo groupingInfo = BrowserUtils.getGroupingInfo(childrenCount);
                        int containersCount = groupingInfo.containersCount;
                        int collapseUnitSize = groupingInfo.collapseUnitSize;

                        children = new HeapWalkerNode[containersCount];

                        for (int i = 0; i < containersCount; i++) {
                            int unitStartIndex = collapseUnitSize * i;
                            int unitEndIndex = Math.min(unitStartIndex + collapseUnitSize, childrenCount) - 1;

                            if ((instanceToSelectIndex != -1)
                                    && ((instanceToSelectIndex >= unitStartIndex) && (instanceToSelectIndex <= unitEndIndex))) {
                                children[i] = new InstancesListContainerNode(InstancesListClassNode.this, unitStartIndex,
                                                                             unitEndIndex, filterValue, sortingColumn,
                                                                             sortingOrder, sortedFilteredInstances,
                                                                             instanceToSelectIndex);
                                instanceToSelectIndex = -1;
                            } else {
                                children[i] = new InstancesListContainerNode(InstancesListClassNode.this, unitStartIndex,
                                                                             unitEndIndex, filterValue, sortingColumn,
                                                                             sortingOrder);

                                if (containerToSelectIndex == i) {
                                    pathToSelect = new TreePath(new Object[] { InstancesListClassNode.this, children[i] });
                                    containerToSelectIndex = -1;
                                    instanceToSelect = null;
                                }
                            }
                        }

                        containerToSelectIndex = -1;
                        instanceToSelect = null;
                    } else {
                        // Class has at least one instance
                        if (sortedFilteredInstances == null) {
                            sortedFilteredInstances = getSortedInstances(filteredInstances, sortingColumn, sortingOrder);
                        }

                        children = new HeapWalkerNode[sortedFilteredInstances.size()];

                        for (int i = 0; i < children.length; i++) {
                            Instance instance = (Instance) sortedFilteredInstances.get(i);
                            children[i] = new InstancesListInstanceNode(instance, InstancesListClassNode.this);

                            if ((instanceToSelect != null)
                                    && (((i == 0) && (instanceToSelect == INSTANCE_FIRST))
                                           || (instance.equals(instanceToSelect)))) {
                                pathToSelect = new TreePath(new Object[] { InstancesListClassNode.this, children[i] });
                                instanceToSelect = null;
                                containerToSelectIndex = -1;
                            }

                            ;
                        }

                        instanceToSelect = null;
                        containerToSelectIndex = -1;
                    }

                    return children;
                }
            };
        }

        protected ImageIcon computeIcon() {
            return getJavaClass().isArray() ? BrowserUtils.ICON_ARRAY : BrowserUtils.ICON_INSTANCE;
        }

        protected String computeName() {
            return jClass.getName();
        }
    }

    public class InstancesListContainerNode extends AbstractHeapWalkerNode implements InstancesListNode {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private String filterValue;
        private boolean sortingOrder;
        private int endIndex;
        private int sortingColumn;
        private int startIndex;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public InstancesListContainerNode(ClassNode classNode, int startIndex, int endIndex, String filterValue,
                                          int sortingColumn, boolean sortingOrder) {
            super(classNode);
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.filterValue = filterValue;
            this.sortingColumn = sortingColumn;
            this.sortingOrder = sortingOrder;
        }

        public InstancesListContainerNode(ClassNode classNode, int startIndex, int endIndex, String filterValue,
                                          int sortingColumn, boolean sortingOrder, List sortedFilteredInstances,
                                          int instanceToSelectIndex) {
            this(classNode, startIndex, endIndex, filterValue, sortingColumn, sortingOrder);

            int itemsCount = endIndex - startIndex + 1;
            HeapWalkerNode[] children = new HeapWalkerNode[itemsCount];

            for (int i = 0; i < itemsCount; i++) {
                children[i] = new InstancesListInstanceNode((Instance) sortedFilteredInstances.get(startIndex + i),
                                                            InstancesListContainerNode.this);

                if (instanceToSelectIndex == (startIndex + i)) {
                    pathToSelect = new TreePath(new Object[] { classNode, InstancesListContainerNode.this, children[i] });
                }
            }

            instanceToSelect = null;
            containerToSelectIndex = -1;

            setChildren(children);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------
        public TreePath getInstancePath(Instance instance) {
            TreePath instancePath = null;

            if (currentlyHasChildren()) {
                HeapWalkerNode[] children = getChildren();

                for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof InstancesListNode) {
                        instancePath = ((InstancesListNode) children[i]).getInstancePath(instance);

                        if (instancePath != null) {
                            break;
                        }
                    }
                }
            }

            return instancePath;
        }

        public boolean isLeaf() {
            return false;
        }

        @Override
        public String getID() {
            return ""; // NOI18N;
        }
        
        public String getReachableSize() {
            return ""; // NOI18N
        }

        protected ChildrenComputer getChildrenComputer() {
            return new ChildrenComputer() {
                    public HeapWalkerNode[] computeChildren() {
                        int itemsCount = endIndex - startIndex + 1;
                        HeapWalkerNode[] children = new HeapWalkerNode[itemsCount];

                        List instances = ((ClassNode) getParent()).getJavaClass().getInstances();
                        List filteredInstances = getFilteredInstances(instances, filterValue);
                        List sortedFilteredInstances = getSortedInstances(filteredInstances, sortingColumn, sortingOrder);

                        for (int i = 0; i < children.length; i++) {
                            Instance instance = (Instance) sortedFilteredInstances.get(startIndex + i);
                            children[i] = new InstancesListInstanceNode(instance, InstancesListContainerNode.this);

                            if ((instanceToSelect != null)
                                    && ((((startIndex + i) == 0) && (instanceToSelect == INSTANCE_FIRST))
                                           || (instance.equals(instanceToSelect)))) {
                                pathToSelect = new TreePath(new Object[] {
                                                                children[i].getParent().getParent(), children[i].getParent(),
                                                                children[i]
                                                            });
                                instanceToSelect = null;
                                containerToSelectIndex = -1;
                            }
                        }

                        return children;
                    }
                };
        }

        protected HeapWalkerNode[] computeChildren() {
            return BrowserUtils.lazilyCreateChildren(this, getChildrenComputer());
        }

        protected Icon computeIcon() {
            return null;
        }

        protected String computeName() {
            return Bundle.InstancesListController_InstancesNumberString((endIndex - startIndex + 1));
        }

        protected String computeType() {
            return getParent().getType();
        }

        protected String computeValue() {
            return getParent().getValue();
        }

        protected String computeSize() {
            return ""; // NOI18N
        }

        protected String computeRetainedSize() {
            return ""; // NOI18N
        }
        
        public Object getNodeID() {
            return startIndex;
        }
    }

    public static class InstancesListInstanceNode extends HeapWalkerNode implements InstancesListNode {

        private static final NumberFormat numberFormat = NumberFormat.getInstance();
        
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private HeapWalkerNode parent;
        private ImageIcon icon;
        private Instance instance;
        private String name;
        private String reachableSize;
        private String retainedSize;
        private String size;
        private String details;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public InstancesListInstanceNode(Instance instance, HeapWalkerNode parent) {
            this.parent = parent;
            this.instance = instance;

            this.name = "#" + instance.getInstanceNumber(); // NOI18N
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------
        
        public HeapWalkerNode getChild(int index) {
            return null;
        }

        public HeapWalkerNode[] getChildren() {
            return new HeapWalkerNode[0];
        }

        public Icon getIcon() {
            if (icon == null) {
                icon = computeIcon();
            }

            return icon;
        }

        public int getIndexOfChild(Object child) {
            return -1;
        }

        public Instance getInstance() {
            return instance;
        }

        @Override
        public String getID() {
            return "0x"+Long.toHexString(getInstance().getInstanceId());
        }

        public TreePath getInstancePath(Instance inst) {
            TreePath instancePath = null;

            if (instance.equals(inst)) {
                ArrayList paths = new ArrayList();
                HeapWalkerNode node = this;

                while (node != null) {
                    paths.add(0, node);
                    node = node.getParent();
                }

                instancePath = new TreePath(paths.toArray());
            }

            return instancePath;
        }

        public boolean isLeaf() {
            return true;
        }

        public int getMode() {
            return 0;
        }

        @Override
        public boolean isModeFields() {
            return false;
        }

        public int getNChildren() {
            return 0;
        }

        public String getName() {
            return name;
        }

        public HeapWalkerNode getParent() {
            return parent;
        }

        public String getReachableSize() {
            if (reachableSize == null)
                reachableSize = "N/A"; // NOI18N
            return reachableSize;
        }

        public String getRetainedSize() {
            if (retainedSize == null)
                retainedSize = numberFormat.format(instance.getRetainedSize());
            return retainedSize;
        }

        public boolean isRoot() {
            return false;
        }

        public String getSimpleType() {
            return parent.getSimpleType();
        }

        public String getSize() {
            if (size == null)
                size = numberFormat.format(instance.getSize());
            return size;
        }

        public String getType() {
            return parent.getType();
        }

        public String getValue() {
            return parent.getValue();
        }
        
        public String getDetails() {
            if (details == null) {
                details = "";
                HeapWalkerNode _root = BrowserUtils.getRoot(parent);
                if (_root instanceof RootNode) {
                    final RootNode root = (RootNode)_root;
                    BrowserUtils.performTask(new Runnable() {
                        public void run() {
                            final String d = root.getDetails(instance);
                            if (d != null) SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    details = d;
                                    root.repaintView();
                                }
                            });
                        }
                    });
                }
            }
            return details;
        }

        public boolean currentlyHasChildren() {
            return false;
        }

        public String toString() {
            return getName();
        }

        protected ImageIcon computeIcon() {
            return getInstance().getJavaClass().isArray() ? BrowserUtils.ICON_ARRAY : BrowserUtils.ICON_INSTANCE;
        }
        
        public Object getNodeID() {
            return instance;
        }
    }

    private static class InstancesComparator implements Comparator {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private boolean sortingOrder;
        private int sortingColumn;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public InstancesComparator(int sortingColumn, boolean sortingOrder) {
            this.sortingColumn = sortingColumn;
            this.sortingOrder = sortingOrder;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int compare(Object o1, Object o2) {
            Instance instance1 = sortingOrder ? (Instance) o1 : (Instance) o2;
            Instance instance2 = sortingOrder ? (Instance) o2 : (Instance) o1;

            switch (sortingColumn) {
                case 0:
                    return Integer.compare(instance1.getInstanceNumber(),instance2.getInstanceNumber());
                case 1:
                    return Long.compare(instance1.getInstanceId(),instance2.getInstanceId());
                case 2:
                    return Long.compare(instance1.getSize(),instance2.getSize());
                case 3:
                    return Long.compare(instance1.getRetainedSize(),instance2.getRetainedSize());
                case 4:
                    return Long.compare(instance1.getReachableSize(),instance2.getReachableSize());
                default:
                    throw new RuntimeException("Unsupported compare operation for " + o1 + ", " + o2); // NOI18N
            }
        }
    }

    // --- Public interface ------------------------------------------------------
    public static final AbstractHeapWalkerNode EMPTY_INSTANCE_NODE = new AbstractHeapWalkerNode(null) {
        protected String computeName() {
            return Bundle.InstancesListController_NoInstanceString();
        }

        protected String computeType() {
            return ""; // NOI18N
        }

        protected String computeValue() {
            return ""; // NOI18N
        }

        protected String computeSize() {
            return ""; // NOI18N
        }

        protected String computeRetainedSize() {
            return ""; // NOI18N
        }

        protected Icon computeIcon() {
            return null;
        }

        public boolean isLeaf() {
            return true;
        }
    };

    public static final Instance INSTANCE_FIRST = new Instance() {
        public JavaClass getJavaClass() {
            return null;
        }

        public long getInstanceId() {
            return -1;
        }

        public int getInstanceNumber() {
            return -1;
        }

        public long getSize() {
            return -1;
        }

        public long getRetainedSize() {
            return -1;
        }

        public long getReachableSize() {
            return -1;
        }

        public List getFieldValues() {
            return null;
        }

        public Object getValueOfField(String name) {
            return null;
        }

        public List getStaticFieldValues() {
            return null;
        }

        public List getReferences() {
            return null;
        }

        public boolean isGCRoot() {
            return false;
        }

        public Instance getNearestGCRootPointer() {
            return null;
        }
    };


    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    public Instance instanceToSelect;
    public int containerToSelectIndex;
    private Instance selectedInstance;
    private InstancesController instancesController;
    private JavaClass jClass;
    private TreePath pathToSelect;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public InstancesListController(InstancesController instancesController) {
        this.instancesController = instancesController;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setClass(JavaClass jClass) {
        this.jClass = jClass;
        ((InstancesListControllerUI) getPanel()).initColumns();
        update();
    }

    // --- Internal interface ----------------------------------------------------
    public HeapWalkerNode getFilteredSortedInstances(String filterValue, int sortingColumn, boolean sortingOrder) {
        if (jClass == null) {
            return EMPTY_INSTANCE_NODE;
        }

        return getInstances(jClass, filterValue, sortingColumn, sortingOrder);
    }

    // computes instance's container
    // note: creates and filters & sorts list of all instances => very slow and memory intensive, use with caution!
    public HeapWalkerNode getInstanceContainer(Instance instance, InstancesListClassNode rootNode) {
        HeapWalkerNode instanceContainer = null;

        List instances = jClass.getInstances();
        List filteredInstances = getFilteredInstances(instances, rootNode.filterValue);
        List sortedFilteredInstances = getSortedInstances(filteredInstances, rootNode.sortingColumn, rootNode.sortingOrder);

        int instanceIndex = sortedFilteredInstances.indexOf(instance);

        if (instanceIndex != -1) {
            int childrenCount = sortedFilteredInstances.size();
            BrowserUtils.GroupingInfo groupingInfo = BrowserUtils.getGroupingInfo(childrenCount);
            int containersCount = groupingInfo.containersCount;
            int collapseUnitSize = groupingInfo.collapseUnitSize;

            instanceContainer = rootNode.getChild(instanceIndex / collapseUnitSize);
        }

        return instanceContainer;
    }

    public InstancesController getInstancesController() {
        return instancesController;
    }

    public Instance getSelectedInstance() {
        return selectedInstance;
    }

    public void instanceSelected(Instance instance) {
        if (selectedInstance == instance) {
            return;
        }

        selectedInstance = instance;
        instancesController.instanceSelected();
    }

    // Used for scheduling selection of new data to be created & displayed
    public void scheduleContainerSelection(int containerIndex) {
        containerToSelectIndex = containerIndex;
        instanceToSelect = null;
    }

    // Used for scheduling selection of new data to be created & displayed
    public void scheduleFirstInstanceSelection() {
        instanceToSelect = INSTANCE_FIRST;
        containerToSelectIndex = -1;
    }

    // Used for scheduling selection of new data to be created & displayed
    public void scheduleInstanceSelection(Instance instance) {
        instanceToSelect = instance;
        containerToSelectIndex = -1;
    }

    // Used for selecting of currently displayed data
    public void selectInstance(Instance instance) {
        ((InstancesListControllerUI) getPanel()).makeVisible();
        ((InstancesListControllerUI) getPanel()).selectInstance(instance);
    }

    public void showInstance(Instance instance) {
        if (jClass != instance.getJavaClass()) {
            scheduleInstanceSelection(instance);
            setClass(instance.getJavaClass());
        } else {
            selectInstance(instance);
        }
    }

    // --- Private implementation ------------------------------------------------
    public void update() {
        ((InstancesListControllerUI) getPanel()).update();
    }

    protected AbstractButton createControllerPresenter() {
        return ((InstancesListControllerUI) getPanel()).getPresenter();
    }

    // --- Protected implementation ----------------------------------------------
    protected JPanel createControllerUI() {
        return new InstancesListControllerUI(this);
    }

    private List getFilteredInstances(List instances, String filterValue) {
        //    List filteredInstances = new LinkedList();
        //    
        //    Iterator instancesIterator = instances.iterator();
        //    while (instancesIterator.hasNext()) {
        //      Instance instance = (Instance)instancesIterator.next();
        //      if (matchesFilter(instance, filterValue)) filteredInstances.add(instance);
        //    }
        //    
        //    return filteredInstances;
        return instances;
    }

    private HeapWalkerNode getInstances(JavaClass jClass, String filterValue, int sortingColumn, boolean sortingOrder) {
        return new InstancesListClassNode(jClass, filterValue, sortingColumn, sortingOrder);
    }

    private List getSortedInstances(List filteredInstances, int sortingColumn, boolean sortingOrder) {
        Collections.sort(filteredInstances, new InstancesComparator(sortingColumn, sortingOrder));

        return filteredInstances;
    }

    private boolean matchesFilter(Instance instance, String filterValue) {
        return true;
    }
}
