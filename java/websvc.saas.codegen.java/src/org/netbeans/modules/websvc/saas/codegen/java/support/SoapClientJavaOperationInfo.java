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
package org.netbeans.modules.websvc.saas.codegen.java.support;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;

/**
 *
 * @author ayubskhan
 */
public class SoapClientJavaOperationInfo extends SoapClientOperationInfo {

    public SoapClientJavaOperationInfo(WsdlSaasMethod m, Project project) {
        super(m, project);
    }

    @Override
    public void initWsdlModelInfo() {
        if (isRPCEncoded()) {
            LibrariesHelper.addDefaultJaxRpcClientJars(getProject(), null, getMethod().getSaas());
        } else {
            LibrariesHelper.addDefaultJaxWsClientJars(getProject(), null, getMethod().getSaas());
        }

    }

    @Override
    public Class getType(Project project, String typeName) {
        return JavaUtil.getType(project, typeName);
    }
}
