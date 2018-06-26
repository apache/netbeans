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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.websvc.manager.api;

import com.sun.tools.ws.processor.model.java.JavaMethod;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.manager.model.WebServiceData;

/**
 * Contains the DataFlavors and the classes for transferring web service
 * metadata to web service consumers.  Current support is for the transfer
 * of web service ports and methods.
 * 
 * XXX should be unified with base NB Web Service DnD functionality
 * 
 * @author quynguyen
 */
public class WebServiceMetaDataTransfer {

    /**
     * The {@link DataFlavor} representing a web service port
     */
    public static final DataFlavor PORT_FLAVOR;
    
    /**
     * The {@link DataFlavor} representing a web service method
     */
    public static final DataFlavor METHOD_FLAVOR;

    public static final DataFlavor METHOD_NODE_FLAVOR;
    protected static final DataFlavor PORT_NODE_FLAVOR;
    
    static {
        try {
            PORT_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-port;class=org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Port"); // NOI18N
            PORT_NODE_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-port;class=org.openide.nodes.Node");
            METHOD_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-method;class=org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Method"); // NOI18N
            METHOD_NODE_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-method;class=org.openide.nodes.Node");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    
    public static final class Port {
        private final WebServiceData wsData;
        private final String portName;
        
        public Port(WebServiceData wsData, String portName) {
            this.wsData = wsData;
            this.portName = portName;
        }

        public WebServiceData getWebServiceData() {
            return wsData;
        }

        public String getPortName() {
            return portName;
        }
    }
    
    public static final class Method {
        private final WebServiceData wsData;
        private final JavaMethod method;
        private final String portName;
        private final WsdlOperation operation;
        
        public Method(WebServiceData wsData, JavaMethod method, String portName, WsdlOperation operation) {
            this.wsData = wsData;
            this.method = method;
            this.portName = portName;
            this.operation = operation;
        }
        
        public WebServiceData getWebServiceData() {
            return wsData;
        }
        
        public JavaMethod getMethod() {
            return method;
        }
        
        public String getPortName() {
            return portName;
        } 
        
        public WsdlOperation getOperation() {
            return operation;
        }        
    }
    
    
    public static final class MethodTransferable implements Transferable {
        private static final DataFlavor[] SUPPORTED_FLAVORS = { METHOD_FLAVOR, METHOD_NODE_FLAVOR };
        private final WebServiceMetaDataTransfer.Method transferData;
        
        public MethodTransferable(WebServiceMetaDataTransfer.Method transferData) {
            this.transferData = transferData;
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return SUPPORTED_FLAVORS;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == SUPPORTED_FLAVORS[0] || flavor == SUPPORTED_FLAVORS[1];
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }else {
                return transferData;
            }
        }
    }

    public static final class PortTransferable implements Transferable {
        private static final DataFlavor[] SUPPORTED_FLAVORS = { PORT_FLAVOR, PORT_NODE_FLAVOR };
        private final WebServiceMetaDataTransfer.Port transferData;
        
        public PortTransferable(WebServiceMetaDataTransfer.Port transferData) {
            this.transferData = transferData;
        }
        
        public PortTransferable(Method method) {
             WebServiceData wsData = method.getWebServiceData();
             String portName = method.getPortName();
             this.transferData = new Port(wsData, portName);
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return SUPPORTED_FLAVORS;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == SUPPORTED_FLAVORS[0] || flavor == SUPPORTED_FLAVORS[1];
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }else {
                return transferData;
            }
        }
    }
    
    
}
