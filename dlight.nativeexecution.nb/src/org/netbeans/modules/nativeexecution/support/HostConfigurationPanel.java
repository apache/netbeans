/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ui.util.ValidatablePanelListener;
import org.netbeans.modules.nativeexecution.api.ui.util.ValidateablePanel;
import org.netbeans.modules.nativeexecution.spi.ui.HostPropertiesPanelProvider;
import org.openide.util.Lookup;

/**
 * A Composite panel that contains all panels provided by
 * {@link HostPropertiesPanelProvider}s.
 *
 * <p>The panel has
 * <code>GridBagLayout</code> layout and all panels are placed one under another
 * (in the order, of providers registration)</p>
 *
 * @author akrasny
 */
public final class HostConfigurationPanel extends ValidateablePanel {

    private final ValidatablePanelListener listener;
    private final List<ValidateablePanel> panels;

    public HostConfigurationPanel(final ExecutionEnvironment env) {
        panels = new ArrayList<>();

        GridBagConstraints gridBagConstraints;
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        listener = new ValidatablePanelListener() {
            @Override
            public void stateChanged(ValidateablePanel src) {
                fireChange();
            }
        };

        addPropertyChangeListener("ExecutionEnvironment", new PropertyChangeListener() { // NOI18N
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                for (ValidateablePanel panel : panels) {
                    panel.putClientProperty(evt.getPropertyName(), evt.getNewValue());
                }
            }
        });

        Collection<? extends HostPropertiesPanelProvider> allProviders =
                Lookup.getDefault().lookupAll(HostPropertiesPanelProvider.class);

        int y = 0;

        for (HostPropertiesPanelProvider provider : allProviders) {
            ValidateablePanel panel = provider.getHostPropertyPanel(env);
            panels.add(panel);
            panel.addValidationListener(listener);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = y++;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            add(panel, gridBagConstraints);
        }

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(new JPanel(), gridBagConstraints);
    }

    @Override
    public boolean hasProblem() {
        for (ValidateablePanel panel : panels) {
            if (panel.hasProblem()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getProblem() {
        for (ValidateablePanel panel : panels) {
            String problem = panel.getProblem();
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }

    @Override
    public void applyChanges(Object customData) {
        for (ValidateablePanel panel : panels) {
            panel.applyChanges(customData);
        }
    }
}
