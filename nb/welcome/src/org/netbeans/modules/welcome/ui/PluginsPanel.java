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

package org.netbeans.modules.welcome.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.LinkButton;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;

/**
 *
 * @author S. Aubrecht
 */
class PluginsPanel extends JPanel implements Constants {

    public PluginsPanel( boolean showInstallPlugins ) {
        super( new GridBagLayout() );
        setOpaque(false);
        if( showInstallPlugins ) {
            InstallConfig ic = InstallConfig.getDefault();

            if( ic.isErgonomicsEnabled() ) {
                addInstallPlugins(BundleSupport.getLabel("InstallPluginsFullIDE"), BundleSupport.getLabel("InstallPluginsDescrFullIDE"));
            } else {
                addInstallPlugins(BundleSupport.getLabel("InstallPlugins"), BundleSupport.getLabel("InstallPluginsDescr"));
            }
            
        } else {
            addActivateFeatures( BundleSupport.getLabel("ActivateFeaturesFullIDE"), BundleSupport.getLabel("ActivateFeaturesDescrFullIDE"));
        }
    }

    private void addActivateFeatures( String label, String description ) {
        LinkButton b = new LinkButton(label, Utils.getLinkColor(), true, "ActivateFeatures" ) { //NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                logUsage();
                new ShowPluginManagerAction("installed").actionPerformed(e); //NOI18N
            }
        };
        b.setFont(GET_STARTED_FONT);
        add( b, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,5,5), 0, 0));
        add( new JLabel(description), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(20,9,5,5), 0, 0));
        add( new JLabel(), new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
    }

    private void addInstallPlugins( String label, String description ) {
        LinkButton b = new LinkButton(label, Utils.getLinkColor(), true, "InstallPlugins") { //NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                logUsage();
                new ShowPluginManagerAction("available").actionPerformed(e); //NOI18N
            }
        };
        b.setFont(GET_STARTED_FONT);
        add( b, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,5,5), 0, 0));
        add( new JLabel(description), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(20,9,5,5), 0, 0));
        add( new JLabel(), new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
    }

    // XXX why is this an Action when it is only invoked directly?
    private static class ShowPluginManagerAction extends AbstractAction {
        private final String initialTab;
        public ShowPluginManagerAction(String initialTab) {
            super( BundleSupport.getLabel( "AddPlugins" ) ); //NOI18N
            this.initialTab = initialTab;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Actions.forID("System", "org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction").actionPerformed(new ActionEvent(e.getSource(), 100, initialTab));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
