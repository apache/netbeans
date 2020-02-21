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
