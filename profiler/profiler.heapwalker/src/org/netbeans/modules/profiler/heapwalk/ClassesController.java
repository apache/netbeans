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

import java.util.List;
import org.netbeans.lib.profiler.heap.*;
import org.netbeans.modules.profiler.heapwalk.ui.ClassesControllerUI;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;


/**
 *
 * @author Jiri Sedlacek
 */
public class ClassesController extends AbstractTopLevelController implements FieldsBrowserController.Handler,
                                                                             NavigationHistoryManager.NavigationHistoryCapable {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class Configuration extends NavigationHistoryManager.Configuration {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final long javaClassID;
        private final List<TreePath> expandedStaticFields;
        private final TreePath selectedStaticField;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Configuration(long javaClassID, List<TreePath> expandedStaticFields, TreePath selectedStaticField) {
            this.javaClassID = javaClassID;
            this.expandedStaticFields = expandedStaticFields;
            this.selectedStaticField = selectedStaticField;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public long getJavaClassID() {
            return javaClassID;
        }
        
        public List<TreePath> getExpandedStaticFields() {
            return expandedStaticFields;
        }
        
        public TreePath getSelectedStaticField() {
            return selectedStaticField;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ClassesListController classesListController;
    private FieldsBrowserController staticFieldsBrowserController;
    private HeapFragmentWalker heapFragmentWalker;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public ClassesController(HeapFragmentWalker heapFragmentWalker) {
        this.heapFragmentWalker = heapFragmentWalker;

        classesListController = new ClassesListController(this);
        staticFieldsBrowserController = new FieldsBrowserController(this, FieldsBrowserController.ROOT_CLASS, true);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Internal interface ----------------------------------------------------
    public ClassesListController getClassesListController() {
        return classesListController;
    }

    // --- NavigationHistoryManager.NavigationHistoryCapable implementation ------
    public Configuration getCurrentConfiguration() {
        // Selected class
        long selectedClassID = -1;
        List<TreePath> expandedStaticFields = null;
        TreePath selectedStaticField = null;
        JavaClass selectedClass = getSelectedClass();

        if (selectedClass != null) {
            selectedClassID = selectedClass.getJavaClassId();
            expandedStaticFields = staticFieldsBrowserController.getExpandedPaths();
            selectedStaticField = staticFieldsBrowserController.getSelectedRow();
        }

        return new Configuration(selectedClassID, expandedStaticFields, selectedStaticField);
    }

    // --- Public interface ------------------------------------------------------
    public HeapFragmentWalker getHeapFragmentWalker() {
        return heapFragmentWalker;
    }

    public JavaClass getSelectedClass() {
        return classesListController.getSelectedClass();
    }

    public FieldsBrowserController getStaticFieldsBrowserController() {
        return staticFieldsBrowserController;
    }

    public void classSelected() {
        JavaClass selectedClass = getSelectedClass();
        staticFieldsBrowserController.setJavaClass(selectedClass);
    }

    public void configure(NavigationHistoryManager.Configuration configuration) {
        if (configuration instanceof Configuration) {
            Configuration c = (Configuration) configuration;

            heapFragmentWalker.switchToHistoryClassesView();

            // Selected class
            JavaClass selectedClass = null;
            long selectedClassID = c.getJavaClassID();

            if (selectedClassID != -1) {
                selectedClass = heapFragmentWalker.getHeapFragment().getJavaClassByID(selectedClassID);
            }

            if (selectedClass != null) {
                staticFieldsBrowserController.restoreState(
                        c.getExpandedStaticFields(), c.getSelectedStaticField());
                classesListController.selectClass(selectedClass);
            }
        } else {
            throw new IllegalArgumentException("Unsupported configuration: " + configuration); // NOI18N
        }
    }

    public void showClass(JavaClass javaClass) {
        heapFragmentWalker.switchToClassesView();

        if (!classesListController.getPanel().isVisible()) {
            classesListController.getPanel().setVisible(true);
        }

        classesListController.selectClass(javaClass);
    }

    // --- FieldsBrowserController.Handler implementation ------------------------
    public void showInstance(Instance instance) {
        heapFragmentWalker.getInstancesController().showInstance(instance);
    }

    protected AbstractButton[] createClientPresenters() {
        return new AbstractButton[] { classesListController.getPresenter(), staticFieldsBrowserController.getPresenter() };
    }

    protected AbstractButton createControllerPresenter() {
        return ((ClassesControllerUI) getPanel()).getPresenter();
    }

    // --- Protected implementation ----------------------------------------------
    protected JPanel createControllerUI() {
        return new ClassesControllerUI(this);
    }
}
