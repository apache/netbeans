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

import com.sun.source.tree.ClassTree;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.SaasClientAuthenticationGenerator;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaUtil;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.openide.filesystems.FileObject;

/**
 * Code generator for Accessing Saas services.
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class RestClientPojoCodeGenerator extends SaasClientCodeGenerator {

    private JavaSource targetSource;
    private FileObject saasServiceFile = null;
    private JavaSource saasServiceJS = null;
    private FileObject serviceFolder = null;
    private SaasClientJavaAuthenticationGenerator authGen;

    public RestClientPojoCodeGenerator() {
        setDropFileType(Constants.DropFileType.JAVA_CLIENT);
    }

    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, WadlSaasMethod.class, getDropFileType()) &&
                Util.isJava(doc)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void init(SaasMethod m, Document doc) throws IOException {
        init(m, new RestClientSaasBean((WadlSaasMethod) m), doc);
    }
    
    public void init(SaasMethod m, RestClientSaasBean saasBean, Document doc) throws IOException {
        super.init(m, doc);
        setBean(saasBean); 
        targetSource = JavaSource.forFileObject(getTargetFile());
        String packageName = JavaSourceHelper.getPackageName(targetSource);
        getBean().setPackageName(packageName);
        
        serviceFolder = null;
        saasServiceFile = SourceGroupSupport.findJavaSourceFile(getProject(),
                getBean().getSaasServiceName());
        if (saasServiceFile != null) {
            saasServiceJS = JavaSource.forFileObject(saasServiceFile);
        }

        this.authGen = new SaasClientJavaAuthenticationGenerator(getBean(),getProject());
        this.authGen.setLoginArguments(getLoginArguments());
        this.authGen.setAuthenticatorMethodParameters(getAuthenticatorMethodParameters());
        this.authGen.setSaasServiceFolder(getSaasServiceFolder());
        this.authGen.setAuthenticationProfile(getBean().getProfile(m, getDropFileType()));
        this.authGen.setDropFileType(getDropFileType());
    }
    
    protected JavaSource getTargetSource() {
        return this.targetSource;
    }

    public SaasClientAuthenticationGenerator getAuthenticationGenerator() {
        return authGen;
    }

    public JavaSource getSaasServiceSource() {
        return saasServiceJS;
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
    public RestClientSaasBean getBean() {
        return (RestClientSaasBean) super.getBean();
    }
    
    @Override
    protected void preGenerate() throws IOException {
        super.preGenerate();
        createRestConnectionFile(getProject());

        if(getBean().getMethod().getSaas().getLibraryJars() == null) {
            WadlSaasEx ex = new WadlSaasEx(getBean().getMethod().getSaas());
            getBean().getMethod().getSaas().setLibraryJars(ex.getLibraryJars());
            getBean().getMethod().getSaas().setJaxbSourceJars(ex.getJaxbSourceJars());
        }
        if (getBean().getMethod().getSaas().getLibraryJars().size() > 0) {
            JavaUtil.addClientJars(getBean(), getProject(), null);
        }
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

        //execute this block before insertSaasServiceAccessCode() 
        setJaxbWrapper();
        //No need to check scanning, since we are not locking document
        //Util.checkScanning();
        insertSaasServiceAccessCode(isInBlock(getTargetDocument()));
        addImportsToTargetFile();

        finishProgressReporting();

        return new HashSet<FileObject>(Collections.<FileObject>emptyList());
    }

    private void setJaxbWrapper() {
        List<QName> repTypesFromWadl = getBean().findRepresentationTypes(getBean().getMethod());
        if (!repTypesFromWadl.isEmpty()) {
            for(QName qName: repTypesFromWadl) {
                String nsUri = qName.getNamespaceURI();
                getBean().addOutputWrapperName(qName.getLocalPart());
                getBean().addOutputWrapperPackageName(
                        (getBean().getGroupName() + "." +
                        getBean().getDisplayName()).toLowerCase() +
                        "." + nsUri.substring(nsUri.lastIndexOf(":") + 1).toLowerCase());
            }
        }
    }

    @Deprecated
    protected void addJaxbLib() throws IOException {
    }
        
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";

        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> params = getServiceMethodParameters();
        clearVariablePatterns();
        updateVariableNames(params);
        List<ParameterInfo> renamedParams = renameParameterNames(params);
        paramUse += Util.getHeaderOrParameterUsage(renamedParams);
        paramDecl += getHeaderOrParameterDeclaration(renamedParams);

        String methodBody = "\n"+INDENT + "try {\n";
        methodBody += paramDecl + "\n";
        methodBody += INDENT_2 + REST_RESPONSE + " "+getResultPattern()+" = " + getBean().getSaasServiceName() +
                "." + getBean().getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += Util.createPrintStatement(
                getBean().getOutputWrapperPackageNames(),
                getBean().getOutputWrapperNames(),
                getDropFileType(),
                getBean().getHttpMethod(),
                getBean().canGenerateJAXBUnmarshaller(), getResultPattern(), INDENT_2);
        methodBody += INDENT + "} catch (Exception ex) {\n";
        methodBody += INDENT_2 + "ex.printStackTrace();\n";
        methodBody += INDENT + "}\n";

        return methodBody;
    }

    protected String getServiceMethodBody() throws IOException {
        String methodBody = "";
        methodBody += INDENT + getFixedParameterDeclaration();

        //Insert authentication code before new "+Constants.REST_CONNECTION+"() call
        methodBody += INDENT_2 + getAuthenticationGenerator().getPreAuthenticationCode() + "\n";

        //Insert parameter declaration
        methodBody += INDENT + getTemplateParameterDefinition(
                getBean().getTemplateParameters(), Constants.PATH_PARAMS, false);
        methodBody += "        " + Util.getHeaderOrParameterDefinition(
                getBean().getQueryParameters(), Constants.QUERY_PARAMS, false);

        methodBody += INDENT_2 + Constants.REST_CONNECTION + 
                " conn = new " + Constants.REST_CONNECTION + "(\"" + getBean().getUrl() + "\"";
        methodBody += ", " + Constants.PATH_PARAMS;
        methodBody += ", " + (!Util.isPutPostFormParams(getBean())?Constants.QUERY_PARAMS:"null");
        methodBody += ");\n";

        //Insert authentication code after new "+Constants.REST_CONNECTION+"() call
        methodBody += INDENT_2 + getAuthenticationGenerator().getPostAuthenticationCode() + "\n";

        HttpMethodType httpMethod = getBean().getHttpMethod();
        String headerUsage = "null";
        if (getBean().getHeaderParameters() != null && getBean().getHeaderParameters().size() > 0) {
            headerUsage = Constants.HEADER_PARAMS;
            methodBody += INDENT + Util.getHeaderOrParameterDefinition(
                    getBean().getHeaderParameters(), Constants.HEADER_PARAMS, false, httpMethod);
        }
        
        //Insert the sleep call to avoid service throttling
        methodBody += INDENT_2 + "sleep(1000);\n";
        
        //Insert the method call
        methodBody += INDENT_2 + "return conn." + httpMethod.prefix() + "(" + headerUsage;
        if (httpMethod == HttpMethodType.PUT || httpMethod == HttpMethodType.POST) {
            if (Util.isPutPostFormParams(getBean())) {
                methodBody += ", " + Constants.QUERY_PARAMS;
            } else if (Util.hasInputRepresentations(getBean())) {
                methodBody += ", " + Constants.PUT_POST_CONTENT;
            } 
        }
        methodBody += ");\n";
        return methodBody;
    }

    protected String getFixedParameterDeclaration() {
        String fixedCode = "";
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
                fixedCode += "String " + getVariableName(param.getName()) + " = \"" + findParamValue(param) + "\";\n";
            }
        }

        return fixedCode;
    }

    protected List<ParameterInfo> getServiceMethodParameters() {
        return Util.getRestClientMethodParameters(getBean());
    }

    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        return Collections.emptyList();
    }

    protected String getLoginArguments() {
        return "";
    }

    protected void addImportsToTargetFile() throws IOException {
        List<String> imports = new ArrayList<String>();
        imports.add(getBean().getSaasServicePackageName() + "." + getBean().getSaasServiceName());
        imports.add(REST_CONNECTION_PACKAGE + "." + REST_RESPONSE);
//        if(getBean().canGenerateJAXBUnmarshaller()) {
//            imports.addAll(Util.getJaxBClassImports());
//        }
        JavaUtil.addImportsToSource(getTargetSource(), imports);
    }

    protected void addImportsToSaasService() throws IOException {
        List<String> imports = new ArrayList<String>();
        imports.add(REST_CONNECTION_PACKAGE + "." + REST_CONNECTION);
        imports.add(REST_CONNECTION_PACKAGE + "." + REST_RESPONSE);
//        if(getBean().canGenerateJAXBUnmarshaller()) {
//            imports.add(InputStream.class.getName());
//        }
        JavaUtil.addImportsToSource(saasServiceJS, imports);
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

    /**
     *  Create Saas Service
     */
    public void createSaasServiceClass() throws IOException {
        if (saasServiceFile == null) {
            SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(getProject());
            String pkg = getBean().getSaasServicePackageName();
            FileObject targetFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0], pkg, true);
            saasServiceJS = JavaSourceHelper.createJavaSource(
                    getBean().getSaasServiceTemplate()+"."+Constants.JAVA_EXT, 
                        targetFolder, pkg, getBean().getSaasServiceName());
            Set<FileObject> files = new HashSet<FileObject>(saasServiceJS.getFileObjects());
            if (files != null && files.size() > 0) {
                saasServiceFile = files.iterator().next();
            }
        }
    }

    /**
     *  Return target and generated file objects
     */
    protected void addSaasServiceMethod() throws IOException {
        List<ParameterInfo> filterParams = getServiceMethodParameters();
        final String[] parameters = getGetParamNames(filterParams);
        final Object[] paramTypes = getGetParamTypes(filterParams);

        if (JavaSourceHelper.isContainsMethod(saasServiceJS,
                getBean().getSaasServiceMethodName(), parameters, paramTypes)) {
            return;
        }
        ModificationResult result = saasServiceJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                Modifier[] modifiers = JavaUtil.PUBLIC_STATIC;

                String type = REST_RESPONSE;
                String bodyText = "{ \n" + getServiceMethodBody() + "\n }";


                String comment = "\n";// NOI18N

                for (String param : parameters) {
                    comment += "@param $PARAM$ \n".replace("$PARAM$", param);// NOI18N

                }
                comment += "@return an instance of " + type;// NOI18N

                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree tree = JavaSourceHelper.addMethod(copy, initial,
                        modifiers, null, null,
                        getBean().getSaasServiceMethodName(), type, parameters, paramTypes,
                        null, null, new String[]{"java.io.IOException"},
                        bodyText, comment);      //NOI18N

                copy.rewrite(initial, tree);
            }
        });
        result.commit();
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
            String paramVal = findParamValue(param);
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

    //String pathParams[] = new String[][]  { {"{volumeId}", volumeId},  {"{objectId}", objectId}}; 
    private String getTemplateParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramsStr = null;
        StringBuffer sb = new StringBuffer();
        for (ParameterInfo param : params) {
            String paramName = getParameterName(param);
            String paramVal = null;
            if (evaluate) {
                paramVal = findParamValue(param);
                if (param.getType() != String.class) {
                    sb.append("{\"" + paramName + "\", \"" + paramVal + "\".toString()},\n");
                } else {
                    if (paramVal != null) {
                        sb.append("{\"{" + paramName + "}\", \"" + paramVal + "\"},\n");
                    } else {
                        sb.append("{\"{" + paramName + "}\", null},\n");
                    }
                }
            } else {
                sb.append("{\"{" + paramName + "}\", " + paramName + "},\n");
            }
        }
        paramsStr = sb.toString();
        if (params.size() > 0) {
            paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
        }

        String paramCode = "";
        paramCode += "             String[][] " + varName + " = new String[][]{\n";
        paramCode += "                 " + paramsStr + "\n";
        paramCode += "             };\n";
        return paramCode;
    }

    private String findParamValue(ParameterInfo param) {
        return Util.findParamValue(param);
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
