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
