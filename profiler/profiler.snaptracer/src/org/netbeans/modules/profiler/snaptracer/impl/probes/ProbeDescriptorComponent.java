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

package org.netbeans.modules.profiler.snaptracer.impl.probes;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.profiler.snaptracer.TracerPackage;
import org.netbeans.modules.profiler.snaptracer.TracerProbeDescriptor;

/**
 *
 * @author Jiri Sedlacek
 */
public final class ProbeDescriptorComponent extends JPanel {

    public ProbeDescriptorComponent(final TracerProbeDescriptor descriptor,
                                    final TracerPackage p,
                                    final SelectionHandler handler) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        JCheckBox checkBox = new JCheckBox() {
            protected void fireActionPerformed(ActionEvent event) {
                if (isSelected()) handler.descriptorSelected(p, descriptor);
                else handler.descriptorUnselected(p, descriptor);
            }
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled && descriptor.isProbeAvailable());
            }
        };
        checkBox.setOpaque(false);
        checkBox.setEnabled(descriptor.isProbeAvailable());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3, 15, 3, 5);
        add(checkBox, c);

        JLabel icon = new JLabel() {
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled && descriptor.isProbeAvailable());
            }
        };
        Icon ic = descriptor.getProbeIcon();
        icon.setIcon(ic);
        icon.setEnabled(descriptor.isProbeAvailable());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(3, 5, 3, 5);
        add(icon, c);

        JLabel name = new JLabel(descriptor.getProbeName()) {
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled && descriptor.isProbeAvailable());
            }
        };
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setEnabled(descriptor.isProbeAvailable());
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 10, 0, 5);
        add(name, c);

        JLabel descr = new JLabel(descriptor.getProbeDescription()) {
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled && descriptor.isProbeAvailable());
            }
        };
        descr.setFont(descr.getFont().deriveFont(Font.PLAIN));
        descr.setEnabled(descriptor.isProbeAvailable());
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 10, 3, 5);
        add(descr, c);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Component[] components = getComponents();
        for (Component c : components) c.setEnabled(enabled);
    }


    public static interface SelectionHandler {

        public void descriptorSelected(TracerPackage p, TracerProbeDescriptor d);

        public void descriptorUnselected(TracerPackage p, TracerProbeDescriptor d);

    }

}
