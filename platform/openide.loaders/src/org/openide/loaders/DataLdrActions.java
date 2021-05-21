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

package org.openide.loaders;


import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

/** Manages actions read and write for a given loader.
 *
 * @author Jaroslav Tulach
 */
final class DataLdrActions extends FolderInstance {
    /** Reference<DataLoader> to know for what loader we work */
    private java.lang.ref.Reference<DataLoader> ref;
    /** last creating task */
    private org.openide.util.Task creation;
    /** processor to use */
    private static org.openide.util.RequestProcessor RP = new org.openide.util.RequestProcessor ("Loader Actions");
    
    public DataLdrActions (DataFolder f, DataLoader l) {
        super (f);
        
        this.ref = new java.lang.ref.WeakReference<DataLoader> (l);
    }
    
    /** Asks the manager to store these actions to disk. Provided for
     * backward compatibility.
     */
    public synchronized void setActions (final SystemAction[] arr) {
        class DoTheWork implements Runnable, FileSystem.AtomicAction {
            private int state;
            
            /** The goal of this method is to make sure that all actions
             * will really be stored on the disk.
             */
            private void work () throws IOException {
                DataObject[] now = folder.getChildren ();
                Map<Object, DataObject> nowToObj = new HashMap<Object, DataObject> ();
                LinkedList<DataObject> sepObjs = new LinkedList<DataObject> ();
                for (int i = 0; i < now.length; i++) {
                    org.openide.cookies.InstanceCookie ic = now[i].getCookie(org.openide.cookies.InstanceCookie.class);

                    if (ic != null) {
                        try {
                            java.lang.Object instance = ic.instanceCreate();

                            if (instance instanceof javax.swing.Action) {
                                nowToObj.put(instance, now[i]);
                                continue;
                            }
                            if (instance instanceof javax.swing.JSeparator) {
                                sepObjs.add(now[i]);
                                continue;
                            }
                        }
                        catch (java.lang.ClassNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                
                ArrayList<DataObject> order = new ArrayList<DataObject> ();
                
                for (int i = 0; i < arr.length; i++) {
                    DataObject obj = nowToObj.remove(arr[i]);
                    if (obj == null) {
                        if (arr[i] != null) {
                            obj = InstanceDataObject.create (folder, null, arr[i].getClass ());
                        } else {
                            if (!sepObjs.isEmpty ()) {
                                obj = sepObjs.removeFirst ();
                            } else {
                                obj = InstanceDataObject.create (folder, "Separator" + order.size (), javax.swing.JSeparator.class);
                            }
                        }
                    }
                    order.add (obj);
                }
                
                // these were there but are not there anymore
                for (DataObject obj: nowToObj.values ()) {
                    obj.delete ();
                }
                for (DataObject obj: sepObjs) {
                    obj.delete ();
                }
                
                folder.setOrder (order.toArray (new DataObject[0]));
            }
            
            public void run () {
                try {
                    switch (state) {
                        case 0:
                            state = 1;
                            folder.getPrimaryFile ().getFileSystem ().runAtomicAction (this);
                            break;
                        case 1:
                            work ();
                            break;
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        DoTheWork dtw = new DoTheWork ();
        creation = RP.post (dtw);
    }
    
    
    /** Creates the actions and notifies the loader.
     */
    @Override
    protected Object createInstance (org.openide.cookies.InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
        ArrayList<javax.swing.Action> list = new ArrayList<javax.swing.Action> ();
        for (int i = 0; i < cookies.length; i++) {
            try {
                Class<?> clazz = cookies[i].instanceClass();
                if (javax.swing.JSeparator.class.isAssignableFrom(clazz)) {
                    list.add(null);
                    continue;
                }
            } catch (ClassNotFoundException cnf) {
                err().log(Level.INFO, "Cannot resolve registration of {0}", cookies[i]); // NOI18N
                err().log(Level.CONFIG, cnf.getMessage(), cnf);
                continue;
            }
            
            Object action = cookies[i].instanceCreate ();
            if (action instanceof javax.swing.Action) {
                list.add ((javax.swing.Action)action);
                continue;
            }
        }
        
        DataLoader l = ref.get ();
        if (l != null) {
            l.setSwingActions (list);
        }
        
        return list.toArray (new javax.swing.Action[0]);
    }

    /** Currently not recursive */
    protected org.openide.cookies.InstanceCookie acceptFolder (DataFolder df) {
        return null;
    }

    /** Creation in our own thread, so we can exclude storage modifications */
    protected org.openide.util.Task postCreationTask (Runnable run) {
        return RP.post (run);
    }
    
    public void waitFinished () {
        org.openide.util.Task t;
        synchronized (this) {
            t = creation;
        }
        
        if (t != null) {
            t.waitFinished ();
        }
        
        super.waitFinished ();
    }
    
}
