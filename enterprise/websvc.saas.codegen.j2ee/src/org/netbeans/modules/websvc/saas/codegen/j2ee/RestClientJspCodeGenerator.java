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
package org.netbeans.modules.websvc.saas.codegen.j2ee;

import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.j2ee.support.J2eeUtil;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.util.Inflector;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.util.Util;

/**
 * Code generator for Accessing Saas services.
 *
 * @author ayubskhan
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class RestClientJspCodeGenerator extends RestClientServletCodeGenerator {

    public RestClientJspCodeGenerator() {
        setDropFileType(Constants.DropFileType.JSP);
    }

    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, WadlSaasMethod.class, getDropFileType()) &&
                Util.isJsp(doc)) {
            return true;
        }
        return false;
    }
    
    /**
     *  Insert the Saas client call
     */
    @Override
    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        clearVariablePatterns();
        addVariablePattern(J2eeUtil.JSP_NAMES_PAGE, 1);
        try {
            String code = "";
            code += J2eeUtil.getJspImports(getTargetDocument(), getStartPosition(), 
                    getBean().getSaasServicePackageName());
            code += J2eeUtil.wrapWithTag(getCustomMethodBody(), getTargetDocument(), getStartPosition()) + "\n";
            insert(code, true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        String indent2 = "                 ";

        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> params = getServiceMethodParameters();
        updateVariableNames(params);
        paramUse += Util.getHeaderOrParameterUsage(renameParameterNames(params));
        
        List<ParameterInfo> filterParams = J2eeUtil.filterJspParameters(super.getServiceMethodParameters());
        filterParams = renameParameterNames(filterParams);
        paramDecl += getHeaderOrParameterDeclaration(filterParams);
        return getCustomMethodBody(paramDecl, paramUse, getResultPattern(), indent2);
    }

    @Override
    protected void addImportsToTargetFile() throws IOException {
    }

    public String findSubresourceLocatorUriTemplate() {
        String subresourceLocatorUriTemplate = getAvailableUriTemplate();
        if (!subresourceLocatorUriTemplate.endsWith("/")) {
            //NOI18N
            subresourceLocatorUriTemplate += "/"; //NOI18N

        }
        return subresourceLocatorUriTemplate;
    }

    private String getAvailableUriTemplate() {
        String uriTemplate = Inflector.getInstance().camelize(getBean().getShortName(), true);
        return uriTemplate;
    }
}
