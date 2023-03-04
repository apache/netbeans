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
package org.openide.util.datatransfer;

import java.awt.datatransfer.*;

import java.io.IOException;


/** Interface for transferring multiple objects at once.
*
* @author Jaroslav Tulach
* @version 0.12 Dec 16, 1997
*/
public interface MultiTransferObject {
    /** Get the number of transferred elements.
    * @return the count
    */
    public int getCount();

    /**  Get the transferable at some index.
     * @param index the index
     * @return the transferable
     */
    public Transferable getTransferableAt(int index);

    /** Test whether a given data flavor is supported by the item at <code>index</code>.
    *
    * @param index the index
    * @param flavor the flavor to test
    * @return <CODE>true</CODE> if the flavor is supported
    */
    public boolean isDataFlavorSupported(int index, DataFlavor flavor);

    /** Test whether each transferred item supports at least one of these
    * flavors. Different items may support different flavors, however.
    * @param array array of flavors
    * @return <code>true</code> if all items support one or more flavors
    */
    public boolean areDataFlavorsSupported(DataFlavor[] array);

    /** Get list of all supported flavors for the item at an index.
    * @param i the index
    * @return array of supported flavors
    */
    public DataFlavor[] getTransferDataFlavors(int i);

    /** Get transfer data for the item at some index.
    * @param indx the index
    * @param flavor the flavor desired
    * @return transfer data for item at that index
    * @throws IOException if there is an I/O problem
    * @throws UnsupportedFlavorException if that flavor is not supported
    */
    public Object getTransferData(int indx, DataFlavor flavor)
    throws UnsupportedFlavorException, IOException;
}
