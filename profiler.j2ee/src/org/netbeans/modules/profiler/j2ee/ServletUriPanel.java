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
