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

package org.netbeans.modules.ide.ergonomics.fod;

import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jirka Rechtacek
 */
public class ModulesActivator {
    
    private Collection<UpdateElement> modules4enable;
    private RequestProcessor.Task enableTask = null;
    private OperationContainer<OperationSupport> enableContainer;
    private ProgressHandle enableHandle;
    private final FindComponentModules finder;
    private final ProgressMonitor progressMonitor;

    public ModulesActivator (Collection<UpdateElement> modules, FindComponentModules f) {
        this(modules, f, null);
    }

    public ModulesActivator (Collection<UpdateElement> modules, FindComponentModules f, ProgressMonitor progressMonitor) {
        if (modules == null || modules.isEmpty ()) {
            throw new IllegalArgumentException ("Cannot construct ModulesActivator with null or empty Collection " + modules);
        }
        modules4enable = modules;
        this.finder = f;
        if (progressMonitor != null) {
            this.progressMonitor = progressMonitor;
        } else {
            this.progressMonitor = ProgressMonitor.DEV_NULL_PROGRESS_MONITOR;
        }
    }

    public void assignEnableHandle (ProgressHandle handle) {
        this.enableHandle = handle;
    }
    
    public RequestProcessor.Task getEnableTask () {
        if (enableTask == null) {
            enableTask = createEnableTask ();
        }
        return enableTask;
    }
    
    private RequestProcessor.Task createEnableTask () {
        assert enableTask == null || enableTask.isFinished () : "The Enable Task cannot be started nor scheduled.";
        enableTask = FeatureManager.getInstance().create (new InstallOrActivateTask(this));
        return enableTask;
    }

    final void enableModules () {
        try {
            doEnableModules ();
        } catch (OperationException ex) {
            progressMonitor.onError(ex.getLocalizedMessage());
            Logger.getLogger(ModulesActivator.class.getName()).warning(ex.getMessage());
        } finally {
            FoDLayersProvider.getInstance().refreshForce();
        }
    }
    
    private void doEnableModules () throws OperationException {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot be called in EQ.";
        enableContainer = null;
        for (UpdateElement module : modules4enable) {
            if (enableContainer == null) {
                enableContainer = OperationContainer.createForEnable ();
            }
            if (enableContainer.canBeAdded (module.getUpdateUnit (), module)) {
                enableContainer.add (module);
            }
        }
        if (enableContainer.listAll ().isEmpty ()) {
            return ;
        }
        assert enableContainer.listInvalid ().isEmpty () :
            "No invalid Update Elements " + enableContainer.listInvalid ();
        if (! enableContainer.listInvalid ().isEmpty ()) {
            throw new IllegalArgumentException ("Some are invalid for enable: " + enableContainer.listInvalid ());
        }
        OperationSupport enableSupport = enableContainer.getSupport ();
        if (enableHandle == null) {
            enableHandle = ProgressHandle.createHandle (
                    getBundle ("ModulesActivator_Enable",
                    ModulesInstaller.presentUpdateElements(finder.getVisibleUpdateElements (modules4enable))));
        }
        progressMonitor.onEnable(enableHandle);
        enableSupport.doOperation (enableHandle);
    }
    
    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (ModulesActivator.class, key, params);
    }
    
}
