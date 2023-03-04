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

import org.netbeans.lib.profiler.heap.*;
import org.netbeans.modules.profiler.heapwalk.ui.HeapFragmentWalkerUI;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "HeapFragmentWalker_ComputeRetainedMsg=<html><b>Retained sizes will be computed.</b><br><br>For large heap dumps this operation can take a significant<br>amount of time. Do you want to continue?</html>",
    "HeapFragmentWalker_ComputeRetainedCaption=Compute Retained Sizes",
    "HeapFragmentWalker_ComputingRetainedMsg=Computing retained sizes...",
    "HeapFragmentWalker_ComputingRetainedCaption=Computing Retained Sizes"
})
public class HeapFragmentWalker {
    public static final int RETAINED_SIZES_UNSUPPORTED = -1;
    public static final int RETAINED_SIZES_UNKNOWN = 0;
    public static final int RETAINED_SIZES_CANCELLED = 1;
    public static final int RETAINED_SIZES_COMPUTING = 2;
    public static final int RETAINED_SIZES_COMPUTED = 3;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AnalysisController analysisController;
    private OQLController oqlController;
    private ClassesController classesController;
    private Heap heapFragment; // TODO: Should be HeapFragment
    private HeapFragmentWalkerUI walkerUI;
    private HeapWalker heapWalker;
    private InstancesController instancesController;
    private NavigationHistoryManager navigationHistoryManager;
    private SummaryController summaryController;

    private List<StateListener> stateListeners;
    private int retainedSizesStatus;
    private final int heapSegment;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public HeapFragmentWalker(Heap heapFragment, HeapWalker heapWalker) {
        this(heapFragment, heapWalker, false);
    }

    public HeapFragmentWalker(Heap heapFragment, HeapWalker heapWalker, boolean supportsRetainedSizes) {
        this(heapFragment, 0, heapWalker, supportsRetainedSizes);
    }

