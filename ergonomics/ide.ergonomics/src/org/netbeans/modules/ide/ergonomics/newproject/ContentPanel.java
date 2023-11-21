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

package org.netbeans.modules.ide.ergonomics.newproject;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

final class ContentPanel extends JPanel {

    public ContentPanel(String name) {
        this.setName(name);
        this.setLayout(new BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
    }

    public void setComponent(JComponent comp) {
        if (SwingUtilities.isEventDispatchThread()) {
            replaceComponent(comp);
        } else {
            SwingUtilities.invokeLater(() -> {
                replaceComponent(comp);
            });
        }
    }

    private void replaceComponent(JComponent comp) {
        removeAll();
        if (comp != null) {
            add(comp);
        }
        revalidate();
        repaint();
    }
    
}

