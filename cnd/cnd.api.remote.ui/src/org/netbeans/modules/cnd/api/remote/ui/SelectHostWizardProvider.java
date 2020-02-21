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

package org.netbeans.modules.cnd.api.remote.ui;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.spi.remote.ui.SelectHostWizardProviderFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

/**
 * Creates a wizard panel that allows to
 * either select an existent host
 * or setup a new one.
 *
 * In the case user decided to set up a new host,
 * it returns a set of additional panels and notifies PropertyChangeListener
 * 
 */
public abstract class SelectHostWizardProvider {

    public static SelectHostWizardProvider createInstance(boolean allowLocal, boolean allowToCreateNewHostDirectly, ChangeListener changeListener) {
        SelectHostWizardProviderFactory factory = Lookup.getDefault().lookup(SelectHostWizardProviderFactory.class);
        return (factory == null) ? null : factory.createHostWizardProvider(allowLocal, allowToCreateNewHostDirectly, changeListener);
    }

    /**
     * Gets first panel - the one that allows to choose an existent host
     * or alternatively to choose an option of creating a new one.
     *
     * @param allowLocal determines whether to allow choosing local host
     * @param propertyChangeListener listener to be notified if user choice changed
     * @return
     */
    public abstract WizardDescriptor.Panel<WizardDescriptor> getSelectHostPanel();

    /**
     * Gets additional panels
     * (in the case user decided to add a new host)
     * @return
     */
    public abstract List<WizardDescriptor.Panel<WizardDescriptor>> getAdditionalPanels();

    public abstract boolean isNewHost();
    public abstract ExecutionEnvironment getSelectedHost();
    public abstract void apply();
}
