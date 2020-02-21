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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

public class SetConfigurationAction extends AbstractAction implements Presenter.Menu, Presenter.Popup {
    
    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_KEY = "org.netbeans.modules.cnd.makeproject.ui.ConfigurationItem"; // NOI18N
    
    private JMenu subMenu;

    private final Project project;
    
    /** Creates a new instance of BrowserAction */
    public SetConfigurationAction(Project project) {
        super( NbBundle.getMessage( SetConfigurationAction.class, "LBL_SetConfigurationAction_Name"),   // NOI18N
               null );
	this.project = project;
        //OpenProjectList.getDefault().addPropertyChangeListener( this );
    }
    
        
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // no operation
    }

    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return subMenu;
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }
        
    private void createSubMenu() {
        if ( subMenu == null ) {
            String label = NbBundle.getMessage( SetConfigurationAction.class, "LBL_SetConfigurationAction_Name" ); // NOI18N
            subMenu = new JMenu( label );
        }
        
        subMenu.removeAll();
        ActionListener jmiActionListener = new MenuItemActionListener(); 
        
	ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
	if (pdp == null)
	    return;

	ConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
	Configuration[] confs = null;
        if (projectDescriptor != null && projectDescriptor.getState() != State.READING) {
            confs = projectDescriptor.getConfs().toArray();
            // Fill menu with items
            for ( int i = 0; i < confs.length; i++ ) {
                JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(confs[i].getName(), confs[i].isDefault());
                subMenu.add(jmi);
                jmi.putClientProperty(PROJECT_KEY, projectDescriptor);
                jmi.addActionListener(jmiActionListener);
            }
            // Now add the Configurations.. action. Do it in it's own menu item othervise it will get shifted to the right.
            subMenu.add(new JSeparator());
            JMenuItem profilesMenuItem = new JMenuItem(NbBundle.getMessage(SetConfigurationAction.class, "LBL_ConfigurationsAction_Name")); // NOI18N
            subMenu.add(profilesMenuItem);
            profilesMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed (ActionEvent event) {
                    CommonProjectActions.customizeProjectAction().actionPerformed(new ActionEvent(this, -1, null));
                }
            });
        }

        subMenu.setEnabled(confs != null && confs.length > 0 );
    }
    
    // Innerclasses ------------------------------------------------------------
    private static class MenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e ) {
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
		ConfigurationDescriptor projectDescriptor = (ConfigurationDescriptor)jmi.getClientProperty( PROJECT_KEY );
                if (projectDescriptor != null ) {
                    projectDescriptor.getConfs().setActive(jmi.getText());
		    //SprojectDescriptor.setModified();
                }
                
            }
            
        }
        
    }
    
}
