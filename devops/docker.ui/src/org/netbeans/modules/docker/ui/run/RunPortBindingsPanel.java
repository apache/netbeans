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
package org.netbeans.modules.docker.ui.run;

import org.netbeans.modules.docker.api.PortMapping;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerImageDetail;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class RunPortBindingsPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final DockerImageDetail info;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private RunPortBindingsVisual component;

    private WizardDescriptor wizard;

    public RunPortBindingsPanel(DockerImageDetail info) {
        this.info = info;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public RunPortBindingsVisual getComponent() {
        if (component == null) {
            component = new RunPortBindingsVisual(info);
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @NbBundle.Messages({
        "MSG_MissingPort=The port to bind can't be empty.",
        "# {0} - conflicting port",
        "MSG_ConflictingPort=There is port conflict on the host ({0}).",
    })
    @Override
    public boolean isValid() {
        // clear the error message
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        List<PortMapping> mapping = component.getPortMapping();
        for (PortMapping m : mapping) {
            if (m.getPort() == null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_MissingPort());
                return false;
            }
        }

        Set<Integer> any = new HashSet<>();
        Set<Integer> all = new HashSet<>();
        Map<String, Set<Integer>> portMap = new HashMap<>();
        for (PortMapping m : mapping) {
            Integer port = m.getHostPort();
            if (port == null) {
                continue;
            }

            if (m.getHostAddress() == null) {
                if (!any.add(port)) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            Bundle.MSG_ConflictingPort(port.toString()));
                    return false;
                }
                if (all.contains(port)) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            Bundle.MSG_ConflictingPort(port.toString()));
                    return false;
                }
            } else {
                all.add(port);
                if (any.contains(port)) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            Bundle.MSG_ConflictingPort(port.toString()));
                    return false;
                }
                Set<Integer> ports = portMap.get(m.getHostAddress());
                if (ports == null) {
                    ports = new HashSet<>();
                    portMap.put(m.getHostAddress(), ports);
                }
                if (!ports.add(port)) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            Bundle.MSG_ConflictingPort(port.toString()));
                    return false;
                }
            }
        }
        return true;
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
        if (wizard == null) {
            wizard = wiz;
        }

        Boolean portRandom = (Boolean) wiz.getProperty(RunTagWizard.RANDOM_BIND_PROPERTY);
        component.setRandomBind(portRandom != null ? portRandom : RunTagWizard.RANDOM_BIND_DEFAULT);
        List<PortMapping> mapping = (List<PortMapping>) wiz.getProperty(RunTagWizard.PORT_MAPPING_PROPERTY);
        component.setPortMapping(mapping != null ? mapping : Collections.<PortMapping>emptyList());

        // XXX revalidate; is this bug?
        changeSupport.fireChange();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(RunTagWizard.RANDOM_BIND_PROPERTY, component.isRandomBind());
        wiz.putProperty(RunTagWizard.PORT_MAPPING_PROPERTY, component.getPortMapping());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
