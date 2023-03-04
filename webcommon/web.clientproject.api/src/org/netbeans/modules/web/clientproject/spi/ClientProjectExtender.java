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
package org.netbeans.modules.web.clientproject.spi;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;

/**
 * Extender interface to extend behavior of new HTML5 Project Wizard.
 * Instances to be registered via @ServiceProvider.
 * @author Jan Becicka
 */
public interface ClientProjectExtender {

    /**
     * Initialize new HTML5 Project.
     * @param wizardDescriptor corresponding WizardDescriptor
     */
    public void initialize(WizardDescriptor wizardDescriptor);

    /**
     * Creates additional Wizard Descriptor Panels, which will be added before default HTML5 Project Wizard Panels.
     * @return
     */
    @NonNull
    public Panel<WizardDescriptor>[] createInitPanels();
    /**
     * Creates additional Wizard Descriptor Panels, which will be added after default HTML5 Projezt Wizard panels.
     * Wizard.
     * @return 
     */
    @NonNull
    public Panel<WizardDescriptor>[] createWizardPanels();

    /**
     * Creates additional changes in Client Side Project.
     * @param projectRoot
     * @param siteRoot
     * @param libsPath 
     */
    public void apply(FileObject projectRoot, FileObject siteRoot, String libsPath);
    
}
