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
package org.netbeans.modules.db.sql.history;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.netbeans.modules.db.sql.execute.ui.SQLHistoryPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author John Baker
 */
public class SQLHistoryManager  {
    public static final String OPT_SQL_STATEMENTS_SAVED_FOR_HISTORY = "SQL_STATEMENTS_SAVED_FOR_HISTORY"; // NOI18N
    public static final int DEFAULT_SQL_STATEMENTS_SAVED_FOR_HISTORY = 100;
    public static final int MAX_SQL_STATEMENTS_SAVED_FOR_HISTORY = 10000;
    public static final String PROP_SAVED = "saved"; //NOI18N

    private static final String SQL_HISTORY_DIRECTORY = "Databases/SQLHISTORY"; // NOI18N
    private static final String SQL_HISTORY_BASE = "sql_history"; // NOI18N
    private static final String SQL_HISTORY_EXT = "xml"; // NOI18N
    private static final String SQL_HISTORY_FILE = SQL_HISTORY_BASE + "." + SQL_HISTORY_EXT; // NOI18N
    private static final String TAG_HISTORY = "history"; // NOI18N
    private static final String TAG_SQL = "sql"; // NOI18N
    private static final String ATTR_DATE = "date"; // NOI18N
    private static final String ATTR_URL = "url"; // NOI18N
    private static final String CONTENT_NEWLINE = "\n"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(SQLHistoryEntry.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(
            SQLHistoryManager.class.getName(), 1, false, false);
    // Time between call to save and real save - usefull to accumulate before save
    private static final int SAVE_DELAY = 5 * 1000;

    private static SQLHistoryManager _instance = null;

    private final RequestProcessor.Task SAVER = RP.create(new Saver());
    private final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT =
            new PropertyChangeSupport(this);

    private SQLHistory sqlHistory;

    protected SQLHistoryManager() {
        loadHistory();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // If a save is pending on shutdown, enforce immediate write
                if (SAVER.getDelay() > 0) {
                    SAVER.schedule(0);
                    SAVER.waitFinished();
                }
            }
        });
    }

    public static synchronized SQLHistoryManager getInstance() {
        if (_instance == null) {
            _instance = new SQLHistoryManager();
        }
        return _instance;
    }

    public int getListSize() {
        return NbPreferences.forModule(SQLHistoryPanel.class).getInt("OPT_SQL_STATEMENTS_SAVED_FOR_HISTORY", DEFAULT_SQL_STATEMENTS_SAVED_FOR_HISTORY);
    }

    protected FileObject getHistoryRoot(boolean create) throws IOException {
        FileObject result = null;
        FileObject historyRootDir = getConfigRoot().getFileObject(getRelativeHistoryPath());
        if (historyRootDir != null || create) {
            if (historyRootDir == null) {
                historyRootDir = FileUtil.createFolder(getConfigRoot(), getRelativeHistoryPath());
            }
            FileObject historyRoot = historyRootDir.getFileObject(getHistoryFilename());

            if (historyRoot != null || create) {
                if (historyRoot == null) {
                    historyRoot = historyRootDir.createData(getHistoryFilename());
                }
                result = historyRoot;
            }
        }
        return result;
    }

    protected FileObject getConfigRoot() {
        return FileUtil.getConfigRoot();
    }

    protected String getRelativeHistoryPath() {
        return SQL_HISTORY_DIRECTORY;
    }

    protected String getHistoryFilename() {
        return SQL_HISTORY_FILE;
    }

    public void setListSize(int listSize) {
        NbPreferences.forModule(SQLHistoryPanel.class).putInt("OPT_SQL_STATEMENTS_SAVED_FOR_HISTORY", listSize);
        sqlHistory.setHistoryLimit(listSize);
    }

    public void saveSQL(SQLHistoryEntry sqlStored) {
        sqlHistory.add(sqlStored);
    }

    private void loadHistory() {
        try {
            sqlHistory = new SQLHistory();
            FileObject historyFile = getHistoryRoot(false);
            if(historyFile != null) {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(historyFile.getInputStream());
                Element rootElement = doc.getDocumentElement();
                if(TAG_HISTORY.equals(rootElement.getTagName())) {
                    NodeList sqlNodes = rootElement.getElementsByTagName(TAG_SQL);
                    for(int i = 0; i < sqlNodes.getLength(); i++) {
                        Element sql = (Element) sqlNodes.item(i);
                        SQLHistoryEntry sqe = new SQLHistoryEntry();
                        sqe.setDateXMLVariant(sql.getAttribute(ATTR_DATE));
                        sqe.setUrl(sql.getAttribute(ATTR_URL));
                        sqe.setSql(sql.getTextContent());
                        sqlHistory.add(sqe);
                    }
                }
            } else {

            }
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            sqlHistory = new SQLHistory();
            LOGGER.log(Level.WARNING, ex.getMessage());
        }
        sqlHistory.setHistoryLimit(getListSize());
    }

    public void save() {
        // On call to save schedule real saving, as save is a often calleed
        // method, this can bundle multiple saves into one write.
        // See bug #209720.
        //
        // There is an potential for a dataloss in case of a forced shutdown
        // of the jvm, but this is considered acceptable (normal shutdown
        // is catered for by a shutdown hook)
        if (SAVER.getDelay() == 0) {
            SAVER.schedule(SAVE_DELAY);
        }
    }

    public SQLHistory getSQLHistory() {
        return sqlHistory;
    }

    /**
     * Add property change listener. Used in tests to wait for scheduled events,
     * e.g. storing to file.
     */
    void addPropertyChangeListener(PropertyChangeListener listener) {
        PROPERTY_CHANGE_SUPPORT.addPropertyChangeListener(listener);
    }

    /**
     * Remove property change listener added using
     * {@link #addPropertyChangeListener}.
     *
     */
    void removePropertyChangeListener(PropertyChangeListener listener) {
        PROPERTY_CHANGE_SUPPORT.removePropertyChangeListener(listener);
    }

    private class Saver implements Runnable {

        @Override
        public void run() {
            Path tempfile = null;
            try {
                final FileObject targetFileObject = getHistoryRoot(true);
                final Path targetFile = FileUtil.toFile(targetFileObject).toPath();
                tempfile = Files.createTempFile(targetFile.getParent(), SQL_HISTORY_BASE, SQL_HISTORY_EXT);
                try ( OutputStream os = Files.newOutputStream(tempfile)) {
                    XMLStreamWriter xsw = XMLOutputFactory
                            .newInstance()
                            .createXMLStreamWriter(os);

                    xsw.writeStartDocument();
                    xsw.writeCharacters(CONTENT_NEWLINE);
                    xsw.writeStartElement(TAG_HISTORY);
                    xsw.writeCharacters(CONTENT_NEWLINE);
                    for (SQLHistoryEntry sqe : sqlHistory) {
                        xsw.writeStartElement(TAG_SQL);
                        xsw.writeAttribute(ATTR_DATE, sqe.getDateXMLVariant());
                        xsw.writeAttribute(ATTR_URL, sqe.getUrl());
                        xsw.writeCharacters(sqe.getSql());
                        xsw.writeEndElement();
                        xsw.writeCharacters(CONTENT_NEWLINE);
                    }
                    xsw.writeEndElement();
                    xsw.flush();
                    xsw.close();
                }
                Files.move(tempfile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                PROPERTY_CHANGE_SUPPORT.firePropertyChange(PROP_SAVED, null, null);
            } catch (IOException | XMLStreamException ex) {
                LOGGER.log(Level.WARNING, null, ex);
                if(tempfile != null && Files.exists(tempfile)) {
                    try {
                        Files.delete(tempfile);
                    } catch (IOException ex1) {
                        LOGGER.log(Level.INFO, "Failed to cleanup temp file", ex1);
                    }
                }
            }
        }
    }
}
