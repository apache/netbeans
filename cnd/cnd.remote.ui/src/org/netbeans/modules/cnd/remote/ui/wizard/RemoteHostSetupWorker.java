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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.spi.remote.setup.HostValidator;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.spi.remote.ui.HostSetupWorkerUI;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 */
public class RemoteHostSetupWorker implements HostSetupWorkerUI {

    private final CreateHostData data;

    /*package*/ RemoteHostSetupWorker(ToolsCacheManager toolsCacheManager) {
        data = new CreateHostData(toolsCacheManager, false);
    }

    @Override
    public Result getResult() {
        return data;
    }

    @Override
    public List<Panel<WizardDescriptor>> getWizardPanels(HostValidator validator) {
        return callUncheckedNewForPanels();
    }

    @SuppressWarnings( "unchecked" )
    private List<WizardDescriptor.Panel<WizardDescriptor>> callUncheckedNewForPanels() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new CreateHostWizardPanel1(data));
        panels.add(new CreateHostWizardPanel2(data));
        panels.add(new CreateHostWizardPanel3(data));
        return panels;
    }
}
