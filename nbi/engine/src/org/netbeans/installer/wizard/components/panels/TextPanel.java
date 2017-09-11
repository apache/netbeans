/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
