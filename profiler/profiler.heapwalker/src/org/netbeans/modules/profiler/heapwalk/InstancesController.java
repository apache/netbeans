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
import org.netbeans.modules.profiler.heapwalk.ui.InstancesControllerUI;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;


/**
 *
 * @author Jiri Sedlacek
 */
public class InstancesController extends AbstractTopLevelController implements FieldsBrowserController.Handler,
                                                                               ReferencesBrowserController.Handler,
                                                                               NavigationHistoryManager.NavigationHistoryCapable {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class Configuration extends NavigationHistoryManager.Configuration {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final long instanceID;
        private final List<TreePath> expandedFields;
        private final TreePath selectedField;
        private final List<TreePath> expandedReferences;
        private final TreePath selectedReference;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Configuration(long instanceID, List<TreePath> expandedFields, TreePath selectedField,
                             List<TreePath> expandedReferences, TreePath selectedReference) {
            this.instanceID = instanceID;
            this.expandedFields = expandedFields;
            this.selectedField = selectedField;
            this.expandedReferences = expandedReferences;
            this.selectedReference = selectedReference;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public long getInstanceID() {
            return instanceID;
        }
        
        public List<TreePath> getExpandedFields() {
            return expandedFields;
        }
        
        public TreePath getSelectedField() {
            return selectedField;
        }
        
        public List getExpandedReferences() {
            return expandedReferences;
        }
        
        public TreePath getSelectedReference() {
            return selectedReference;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ClassPresenterPanel classPresenter;
    private FieldsBrowserController fieldsBrowserController;
    private HeapFragmentWalker heapFragmentWalker;
    private InstancesListController instancesListController;
    private JavaClass selectedClass;
    private ReferencesBrowserController referencesBrowserController;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public InstancesController(HeapFragmentWalker heapFragmentWalker) {
        this.heapFragmentWalker = heapFragmentWalker;

        classPresenter = new ClassPresenterPanel() {
            public void refresh() { setJavaClass(selectedClass); }
        };
        instancesListController = new InstancesListController(this);
        boolean showClassLoaders = heapFragmentWalker.countClassLoaders() > 1;
        fieldsBrowserController = new FieldsBrowserController(this, FieldsBrowserController.ROOT_INSTANCE, showClassLoaders);
        referencesBrowserController = new ReferencesBrowserController(this);

        classPresenter.setHeapFragmentWalker(heapFragmentWalker);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setClass(final JavaClass jClass) {
        if (jClass == selectedClass) {
            return;
        }

        selectedClass = jClass;
        ((InstancesControllerUI) getPanel()).update();
        updateClientPresentersEnabling(getClientPresenters()); // update enabling when no class selected

        classPresenter.setJavaClass(jClass);

        if (!instancesListController.getPanel().isVisible()) {
            instancesListController.getPanel().setVisible(true); // must be opened first to propagate selection to Fields & References browser. Anyway, doesn't make much sense at all when closed.
        }

        instancesListController.scheduleFirstInstanceSelection();
        instancesListController.setClass(jClass);
    }

    // --- Internal interface ----------------------------------------------------
    public ClassPresenterPanel getClassPresenterPanel() {
        return classPresenter;
    }

    // --- NavigationHistoryManager.NavigationHistoryCapable implementation ------

    //  public NavigationHistoryManager.Configuration getCurrentConfiguration() {
    //    return new NavigationHistoryManager.Configuration();
    //  }
    //
    //  public void configure(NavigationHistoryManager.Configuration configuration) {
    //    heapFragmentWalker.switchToHistoryInstancesView();
    //  }
    public Configuration getCurrentConfiguration() {
        // Selected instance
        long selectedInstanceID = -1;
        List<TreePath> expandedFields = null;
        TreePath selectedField = null;
        List<TreePath> expandedReferences = null;
        TreePath selectedReference = null;
        Instance selectedInstance = getSelectedInstance();

        if (selectedInstance != null) {
            selectedInstanceID = selectedInstance.getInstanceId();
            expandedFields = fieldsBrowserController.getExpandedPaths();
            selectedField = fieldsBrowserController.getSelectedRow();
            expandedReferences = referencesBrowserController.getExpandedPaths();
            selectedReference = referencesBrowserController.getSelectedRow();
        }

        return new Configuration(selectedInstanceID, expandedFields, selectedField,
                                 expandedReferences, selectedReference);
    }

    public FieldsBrowserController getFieldsBrowserController() {
        return fieldsBrowserController;
    }

    // --- Public interface ------------------------------------------------------
    public HeapFragmentWalker getHeapFragmentWalker() {
        return heapFragmentWalker;
    }

    public InstancesListController getInstancesListController() {
        return instancesListController;
    }

    public ReferencesBrowserController getReferencesBrowserController() {
        return referencesBrowserController;
    }

    public JavaClass getSelectedClass() {
        return selectedClass;
    }

    public Instance getSelectedInstance() {
        return instancesListController.getSelectedInstance();
    }

    public void configure(NavigationHistoryManager.Configuration configuration) {
        if (configuration instanceof Configuration) {
            Configuration c = (Configuration) configuration;

            // Selected instance
            Instance selectedInstance = null;
            long selectedInstanceID = c.getInstanceID();

            if (selectedInstanceID != -1) {
                selectedInstance = heapFragmentWalker.getHeapFragment().getInstanceByID(selectedInstanceID);
            }

            if (selectedInstance != null) {
                final JavaClass jClass = selectedInstance.getJavaClass();

                selectedClass = jClass;
                ((InstancesControllerUI) getPanel()).update();
                updateClientPresentersEnabling(getClientPresenters()); // update enabling when no class selected

                heapFragmentWalker.switchToHistoryInstancesView();

                classPresenter.setJavaClass(jClass);
                
                fieldsBrowserController.restoreState(
                        c.getExpandedFields(), c.getSelectedField());
                referencesBrowserController.restoreState(
                        c.getExpandedReferences(), c.getSelectedReference());
                
                instancesListController.showInstance(selectedInstance);
            } else {
                heapFragmentWalker.switchToHistoryInstancesView();
            }
        } else {
            throw new IllegalArgumentException("Unsupported configuration: " + configuration); // NOI18N
        }
    }

    public void instanceSelected() {
        Instance selectedInstance = getSelectedInstance();
        fieldsBrowserController.setInstance(selectedInstance);
        referencesBrowserController.setInstance(selectedInstance);
    }

    public void showClass(JavaClass javaClass) {
        heapFragmentWalker.getClassesController().showClass(javaClass);
    }

    // --- FieldsBrowserController.Handler implementation ------------------------
    public void showInstance(final Instance instance) {
        final JavaClass jClass = instance.getJavaClass();

        selectedClass = jClass;
        ((InstancesControllerUI) getPanel()).update();
        updateClientPresentersEnabling(getClientPresenters()); // update enabling when no class selected

        heapFragmentWalker.switchToInstancesView();

        //    BrowserUtils.performTask(new Runnable() {
        //      public void run() {
        classPresenter.setJavaClass(jClass);
        instancesListController.showInstance(instance);

        //      }
        //    });
    }

    protected AbstractButton[] createClientPresenters() {
        return new AbstractButton[] {
                   instancesListController.getPresenter(), fieldsBrowserController.getPresenter(),
                   referencesBrowserController.getPresenter()
               };
    }

    protected AbstractButton createControllerPresenter() {
        return ((InstancesControllerUI) getPanel()).getPresenter();
    }

    // --- Protected implementation ----------------------------------------------
    protected JPanel createControllerUI() {
        return new InstancesControllerUI(this);
    }

    protected void updateClientPresentersEnabling(AbstractButton[] clientPresenters) {
        if (selectedClass == null) {
            for (int i = 0; i < clientPresenters.length; i++) {
                clientPresenters[i].setVisible(false);
            }
        } else {
            for (int i = 0; i < clientPresenters.length; i++) {
                clientPresenters[i].setVisible(true);
            }

            super.updateClientPresentersEnabling(clientPresenters);
        }
    }
}
