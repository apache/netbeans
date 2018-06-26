/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.core.webservices.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import org.openide.util.NbBundle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;

public class EnterWSDLUrlPanel extends JPanel {
    private String defaultWSDLUrl;

    public EnterWSDLUrlPanel(String defaultWSDLUrl) {
        this.defaultWSDLUrl = defaultWSDLUrl;
        initComponents();
        populateWSDLUrls();

    }
    
    private void populateWSDLUrls() {
        String[] urls = new String[]{defaultWSDLUrl};  //FIX-ME:what else shd we include?
        for(int i = 0; i < urls.length; i++) {
            wsdlURLComboBox.addItem(urls[i]);
        }
    }
    
    public String getSelectedWSDLUrl() {
        return wsdlURLComboBox.getSelectedItem().toString();
    }
    
    private void initComponents() {
        inputLabel = new JLabel(NbBundle.getMessage(EnterWSDLUrlPanel.class, "LBL_Input_WSDL_Url"));
        wsdlURLComboBox = new JComboBox();
        wsdlURLComboBox.setEditable(true);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(inputLabel, gbc);
        gbc.gridy = 1;
        gbc.weightx = 2.0;
        add(wsdlURLComboBox, gbc);
    }
    
    private JLabel inputLabel;
    private JComboBox wsdlURLComboBox;
    
}
