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

package org.netbeans.modules.profiler.heapwalk;

import java.util.List;
import org.netbeans.lib.profiler.heap.*;
import org.netbeans.modules.profiler.heapwalk.model.AbstractHeapWalkerNode;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNodeFactory;
import org.netbeans.modules.profiler.heapwalk.ui.FieldsBrowserControllerUI;
import org.openide.util.NbBundle;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "FieldsBrowserController_NoInstanceSelectedString=<No Instance Selected>",
    "FieldsBrowserController_NoClassSelectedString=<No Class Selected>",
    "FieldsBrowserController_NoneString=<none>"
})
public class FieldsBrowserController extends AbstractController {

    private final boolean showClassLoaders;
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface Handler {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public HeapFragmentWalker getHeapFragmentWalker();

        public void showClass(JavaClass javaClass);

        public void showInstance(Instance instance);
    }

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

//    private class FieldsComparator implements Comparator {
//        //~ Instance fields ------------------------------------------------------------------------------------------------------
//
//        private boolean sortingOrder;
//        private int sortingColumn;
//
//        //~ Constructors ---------------------------------------------------------------------------------------------------------
//
//        public FieldsComparator(int sortingColumn, boolean sortingOrder) {
//            this.sortingColumn = sortingColumn;
//            this.sortingOrder = sortingOrder;
//        }
//
//        //~ Methods --------------------------------------------------------------------------------------------------------------
//
//        public int compare(Object o1, Object o2) {
//            FieldValue field1 = sortingOrder ? (FieldValue) o1 : (FieldValue) o2;
//            FieldValue field2 = sortingOrder ? (FieldValue) o2 : (FieldValue) o1;
//
//            switch (sortingColumn) {
//                case 0: // Name
//                    return field1.getField().getName().compareTo(field2.getField().getName());
//                case 1: // Type
//                    return field1.getField().getType().getName().compareTo(field2.getField().getType().getName());
//                case 2: // Value
//                    return field1.getValue().compareTo(field2.getValue());
//                default:
//                    throw new RuntimeException("Unsupported compare operation for " + o1 + ", " + o2); // NOI18N
//            }
//        }
//    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final int ROOT_INSTANCE = 0;
    public static final int ROOT_CLASS = 1;

