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

package org.openide.util.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import javax.swing.event.EventListenerList;
import org.openide.util.NbBundle;

/** Provides additional operations on
* a transferable.
*
* @author Jaroslav Tulach
*/
public class ExTransferable extends Object implements Transferable {
    /** An implementation of <code>Transferable</code> that contains no data. */
    public static final Transferable EMPTY = new Empty();

    /** Flavor for transfer of multiple objects.
    */
    public static final DataFlavor multiFlavor;
    static {
        try {
            multiFlavor = new DataFlavor(
                    "application/x-java-openide-multinode;class=org.openide.util.datatransfer.MultiTransferObject", // NOI18N
                    NbBundle.getBundle(ExTransferable.class).getString("transferFlavorsMultiFlavorName"),
                    MultiTransferObject.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    /** hash map that assigns objects to dataflavors (DataFlavor, Single) */
    private LinkedHashMap<DataFlavor,Single> map;

    /** listeners */
    private EventListenerList listeners;

    /** Creates new support.
    * @param t transferable to to copy values from
    * @param o clipobard owner (or null)
    */
    private ExTransferable(final Transferable t) {
        map = new LinkedHashMap<DataFlavor,Single>();

        final DataFlavor[] df = t.getTransferDataFlavors();

        if (df != null) {
            for (int i = 0; i < df.length; i++) {
                try {
                    final int fi = i;
                    map.put(
                        df[i],
                        new Single(df[i]) {
                            public Object getData() throws IOException, UnsupportedFlavorException {
                                return t.getTransferData(df[fi]);
                            }
                        }
                    );
                } catch (Exception ex) {
                    // ignore if the data cannot be retrived
                }
            }
        }
    }

    /** Add a new flavor with its data.
    * @param single the single transferable to use
    */
    public void put(Single single) {
        map.put(single.flavor, single);
    }

    /** Remove a flavor from the supported set.
     * @param flavor the flavor to remove
    */
    public void remove(DataFlavor flavor) {
        map.remove(flavor);
    }

    /* Get supported flavors.
     * @return the flavors
    */
    public DataFlavor[] getTransferDataFlavors() {
        return map.keySet().toArray(new DataFlavor[0]);
    }

    /* Is this flavor supported?
    * @param flavor flavor to test
    * @return <code>true</code> if this flavor is supported
    */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return map.containsKey(flavor);
    }

    /* Get the transferable data for this flavor.
     * @param flavor the flavor
     * @return the data
     * @throws IOException currently not thrown
     * @throws UnsupportedFlavorException if that flavor is not supported
    */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        Single o = map.get(flavor);

        if (o == null) {
            throw new UnsupportedFlavorException(flavor);
        }

        return o.getTransferData(flavor);
    }

    /** Method to create a new extended transferable from a plain transferable.
    * If the given transferable is already <code>ExTransferable</code>, then it
    * is returned as is.
    * Otherwise the data is copied.
    *
    * @param t transferable to create support for
    * @return extended transferable
    */
    public static ExTransferable create(Transferable t) {
        // [PENDING] check should probably be: if (t.getClass() == ExTransferable.class)
        // (in case for some weird reason someone subclasses ExTransferable)
        if (t instanceof ExTransferable) {
            return (ExTransferable) t;
        }

        return new ExTransferable(t);
    }

    /** Adds a listener to watch the life-cycle of this object.
    *
    * @param l the listener
    */
    public final synchronized void addTransferListener(TransferListener l) {
        if (listeners == null) {
            listeners = new EventListenerList();
        }

        listeners.add(TransferListener.class, l);
    }

    /** Removes a listener.
    */
    public final synchronized void removeTransferListener(TransferListener l) {
        if (listeners != null) {
            listeners.remove(TransferListener.class, l);
        }
    }

    /** Fires notification to all listeners about
    * accepting the drag.
    * @param action one of java.awt.dnd.DnDConstants.ACTION_*
    */
    final void fireAccepted(int action) {
        if (listeners == null) {
            return;
        }

        Object[] arr = listeners.getListenerList();

        for (int i = arr.length - 1; i >= 0; i -= 2) {
            ((TransferListener) arr[i]).accepted(action);
        }
    }

    /** Fires notification to all listeners about
    * accepting the drag.
    */
    final void fireRejected() {
        if (listeners == null) {
            return;
        }

        Object[] arr = listeners.getListenerList();

        for (int i = arr.length - 1; i >= 0; i -= 2) {
            ((TransferListener) arr[i]).rejected();
        }
    }

    /** Fires notification to all listeners about
    * accepting the drag.
    */
    final void fireOwnershipLost() {
        if (listeners == null) {
            return;
        }

        Object[] arr = listeners.getListenerList();

        for (int i = arr.length - 1; i >= 0; i -= 2) {
            ((TransferListener) arr[i]).ownershipLost();
        }
    }

    /** Support for transferable owner with only one data flavor.
    * Subclasses need only implement {@link #getData}.
    */
    public abstract static class Single extends Object implements Transferable {
        /** the supported data flavor */
        private DataFlavor flavor;

        /** Constructor.
        * @param flavor flavor of the data
        */
        public Single(DataFlavor flavor) {
            this.flavor = flavor;
        }

        /* Flavors that are supported.
        * @return array with <CODE>contextFlavor</CODE>
        * @see TransferFlavors.contextFlavor
        */
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { flavor };
        }

        /* Is the flavor supported?
        * @param flavor flavor to test
        * @return true if this flavor is supported
        */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return this.flavor.equals(flavor);
        }

