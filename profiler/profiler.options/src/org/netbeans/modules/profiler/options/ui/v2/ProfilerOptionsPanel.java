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

package org.netbeans.modules.profiler.options.ui.v2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilerOptionsPanel extends JPanel {
    
    public abstract String getDisplayName();
    
    public abstract void storeTo(ProfilerIDESettings settings);
    
    public abstract void loadFrom(ProfilerIDESettings settings);
    
    public abstract boolean equalsTo(ProfilerIDESettings settings);
    
    
    public class Separator extends JPanel {
        public Separator(String caption) {
            setLayout(new GridBagLayout());

            GridBagConstraints constraints;

            // captionLabel
            JLabel captionLabel = new JLabel(caption, JLabel.LEADING);
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(0, 0, 0, 0);
            add(captionLabel, constraints);

            // captionSeparator
            JSeparator captionSeparator = new JSeparator();
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(0, 4, 0, 0);
            add(captionSeparator, constraints);
        }
    }
    
}
