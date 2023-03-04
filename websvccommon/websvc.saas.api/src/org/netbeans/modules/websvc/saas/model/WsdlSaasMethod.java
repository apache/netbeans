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
package org.netbeans.modules.websvc.saas.model;

//import com.sun.tools.ws.processor.model.Operation;
//import com.sun.tools.ws.processor.model.java.JavaMethod;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;

/**
 *
 * @author nam
 */
public class WsdlSaasMethod extends SaasMethod {
    WsdlSaasPort parent;
    WSPort port;
    WSOperation operation;

    public WsdlSaasMethod(WsdlSaas saas, Method method) {
        super(saas, method);
    }

    public WsdlSaasMethod(WsdlSaasPort port, WSOperation operation) {
        super(port.getParentSaas(), null);
        this.parent = port;
        this.port = port.getWsdlPort();
        this.operation = operation;
    }

    public String getName() {
        if (getMethod() != null) {
            return getMethod().getName();
        }
        assert operation != null : "Should have non-null operation when filter method does not exist";
        return operation.getName();
    }

    public String getDisplayName() {
        return getName();
    }
    
    @Override
    public WsdlSaas getSaas() {
        return (WsdlSaas) super.getSaas();
    }

    public WSOperation getWsdlOperation() {
        init();
        return operation;
    }

    public WSPort getWsdlPort() {
        init();
        return port;
    }

    public JavaMethod getJavaMethod() {
        return getWsdlOperation().getJavaMethod();
    }

    private void init() {
        if (port == null || operation == null) {
            assert getMethod() != null : "Should have non-null filter method";
            for (WSPort p : getSaas().getWsdlModel().getPorts()) {
                if (! p.getName().equals(getMethod().getPortName())) {
                    continue;
                }
                port = p;

                for (WSOperation op : port.getOperations()) {
                    if (op.getName().equals(getMethod().getOperationName())) {
                        operation = op;
                        return;
                    }
                }
                break;
            }
        }
    }
}
