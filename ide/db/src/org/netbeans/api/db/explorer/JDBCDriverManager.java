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

package org.netbeans.api.db.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.db.explorer.dlg.AddDriverDialog;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 * This class manages the list of JDBC drivers registered in the Database Explorer.
 */
public final class JDBCDriverManager {
    
    /**
     * The JDBCDriverManager singleton instance.
     */
    private static JDBCDriverManager DEFAULT = null;
    
    private Lookup.Result<JDBCDriver> result = getLookupResult();
    
    /**
     * The list of listeners.
     */
    private final Set<JDBCDriverListener> listeners = new HashSet<JDBCDriverListener> ();
    
    /**
     * 
     * Gets the JDBCDriverManager singleton instance.
     * 
     * @return the JDBCDriverManager singleton instance.
     */
    public static synchronized JDBCDriverManager getDefault() {
        if (DEFAULT == null) {
            // init runtimes
            DatabaseRuntimeManager.getDefault().getRuntimes();
            DEFAULT = new JDBCDriverManager();
        }
        return DEFAULT;
    }

    /**
     * Private constructor.
     */
    private JDBCDriverManager() {
        // issue 75204: forces the DataObject's corresponding to the JDBCDriver's
        // to be initialized and held strongly so the same JDBCDriver is
        // returns as long as it is held strongly
        result.allInstances(); 

        result.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent e) {
                fireListeners();
            }
        });
    }
    
    /** 
     * Gets the registered JDBC drivers.
     *
     * @return a non-null array of JDBCDriver instances.
     */
    public JDBCDriver[] getDrivers() {
        Collection<? extends JDBCDriver> drivers = result.allInstances();
        return drivers.toArray (new JDBCDriver[0]);
    }
    
    /**
     * Gets the registered JDBC drivers with the specified class name.
     *
     * @param drvClass driver class name; must not be null.
     *
     * @return a non-null array of JDBCDriver instances with the specified class name.
     *
     * @throws NullPointerException if the specified class name is null.
     */
    public JDBCDriver[] getDrivers(String drvClass) {
        if (drvClass == null) {
            throw new NullPointerException();
        }
        LinkedList<JDBCDriver> res = new LinkedList<>();
        JDBCDriver[] drvs = getDrivers();
        for (int i = 0; i < drvs.length; i++) {
            if (drvClass.equals(drvs[i].getClassName())) {
                res.add(drvs[i]);
            }
        }
        return res.toArray (new JDBCDriver[0]);
    }

    /**
     * Adds a new JDBC driver.
     * 
     * @param driver the JDBCDriver instance describing the driver to be added;
     * must not be null.
     *
     * @throws NullPointerException if the specified driver is null.
     *         DatabaseException if an error occurred while adding the driver.
     */
    public void addDriver(JDBCDriver driver) throws DatabaseException {
        if (driver == null) {
            throw new NullPointerException();
        }
        try {
            JDBCDriverConvertor.create(driver);
        } catch (IOException ioe) {
            throw new DatabaseException(ioe);
        }
    }
    
    /**
     * Removes a JDBC driver.
     * 
     * @param driver the JDBCDriver instance to be removed.
     *
     * @throws DatabaseException if an error occurred while adding the driver.
     */
    public void removeDriver(JDBCDriver driver) throws DatabaseException {
        try {
            JDBCDriverConvertor.remove(driver);
        } catch (IOException ioe) {
            throw new DatabaseException(ioe);
        }
    }
    
    /**
     * Shows the Add Driver dialog, allowing the user to add a new JDBC driver.
     */
    public void showAddDriverDialog() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AddDriverDialog.showDialog();
                }
            });
        } else {
            AddDriverDialog.showDialog();
        }
    }

    /**
     * Shows the Add Driver dialog synchronously.  Must be run from the
     * AWT event thread; an IllegalStateException will be thrown if this
     * method is called from any other thread.
     *
     * @return the new driver that was added, or null if the driver was
     *         not successfully created.
     *
     * @throws IllegalStateException if the calling thread is not the event
     *         dispatching thread.
     *
     * @since 1.27
     */
    public JDBCDriver showAddDriverDialogFromEventThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("The current thread is not the event dispatching thread."); // NOI18N
        }
        return AddDriverDialog.showDialog();
    }
    
    /**
     * Registers a JDBCDriverListener.
     */
    public void addDriverListener(JDBCDriverListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Unregisters the specified JDBCDriverListener.
     */
    public void removeDriverListener(JDBCDriverListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void fireListeners() {
        List<JDBCDriverListener> listenersCopy;
        
        synchronized (listeners) {
            listenersCopy = new ArrayList<JDBCDriverListener>(listeners);
        }
        
        for (JDBCDriverListener listener : listenersCopy) {
            listener.driversChanged();
        }
    }
    
    private synchronized Lookup.Result<JDBCDriver> getLookupResult() {
        return Lookups.forPath(JDBCDriverConvertor.DRIVERS_PATH).lookupResult(JDBCDriver.class);
    }
}
