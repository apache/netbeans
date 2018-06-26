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
package org.netbeans.modules.websvc.saas.codegen.php;

import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.php.util.PhpUtil;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class RestClientPhpCodeGenerator extends AbstractRestCodeGenerator {

    public static final String INDENT = "             ";
    
    private FileObject saasServiceFile = null;
    private FileObject serviceFolder = null;
    private FileObject saasFolder = null;
    private SaasClientPhpAuthenticationGenerator authGen;

    public RestClientPhpCodeGenerator() {
        setDropFileType(Constants.DropFileType.PHP);
    }
    
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, WadlSaasMethod.class, getDropFileType()) &&
                PhpUtil.isPhp(doc)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void init(SaasMethod m, Document doc) throws IOException {
        super.init(m, doc);
        setBean(new RestClientSaasBean((WadlSaasMethod) m, true));
        
        this.serviceFolder = null;
        this.saasFolder = null;
        
        this.authGen = new SaasClientPhpAuthenticationGenerator(getBean(), getProject());
        this.authGen.setLoginArguments(getLoginArguments());
        this.authGen.setAuthenticatorMethodParameters(getAuthenticatorMethodParameters());
        this.authGen.setSaasServiceFolder(getSaasServiceFolder());
        this.authGen.setAuthenticationProfile(getBean().getProfile(m, getDropFileType()));
        this.authGen.setDropFileType(getDropFileType());
    }

    @Override
    public RestClientSaasBean getBean() {
        return (RestClientSaasBean) super.getBean();
    }

    public SaasClientPhpAuthenticationGenerator getAuthenticationGenerator() {
        return authGen;
    }

    public FileObject getSaasServiceFolder() throws IOException {
        if (serviceFolder == null) {
            FileObject rootFolder = getSourceRootFolder();
            String folderName = getBean().getSaasServicePackageName().replace(".", "_");
            serviceFolder = rootFolder.getFileObject(folderName);
            if(serviceFolder == null)
                serviceFolder = FileUtil.createFolder(rootFolder, folderName);
        }
        return serviceFolder;
    }
    
    public FileObject getSaasFolder() throws IOException {
        if (saasFolder == null) {
            FileObject rootFolder = getSourceRootFolder();
            String folderName = SaasClientCodeGenerator.REST_CONNECTION_PACKAGE.replace(".", "_");
            saasFolder = rootFolder.getFileObject(folderName);
            if(saasFolder == null)
                saasFolder = FileUtil.createFolder(rootFolder, folderName);
        }
        return saasFolder;
    }
    
    private FileObject getSourceRootFolder() {
        Sources sources = ProjectUtils.getSources(getProject());
        SourceGroup[] groups = sources.getSourceGroups("PHPSOURCE");
        FileObject rootFolder = getProject().getProjectDirectory();
        if (groups != null && groups.length > 0 && groups[0] != null &&
                groups[0].getRootFolder() != null) {
            rootFolder = groups[0].getRootFolder();
        }
        return rootFolder;
    }

    @Override
    public Set<FileObject> generate() throws IOException {
        preGenerate();

        //Create Authenticator classes
        getAuthenticationGenerator().createAuthenticatorClass();

        //Create Authorization classes
        getAuthenticationGenerator().createAuthorizationClasses();

        createSaasServiceClass();
        addSaasServiceMethod();
        addImportsToSaasService();

        //Modify Authenticator class
        getAuthenticationGenerator().modifyAuthenticationClass(); 

        insertSaasServiceAccessCode(isInBlock(getTargetDocument()));
        addImportsToTargetFile();

        finishProgressReporting();

        return new HashSet<FileObject>(Collections.<FileObject>emptySet());
    }
    
    @Override
    protected void preGenerate() throws IOException {
        super.preGenerate();
        createRestConnectionFile(getProject());
    }
    
    protected String getServiceMethodBody() throws IOException {
        String methodBody = "";
        methodBody += INDENT + getFixedParameterDeclaration();

        //Insert authentication code before new "+Constants.REST_CONNECTION+"() call
        methodBody += getAuthenticationGenerator().getPreAuthenticationCode() + "\n";

        //Insert parameter declaration
        methodBody += INDENT + getTemplateParameterDefinition(
                getBean().getTemplateParameters(), Constants.PATH_PARAMS, false);
        methodBody += INDENT + getHeaderOrParameterDefinition(
                getBean().getQueryParameters(), Constants.QUERY_PARAMS, false);

        methodBody += INDENT + "$conn = new " + Constants.REST_CONNECTION + "(\"" + 
                getBean().getUrl() + "\"";
        methodBody += ", $" + Constants.PATH_PARAMS;
        methodBody += ", " + (!Util.isPutPostFormParams(getBean())?"$"+Constants.QUERY_PARAMS:"array()");
        methodBody += ");\n";

        //Insert authentication code after new "+Constants.REST_CONNECTION+"() call
        methodBody += "             " +
                getAuthenticationGenerator().getPostAuthenticationCode() + "\n";

        HttpMethodType httpMethod = getBean().getHttpMethod();
        if (getBean().getHeaderParameters() != null && getBean().getHeaderParameters().size() > 0) {
            methodBody += "        " + getHeaderOrParameterDefinition(getBean().getHeaderParameters(), Constants.HEADER_PARAMS, false, httpMethod);
            methodBody += INDENT+"$conn->setHeaders($" +Constants.HEADER_PARAMS+");\n";
        }

        //Insert the sleep call to avoid service throttling
        methodBody += INDENT + "sleep(1);\n";
        
        //Insert the method call
        methodBody += INDENT + "return $conn->" + httpMethod.prefix() + "(";
        if (httpMethod == HttpMethodType.PUT || httpMethod == HttpMethodType.POST) {
            if (Util.isPutPostFormParams(getBean())) {
                methodBody += "$" + Constants.QUERY_PARAMS;
            } else if (Util.hasInputRepresentations(getBean())) {
                methodBody += "$" + Constants.PUT_POST_CONTENT;
            } else {
                methodBody += "null";
            }
        }
        methodBody += ");\n";
        return methodBody;
    }

    protected String getFixedParameterDeclaration() {
        String fixedCode = "";
        List<ParameterInfo> inputParams = getBean().getInputParameters();
        List<ParameterInfo> signParams = null;

        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.SESSION_KEY) {
            SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
            signParams = sessionKey.getParameters();
        } else {
            signParams = Collections.emptyList();
        }
        for (ParameterInfo param : getBean().getInputParameters()) {
            if (param.isFixed() && !Util.isContains(param, signParams)) {
                fixedCode += "     $" + getVariableName(param.getName()) + " = \"" + PhpUtil.findParamValue(param) + "\";\n";
            }
        }

        return fixedCode;
    }

    protected List<ParameterInfo> getServiceMethodParameters() {
        List<ParameterInfo> params = getBean().filterParametersByAuth(getBean().filterParameters(
                new ParamFilter[]{ParamFilter.FIXED}));
        HttpMethodType httpMethod = getBean().getHttpMethod();

        if (httpMethod == HttpMethodType.PUT || httpMethod == HttpMethodType.POST) {

            ParameterInfo contentTypeParam = Util.findParameter(getBean().getInputParameters(), Constants.CONTENT_TYPE);
            Class contentType = InputStream.class;

            if (contentTypeParam != null) {
                if (!contentTypeParam.isFixed() && !params.contains(contentTypeParam)) {
                    params.add(contentTypeParam);
                } else {
                    String value = PhpUtil.findParamValue(contentTypeParam);
                    if (value.equals("text/plain") || value.equals("application/xml") ||
                            value.equals("text/xml")) {     //NOI18N

                        contentType = String.class;
                    }
                }
                if (!getBean().findInputRepresentations(getBean().getMethod()).isEmpty()) {
                    params.add(new ParameterInfo(Constants.PUT_POST_CONTENT, contentType));
                }
            }
        }
        return params;
    }

    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        return Collections.emptyList();
    }

    protected String getLoginArguments() {
        return "";
    }

    protected void addImportsToTargetFile() throws IOException {
        List<String> imports = new ArrayList<String>();
        imports.add(getBean().getSaasServicePackageName() + "->" + getBean().getSaasServiceName());
        imports.add(REST_CONNECTION_PACKAGE + "->" + REST_RESPONSE);
        addImportsToPhp(getTargetFile(), imports);
    }

    /**
     *  Insert the Saas client call
     */
    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        try {
            String inclStr = "\ninclude_once \"" + getSaasServiceFolder().getName() + "/" + 
                    getBean().getSaasServiceName() + ".php\";\n";
            String code = PhpUtil.wrapWithTag(inclStr+getCustomMethodBody(), getTargetDocument(), getStartPosition()) + "\n";
            insert(code, true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     *  Create Saas Service
     */
    public void createSaasServiceClass() throws IOException {
        DataObject d = Util.createDataObjectFromTemplate(
                getBean().getSaasServiceTemplate()+"."+Constants.PHP_EXT, 
                    getSaasServiceFolder(), getBean().getSaasServiceName());
        if(d != null) {
            saasServiceFile = d.getPrimaryFile();
            try {
                Document doc = Util.getDocument(saasServiceFile);
                replaceDocument(doc, "__SAAS_FOLDER__", getSaasFolder().getName());
                replaceDocument(doc, "__CONSTRUCT__", "__construct");
            } catch (BadLocationException ex) {
                throw new IOException(ex.getMessage());
            }
        }
    }

    /**
     *  Return target and generated file objects
     */
    protected void addSaasServiceMethod() throws IOException {
        String indent = "        ";
        List<ParameterInfo> filterParams = getServiceMethodParameters();
        final String[] parameters = getGetParamNames(filterParams);
        final Object[] paramTypes = getGetParamTypes(filterParams);
        String paramUse = getHeaderOrParameterUsage(filterParams);

        if (isContainsMethod(saasServiceFile,
                getBean().getSaasServiceMethodName(), parameters, paramTypes)) {
            return;
        }

        String type = REST_RESPONSE;
        String bodyText = "{ \n" + getServiceMethodBody() + "\n"+indent+"}";


        String comment = "\n";// NOI18N

        for (String param : parameters) {
            comment += indent+"@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);// NOI18N

        }
        comment += indent+"@return an instance of " + type;// NOI18N
        comment = "/*"+comment+"*/";
        
        String code = "\n" + indent + comment + 
                "\n" + indent + "public static function "+
                getBean().getSaasServiceMethodName()+"("+paramUse+")"+
                bodyText+"\n";
        try {
            Document saasServiceDoc = Util.getDocument(saasServiceFile);
            int start = PhpUtil.findText(saasServiceDoc, "}", false);
            int end = start;
            insert(code, start, end, saasServiceDoc, true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public ParameterInfo findParameter(String name) {
        List<ParameterInfo> params = getBean().getInputParameters();
        if (params != null) {
            for (ParameterInfo param : params) {
                if (param.getName().equals(name)) {
                    return param;
                }
            }
        }
        return null;
    }

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params,
            String indent) {
        if (indent == null) {
            indent = " ";
        }
        String paramDecl = "";
        for (ParameterInfo param : params) {
            String name = getVariableName(param.getName());
            String paramVal = PhpUtil.findParamValue(param);
            if (param.getType() != String.class) {
                paramDecl += indent + "$" + name + " = " + paramVal + ";\n";
            } else {
                if (paramVal != null) {
                    paramDecl += indent + "$" + name + " = \"" + paramVal + "\";\n";
                } else {
                    paramDecl += indent + "$" + name + " = null;\n";
                }
            }
        }
        return paramDecl;
    }

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params) {
        String indent = "                 ";
        return getHeaderOrParameterDeclaration(params, indent);
    }

    private void addImportsToPhp(FileObject saasServiceFile, List<String> imports) {
        
    }

    //$pathParams = array();
    //$pathParams["{volumeId}"] = $volumeId;
    //$pathParams["{objectId}"] = $objectId; 
    private String getTemplateParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramCode = "$" + varName + " = array();\n";
        if(getBean().getTemplateParameters() != null && getBean().getTemplateParameters().size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (ParameterInfo param : params) {
                sb.append(getHeaderOrParameterDefinitionPart(param, varName, evaluate, true));
            }
            paramCode += sb.toString() + "\n";
        }
        return paramCode;
    }

    private boolean isContainsMethod(FileObject saasServiceFile, 
            String saasServiceMethodName, String[] parameters, Object[] paramTypes) throws IOException {
        try {
            return PhpUtil.findText(Util.getDocument(saasServiceFile), 
                    "public static function "+saasServiceMethodName, true) != -1;
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> filterParams = getServiceMethodParameters();//includes request, response also
        paramUse += getHeaderOrParameterUsage(filterParams);
        paramDecl += getHeaderOrParameterDeclaration(filterParams);
        String methodBody = "";
        methodBody += INDENT + "try {\n";
        methodBody += paramDecl + "\n";
        methodBody += INDENT_2 + "$result = " + getBean().getSaasServiceName() +
                "::" + getBean().getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += INDENT_2 + "echo $result->getResponseBody();\n";
        methodBody += INDENT + "} catch(Exception $e) {\n";
        methodBody += INDENT_2 + "    echo \"Exception occured: \".$e;\n";
        methodBody += INDENT + "}\n";
        return methodBody;
    }

    public static String getHeaderOrParameterUsage(List<ParameterInfo> params) {
        String paramUsage = "";
        for (ParameterInfo param : params) {
            String name = Util.getParameterName(param, true, true, true);
            paramUsage += "$" + name + ", ";
        }
        if (params.size() > 0) {
            paramUsage = paramUsage.substring(0, paramUsage.length() - 2);
        }
        return paramUsage;
    }
    
    public static String getHeaderOrParameterDefinitionPart(List<ParameterInfo> params, 
            String varName, boolean evaluate) {
        return getHeaderOrParameterDefinitionPart(params, varName, evaluate, false);
    }
    
    public static String getHeaderOrParameterDefinitionPart(List<ParameterInfo> params, 
            String varName, boolean evaluate, boolean isTemplate) {
        StringBuffer sb = new StringBuffer();
        for (ParameterInfo param : params) {
            sb.append(getHeaderOrParameterDefinitionPart(param, varName, 
                    evaluate || param.isApiKey(), isTemplate));
        }
        return sb.toString();
    }
    
    public static String getHeaderOrParameterDefinitionPart(ParameterInfo param, 
            String varName, boolean evaluate, boolean isTemplate) {
        StringBuffer sb = new StringBuffer();
        String paramName = Util.getParameterName(param);
        if(isTemplate)
            paramName = "{"+paramName+"}";
        String paramVal = null;
        String indent = INDENT;
        if(evaluate) {
            paramVal = PhpUtil.findParamValue(param);
            if (param.getType() != String.class) {
                sb.append(indent+"$"+varName+"[\"" + paramName + "\"] = $" + paramVal + ";\n");
            } else {
                if (paramVal != null) {
                    sb.append(indent+"$"+varName+"[\"" + paramName + "\"] = $" + paramVal + ";\n");
                } else {
                    sb.append(indent+"$"+varName+"[\"" + paramName + "\"] = null;\n");
                }
            }
        } else {
            if (param.getType() != String.class) {
                sb.append(indent+"$"+varName+"[\"" + paramName + "\"] = $" + Util.getVariableName(param.getName()) + ";\n");
            } else {
                sb.append(indent+"$"+varName+"[\"" + paramName + "\"] = $" + Util.getVariableName(param.getName()) + ";\n");
            }
        }
        return sb.toString();
    }
    
    public String getHeaderOrParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramCode = INDENT+"$" + varName + " = array();\n";
        if (getBean().getQueryParameters() != null &&
                getBean().getQueryParameters().size() > 0) {
            paramCode += getHeaderOrParameterDefinitionPart(params, varName, evaluate) + "\n";
        }
        return paramCode;
    }
    
    public String getHeaderOrParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate, HttpMethodType httpMethod) {
        String part = getHeaderOrParameterDefinitionPart(params, varName, evaluate);
        if (httpMethod == HttpMethodType.PUT ||
                httpMethod == HttpMethodType.POST) {
            if (!Util.isContains(params, new ParameterInfo(Constants.CONTENT_TYPE, String.class))) {
                part += INDENT+"$"+varName+"[\"" + Constants.CONTENT_TYPE + "\"] = $"+Util.getVariableName(Constants.CONTENT_TYPE)+";\n";
            }
        }
        String paramCode = "";
        paramCode += "$" + varName + " = array();\n";
        if(getBean().getAuthenticationType() == SaasAuthenticationType.SIGNED_URL) {
            paramCode += INDENT+"$headerParams[\"Date\"] =  $date;\n";
        }
        paramCode += part + "\n";
        return paramCode;
    }

    private void replaceDocument(Document doc, String searchText, String replaceText) throws BadLocationException {
        int len = doc.getLength();
        String content = doc.getText(0, len);
        content = content.replace(searchText, replaceText);
        insert(content, 0, len, doc, true);
    }
}
