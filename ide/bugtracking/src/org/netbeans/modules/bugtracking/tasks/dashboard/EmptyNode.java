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
package org.netbeans.modules.bugtracking.tasks.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.ProgressLabel;
import org.netbeans.modules.team.commons.treelist.TreeLabel;

/**
 * Empty Node. E.g. No Projects Open, No Projects Bookmarked
 *
 * @author Jan Becicka
 */
public class EmptyNode extends LeafNode {

    private JPanel panel;
    private JLabel lbl;
    private ProgressLabel progress;
    private String title;
    private String p;
    private final Object LOCK = new Object();
    private int loadingCounter = 0;

    public EmptyNode(String name, String progress) {
        super(null);
        this.title = name;
        this.p = progress;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        if (null == panel) {
            panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(0, 0, 0, 0));
            panel.setOpaque(false);

            lbl = new TreeLabel(title);
            progress = createProgressLabel(p);

            panel.add(lbl, BorderLayout.WEST);
            panel.add(progress, BorderLayout.CENTER);
        }

        synchronized (LOCK) {
            progress.setVisible(loadingCounter > 0);
            lbl.setVisible(loadingCounter <= 0);
        }
        return panel;
    }

    void loadingStarted() {
        synchronized (LOCK) {
            loadingCounter++;
            fireContentChanged();
        }
    }

    void loadingFinished() {
        synchronized (LOCK) {
            loadingCounter--;
            if (loadingCounter < 0) {
                loadingCounter = 0;
            }
            if (loadingCounter == 0) {
                if (progress != null) {
                    progress.stop();
                }
            }
            fireContentChanged();
        }
    }
}