        /* Creates transferable data for this flavor.
        */
        public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
            if (!this.flavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }

            return getData();
        }

        /** Abstract method to override to provide the right data for this
        * transferable.
        *
        * @return the data
        * @throws IOException when an I/O error occurs
        * @throws UnsupportedFlavorException if the flavor is not supported
        */
        protected abstract Object getData() throws IOException, UnsupportedFlavorException;
    }

    /** Transferable object for multiple transfer.
     * It allows several types of data
    * to be combined into one clipboard element.
    *
    * @author Jaroslav Tulach
    */
    public static class Multi extends Object implements Transferable {
        /** supported flavors list */
        private static final DataFlavor[] flavorList = { multiFlavor };

        /** object that is about to be return as result of transfer */
        private MultiTransferObject transferObject;

        /** Constructor taking a list of <code>Transferable</code> objects.
         *
         * @param trans array of transferable objects
         */
        public Multi(Transferable[] trans) {
            transferObject = new TransferObjectImpl(trans);
        }

        /** Get supported flavors.
         * @return only one flavor, {@link #multiFlavor}
        */
        public DataFlavor[] getTransferDataFlavors() {
            return flavorList;
        }

        /** Is this flavor supported?
         * @param flavor the flavor
        * @return <code>true</code> only if the flavor is {@link #multiFlavor}
        */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(multiFlavor);
        }

        /** Get transfer data.
         * @param flavor the flavor ({@link #multiFlavor})
        * @return {@link MultiTransferObject} that represents data in this object
        * @exception UnsupportedFlavorException when the flavor is not supported
        * @exception IOException when it is not possible to read data
        */
        public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }

            return transferObject;
        }

        /** Class implementing MultiTransferObject interface. */
        static class TransferObjectImpl implements MultiTransferObject {
            /** transferable objects */
            private Transferable[] trans;

            /** Creates new object from transferable objects.
            * @param trans array of transferable objects
            */
            public TransferObjectImpl(Transferable[] trans) {
                this.trans = trans;
            }

            /** Number of transfered elements.
            * @return number of elements
            */
            public int getCount() {
                return trans.length;
            }

            /** @return Transferable at the specific index */
            public Transferable getTransferableAt(int index) {
                return trans[index];
            }

            /** Test whether data flavor is supported by index-th item.
            *
            * @param index the index of
            * @param flavor flavor to test
            * @return <CODE>true</CODE> if flavor is supported by all elements
            */
            public boolean isDataFlavorSupported(int index, DataFlavor flavor) {
                try {
                    return trans[index].isDataFlavorSupported(flavor);
                } catch (Exception e) {
                    return false;

                    // patch to get the Netbeans start under Solaris
                    // [PENDINGbeta]
                }
            }

            /** Test whether each transfered item supports at least one of these
            * flavors. Each item can support different flavor.
            * @param array array of flavors
            */
            public boolean areDataFlavorsSupported(DataFlavor[] array) {
                HashSet<DataFlavor> flav = new HashSet<DataFlavor>();

                for (int i = 0; i < array.length; i++) {
                    flav.add(array[i]);
                }


// cycles through all transferable objects and scans their content
// to find out if each supports at least one requested flavor
outer: 
                for (int i = 0; i < trans.length; i++) {
                    // insert all flavors of the first object into array
                    DataFlavor[] flavors = trans[i].getTransferDataFlavors();

                    if (flavors == null) {
                        return false;
                    }

                    // loop through rest of Transferable objects
                    for (int j = 0; j < flavors.length; j++) {
                        if (flav.contains(flavors[j])) {
                            // this flavor is supported
                            continue outer;
                        }
                    }

                    // for this transferable no flavor is supported
                    return false;
                }

                return true;
            }

            /** Gets list of all supported flavors for i-th element.
            * @param i the element to find flavors for
            * @return array of supported flavors
            */
            public DataFlavor[] getTransferDataFlavors(int i) {
                return trans[i].getTransferDataFlavors();
            }

            /**
            * @param indx index of element to work with
            * @param flavor one needs to obtain
            * @return object for the flavor of the i-th element
            */
            public Object getTransferData(int indx, DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
                return trans[indx].getTransferData(flavor);
            }

            /* Compute common flavors.
            * @param t array of transferable objects
            * @return array of common flavors
            * /
            private static DataFlavor[] computeCommonFlavors (Transferable[] t) {
                if (t.length == 0) {
                    // no flavor is supported => return empty array
                    return new DataFlavor[] { };
                }

                // insert all flavors of the first object into array
                DataFlavor[] flavors = t[0].getTransferDataFlavors ();
                // number of non null elements in flavors array
                int flavorsCount = (flavors == null)? 0 : flavors.length;
                int flavorsLength = flavorsCount; // non-changing length of the original flavors array

                // loop through rest of Transferable objects
                for (int i = 1; i < t.length; i++) {
                    // loop through array
                    for (int j = 0; j < flavorsLength; j++) {
                        // if the flavor is not supported
                        boolean supported = false;
                        try {
                            supported = t[i].isDataFlavorSupported (flavors[j]);
                        } catch (Exception e) {
                            // patch to get the Netbeans start under Solaris
                            // [PENDINGbeta]
                        }
                        if (flavors[j] != null && !supported) {
                            // then clear it
                            flavors[j] = null;
                            flavorsCount--;
                        }
                    }
                }

                // create resulting array
                DataFlavor[] result = new DataFlavor[flavorsLength];
                for (int i = 0, j = 0; i < flavorsLength; i++) {
                    if (flavors[i] != null) {
                        // add it to the result
                        result[j++] = flavors[i];
                    }
                }

                return result;
            }
            */
        }
    }

    /** TransferableOwnerEmpty is TransferableOwner that contains no data.
    *
    * @author Jaroslav Tulach
    */
    private static class Empty extends Object implements Transferable {
        /** Package private constructor to allow only one instance from TransferableOwner.
        */
        Empty() {
        }

        /** Flavors that are supported.
        * @return empty array
        */
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {  };
        }

        /** Does not support any flavor
        * @param flavor flavor to test
        * @return false
        */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return false;
        }

        /** Creates transferable data for this flavor.
        * @exception UnsupportedFlavorException does not support any flavor
        */
        public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
