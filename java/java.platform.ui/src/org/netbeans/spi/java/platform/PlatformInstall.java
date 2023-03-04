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

package org.netbeans.spi.java.platform;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Defines an API for registering custom Java platform installer. The Installer
 * is responsible for recognizing the platform, through its {@link #accept} method,
 * and for instantiation itself, through the provided wizard iterator.
 * Consult the {@link GeneralPlatformInstall} javadoc about the {@link PlatformInstall} registration.
 *
 * @author Svata Dedic, Tomas Zezula
 */
public abstract class PlatformInstall extends GeneralPlatformInstall {
    /**
     * Creates a {@link WizardDescriptor.InstantiatingIterator} for an accepted
     * folder. The platform definition file returned by the instantiate method
     * should be created in the Services/Platforms/org-netbeans-api-java-Platform
     * folder on the system filesystem.
     * @return TemplateWizard.Iterator instance responsible for instantiating
     * the platform. The instantiate method of the returned iterator should
     * return the Set containing the platform.
     */
    public abstract WizardDescriptor.InstantiatingIterator<WizardDescriptor> createIterator(FileObject baseFolder);

    /**
     * Checks whether a given folder contains a platform of the supported type.
     * The check done by this method should be quick and should not involve launching the virtual machine,
     * the expensive check, if needed, should be done in the wizard panel.
     * @param baseFolder folder which may be an installation root of a platform
     * @return true if the folder is recognized
     */
    public abstract boolean accept(FileObject baseFolder);    

}
