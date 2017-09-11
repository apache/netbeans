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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.startup.layers;

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
    private transient java.util.ArrayList<PropertyChangeListener> propertyChangeListeners;
    
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
            propertyChangeListeners = new java.util.ArrayList<PropertyChangeListener>();
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
        java.util.ArrayList list;
        synchronized (this) {
            if (propertyChangeListeners == null || propertyChangeListeners.size() == 0) return;
            list = (java.util.ArrayList)propertyChangeListeners.clone();
        }
        java.beans.PropertyChangeEvent event = new java.beans.PropertyChangeEvent(this, name, null, null);
        for (int i = 0; i < list.size(); i++) {
            try {
                ((PropertyChangeListener) list.get(i)).propertyChange(event);
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
