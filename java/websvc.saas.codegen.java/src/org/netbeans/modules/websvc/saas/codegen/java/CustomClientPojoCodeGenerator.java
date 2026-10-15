/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.saas.codegen.java;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
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
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.SaasClientAuthenticationGenerator;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.codegen.model.CustomClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.netbeans.modules.websvc.saas.codegen.java.support.*;

/**
 * Code generator for Accessing Saas services.
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class CustomClientPojoCodeGenerator extends SaasClientCodeGenerator {
    public static final String VAR_NAMES_RESULT_DECL = RestClientPojoCodeGenerator.VAR_NAMES_RESULT_DECL;
    
    private JavaSource targetSource;  
    private FileObject serviceFolder;
    private SaasClientJavaAuthenticationGenerator authGen;
    
    public CustomClientPojoCodeGenerator() {
        setDropFileType(Constants.DropFileType.JAVA_CLIENT);
    }
    
    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, CustomSaasMethod.class, getDropFileType()) &&
                Util.isJava(doc)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void init(SaasMethod m, Document doc) throws IOException {
        init(m, new CustomClientSaasBean((CustomSaasMethod) m, false), doc); 
    }

    
    @Override 
    protected void preGenerate() throws IOException {
        createRestConnectionFile(getProject());
        
        //add JAXB Classes, etc, if available
        if(getBean().getMethod().getSaas().getLibraryJars().size() > 0)
            JavaUtil.addClientJars(getBean(), getProject(), null);
        
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
                    Map<String, String> templates = getBean().getArtifactTemplates(Constants.DropFileType.RESOURCE.prefix());
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
    
    public void init(SaasMethod m, CustomClientSaasBean saasBean, Document doc) throws IOException {
        super.init(m, doc);
        setBean(saasBean);

        this.targetSource = JavaSource.forFileObject(getTargetFile());
        String packageName = JavaSourceHelper.getPackageName(getTargetSource());
        getBean().setPackageName(packageName);
        
        this.serviceFolder = null;
        
        this.authGen = new SaasClientJavaAuthenticationGenerator(getBean(), getProject());
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

    public SaasClientAuthenticationGenerator getAuthenticationGenerator() {
        return authGen;
    }
        
    protected JavaSource getTargetSource() {
        return this.targetSource;
    }

    public FileObject getSaasServiceFolder() throws IOException {
        if (serviceFolder == null) {
            SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(getProject());
            serviceFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0],
                    getBean().getSaasServicePackageName(), true);
        }
        return serviceFolder;
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
        setJaxbWrapper();
        insertSaasServiceAccessCode(isInBlock(getTargetDocument()));
        addImportsToTargetFile();
        
        finishProgressReporting();

        return new HashSet<FileObject>(Collections.<FileObject>emptyList());
    }
    
    private void setJaxbWrapper() {
        //TODO
//        List<QName> repTypesFromWadl = getBean().findRepresentationTypes(getBean().getMethod());
//        if(!repTypesFromWadl.isEmpty()) {
//            getBean().setOutputWrapperName(repTypesFromWadl.get(0).getLocalPart());
//            getBean().setOutputWrapperPackageName(
//                    (getBean().getGroupName()+"."+
//                        getBean().getDisplayName()).toLowerCase());
//        }
    }
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";

        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        String indent = "        ";
        String indent2 = "             ";
        List<ParameterInfo> params = getServiceMethodParameters();
        clearVariablePatterns();
        updateVariableNames(params);
        List<ParameterInfo> renamedParams = renameParameterNames(params);
        paramUse += Util.getHeaderOrParameterUsage(renamedParams);
        paramDecl += getHeaderOrParameterDeclaration(renamedParams);

        String methodBody = "\n"+indent + "try {\n";
        methodBody += paramDecl + "\n";
        methodBody += indent2 + REST_RESPONSE + " "+getResultPattern()+" = " + getBean().getSaasServiceName() +
                "." + getBean().getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += Util.createPrintStatement(
                getBean().getOutputWrapperPackageNames(),
                getBean().getOutputWrapperNames(),
                getDropFileType(),
                getBean().getHttpMethod(),
                getBean().canGenerateJAXBUnmarshaller(), getResultPattern(), indent2);
        methodBody += indent+"} catch (Exception ex) {\n";
        methodBody += indent2+"ex.printStackTrace();\n";
        methodBody += indent+"}\n";

        return methodBody;
    }
    
    /**
     *  Insert the Saas client call
     */
    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        try {
            String code = "";
            if (isInBlock) {
                code = getCustomMethodBody();
            } else {
                code = "\nprivate String call" + getBean().getName() + "Service() {\n"; // NOI18n
                code += getCustomMethodBody() + "\n";
                code += "return "+getResultPattern()+";\n";
                code += "}\n";
            }
            insert(code, true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    protected void addImportsToTargetFile() throws IOException {
        List<String> imports = new ArrayList<String>();
        imports.add(getBean().getSaasServicePackageName() + "." + getBean().getSaasServiceName());
        if(getBean().getAuthenticationType() != SaasAuthenticationType.PLAIN)
            imports.add(getBean().getSaasServicePackageName() + "." + getBean().getAuthenticatorClassName());
        imports.add(REST_CONNECTION_PACKAGE + "." + REST_RESPONSE);
        JavaUtil.addImportsToSource(getTargetSource(), imports);
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
    

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params,
            String indent) {
        if (indent == null) {
            indent = " ";
        }
        String paramDecl = "";
        for (ParameterInfo param : params) {
            String name = getVariableName(param.getName());
            String paramVal = Util.findParamValue(param);
            if (param.getType() != String.class) {
                paramDecl += indent + param.getType().getName() + " " + name + " = " + paramVal + ";\n";
            } else {
                if (paramVal != null) {
                    paramDecl += indent + "String " + name + " = \"" + paramVal + "\";\n";
                } else {
                    paramDecl += indent + "String " + name + " = null;\n";
                }
            }
        }
        return paramDecl;
    }

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params) {
        String indent = "                 ";
        return getHeaderOrParameterDeclaration(params, indent);
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
    
    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        return Collections.emptyList();
    }

    protected String getLoginArguments() {
        return "";
    }
    
    protected void createRestConnectionFile(Project project) throws IOException {
        SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(project);
        String pkg = REST_CONNECTION_PACKAGE;
        FileObject targetFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0],pkg , true);
        JavaSourceHelper.createJavaSource(REST_CONNECTION_TEMPLATE, targetFolder, pkg, REST_CONNECTION);
        String restResponseTemplate = REST_RESPONSE_TEMPLATE;
        JavaSource restResponseJS = JavaSourceHelper.createJavaSource(restResponseTemplate, targetFolder, pkg, REST_RESPONSE);
    }
}
