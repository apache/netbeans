/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        return drivers.toArray (new JDBCDriver[drivers.size ()]);
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
        return res.toArray (new JDBCDriver[res.size ()]);
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
