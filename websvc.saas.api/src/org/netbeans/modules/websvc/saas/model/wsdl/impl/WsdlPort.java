/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.websvc.saas.model.wsdl.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;

/**
 *
 * @author Roderico Cruz
 */
public class WsdlPort implements WSPort{
    Port port;
    public WsdlPort(Port port){
        this.port = port;
    }
    public Object getInternalJAXWSPort() {
        return port;
    }

    public List<WSOperation> getOperations() {
        Collection<Operation> operations = port.getBinding().get().getType().get().getOperations();
        List<WSOperation> ops = new ArrayList<WSOperation>();
        for(Operation operation : operations){
            ops.add(new WsdlOperation(operation));
        }
        return ops;
    }

    public String getName() {
        return port.getName();
    }

    public String getNamespaceURI() {
        return port.getPeer().getNamespaceURI();
    }

    public String getJavaName() {
        return port.getName();  //TODO is this relevant??
    }

    public String getPortGetter() {
        return "get" + port.getName() + "Port";
    }

    public String getSOAPVersion() {
        List<SOAPBinding> soapBindings = port.getBinding().get().getExtensibilityElements(SOAPBinding.class);
        return soapBindings.get(0).getTransportURI();  //todo need to compute this
    }

    public String getStyle() {
        List<SOAPBinding> soapBindings = port.getBinding().get().getExtensibilityElements(SOAPBinding.class);
        return soapBindings.get(0).getStyle().toString();
    }

    public boolean isProvider() {
       return false;
    }

    public String getAddress() {
        List<SOAPAddress> addresses = port.getExtensibilityElements(SOAPAddress.class);
        if ( addresses.size() >0 ){
            SOAPAddress address = addresses.get(0);
            return address.getLocation();
        }
        return null;
    }

}
