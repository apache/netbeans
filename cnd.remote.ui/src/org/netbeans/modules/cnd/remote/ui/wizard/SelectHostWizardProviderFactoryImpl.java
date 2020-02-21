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

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.ui.SelectHostWizardProvider;
import org.netbeans.modules.cnd.spi.remote.ui.SelectHostWizardProviderFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=SelectHostWizardProviderFactory.class)
public class SelectHostWizardProviderFactoryImpl implements SelectHostWizardProviderFactory {

    public SelectHostWizardProviderFactoryImpl() {
    }

    @Override
    public SelectHostWizardProvider createHostWizardProvider(boolean allowLocal, boolean allowToCreateNewHostDirectly, ChangeListener changeListener) {
        return new SelectHostWizardProviderImpl(allowLocal, allowToCreateNewHostDirectly, changeListener);
    }


    private static class SelectHostWizardProviderImpl extends SelectHostWizardProvider {

        private final SelectHostWizardPanel panel;

        public SelectHostWizardProviderImpl(boolean allowLocal, boolean allowToCreateNewHostDirectly, ChangeListener changeListener) {
            panel = new SelectHostWizardPanel(allowLocal, allowToCreateNewHostDirectly, changeListener);
        }

        @Override
        public WizardDescriptor.Panel<WizardDescriptor> getSelectHostPanel() {
            return panel;
        }

        @Override
        public List<WizardDescriptor.Panel<WizardDescriptor>> getAdditionalPanels() {
            return panel.getAdditionalPanels();
        }

        @Override
        public boolean isNewHost() {
            return panel.isNewHost();
        }

        @Override
        public ExecutionEnvironment getSelectedHost() {
            return panel.getSelectedHost();
        }

        @Override
        public void apply() {
            panel.apply();
        }
    }
}
