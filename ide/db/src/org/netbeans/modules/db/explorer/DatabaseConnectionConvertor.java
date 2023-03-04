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

package org.netbeans.modules.db.explorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.Environment;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads and writes the database connection registration format.
 *
 * @author Radko Najman, Andrei Badea, Jiri Rechtacek
 */
public class DatabaseConnectionConvertor implements Environment.Provider, InstanceCookie.Of {
    
    /**
     * The path where the connections are registered in the SystemFileSystem.
     */
    public static final String CONNECTIONS_PATH = "Databases/Connections"; // NOI18N
    
    public static final Logger LOGGER = 
            Logger.getLogger(DatabaseConnectionConvertor.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(DatabaseConnectionConvertor.class);
    
    /**
     * The delay by which the write of the changes is postponed.
     */
    private static final int DELAY = 2000;
    
    // Ensures DO's created for newly registered connections cannot be garbage-collected
    // before they are recognized by FolderLookup. This makes sure the FolderLookup
    // will return the originally registered connection instance.
    private static final WeakHashMap<DatabaseConnection, DataObject> newConn2DO = new WeakHashMap<>();

    // Helps ensure that when recognizing a new DO for a newly registered connection,
    // the DO will hold the originally registered connection instance instead of creating a new one.
    private static final Map<FileObject, DatabaseConnection> newFile2Conn = new ConcurrentHashMap<>();
    
    private final Reference<XMLDataObject> holder;

    /**
     * The lookup provided through Environment.Provider.
     */
    private Lookup lookup = null;

    private Reference<DatabaseConnection> refConnection = new WeakReference<>(null);
    
    private PCL listener;

    // a essential method for testing DB Explorer, don't remove it.
    private static DatabaseConnectionConvertor createProvider() {
        return new DatabaseConnectionConvertor();
    }
    
    private DatabaseConnectionConvertor() {
        holder = new WeakReference<>(null);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private DatabaseConnectionConvertor(XMLDataObject object) {
        holder = new WeakReference<>(object);
        InstanceContent cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
    }
    
    private DatabaseConnectionConvertor(XMLDataObject object, DatabaseConnection existingInstance) {
        this(object);
        refConnection = new WeakReference<>(existingInstance);
        attachListener();
    }
    
    // Environment.Provider methods
    
    @Override
    public Lookup getEnvironment(DataObject obj) {
        DatabaseConnection existingInstance = newFile2Conn.remove(obj.getPrimaryFile());
        if (existingInstance != null) {
            return new DatabaseConnectionConvertor((XMLDataObject)obj, existingInstance).getLookup();
        } else {
            return new DatabaseConnectionConvertor((XMLDataObject)obj).getLookup();
        }
    }
    
    // InstanceCookie.Of methods

    @Override
    public String instanceName() {
        XMLDataObject obj = getHolder();
        return obj == null ? "" : obj.getName();
    }
    
    @Override
    public Class<DatabaseConnection> instanceClass() {
        return DatabaseConnection.class;
    }
    
    @Override
    public boolean instanceOf(Class<?> type) {
        return (type.isAssignableFrom(DatabaseConnection.class));
    }

    @Override
    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        synchronized (this) {
            Object o = refConnection.get();
            if (o != null) {
                return o;
            }

            XMLDataObject obj = getHolder();
            if (obj == null) {
                return null;
            }
            FileObject connectionFO = obj.getPrimaryFile();
            Handler handler = new Handler(connectionFO.getNameExt());
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new InputSource(obj.getPrimaryFile().getInputStream());
                is.setSystemId(connectionFO.toURL().toExternalForm());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(EntityCatalog.getDefault());

                reader.parse(is);
            } catch (SAXException ex) {
                Exception x = ex.getException();
                LOGGER.log(Level.FINE, "Cannot read " + obj + ". Cause: " + ex.getLocalizedMessage(), ex);
                if (x instanceof java.io.IOException) {
                    throw (IOException)x;
                } else {
                    throw new java.io.IOException(ex.getMessage());
            }
            }

            DatabaseConnection inst = createDatabaseConnection(handler);
            refConnection = new WeakReference<>(inst);
            attachListener();
            return inst;
        }
    }
    
    private XMLDataObject getHolder() {
        return holder.get();
    }

    private void attachListener() {
        listener = new PCL();
        DatabaseConnection dbconn = (refConnection.get());
        dbconn.addPropertyChangeListener(WeakListeners.propertyChange(listener, dbconn));
    }

