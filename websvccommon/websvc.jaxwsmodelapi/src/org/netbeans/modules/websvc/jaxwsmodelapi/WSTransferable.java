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
/*
 * WebServiceTransferable.java
 *
 * Created on August 23, 2006, 1:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.jaxwsmodelapi;

import org.netbeans.modules.websvc.jaxwsmodelapi.WSReference;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author ayubskhan
 */
public class WSTransferable  extends ExTransferable.Single {
    private WSReference ref;
    
    public static final DataFlavor WS_FLAVOR =
        new DataFlavor(WSReference.class, "webservice ref");
    
    /** Creates a new instance of WebServiceTransferable */
    public WSTransferable(WSReference ref) {
        super(WS_FLAVOR);
        this.ref = ref;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return WS_FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (WS_FLAVOR.equals(flavor)) {
            return ref;
        } 
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[] {
            WS_FLAVOR
        };
    }

    @Override
    protected Object getData() throws IOException, UnsupportedFlavorException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
