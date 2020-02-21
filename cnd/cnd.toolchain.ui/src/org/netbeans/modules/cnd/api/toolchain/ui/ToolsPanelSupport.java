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
package org.netbeans.modules.cnd.api.toolchain.ui;

import org.netbeans.modules.cnd.toolchain.ui.options.IsChangedListener;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.toolchain.support.ToolchainChangeSupport;
import org.netbeans.modules.cnd.toolchain.ui.options.AddCompilerSetPanel;
import org.netbeans.modules.cnd.toolchain.ui.options.HostToolsPanelModel;
import org.netbeans.modules.cnd.toolchain.ui.options.ToolsPanel;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.windows.WindowManager;

/**
 *
 */
public class ToolsPanelSupport extends ToolchainChangeSupport {
    private static CompilerSet currentCompilerSet;
    private static final ToolsCacheManager cacheManager = (ToolsCacheManager) ToolsCacheManager.createInstance();
    // component.getClientProperty(OK_LISTENER_KEY) can have vetoable listener (VetoableChangeListener)
    public final static String OK_LISTENER_KEY = "okVetoableListener"; // NOI18N
    // component.getClientProperty(OK_LISTENER_KEY) can have selected toolchain name (String)
    public final static String SELECTED_TOOLCHAIN_KEY = "selectedToolchain"; // NOI18N
    
    private static final RequestProcessor RP = new RequestProcessor("ToolsPanelSupport", 1); // NOI18N
    
    public static ToolsCacheManager getToolsCacheManager() {
        return cacheManager;
    }

    private static final Set<ChangeListener> listenerChanged = new WeakSet<ChangeListener>();

    public static void addCompilerSetChangeListener(ChangeListener l) {
        synchronized (listenerChanged) {
            listenerChanged.add(l);
        }
    }

    public static void removeCompilerSetChangeListener(ChangeListener l) {
        synchronized (listenerChanged) {
            listenerChanged.remove(l);
        }
    }

    public static void fireCompilerSetChange(CompilerSet  set) {
        ChangeEvent ev = new ChangeEvent(set);
        currentCompilerSet = set;
        synchronized (listenerChanged) {
            for (ChangeListener l : listenerChanged) {
                l.stateChanged(ev);
            }
        }
    }

    public static void fireCompilerSetModified(CompilerSet set) {
        ChangeEvent ev = new ChangeEvent(set);
        synchronized (listenerModified) {
            for (ChangeListener l : listenerModified) {
                l.stateChanged(ev);
            }
        }
    }

    public static CompilerSet getCurrentCompilerSet() {
        return currentCompilerSet;
    }

    private static final Set<IsChangedListener> listenerIsChanged = new WeakSet<IsChangedListener>();

    public static void addIsChangedListener(IsChangedListener l) {
        synchronized (listenerIsChanged) {
            for (IsChangedListener old : listenerIsChanged) {
                if (old.getClass().equals(l.getClass())) {
                    listenerIsChanged.remove(old);
                    break;
                }
            }
            listenerIsChanged.add(l);
        }
    }

    public static void removeIsChangedListener(IsChangedListener l) {
        synchronized (listenerIsChanged) {
            listenerIsChanged.remove(l);
        }
    }

    public static boolean isChangedInOtherPanels() {
        boolean isChanged = false;
        synchronized (listenerIsChanged) {
            for (IsChangedListener l : listenerIsChanged) {
                if (l.isChanged()) {
                    isChanged = true;
                    break;
                }
            }
        }
        return isChanged;
    }

