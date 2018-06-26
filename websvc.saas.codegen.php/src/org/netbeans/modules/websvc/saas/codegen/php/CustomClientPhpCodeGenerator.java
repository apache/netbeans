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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.CustomClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.php.util.PhpUtil;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class CustomClientPhpCodeGenerator extends AbstractRestCodeGenerator {

    private FileObject saasServiceFile = null;
    private FileObject serviceFolder = null;
    private FileObject saasFolder = null;
    private SaasClientPhpAuthenticationGenerator authGen;

    public CustomClientPhpCodeGenerator() {
        setDropFileType(Constants.DropFileType.PHP);
    }
    
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, CustomSaasMethod.class, getDropFileType()) &&
                PhpUtil.isPhp(doc)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void init(SaasMethod m, Document doc) throws IOException {
        init(m, new CustomClientSaasBean((CustomSaasMethod) m, true), doc);
    }
    
    public void init(SaasMethod m, CustomClientSaasBean saasBean, Document doc) throws IOException {
        super.init(m, doc);
        setBean(saasBean);

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
    public CustomClientSaasBean getBean() {
        return (CustomClientSaasBean) super.getBean();
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
                
        //Modify Authenticator class
        getAuthenticationGenerator().modifyAuthenticationClass(); 
        
        //execute this block before insertSaasServiceAccessCode() 
        insertSaasServiceAccessCode(isInBlock(getTargetDocument()));
        addImportsToTargetFile();
        
        finishProgressReporting();

        return new HashSet<FileObject>(Collections.<FileObject>emptyList());
    }
    
    @Override
    protected void preGenerate() throws IOException {
        createRestConnectionFile(getProject());
        
        getTargetFolder().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                try {
                    /*
                      Already taken care by Util.addClientJars()
                     List<String> libs = getBean().getArtifactLibs(); 
                    for(String lib: libs) {
                        //TODO - Fix the copyFile method
                        copyFile(lib, FileUtil.toFile(getTargetFolder()));
                    }*/
                    Map<String, String> templates = getBean().getArtifactTemplates(Constants.DropFileType.PHP.prefix());
                    for(Map.Entry e: templates.entrySet()) {
                        String id = (String) e.getKey();
                        String template = (String) e.getValue();
                        Util.createDataObjectFromTemplate(template, getSaasServiceFolder(), id);
                    }
                } finally {
                }
            }
        });
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
                fixedCode += "$" + getVariableName(param.getName()) + " = $" + PhpUtil.findParamValue(param) + ";\n";
            }
        }

        return fixedCode;
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

    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        String indent2 = "                    ";
        
        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> filterParams = getServiceMethodParameters();//includes request, response also
        paramUse += getHeaderOrParameterUsage(filterParams);
        paramDecl += getHeaderOrParameterDeclaration(filterParams);
        String methodBody = "";
        methodBody += indent2 + "try {\n";
        methodBody += paramDecl + "\n";
        methodBody += indent2 + "$result = " + getBean().getSaasServiceName() +
                "::" + getBean().getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += indent2 + "echo $result->getResponseBody();\n";
        methodBody += indent2 + "} catch(Exception $e) {\n";
        methodBody += indent2 + "    echo \"Exception occured: \".$e;\n";
        methodBody += indent2 + "}\n";
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
    
    public static String getHeaderOrParameterDefinitionPart(List<ParameterInfo> params, String varName, boolean evaluate) {
        StringBuffer sb = new StringBuffer();
        for (ParameterInfo param : params) {
            sb.append(getHeaderOrParameterDefinitionPart(param, varName, evaluate || param.isApiKey()));
        }
        return sb.toString();
    }
    
    public static String getHeaderOrParameterDefinitionPart(ParameterInfo param, String varName, boolean evaluate) {
        StringBuffer sb = new StringBuffer();
        String paramName = Util.getParameterName(param);
        String paramVal = null;
        String indent = "             ";
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
    
    public static String getHeaderOrParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramCode = "";
        paramCode += "$" + varName + " = array();\n";
        paramCode += getHeaderOrParameterDefinitionPart(params, varName, evaluate) + "\n";
        return paramCode;
    }
    
    public static String getHeaderOrParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate, HttpMethodType httpMethod) {
        String part = getHeaderOrParameterDefinitionPart(params, varName, evaluate);
        if (httpMethod == HttpMethodType.PUT ||
                httpMethod == HttpMethodType.POST) {
            if (!Util.isContains(params, new ParameterInfo(Constants.CONTENT_TYPE, String.class))) {
                part += ", array(\"" + Constants.CONTENT_TYPE + "\" => " + Util.getVariableName(Constants.CONTENT_TYPE) + ")";
            }
        }
        String paramCode = "";
        paramCode += "$" + varName + " = array();\n";
        paramCode += part + "\n";
        return paramCode;
    }
    
    protected List<ParameterInfo> getServiceMethodParameters() {
        List<ParameterInfo> params = getBean().filterParametersByAuth(getBean().filterParameters(
                new ParamFilter[]{ParamFilter.FIXED}));
        HttpMethodType httpMethod = getBean().getHttpMethod();
        
        if (httpMethod == HttpMethodType.PUT || httpMethod == HttpMethodType.POST) {
            
            ParameterInfo contentTypeParam = Util.findParameter(getBean().getInputParameters(), Constants.CONTENT_TYPE);
            Class contentType = InputStream.class;
            
            if (contentTypeParam == null) {
                params.add(new ParameterInfo(Constants.CONTENT_TYPE, String.class));
            } else {
                if (!contentTypeParam.isFixed() && !params.contains(contentTypeParam)) {
                    params.add(contentTypeParam);
                } else {
                    String value = Util.findParamValue(contentTypeParam);
                    if (value.equals("text/plain") || value.equals("application/xml") ||
                            value.equals("text/xml")) {     //NOI18N
                        contentType = String.class;
                    }
                }
            }
            params.add(new ParameterInfo(Constants.PUT_POST_CONTENT, contentType));
        }
        return params;
    }
    
    public static List<ParameterInfo> getAuthenticatorMethodParametersForWeb() {
        List<ParameterInfo> params = new ArrayList<ParameterInfo>();
        params.add(new ParameterInfo(Constants.HTTP_SERVLET_REQUEST_VARIABLE, Object.class,
                Constants.HTTP_SERVLET_REQUEST_CLASS));
        params.add(new ParameterInfo(Constants.HTTP_SERVLET_RESPONSE_VARIABLE, Object.class,
                Constants.HTTP_SERVLET_RESPONSE_CLASS));
        return params;
    }

    public static List<ParameterInfo> getServiceMethodParametersForWeb(CustomClientSaasBean bean) {
        List<ParameterInfo> params = new ArrayList<ParameterInfo>();
        params.addAll(getAuthenticatorMethodParametersForWeb());
        params.addAll(bean.filterParametersByAuth(bean.filterParameters(
                new ParamFilter[]{ParamFilter.FIXED})));
        return params;
    }
}
