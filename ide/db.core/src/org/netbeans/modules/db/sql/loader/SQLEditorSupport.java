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
package org.netbeans.modules.db.sql.loader;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteCookie;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.netbeans.modules.db.core.SQLCoreUILogger;
import org.netbeans.modules.db.dataview.api.DataViewPageContext;
import org.netbeans.modules.db.sql.execute.SQLExecuteHelper;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.netbeans.modules.db.sql.execute.SQLExecutionResults;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.xml.XMLUtil;

/** 
 * Editor support for SQL data objects. There can be two "kinds" of SQL editors: one for normal
 * DataObjects and one for "console" DataObjects. In the latter case the editor doesn't allow its 
 * contents to be saved explicitly, its name doesn't contain a "*" when it is modified, the respective
 * DataObject is deleted when the editor is closed, and the contents is saved when the editor is 
 * deactivated or upon exiting NetBeans.
 *
 * @author Jesse Beaumont, Andrei Badea
 */
public class SQLEditorSupport extends DataEditorSupport 
        implements OpenCookie, EditCookie, EditorCookie.Observable, 
        PrintCookie, SQLExecuteCookie, CloseCookie {
    
    private static final Logger LOGGER = Logger.getLogger(SQLEditorSupport.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    static final String EDITOR_CONTAINER = "sqlEditorContainer"; // NOI18N
    
    private final PropertyChangeSupport sqlPropChangeSupport = new PropertyChangeSupport(this);
    
    // the RequestProcessor used for executing statements.
    private final RequestProcessor rp = new RequestProcessor("SQLExecution", 1, true); // NOI18N
    
    // the database connection to execute against
    private volatile DatabaseConnection dbconn;
    
    // whether we are executing statements
    private volatile boolean executing;
    
    // execution results. Not synchronized since accessed only from rp of throughput 1.
    private SQLExecutionResults executionResults;
    
    // execution logger
    private SQLExecutionLoggerImpl logger;
    private final Object loggerLock = new Object();
    
    /** 
     * SaveCookie for this support instance. The cookie is adding/removing 
     * data object's cookie set depending on if modification flag was set/unset. 
     */
    private final SaveCookie saveCookie = new SaveCookie() {
        @Override
        public void save() throws IOException {
            saveDocument();
        }
    };
    
    public SQLEditorSupport(SQLDataObject obj) {
        super(obj, null, new Environment(obj));
        setMIMEType(SQLDataLoader.SQL_MIME_TYPE);
        obj.addPropertyChangeListener(
                new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateTitles();
    }
        });
    }
    
    @Override
    protected boolean notifyModified () {
        if (!super.notifyModified()) {
            return false;
        }
        
        if (!isConsole()) {
            // Add the save cookie to the data object
            SQLDataObject obj = (SQLDataObject) getDataObject();
            if (obj.getLookup().lookup(SaveCookie.class) == null) {
                obj.addCookie(saveCookie);
                obj.setModified(true);
            }
        }

        return true;
    }

    @Override
    protected void initializeCloneableEditor(CloneableEditor editor) {
        super.initializeCloneableEditor(editor);
        ((SQLCloneableEditor) editor).initialize();
    }
    
    @Override
    protected Pane createPane() {
        Pane pane;
        if(getDataObject().getPrimaryFile().toURL().toExternalForm().startsWith("nbfs://")) {
            pane = new SQLCloneableEditor(Lookups.fixed(this, getDataObject()));
        } else {
            pane = (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(
                SQLDataLoader.SQL_MIME_TYPE, getDataObject());
        }
        return pane;
    }
    
    @Override
    protected boolean asynchronousOpen() {
        return false;
    }

    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();

        // Remove the save cookie from the data object
        SQLDataObject obj = (SQLDataObject) getDataObject();
        Cookie cookie = obj.getLookup().lookup(SaveCookie.class);
        if (cookie != null && cookie.equals(saveCookie)) {
            obj.removeCookie(saveCookie);
            obj.setModified(false);
        }
    }
    
    @Override
    protected String messageToolTip() {
        if (isConsole()) {
            DatabaseConnection dc = getDatabaseConnection();
            if (dc != null) {
                try {
                    return String.format(
                            "<html>%s<br>%s<br>JDBC-URL: %s</html>",
                            XMLUtil.toAttributeValue(
                            getDataObject().getPrimaryFile().getName()),
                            XMLUtil.toAttributeValue(dc.getDisplayName()),
                            XMLUtil.toAttributeValue(dc.getDatabaseURL()));
                } catch (CharConversionException ex) {
                    LOGGER.log(Level.WARNING, "", ex);
            return getDataObject().getPrimaryFile().getName();
                }
        } else {
                return getDataObject().getPrimaryFile().getName();
            }
        } else {
            return super.messageToolTip();
        }
    }
    
    @Override
    protected String messageName() {
        if (!isValid()) {
            return ""; // NOI18N
        }
        if (isConsole()) {
            if (getDatabaseConnection() != null) {
                String connectionName = getDatabaseConnection().getDisplayName();
                if (connectionName.length() > 25) {
                    connectionName = connectionName.substring(0, 25) + "\u2026";
                }
                return NbBundle.getMessage(SQLEditorSupport.class, "LBL_ConsoleWithConnection",
                    getDataObject().getName(),
                    connectionName);
            }
            return NbBundle.getMessage(SQLEditorSupport.class, "LBL_Console",
                    getDataObject().getName());
        } else {
            return super.messageName();
        }
    }
    
    @Override
    protected String messageHtmlName() {
        if (!isValid()) {
            return ""; // NOI18N
        }
        if (isConsole()) {
            // just the name, no modified or r/o flags
            String name = messageName();
            if (name != null) {
                if (!name.startsWith("<html>")) { // NOI18N
                    name = "<html>" + name; // NOI18N
                }
            }
            return name;
        } else {
            return super.messageHtmlName();
        }
    }
    
    @Override
    protected void notifyClosed() {
        super.notifyClosed();
        
        closeExecutionResult();
        closeLogger();
        
        if (isConsole() && isValid()) {
            try {
                getDataObject().delete();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    @Override
    protected boolean canClose() {
        if (isConsole()) {
            return true;
        } else {
            return super.canClose();
        }
    }
    
    boolean isConsole() {
        return ((SQLDataObject) getDataObject()).isConsole();
    }
    
    boolean isValid() {
        return getDataObject().isValid();
    }
    
    @Override
    protected Component wrapEditorComponent(Component editor) {
        JPanel container = new JPanel(new BorderLayout());
        container.setName(EDITOR_CONTAINER); // NOI18N
        container.add(editor, BorderLayout.CENTER);
        return container;
    }
    
    @Override
    public void open() {
        SQLCoreUILogger.logEditorOpened();
        super.open();
    }
    
    @Override
    public void edit() {
        SQLCoreUILogger.logEditorOpened();
        super.edit();
    }
    
    void addSQLPropertyChangeListener(PropertyChangeListener listener) {
        sqlPropChangeSupport.addPropertyChangeListener(listener);
    }
    
    void removeSQLPropertyChangeListener(PropertyChangeListener listener) {
        sqlPropChangeSupport.removePropertyChangeListener(listener);
    }
    
    @Override
    public void setDatabaseConnection(DatabaseConnection dbconn) {
        DatabaseConnection oldCon = this.dbconn;
        this.dbconn = dbconn;
        sqlPropChangeSupport.firePropertyChange(
                SQLExecution.PROP_DATABASE_CONNECTION, oldCon, dbconn);
        updateTitles();
    }
    
    @Override
    public DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }

    @Override
    public void execute() {
        Document doc = getDocument();
        if (doc == null) {
            return;
        }
        String sql;
        try {
            sql = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            // should not happen
            Logger.getLogger("global").log(Level.INFO, null, e);
            sql = ""; // NOI18N
        }
        execute(sql, 0, sql.length(), null);
    }

    @Override
    public void saveAs(FileObject folder, String fileName) throws IOException {
        String fn = FileUtil.getFileDisplayName(folder) + File.separator + fileName; 
        File existingFile = FileUtil.normalizeFile(new File(fn));
        if (existingFile.exists()) {
            NotifyDescriptor confirm = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(SQLEditorSupport.class,
                    "MSG_ConfirmReplace", fileName),
                    NbBundle.getMessage(SQLEditorSupport.class,
                    "MSG_ConfirmReplaceFileTitle"),
                    NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(confirm);
            if (!confirm.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                return;
            }
        }
        if (isConsole()) {
            // #166370 - if console, need to save document before copying
            saveDocument();
        }
        super.saveAs(folder, fileName);
    }

    /**
     * Executes either all or a part of the given sql string (which can contain
     * zero or more SQL statements). If startOffset &lt; endOffset, the part of
     * sql specified is executed. If startOffset == endOffset, the statement
     * containing the character at startOffset, if any, is executed.
     *
     * @param sql the SQL string to execute. If it contains multiple lines they 
     * have to be delimited by \n.
     */
    void execute(String sql, int startOffset, int endOffset, SQLCloneableEditor editor) {
        DatabaseConnection conn = this.dbconn;
        if (conn == null) {
            return;
        }
        SQLExecutor executor = new SQLExecutor(this, conn, sql, startOffset, endOffset, editor);
        final RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
        if ((sql.toUpperCase().indexOf("CREATE") != -1) || (sql.toUpperCase().indexOf("DROP") != -1)) { // NOI18N
            task.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(Task task) {
                    task.removeTaskListener(this);
                    refresh();
                }
            });
        }
    }
    
    boolean isExecuting() {
        return executing;
    }
    
    private void setExecuting(boolean executing) {
        boolean oldExecuting = this.executing;
        this.executing = executing;
        sqlPropChangeSupport.firePropertyChange(SQLExecution.PROP_EXECUTING, oldExecuting, this.executing);
    }
    
    private void setResultsToEditors(final SQLExecutionResults results, final SQLCloneableEditor editor) {
       Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                List<Component> components = null;
                
                if (results != null) {
                    components = new ArrayList<Component>();

                    for (SQLExecutionResult result : results.getResults()) {
                        for(Component component : result.getDataView().createComponents()){
                            components.add(component);
                        }
                    }
                }
                
                if (editor != null) {
                    editor.setResults(components);
                } else {
                Enumeration editors = allEditors.getComponents();
                while (editors.hasMoreElements()) {
                    CloneableTopComponent editor = (CloneableTopComponent) editors.nextElement();
                    SQLCloneableEditor ce = editor.getLookup().lookup(SQLCloneableEditor.class);
                    if (ce != null) {
                        ce.setResults(components);
                    }
                }
            }
            }
        });
    }
    
    private void setExecutionResults(SQLExecutionResults executionResults) {
        this.executionResults = executionResults;
    }
    
    private void closeExecutionResult() {
        setResultsToEditors(null, null);
        
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (executionResults != null) {
                    executionResults = null;
                }
            }
        };
        
        // need to run the Runnable in the request processor
        // since it makes JDBC calls, possibly blocking
        // the calling thread
        
        // closeExceptionResult is sometimes called in the RP,
        // e.g. while executing statements
        if (rp.isRequestProcessorThread()) {
            run.run();
        } else {
            rp.post(run);
        }
    }
    
    private void refresh() {
        if (dbconn == null) {
            return;
        }
        ConnectionManager.getDefault().refreshConnectionInExplorer(dbconn);
    }
    
    private SQLExecutionLoggerImpl getOrCreateLogger() {
        synchronized (loggerLock) {
            if (logger == null) {
                String loggerDisplayName;
                if (isConsole()) {
                    loggerDisplayName = getDataObject().getName();
                } else {
                    loggerDisplayName = getDataObject().getNodeDelegate().getDisplayName();
                }
                logger = new SQLExecutionLoggerImpl(loggerDisplayName, this);
            }

            return logger;
        }
    }
    
    private void closeLogger() {
        synchronized (loggerLock) {
            if (logger != null) {
                logger.close();
                logger = null;
            }
        }
    }

    private static final class SQLExecutor implements Runnable, Cancellable {
        private final SQLCloneableEditor editor;
        private final SQLEditorSupport parent;

        // the connections which the statements are executed against
        private final DatabaseConnection dbconn;
        
        // the currently executed statement(s)
        private final String sql;
        
        private final int startOffset, endOffset;
        
        // the task representing the execution of statements
        private RequestProcessor.Task task;
        
        public SQLExecutor(SQLEditorSupport parent, DatabaseConnection dbconn, String sql, int startOffset, int endOffset, SQLCloneableEditor editor) {
            assert parent != null;
            assert dbconn != null;
            assert sql != null;
            
            this.parent = parent;
            this.dbconn = dbconn;
            this.sql = sql;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.editor = editor;
        }
        
        public void setTask(RequestProcessor.Task task) {
            this.task = task;
        }
        
        @Override
        public void run() {
            assert task != null : "Should have called setTask()"; // NOI18N
            
            parent.setExecuting(true);
            try {
                if (LOG) {
                    LOGGER.log(Level.FINE, "Started the SQL execution task"); // NOI18N
                    LOGGER.log(Level.FINE, "Executing against " + dbconn); // NOI18N
                }

                Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
                    @Override
                    public Void run() {
                        ConnectionManager.getDefault().showConnectionDialog(dbconn);
                        return null;
                    }
                });

                Connection conn = dbconn.getJDBCConnection();
                if (LOG) {
                    LOGGER.log(Level.FINE, "SQL connection: " + conn); // NOI18N
                }
                if (conn == null) {
                    return;
                }

                // need to save the document, otherwise the Line.Set.getOriginal mechanism does not work
                try {
                    Mutex.EVENT.readAccess(new Mutex.ExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            parent.saveDocument();
                            return null;
                        }
                    });
                } catch (MutexException e) {
                    Exceptions.printStackTrace(e.getException());
                    return;
                }

                ProgressHandle handle = ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(SQLEditorSupport.class,
                        "LBL_ExecutingStatements"), this);
                handle.start();
                try {
                    handle.switchToIndeterminate();

                    setStatusText(""); // NOI18N

                    if (LOG) {
                        LOGGER.log(Level.FINE,
                                "Closing the old execution result"); // NOI18N
                    }
                    int pageSize = -1;
                    if (parent.executionResults != null && parent.executionResults.size() > 0) {
                        for (SQLExecutionResult res : parent.executionResults.getResults()) {
                            int ps = DataViewPageContext.getPageSize(res.getDataView());
                            pageSize = (pageSize < ps ? ps : pageSize);
                        }
                    }
                    if (pageSize == -1) {
                        pageSize = DataViewPageContext.getStoredPageSize();
                    }
                    parent.closeExecutionResult();

                    SQLExecutionLoggerImpl logger = parent.getOrCreateLogger();
                    SQLExecutionResults executionResults = SQLExecuteHelper.execute(
                            sql, startOffset, endOffset, dbconn, logger, pageSize);
                    handleExecutionResults(executionResults, logger);
                } finally {
                    handle.finish();
                }
            } finally {
                parent.setExecuting(false);
            }
        }
        
        private void handleExecutionResults(SQLExecutionResults executionResults, SQLExecutionLoggerImpl logger) {
            if (executionResults == null) {
                // execution cancelled
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class,
                        "LBL_ExecutionCancelled"));
                return;
            }

            parent.setExecutionResults(executionResults);
            
            if (executionResults.size() <= 0) {
                // no results, but successfull
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class,
                        "LBL_ExecutedSuccessfully"));
                return;
            }

            parent.setResultsToEditors(executionResults, editor);

            if (executionResults.hasExceptions()) {
                // there was at least one exception
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class,
                        "LBL_ExecutionFinishedWithErrors"));
            } else {
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class,
                        "LBL_ExecutedSuccessfully"));
            }
        }
        
        private void setStatusText(String statusText) {
            StatusDisplayer.getDefault().setStatusText(statusText);
        }
        
        @Override
        public boolean cancel() {
            return task.cancel();
        }
    }

    /** 
     * Environment for this support. Ensures that
     * getDataObject().setModified(true) is not called if this support's editor
     * was opened as a console.
     */
    static final class Environment extends DataEditorSupport.Env {

        public static final long serialVersionUID = 7968926994844480435L;

        private transient boolean modified = false;

        private transient FileLock fileLock;

        public Environment(SQLDataObject obj) {
            super(obj);
        }

        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        @Override
        protected FileLock takeLock() throws IOException {
            MultiDataObject obj = (MultiDataObject) getDataObject();
            fileLock = obj.getPrimaryEntry().takeLock();
            return fileLock;
        }

        @Override
        public void markModified() throws IOException {
            if (findSQLEditorSupport().isConsole()) {
                modified = true;
            } else {
                super.markModified();
            }
        }

        @Override
        public void unmarkModified() {
            if (findSQLEditorSupport().isConsole()) {
                modified = false;
                if (fileLock != null && fileLock.isValid()) {
                    fileLock.releaseLock();
                }
            } else {
                super.unmarkModified();
            }
        }

        @Override
        public boolean isModified() {
            if (findSQLEditorSupport().isConsole()) {
                return modified;
            } else {
                return super.isModified();
            }
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return findSQLEditorSupport();
        }

        private SQLEditorSupport findSQLEditorSupport() {
            return getDataObject().getLookup().lookup(SQLEditorSupport.class);
        }
    }
}