    HeapFragmentWalker(Heap heapFragment, int heapSegment, HeapWalker heapWalker, boolean supportsRetainedSizes) {
        this.heapFragment = heapFragment;
        this.heapSegment = heapSegment;
        this.heapWalker = heapWalker;

        this.retainedSizesStatus = supportsRetainedSizes ? RETAINED_SIZES_UNKNOWN :
                                                        RETAINED_SIZES_UNSUPPORTED;

        summaryController = new SummaryController(this);
        classesController = new ClassesController(this);
        instancesController = new InstancesController(this);
        analysisController = new AnalysisController(this);

        navigationHistoryManager = new NavigationHistoryManager(this);
        oqlController = new OQLController(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public final int computeRetainedSizes(boolean masterAction, boolean interactive) {
        
        synchronized(this) {
           if (retainedSizesStatus == RETAINED_SIZES_UNSUPPORTED ||
               retainedSizesStatus == RETAINED_SIZES_COMPUTED)
               return retainedSizesStatus;
        }

        if (interactive && !ProfilerDialogs.displayConfirmationDNSA(
                Bundle.HeapFragmentWalker_ComputeRetainedMsg(), 
                Bundle.HeapFragmentWalker_ComputeRetainedCaption(),
                null, "HeapFragmentWalker.computeRetainedSizes", false)) { //NOI18N
            return changeState(RETAINED_SIZES_CANCELLED, masterAction);
        } else {
            changeState(RETAINED_SIZES_COMPUTING, masterAction);
            List<JavaClass> classes = heapFragment.getAllClasses();
            if (classes.size() > 0) {
                ProgressHandle pd = interactive ? ProgressHandle.createHandle(Bundle.HeapFragmentWalker_ComputingRetainedMsg()) : null;
                if (pd != null) {
                    pd.start();
                }
                classes.get(0).getRetainedSizeByClass();
                if (pd != null) pd.finish();
            }
            
            return changeState(RETAINED_SIZES_COMPUTED, masterAction);
        }
    }

    public final synchronized int getRetainedSizesStatus() {
        return retainedSizesStatus;
    }

    public final void addStateListener(StateListener listener) {
        if (stateListeners == null) stateListeners = new ArrayList<StateListener>();
        if (!stateListeners.contains(listener)) stateListeners.add(listener);
    }

    public final void removeStateListener(StateListener listener) {
        if (stateListeners == null || !stateListeners.contains(listener)) return;
        stateListeners.remove(listener);
        if (stateListeners.size() == 0) stateListeners = null;
    }

    private synchronized int changeState(final int newState, final boolean masterChange) {
        retainedSizesStatus = newState;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (stateListeners == null) return;
                StateEvent e = new StateEvent(newState, masterChange);
                for (StateListener listener : stateListeners)
                    listener.stateChanged(e);
            }
        });
        return retainedSizesStatus;
    }


    public AbstractTopLevelController getActiveController() {
        HeapFragmentWalkerUI ui = (HeapFragmentWalkerUI) getPanel();

        if (ui == null) {
            return null; // Debugger overrides getPanel() and always returns null
        }

        if (ui.isSummaryViewActive()) {
            return summaryController;
        } else if (ui.isClassesViewActive()) {
            return classesController;
        } else if (ui.isInstancesViewActive()) {
            return instancesController;
        } else if (ui.isAnalysisViewActive()) {
            return analysisController;
        } else if (ui.isOQLViewActive()) {
            return oqlController;
        }

        return null;
    }

    public OQLController getOQLController() {
        return oqlController;
    }

    public AnalysisController getAnalysisController() {
        return analysisController;
    }

    public ClassesController getClassesController() {
        return classesController;
    }

    // --- Public interface ------------------------------------------------------
    public File getHeapDumpFile() {
        return heapWalker.getHeapDumpFile();
    }

    public Lookup.Provider getHeapDumpProject() {
        return heapWalker.getHeapDumpProject();
    }

    public Heap getHeapFragment() {
        return heapFragment;
    }
    
    public int getHeapSegment() {
        return heapSegment;
    }

    public InstancesController getInstancesController() {
        return instancesController;
    }

    public boolean isNavigationBackAvailable() {
        return navigationHistoryManager.isNavigationBackAvailable();
    }

    public boolean isNavigationForwardAvailable() {
        return navigationHistoryManager.isNavigationForwardAvailable();
    }

    public JPanel getPanel() {
        if (walkerUI == null) {
            walkerUI = new HeapFragmentWalkerUI(this);
        }

        return walkerUI;
    }

    public SummaryController getSummaryController() {
        return summaryController;
    }

    public long getTotalLiveBytes() {
        return heapFragment.getSummary().getTotalLiveBytes();
    }

    public long getTotalLiveInstances() {
        return heapFragment.getSummary().getTotalLiveInstances();
    }

    // --- Navigation history support
    public void createNavigationHistoryPoint() {
        HeapFragmentWalkerUI ui = (HeapFragmentWalkerUI) getPanel();

        if (ui == null) {
            return; // Debugger overrides getPanel() and always returns null
        }

        navigationHistoryManager.createNavigationHistoryPoint();
        ui.updateNavigationActions();
    }

    public void navigateBack() {
        HeapFragmentWalkerUI ui = (HeapFragmentWalkerUI) getPanel();

        if (ui == null) {
            return; // Debugger overrides getPanel() and always returns null
        }

        navigationHistoryManager.navigateBack();
        ui.updateNavigationActions();
    }

    public void navigateForward() {
        HeapFragmentWalkerUI ui = (HeapFragmentWalkerUI) getPanel();

        if (ui == null) {
            return; // Debugger overrides getPanel() and always returns null
        }

        navigationHistoryManager.navigateForward();
        ui.updateNavigationActions();
    }

    public void showInstancesForClass(JavaClass jClass) {
        switchToInstancesView();
        instancesController.setClass(jClass);
    }

    public void switchToOQLView() {
        ((HeapFragmentWalkerUI) getPanel()).showOQLView();
    }

    public void switchToAnalysisView() {
        ((HeapFragmentWalkerUI) getPanel()).showAnalysisView();
    }

    public void switchToClassesView() {
        ((HeapFragmentWalkerUI) getPanel()).showClassesView();
    }

    public void switchToHistoryOQLView() {
        ((HeapFragmentWalkerUI) getPanel()).showHistoryOQLView();
    }

    public void switchToHistoryAnalysisView() {
        ((HeapFragmentWalkerUI) getPanel()).showHistoryAnalysisView();
    }

    public void switchToHistoryClassesView() {
        ((HeapFragmentWalkerUI) getPanel()).showHistoryClassesView();
    }

    public void switchToHistoryInstancesView() {
        ((HeapFragmentWalkerUI) getPanel()).showHistoryInstancesView();
    }

    public void switchToHistorySummaryView() {
        ((HeapFragmentWalkerUI) getPanel()).showHistorySummaryView();
    }

    public void switchToInstancesView() {
        ((HeapFragmentWalkerUI) getPanel()).showInstancesView();
    }

    public void switchToSummaryView() {
        ((HeapFragmentWalkerUI) getPanel()).showSummaryView();
    }

    // ---

    // --- Internal interface ----------------------------------------------------
    NavigationHistoryManager.NavigationHistoryCapable getNavigationHistorySource() {
        AbstractTopLevelController activeController = getActiveController();

        if (activeController instanceof NavigationHistoryManager.NavigationHistoryCapable) {
            return (NavigationHistoryManager.NavigationHistoryCapable) activeController;
        }

        return null;
    }

    private Integer classLoaderCount;
    public final int countClassLoaders() {
        if (this.classLoaderCount != null) {
            return this.classLoaderCount;
        }
        int nclassloaders = 0;
        JavaClass cl = heapFragment.getJavaClassByName("java.lang.ClassLoader"); // NOI18N
        if (cl != null) {
            nclassloaders = cl.getInstancesCount();

            Collection<JavaClass> jcs = cl.getSubClasses();

            for (JavaClass jc : jcs) {
                nclassloaders += jc.getInstancesCount();
            }
        }
        return this.classLoaderCount = nclassloaders;
    }


    public static interface StateListener {

        public void stateChanged(StateEvent e);

    }

    public static final class StateEvent {

        private int retainedSizesStatus;
        private boolean masterChange;


        StateEvent(int retainedSizesStatus) {
            this(retainedSizesStatus, false);
        }

        StateEvent(int retainedSizesStatus, boolean masterChange) {
            this.retainedSizesStatus = retainedSizesStatus;
            this.masterChange = masterChange;
        }

        public int getRetainedSizesStatus() { return retainedSizesStatus; }

        public boolean isMasterChange() { return masterChange; }

    }

}
