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

package org.netbeans.modules.j2ee.common.ui;

import java.awt.Dialog;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Support for managing broken/missing Databases.
 *
 * PLEASE NOTE! This is just a temporary solution. BrokenReferencesSupport from
 * the java project support currently does not allow to plug in a check for missing
 * Databases. Once BrokenReferencesSupport will support it, this class should be
 * removed.
 */
public class BrokenDatasourceSupport {
    
    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;
    
    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    private BrokenDatasourceSupport() {}
    
    /**
     * Shows UI customizer which gives users chance to fix encountered problems,
     * i.e. choose appropriate data source.
     *
     * @param project
     *
     */
    public static void fixDatasources(final Project project) {
        MissingDatabaseConnectionWarning.selectDatasources(
                NbBundle.getMessage(BrokenDatasourceSupport.class,"LBL_Resolve_Missing_Datasources_Title"),org.openide.util.NbBundle.getMessage(BrokenDatasourceSupport.class, "ACSD_Resolve_Missing_Datasources"), project); //  NOI18N
    }
    
    /**
     * Show alert message box informing user that a project has missing
     * database connections. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box.
     */
    public static synchronized void showAlert() {
        // Do not show alert if it is already shown or if it was shown
        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
        if (brokenAlertShown
                || brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis()
                || !J2EEUISettings.getDefault().isShowAgainBrokenDatasourceAlert()) {
            return;
        }
        
        brokenAlertShown = true;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    BrokenDatasourceAlertPanel alert = new BrokenDatasourceAlertPanel();
                    JButton close = new JButton(
                            NbBundle.getMessage(BrokenDatasourceSupport.class, "LBL_BrokenDatasourcesCustomizer_Close"));
                    close.getAccessibleContext().setAccessibleDescription(
                            NbBundle.getMessage(BrokenDatasourceSupport.class, "ACSD_BrokenDatasourcesCustomizer_Close"));
                    DialogDescriptor dd = new DialogDescriptor(
                            alert,
                            NbBundle.getMessage(BrokenDatasourceAlertPanel.class, "MSG_Broken_Datasources_Title"),
                            true,
                            new Object[] {close},
                            close,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null);
                    dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                    Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                    dlg.setVisible(true);
                } finally {
                    synchronized (BrokenDatasourceSupport.class) {
                        brokenAlertLastTime = System.currentTimeMillis();
                        brokenAlertShown = false;
                    }
                }
            }
        });
    }
    
    /**
     * Returns set of broken datasources
     *
     * @param project
     * @return Set<Datasource>  returns a set of data sources that don't have corresponding database connections
     */
    public static Set<Datasource> getBrokenDatasources(Project project) {
        J2eeModuleProvider jmp =
                (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        
        Set<Datasource> dss = null;
        try {
            dss = jmp.getConfigSupport().getDatasources();
        } catch (ConfigurationException e) {
            dss = new HashSet<Datasource>();
        }
        
        Set<Datasource> brokenDatasources = new HashSet<Datasource>();
        Iterator it = dss.iterator();
        while (it.hasNext()) {
            Datasource ds = (Datasource)it.next();
            if(!isFound(ds)){
                brokenDatasources.add(ds);
            }
        }
        
        return brokenDatasources;
    }
    
    private static boolean isFound(Datasource ds) {
        boolean found = false;
        String url = ds.getUrl();
        String username = ds.getUsername();
        DatabaseConnection[] dbConns = ConnectionManager.getDefault().getConnections();
        
        for (DatabaseConnection dbCon : dbConns) {
            String url1 = dbCon.getDatabaseURL();
            String username1 = dbCon.getUser();
            if (matchURL(url, url1, true) && Utilities.compareObjects(username, username1)) {
                found = true;
            }
        }
        return found;
    }
    
    private static boolean matchURL(String jdbcResourceUrl, String dsInfoUrl, boolean ignoreCase) {
        if (ignoreCase){
            jdbcResourceUrl = jdbcResourceUrl.toLowerCase();
            dsInfoUrl = dsInfoUrl.toLowerCase();
        }
        if (jdbcResourceUrl.equals(dsInfoUrl)){
            return true;
        }
        
        if (jdbcResourceUrl.contains("derby")) {
            int lastIndexOfColon = jdbcResourceUrl.lastIndexOf(":");
            int lastIndexOfSlash = jdbcResourceUrl.lastIndexOf("/");
            
            if (lastIndexOfColon >= 0 && lastIndexOfSlash >= 0) {
                String newJdbcResourceUrl = jdbcResourceUrl.substring(0, lastIndexOfColon) + jdbcResourceUrl.substring(lastIndexOfSlash);
                if (newJdbcResourceUrl.equals(dsInfoUrl)){
                    return true;
                }
            }
        }
        
        int nextIndex = 0;
        if (dsInfoUrl != null) {
            char[] jdbcResourceUrlChars = jdbcResourceUrl.toCharArray();
            char[] dsInfoUrlChars = dsInfoUrl.toCharArray();
            if (dsInfoUrlChars.length == jdbcResourceUrlChars.length) {
                for (int i = 0; i < jdbcResourceUrlChars.length - 1; i++) {
                    if ((jdbcResourceUrlChars[i] != dsInfoUrlChars[i]) && jdbcResourceUrlChars[i] == ':') {
                        nextIndex = 1;
                    } else if (jdbcResourceUrlChars[i + nextIndex] != dsInfoUrlChars[i]) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
