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

package org.netbeans.modules.db.mysql.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.mysql.installations.BundledInstallation;
import org.netbeans.modules.db.mysql.installations.ui.SelectInstallationPanel;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Supporting methods to work with the registered implementations of Installation
 * 
 * @author David Van Couvering
 */
public class InstallationManager {    
    private static Logger LOGGER = 
            Logger.getLogger(InstallationManager.class.getName());
    
    private static ArrayList<Installation> INSTALLATIONS = null;
    
    private static final String INSTALLATION_PROVIDER_PATH = 
            "Databases/MySQL/Installations"; // NOI18N
    private static final String ICON_BASE =
            "org/netbeans/modules/db/mysql/resources/database.gif";     //NOI18N

    public static synchronized List<Installation> getInstallations(Collection loadedInstallations) {
        if ( INSTALLATIONS == null ) {
            // First see if we're bundled with MySQL.  If so, just return
            // the bundled installation
            Installation bundled = BundledInstallation.getDefault();
            if (bundled.isInstalled()) {
                ArrayList<Installation> bundledList = new ArrayList<Installation>();
                bundledList.add(bundled);
                return bundledList;
            }

            // Now order them so that the stack-based installations come first.
            // See the javadoc for Installation.isStackInstall() for the reasoning 
            // behind this.
            ArrayList<Installation> stackInstalls = new ArrayList<Installation>();
            ArrayList<Installation> stdInstalls = new ArrayList<Installation>();

            for ( Iterator it = loadedInstallations.iterator() ; it.hasNext() ; ) {
                Installation installation = (Installation)it.next();

                if ( installation.isStackInstall() ) {
                    stackInstalls.add(installation);                
                } else {
                    stdInstalls.add(installation);
                }
            }

            INSTALLATIONS = new ArrayList<Installation>();
            INSTALLATIONS.addAll(stackInstalls);
            INSTALLATIONS.addAll(stdInstalls);
        }
        
        return INSTALLATIONS;
    }
    
    /**
     * Get all valid installations of MySQL in this system.
     */
    public static List<Installation> detectAllInstallations() {
        List<Installation> installationCopy = new CopyOnWriteArrayList<Installation>();
        Collection<Installation> loadedInstallations = new ArrayList<Installation>(3);
        loadedInstallations.addAll(Lookups.forPath(INSTALLATION_PROVIDER_PATH)
                .lookupAll(Installation.class));
        Collection<? extends MultiInstallation> multiInstallations =
                Lookups.forPath(INSTALLATION_PROVIDER_PATH).lookupAll(
                MultiInstallation.class);
        for (MultiInstallation mi : multiInstallations) {
            loadedInstallations.addAll(mi.getInstallations());
        }
        installationCopy.addAll(InstallationManager.getInstallations(loadedInstallations));
        List<Installation> validInstallations =
                new ArrayList<Installation>(3);
        
        for (Installation installation : installationCopy) {            
            LOGGER.log(Level.FINE, "Looking for MySQL installation " + 
                    installation.getStartCommand()[0] + 
                    installation.getStartCommand()[1]);
            
            if ( installation.isInstalled() ) {
                LOGGER.log(Level.FINE, "Installation is installed");
                validInstallations.add(installation);
            }
        }
        return validInstallations;
    }

    /**
     * See if we can detect the paths to the various admin tools
     *
     * @return a valid installation if detected, null otherwise.  Returns the
     *      first installation found, so if there are multiple installations
     *      the other ones available will not be detected.
     */
    public static Installation detectInstallation() {
        List<Installation> allInstallations = detectAllInstallations();
        if (allInstallations.isEmpty()) {
            return null;
        } else {
            if (allInstallations.size() > 1) {
                notifyAboutMultipleInstallations();
            }
            return allInstallations.get(0);
        }
    }

    @NbBundle.Messages({
        "NotifyMultipleInstallations.title=Multiple MySQL installations found",
        "NotifyMultipleInstallations.text=Select the installation to use"
    })
    private static void notifyAboutMultipleInstallations() {
        NotificationDisplayer.getDefault().notify(
                Bundle.NotifyMultipleInstallations_title(),
                ImageUtilities.loadImageIcon(ICON_BASE, false),
                Bundle.NotifyMultipleInstallations_text(),
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectInstallationPanel.showSelectInstallationDialog();
            }
        }, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }
}
