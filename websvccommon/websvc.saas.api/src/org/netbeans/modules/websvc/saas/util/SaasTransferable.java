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
package org.netbeans.modules.websvc.saas.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider;
import org.openide.util.Lookup;

/**
 *
 * @author nam
 */
public class SaasTransferable<T> implements Transferable {

    public static final Set<DataFlavor> WSDL_METHOD_FLAVORS = new HashSet<DataFlavor>(
            Arrays.asList(new DataFlavor[] {
        ConsumerFlavorProvider.WSDL_METHOD_FLAVOR,
        ConsumerFlavorProvider.WSDL_METHOD_NODE_FLAVOR
    }));
    
    public static final Set<DataFlavor> WADL_METHOD_FLAVORS = new HashSet<DataFlavor>(
            Arrays.asList(new DataFlavor[] {
        ConsumerFlavorProvider.WADL_METHOD_FLAVOR,
        ConsumerFlavorProvider.WADL_METHOD_NODE_FLAVOR
    }));
    
    public static final Set<DataFlavor> WSDL_PORT_FLAVORS = new HashSet<DataFlavor>(
            Arrays.asList(new DataFlavor[] {
        ConsumerFlavorProvider.PORT_FLAVOR,
        ConsumerFlavorProvider.PORT_NODE_FLAVOR
    }));

    public static final Set<DataFlavor> WSDL_SERVICE_FLAVORS = new HashSet<DataFlavor>(
            Arrays.asList(new DataFlavor[] {
        ConsumerFlavorProvider.WSDL_SERVICE_FLAVOR,
        ConsumerFlavorProvider.WSDL_SERVICE_NODE_FLAVOR
    }));
    
    public static final Set<DataFlavor> CUSTOM_METHOD_FLAVORS = new HashSet<DataFlavor>(
            Arrays.asList(new DataFlavor[] {
        ConsumerFlavorProvider.CUSTOM_METHOD_FLAVOR,
        ConsumerFlavorProvider.CUSTOM_METHOD_NODE_FLAVOR
    }));
    
    private final T transferData;
    private final Set<DataFlavor> flavors;

    public SaasTransferable(T transferData, Set<DataFlavor> flavors) {
        this.transferData = transferData;
        this.flavors = flavors;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors.toArray(new DataFlavor[0]);
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        } else {
            return transferData;
        }
    }
    
    public static Transferable addFlavors(Transferable transfer) {
        Collection<? extends ConsumerFlavorProvider> providers = Lookup.getDefault().lookupAll(ConsumerFlavorProvider.class);
        Transferable result = transfer;
        
        for (ConsumerFlavorProvider p : providers) {
            result = p.addDataFlavors(result);
        }
        return result;
    }
}
