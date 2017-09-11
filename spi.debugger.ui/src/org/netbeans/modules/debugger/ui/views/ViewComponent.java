/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.ui.views;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Martin Entlicher
 */
public class ViewComponent extends JComponent implements org.openide.util.HelpCtx.Provider {
    
    private String icon;
    private String name;
    private String helpID;
    private String propertiesHelpID;
    private transient JComponent contentComponent;
    private ViewModelListener viewModelListener;
    
    public ViewComponent(String icon, String name, String helpID, String propertiesHelpID) {
        this.icon = icon;
        this.name = name;
        this.helpID = helpID;
        this.propertiesHelpID = propertiesHelpID;
        initComponents();
    }
    
    private void initComponents() {
        setLayout (new BorderLayout ());
        contentComponent = new javax.swing.JPanel(new BorderLayout ());
        add (contentComponent, BorderLayout.CENTER);  //NOI18N
        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorderPainted(true);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            toolBar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        toolBar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                javax.swing.UIManager.getDefaults().getColor("Separator.background")),
                javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                javax.swing.UIManager.getDefaults().getColor("Separator.foreground"))));
        add(toolBar, BorderLayout.WEST);
        JComponent buttonsPane = toolBar;
        viewModelListener = new ViewModelListener (
            name,
            contentComponent,
            buttonsPane,
            propertiesHelpID,
            ImageUtilities.loadImage(icon)
        );
    }

    @Override
    public void removeNotify() {
        if (viewModelListener != null) {
            viewModelListener.destroy ();
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(helpID);
    }
}
