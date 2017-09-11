/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.dlg;

import java.awt.Dialog;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.DatabaseModule;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.action.ConnectUsingDriverAction;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class AddConnectionWizard extends ConnectionDialogMediator implements WizardDescriptor.Iterator<AddConnectionWizard> {

    private String downloadFrom;
    private final Set<String> allPrivilegedFileNames = new HashSet<>();
    private String privilegedFileName;
    private String[] steps;
    private WizardDescriptor.Panel<AddConnectionWizard>[] panels;
    private int index;
    private ChoosingDriverPanel driverPanel;
    private final String pwd;
    private final String databaseUrl;
    private final String user;
    private DatabaseConnection connection;
    private List<String> schemas = null;
    private String currentSchema;
    private JDBCDriver jdbcDriver;
    private boolean increase = false;
    private WizardDescriptor wd;

    private AddConnectionWizard(JDBCDriver driver, String databaseUrl, String user, String password) {
        this.databaseUrl = databaseUrl;
        this.user = user;
        this.pwd = password;
        wd = new WizardDescriptor(this, this);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wd.setTitleFormat(new MessageFormat("{0}"));
        wd.setTitle(NbBundle.getMessage(AddConnectionWizard.class, "PredefinedWizard.WizardTitle")); // NOI18N
        updateState(driver);
    }

    @Override
    protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static DatabaseConnection showWizard(JDBCDriver driver, String databaseUrl, String user, String password) {
        AddConnectionWizard wiz = new AddConnectionWizard(driver, databaseUrl, user, password);
        wiz.showWizard();
        return wiz.getResult();
    }
    
    private void showWizard() {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wd);
        dialog.setVisible(true);
    }
    
    private DatabaseConnection getResult() {
        boolean cancelled = wd.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            assert getDatabaseConnection() != null : "DatabaseConnection found.";
            DatabaseConnection conn = getDatabaseConnection();
            if (getSchemas() != null) {
                conn.setSchema(getCurrentSchema());
            }
            try {
                ConnectionList.getDefault().add(getDatabaseConnection());
                return getDatabaseConnection();
            } catch (DatabaseException ex) {
                Logger.getLogger(AddConnectionWizard.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
                DbUtilities.reportError(NbBundle.getMessage(ConnectUsingDriverAction.class, "ERR_UnableToAddConnection"), ex.getMessage()); // NOI18N
                closeConnection();
            }
        }
        return null;
    }
    
    
    public static interface Panel extends WizardDescriptor.Panel<AddConnectionWizard>{}
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel<AddConnectionWizard>[] getPanels() {
        if (panels == null) {
            if (jdbcDriver != null) {
                URL[] jars = jdbcDriver.getURLs();
                if (jars != null && jars.length > 0) {
                        FileObject jarFO = URLMapper.findFileObject(jars[0]);
                        if (jarFO != null && jarFO.isValid()) {
                            this.allPrivilegedFileNames.add(jarFO.getNameExt());
                            this.increase = true;
                        }
                }
            }
            driverPanel = new ChoosingDriverPanel(jdbcDriver);
            panels = new Panel[] {
                driverPanel,
                new ConnectionPanel(),
                new ChoosingSchemaPanel(),
                new ChoosingConnectionNamePanel()
            };
            steps = new String[panels.length];
            steps = new String[] {
                NbBundle.getMessage(AddConnectionWizard.class, "ChoosingDriverUI.Name"), // NOI18N
                NbBundle.getMessage(AddConnectionWizard.class, "ConnectionPanel.Name"), // NOI18N
                NbBundle.getMessage(AddConnectionWizard.class, "ChoosingSchemaPanel.Name"), // NOI18N
                NbBundle.getMessage(AddConnectionWizard.class, "ChooseConnectionNamePanel.Name"), // NOI18N
            };
        }
        return panels;
    }

    @Override
    public WizardDescriptor.Panel<AddConnectionWizard> current() {
        // init panels first
        getPanels();
        if (increase) {
            index++;
            increase = false;
        }
                
        return getPanels()[index];
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().length; // NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    void setDriver(JDBCDriver driver) {
        if (driver == null) {
            updateState(null);
        } else {
            updateState(driver);
        }
    }

    JDBCDriver getDriver() {
        return this.jdbcDriver;
    }

    String getDatabaseUrl() {
        return databaseUrl;
    }
    
    String getUser() {
        return user;
    }
    
    String getPassword() {
        return pwd;
    }
    
    String[] getSteps() {
        return steps;
    }
    
    void setDatabaseConnection(DatabaseConnection conn) {
        this.connection = conn;
    }
    
    DatabaseConnection getDatabaseConnection() {
        return this.connection;
    }

    void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }
    
    List<String> getSchemas() {
        return schemas;
    }
    
    void setCurrentSchema(String schema) {
        this.currentSchema = schema;
    }
    
    String getCurrentSchema() {
        return currentSchema;
    }

    String getDownloadFrom() {
        return downloadFrom;
    }

    Collection<String> getAllPrivilegedNames() {
        return this.allPrivilegedFileNames;
    }

    String getPrivilegedName() {
        return this.privilegedFileName;
    }

    @Override
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (DatabaseException ex) {
            }
            connection = null;
        }
    }

    private void updateState(JDBCDriver driver) {
        if (driver != null) {
            if (driver.getName().contains(DatabaseModule.IDENTIFIER_ORACLE)) {
                this.downloadFrom = NbBundle.getMessage(AddConnectionWizard.class, "oracle.from"); // NOI18N
                this.allPrivilegedFileNames.clear();
                this.privilegedFileName = NbBundle.getMessage(AddConnectionWizard.class, "oracle.driver.name"); // NOI18N
                StringTokenizer st = new StringTokenizer(NbBundle.getMessage(AddConnectionWizard.class, "oracle.driver.name.prefix"), ","); // NOI18N
                while (st.hasMoreTokens()) {
                    this.allPrivilegedFileNames.add(st.nextToken().trim());
                }
            } else if (driver.getName().contains(DatabaseModule.IDENTIFIER_MYSQL)) {
                this.downloadFrom = NbBundle.getMessage(AddConnectionWizard.class, "mysql.from"); // NOI18N
                this.allPrivilegedFileNames.clear();
                this.privilegedFileName = NbBundle.getMessage(AddConnectionWizard.class, "mysql.driver.name"); // NOI18N
                StringTokenizer st = new StringTokenizer(NbBundle.getMessage(AddConnectionWizard.class, "mysql.driver.name.prefix"), ","); // NOI18N
                while (st.hasMoreTokens()) {
                    this.allPrivilegedFileNames.add(st.nextToken().trim());
                }
            } else {
                // others
                this.downloadFrom = null;
                this.privilegedFileName = ""; // NOI18N
                this.allPrivilegedFileNames.clear();
            }
            this.jdbcDriver = driver;
        }
    }

    public NotificationLineSupport getNotificationLineSupport() {
        return wd.getNotificationLineSupport();
    }
}
