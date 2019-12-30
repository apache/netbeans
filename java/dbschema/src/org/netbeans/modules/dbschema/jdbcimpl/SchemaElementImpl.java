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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.*;

public class SchemaElementImpl extends DBElementImpl implements SchemaElement.Impl {
    
    private static Logger LOGGER = Logger.getLogger(
            SchemaElementImpl.class.getName());

    private DBElementsCollection tables;

    private DBIdentifier _schema;
    private DBIdentifier _catalog;
    private String _url;
    private String _username;
    private String _driver;
    private String _databaseProductName;
    private String _databaseProductVersion;
    private String _driverName;
    private String _driverVersion;

    private transient DatabaseMetaData dmd;
    private transient String catalog;
    
    private transient volatile boolean stop;
    
    public transient PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    
    private transient int progress;
    
    /** Creates new SchemaElementImpl */
    public SchemaElementImpl() {
        this(null);
    }
  
    public SchemaElementImpl(ConnectionProvider cp) {
        tables = new DBElementsCollection(this, new TableElement[0]);
        
        //workaround for bug #4396371
        //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
        Object hc = String.valueOf(tables.hashCode());
        while (DBElementsCollection.instances.contains(hc)) {
            tables = new DBElementsCollection(this, new TableElement[0]);
            hc = String.valueOf(tables.hashCode());
        }
        DBElementsCollection.instances.add(hc);
        
		if (cp != null) {
			try {
				String schema;

				dmd = cp.getDatabaseMetaData();

				_url = dmd.getURL();
				_username = dmd.getUserName();
//				schema = dmd.getUserName();
				schema = cp.getSchema();
				_schema = schema == null ? DBIdentifier.create("") : DBIdentifier.create(schema); //NOI18N
				catalog = cp.getConnection().getCatalog();
				_catalog = catalog == null ? DBIdentifier.create("") : DBIdentifier.create(catalog); //NOI18N
				_driver = cp.getDriver();
				_databaseProductName = dmd.getDatabaseProductName().trim();
				_databaseProductVersion = dmd.getDatabaseProductVersion();
				_driverName = dmd.getDriverName();
				_driverVersion = dmd.getDriverVersion();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
        
        stop = false;
    }
  
    public void setName(DBIdentifier name) throws DBException {        
        int pos;
        String fullName = name.getFullName();
        
        if (fullName == null) {
            fullName = name.getName();
            name.setFullName(fullName);
        }
        
        pos = fullName.lastIndexOf("/");
        if (pos != -1)
            name.setName(fullName.substring(pos + 1));
        else if (fullName.indexOf(".") != -1)
            name.setName(fullName);
        
        _name = name;
    }
    
    public DBIdentifier getName() {
        return _name;
    }

    /** Get the parsing status of the element.
    * This is a non-blocking operation.
    * @return one of {@link #STATUS_NOT}, {@link #STATUS_ERROR},
    * {@link #STATUS_PARTIAL}, or {@link #STATUS_OK}
    */
    public int getStatus() {
//NOT IMPLEMENTED YET !!!
        return SchemaElement.STATUS_OK;
    }
  
    /** Set the schema name of this schema snapshot.
    * @param id the schema name, or <code>null</code>
    * @exception DBException if the operation cannot proceed
    */
    public void setSchema(DBIdentifier schema) throws DBException {
        _schema = schema;
    }
  
    /** Get the schema name of this schema snapshot.
    * @return the schema name, or <code>null</code> if this snapshot does
    * not have a schema name
    */
    public DBIdentifier getSchema() {
        return _schema;
    }
  
    /** Set the catalog name of this schema snapshot.
    * @param id the catalog name, or <code>null</code>
    * @exception DBException if the operation cannot proceed
    */
    public void setCatalog(DBIdentifier catalog) throws DBException {
        _catalog = catalog;
    }
  
    /** Get the catalog name of this schema snapshot.
    * @return the catalog name, or <code>null</code> if this snapshot does
    * not have a catalog name
    */
    public DBIdentifier getCatalog() {
        return _catalog;
    }
  
    /** Change the set of tables.
     * @param elems the tables to change
     * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception DBException if the action cannot be handled
     */
    public void changeTables(TableElement[] elems,int action) throws DBException {
        tables.changeElements(elems, action);
    }
    
    /** Get all tables.
     * @return the tables
     */
    public TableElement[] getTables() {
        DBElement[] dbe = tables.getElements();
        return Arrays.asList(dbe).toArray(new TableElement[dbe.length]);
    }

    /** Find a table by name.
    * @param name the name for which to look
    * @return the table, or <code>null</code> if it does not exist
    */
    public TableElement getTable(DBIdentifier name) {
        TableElement tableElement = (TableElement) tables.find(name);
        if (tableElement != null) {
            return tableElement;
        } else if (getDriver().contains("mysql")) { //NOI18N
            // Case-insensitive search - workaround for #167389
            for (DBElement dbElement : tables.getElements()) {
                TableElement te = (TableElement) dbElement;
                if (te.getName().getName().equalsIgnoreCase(name.getName())) {
                    return te;
                }
            }
        }
        return null;
    }
    
    public void initTables(ConnectionProvider cp) {
        initTables(cp, null, null, false); //false = check tables references
    }
    
    public void initTables(ConnectionProvider cp, LinkedList t, LinkedList v) {
        initTables(cp, t, v, false); //false = check tables references
    }
        
    public void initTables(ConnectionProvider cp, LinkedList t, LinkedList v, boolean allTables) {
        if (cp !=null)
            try {
                progress = 0;
                LinkedList tables = new LinkedList();
                LinkedList views = new LinkedList();
                LinkedList tablesTmp = new LinkedList();
                LinkedList viewsTmp = new LinkedList();
//                String user = dmd.getUserName().trim();
                String user = cp.getSchema();
                List recycleBinTables;
                ResultSet rs;
                
                DDLBridge bridge = null;
                if (IDEUtil.isIDERunning())
                    bridge = new DDLBridge(cp.getConnection(), cp.getSchema(), dmd);
                
                // issue 76953: do not display tables from the Recycle Bin on Oracle 10 and higher
                if ("Oracle".equals(dmd.getDatabaseProductName()) ) { // NOI18N
                    recycleBinTables = getOracleRecycleBinTables();
                } else {
                    recycleBinTables = Collections.EMPTY_LIST;
                }

//get the list of all tables and views
                if (bridge != null) {
                    bridge.getDriverSpecification().getTables("%", new String[] {"TABLE"}); //NOI18N
                    rs = bridge.getDriverSpecification().getResultSet();
                } else
                    rs = dmd.getTables(catalog, user, "%", new String[] {"TABLE"}); //NOI18N

                if (rs != null) {
                    while (rs.next()) {
                        if (isStop()) {
                            rs.close();
                            return;
                        }

                        String tableTmp;
                        if (bridge != null)
                            tableTmp = bridge.getDriverSpecification().getRow().get(new Integer(3));
                        else
                            tableTmp = rs.getString("TABLE_NAME").trim(); //NOI18N
                        
                        if (!recycleBinTables.contains(tableTmp)) {
                            tablesTmp.add(tableTmp);
                        }
                    }
                    rs.close();
                }

                rs = null;
                if (bridge != null)
                    if (bridge.getDriverSpecification().areViewsSupported()) {
                        bridge.getDriverSpecification().getTables("%", new String[] {"VIEW"}); //NOI18N
                        rs = bridge.getDriverSpecification().getResultSet();
                    }
                else
                    if (MetaDataUtil.areViewsSupported(dmd.getDatabaseProductName()))
                        rs = dmd.getTables(catalog, user, "%", new String[] {"VIEW"}); //NOI18N

                if (rs != null) {
                    while (rs.next()) {
                        if (isStop()) {
                            rs.close();
                            return;
                        }

                        if (bridge != null)
                            viewsTmp.add(bridge.getDriverSpecification().getRow().get(new Integer(3)));
                        else
                            viewsTmp.add(rs.getString("TABLE_NAME").trim()); //NOI18N
                    }
                    rs.close();
                }
//list of all tables and views collected
                            
                if (t == null && v == null) {
                    tables = tablesTmp;
                    views = viewsTmp;
                } else {
                    t = checkNames(t, tablesTmp);
                    v = checkNames(v, viewsTmp);
                    
                    if (allTables)
                        tables = t;
                    else
                        tables = checkReferences(t, bridge, user);
                    
                    views = v;
                }
                
                // the tables are included twice because for each table
                // the progress is incremented twice (once for the table itself and once for the keys)
                propertySupport.firePropertyChange("totalCount", null, new Integer(2 * tables.size() + views.size())); //NOI18N
                
                initTables(cp, tables, allTables);
                initViews(cp, views, bridge);
            } catch (Exception exc) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    exc.printStackTrace();
            }
    }
    
    private LinkedList checkNames(LinkedList toCheck, LinkedList names) {
        LinkedList result = new LinkedList();
        
        for (int i = 0; i < toCheck.size(); i++) {
            Object table = toCheck.get(i);
            
            if (names.contains(table))
                result.add(table);
            else
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    System.out.println("Cannot find " + table + " table in the database."); //NOI18N
        }
        
        return result;
    }
    
    private LinkedList checkReferences(LinkedList tables, DDLBridge bridge, String schema) throws SQLException {
        ResultSet rs;
        String pkSchema;
        String fkSchema;
        String refTable;                    

        for (int i = 0; i < tables.size(); i++) { //add all referenced tables
            if (bridge != null) {
                bridge.getDriverSpecification().getImportedKeys(tables.get(i).toString());
                rs = bridge.getDriverSpecification().getResultSet();
            } else
                rs = dmd.getImportedKeys(catalog, schema, tables.get(i).toString());

            if (rs != null) {
                Map rset = new HashMap();
                String c1, c2, s1, s2;
                while (rs.next()) {
                    if (bridge != null) {
                        rset = bridge.getDriverSpecification().getRow();
                        
                        //test references between two schemas
                        c1 = (String) rset.get(new Integer(1));
                        s1 = (String) rset.get(new Integer(2));
                        c2 = (String) rset.get(new Integer(5));
                        s2 = (String) rset.get(new Integer(6));
                        
                        if (comp(c1, c2)) {
                            if (! comp(s1, s2))
                                continue;
                        } else
                            continue;                            
                        
                        pkSchema = (String) rset.get(new Integer(2));
                        fkSchema = (String) rset.get(new Integer(6));
                        if ((pkSchema == fkSchema) || (pkSchema.equals(fkSchema))) {
                            refTable = (String) rset.get(new Integer(3));
                            if (! tables.contains(refTable))
                                tables.add(refTable);
                        }
                        rset.clear();
                    } else {
                        //test references between two schemas
                        c1 = rs.getString("PKTABLE_CAT"); //NOI18N
                        s1 = rs.getString("PKTABLE_SCHEM"); //NOI18N
                        c2 = rs.getString("FKTABLE_CAT"); //NOI18N
                        s2 = rs.getString("FKTABLE_SCHEM"); //NOI18N
                        
                        if (comp(c1, c2)) {
                            if (! comp(s1, s2))
                                continue;
                        } else
                            continue;                            
                        
                        pkSchema = rs.getString("PKTABLE_SCHEM"); //NOI18N
                        if (pkSchema != null) {
                            pkSchema = pkSchema.trim();
                        }
                        fkSchema = rs.getString("FKTABLE_SCHEM"); //NOI18N
                        if (fkSchema != null) {
                            fkSchema = fkSchema.trim();
                        }
                        if ((pkSchema == fkSchema) || (pkSchema.equals(fkSchema))) {
                            refTable = rs.getString("PKTABLE_NAME").trim(); //NOI18N
                            if (! tables.contains(refTable))
                                tables.add(refTable);
                        }
                    }
                }
                rs.close();
            }
        }
        
        return tables;
    }
    
    private void initTables(ConnectionProvider cp, LinkedList tables, boolean allTables) throws DBException {
        String name;
        
        for (int i = 0; i < tables.size(); i++) {
            if (isStop())
                return;

            name = tables.get(i).toString();
            propertySupport.firePropertyChange("tableName", null, name); //NOI18N
            TableElementImpl tei = new TableElementImpl(name);
            tei.setTableOrView(true);
            TableElement[] te = {new TableElement(tei, (SchemaElement) element)};

            tei.initColumns(cp);
            tei.initIndexes(cp);
            changeTables(te, DBElement.Impl.ADD);

            progress++;
            propertySupport.firePropertyChange("progress", null, new Integer(progress)); //NOI18N
        }

        TableElement te;
        String tableName;
        for (int i = 0; i < tables.size(); i++) {
            if (isStop())
                return;

            tableName = tables.get(i).toString();
            te = getTable(DBIdentifier.create(tableName));
            if (te != null) {
                propertySupport.firePropertyChange("FKt", null, tableName); //NOI18N
                int fkOption = allTables ? 3 : 0;
                ((TableElementImpl) te.getElementImpl()).initKeys(cp, fkOption, tableName);
            }

            progress++;
            propertySupport.firePropertyChange("progress", null, new Integer(progress)); //NOI18N
        }
    }
    
    private void initViews(ConnectionProvider cp, LinkedList views, DDLBridge bridge) throws DBException, SQLException {
        String name;
        ResultSet rs;
        
        for (int i = 0; i < views.size(); i++) {
            if (isStop())
                return;

            name = views.get(i).toString();
            propertySupport.firePropertyChange("viewName", null, name); //NOI18N
            TableElementImpl tei = new TableElementImpl(name);
            tei.setTableOrView(false);
            TableElement te = new TableElement(tei, (SchemaElement) element);
            tei.initColumns(cp);

            String database = dmd.getDatabaseProductName();
            if (database!=null){ //could be null see bug 53887
                database = database.trim();
            }
            else{
                database=""; //NOI18N
            }
            if (database.equalsIgnoreCase("Oracle") || database.equalsIgnoreCase("Microsoft SQL Server")) {  //NOI18N
                propertySupport.firePropertyChange("FKv", null, name); //NOI18N
                
                ViewDependency vd = new ViewDependency(cp, cp.getSchema(), name);
                LinkedList tables = new LinkedList();
                LinkedList columns = new LinkedList();

                tables.clear();
                columns.clear();
                vd.constructPK();
                tables = vd.getTables();
                columns  = vd.getColumns();
                if (! columns.isEmpty()) {
                    boolean all = false;
                    for (int k = 0; k < columns.size(); k++)
                        //test if the view is created over all columns and try to eliminate agregation functions
                        if (((String) columns.get(k)).trim().endsWith("*")) {
                            all = true;
                            break;
                        }
                            
                    boolean capture = true;
                    LinkedList pkTables = new LinkedList();
                    for (int j = 0; j < tables.size(); j++) {
                        if (isStop())
                            return;

                        //compute PK
                        if (bridge != null) {
                            bridge.getDriverSpecification().getPrimaryKeys(tables.get(j).toString());
                            rs = bridge.getDriverSpecification().getResultSet();
                        } else
                            rs = cp.getDatabaseMetaData().getPrimaryKeys(cp.getConnection().getCatalog(), cp.getSchema(), tables.get(j).toString());

                        if (rs != null) {
                            if (! all) {
                                String colName;
                                Map rset = new HashMap();
                                while (rs.next()) {
                                    if (bridge != null) {
                                        rset = bridge.getDriverSpecification().getRow();
                                        colName = (String) rset.get(new Integer(4));
                                        rset.clear();
                                    } else
                                        colName = rs.getString("COLUMN_NAME").trim(); //NOI18N                                    

                                    if (columns.contains(colName.toLowerCase()) || columns.contains(tables.get(j).toString().toLowerCase() + "." + colName.toLowerCase()))
                                        continue;
                                    else {
                                        capture = false;
                                        break;
                                    }
                                }
                            }

                            if (capture)
                                pkTables.add(tables.get(j).toString());

                            rs.close();
                        }
                    }

                    if (capture)
                        for (int j = 0; j < pkTables.size(); j++) {
                            //capture PK
                            tei.initIndexes(cp, pkTables.get(j).toString());
                            tei.initKeys(cp, 1, pkTables.get(j).toString());

                            LinkedList tempList = new LinkedList();
                            UniqueKeyElement[] keys = te.getUniqueKeys();
                            for (int k = 0; k < keys.length; k++)
                                if (keys[k].isPrimaryKey())
                                    tempList.add(keys[k]);

                            keys = new UniqueKeyElement[tempList.size()];
                            for (int k = 0; k < tempList.size(); k++)
                                keys[k] = (UniqueKeyElement) tempList.get(k);
                            te.setKeys(keys);

                            IndexElement[] indexes = new IndexElement[keys.length];
                            for (int k = 0; k < keys.length; k++)
                                indexes[k] = keys[k].getAssociatedIndex();
                            te.setIndexes(indexes);
                        }

                    //compound PKs
                    if (te.getUniqueKeys().length > 1) {
                        IndexElementImpl iei = new IndexElementImpl(tei, "GENERATED_PK_" + tei.getName().getName(), true);
                        IndexElement[] ie = {new IndexElement(iei, te)};
                        IndexElement[] ies = te.getIndexes();
                        for (int j = 0; j < ies.length; j++)
                            iei.changeColumns(ies[j].getColumns(), DBElement.Impl.ADD);
                        te.setIndexes(ie);
                        
                        IndexElement ii = te.getIndexes()[0];
                        UniqueKeyElementImpl ukei = new UniqueKeyElementImpl(ii.getName().getName(), true);
                        UniqueKeyElement uke = new UniqueKeyElement(ukei, te, ii);
                        uke.setColumns(ii.getColumns());
                        tei.changeKeys(new UniqueKeyElement[] {uke}, DBElement.Impl.SET);
                    }
                    
                    //compute FKs
                    LinkedList toCapture = new LinkedList();
                    LinkedList validFKs = new LinkedList();
                    LinkedList fkTables = new LinkedList();
                    for (int j = 0; j < tables.size(); j++) {
                        if (isStop())
                            return;

                        if (bridge != null) {
                            bridge.getDriverSpecification().getImportedKeys(tables.get(j).toString());
                            rs = bridge.getDriverSpecification().getResultSet();
                        } else
                            rs = cp.getDatabaseMetaData().getImportedKeys(cp.getConnection().getCatalog(), cp.getSchema(), tables.get(j).toString());
                        
                        if (rs != null) {
                            Map rset = new HashMap();
                            LinkedList local = new LinkedList();
                            LinkedList ref = new LinkedList();
                            LinkedList fk = new LinkedList();
                            String fkName, c1, c2, s1, s2;
                            while (rs.next()) {
                                if (bridge != null) {
                                    rset = bridge.getDriverSpecification().getRow();
                                    
                                    //test references between two schemas
                                    c1 = (String) rset.get(new Integer(1));
                                    s1 = (String) rset.get(new Integer(2));
                                    c2 = (String) rset.get(new Integer(5));
                                    s2 = (String) rset.get(new Integer(6));

                                    if (comp(c1, c2)) {
                                        if (! comp(s1, s2))
                                            continue;
                                    } else
                                        continue;                            
                                    
                                    fkName = (String) rset.get(new Integer(12));
                                    if (fkName == null)
                                        continue;
                                    else
                                        fkName = fkName.trim();
//                                    schemas = ((rset.get(new Integer(6)) == rset.get(new Integer(2))) || rset.get(new Integer(6)).equals(rset.get(new Integer(2)))) ? true : false;                                    
                                    local.add(fkName + "." + ((String) rset.get(new Integer(7))) + "." + ((String) rset.get(new Integer(8)))); //NOI18N
                                    ref.add(fkName + "." + ((String) rset.get(new Integer(3))) + "." + ((String) rset.get(new Integer(4)))); //NOI18N
                                    if (! fk.contains(fkName))
                                        fk.add(fkName);
                                    rset.clear();
                                } else {
                                    //test references between two schemas
                                    c1 = rs.getString("PKTABLE_CAT"); //NOI18N
                                    s1 = rs.getString("PKTABLE_SCHEM"); //NOI18N
                                    c2 = rs.getString("FKTABLE_CAT"); //NOI18N
                                    s2 = rs.getString("FKTABLE_SCHEM"); //NOI18N

                                    if (comp(c1, c2)) {
                                        if (! comp(s1, s2))
                                            continue;
                                    } else
                                        continue;
                        
                                    fkName = rs.getString("FK_NAME"); //NOI18N
                                    if (fkName == null)
                                        continue;
                                    else
                                        fkName = fkName.trim();
//                                    schemas = ((rs.getString("FKTABLE_SCHEM") == rs.getString("PKTABLE_SCHEM")) || rs.getString("FKTABLE_SCHEM").equals(rs.getString("PKTABLE_SCHEM"))) ? true : false;                                    
                                    local.add(fkName + "." + rs.getString("FKTABLE_NAME").trim() + "." + rs.getString("FKCOLUMN_NAME").trim()); //NOI18N
                                    ref.add(fkName + "." + rs.getString("PKTABLE_NAME").trim() + "." + rs.getString("PKCOLUMN_NAME").trim()); //NOI18N
                                    if (! fk.contains(fkName))
                                        fk.add(fkName);
                                }
                            }
                            rs.close();
                            
                            String colName;
                            for (int k = 0; k < fk.size(); k++) {
                                fkName = fk.get(k).toString();
                                for (int l = 0; l < local.size(); l++) {
                                    colName = local.get(l).toString();
                                    if (colName.startsWith(fkName))
                                        colName = colName.substring(colName.lastIndexOf(".") + 1).toLowerCase();
                                    else
                                        continue;
                                    
                                    if (all || columns.contains(colName) || columns.contains(tables.get(j).toString().toLowerCase() + "." + colName)) {
                                        continue;
                                    } else {
                                        fk.set(k, null);
                                        break;
                                    }
                                }
                                    
                                if (fk.get(k) != null)
                                    for (int l = 0; l < ref.size(); l++) {
                                        colName = ref.get(l).toString();
                                        if (colName.startsWith(fkName)) {
                                            colName = colName.substring(colName.indexOf(".") + 1, colName.lastIndexOf("."));
                                            if (getTable(DBIdentifier.create(colName)) == null)
                                                toCapture.add(colName);
                                            break;
                                        }
                                    }
                            }
                                
                            String tblName = tables.get(j).toString();
                            for (int k = 0; k < fk.size(); k++) {
                                Object o = fk.get(k);
                                if (o != null) {
                                    validFKs.add(o);
                                    if (! fkTables.contains(tblName))
                                        fkTables.add(tblName);
                                }
                            }
                        }
                    }

                    initTables(cp, checkReferences(toCapture, bridge, cp.getSchema()), false);

                    for (int j = 0; j < fkTables.size(); j++)
                        tei.initKeys(cp, 2, fkTables.get(j).toString());
                    
                    LinkedList tempList = new LinkedList();
                    ForeignKeyElement[] fke = te.getForeignKeys();
                    UniqueKeyElement[] uke = te.getUniqueKeys();
                    for (int j = 0; j < fke.length; j++)
                        if (validFKs.contains(fke[j].getName().getName()))
                            tempList.add(fke[j]);
                    KeyElement[] ke = new KeyElement[uke.length + tempList.size()];
                    for (int j = 0; j < uke.length; j++)
                        ke[j] = uke[j];
                    int idx = uke.length;
                    for (int j = 0; j < tempList.size(); j++)
                        ke[j + idx] = (ForeignKeyElement) tempList.get(j);
                    
                    te.setKeys(ke);

                }
            }

            changeTables(new TableElement[] {te}, DBElement.Impl.ADD);

            progress++;
            propertySupport.firePropertyChange("progress", null, new Integer(progress)); //NOI18N
        }
    }
    
    private List getOracleRecycleBinTables() {
        List result = new ArrayList();
        try {
            if ( dmd.getDatabaseMajorVersion() < 10 ) {
                return Collections.EMPTY_LIST;
            }
            
            Statement stmt = dmd.getConnection().createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT OBJECT_NAME FROM RECYCLEBIN WHERE TYPE = 'TABLE'"); // NOI18N
                try {
                    while (rs.next()) {
                        result.add(rs.getString("OBJECT_NAME")); // NOI18N
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (SQLException exc) {
            // Some older versions of Oracle driver throw an exception on 
            // getDatabaseMajorVersion()
            LOGGER.log(Level.WARNING, "Some older versions of the Oracle " +
                    " driver do not support getDatabaseMajorVersion().  " +
                    " Setting recycle bin tables to an empty list.", 
                    exc); // NOI18N

            result = Collections.EMPTY_LIST;
        } catch (AbstractMethodError ame) {
            LOGGER.log(Level.WARNING, "Some older versions of the Oracle " +
                    " driver do not support getDatabaseMajorVersion().  " +
                    " Setting recycle bin tables to an empty list.", 
                    ame); // NOI18N
            result = Collections.EMPTY_LIST;
        }
        return result;
    }
  
    /** Getter for property url.
    * @return Value of property url.
    */
    public String getUrl() {
        return _url;
    }
  
    /** Setter for property url.
    * @param url New value of property url.
    */
    public void setUrl(String url) throws DBException {
        _url = url;
    }
  
    /** Getter for property username.
    * @return Value of property username.
    */
    public String getUsername() {
        return _username;
    }
  
    /** Setter for property username.
    * @param username New value of property username.
    */
    public void setUsername(String username) throws DBException {
        _username = username;
    }
  
    /** Getter for property driver.
     * @return Value of property driver.
     */
    public String getDriver() {
        return _driver;
    }
  
    /** Setter for property driver.
     * @param driver New value of property driver.
     */
    public void setDriver(String driver) {
        _driver = driver;
    }
  
    /** Getter for property databaseProductName.
     * @return Value of property databaseProductName.
     */
    public String getDatabaseProductName() {
        return _databaseProductName;
    }
  
    /** Setter for property databaseProductName.
     * @param databaseProductName New value of property databaseProductName.
     */
    public void setDatabaseProductName(String databaseProductName) throws DBException {
        _databaseProductName = databaseProductName;
    }
  
    /** Getter for property databaseProductVersion.
     * @return Value of property databaseProductVersion.
     */
    public String getDatabaseProductVersion() {
        return _databaseProductVersion;
    }
  
    /** Setter for property databaseProductVersion.
     * @param databaseProductVersion New value of property databaseProductVersion.
     */
    public void setDatabaseProductVersion(String databaseProductVersion) throws DBException {
        _databaseProductVersion = databaseProductVersion;
    }
  
    /** Getter for property driverName.
     * @return Value of property driverName.
     */
    public String getDriverName() {
        return _driverName;
    }
  
    /** Setter for property driverName.
     * @param driverName New value of property driverName.
     */
    public void setDriverName(String driverName) throws DBException {
        _driverName = driverName;
    }
  
    /** Getter for property driverVersion.
     * @return Value of property driverVersion.
     */
    public String getDriverVersion() {
        return _driverVersion;
    }
  
    /** Setter for property driverVersion.
     * @param driverVersion New value of property driverVersion.
     */
    public void setDriverVersion(String driverVersion) throws DBException {
        _driverVersion = driverVersion;
    }
    
    public boolean isStop() {
        return stop;
    }
    
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    //========== property change support needed for progressbar ==========
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener (l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener (l);
    }

	//=============== extra methods needed for xml archiver ==============

	/** Returns the table collection of this schema element.  This method
	 * should only be used internally and for cloning and archiving.
	 * @return the table collection of this schema element
	 */
	public DBElementsCollection getTableCollection ()
	{
		return tables;
	}

	/** Set the table collection of this claschemass element to the supplied
	 * collection.  This method should only be used internally and for
	 * cloning and archiving.
	 * @param collection the table collection of this schema element
	 */
	public void setTableCollection (DBElementsCollection collection)
	{
		tables = collection;
	}
}
