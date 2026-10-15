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
package org.netbeans.modules.java.disco;

import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.openide.WizardDescriptor;

public class SelectPackageWizardPanel extends AbstractWizardPanel<SelectPackagePanel> {

    private final WizardState state;

    SelectPackageWizardPanel(WizardState state) {
        this.state = state;
    }

    @Override
    @UIEffect
    public SelectPackagePanel createComponent() {
        SelectPackagePanel component = SelectPackagePanel.create();
        component.addPropertyChangeListener(SelectPackagePanel.PROP_VALIDITY_CHANGED, (e) -> fireChangeListeners());
        return component;
    }

    @Override
    public boolean isValid() {
        return getComponent().getSelectedPackage() != null;
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (!isValid())
            return;

        PkgSelection bi = getComponent().getSelectedPackage();
        if (bi == null)
            throw new IllegalStateException("Null package"); //but really, if isValid is true this should not happen
        state.selection = bi;
    }

}
