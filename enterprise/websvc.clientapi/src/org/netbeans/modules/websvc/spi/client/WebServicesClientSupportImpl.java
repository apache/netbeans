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

package org.netbeans.modules.websvc.spi.client;

import java.io.IOException;
import java.util.List;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;

/**
 *
 * @author Peter Williams
 */
public interface WebServicesClientSupportImpl {
	
    public void addServiceClient(String serviceName, String packageName,
        String sourceUrl, FileObject configFile, ClientStubDescriptor stubDescriptor);

    public void addServiceClient(String serviceName, String packageName, 
        String sourceUrl, FileObject configFile, ClientStubDescriptor stubDescriptor, String[] wscompileFeatures);
    
    public void addServiceClientReference(String serviceName, String fqServiceName, String relativeWsdlPath, String mappingPath, String[] portSEIInfo);

    public void removeServiceClient(String serviceName);
		
    public FileObject getWsdlFolder(boolean create) throws IOException;

    public FileObject getDeploymentDescriptor();
	
    public List<ClientStubDescriptor> getStubDescriptors();
    
    public List/*WsCompileClientEditorSupport.ServiceSettings*/ getServiceClients();
    
    public String getWsdlSource(String serviceName);
    
    public void setWsdlSource(String serviceName, String wsdlSource);
    
    public void setProxyJVMOptions(String proxyHost, String proxyPort);
    
    public String getServiceRefName(String serviceName);
    
}
