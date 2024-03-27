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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider2;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.SourceLevelChecker;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseTablesPanel extends javax.swing.JPanel implements AncestorListener{

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final DBSchemaManager dbschemaManager = new DBSchemaManager();

    private PersistenceGenerator persistenceGen;

    private SchemaElement sourceSchemaElement;
    private DatabaseConnection dbconn;
    private FileObject dbschemaFile;
    private String datasourceName;
    private TableClosure tableClosure;

    private boolean sourceSchemaUpdateEnabled;
    private boolean allowUpdateRecreate = true;

    private ChangeListener changeListener = null;
    private ServerStatusProvider2 serverStatusProvider;
    private DBSchemaFileList dbschemaFileList;
    private TableSource tableSource;
    private FileObject targetFolder;

    private String[] filterComboTxts = {
        org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_FILTERCOMBOBOX_ALL"),//NOI18N
        org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_FILTERCOMBOBOX_NEW"),//NOI18N
        org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_FILTERCOMBOBOX_UPDATE")//NOI18N
    };
    private TableUISupport.FilterAvailable filterAvailable = TableUISupport.FilterAvailable.ANY;

    private Project project;

    public DatabaseTablesPanel() {
        initComponents();
        initInitial();
        ListSelectionListener selectionListener = ( (ListSelectionEvent e) -> updateButtons() );
        availableTablesList.getSelectionModel().addListSelectionListener(selectionListener);
        selectedTablesList.getSelectionModel().addListSelectionListener(selectionListener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void initialize(final Project project, DBSchemaFileList dbschemaFileList, PersistenceGenerator persistenceGen, TableSource tableSource, FileObject targetFolder) {
        this.persistenceGen = persistenceGen;
        this.project = project;
        this.dbschemaFileList = dbschemaFileList;
        this.tableSource = tableSource;
        this.targetFolder = targetFolder;
        addAncestorListener(this);
    }
    
    private void initSubComponents(){

        changeListener = (ChangeEvent e) -> {
            if (project != null && ProviderUtil.isValidServerInstanceOrNone(project)) {
                // stop listening once a server was set
                serverStatusProvider.removeChangeListener(changeListener);
                datasourceLocalComboBox.setModel(new DefaultComboBoxModel());
                initializeWithDbConnections();
                // #190671 - because of hacks around server set in maven
                // listen and update data sources after server was set here again.
                // In theory this should not be necessary and
                // j2ee.common.DatasourceUIHelper.performServerSelection should have done
                // everything necessary but often at that time
                // PersistenceProviderSupplier.supportsDefaultProvider() is still false
                // (server change was not propagated there yet). In worst case combo model will be set twice:
                datasourceServerComboBox.setModel(new DefaultComboBoxModel());
                initializeWithDatasources();
                Mnemonics.setLocalizedText(datasourceLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Datasource"));
            }
        };

        // if no server is set then listen on the server selection:
        if (!ProviderUtil.isValidServerInstanceOrNone(project)) {
            serverStatusProvider = project.getLookup().lookup(ServerStatusProvider2.class);
            if (serverStatusProvider != null) {
                serverStatusProvider.addChangeListener(changeListener);
            }
        }

        {
            boolean hasJPADataSourcePopulator = project.getLookup().lookup(JPADataSourcePopulator.class) != null;
            initializeWithDatasources();
            initializeWithDbConnections();
            Mnemonics.setLocalizedText(datasourceLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Datasource"));

            DBSchemaUISupport.connect(dbschemaComboBox, dbschemaFileList);
            boolean hasDBSchemas = (dbschemaComboBox.getItemCount() > 0 && dbschemaComboBox.getItemAt(0) instanceof FileObject);

            dbschemaRadioButton.setEnabled(hasDBSchemas);
            dbschemaRadioButton.setVisible(hasDBSchemas);
            dbschemaComboBox.setEnabled(hasDBSchemas);
            dbschemaComboBox.setVisible(hasDBSchemas);
            datasourceLocalRadioButton.setVisible(hasDBSchemas || hasJPADataSourcePopulator);
            datasourceServerRadioButton.setVisible(hasJPADataSourcePopulator);
            datasourceServerRadioButton.setEnabled(hasJPADataSourcePopulator);
            datasourceServerComboBox.setEnabled(hasJPADataSourcePopulator);
            datasourceServerComboBox.setVisible(hasJPADataSourcePopulator);

            selectDefaultTableSource(tableSource, hasJPADataSourcePopulator, project, targetFolder);
        } 

        // hack to ensure the progress dialog displayed by updateSourceSchema()
        // is displayed on top of the wizard dialog. Needed because when initialize()
        // is called wizard dialog might be non-visible, so the progress dialog
        // would be displayed before the wizard dialog.
        sourceSchemaUpdateEnabled = true;
        SwingUtilities.invokeLater( () -> updateSourceSchema() );
    }
    
    private void initInitial(){
        //just to avoid mix of controls before actual initialization
        dbschemaRadioButton.setEnabled(false);
        dbschemaComboBox.setEnabled(false);
        dbschemaRadioButton.setVisible(false);
        dbschemaComboBox.setVisible(false);   
        datasourceServerRadioButton.setVisible(false);
        org.openide.awt.Mnemonics.setLocalizedText(datasourceLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Wait"));
    }

    private void initializeWithDatasources() {
        JPADataSourcePopulator dsPopulator = project.getLookup().lookup(JPADataSourcePopulator.class);
        if(dsPopulator != null) {
            dsPopulator.connect(datasourceServerComboBox);
        } else {
            datasourceServerComboBox.removeAllItems();
        }
    }

    private void initializeWithDbConnections() {
        DatabaseExplorerUIs.connect(datasourceLocalComboBox, ConnectionManager.getDefault());
    }

    /**
     * Selects the default table source (cf. issue 74113).
     */
    private void selectDefaultTableSource(TableSource tableSource, boolean withDatasources, Project project, FileObject targetFolder) {
        if (tableSource == null) {
            // the wizard is invoked for the first time for this project
            // the first schema file found (in this package, if possible)
            // should be selected
            int dbschemaCount = dbschemaComboBox.getItemCount();
            if (targetFolder != null) {
                SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
                SourceGroup targetSourceGroup = SourceGroups.getFolderSourceGroup(sourceGroups, targetFolder);
                if (targetSourceGroup != null) {
                    for (int i=0; i<dbschemaCount; i++){
                        Object nextSchema = dbschemaComboBox.getItemAt(i);
                        if (nextSchema instanceof FileObject) {
                            FileObject parent = ((FileObject)nextSchema).getParent();
                            if (parent.equals(targetFolder)){
                                dbschemaComboBox.setSelectedIndex(i);
                                dbschemaRadioButton.setSelected(true);
                                return;
                            }
                        }
                    }
                }
            }
            if (dbschemaCount > 0 && dbschemaComboBox.getItemAt(0) instanceof FileObject) {
                dbschemaComboBox.setSelectedIndex(0);
                dbschemaRadioButton.setSelected(true);
                return;
            }
        } else {
            // the wizard has already been invoked -- try to select the previous table source
            String tableSourceName = tableSource.getName();
            switch (tableSource.getType()) {
            case DATA_SOURCE:
                // if the previous source was a data source, it should be selected
                // only if a database connection can be found for it and we can
                // connect to that connection without displaying a dialog
                if (withDatasources && selectDatasource(tableSourceName, false)) {
                    return;
                }
                break;

            case CONNECTION:
                // if the previous source was a database connection, it should be selected
                // only if we can connect to it without displaying a dialog
                if (selectDbConnection(tableSourceName)) {
                    return;
                }
                break;

            case SCHEMA_FILE:
                // if the previous source was a dbschema file, it should be always selected
                if (selectDBSchemaFile(tableSourceName)) {
                    return;
                }
                break;
            }
        }
        
        //try to find pu for the project
        //nothing is selected based on previos selection, try to select based on persistence.xml
        boolean puExists = false;
        try {
            puExists = ProviderUtil.persistenceExists(project, targetFolder);
        } catch (InvalidPersistenceXmlException | RuntimeException ex) {
        }

        if(puExists){
            PUDataObject pud = null;
            try {
                pud = ProviderUtil.getPUDataObject(project, targetFolder, null);
            } catch (InvalidPersistenceXmlException ex) {
                Exceptions.printStackTrace(ex);
            }
            PersistenceUnit pu = (pud !=null && pud.getPersistence().getPersistenceUnit().length==1) ? pud.getPersistence().getPersistenceUnit()[0] : null;
            if(pu !=null ){
                if(withDatasources){
                    String jtaDs = pu.getJtaDataSource();
                    boolean jta = jtaDs != null || (Util.isContainerManaged(project) && 
                            (pu.getTransactionType() == null 
                            || pu.getTransactionType().equals(PersistenceUnit.JTA_TRANSACTIONTYPE)));
                    if(jta){
                        selectDatasource(jtaDs, true);
                    }
                    else {
                        String nJtaDs = pu.getNonJtaDataSource();
                        if(nJtaDs != null) {
                            selectDatasource(nJtaDs, true);
                        }
                    }
                } else {
                    //try to find jdbc connection
                    DatabaseConnection cn = ProviderUtil.getConnection(pu);
                    if(cn != null){
                        datasourceServerComboBox.setSelectedItem(cn);
                    }
                }
            }
        }

        // nothing got selected so far, so select the data source / connection
        // radio button, but don't select an actual data source or connection
        // (since this would cause the connect dialog to be displayed)
        if(datasourceServerComboBox.isVisible()) {
            datasourceServerRadioButton.setSelected(true);
        } else {
            datasourceLocalRadioButton.setSelected(true);
        }
    }

    /**
     * Finds the database connections whose database URL and user name equal
     * the database URL and the user name of the passed data source.
     *
     * @param  datasource the data source.
     *
     * @return the list of database connections; never null.
     *
     * @throws NullPointerException if the datasource parameter was null.
     */
    private static List<DatabaseConnection> findDatabaseConnections(JPADataSource datasource) {
        // copied from j2ee.common.DatasourceHelper (can't depend on that)
        if (datasource == null) {
            throw new NullPointerException("The datasource parameter cannot be null."); // NOI18N
        }

        List<DatabaseConnection> result = new ArrayList<>();

        String databaseUrl = datasource.getUrl();
        String user = datasource.getUsername();
        for (DatabaseConnection dbconn : ConnectionManager.getDefault().getConnections()) {
            if (databaseUrl.equals(dbconn.getDatabaseURL())) {
                result.add(dbconn);
            }
        }

        List<DatabaseConnection> resultUserMatched = result
                .stream()
                .filter(dc -> Objects.equals(user, dc.getUser()))
                .collect(Collectors.toList());

        if(! resultUserMatched.isEmpty()) {
            return resultUserMatched;
        } else {
            return Collections.unmodifiableList(result);
        }
    }

    /**
     * Tries to select the given data source and returns true if successful.
     * @param skipChecks if need just to select without verifications
     */
    private boolean selectDatasource(String jndiName, boolean skipChecks) {
        JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
        if (dsProvider == null){
            return false;
        }
        if(jndiName == null) {
            jndiName = "java:comp/DefaultDataSource";//try default
        }
        JPADataSource datasource = null;
        for (JPADataSource each : dsProvider.getDataSources()){
            if (jndiName.equals(each.getJndiName())){
                datasource = each;
            }
        }
        
        // The datasource can be null if the dsProvider.getDataSources() is empty 
        // or the jndiName can not be found. See issue 154641
        if(datasource == null) {
            return false;
        }

        List<DatabaseConnection> dbconns = findDatabaseConnections(datasource);
        if (dbconns.isEmpty()) {
            return false;
        }
        if(!skipChecks){
            DatabaseConnection dbcon = dbconns.get(0);
            if (dbcon.getJDBCConnection() == null) {
                return false;
            }
        }
        boolean selected = false;
        for(int i=0; i<datasourceServerComboBox.getItemCount(); i++){
            Object item = datasourceServerComboBox.getItemAt(i);
            JPADataSource jpaDS = dsProvider.toJPADataSource(item);
            if(jpaDS!=null){
                if( Objects.equals(datasource.getJndiName(), jpaDS.getJndiName())
                        && Objects.equals(datasource.getUrl(), jpaDS.getUrl())
                        && Objects.equals(datasource.getUsername(), jpaDS.getUsername())) {
                    datasourceServerComboBox.setSelectedIndex(i);
                    selected = true;
                    break;
                }
            }
        }
        if (!selected) {
            return false;
        }
        datasourceServerRadioButton.setSelected(true);
        return true;
    }

    /**
     * Tries to select the given connection and returns true if successful.
     */
    private boolean selectDbConnection(String name) {
        DatabaseConnection dbcon = ConnectionManager.getDefault().getConnection(name);
        if (dbcon == null || dbcon.getJDBCConnection() == null) {
            return false;
        }
        datasourceLocalComboBox.setSelectedItem(dbcon);
        if (!dbcon.equals(datasourceLocalComboBox.getSelectedItem())) {
            return false;
        }
        datasourceLocalRadioButton.setSelected(true);
        return true;
    }

    /**
     * Tries to select the given dbschema file and returns true if successful.
     */
    private boolean selectDBSchemaFile(String name) {
        FileObject dbschemaFl = FileUtil.toFileObject(new File(name));
        if (dbschemaFl == null) {
            return false;
        }
        dbschemaComboBox.setSelectedItem(dbschemaFl);
        if (!dbschemaFl.equals(dbschemaComboBox.getSelectedItem())) {
            return false;
        }
        dbschemaRadioButton.setSelected(true);
        return true;
    }

    public SchemaElement getSourceSchemaElement() {
        return sourceSchemaElement;
    }

    public DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }

    public FileObject getDBSchemaFile() {
        return dbschemaFile;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public TableClosure getTableClosure() {
        return tableClosure;
    }


    private void updateSourceSchema() {
        if (!sourceSchemaUpdateEnabled) {
            return;
        }

        sourceSchemaElement = null;
        datasourceName = null;
        dbconn = null;
        dbschemaFile = null;


        if (datasourceLocalRadioButton.isSelected()) {
            dbconn = (DatabaseConnection) datasourceLocalComboBox.getSelectedItem();
            try {
                if(dbconn != null) {
                    sourceSchemaElement = dbschemaManager.getSchemaElement(dbconn);
                }
            } catch (SQLException e) {
                notify(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_DatabaseError"));
            } finally {
                if(sourceSchemaElement == null){
                    datasourceServerComboBox.setSelectedIndex(-1);//drop to default selection instead of keep not loaded
                }
            }
        } else if (datasourceServerRadioButton.isSelected()) {
            Object item = datasourceServerComboBox.getSelectedItem();
            JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
            JPADataSource jpaDS = dsProvider != null ? dsProvider.toJPADataSource(item) : null;
            if (jpaDS != null) {
                List<DatabaseConnection> dbconns = findDatabaseConnections(jpaDS);
                if (!dbconns.isEmpty()) {
                    dbconn = dbconns.get(0);
                } else {
                    String drvClass = jpaDS.getDriverClassName();
                    if (drvClass == null) {
                        notify(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_NoDriverClassName"));
                    } else {
                        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(drvClass);
                        if (drivers.length == 0) {
                            notify(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_NoDriverError", drvClass));
                        } else {
                            JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers(drvClass)[0];
                            dbconn = ConnectionManager.getDefault().showAddConnectionDialogFromEventThread(
                                    driver, jpaDS.getUrl(), jpaDS.getUsername(), jpaDS.getPassword());
                        }
                    }
                }
                if (dbconn != null) {
                    try {
                        sourceSchemaElement = dbschemaManager.getSchemaElement(dbconn);
                        datasourceName = jpaDS.getJndiName();
                    } catch (SQLException e) {
                        notify(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_DatabaseError"));
                    }
                }
            }
        } else if (dbschemaRadioButton.isSelected()) {
            Object item = dbschemaComboBox.getSelectedItem();
            if (item instanceof FileObject) {
                dbschemaFile = (FileObject)item;
                sourceSchemaElement = dbschemaManager.getSchemaElement(dbschemaFile);
            }
        }

        TableProvider tableProvider = null;

        if (sourceSchemaElement != null) {
            tableProvider = new DBSchemaTableProvider(sourceSchemaElement, persistenceGen, project);
        } else {
            tableProvider = new EmptyTableProvider();
        }

        tableClosure = new TableClosure(tableProvider);
        tableClosure.setClosureEnabled(tableClosureCheckBox.isSelected());

        TableUISupport.connectAvailable(availableTablesList, tableClosure, filterAvailable);
        TableUISupport.connectSelected(selectedTablesList, tableClosure);

        updateButtons();

        changeSupport.fireChange();
    }

    private static void notify(String message) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }

    private void updateSourceSchemaComboBoxes() {
        datasourceLocalComboBox.setEnabled(datasourceLocalRadioButton.isSelected());
        datasourceServerComboBox.setEnabled(datasourceServerRadioButton.isSelected());
        dbschemaComboBox.setEnabled(dbschemaRadioButton.isSelected());
    }

    private void updateButtons() {
        Set<Table> addTables = TableUISupport.getSelectedTables(availableTablesList, true);
        Set<Table> allSelectedTables = TableUISupport.getSelectedTables(availableTablesList, false);
        addButton.setEnabled(tableClosure.canAddAllTables(addTables));

        addAllButton.setEnabled(!TableUISupport.getEnabledTables(availableTablesList).isEmpty());

        Set<Table> tables = TableUISupport.getSelectedTables(selectedTablesList);
        removeButton.setEnabled(tableClosure.canRemoveAllTables(tables));

        removeAllButton.setEnabled(!tableClosure.getSelectedTables().isEmpty());
        String problems = "";
        for (Table t : allSelectedTables) {
            if (t.isDisabled()) {
                if (t.getDisabledReason() instanceof Table.ExistingDisabledReason) {
                    String existingClass = ((Table.ExistingDisabledReason) t.getDisabledReason()).getFQClassName();
                    if(allowUpdateRecreate){
                        problems += (problems.length()>0 ? "\n" : "") + NbBundle.getMessage(DatabaseTablesPanel.class, "MSG_Already_Mapped_UpdateAllowed", new Object[] {t.getName(), existingClass});
                    } else {
                        problems += (problems.length()>0 ? "\n" : "") + NbBundle.getMessage(DatabaseTablesPanel.class, "MSG_Already_Mapped_UpdateAllowed", new Object[] {t.getName(), existingClass});
                    }
                } else if (t.getDisabledReason() instanceof Table.ExistingNotInSourceDisabledReason) {
                    String existingClass = ((Table.ExistingNotInSourceDisabledReason) t.getDisabledReason()).getFQClassName();
                    problems += (problems.length()>0 ? "\n" : "") + NbBundle.getMessage(DatabaseTablesPanel.class, "MSG_Already_Mapped_NotInSource", new Object[] {t.getName(), existingClass});
                } else if (t.getDisabledReason() instanceof Table.NoPrimaryKeyDisabledReason) {
                    problems += (problems.length()>0 ? "\n" : "") + NbBundle.getMessage(DatabaseTablesPanel.class, "MSG_No_Primary_Key", new Object[] {t.getName()});
                }
            }
        }
        tableErrorScroll.setVisible(! problems.trim().isEmpty());
        tableError.setText(problems);
        tableError.setCaretPosition(0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        schemaSource = new javax.swing.ButtonGroup();
        comboPanel = new javax.swing.JPanel();
        datasourceLabel = new javax.swing.JLabel();
        datasourceLocalRadioButton = new javax.swing.JRadioButton();
        datasourceLocalComboBox = new javax.swing.JComboBox();
        datasourceServerRadioButton = new javax.swing.JRadioButton();
        datasourceServerComboBox = new javax.swing.JComboBox();
        dbschemaRadioButton = new javax.swing.JRadioButton();
        dbschemaComboBox = new javax.swing.JComboBox();
        tablesPanel = new TablesPanel();
        availableTablesLabel = new javax.swing.JLabel();
        availableTablesScrollPane = new javax.swing.JScrollPane();
        availableTablesList = TableUISupport.createTableList();
        selectedTablesLabel = new javax.swing.JLabel();
        selectedTablesScrollPane = new javax.swing.JScrollPane();
        selectedTablesList = TableUISupport.createTableList();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addAllButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        tableClosureCheckBox = new javax.swing.JCheckBox();
        addAllTypeCombo = new javax.swing.JComboBox();
        tableErrorScroll = new javax.swing.JScrollPane();
        tableError = new javax.swing.JTextPane();

        setMinimumSize(new java.awt.Dimension(200, 300));
        setName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_DatabaseTables")); // NOI18N
        setPreferredSize(new java.awt.Dimension(496, 350));
        setLayout(new java.awt.GridBagLayout());

        comboPanel.setLayout(new java.awt.GridBagLayout());

        datasourceLabel.setLabelFor(datasourceServerComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(datasourceLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Datasource")); // NOI18N
        datasourceLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 4, 4));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        comboPanel.add(datasourceLabel, gridBagConstraints);

        schemaSource.add(datasourceLocalRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(datasourceLocalRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_LocalDatasource")); // NOI18N
        datasourceLocalRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                datasourceLocalRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        comboPanel.add(datasourceLocalRadioButton, gridBagConstraints);

        datasourceLocalComboBox.setEnabled(false);
        datasourceLocalComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datasourceLocalComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 0);
        comboPanel.add(datasourceLocalComboBox, gridBagConstraints);

        schemaSource.add(datasourceServerRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(datasourceServerRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_RemoteDatasource")); // NOI18N
        datasourceServerRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                datasourceServerRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        comboPanel.add(datasourceServerRadioButton, gridBagConstraints);

        datasourceServerComboBox.setEnabled(false);
        datasourceServerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datasourceServerComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 0);
        comboPanel.add(datasourceServerComboBox, gridBagConstraints);

        schemaSource.add(dbschemaRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(dbschemaRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_SchemaDatasource")); // NOI18N
        dbschemaRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dbschemaRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        comboPanel.add(dbschemaRadioButton, gridBagConstraints);

        dbschemaComboBox.setEnabled(false);
        dbschemaComboBox.setNextFocusableComponent(availableTablesList);
        dbschemaComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbschemaComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 0);
        comboPanel.add(dbschemaComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(comboPanel, gridBagConstraints);

        tablesPanel.setPreferredSize(new java.awt.Dimension(440, 174));
        tablesPanel.setLayout(new java.awt.GridBagLayout());

        availableTablesLabel.setLabelFor(availableTablesList);
        org.openide.awt.Mnemonics.setLocalizedText(availableTablesLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_AvailableTables")); // NOI18N
        availableTablesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "TXT_AvailableTables")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        tablesPanel.add(availableTablesLabel, gridBagConstraints);

        availableTablesScrollPane.setPreferredSize(new java.awt.Dimension(160, 130));

        availableTablesList.setNextFocusableComponent(addButton);
        availableTablesScrollPane.setViewportView(availableTablesList);
        availableTablesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSN_AvailableTables")); // NOI18N
        availableTablesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSD_AvailableTables")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablesPanel.add(availableTablesScrollPane, gridBagConstraints);

        selectedTablesLabel.setLabelFor(selectedTablesList);
        org.openide.awt.Mnemonics.setLocalizedText(selectedTablesLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_SelectedTables")); // NOI18N
        selectedTablesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "TXT_SelectedTables")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        tablesPanel.add(selectedTablesLabel, gridBagConstraints);

        selectedTablesScrollPane.setPreferredSize(new java.awt.Dimension(160, 130));
        selectedTablesScrollPane.setViewportView(selectedTablesList);
        selectedTablesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSN_SelectedTables")); // NOI18N
        selectedTablesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSD_SelectedTables")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablesPanel.add(selectedTablesScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Remove")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addAllButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_AddAll")); // NOI18N
        addAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 0);
        buttonPanel.add(addAllButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeAllButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_RemoveAll")); // NOI18N
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(removeAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 11);
        tablesPanel.add(buttonPanel, gridBagConstraints);

        tableClosureCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(tableClosureCheckBox, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_IncludeRelatedTables")); // NOI18N
        tableClosureCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "TXT_IncludeRelatedTables")); // NOI18N
        tableClosureCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableClosureCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tableClosureCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 4, 0);
        tablesPanel.add(tableClosureCheckBox, gridBagConstraints);

        addAllTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(filterComboTxts));
        addAllTypeCombo.setRenderer(new ItemListCellRenderer());
        addAllTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllTypeComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 4, 0);
        tablesPanel.add(addAllTypeCombo, gridBagConstraints);
        addAllTypeCombo.getAccessibleContext().setAccessibleName("Tables filter");
        addAllTypeCombo.getAccessibleContext().setAccessibleDescription("Tables filter");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(tablesPanel, gridBagConstraints);

        tableErrorScroll.setBorder(null);

        tableError.setEditable(false);
        tableError.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tableError.setOpaque(false);
        tableErrorScroll.setViewportView(tableError);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(tableErrorScroll, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void tableClosureCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tableClosureCheckBoxItemStateChanged
        tableClosure.setClosureEnabled(tableClosureCheckBox.isSelected());
    }//GEN-LAST:event_tableClosureCheckBoxItemStateChanged

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        tableClosure.removeAllTables();
        selectedTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void addAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllButtonActionPerformed
        Set<Table> tables = TableUISupport.getEnabledTables(availableTablesList);
        tableClosure.addTables(tables);
        availableTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_addAllButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Set<Table> tables = TableUISupport.getSelectedTables(selectedTablesList);
        tableClosure.removeTables(tables);
        selectedTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Set<Table> tables = TableUISupport.getSelectedTables(availableTablesList, true);
        tableClosure.addTables(tables);
        availableTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_addButtonActionPerformed

    private void dbschemaComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbschemaComboBoxActionPerformed
        updateSourceSchema();
    }//GEN-LAST:event_dbschemaComboBoxActionPerformed

    private void dbschemaRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_dbschemaRadioButtonItemStateChanged
        updateSourceSchemaComboBoxes();
        updateSourceSchema();
    }//GEN-LAST:event_dbschemaRadioButtonItemStateChanged

    private void datasourceServerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datasourceServerComboBoxActionPerformed
        datasourceServerComboBox.hidePopup();
        updateSourceSchema();
    }//GEN-LAST:event_datasourceServerComboBoxActionPerformed

    private void datasourceServerRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_datasourceServerRadioButtonItemStateChanged
        updateSourceSchemaComboBoxes();
        updateSourceSchema();
    }//GEN-LAST:event_datasourceServerRadioButtonItemStateChanged

    private void addAllTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllTypeComboActionPerformed
        if(filterComboTxts[0].equals(addAllTypeCombo.getSelectedItem().toString())){
            filterAvailable = filterAvailable.ANY;
        } else if (filterComboTxts[1].equals(addAllTypeCombo.getSelectedItem().toString())){
            filterAvailable = filterAvailable.NEW;
        } else {
            filterAvailable = filterAvailable.UPDATE;
        }
        TableUISupport.connectAvailable(availableTablesList, tableClosure, filterAvailable);
    }//GEN-LAST:event_addAllTypeComboActionPerformed

    private void datasourceLocalRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_datasourceLocalRadioButtonItemStateChanged
        updateSourceSchemaComboBoxes();
        updateSourceSchema();
    }//GEN-LAST:event_datasourceLocalRadioButtonItemStateChanged

    private void datasourceLocalComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datasourceLocalComboBoxActionPerformed
        datasourceLocalComboBox.hidePopup();
        updateSourceSchema();
    }//GEN-LAST:event_datasourceLocalComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAllButton;
    private javax.swing.JComboBox addAllTypeCombo;
    private javax.swing.JButton addButton;
    private javax.swing.JLabel availableTablesLabel;
    private javax.swing.JList availableTablesList;
    private javax.swing.JScrollPane availableTablesScrollPane;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel comboPanel;
    private javax.swing.JLabel datasourceLabel;
    private javax.swing.JComboBox datasourceLocalComboBox;
    private javax.swing.JRadioButton datasourceLocalRadioButton;
    private javax.swing.JComboBox datasourceServerComboBox;
    private javax.swing.JRadioButton datasourceServerRadioButton;
    private javax.swing.JComboBox dbschemaComboBox;
    private javax.swing.JRadioButton dbschemaRadioButton;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.ButtonGroup schemaSource;
    private javax.swing.JLabel selectedTablesLabel;
    private javax.swing.JList selectedTablesList;
    private javax.swing.JScrollPane selectedTablesScrollPane;
    private javax.swing.JCheckBox tableClosureCheckBox;
    private javax.swing.JTextPane tableError;
    private javax.swing.JScrollPane tableErrorScroll;
    private javax.swing.JPanel tablesPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void ancestorAdded(AncestorEvent event) {
        initSubComponents();
        removeAncestorListener(this);     
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
        
    }

    private final class TablesPanel extends JPanel {

        @Override
        public void doLayout() {
            super.doLayout();

            Rectangle availableBounds = availableTablesScrollPane.getBounds();
            Rectangle selectedBounds = selectedTablesScrollPane.getBounds();

            if (Math.abs(availableBounds.width - selectedBounds.width) > 1) {
                GridBagConstraints buttonPanelConstraints = ((GridBagLayout)getLayout()).getConstraints(buttonPanel);
                int totalWidth = getWidth() - buttonPanel.getWidth() - buttonPanelConstraints.insets.left - buttonPanelConstraints.insets.right;
                int equalWidth = totalWidth / 2;
                int xOffset = equalWidth - availableBounds.width;

                availableBounds.width = equalWidth;
                availableTablesScrollPane.setBounds(availableBounds);

                Rectangle addAllCmbRec = addAllTypeCombo.getBounds();
                if((addAllCmbRec.x+addAllCmbRec.width)!=(availableBounds.x+availableBounds.width)){
                    addAllCmbRec.x=(availableBounds.x+availableBounds.width)-addAllCmbRec.width;
                    addAllTypeCombo.setBounds(addAllCmbRec);
                }

                Rectangle buttonBounds = buttonPanel.getBounds();
                buttonBounds.x += xOffset;
                buttonPanel.setBounds(buttonBounds);

                Rectangle labelBounds = selectedTablesLabel.getBounds();
                labelBounds.x += xOffset;
                selectedTablesLabel.setBounds(labelBounds);

                selectedBounds.x += xOffset;
                selectedBounds.width = totalWidth - equalWidth;
                selectedTablesScrollPane.setBounds(selectedBounds);

                Rectangle tableClosureBounds = tableClosureCheckBox.getBounds();
                tableClosureBounds.x += xOffset;
                tableClosureBounds.width = totalWidth - equalWidth;
                tableClosureCheckBox.setBounds(tableClosureBounds);
            }
        }
    }

    public static final class WizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private DatabaseTablesPanel component;
        private boolean componentInitialized;

        private WizardDescriptor wizardDescriptor;
        private Project project;

        boolean waitingForScan;
        
        private String title;
        
        public WizardPanel(String wizardTitle) {
            title = wizardTitle;
        }

        @Override
        public DatabaseTablesPanel getComponent() {
            if (component == null) {
                component = new DatabaseTablesPanel();
                component.addChangeListener(this);
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
                return new HelpCtx(DatabaseTablesPanel.class);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            wizardDescriptor = settings;
            if (title != null) {
                wizardDescriptor.putProperty("NewFileWizard_Title", title); // NOI18N
            }
            
            if (!componentInitialized) {
                componentInitialized = true;

                project = Templates.getProject(wizardDescriptor);
                RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);
                DBSchemaFileList dbschemaFileList = helper.getDBSchemaFileList();
                PersistenceGenerator persistenceGen = helper.getPersistenceGenerator();
                TableSource tableSource = helper.getTableSource();
                FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);

                getComponent().initialize(project, dbschemaFileList, persistenceGen, tableSource, targetFolder);
            }
        }

        @Override
        public boolean isValid() {

            // TODO: RETOUCHE
            //            if (JavaMetamodel.getManager().isScanInProgress()) {
            if (false){
                if (!waitingForScan) {
                    waitingForScan = true;
                    RequestProcessor.Task task = RequestProcessor.getDefault().create( () -> {
                        // TODO: RETOUCHE
                        // JavaMetamodel.getManager().waitScanFinished();
                        waitingForScan = false;
                        changeSupport.fireChange();
                    });
                    setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "scanning-in-progress"));
                    task.schedule(0);
                }
                return false;
            }
            Sources sources=ProjectUtils.getSources(project);
            SourceGroup groups[]=sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if(groups == null || groups.length == 0) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class,"ERR_JavaSourceGroup")); //NOI18N
                getComponent().datasourceLocalComboBox.setEnabled(false);
                getComponent().datasourceServerComboBox.setEnabled(false);
                getComponent().dbschemaComboBox.setEnabled(false);
                return false;
            }

            if (SourceLevelChecker.isSourceLevel14orLower(project)) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_NeedProperSourceLevel"));
                return false;
            }

            if (getComponent().getSourceSchemaElement() == null) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_SelectTableSource"));
                return false;
            }

            if (getComponent().getTableClosure().getSelectedTables().size() <= 0) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_SelectTables"));
                return false;
            }

            // any view among selected tables?
            for (Table table : getComponent().getTableClosure().getSelectedTables()) {
                if (!table.isTable()) {
                    setWarningMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "MSG_ViewSelected"));
                    return true;
                }
            }

            setErrorMessage(" "); // NOI18N

            if (!ProviderUtil.isValidServerInstanceOrNone(project)) {
                setWarningMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_MissingServer"));
            }

            return true;
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);

            SchemaElement sourceSchemaElement = getComponent().getSourceSchemaElement();
            DatabaseConnection dbconn = getComponent().getDatabaseConnection();
            FileObject dbschemaFile = getComponent().getDBSchemaFile();
            String datasourceName = getComponent().getDatasourceName();

            if (dbschemaFile != null) {
                helper.setTableSource(sourceSchemaElement, dbschemaFile);
            } else {
                helper.setTableSource(sourceSchemaElement, dbconn, datasourceName);
            }
            if(getComponent().getTableClosure() != null) {
                helper.setTableClosure(getComponent().getTableClosure());
            }
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            changeSupport.fireChange();
        }

        private void setErrorMessage(String errorMessage) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage);
        }

        private void setWarningMessage(String warningMessage) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, warningMessage);
        }
    }
    private static class ItemListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            JLabel label = (JLabel)component;

                label.setText(value.toString());
             return label;
        }
    }
}
