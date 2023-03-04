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

package org.openide.actions;


import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.LifecycleManager;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/** Save all open objects.
* @see DataObject#getRegistry
* @see LifecycleManager#saveAll
*
* @author   Jan Jancura, Ian Formanek
*/
public final class SaveAllAction extends CallableSystemAction {

    public SaveAllAction () {
        // listen to the changes
        chl = new ModifiedListL();
        DataObject.getRegistry().addChangeListener(
            (org.openide.util.WeakListeners.change(chl, DataObject.getRegistry())));
    }

    static final long serialVersionUID = 333L;

    /** to make sure only one instance of this class can run at a time */
    private static final Object RUNNING = new Object ();

    /** Reference to the change listener
    * (we treat it weakly, so we have to to prevent it from
    * being finalized before finalization of this action) */
    private ChangeListener chl;

    /* Creates new HashMap and inserts some properties to it.
    * @return the hash map
    */
    @Override
    protected void initialize () {
        super.initialize ();
        // false by default
        putProperty (PROP_ENABLED, Boolean.FALSE);
        // default tooltip  warning about CoS feature #148977
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(
             org.openide.loaders.DataObject.class, "HINT_SaveAll"));
        // listen to the changes
        chl = new ModifiedListL();
        DataObject.getRegistry().addChangeListener(
            (org.openide.util.WeakListeners.change(chl, DataObject.getRegistry())));
    }

    public String getName() {
        return NbBundle.getMessage(org.openide.loaders.DataObject.class, "SaveAll");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (SaveAllAction.class);
    }

    @Override
    protected String iconResource () {
        return "org/openide/loaders/saveAll.gif"; // NOI18N
    }

    public void performAction() {
        synchronized (RUNNING) {
            while (getProperty (RUNNING) != null) {
                try {
                    RUNNING.wait ();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            putProperty (RUNNING, RUNNING);
        }
        try {
            LifecycleManager.getDefault().saveAll();
        } finally {
            synchronized (RUNNING) {
                putProperty (RUNNING, null);
                RUNNING.notifyAll ();
            }
            
        }
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    /* Listens to the chnages in list of modified data objects
    * and enables / disables this action appropriately */
    final class ModifiedListL implements ChangeListener {
        public void stateChanged(final ChangeEvent evt) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    setEnabled(DataObject.getRegistry().getModifiedSet().size() > 0);
                }
            });
        }
    } // end of ModifiedListL inner class
}
