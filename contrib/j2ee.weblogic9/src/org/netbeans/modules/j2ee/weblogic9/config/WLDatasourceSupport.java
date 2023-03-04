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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDataSource;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDataSourceParamsType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDriverParamsType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcPropertiesType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcPropertyType;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class WLDatasourceSupport {

    private static final String JDBC_FILE = "-jdbc.xml"; // NOI18N

    private static final String NAME_PATTERN = "datasource-"; // NOI18N

    private static final FileFilter JDBC_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isDirectory() && pathname.getName().endsWith(JDBC_FILE);
        }
    };

    private static final Logger LOGGER = Logger.getLogger(WLDatasourceSupport.class.getName());

    private final File resourceDir;

    public WLDatasourceSupport(File resourceDir) {
        assert resourceDir != null : "Resource directory can't be null"; // NOI18N
        this.resourceDir = FileUtil.normalizeFile(resourceDir);
    }

    public static Set<WLDatasource> getSystemDatasources(MBeanServerConnection con, ObjectName service) throws ConfigurationException {
        try {
            ObjectName[] adminServers = (ObjectName[]) con
                    .getAttribute(service, "ServerRuntimes"); // NOI18N
            Set<String> adminNames = new HashSet<String>();
            for (ObjectName adminServer : adminServers) {
                adminNames.add(con.getAttribute(adminServer, "Name").toString()); // NOI18N
            }
            
            ObjectName config = (ObjectName) con.getAttribute(
                    service, "DomainConfiguration"); // NOI18N

            ObjectName objectNames[] = (ObjectName[]) con.getAttribute(
                    config, "SystemResources"); // NOI18N

            Set<WLDatasource> result = new HashSet<WLDatasource>();
            for (ObjectName resource : objectNames) {
                String type = con.getAttribute(resource, "Type").toString();// NOI18N
                if ("JDBCSystemResource".equals(type)) { // NOI18N
                    ObjectName dataSource = (ObjectName) con
                            .getAttribute(resource, "JDBCResource"); // NOI18N
                    ObjectName[] targets = (ObjectName[]) con
                            .getAttribute(resource, "Targets"); // NOI18N

                    String name = con.getAttribute(dataSource,
                            "Name").toString(); // NOI18N
                    boolean foundAdminServer = false;
                    for (ObjectName target : targets) {
                        String targetServer = con.getAttribute(
                                target, "Name").toString(); // NOI18N
                        if (adminNames.contains(targetServer)) {
                            foundAdminServer = true;
                        }
                    }
                    if (!foundAdminServer) {
                        continue;
                    }

                    ObjectName dataSourceParams = (ObjectName) con
                            .getAttribute(dataSource,
                                    "JDBCDataSourceParams"); // NOI18N
                    ObjectName driverParams = (ObjectName) con
                            .getAttribute(dataSource,
                                    "JDBCDriverParams"); // NOI18N
                    String jndiNames[] = (String[]) con
                            .getAttribute(dataSourceParams,
                                    "JNDINames"); // NOI18N
                    String url = (String) con.getAttribute(driverParams, "Url"); // NOI18N
                    String driver = (String) con.getAttribute(driverParams, "DriverName"); // NOI18N
                    ObjectName[] properties = (ObjectName[]) con.getAttribute((ObjectName) con
                            .getAttribute(driverParams, "Properties"), "Properties"); // NOI18N
                    String user = null;
                    for (ObjectName prop : properties) {
                        String propName = (String) con.getAttribute(prop, "Name"); // NOI18N
                        if ("user".equals(propName)) { // NOI18N
                            user = (String) con.getAttribute(prop, "Value"); // NOI18N
                            break;
                        }
                    }
                    if (jndiNames.length == 0) {
                        jndiNames = new String[] {name};
                    }
                    for (String jndi : jndiNames) {
                        result.add(new WLDatasource(name, url, jndi, user, null, driver, null, true));
                    }
                }
            }
            return result;
        } catch (MBeanException ex) {
            throw new ConfigurationException("Datasource fetch failed", ex);
        } catch (AttributeNotFoundException ex) {
            throw new ConfigurationException("Datasource fetch failed", ex);
        } catch (InstanceNotFoundException ex) {
            throw new ConfigurationException("Datasource fetch failed", ex);
        } catch (ReflectionException ex) {
            throw new ConfigurationException("Datasource fetch failed", ex);
        } catch (IOException ex) {
            throw new ConfigurationException("Datasource fetch failed", ex);
        }
    }

    static Set<WLDatasource> getDatasources(File domain, FileObject inputFile,
            boolean systemDefault) throws ConfigurationException {
        if (inputFile == null || !inputFile.isValid() || !inputFile.canRead()) {
            if (LOGGER.isLoggable(Level.INFO) && inputFile != null) {
                LOGGER.log(Level.INFO, NbBundle.getMessage(WLDatasourceManager.class, "ERR_WRONG_CONFIG_DIR", inputFile));
            }
            return Collections.emptySet();
        }
        if (inputFile.isData() && inputFile.hasExt("xml")) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                JdbcHandler handler = new JdbcHandler(domain);
                parser.parse(new BufferedInputStream(inputFile.getInputStream()), handler);

                Map<File, Boolean> confs = new HashMap<File, Boolean>();
                Set<String> nameOnly = new HashSet<String>();

                // load by path in config.xml
                for (JdbcResource resource : handler.getResources()) {
                    // FIXME check target
                    if (resource.getFile() != null) {
                        File config = resource.resolveFile();
                        if (config != null) {
                            confs.put(config, resource.isSystem());
                        }
                    } else if (resource.getName() != null && resource.isSystem()) {
                        nameOnly.add(resource.getName());
                    }
                }

                Set<WLDatasource> result = new HashSet<WLDatasource>();
                result.addAll(getDatasources(confs));

                // load those in config/jdbc by name
                if (!nameOnly.isEmpty()) {
                    Set<WLDatasource> configDatasources =
                            getDatasources(domain, inputFile.getParent().getFileObject("jdbc"), true); // NOI18N
                    for (WLDatasource ds : configDatasources) {
                        if (nameOnly.contains(ds.getName())) {
                            result.add(ds);
                        }
                    }
                }

                return result;
            } catch (IOException ex) {
                return Collections.emptySet();
            } catch (ParserConfigurationException ex) {
                return Collections.emptySet();
            } catch (SAXException ex) {
                return Collections.emptySet();
            }
        } else if (inputFile.isFolder()) {
            File file = FileUtil.toFile(inputFile);
            Map<File, Boolean> confs = new HashMap<File, Boolean>();
            for (File jdbcFile : file.listFiles(JDBC_FILE_FILTER)) {
                confs.put(jdbcFile, systemDefault);
            }

            if (confs.isEmpty()) { // nowhere to search
                return Collections.emptySet();
            }

            return getDatasources(confs);
        }
        return Collections.emptySet();
    }

    private static Set<WLDatasource> getDatasources(Map<File, Boolean> confs) throws ConfigurationException {
        Set<WLDatasource> datasources = new HashSet<WLDatasource>();

        for (Map.Entry<File, Boolean> entry : confs.entrySet()) {
            File dsFile = entry.getKey();
            try {
                JdbcDataSource ds = null;
                try {
                    ds = JdbcDataSource.createGraph(dsFile);
                } catch (RuntimeException re) {
                    String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_NotParseableDatasources", dsFile.getAbsolutePath());
                    LOGGER.log(Level.INFO, msg);
                    continue;
                }

                // FIXME multi datasources
                String[] names = getJndiNames(ds);
                if (names != null) {
                    String name = getName(ds);
                    String connectionURl = getConnectionUrl(ds);
                    String userName = getUserName(ds);
                    String driverClass = getDriverClass(ds);
                    String password = getPassword(ds);

                    // the default JNDI name is datasource name
                    // TODO should we just add ds name to jndi names
                    if (names.length == 0) {
                        names = new String[] {name};
                    }
                    for (String jndiName : names) {
                        datasources.add(new WLDatasource(name, connectionURl,
                                jndiName, userName, password, driverClass, dsFile, entry.getValue()));
                    }
                }
            } catch (IOException ioe) {
                String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_CannotReadDatasources", dsFile.getAbsolutePath());
                LOGGER.log(Level.FINE, null, ioe);
                throw new ConfigurationException(msg, ioe);
            } catch (RuntimeException re) {
                String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_NotParseableDatasources", dsFile.getAbsolutePath());
                LOGGER.log(Level.FINE, null, re);
                throw new ConfigurationException(msg, re);
            }
        }

        return datasources;
    }

    public Set<WLDatasource> getDatasources() throws ConfigurationException {
        FileObject resource = FileUtil.toFileObject(resourceDir);

        return getDatasources(null, resource, false);
    }

    public Datasource createDatasource(final String jndiName, final String  url, final String username,
            final String password, final String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {

        WLDatasource ds = modifyDatasource(new DatasourceModifier() {

            @Override
            public ModifiedDatasource modify(Set<JdbcDataSource> datasources) throws DatasourceAlreadyExistsException {
                for (JdbcDataSource ds : datasources) {
                    String[] names = getJndiNames(ds);
                    if (names != null) {
                        for (String name : names) {
                            if (name.equals(jndiName)) {
                                WLDatasource existing = new WLDatasource(
                                        getName(ds), getConnectionUrl(ds), name,
                                        getUserName(ds), getPassword(ds), getDriverClass(ds), null, false);
                                throw new DatasourceAlreadyExistsException(existing);
                            }
                        }
                    }
                }

                // create the datasource
                ensureResourceDirExists();

                File candidate;
                int counter = 1;
                do {
                    candidate = new File(resourceDir, NAME_PATTERN
                            + counter + JDBC_FILE);
                    counter++;
                } while (candidate.exists());

                JdbcDataSource ds = new JdbcDataSource();
                setName(ds, jndiName);
                setConnectionUrl(ds, url);
                addJndiName(ds, jndiName);
                setUserName(ds, username);
                setPassword(ds, password);
                setDriverClass(ds, driver);

                try {
                    writeFile(candidate, ds);
                } catch (ConfigurationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return new ModifiedDatasource(candidate, ds);
            }
        });

        return ds;
    }

    private WLDatasource modifyDatasource(DatasourceModifier modifier)
            throws ConfigurationException, DatasourceAlreadyExistsException {

        try {
            ensureResourceDirExists();

            FileObject resourceDirObject = FileUtil.toFileObject(resourceDir);
            assert resourceDirObject != null;

            Map<JdbcDataSource, DataObject> datasources = new LinkedHashMap<JdbcDataSource, DataObject>();
            for (FileObject dsFileObject : resourceDirObject.getChildren()) {
                if (dsFileObject.isData() && dsFileObject.getNameExt().endsWith(JDBC_FILE)) {

                    DataObject datasourceDO = DataObject.find(dsFileObject);

                    EditorCookie editor = (EditorCookie) datasourceDO.getCookie(EditorCookie.class);
                    StyledDocument doc = editor.getDocument();
                    if (doc == null) {
                        doc = editor.openDocument();
                    }

                    JdbcDataSource source = null;
                    try {  // get the up-to-date model
                        // try to create a graph from the editor content
                        byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                        source = JdbcDataSource.createGraph(new ByteArrayInputStream(docString));
                    } catch (RuntimeException e) {
                        InputStream is = new BufferedInputStream(dsFileObject.getInputStream());
                        try {
                            source = JdbcDataSource.createGraph(is);
                        } finally {
                            is.close();
                        }
                        if (source == null) {
                            // neither the old graph is parseable, there is not much we can do here
                            // we could skip it but we can't be sure whether there are duplicate
                            // entries
                            // TODO: should we notify the user?
                            throw new ConfigurationException(
                                    NbBundle.getMessage(WLDatasourceSupport.class, "MSG_datasourcesXmlCannotParse", dsFileObject.getNameExt()));
                        }
                        // current editor content is not parseable, ask whether to override or not
                        NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                                NbBundle.getMessage(WLDatasourceSupport.class, "MSG_datasourcesXmlNotValid", dsFileObject.getNameExt()),
                                NotifyDescriptor.YES_NO_OPTION);
                        Object result = DialogDisplayer.getDefault().notify(notDesc);
                        if (result == NotifyDescriptor.NO_OPTION) {
                            // keep the old content
                            return null;
                        }
                        datasources.put(source, datasourceDO);
                    }
                }
            }

            ModifiedDatasource modifiedSource = modifier.modify(datasources.keySet());

            // TODO for now this code won't be called probably as there is no
            // real modify in our code just create
            DataObject datasourceDO = datasources.get(modifiedSource.getDatasource());
            if (datasourceDO != null) {
                boolean modified = datasourceDO.isModified();
                EditorCookie editor = (EditorCookie) datasourceDO.getCookie(EditorCookie.class);
                StyledDocument doc = editor.getDocument();
                if (doc == null) {
                    doc = editor.openDocument();
                }
                replaceDocument(doc, modifiedSource.getDatasource());

                if (!modified) {
                    SaveCookie cookie = (SaveCookie) datasourceDO.getCookie(SaveCookie.class);
                    cookie.save();
                }
            }

            // FIXME multi datasources
            String[] names = getJndiNames(modifiedSource.getDatasource());
            if (names != null && names.length > 0) {
                String name = getName(modifiedSource.getDatasource());
                String connectionURl = getConnectionUrl(modifiedSource.getDatasource());
                String userName = getUserName(modifiedSource.getDatasource());
                String driverClass = getDriverClass(modifiedSource.getDatasource());
                String password = getPassword(modifiedSource.getDatasource());

                return new WLDatasource(name, connectionURl,
                            names[0], userName, password, driverClass, modifiedSource.getFile(), false);
            }
        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WLDatasourceSupport.class, "MSG_CannotUpdate");
            throw new ConfigurationException(msg, ioe);
        }

        return null;
    }

    private void writeFile(final File file, final BaseBean bean) throws ConfigurationException {
        assert file != null : "File to write can't be null"; // NOI18N
        assert file.getParentFile() != null : "File parent folder can't be null"; // NOI18N

        try {
            FileObject cfolder = FileUtil.toFileObject(FileUtil.normalizeFile(file.getParentFile()));
            if (cfolder == null) {
                try {
                    cfolder = FileUtil.createFolder(FileUtil.normalizeFile(file.getParentFile()));
                } catch (IOException ex) {
                    throw new ConfigurationException(NbBundle.getMessage(WLDatasourceSupport.class,
                            "MSG_FailedToCreateConfigFolder", file.getParentFile().getAbsolutePath()));
                }
            }

            final FileObject folder = cfolder;
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = folder.getFileObject(name);
                        if (configFO == null) {
                            configFO = folder.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream (configFO.getOutputStream(lock), 4086);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch(IOException ioe) {
                                LOGGER.log(Level.FINE, null, ioe);
                            }
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                }
            });
            
            FileUtil.refreshFor(file);
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }

    private void replaceDocument(final StyledDocument doc, BaseBean graph) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            graph.write(out);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, out.toString(), null);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        });
    }

    private void ensureResourceDirExists() {
        if (!resourceDir.exists()) {
            resourceDir.mkdir();
            FileUtil.refreshFor(resourceDir);
        }
    }

    private static String getName(JdbcDataSource ds) {
        return ds.getName();
    }

    private static void setName(JdbcDataSource ds, String name) {
        ds.setName(name);
    }

    private static String[] getJndiNames(JdbcDataSource ds) {
        JdbcDataSourceParamsType params = ds.getJdbcDataSourceParams();
        if (params != null) {
            return params.getJndiName();
        }
        return null;
    }

    private static void addJndiName(JdbcDataSource ds, String name) {
        JdbcDataSourceParamsType params = ds.getJdbcDataSourceParams();
        if (params == null) {
            params = new JdbcDataSourceParamsType();
            ds.setJdbcDataSourceParams(params);
        }
        
        String[] oldNames = params.getJndiName();
        if (oldNames != null) {
            String[] newNames = new String[oldNames.length + 1];
            System.arraycopy(oldNames, 0, newNames, 0, oldNames.length);
            newNames[newNames.length - 1] = name;
            params.setJndiName(newNames);
        } else {
            params.setJndiName(new String[] {name});
        }
    }

    private static String getConnectionUrl(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            return params.getUrl();
        }
        return null;
    }

    private static void setConnectionUrl(JdbcDataSource ds, String url) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params == null) {
            params = new JdbcDriverParamsType();
            ds.setJdbcDriverParams(params);
        }
        params.setUrl(url);
    }

    private static String getDriverClass(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            return params.getDriverName();
        }
        return null;
    }

    private static void setDriverClass(JdbcDataSource ds, String driver) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params == null) {
            params = new JdbcDriverParamsType();
            ds.setJdbcDriverParams(params);
        }
        params.setDriverName(driver);
    }

    private static String getUserName(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            JdbcPropertiesType props = params.getProperties();
            if (props != null) {
                for (JdbcPropertyType item : props.getProperty2()) {
                    if ("user".equals(item.getName())) { // NOI18N
                        return item.getValue();
                    }
                }
            }
        }
        return null;
    }

    private static void setUserName(JdbcDataSource ds, String username) {
        setProperty(ds, "user", username); // NOI18N
    }

    // FIXME we return empty string for encrypted passwords
    private static String getPassword(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            String encrypted = params.getPasswordEncrypted();
            if (encrypted != null) {
                return "";
            } else {
                JdbcPropertiesType props = params.getProperties();
                if (props != null) {
                    for (JdbcPropertyType item : props.getProperty2()) {
                        if ("password".equals(item.getName())) { // NOI18N
                            return item.getValue();
                        }
                    }
                }
            }
        }
        return null;
    }

    // we set it only as plain text property - this is allowed in weblogic's
    // development mode only
    private static void setPassword(JdbcDataSource ds, String password) {
        setProperty(ds, "password", password); // NOI18N
    }

    private static void setProperty(JdbcDataSource ds, String key, String value) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params == null) {
            params = new JdbcDriverParamsType();
            ds.setJdbcDriverParams(params);
        }

        JdbcPropertiesType props = params.getProperties();
        if (props == null) {
            props = new JdbcPropertiesType();
            params.setProperties(props);
        }

        for (JdbcPropertyType item : props.getProperty2()) {
            if (key.equals(item.getName())) {
                item.setValue(value);
                return;
            }
        }

        JdbcPropertyType item = new JdbcPropertyType();
        item.setName(key);
        item.setValue(value);
        props.addProperty2(item);
    }

    private interface DatasourceModifier {

        @NonNull
        ModifiedDatasource modify(Set<JdbcDataSource> datasources) throws DatasourceAlreadyExistsException;

    }

    private static class ModifiedDatasource {

        private final File file;

        private final JdbcDataSource datasource;

        public ModifiedDatasource(File file, JdbcDataSource datasource) {
            this.file = file;
            this.datasource = datasource;
        }

        public JdbcDataSource getDatasource() {
            return datasource;
        }

        public File getFile() {
            return file;
        }
    }

    private static class JdbcSystemResourceHandler extends DefaultHandler {

        private final List<JdbcResource> resources = new ArrayList<JdbcResource>();

        private final File configDir;

        private final StringBuilder value = new StringBuilder();

        private JdbcResource resource;

        public JdbcSystemResourceHandler(File configDir) {
            this.configDir = configDir;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            value.setLength(0);
            if ("jdbc-system-resource".equals(qName)) { // NOI18N
                resource = new JdbcResource(configDir, true);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (resource == null) {
                return;
            }

            if ("jdbc-system-resource".equals(qName)) { // NOI18N
                resources.add(resource);
                resource = null; 
            } else if("name".equals(qName)) { // NOI18N
                resource.setName(value.toString());
            } else if ("taget".equals(qName)) { // NOI18N
                resource.setTarget(value.toString());
            } else if ("descriptor-file-name".equals(qName)) { // NOI18N
                resource.setFile(value.toString());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public List<JdbcResource> getResources() {
            return resources;
        }
    }

    private static class JdbcApplicationHandler extends DefaultHandler {

        private final List<JdbcResource> resources = new ArrayList<JdbcResource>();

        private final File domainDir;

        private final StringBuilder value = new StringBuilder();

        private JdbcResource resource;

        private boolean isJdbc;

        public JdbcApplicationHandler(File domainDir) {
            this.domainDir = domainDir;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            value.setLength(0);
            if ("app-deployment".equals(qName)) { // NOI18N
                resource = new JdbcResource(domainDir, false);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (resource == null) {
                return;
            }

            if ("app-deployment".equals(qName)) { // NOI18N
                if (isJdbc) {
                    resources.add(resource);
                }
                isJdbc = false;
                resource = null;
            } else if("name".equals(qName)) { // NOI18N
                resource.setName(value.toString());
            } else if ("taget".equals(qName)) { // NOI18N
                resource.setTarget(value.toString());
            } else if ("source-path".equals(qName)) { // NOI18N
                resource.setFile(value.toString());
            } else if ("module-type".equals(qName)) { // NOI18N
                if ("jdbc".equals(value.toString())) {
                    isJdbc = true;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public List<JdbcResource> getResources() {
            return resources;
        }
    }

    private static class JdbcHandler extends DefaultHandler {

        private final JdbcSystemResourceHandler system;

        private final JdbcApplicationHandler application;

        public JdbcHandler(File domainDir) {
            File configDir = domainDir != null ? new File(domainDir, "config") : null;
            system = new JdbcSystemResourceHandler(configDir);
            application = new JdbcApplicationHandler(domainDir);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            system.startElement(uri, localName, qName, attributes);
            application.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            system.endElement(uri, localName, qName);
            application.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            system.characters(ch, start, length);
            application.characters(ch, start, length);
        }

        public List<JdbcResource> getResources() {
            List<JdbcResource> resources = new ArrayList<JdbcResource>();
            resources.addAll(system.getResources());
            resources.addAll(application.getResources());
            return resources;
        }
    }

    private static class JdbcResource {

        private final File baseFile;

        private final boolean system;
        
        private String name;

        private String target;

        private String file;

        public JdbcResource(File baseFile, boolean system) {
            this.baseFile = baseFile;
            this.system = system;
        }

        @CheckForNull
        public File resolveFile() {
            if (file == null) {
                return null;
            }

            File config = new File(file);
            if (!config.isAbsolute()) {
                if (baseFile != null) {
                    config = new File(baseFile, file);
                } else {
                    return null;
                }
            }
            if (config.exists() && config.isFile() && config.canRead()) {
                return config;
            }
            return null;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public boolean isSystem() {
            return system;
        }

    }
}
