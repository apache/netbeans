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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
