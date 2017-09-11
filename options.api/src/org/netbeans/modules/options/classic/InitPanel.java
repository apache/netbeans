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

package org.netbeans.modules.options.classic;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.openide.util.AsyncGUIJob;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Radek Matous
 */
public class InitPanel extends JPanel implements AsyncGUIJob {

    private javax.swing.JLabel initComponent;
    private OptionsAction.OptionsPanel oPanel;
    private static InitPanel defInstance;

    static InitPanel getDefault(OptionsAction.OptionsPanel oPanel) {
        if (defInstance == null) {
            defInstance = new InitPanel(oPanel);
        }
        return defInstance;
    }

    private InitPanel(OptionsAction.OptionsPanel oPanel) {
        super();
        this.oPanel = oPanel;        
        initComponents();
    }   

    protected void initComponents() {        
        if (!oPanel.isPrepared()) {
            initComponent = new JLabel(NbBundle.getMessage(InitPanel.class, "LBL_computing")); // NOI18N
            initComponent.setPreferredSize(new Dimension(850, 450));
            // avoid flicking ?
            Color c = UIManager.getColor("Tree.background"); // NOI18N
            if (c == null) {
                //GTK 1.4.2 will return null for Tree.background
                c = Color.WHITE;
            }
            initComponent.setBackground(c);    // NOI18N               
            initComponent.setHorizontalAlignment(SwingConstants.CENTER);
            initComponent.setOpaque(true);
            
            CardLayout card = new CardLayout();
            setLayout(card);            
            add(initComponent, "init");    // NOI18N
            card.show(this, "init"); // NOI18N        
            Utilities.attachInitJob(this, this);
        } else {
            finished();  
        }
    }
    
    public void construct() {
        oPanel.prepareNodes();        
    }

    public void finished() {
        //initComponent.setBackground((Color) javax.swing.UIManager.getDefaults().get("Tree.background"));    // NOI18N
        add(oPanel, "ready");   // NOI18N                
        CardLayout card = (CardLayout) getLayout();
        card.show(this, "ready"); // NOI18N            
        oPanel.requestFocus(); // #44487
    }
}
