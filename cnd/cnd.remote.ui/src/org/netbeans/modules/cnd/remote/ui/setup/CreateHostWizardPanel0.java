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
package org.netbeans.modules.cnd.remote.ui.setup;

import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.spi.remote.setup.HostSetupProvider;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.spi.remote.setup.HostSetupWorker;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/*package*/ final class CreateHostWizardPanel0 implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private CreateHostVisualPanel0 component;
    private final List<HostSetupProvider> providers;
    private final ChangeListener changeListener;
    private HostSetupProvider lastSelectedProvider;

    private HostSetupWorker selectedWorker;
    private final ToolsCacheManager cacheManager;

    public CreateHostWizardPanel0(ChangeListener changeListener, 
            List<HostSetupProvider> providers, ToolsCacheManager cacheManager) {
        this.providers = providers;
        this.changeListener = changeListener;
        this.cacheManager = cacheManager;
        this.lastSelectedProvider = providers.get(0);
        this.selectedWorker = lastSelectedProvider.createHostSetupWorker(cacheManager);
    }

    public HostSetupWorker getSelectedWorker() {
        return selectedWorker;
    }

    @Override
    public CreateHostVisualPanel0 getComponent() {
        if (component == null) {
            component = new CreateHostVisualPanel0(this, providers);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return getComponent().getSelectedProvider() != null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // change support
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
        applyChanges();
    }

    ////////////////////////////////////////////////////////////////////////////
    // settings
    @Override
    public void readSettings(WizardDescriptor settings) {
        getComponent().reset();
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        applyChanges();
    }

    public void applyChanges() {
        HostSetupProvider provider = getComponent().getSelectedProvider();
        assert provider != null;

        if (!provider.equals(lastSelectedProvider)) {
            lastSelectedProvider = provider;
            selectedWorker = provider.createHostSetupWorker(cacheManager);
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }
}
