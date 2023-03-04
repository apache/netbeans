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

package org.netbeans.modules.db.explorer.dlg;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Base class for the connection dialogs.
 *
 * @author Andrei Badea
 */
public abstract class ConnectionDialogMediator {
    
    public static final String PROP_VALID = "valid"; // NOI18N
    
    private final List<ConnectionProgressListener> connProgressListeners = new ArrayList<>();
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    
    private boolean valid = true;
    
    public void addConnectionProgressListener(ConnectionProgressListener listener) {
        synchronized (connProgressListeners) {
            connProgressListeners.add(listener);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void closeConnection()
    {
    }
    
    protected abstract boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema);

    /**
     * An async version of retrieveSchemas.
     * 
     * @param schemaPanel the schema panel
     * @param dbcon the db connection
     * @param defaultSchema the name of the default schema
     * 
     * @return the Task instance passed to the Requestprocessor
     */
    protected Task retrieveSchemasAsync(final SchemaPanel schemaPanel, final DatabaseConnection dbcon, final String defaultSchema)
    {
        fireConnectionStarted();
        
        Task task = RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                retrieveSchemas(schemaPanel, dbcon, defaultSchema);
                fireConnectionFinished();
            }
        });

        return task;
    }

    
    protected void fireConnectionStarted() {
        for (Iterator<ConnectionProgressListener> i = connProgressListenersCopy(); i.hasNext();) {
            i.next().connectionStarted();
        }
    }
    
    protected void fireConnectionStep(String step) {
        for (Iterator<ConnectionProgressListener> i = connProgressListenersCopy(); i.hasNext();) {
            i.next().connectionStep(step);
        }
    }
    
    protected void fireConnectionFinished() {
        for (Iterator<ConnectionProgressListener> i = connProgressListenersCopy(); i.hasNext();) {
            i.next().connectionFinished();
        }
    }

    protected void fireConnectionFailed() {
        for (Iterator<ConnectionProgressListener> i = connProgressListenersCopy(); i.hasNext();) {
            i.next().connectionFailed();
        }
    }
    
    private Iterator<ConnectionProgressListener> connProgressListenersCopy() {
        List<ConnectionProgressListener> listenersCopy = null;
        synchronized (connProgressListeners) {
            listenersCopy = new ArrayList<>(connProgressListeners);
        }
        return listenersCopy.iterator();
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
        propChangeSupport.firePropertyChange(PROP_VALID, null, null);
    }
    
    public boolean getValid() {
        return valid;
    }
}
