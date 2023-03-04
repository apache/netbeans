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

package org.netbeans.modules.search.ui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Panel for displaying a list of issues encountered either before or during
 * replacing.
 *
 * @see  ResultView
 * @author  Marian Petras
 */
final class IssuesPanel extends JPanel {
    
    /**
     */
    IssuesPanel(String title, String[] issues) {
        super(new BorderLayout());
        add(new JLabel(title), BorderLayout.NORTH);
        add(new JScrollPane(issues != null ? new JList<>(issues) : new JList<String>()), // defensive fix against #120879
                BorderLayout.CENTER);
    }
    
}
