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
package org.netbeans.modules.nativeexecution.support;

import org.netbeans.modules.nativeexecution.api.util.Authentication;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ui.util.ValidateablePanel;
import org.netbeans.modules.nativeexecution.spi.ui.HostPropertiesPanelProvider;
import org.netbeans.modules.nativeexecution.ui.AuthenticationSettingsPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation of HostsPropertiesPanelProvider that returns a panel
 * with authentication settings.
 *
 * @author akrasny
 */
@ServiceProvider(service = HostPropertiesPanelProvider.class,   position = 100)
public class HostPropertiesPanelProviderImpl implements HostPropertiesPanelProvider {

    @Override
    public ValidateablePanel getHostPropertyPanel(ExecutionEnvironment env) {
        Authentication auth = Authentication.getFor(env);
        AuthenticationSettingsPanel panel = new AuthenticationSettingsPanel(auth, env != null);
        return panel;
    }
}
