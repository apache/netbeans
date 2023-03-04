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
package org.netbeans.modules.websvc.saas.codegen;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants.DropFileType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author Ayub Khan
 */
public abstract class SaasClientAuthenticationGenerator {

    private SaasBean bean = null;
    private String loginArgs;
    private FileObject serviceFolder;
    private List<ParameterInfo> authMethodParams;
    private Project project;
    private String authProfile;
    private DropFileType dropFileType;

    public SaasClientAuthenticationGenerator(SaasBean bean,
            Project project) {
        this.bean = bean;
        this.project = project;
    }

    public SaasBean getBean() {
        return bean;
    }
    
    public Project getProject() {
        return project;
    }
    
    public DropFileType getDropFileType() {
        return dropFileType;
    }

    public void setDropFileType(DropFileType dropFileType) {
        this.dropFileType = dropFileType;
    }
    
    public String getAuthenticationProfile() {
        return this.authProfile;
    }
    
    public void setAuthenticationProfile(String authProfile) {
        this.authProfile = authProfile;
    }
    
    public String getLoginArguments() {
        return loginArgs;
    }
    
    public void setLoginArguments(String loginArgs) {
        this.loginArgs = loginArgs;
    }
    
    public List<ParameterInfo> getAuthenticatorMethodParameters() {
        return authMethodParams;
    }
    
    public void setAuthenticatorMethodParameters(List<ParameterInfo> authMethodParams) {
        this.authMethodParams = authMethodParams;
    }
    
    public FileObject getSaasServiceFolder() throws IOException {
        return serviceFolder;
    }
    
    public void setSaasServiceFolder(FileObject serviceFolder) throws IOException {
        this.serviceFolder = serviceFolder;
    }
    
    public SaasAuthenticationType getAuthenticationType() throws IOException {
        return getBean().getAuthenticationType();
    }

    /* 
     * Insert this code before new "+Constants.REST_CONNECTION+"()
     */
    public abstract String getPreAuthenticationCode();
    /* 
     * Insert this code after new "+Constants.REST_CONNECTION+"()
     */
    public abstract String getPostAuthenticationCode();

    /**
     *  Create Authenticator
     */
    public abstract void createAuthenticatorClass() throws IOException;

    
    /**
     *  Create Authorization Frame
     */
    public abstract void createAuthorizationClasses() throws IOException;

    /**
     *  Return target and generated file objects
     */
    public abstract void modifyAuthenticationClass() throws IOException;
    
    /**
     *  Return target and generated file objects
     */
    public abstract void modifyAuthenticationClass(final String comment, final Object[] modifiers,
            final Object returnType, final String name, final String[] parameters, final Object[] paramTypes,
            final Object[] throwList, final String bodyText)
            throws IOException;

    public abstract String getLoginBody(SaasBean bean,
            String groupName, String paramVariableName) throws IOException;

    public abstract String getLogoutBody();

    public abstract String getTokenBody(SaasBean bean,
            String groupName, String paramVariableName, String saasServicePkgName) throws IOException;

    public abstract String getSignParamUsage(List<ParameterInfo> signParams, String groupName);
}
