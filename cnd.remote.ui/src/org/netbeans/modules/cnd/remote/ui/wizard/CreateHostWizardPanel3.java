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
package org.netbeans.modules.cnd.remote.ui.wizard;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/*package*/ final class CreateHostWizardPanel3 implements WizardDescriptor.Panel<WizardDescriptor> {

    private CreateHostVisualPanel3 component;
    private final CreateHostData data;

    public CreateHostWizardPanel3(CreateHostData data) {
        this.data = data;
    }

    @Override
    public CreateHostVisualPanel3 getComponent() {
        if (component == null) {
            component = new CreateHostVisualPanel3(data);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewRemoteDevelopmentHostWizardP3");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        getComponent().init();
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        // "hostUID" is needed in the case this page works within another wizard
        // it isn't surprizing that WizardConstants.PROPERTY_HOST_UID from makeproject is used - the panel is common for setting up a host and creating a project
        WizardConstants.PROPERTY_HOST_UID.put(settings, ExecutionEnvironmentFactory.toUniqueID(data.getExecutionEnvironment()));
        data.setDisplayName(getComponent().getHostDisplayName());
        data.setSyncFactory(getComponent().getRemoteSyncFactory());
    }
}

