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


/**
 * Defines an API for registering custom Java platform installer. The installer
 * is responsible for instantiation of {@link JavaPlatform} through the provided
 * TemplateWizard.Iterator. If your installer selects the platform on the local disk you
 * probably don't want to use this class, the {@link PlatformInstall} class
 * creates an platform chooser for you. You want to use this class if the
 * platform is not on the local disk, eg. you want to download it from the web.
 * 
 * Consult the {@link GeneralPlatformInstall} javadoc about the {@link CustomPlatformInstall} registration.
 * 
 * @author Tomas Zezula
 * @since 1.5
 */
public abstract class CustomPlatformInstall extends GeneralPlatformInstall {
    
    /**
     * Returns the {@link WizardDescriptor#InstantiatingIterator} used to install
     * the platform. The platform definition file returned by the instantiate method
     * should be created in the Services/Platforms/org-netbeans-api-java-Platform
     * folder on the system filesystem.
     * @return TemplateWizard.Iterator instance responsible for instantiating
     * the platform. The instantiate method of the returned iterator should
     * return the Set containing the platform.
     */
    public abstract WizardDescriptor.InstantiatingIterator<WizardDescriptor> createIterator();
    
}
