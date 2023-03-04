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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;


/**
 * Provide a class supporting Drag and Drop for Session and Entity Beans.
 * @author  Chris Webster
 */
public class EjbTransferable implements Transferable {
//    private static final DataFlavor TEXT_FLAVOR =
//        new DataFlavor("text/plain; charset=unicode", null);
    public static final DataFlavor EJB_FLAVOR =
        new DataFlavor(EjbReference.class, "ejb ref");
    
//    private final String stringRep;
    private final EjbReference ref;
    
    public EjbTransferable(String stringRep, EjbReference ref) {
//        this.stringRep = stringRep;
        this.ref = ref;
    }
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (EJB_FLAVOR.equals(flavor)) {
            return ref;
//        } else if (TEXT_FLAVOR.equals(flavor)) {
//            return stringRep;
        } 
        throw new UnsupportedFlavorException(flavor);
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {
//            TEXT_FLAVOR,
            EJB_FLAVOR
        };
    }
    
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return /*TEXT_FLAVOR.equals(flavor) ||*/ EJB_FLAVOR.equals(flavor);
    }
}
