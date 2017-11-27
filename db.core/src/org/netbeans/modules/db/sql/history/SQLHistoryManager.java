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
package org.netbeans.modules.db.sql.history;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.netbeans.modules.db.sql.execute.ui.SQLHistoryPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

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
    private static final String SQL_HISTORY_FILE = "sql_history.xml"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(SQLHistoryEntry.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(
            SQLHistoryManager.class.getName(), 1, false, false);
    // Time between call to save and real save - usefull to accumulate before save
    private static final int SAVE_DELAY = 5 * 1000;    
    
    private static SQLHistoryManager _instance = null;    
    
    private final RequestProcessor.Task SAVER = RP.create(new Saver());
    private final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT =
            new PropertyChangeSupport(this);
    
    private JAXBContext context;
    private SQLHistory sqlHistory;

    protected SQLHistoryManager() {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SQLHistoryManager.class.getClassLoader());
        try {
            context = JAXBContext.newInstance("org.netbeans.modules.db.sql.history", SQLHistoryManager.class.getClassLoader());
            loadHistory();
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
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
                if(historyRoot == null) {
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
        try (InputStream is = getHistoryRoot(false).getInputStream()) {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            sqlHistory = (SQLHistory) unmarshaller.unmarshal(is);
            sqlHistory.setHistoryLimit(getListSize());
        } catch (JAXBException | IOException | RuntimeException ex) {
            sqlHistory = new SQLHistory();
            sqlHistory.setHistoryLimit(getListSize());
            LOGGER.log(Level.INFO, ex.getMessage());
        }
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
            try {
                final FileObject targetFile = getHistoryRoot(true);
                targetFile.getFileSystem().
                        runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        OutputStream os = null;
                        try {
                            Marshaller marshaller = context.createMarshaller();
                            os = targetFile.getOutputStream();
                            marshaller.marshal(sqlHistory, os);
                        } catch (JAXBException | IOException | RuntimeException ex) {
                            LOGGER.log(Level.INFO, ex.getMessage(), ex);
                        } finally {
                            try {
                                if (os != null) {
                                    os.close();
                                }
                                PROPERTY_CHANGE_SUPPORT.firePropertyChange(
                                        PROP_SAVED, null, null);
                            } catch (IOException ex) {
                                LOGGER.log(Level.INFO, null, ex);
                            }
                        }
                    }
                });
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, ex.getMessage());
            }
        }
    }
}
