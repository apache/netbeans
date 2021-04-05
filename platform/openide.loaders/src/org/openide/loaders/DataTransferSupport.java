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
/*
 * DataTransferSupport.java
 *
 * Created on June 18, 2001, 1:26 PM
 */

package org.openide.loaders;


import java.awt.datatransfer.Transferable;
import java.io.*;
import java.util.Arrays;
import java.util.logging.*;
import org.openide.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.datatransfer.*;

/** Support for data transfer Paste operation.
 * @author  Vita Stejskal
 */
abstract class DataTransferSupport {

    /** Defines array of classes implementing paste for specified clipboard operation.
     * @param op clopboard operation to specify paste types for
     * @return array of classes extending PasteTypeExt class
     */
    protected abstract PasteTypeExt [] definePasteTypes (int op);
    /** Defines array of data clipboard operations recognized by this paste support.
     * @return array of DataFlavors
     */
    protected abstract int [] defineOperations ();
    /** Override in order to support additional paste types.
     * @param t clipboard Transferable object, list of transfered DataObjects with their flavors
     * @param s list of paste types supported for transfered objects
     */
    protected void handleCreatePasteTypes (Transferable t, java.util.List<PasteType> s) {
    }
    /** Fills in the list of paste types available for given set to transfered
     * DataObjects.
     * @param t clipboard Transferable object, list of transfered DataObjects with their flavors
     * @param s list of paste types supported for transfered objects
     */
    public final void createPasteTypes (Transferable t, java.util.List<PasteType> s) {
        /** All supported operations. */
        int [] ops = defineOperations ();

        for (int i = 0; i < ops.length; i++) {
            DataObject objs [] = LoaderTransfer.getDataObjects (t, ops[i]);
            PasteTypeExt pts [];

            if (objs == null || objs.length == 0)
                continue;

            pts = definePasteTypes (ops[i]);

            for (int j = 0; j < pts.length; j++) {
                pts[j].setDataObjects (objs);
                if (pts[j].canPaste ())
                    s.add (pts[j]);
            }
        }

        handleCreatePasteTypes (t, s);
    }
    
    private static final Logger err = Logger.getLogger("org.openide.loaders.DataTransferSupport"); //NOI18N
    
    /** Supports paste of multiple DataObject at once.
     */
    static abstract class PasteTypeExt extends PasteType {
        private static final RequestProcessor RP = new RequestProcessor("Paste Support"); // NOI18N
        
        /** All DataObjects being pasted. */
        private DataObject objs [];
        /** Create paste type. */
        public PasteTypeExt () {
        }
        /** Can DataObject be pasted.
         * @param obj DataObject to be pasted
         * @return result of the test
         */
        protected abstract boolean handleCanPaste (DataObject obj);
        /** Handles the paste action
        * @param obj pasted DataObject
        */
        protected abstract void handlePaste (DataObject obj) throws IOException;
        /** Could be clipboard clean up after the paste operation is finished or
         * should its content be preserved.
         * @return default implementation returns <code>false</code>
         */
        protected boolean cleanClipboard () {
            return false;
        }
        /** Paste all DataObjects */
        public final boolean canPaste () {
            for (int i = 0; i < objs.length; i++) {
                if (!handleCanPaste (objs[i]))
                    return false;
            }
            return true;
        }
        /** Paste all DataObjects */
        @Override
        public final Transferable paste() throws IOException {
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                RP.post(new java.lang.Runnable() {
                    @Override
                    public void run() {
                        java.lang.String n = org.openide.awt.Actions.cutAmpersand(getName());
                        org.netbeans.api.progress.ProgressHandle h = org.netbeans.api.progress.ProgressHandleFactory.createHandle(n);

                        h.start();
                        h.switchToIndeterminate();
                        try {
                            doPaste();
                        } catch (java.io.IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        } finally {
                            h.finish();
                        }
                    }
                });
            } else {
                doPaste();
            }
            // clear clipboard or preserve content
            return cleanClipboard() ? ExTransferable.EMPTY : null;
        }
        
        private void doPaste () throws IOException {
	    if (err.isLoggable (Level.FINE)) {
		err.log(Level.FINE, null, new Throwable ("Issue #58666: Called " + this + " doPaste() on objects " + Arrays.asList (objs))); // NOI18N
	    }
            for (int i = 0; i < objs.length; i++)
                handlePaste (objs[i]);
        }
	
        public final void setDataObjects (DataObject objs []) {
            this.objs = objs;
        }
    }

    /** Paste types for data objects.
    */
    static class SerializePaste extends PasteType {
        private InstanceCookie cookie;
        private DataFolder target;
        
        /**
        * @param obj object to work with
        */
        public SerializePaste (DataFolder target, InstanceCookie cookie) {
            this.cookie = cookie;
            this.target = target;
        }

        /** The name is obtained from the bundle.
        * @return the name
        */
        public String getName () {
            return DataObject.getString ("PT_serialize");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (SerializePaste.class);
        }

        /** Paste.
        */
        public final Transferable paste () throws IOException {
            final DataFolder trg = getTargetFolder();
            String name = cookie.instanceName ();
            int i = name.lastIndexOf ('.') + 1;
            if (i != 0 && i != name.length ()) {
                name = name.substring (i);
            }

            name = FileUtil.findFreeFileName (trg.getPrimaryFile (), name, "ser"); // NOI18N


            final NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine (
                                                      DataObject.getString ("SerializeBean_Text"),
                                                      DataObject.getString ("SerializeBean_Title")
                                                  );
            nd.setInputText (name);

            if (NotifyDescriptor.OK_OPTION == DialogDisplayer.getDefault ().notify (nd)) {
                DataObjectPool.getPOOL().runAtomicAction (trg.getPrimaryFile (), new FileSystem.AtomicAction () {
                            public void run () throws IOException {
                                FileObject fo = trg.getPrimaryFile ().createData (nd.getInputText (), "ser"); // NOI18N
                                FileLock lock = fo.lock ();
                                ObjectOutputStream oos = null;
                                try {
                                    oos = new ObjectOutputStream (
                                              new java.io.BufferedOutputStream (fo.getOutputStream (lock))
                                          );
                                    oos.writeObject (cookie.instanceCreate ());
                                } catch (ClassNotFoundException e) {
                                    throw new IOException (e.getMessage ());
                                } finally {
                                    if (oos != null) oos.close ();
                                    lock.releaseLock ();
                                }
                            }
                        });
            }

            // preserve clipboard
            return null;
        }

        protected DataFolder getTargetFolder() throws IOException {
            return target;
        }
    }

    /** Paste types for data objects.
    */
    static class InstantiatePaste extends PasteType {
        private InstanceCookie cookie;
        private DataFolder target;
        
        /**
        * @param obj object to work with
        */
        public InstantiatePaste (DataFolder target, InstanceCookie cookie) {
            this.cookie = cookie;
            this.target = target;
        }

        /** The name is obtained from the bundle.
        * @return the name
        */
        public String getName () {
            return DataObject.getString ("PT_instance");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (InstantiatePaste.class);
        }

        /** Paste.
        */
        public final Transferable paste () throws IOException {
            try {
                Class<?> clazz = cookie.instanceClass ();
                
                // create the instance
                InstanceDataObject.create(getTargetFolder(), null, clazz);
            } catch (ClassNotFoundException ex) {
                throw new IOException (ex.getMessage ());
            }

            // preserve clipboard
            return null;
        }

        protected DataFolder getTargetFolder() throws IOException {
            return target;
        }
    }
}
