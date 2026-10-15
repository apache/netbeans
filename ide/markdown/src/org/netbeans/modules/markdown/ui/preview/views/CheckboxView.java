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

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.text.Element;
import javax.swing.text.html.FormView;

/**
 *
 * @author moacirrf
 */
public class CheckboxView extends FormView {

    public CheckboxView(Element elem) {
        super(elem);
    }

    @Override
    protected Component createComponent() {
        Component component = super.createComponent();
        if (component instanceof JCheckBox) {
            JCheckBox c = (JCheckBox) component;
            c.setBorder(BorderFactory.createEmptyBorder(0, 0, -4, 0));
            c.addActionListener((ActionEvent e) -> c.setSelected(!c.isSelected()));
        }
        return component;
    }
}
