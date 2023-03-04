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

package org.netbeans.modules.web.spi.webmodule;

import org.openide.WizardDescriptor;

/**
 * Extension to {@link org.openide.WizardDescriptor.Panel}. It allows to enable or disable
 * the framework configuration panel components as requested by the different
 * usages of the panel.
 *
 * @deprecated This class has been replaced with {@link org.netbeans.modules.web.spi.webmodule.WebModuleExtender}.
 */
@Deprecated
public interface FrameworkConfigurationPanel extends WizardDescriptor.Panel {

    /**
     * Enables or disables the panel components.
     *
     * @param enable if the components should be enabled or disabled
     */
    public void enableComponents(boolean enable);
}