    private static DatabaseConnection createDatabaseConnection(Handler handler) {
        DatabaseConnection dbconn = new DatabaseConnection(
                handler.driverClass, 
                handler.driverName,
                handler.connectionUrl,
                handler.schema,
                handler.user,
                handler.connectionProperties);
        dbconn.setConnectionFileName(handler.connectionFileName);
        if (handler.displayName != null) {
            dbconn.setDisplayName(handler.displayName);
        }
        for (String importantSchema : handler.importantSchemas) {
            dbconn.addImportantSchema(importantSchema);
        }
        for (String importantDatabase : handler.importantCatalogs) {
            dbconn.addImportantCatalog(importantDatabase);
        }
        dbconn.setSeparateSystemTables(handler.separateSystemTables);
        if (handler.useScrollableCursors != null) {
            dbconn.setUseScrollableCursors(handler.useScrollableCursors);
        }
        LOGGER.log(Level.FINE, "Created DatabaseConnection[{0}] from file: {1}",
                new Object[] {dbconn, handler.connectionFileName});

        return dbconn;
    }

    /**
     * Creates the XML file describing the specified database connection.
     */
    public static DataObject create(DatabaseConnection dbconn) throws IOException {
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), CONNECTIONS_PATH);
        DataFolder df = DataFolder.findFolder(fo);

        AtomicWriter writer = new AtomicWriter(dbconn, df, convertToFileName(dbconn.getName()));
        df.getPrimaryFile().getFileSystem().runAtomicAction(writer);
        return writer.holder;
    }
    
    private static String convertToFileName(String databaseURL) {
        return databaseURL.substring(0, Math.min(32, databaseURL.length())).replaceAll("[^\\p{Alnum}]", "_"); // NOI18N
    }
    
    /**
     * Removes the file describing the specified database connection.
     */
    public static void remove(DatabaseConnection dbconn) throws IOException {
        String name = dbconn.getName();
        FileObject fo = FileUtil.getConfigFile(CONNECTIONS_PATH); //NOI18N
        // If CONNECTIONS_PATH can't be found (getConfigFile returns null)
        // its useless to try to delete any connection
        if (fo == null) {
            return;
        }
        DataFolder folder = DataFolder.findFolder(fo);
        DataObject[] objects = folder.getChildren();
        
        for (int i = 0; i < objects.length; i++) {
            InstanceCookie ic = objects[i].getCookie(InstanceCookie.class);
            if (ic != null) {
                Object obj;
                try {
                    obj = ic.instanceCreate();
                } catch (ClassNotFoundException e) {
                    continue;
                }
                if (obj instanceof DatabaseConnection) {
                    DatabaseConnection connection = (DatabaseConnection)obj;
                    if (connection.getName().equals(name)) {
                        objects[i].delete();
                        break;
                    }
                }
            }
        }
    }
    
    Lookup getLookup() {
        return lookup;
    }
    
    static byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(value);
    }

    static String decodePassword(byte[] bytes) throws CharacterCodingException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        ByteBuffer input = ByteBuffer.wrap(bytes);
        int outputLength = (int)(bytes.length * (double)decoder.maxCharsPerByte());
        if (outputLength == 0) {
            return ""; // NOI18N
        }
        char[] chars = new char[outputLength];
        CharBuffer output = CharBuffer.wrap(chars);
        CoderResult result = decoder.decode(input, output, true);
        if (!result.isError() && !result.isOverflow()) {
            result = decoder.flush(output);
        }
        if (result.isError() || result.isOverflow()) {
            throw new CharacterCodingException();
        } else {
            return new String(chars, 0, output.position());
        }
    }
    
    /**
     * Atomic writer for writing a changed/new database connection.
     */
    private static final class AtomicWriter implements FileSystem.AtomicAction {
        
        DatabaseConnection instance;
        MultiDataObject holder;
        String fileName;
        DataFolder parent;

        /**
         * Constructor for writing to an existing file.
         */
        AtomicWriter(DatabaseConnection instance, MultiDataObject holder) {
            this.instance = instance;
            this.holder = holder;
            this.fileName = holder.getPrimaryFile().getNameExt();
        }

        /**
         * Constructor for creating a new file.
         */
        AtomicWriter(DatabaseConnection instance, DataFolder parent, String fileName) {
            this.instance = instance;
            this.fileName = fileName;
            this.parent = parent;
        }

        @Override
        public void run() throws java.io.IOException {
            FileLock lck;
            FileObject data;

            if (holder != null) {
                data = holder.getPrimaryEntry().getFile();
                lck = holder.getPrimaryEntry().takeLock();
            } else {
                FileObject folder = parent.getPrimaryFile();
                String fn = FileUtil.findFreeFileName(folder, fileName, "xml"); //NOI18N
                data = folder.createData(fn, "xml"); //NOI18N
                lck = data.lock();
            }

            try (OutputStream ostm = data.getOutputStream(lck);
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                    ) {
                write(writer, data.getNameExt());
                writer.flush();
            } finally {
                lck.releaseLock();
            }

            if (holder == null) {
                newFile2Conn.put(data, instance);
                holder = (MultiDataObject)DataObject.find(data);
                // ensure the Environment.Provider.getEnvironment() is called for the new DataObject
                holder.getCookie(InstanceCookie.class);
                newConn2DO.put(instance, holder);
            }
        }

        void write(PrintWriter pw, String name) throws IOException {
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE connection PUBLIC '-//NetBeans//DTD Database Connection 1.2//EN' 'http://www.netbeans.org/dtds/connection-1_2.dtd'>"); //NOI18N
            pw.println("<connection>"); //NOI18N
            pw.println("  <driver-class value='" + XMLUtil.toAttributeValue(instance.getDriver()) + "'/>"); //NOI18N
            pw.println("  <driver-name value='" + XMLUtil.toAttributeValue(instance.getDriverName()) + "'/>"); // NOI18N
            pw.println("  <database-url value='" + XMLUtil.toAttributeValue(instance.getDatabase()) + "'/>"); //NOI18N
            if (instance.getSchema() != null) {
                pw.println("  <schema value='" + XMLUtil.toAttributeValue(instance.getSchema()) + "'/>"); //NOI18N
            }
            if (instance.getUser() != null) {
                pw.println("  <user value='" + XMLUtil.toAttributeValue(instance.getUser()) + "'/>"); //NOI18N
            }
            if (!instance.getName().equals(instance.getDisplayName())) {
                pw.println("  <display-name value='" + XMLUtil.toAttributeValue(instance.getDisplayName()) + "'/>"); //NOI18N
            }
            for (String importantSchema : instance.getImportantSchemas()) {
                pw.println("  <important-schema value='" + XMLUtil.toAttributeValue(importantSchema) + "'/>"); //NOI18N
            }
            for (String importantDatabase : instance.getImportantCatalogs()) {
                pw.println("  <important-catalog value='" + XMLUtil.toAttributeValue(importantDatabase) + "'/>"); //NOI18N
            }
            if (instance.rememberPassword() ) {
                char[] password = instance.getPassword() == null ? new char[0] : instance.getPassword().toCharArray();
                
                DatabaseConnection.storePassword(name, password);
            } else {
                DatabaseConnection.deletePassword(name);
            }
            if (instance.getConnectionProperties() != null) {
                Properties p = instance.getConnectionProperties();
                for (String key : p.stringPropertyNames()) {
                    pw.println("  <connection-property>");              //NOI18N
                    pw.print("    <name>");                             //NOI18N
                    pw.print(XMLUtil.toElementContent(key));
                    pw.println("</name>");                              //NOI18N
                    pw.print("    <value>");                            //NOI18N
                    pw.print(XMLUtil.toElementContent(p.getProperty(key)));
                    pw.println("</value>");                             //NOI18N
                    pw.println("  </connection-property>");             //NOI18N
                }
            }
            if (instance.isSeparateSystemTables()) {
                pw.println("  <separate-system-tables value='true'/>"); //NOI18N
            }
            pw.println("  <use-scrollable-cursors value='" + instance.isUseScrollableCursors() + "'/>"); //NOI18N
            pw.println("</connection>"); //NOI18N
        }        
    }

    /**
     * SAX handler for reading the XML file.
     */
    private static final class Handler extends DefaultHandler {
        
        private static final String ELEMENT_DRIVER_CLASS = "driver-class"; // NOI18N
        private static final String ELEMENT_DRIVER_NAME = "driver-name"; // NOI18N
        private static final String ELEMENT_DATABASE_URL = "database-url"; // NOI18N
        private static final String ELEMENT_SCHEMA = "schema"; // NOI18N
        private static final String ELEMENT_USER = "user"; // NOI18N
        private static final String ELEMENT_PASSWORD = "password"; // NOI18N
        private static final String ELEMENT_DISPLAY_NAME = "display-name"; // NOI18N
        private static final String ELEMENT_IMPORTANT_SCHEMA = "important-schema"; //NOI18N
        private static final String ELEMENT_IMPORTANT_CATALOG = "important-catalog"; //NOI18N
        private static final String ELEMENT_CONNECTION_PROPERTY = "connection-property"; // NOI18N
        private static final String ELEMENT_SEPARATE_SYS_TABLES = "separate-system-tables"; //NOI18N
        private static final String ELEMENT_USE_SCROLLABLE_CURSORS = "use-scrollable-cursors"; //NOI18N
        private static final String ELEMENT_CONNECTION_PROPERTY_NAME = "name"; // NOI18N
        private static final String ELEMENT_CONNECTION_PROPERTY_VALUE = "value"; // NOI18N
        private static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
        
        final String connectionFileName;
        private boolean readingProperty = false;
        private String propertyName;
        private String propertyValue;
        private final StringBuilder buffer = new StringBuilder();
        
        String driverClass;
        String driverName;
        String connectionUrl;
        String schema;
        String user;
        String displayName;
        Properties connectionProperties;
        boolean separateSystemTables = false;
        Boolean useScrollableCursors = null;
        List<String> importantSchemas = new ArrayList<>();
        List<String> importantCatalogs = new ArrayList<>();
        
        public Handler(String connectionFileName) {
            this.connectionFileName = connectionFileName;
            this.connectionProperties = new Properties();
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        @SuppressWarnings("deprecation") // Backward compatibility
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            String value = attrs.getValue(ATTR_PROPERTY_VALUE);
            if (ELEMENT_DRIVER_CLASS.equals(qName)) {
                driverClass = value;
            } else if (ELEMENT_DRIVER_NAME.equals(qName)) {
                driverName = value;
            } else if (ELEMENT_DATABASE_URL.equals(qName)) {
                connectionUrl = value;
            } else if (ELEMENT_SCHEMA.equals(qName)) {
                schema = value;
            } else if (ELEMENT_USER.equals(qName)) {
                user = value;
            } else if (ELEMENT_DISPLAY_NAME.equals(qName)) {
                displayName = value;
            } else if (ELEMENT_CONNECTION_PROPERTY.equals(qName)) {
                readingProperty = true;
                propertyName = "";                                      //NOI18N
                propertyValue = "";                                     //NOI18N
            } else if (readingProperty && ELEMENT_CONNECTION_PROPERTY_NAME.equals(qName)) {
                buffer.setLength(0);
            } else if (readingProperty && ELEMENT_CONNECTION_PROPERTY_VALUE.equals(qName)) {
                buffer.setLength(0);
            } else if (ELEMENT_PASSWORD.equals(qName)) {
                // reading old settings
                byte[] bytes = null;
                try {
                    bytes = decodeBase64(value);
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING,
                            "Illegal Base 64 string in password for connection " 
                            + connectionFileName, e); // NOI18N
                        // no password stored => this will require the user to re-enter the password
                }
                if (bytes != null) {
                    try {
                        LOGGER.log(Level.FINE, "Reading old settings from {0}", connectionFileName);
                        DatabaseConnection.storePassword(connectionFileName, decodePassword(bytes).toCharArray());
                    } catch (CharacterCodingException e) {
                        LOGGER.log(Level.WARNING,
                                "Illegal UTF-8 bytes in password for connection "
                                + connectionFileName, e); // NOI18N
                        // no password stored => this will require the user to re-enter the password
                    }
                }
            } else if (ELEMENT_IMPORTANT_SCHEMA.equals(qName)) {
                importantSchemas.add(value);
            } else if (ELEMENT_IMPORTANT_CATALOG.equals(qName)) {
                importantCatalogs.add(value);
            } else if (ELEMENT_SEPARATE_SYS_TABLES.equals(qName)) {
                separateSystemTables = Boolean.parseBoolean(value);
            } else if (ELEMENT_USE_SCROLLABLE_CURSORS.equals(qName)) {
                useScrollableCursors = Boolean.parseBoolean(value);
            }
        }

        @Override
        public void ignorableWhitespace(char[] chars, int start, int length) throws SAXException {
            if (readingProperty) {
                buffer.append(chars, start, length);
            }
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            if (readingProperty) {
                buffer.append(chars, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (readingProperty && ELEMENT_CONNECTION_PROPERTY.equals(qName)) {
                connectionProperties.put(propertyName, propertyValue);
                readingProperty = false;
                propertyName = "";
                propertyValue = "";
                buffer.setLength(0);
            } else if (readingProperty && ELEMENT_CONNECTION_PROPERTY_NAME.equals(qName)) {
                propertyName = buffer.toString();
            } else if (readingProperty && ELEMENT_CONNECTION_PROPERTY_VALUE.equals(qName)) {
                propertyValue = buffer.toString();
            }
        }
    }
    
    private final class PCL implements PropertyChangeListener, Runnable {
        
        /**
         * The list of PropertyChangeEvent that cause the connections to be saved.
         * Should probably be a set of DatabaseConnection's instead.
         */
        LinkedList<PropertyChangeEvent> keepAlive = new LinkedList<>();
        
        RequestProcessor.Task saveTask = null;
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("state".equals(evt.getPropertyName())) { //NOI18N
                return;
            }
            synchronized (this) {
                if (saveTask == null) {
                    saveTask = RP.create(this);
                }
                keepAlive.add(evt);
            }
            saveTask.schedule(DELAY);
        }
        
        @Override
        public void run() {
            PropertyChangeEvent e;

            synchronized (this) {
                e = keepAlive.removeFirst();
            }
            DatabaseConnection dbconn = (DatabaseConnection)e.getSource();
            XMLDataObject obj = getHolder();
            if (obj == null) {
                return;
            }
            try {
                obj.getPrimaryFile().getFileSystem().runAtomicAction(new AtomicWriter(dbconn, obj));
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            }
        }
    }
}
