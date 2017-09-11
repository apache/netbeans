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
