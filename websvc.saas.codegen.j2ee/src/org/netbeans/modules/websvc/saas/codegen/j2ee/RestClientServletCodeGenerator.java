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
package org.netbeans.modules.websvc.saas.codegen.j2ee;

import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.RestClientPojoCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaUtil;
import org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.j2ee.support.J2eeUtil;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.SaasMethod;

/**
 * Code generator for Accessing Saas services.
 *
 * @author ayubskhan
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class RestClientServletCodeGenerator extends RestClientPojoCodeGenerator {
    
    private SaasClientJ2eeAuthenticationGenerator j2eeAuthGen;
    
    public RestClientServletCodeGenerator() {
        setDropFileType(Constants.DropFileType.SERVLET);
        setPrecedence(1);
    }

    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, WadlSaasMethod.class, getDropFileType()) &&
                J2eeUtil.isServlet(NbEditorUtilities.getDataObject(doc))) {
            return true;
        }
        return false;
    }
    
    @Override
    public void init(SaasMethod m, Document doc) throws IOException {
        super.init(m, new RestClientSaasBean((WadlSaasMethod) m, true), doc);

        this.j2eeAuthGen = new SaasClientJ2eeAuthenticationGenerator(getBean(),getProject());
        this.j2eeAuthGen.setLoginArguments(getLoginArguments());
        this.j2eeAuthGen.setAuthenticatorMethodParameters(getAuthenticatorMethodParameters());
        this.j2eeAuthGen.setSaasServiceFolder(getSaasServiceFolder());
        this.j2eeAuthGen.setAuthenticationProfile(getBean().getProfile(m, getDropFileType()));
        this.j2eeAuthGen.setDropFileType(getDropFileType());
    }

    @Override
    public SaasClientJ2eeAuthenticationGenerator getAuthenticationGenerator() {
        return j2eeAuthGen;
    }
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        String indent2 = "                 ";

        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> params = getServiceMethodParameters();
        clearVariablePatterns();
        updateVariableNames(params);
        List<ParameterInfo> renamedParams = renameParameterNames(params);

        paramUse += Util.getHeaderOrParameterUsage(renamedParams);
        paramDecl += getHeaderOrParameterDeclaration(renameParameterNames(super.getServiceMethodParameters()));
        return getCustomMethodBody(paramDecl, paramUse, getResultPattern(), indent2);
    }

    protected String getCustomMethodBody(String paramDecl, String paramUse, 
            String resultVarName, String indent2) {
        String indent = "             ";
        String methodBody = "\n";
        methodBody += indent + "try {\n";
        methodBody += paramDecl + "\n";
        methodBody += indent2 + REST_RESPONSE + " "+resultVarName+" = " + getBean().getSaasServiceName() +
                "." + getBean().getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += Util.createPrintStatement(
                getBean().getOutputWrapperPackageNames(),
                getBean().getOutputWrapperNames(),
                getDropFileType(),
                getBean().getHttpMethod(),
                getBean().canGenerateJAXBUnmarshaller(), resultVarName, indent2);
        methodBody += indent + "} catch (Exception ex) {\n";
        methodBody += indent2 + "ex.printStackTrace();\n";
        methodBody += indent + "}\n";
        return methodBody;
    }

    @Override
    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        if (getBean().getAuthenticationType() == SaasAuthenticationType.SESSION_KEY ||
                getBean().getAuthenticationType() == SaasAuthenticationType.HTTP_BASIC) {
            return Util.getAuthenticatorMethodParametersForWeb();
        } else {
            return super.getAuthenticatorMethodParameters();
        }
    }

    @Override
    protected List<ParameterInfo> getServiceMethodParameters() {
        if (getBean().getAuthenticationType() == SaasAuthenticationType.SESSION_KEY ||
                getBean().getAuthenticationType() == SaasAuthenticationType.HTTP_BASIC) {
            List<ParameterInfo> params = Util.getServiceMethodParametersForWeb(getBean());
            Util.getRestClientPutPostParameters(getBean(), params);
            return params;
        } else {
            return super.getServiceMethodParameters();
        }
    }

    @Override
    protected String getLoginArguments() {
        return Util.getLoginArgumentsForWeb();
    }

    @Override
    protected void addImportsToTargetFile() throws IOException {
        super.addImportsToTargetFile();
        if (getDropFileType() == Constants.DropFileType.RESOURCE) {
            List<String> imports = new ArrayList<String>();
            imports.add(Constants.JAVA_ANNOTATION_PACKAGE + Constants.JAVA_ANNOTATION_RESOURCE);
            imports.add(Constants.HTTP_SERVLET_PACKAGE + Constants.HTTP_SERVLET_REQUEST_CLASS);
            imports.add(Constants.HTTP_SERVLET_PACKAGE + Constants.HTTP_SERVLET_RESPONSE_CLASS);
            JavaUtil.addImportsToSource(getTargetSource(), imports);

            //Also add injection member variables
            Map<String, String> fieldsMap = new HashMap<String, String>();
            JavaSourceHelper.getAvailableFieldSignature(getTargetSource(), fieldsMap);
            String[] annotations = new String[]{Constants.JAVA_ANNOTATION_PACKAGE+Constants.JAVA_ANNOTATION_RESOURCE};
            Object[] annotationAttrs = new Object[]{null};
            List<ParameterInfo> injectionParams = Util.getAuthenticatorMethodParametersForWeb();
            for (ParameterInfo p : injectionParams) {
                String sign = JavaSourceHelper.createFieldSignature(p.getTypeName(),
                        getParameterName(p, true, true, true));
                if (!fieldsMap.containsKey(sign)) {
                    JavaUtil.addInputParamField(getTargetSource(), p, annotations, annotationAttrs);
                }
            }
        }
    }

    @Override
    protected void addImportsToSaasService() throws IOException {
        super.addImportsToSaasService();

        if (getBean().getAuthenticationType() == SaasAuthenticationType.SESSION_KEY ||
                getBean().getAuthenticationType() == SaasAuthenticationType.HTTP_BASIC) {
            List<String> imports = new ArrayList<String>();
            imports.add(Constants.HTTP_SERVLET_PACKAGE + Constants.HTTP_SERVLET_REQUEST_CLASS);
            imports.add(Constants.HTTP_SERVLET_PACKAGE + Constants.HTTP_SERVLET_RESPONSE_CLASS);
            JavaUtil.addImportsToSource(getSaasServiceSource(), imports);
        }
    }
}
