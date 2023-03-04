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

package org.netbeans.modules.websvc.saas.codegen.j2ee.support;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.saas.codegen.java.support.SoapClientJavaOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamStyle;


/**
 *
 * @author ayubskhan
 */
public class SoapClientJ2eeOperationInfo extends SoapClientJavaOperationInfo {
    
    private List<ParameterInfo> headerParams;

    public SoapClientJ2eeOperationInfo(WsdlSaasMethod m, Project project) {
        super(m, project);
    }

    @Override
    public List<ParameterInfo> getSoapHeaderParameters() {
        if (headerParams == null) {
            headerParams = new java.util.ArrayList<ParameterInfo>();
            Map<QName,String> params = SoapClientUtils.getSoapHandlerParameters(
                    getXamWsdlModel(), getPort(), getOperation());
            for (Map.Entry<QName,String> entry : params.entrySet()) {
                Class type = getType(getProject(), entry.getValue());
                ParameterInfo info = new ParameterInfo(entry.getKey(), type, entry.getValue());
                info.setStyle(ParamStyle.UNKNOWN);
                headerParams.add(info);
            }
        }
        return headerParams;
    }
}
