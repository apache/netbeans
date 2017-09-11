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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        return flavors.toArray(new DataFlavor[flavors.size()]);
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
