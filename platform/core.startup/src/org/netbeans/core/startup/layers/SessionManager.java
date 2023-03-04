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

package org.netbeans.core.startup.layers;

import java.util.List;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.netbeans.core.startup.layers.LocalFileSystemEx;
import org.netbeans.core.startup.layers.SystemFileSystem;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/** Session manager.
 *
 * @author  Jan Pokorsky
 */
public final class SessionManager {
    /** session is opened */
    public static final String PROP_OPEN = "session_open"; // NOI18N
    /** session is closed */
    public static final String PROP_CLOSE = "session_close"; // NOI18N
    /** session layer */
    public static final String LAYER_SESSION = "session"; // NOI18N
    /** instalation layer */
    public static final String LAYER_INSTALL = "install"; // NOI18N
    
    private static SessionManager sm = null;
    /** default system filesystem */
    private SystemFileSystem systemFS;
    private HashMap<String,FileSystem> layers = new HashMap<String,FileSystem>(); //<layer_key, fs>
    
    /** Utility field holding list of PropertyChangeListeners. */
    private transient List<PropertyChangeListener> propertyChangeListeners;
    
    /** Creates new SessionManager */
    private SessionManager() {
    }
    
    /** get default one */
    public static SessionManager getDefault() {
        if (sm == null) {
            sm = new SessionManager();
        }
        return sm;
    }
    
    /** Initializes and creates new repository. This repository's system fs is
    * based on the content of ${HOME_DIR}/system and ${USER_DIR}/system directories
    *
    * @param userDir directory where user can write 
    * @param homeDir directory where netbeans has been installed, user need not have write access
    * @param extradirs 0+ extra dirs to add; cf. #27151
    * @return repository
    * @exception PropertyVetoException if something fails
    */
    public FileSystem create(File userDir, File homeDir, File[] extradirs)
    throws java.beans.PropertyVetoException, IOException {
        systemFS = SystemFileSystem.create(userDir, homeDir, extradirs);
        layers.put(LAYER_INSTALL, systemFS.getInstallationLayer());
        layers.put(LAYER_SESSION, systemFS.getUserLayer());
        return systemFS;
    }
    
    /** Close session */
    public void close() {
        firePropertyChange(PROP_CLOSE);
        waitForLocks ();
    }
    
    /** get a layer associated with the name
     * @param name layer name (LAYER_SESSION, ...)
     * @return layer, can be <code>null</null>
     */
    public FileSystem getLayer(String name) {
        return layers.get(name);
    }

    /** Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListeners == null ) {
            propertyChangeListeners = new ArrayList<>();
        }
        propertyChangeListeners.add(listener);
    }
    
    /** Removes PropertyChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        if (propertyChangeListeners != null ) {
            propertyChangeListeners.remove(listener);
        }
    }
    
    /** Notifies all registered listeners about the event.
     * @param name the name to be fired
     */
    private void firePropertyChange(String name) {
        List<PropertyChangeListener> list;
        synchronized (this) {
            if (propertyChangeListeners == null || propertyChangeListeners.size() == 0) return;
            list = new ArrayList<>(propertyChangeListeners);
        }
        java.beans.PropertyChangeEvent event = new java.beans.PropertyChangeEvent(this, name, null, null);
        for (int i = 0; i < list.size(); i++) {
            try {
                list.get(i).propertyChange(event);
            }
            catch (RuntimeException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private void waitForLocks () {
        int count = 50; // 5 secs.
        
        try {
            while (LocalFileSystemEx.hasLocks () && 0 < count) {
                Thread.sleep(100);
                count--;
            }
        } catch (InterruptedException e) {
            // ignore
        }
        
        if (LocalFileSystemEx.hasLocks ()) {
//            new Throwable ("SessionManager.waitForLocks callers thread.").printStackTrace ();

            // timed out!
            String locks [] = LocalFileSystemEx.getLocks ();
            StringBuilder msg = new StringBuilder (256);
            msg.append ("Settings saving "); //NOI18N
            msg.append (count == 0 ? "timeout!" : "interrupted!"); //NOI18N
            msg.append ("\nList of pending locks:\n"); //NOI18N
            for (int i = 0; i < locks.length; i++) {
                msg.append (locks[i]);
                msg.append ("\n"); //NOI18N
/*                
                Throwable source = LocalFileSystemEx.getLockSource (locks[i]);
                if (source != null) {
                    StringWriter sw = new StringWriter (1024);
                    PrintWriter w = new PrintWriter (sw);
                    source.printStackTrace (w);
                    w.close ();

                    msg.append (sw.getBuffer ());
                    msg.append ("\n"); //NOI18N
                }
 */
            }
            System.err.println(msg.toString ());
        }
    }
}
