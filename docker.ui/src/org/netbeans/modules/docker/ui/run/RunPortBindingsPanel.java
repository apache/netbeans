/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
