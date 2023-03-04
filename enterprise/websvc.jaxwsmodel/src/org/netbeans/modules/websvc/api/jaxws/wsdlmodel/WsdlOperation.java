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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.Operation;
import java.util.*;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.java.WsdlJavaMethod;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class WsdlOperation implements WSOperation {
    
    private String operationName;
    private Operation operation;
    /** Creates a new instance of WsdlOperation */
    public WsdlOperation(Operation operation) {
        this.operation=operation;
    }
    
    public Object getInternalJAXWSOperation() {
        return operation;
    }
    
    public JavaMethod getJavaMethod() {
        Operation op = (Operation)getInternalJAXWSOperation();
        com.sun.tools.ws.processor.model.java.JavaMethod m = (op != null) ? op.getJavaMethod() : null;
        WsdlJavaMethod method = new WsdlJavaMethod(m);
        return method;
    }
    
    public String getName() {
        if (operationName == null) {
            operationName = operation.getName().getLocalPart();
        }
        String postfix=null;
        switch (getOperationType()) {
            case TYPE_NORMAL:break;
            case TYPE_ASYNC_POLLING: {
                postfix = NbBundle.getMessage(WsdlOperation.class,"TXT_asyncPolling");
                break;
            }
            case TYPE_ASYNC_CALLBACK: {
                postfix = NbBundle.getMessage(WsdlOperation.class,"TXT_asyncCallback");
                break;
            }
            default:
        }
        if (postfix!=null)
            operationName = NbBundle.getMessage(WsdlOperation.class,"TXT_operationName",operationName,postfix);
        return operationName; 
    }
    
    public String getJavaName() {
        return operation.getJavaMethod().getName();
    }
    
    public String getReturnTypeName() {
        return operation.getJavaMethod().getReturnType().getName();
    }
    
    public List<WsdlParameter> getParameters() {
        List<WsdlParameter> wsdlParameters = new ArrayList<WsdlParameter> ();
        if (operation==null) return wsdlParameters;
        List<com.sun.tools.ws.processor.model.java.JavaParameter> parameterList = operation.getJavaMethod().getParametersList();
        for (com.sun.tools.ws.processor.model.java.JavaParameter param: parameterList)
            wsdlParameters.add(new WsdlParameter(param));
        return wsdlParameters;
    }
    
    public Iterator<String> getExceptions() {
        return operation.getJavaMethod().getExceptions();
    }
    
    public int getOperationType() {
        String returnType = getReturnTypeName();
        if (returnType.startsWith("javax.xml.ws.Response")) { //NOI18N
            return TYPE_ASYNC_POLLING;
        } else if (returnType.startsWith("java.util.concurrent.Future")) { //NOI18N
            return TYPE_ASYNC_CALLBACK;
        } else return TYPE_NORMAL;
        
    }

    public String getOperationName() {
        if (operationName == null) {
            operationName = operation.getName().getLocalPart();
        }
        return operationName;
    }
    
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
    
}
