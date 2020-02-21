/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.cnd.diagnostics.clank;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.clang.tools.services.checkers.api.ClankCLOptionsProvider;
//import org.clang.tools.services.checkers.api.ClankCLOptionsProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;

/**
 *
 */
final class ClankCLArsPanel extends JPanel{
    private final Preferences preferences;

    public ClankCLArsPanel(Preferences preferences)  {
        if (preferences != null) {
            if (preferences.absolutePath().endsWith("/"+ClankDiagnoticsErrorProvider.NAME)) { //NOI18N
                this.preferences = preferences;
            } else {
                this.preferences = preferences.node(ClankDiagnoticsErrorProvider.NAME);
            }
        } else {
            this.preferences = AuditPreferences.AUDIT_PREFERENCES_ROOT.node(ClankDiagnoticsErrorProvider.NAME);
        }
        initComponents();
        
    }
    
    
    private void initComponents() {
        //get values from the provider
        final String[] args = ClankCLOptionsDeafaultImpl.getArgs();
        
        setLayout(new GridBagLayout());
        
        for (String arg : args) {            
            final JCheckBox checkBox = new JCheckBox(arg);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //put to preferences
                    preferences.putBoolean(arg, checkBox.isSelected());
                }
            });
            checkBox.setSelected(preferences.getBoolean(arg, true));
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);

            add(checkBox, gridBagConstraints);
        }
    }

    
    
}
