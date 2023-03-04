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
package org.netbeans.modules.websvc.saas.spi;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasPort;
import org.openide.nodes.Node;

/**
 *
 * Service class that allows consumers to add to the DataFlavors present in
 * the SaaS nodes Transferable.
 *
 * @author quynguyen
 */
public interface ConsumerFlavorProvider {
    
    public static final DataFlavor WSDL_SERVICE_FLAVOR = new DataFlavor(WsdlSaas.class, "SaaS WSDL Service"); //NOI18N
    public static final DataFlavor WSDL_SERVICE_NODE_FLAVOR = new DataFlavor(Node.class, "SaaS WSDL Service Node"); //NOI18N
    public static final DataFlavor PORT_FLAVOR = new DataFlavor(WsdlSaasPort.class, "SaaS WSDL Port"); //NOI18N
    public static final DataFlavor PORT_NODE_FLAVOR = new DataFlavor(Node.class, "SaaS WSDL Port Node"); //NOI18N
    public static final DataFlavor WSDL_METHOD_FLAVOR = new DataFlavor(WsdlSaasMethod.class, "SaaS WSDL Operation"); //NOI18N
    public static final DataFlavor WSDL_METHOD_NODE_FLAVOR = new DataFlavor(Node.class, "SaaS WSDL Operation Node"); //NOI18N
            
    public static final DataFlavor WADL_METHOD_FLAVOR = new DataFlavor(WadlSaasMethod.class, "SaaS WADL Method"); //NOI18N
    public static final DataFlavor WADL_METHOD_NODE_FLAVOR = new DataFlavor(Node.class, "SaaS WADL Method Node"); //NOI18N
    
    public static final DataFlavor CUSTOM_METHOD_FLAVOR = new DataFlavor(CustomSaasMethod.class, "SaaS Custom Method"); //NOI18N
    public static final DataFlavor CUSTOM_METHOD_NODE_FLAVOR = new DataFlavor(Node.class, "SaaS Custom Method Node"); //NOI18N
    /**
     * Add DataFlavors specific to a web service consumer to the base <code>Transferable</code>.
     * This method must not modify existing <code>DataFlavor</code> to data mappings.
     * 
     * @param t the base <code>Transferable</code>
     * @return a <code>Transferable</code> that has the same data flavors as <code>t</code> with possible additions
     */
    public Transferable addDataFlavors(Transferable t);
}