    public static List<Runnable> saveChangesInOtherPanels() {
        List<Runnable> res = new ArrayList<Runnable>();
        synchronized (listenerIsChanged) {
            for (IsChangedListener l : listenerIsChanged) {
                Runnable saveChanges = l.saveChanges();
                if (saveChanges != null) {
                    res.add(saveChanges);
                }
            }
        }
        return res;
    }

   
    /**
     * returns toolchain manager component to be embedded in other containers
     * @param env execution environment for which manager is created
     * @param selectedCompilerSetName the name of the compiler set to select (null is allowed)
     * @return toolchain manager component for specified execution environment
     *  reference to listener to be used by containers to notify about OK is in component
     *  property OK_LISTENER_KEY (VetoableChangeListener)
     *  client can find selected toolchain after OK in property
     *  SELECTED_TOOLCHAIN_KEY (String name of toolchain)
     */
    public static JComponent getToolsPanelComponent(ExecutionEnvironment env, String selectedCompilerSetName) {
        HostToolsPanelModel model = new HostToolsPanelModel(env);
        if (selectedCompilerSetName != null && selectedCompilerSetName.length() > 0) {
            model.setSelectedCompilerSetName(selectedCompilerSetName);
        }
        final ToolsPanel tp = new ToolsPanel(model, "ConfiguringBuildTools"); // NOI18N
        tp.update();
        VetoableChangeListener okL = new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                tp.applyChanges();
                tp.putClientProperty(SELECTED_TOOLCHAIN_KEY, tp.getSelectedToolchain());
            }
        };
        tp.putClientProperty(OK_LISTENER_KEY, okL); // NOI18N
        return tp;
    }

    /**
     * Invokes new toolchain creation wizard and saves created toolchain
     * in {@link ExecutionEnvironment}'s {@link CompilerSetManager}.
     *
     * Note that saving happens asynchronously, i.e. the new toolchain
     * might not be saved yet when this method returns.
     *
     * This method must be called from EDT.
     *
     * @param env execution environment
     * @param predefinedPath if is not null, IDE will search tool collection in this path
     * @return created toolchain, or <code>null</code> if the wizard was canceled
     */
    public static Future<CompilerSet> invokeNewCompilerSetWizard(final ExecutionEnvironment env, String predefinedPath) {
        final CompilerSetManager csm =  cacheManager.getCompilerSetManagerCopy(env, true);
        final CompilerSet cs = AddCompilerSetPanel.invokeMe(csm, predefinedPath);
        if (cs != null) {
            return RP.submit(
                    new CompilerSetAction(csm, cs, CompilerSetActionType.ADD), cs);
        }
        return null;
    }

    /**
     * Changes default toolchain for an {@link ExecutionEnvironment}.
     *
     * Note that saving happens asynchronously, i.e. the new default toolchain
     * might not be saved yet when this method returns.
     *
     * This method can be called from EDT or other thread.
     *
     * @param env  execution environment
     * @param csName  new default toolchain name
     * @return task to wait for completion
     */
    public static RequestProcessor.Task setDefaultCompilerSet(final ExecutionEnvironment env, final String csName) {
        final CompilerSetManager csm =  cacheManager.getCompilerSetManagerCopy(env, true);
        return RP.post(
                new CompilerSetAction(csm, csm.getCompilerSet(csName), CompilerSetActionType.SET_DEFAULT));
    }

    /**
     * Removes toolchain from an {@link ExecutionEnvironment}.
     *
     * Note that removal happens asynchronously, i.e. the toolchain
     * might not be removed yet when this method returns.
     *
     * This method can be called from EDT or other thread.
     *
     * @param env  execution environment
     * @param csName  name of toolchain to remove
     * @return task to wait for completion. Can be NULL!
     */
    public static RequestProcessor.Task removeCompilerSet(final ExecutionEnvironment env, final String csName) {
        final CompilerSetManager csm =  cacheManager.getCompilerSetManagerCopy(env, true);
        if (csm != null) {
            final CompilerSet cs = csm.getCompilerSet(csName);
            if (cs != null) {
                return RP.post(
                        new CompilerSetAction(csm, cs, CompilerSetActionType.REMOVE));
            }
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ToolsPanelSupport.class, "ErrorCompilerSetNotFound"));
        return null;
    }

    /**
     * Initiates toolchain discovery which restores the default
     * toolchain list for the given {@link ExecutionEnvironment}.
     *
     * Note that saving happens asynchronously, i.e. the new toolchain list
     * might not be saved yet when this method returns.
     *
     * This method must be called from EDT.
     *
     * @param env  execution environment
     * @return task to wait for completion
     */
    public static void restoreCompilerSets(final ExecutionEnvironment env) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                CompilerSetManager oldCsm = CompilerSetManager.get(env);
                cacheManager.restoreCompilerSets(oldCsm);
                cacheManager.applyChanges(null);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            ModalMessageDlg.runLongTask(
                    WindowManager.getDefault().getMainWindow(),
                    runnable, null, null,
                    NbBundle.getMessage(ToolsPanelSupport.class, "RestoringToolchainsTitle"), // NOI18N
                    NbBundle.getMessage(ToolsPanelSupport.class, "RestoringToolchainsMessage")); // NOI18N
        } else {
            runnable.run();
        }
    }

    private static enum CompilerSetActionType {
        NONE,
        ADD,
        REMOVE,
        SET_DEFAULT
    }

    private static final class CompilerSetAction implements Runnable {

        private final CompilerSetManager compilerSetManager;
        private final CompilerSet compilerSet;
        private final CompilerSetActionType action;

        public CompilerSetAction(CompilerSetManager compilerSetManager, CompilerSet compilerSet, CompilerSetActionType action) {
            CndUtils.assertNotNull(compilerSetManager, "null compilerSetManager"); //NOI18N
            CndUtils.assertNotNull(compilerSet, "null compilerSet"); //NOI18N
            this.compilerSetManager = compilerSetManager;
            this.compilerSet = compilerSet;
            this.action = action;
        }

        @Override
        public void run() {
            if (compilerSet == null || compilerSetManager == null) {
                return;
            }
            switch (action) {
                case ADD:
                    compilerSetManager.add(compilerSet);
                    break;
                case REMOVE:
                    compilerSetManager.remove(compilerSetManager.getCompilerSet(compilerSet.getName()));
                    break;
                case SET_DEFAULT:
                    compilerSetManager.setDefault(compilerSetManager.getCompilerSet(compilerSet.getName()));
                    break;
            }
            cacheManager.applyChanges(null);
        }
    }

    private ToolsPanelSupport() {
    }
}
