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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import org.netbeans.modules.rust.cargo.api.CargoCLICommand;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.netbeans.modules.rust.cargo.impl.nodes.RustProjectDependenciesNode;
import org.netbeans.modules.rust.cargo.impl.nodes.RustProjectDependenciesNode.DependencyType;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.netbeans.modules.rust.cargo.api.CargoCLI;

/**
 *
 * @author antonio
 */
public class RustAddDependencyAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(RustAddDependencyAction.class.getName());

    private final CargoTOML cargotoml;
    private final RustProjectDependenciesNode.DependencyType dependencyType;

    private final static String getName(DependencyType type) {
        switch (type) {
            case DEPENDENCY:
                return NbBundle.getMessage(RustAddDependencyAction.class, "ADD_DEPENDENCY");
            case DEV_DEPENDENCY:
                return NbBundle.getMessage(RustAddDependencyAction.class, "ADD_DEV_DEPENDENCY");
            case BUILD_DEPENDENCY:
                return NbBundle.getMessage(RustAddDependencyAction.class, "ADD_BUILD_DEPENDENCY");
            default:
                return "???"; // NOI18N
        }
    }

    public RustAddDependencyAction(CargoTOML cargotoml, RustProjectDependenciesNode.DependencyType dependencyType) {
        super(getName(dependencyType));
        this.cargotoml = cargotoml;
        this.dependencyType = dependencyType;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new RustAddDependencyWizardPanel1(cargotoml, dependencyType));
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        switch (dependencyType) {
            case BUILD_DEPENDENCY:
                wiz.setTitle(NbBundle.getMessage(RustAddDependencyAction.class, "ADD_BUILD_DEPENDENCY"));
                break;
            case DEPENDENCY:
                wiz.setTitle(NbBundle.getMessage(RustAddDependencyAction.class, "ADD_DEPENDENCY"));
                break;
            case DEV_DEPENDENCY:
                wiz.setTitle(NbBundle.getMessage(RustAddDependencyAction.class, "ADD_DEV_DEPENDENCY"));
                break;
        }
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            List<RustPackage> packages = (List<RustPackage>) wiz.getProperty(RustAddDependencyWizardPanel1.PROP_SELECTED_PACKAGES);
            List<String> names = packages.stream().map((p) -> String.format("%s@%s", p.getName(), p.getVersion())).collect(Collectors.toList());
            switch(dependencyType) {
                case BUILD_DEPENDENCY:
                    names.add(0, "--build"); // NOI18N
                    break;
                case DEV_DEPENDENCY:
                    names.add(0, "--dev"); // NOI18N
                    break;
            }
            CargoCLI cargo = Lookup.getDefault().lookup(CargoCLI.class);
            try {
                cargo.cargo(cargotoml, new CargoCLICommand[]{CargoCLICommand.CARGO_ADD}, names.toArray(new String[0]));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
