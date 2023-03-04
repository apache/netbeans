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

package org.netbeans.modules.profiler.j2ee;

import org.openide.util.NbBundle;


/**
 * Copied & modified from package org.netbeans.modules.web.project.ui
 *
 * @author  mkuchtiak
 * @author  Jiri Sedlacek
 */
public class ServletUriPanel extends javax.swing.JPanel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField textField1;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates new form ServletUriPanel */
    public ServletUriPanel(String selectedItem) {
        initComponents();
        jLabel2.setText(selectedItem);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public java.awt.Dimension getPreferredSize() {
        return new java.awt.Dimension(super.getPreferredSize().width + 30, super.getPreferredSize().height);
    }

    public String getServletUri() {
        String servletUri = jLabel2.getText() + textField1.getText();

        if ((servletUri != null) && (servletUri.length() > 0) && (servletUri.charAt(0) != '/')) { // NOI18N
            servletUri = "/" + servletUri; // NOI18N
        }

        return servletUri;
    }

    @NbBundle.Messages({
        "LBL_setServletURI=<html>If required, provide some request parameters for the servlet:<br>e.g. /flowerServlet<b>?flower\\=rose&amp;color\\=red</b></html>",
        "ACC_setServletURI=Provide parameters that will be passed to the servlet."
    })
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        textField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(Bundle.LBL_setServletURI());
        jLabel1.setLabelFor(textField1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 12, 0);
        add(jLabel2, gridBagConstraints);

        textField1.getAccessibleContext()
                  .setAccessibleDescription(Bundle.ACC_setServletURI());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 12, 12);
        add(textField1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(new javax.swing.JPanel(), gridBagConstraints);
    }
}
