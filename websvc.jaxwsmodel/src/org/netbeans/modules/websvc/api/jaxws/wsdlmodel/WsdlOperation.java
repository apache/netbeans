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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
