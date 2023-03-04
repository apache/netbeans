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
package org.netbeans.modules.bugtracking.tasks;

import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 *
 * @author jpeska
 */
public class DashboardToolbar extends JToolBar {

    public DashboardToolbar() {
        super();
        setFloatable(false);
        setRollover(true);
        setBorderPainted(false);
        setOpaque(false);
    }

    public void addButton(JButton button) {
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        button.setBorderPainted(false);
        button.setFocusable(false);
        button.setOpaque(false);
        add(button);
    }
}
