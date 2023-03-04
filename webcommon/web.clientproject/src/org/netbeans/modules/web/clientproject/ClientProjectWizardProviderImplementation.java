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
package org.netbeans.modules.web.clientproject;

import org.netbeans.modules.web.clientproject.spi.ClientProjectWizardProviderImpl;
import org.netbeans.modules.web.clientproject.ui.wizard.ClientSideProjectWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=ClientProjectWizardProviderImpl.class)
public class ClientProjectWizardProviderImplementation implements ClientProjectWizardProviderImpl{

    @Override
    public WizardDescriptor.InstantiatingIterator<WizardDescriptor> newClientProjectWithExtender() {
        return ClientSideProjectWizardIterator.newProjectWithExtender();
    }
    
    @Override
    public WizardDescriptor.InstantiatingIterator<WizardDescriptor> existingHtml5Project() {
        return ClientSideProjectWizardIterator.existingHtml5Project();
    }
    
}
