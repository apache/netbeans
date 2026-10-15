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
package org.netbeans.modules.markdown.ui.preview.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;

/**
 * 
 * hr tags don't have any customization from CSS using swing html renderer
 * 
 * Render patch to have controlled display
 */
public class HorizontalRuleView extends ComponentView {

    public HorizontalRuleView(Element elem) {
        super(elem);
    }

    @Override
    protected Component createComponent() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        JSeparator component = new JSeparator(); 
        component.setOrientation(HORIZONTAL);
        component.setBorder(BorderFactory.createLineBorder(new Color(209, 217, 224), 2));
        panel.add(component);
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));
        panel.setLayout(new GridLayout(0,1)); 
        return panel;
    }
}
