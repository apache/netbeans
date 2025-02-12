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
package org.netbeans.modules.javaee.wildfly;


import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class ChangelogWildflyPlugin {
    private static final Logger LOGGER = Logger.getLogger(ChangelogWildflyPlugin.class.getName());
    private static final String VERSION_PREF = "version";

    public static void showChangelog() {
        try {
            Preferences prefs = NbPreferences.forModule(ChangelogWildflyPlugin.class);
            int version = Integer.parseInt(NbBundle.getMessage(ChangelogWildflyPlugin.class, VERSION_PREF));
            if (prefs.getInt(VERSION_PREF, 5) < version) {
                NotificationDisplayer.getDefault().notify(NbBundle.getMessage(ChangelogWildflyPlugin.class, "MSG_CHANGES_TITLE"),
                        ImageUtilities.loadImageIcon("org/netbeans/modules/javaee/wildfly/resources/wildfly.png", false),
                        new JLabel(NbBundle.getMessage(ChangelogWildflyPlugin.class, "MSG_CHANGES_SUMMARY")),
                        new JLabel(NbBundle.getMessage(ChangelogWildflyPlugin.class, "MSG_CHANGES_DESC")),
                        NotificationDisplayer.Priority.NORMAL, Category.INFO);
                prefs.putInt(VERSION_PREF, version);
            }
        } catch (MissingResourceException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }
}
