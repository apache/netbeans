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

package org.netbeans.installer.wizard.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class TextPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String TEXT_PROPERTY =
            "text"; // NOI18N
    public static final String CONTENT_TYPE_PROPERTY =
            "content.type"; // NOI18N
    
    public static final String DEFAULT_TEXT =
            ResourceUtils.getString(TextPanel.class,
            "TP.text"); // NOI18N
    public static final String DEFAULT_CONTENT_TYPE =
            ResourceUtils.getString(TextPanel.class,
            "TP.content.type"); // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public TextPanel() {
        setProperty(TEXT_PROPERTY, DEFAULT_TEXT);
        setProperty(CONTENT_TYPE_PROPERTY, DEFAULT_CONTENT_TYPE);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new TextPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class TextPanelUi extends WizardPanelUi {
        protected TextPanel component;
        
        public TextPanelUi(TextPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new TextPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class TextPanelSwingUi extends WizardPanelSwingUi {
        protected TextPanel component;
        
        private NbiTextPane textPane;
        
        public TextPanelSwingUi(
                final TextPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }

        @Override
        public String getTitle() {
            return null; // text panels do not have a title
        }
        
        @Override
        protected void initialize() {
            textPane.setContentType(
                    component.getProperty(CONTENT_TYPE_PROPERTY));
            textPane.setText(
                    component.getProperty(TEXT_PROPERTY));
        }
        
        private void initComponents() {
            // textPane /////////////////////////////////////////////////////////////
            textPane = new NbiTextPane();
            
            // this /////////////////////////////////////////////////////////////////
            add(textPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 11, 11),       // padding
                    0, 0));                           // ??? (padx, pady)
        }
    }
}