    // --- Public interface ------------------------------------------------------
    public static final AbstractHeapWalkerNode EMPTY_INSTANCE_NODE = new AbstractHeapWalkerNode(null) {
        protected String computeName() {
            return Bundle.FieldsBrowserController_NoInstanceSelectedString();
        }

        protected String computeType() {
            return Bundle.FieldsBrowserController_NoneString();
        }

        protected String computeValue() {
            return Bundle.FieldsBrowserController_NoneString();
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

    public static final AbstractHeapWalkerNode EMPTY_CLASS_NODE = new AbstractHeapWalkerNode(null) {
        protected String computeName() {
            return Bundle.FieldsBrowserController_NoClassSelectedString();
        }

        protected String computeType() {
            return Bundle.FieldsBrowserController_NoneString();
        }

        protected String computeValue() {
            return Bundle.FieldsBrowserController_NoneString();
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


    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Handler instancesControllerHandler;
    private Instance instance;
    private JavaClass javaClass;
    private int rootMode;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public FieldsBrowserController(Handler instancesControllerHandler, int rootMode) {
        this(instancesControllerHandler, rootMode, true);
    }

    public FieldsBrowserController(Handler instancesControllerHandler, int rootMode, boolean showClassLoaders) {
        this.instancesControllerHandler = instancesControllerHandler;
        this.rootMode = rootMode;
        this.showClassLoaders = showClassLoaders;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Internal interface ----------------------------------------------------
    public Handler getInstancesControllerHandler() {
        return instancesControllerHandler;
    }

    public HeapWalkerNode getFilteredSortedFields(String filterValue, int sortingColumn, boolean sortingOrder) {
        if (rootMode == ROOT_INSTANCE) {
            if (instance == null) {
                return EMPTY_INSTANCE_NODE;
            }

            return getSortedFields(getFilteredFields(getFields(instance), filterValue), sortingColumn, sortingOrder);
        } else if (rootMode == ROOT_CLASS) {
            if (javaClass == null) {
                return EMPTY_CLASS_NODE;
            }

            return getSortedFields(getFilteredFields(getFields(javaClass), filterValue), sortingColumn, sortingOrder);
        } else {
            return null;
        }
    }

    public void setInstance(Instance instance) {
        this.rootMode = ROOT_INSTANCE;
        this.instance = instance;
        this.javaClass = null;
        update();
    }

    public void setJavaClass(JavaClass javaClass) {
        this.rootMode = ROOT_CLASS;
        this.instance = null;
        this.javaClass = javaClass;
        update();
    }

    public int getRootMode() {
        return rootMode;
    }

    public void createNavigationHistoryPoint() {
        instancesControllerHandler.getHeapFragmentWalker().createNavigationHistoryPoint();
    }

    public void navigateToClass(JavaClass javaClass) {
        instancesControllerHandler.showClass(javaClass);
    }

    public void navigateToInstance(Instance instance) {
        instancesControllerHandler.showInstance(instance);
    }

    public void showInstance(Instance instance) {
        if (this.instance != instance) {
            setInstance(instance);
        }
    }

    public void showJavaClass(JavaClass javaClass) {
        if (this.javaClass != javaClass) {
            setJavaClass(javaClass);
        }
    }

    // --- Private implementation ------------------------------------------------
    public void update() {
        ((FieldsBrowserControllerUI) getPanel()).update();
    }

    protected AbstractButton createControllerPresenter() {
        return ((FieldsBrowserControllerUI) getPanel()).getPresenter();
    }

    public List<TreePath> getExpandedPaths() {
        return ((FieldsBrowserControllerUI)getPanel()).getExpandedPaths();
    }

    public TreePath getSelectedRow() {
        return ((FieldsBrowserControllerUI)getPanel()).getSelectedRow();
    }

    public void restoreState(List expanded, TreePath selected) {
        ((FieldsBrowserControllerUI)getPanel()).restoreState(expanded, selected);
    }

    // --- Protected implementation ----------------------------------------------
    protected JPanel createControllerUI() {
        return new FieldsBrowserControllerUI(this);
    }

    private HeapWalkerNode getFields(final Instance instance) {
        int fieldsMode = showClassLoaders ? HeapWalkerNode.MODE_FIELDS : HeapWalkerNode.MODE_FIELDS_NO_CLASSLOADER;

        return HeapWalkerNodeFactory.createRootInstanceNode(instance, "this",   // NOI18N
                new Runnable() { public void run() { ((FieldsBrowserControllerUI) getPanel()).refreshView(); } },
                new Runnable() { public void run() { getPanel().repaint(); } },
                fieldsMode, instancesControllerHandler.getHeapFragmentWalker().getHeapFragment());
    }

    private HeapWalkerNode getFields(final JavaClass javaClass) {
        return HeapWalkerNodeFactory.createRootClassNode(javaClass, "class", // NOI18N
                new Runnable() { public void run() { ((FieldsBrowserControllerUI) getPanel()).refreshView(); } },
                new Runnable() { public void run() { getPanel().repaint(); } },
                HeapWalkerNode.MODE_FIELDS, instancesControllerHandler.getHeapFragmentWalker().getHeapFragment());
    }

    private HeapWalkerNode getFilteredFields(HeapWalkerNode fields, String filterValue) {
        //    ArrayList filteredFields = new ArrayList();
        //
        //    Iterator fieldsIterator = fields.iterator();
        //    while (fieldsIterator.hasNext()) {
        //      FieldValue field = (FieldValue)fieldsIterator.next();
        //      if (matchesFilter(field)) filteredFields.add(field);
        //    }
        //
        //    return filteredFields;
        return fields;
    }

    private HeapWalkerNode getSortedFields(HeapWalkerNode filteredFields, int sortingColumn, boolean sortingOrder) {
        //Collections.sort(filteredFields, new FieldsComparator(sortingColumn, sortingOrder));
        return filteredFields;
    }

//    private boolean matchesFilter(FieldValue field) {
//        return true;
//    }
}
