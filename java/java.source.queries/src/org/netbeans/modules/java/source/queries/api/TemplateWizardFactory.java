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
package org.netbeans.modules.java.source.queries.api;

import org.netbeans.modules.java.source.queries.spi.TemplateWizardProvider;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

/**
 * Returns template wizard iterator. This factory class allows to have different wizard
 * iterators in NetBeans and JDev
 * @author jhorvath
 */
public class TemplateWizardFactory {
    
    private TemplateWizardFactory () {
    }
    
    /**
     * Returns an iterator for template wizard
     * @return an {@link WizardDescriptor.InstantiatingIterator} 
     */
    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> create() {
        return getProvider().createWizard();
    }
    
    /**
     * Returns an iterator for template wizard, with editable superclass
     * @return an {@link WizardDescriptor.InstantiatingIterator} 
     */
    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> createForSuperClass() {
        return getProvider().createWizardForSuperClass();
    }
    
    private static TemplateWizardProvider getProvider() {
        TemplateWizardProvider provider = Lookup.getDefault().lookup(TemplateWizardProvider.class);
        if (provider == null) {
            throw new IllegalStateException("No TemplateWizardProvider found in the Lookup");    //NOI18N
        }
        return provider;
    }
}
