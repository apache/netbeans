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
package org.netbeans.modules.rust.cargo.impl.nodes.actions.dependencies;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.netbeans.modules.rust.cargo.impl.nodes.RustProjectDependenciesNode.DependencyType;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

public class RustAddDependencyWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor> {

    public static final String PROP_PACKAGES = "packages"; // NOI18N
    public static final String PROP_SELECTED_PACKAGES = "selected-packages"; // NOI18N

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private RustAddDependencyVisualPanel1 component;

    private final CargoTOML cargotoml;
    private final DependencyType dependencyType;
    private final ChangeSupport changeSupport;

    public RustAddDependencyWizardPanel1(CargoTOML cargotoml, DependencyType dependencyType) {
        this.cargotoml = cargotoml;
        this.dependencyType = dependencyType;
        this.changeSupport = new ChangeSupport(this);
    }

    @Override
    public RustAddDependencyVisualPanel1 getComponent() {
        if (component == null) {
            component = new RustAddDependencyVisualPanel1(cargotoml, changeSupport, dependencyType);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return ! getComponent().getSelectedPackages().isEmpty();
   }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        List<RustPackage> packages = (List<RustPackage>) wiz.getProperty(PROP_PACKAGES);
        component.setPackages(packages);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(PROP_PACKAGES, component.getPackages());
        wiz.putProperty(PROP_SELECTED_PACKAGES, component.getSelectedPackages());
    }

}
