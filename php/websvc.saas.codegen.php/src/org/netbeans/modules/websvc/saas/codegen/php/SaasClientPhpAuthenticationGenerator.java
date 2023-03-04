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
package org.netbeans.modules.websvc.saas.codegen.php;

import java.io.BufferedReader;
import java.io.FileReader;
import org.netbeans.modules.websvc.saas.codegen.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants.DropFileType;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.HttpBasicAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseTemplates;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseTemplates.Template;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SignedUrlAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.Time;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class SaasClientPhpAuthenticationGenerator extends SaasClientAuthenticationGenerator {
    
    public static final String INDENT = "             ";
    
    private FileObject saasAuthFile;
    private FileObject loginFile;
    private FileObject callbackFile;
    
    public SaasClientPhpAuthenticationGenerator(SaasBean bean,
            Project project) {
        super(bean, project);
    }

    /* 
     * Insert this code before new "+Constants.REST_CONNECTION+"()
     */
    public String getPreAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.API_KEY) {
            methodBody += INDENT+"$apiKey = " + getBean().getAuthenticatorClassName() + "::getApiKey();";
        } else if (authType == SaasAuthenticationType.SESSION_KEY) {
            SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
            methodBody += INDENT + getBean().getAuthenticatorClassName() + "::login(" + getLoginArguments() + ");\n";
            List<ParameterInfo> signParams = sessionKey.getParameters();
            String paramStr = "";

            if (signParams != null && signParams.size() > 0) {
                paramStr = getSignParamDeclaration(getBean(), signParams, Collections.<ParameterInfo>emptyList());
            }

            String sigName = sessionKey.getSigKeyName();
            paramStr += INDENT+"$sign_params = array();\n";
            for (ParameterInfo p : getBean().getInputParameters()) {
                if (p.getName().equals(sigName)) continue;
                
                paramStr += INDENT+"$sign_params[\"" + p.getName() + "\"] = $" +
                        Util.getVariableName(p.getName()) + ";\n";
            }
            paramStr += INDENT+"$" + Util.getVariableName(sigName) + " = " +
                    getBean().getAuthenticatorClassName() + "::sign($sign_params);\n";//sig
            methodBody += paramStr;

        } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
            methodBody += INDENT + getBean().getAuthenticatorClassName() + "::login(" + getLoginArguments() + ");\n";
        }
        return methodBody;
    }

    /* 
     * Insert this code after new "+Constants.REST_CONNECTION+"()
     */
    public String getPostAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.SIGNED_URL) {
            SignedUrlAuthentication signedUrl = (SignedUrlAuthentication) getBean().getAuthentication();
            List<ParameterInfo> signParams = signedUrl.getParameters();
            if (signParams != null && signParams.size() > 0) {
                String paramStr = getSignParamDeclaration(getBean(), signParams, getBean().getInputParameters());
                paramStr += INDENT+"$sign_params = array();\n";
                for (ParameterInfo p : signParams) {
                    paramStr += INDENT+"$sign_params[\"" + p.getName() + "\"] = $" +
                            Util.getVariableName(p.getName()) + ";\n";
                }
                paramStr += INDENT+"$" +Util.getVariableName(signedUrl.getSigKeyName()) + " = " +
                        getBean().getAuthenticatorClassName() + "::sign($sign_params);\n";
                methodBody += paramStr;
            }
        } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
            String serviceName = "";
            try {
                serviceName = getSaasServiceFolder().getName();
            } catch (IOException ex) {
            }
            methodBody += INDENT + "$username = "+getBean().getAuthenticatorClassName()+"::getSession(\"" + serviceName + "username\");\n";
            methodBody += INDENT + "$password = "+getBean().getAuthenticatorClassName()+"::getSession(\"" + serviceName + "password\");\n";
            methodBody += INDENT + "$conn->setAuthentication($username, $password);\n";
        }
        return methodBody;
    }

    /**
     *  Create Authenticator
     */
    public void createAuthenticatorClass() throws IOException {
        FileObject targetFolder = getSaasServiceFolder();
        if(!getBean().isUseTemplates()) {
            if(saasAuthFile == null) {
                String authFileName = getBean().getAuthenticatorClassName();
                String authTemplate = null;
                SaasAuthenticationType authType = getBean().getAuthenticationType();
                if (authType == SaasAuthenticationType.API_KEY ||
                        authType == SaasAuthenticationType.HTTP_BASIC ||
                        authType == SaasAuthenticationType.SIGNED_URL ||
                        authType == SaasAuthenticationType.SESSION_KEY) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                }
                if (authTemplate != null) {
                    DataObject d = Util.createDataObjectFromTemplate(
                            authTemplate + Constants.SERVICE_AUTHENTICATOR + "."+Constants.PHP_EXT,
                            targetFolder, authFileName);// NOI18n
                    if(d != null)
                        saasAuthFile = d.getPrimaryFile();
                }
            }
        } else {
            UseTemplates useTemplates = null;
            if(getBean().getAuthentication() instanceof SessionKeyAuthentication) {
                SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
                useTemplates = sessionKey.getUseTemplates();
            } else if(getBean().getAuthentication() instanceof HttpBasicAuthentication) {
                HttpBasicAuthentication httpBasic = (HttpBasicAuthentication) getBean().getAuthentication();
                useTemplates = httpBasic.getUseTemplates();
            }
            if(useTemplates != null) {
                String dropType = getDropFileType().prefix();
                for (Template template : useTemplates.getTemplates()) {
                    if(!template.getDropTypeList().contains(dropType))
                        continue;
                    String id = template.getId();
                    String type = template.getType();
                    String templateUrl = template.getUrl();
                    if (templateUrl.contains("Authenticator")) {
                        String fileName = getBean().getAuthenticatorClassName();
                        FileObject fobj = targetFolder.getFileObject(fileName);
                        if (fobj == null) {
                            Util.createDataObjectFromTemplate(templateUrl, targetFolder,
                                    fileName);
                            Map<String, String> tokens = new HashMap<String, String>();
                            tokens.put("__GROUP__", targetFolder.getName());
                            replaceTokens(targetFolder.getFileObject(fileName, Constants.PHP_EXT), tokens);
                        }
                    }
                }
            }
        }

        //Also copy config
        if(getBean().getAuthenticationType() != SaasAuthenticationType.PLAIN) {
            String profileName = getBean().getAuthenticatorClassName()+"Profile";
            if (getAuthenticationProfile() != null && !getAuthenticationProfile().trim().equals("")) {
                try {
                    Util.createDataObjectFromTemplate(getAuthenticationProfile(),
                            targetFolder, profileName);
                } catch (Exception ex) {
                    throw new IOException("Profile file specified in " +
                            "saas-services/service-metadata/authentication/@profile, " +
                            "not found: " + getAuthenticationProfile());// NOI18n
                }
            }
        }
    }

    
    /**
     *  Create Authorization Frame
     */
    public void createAuthorizationClasses() throws IOException {
        if (getBean().isDropTargetWeb()) {
            List<ParameterInfo> filterParams = getAuthenticatorMethodParameters();
            final String[] parameters = Util.getGetParamNames(filterParams);
            final Object[] paramTypes = Util.getGetParamTypes(filterParams);
            createSessionKeyAuthorizationClassesForWeb(
                getBean(), getProject(),
                getBean().getSaasName(), getBean().getSaasServicePackageName(), 
                getSaasServiceFolder(), loginFile, callbackFile,
                parameters, paramTypes, getBean().isUseTemplates(), getDropFileType()
            );
        }
    }

    /**
     *  Return target and generated file objects
     */
    public void modifyAuthenticationClass() throws IOException {
    }

    /**
     *  Return target and generated file objects
     */
    public void modifyAuthenticationClass(final String comment, final Object[] modifiers,
            final Object returnType, final String name, final String[] parameters, final Object[] paramTypes,
            final Object[] throwList, final String bodyText)
            throws IOException {
    }

    public String getLoginBody(SaasBean bean,
            String groupName, String paramVariableName) throws IOException {
        if (getBean().isDropTargetWeb()) {
            if (getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
                return null;
            }
            return Util.createSessionKeyLoginBodyForWeb(bean, groupName, paramVariableName);
        }
        String methodBody = "";
        return methodBody;
    }

    public String getLogoutBody() {
        String methodBody = "";
        return methodBody;
    }

    public String getTokenBody(SaasBean bean,
            String groupName, String paramVariableName, String saasServicePkgName) throws IOException {
        if (getBean().isDropTargetWeb()) {
            if (getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
                return null;
            }
            return Util.createSessionKeyTokenBodyForWeb(bean, groupName, paramVariableName,
                    saasServicePkgName);
        }
        String authFileName = getBean().getAuthorizationFrameClassName();
        String methodBody = "";
        return methodBody;
    }

    public String getSignParamUsage(List<ParameterInfo> signParams, String groupName) {
        return Util.getSignParamUsage(signParams, groupName, 
                getBean().isDropTargetWeb());
    }
    
    /*
     * Generates something like 
    $apiKey = FacebookAuthenticator::getApiKey();
    $sessionKey = FacebookAuthenticator::getSessionKey();
    $method = "facebook.friends.get";
    $v = "1.0";
    $callId = RestConnection::currentTimeMillis();
     */
    public static String getSignParamDeclaration(SaasBean bean,
            List<ParameterInfo> signParams, List<ParameterInfo> filterParams) {
        String paramStr = "";
        for (ParameterInfo p : signParams) {
            String[] pIds = getParamIds(p, bean.getSaasName(),
                    bean.isDropTargetWeb());
            if (pIds != null) {//process special case
                paramStr += INDENT+ "$" + Util.getVariableName(pIds[0]) + " = " + pIds[1] + ";\n";
                continue;
            }
            if (Util.isContains(p, filterParams)) {
                continue;
            }

            paramStr += INDENT+ "$" + Util.getVariableName(p.getName()) + " = ";
            if (p.getFixed() != null) {
                paramStr += "\"" + p.getFixed() + "\";\n";
            } else if (p.getType() == Date.class) {
                paramStr += "$conn->getDate();\n";
            } else if (p.getType() == Time.class) {
                paramStr += "RestConnection::currentTimeMillis();\n";
            } else if (p.getType() == HttpMethodType.class) {
                paramStr += "\"" + bean.getHttpMethod().value() + "\";\n";
            } else if (p.isRequired()) {
                if (p.getDefaultValue() != null) {
                    paramStr += getQuotedValue(p.getDefaultValue().toString()) + ";\n";
                } else {
                    paramStr += "\"\";\n";
                }
            } else {
                if (p.getDefaultValue() != null) {
                    paramStr += getQuotedValue(p.getDefaultValue().toString()) + ";\n";
                } else {
                    paramStr += "null;\n";
                }
            }
        }
        paramStr += "\n";
        return paramStr;
    }
    
    public static String getQuotedValue(String value) {
        StringBuffer sb = new StringBuffer();
        String[] parts = value.replace("+", "&plus;").split("&plus;");
        for(String part:parts) {
            if(isWord(part))
                sb.append("$"+part.trim()+".");
            else
                sb.append(part+".");
        }
        String str = sb.toString();
        if(parts.length > 0)
            str = str.substring(0, str.length()-1);
        return Util.getQuotedValue(str);
    }
    
    public static boolean isWord(String part) {
        if(part == null || part.trim().equals(""))
            return false;
        String word = part.trim();
        for(char ch:word.toCharArray()) {
            if(!Character.isLetter(ch))
                return false;
        }
        return true;
    }
    
    public static String[] getParamIds(ParameterInfo p, String groupName,
            boolean isDropTargetWeb) {
        if (p.getId() != null) {//process special case
            String[] pElems = p.getId().split("=");
            if (pElems.length == 2) {
                String val = pElems[1];
                if (val.startsWith("{")) {
                    val = val.substring(1);
                }
                if (val.endsWith("}")) {
                    val = val.substring(0, val.length() - 1);
                }
                val = Util.getVariableName(val);
                val = Util.getAuthenticatorClassName(groupName) + "::" +
                        "get" + val.substring(0, 1).toUpperCase() + val.substring(1);
                val += "()";
                return new String[]{pElems[0], val};
            }
        }
        return null;
    }
    
    public static void createSessionKeyAuthorizationClassesForWeb(
            SaasBean bean, Project project,
            String groupName, String saasServicePackageName, 
            FileObject targetFolder, FileObject loginFile, FileObject callbackFile,
            final String[] parameters, final Object[] paramTypes, boolean isUseTemplates,
            DropFileType dropFileType) throws IOException {
        SaasAuthenticationType authType = bean.getAuthenticationType();
        if (authType == SaasAuthenticationType.SESSION_KEY ||
                authType == SaasAuthenticationType.HTTP_BASIC) {
            UseTemplates useTemplates = null;
            if (bean.getAuthentication() instanceof SessionKeyAuthentication) {
                SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
                useTemplates = sessionKey.getUseTemplates();
            } else if (bean.getAuthentication() instanceof HttpBasicAuthentication) {
                HttpBasicAuthentication httpBasic = (HttpBasicAuthentication) bean.getAuthentication();
                useTemplates = httpBasic.getUseTemplates();
            }
            if (useTemplates != null) {
                String dropType = dropFileType.prefix();
                for (Template template : useTemplates.getTemplates()) {
                    if (!template.getDropTypeList().contains(dropType)) {
                        continue;
                    }
                    String id = template.getId();
                    String type = template.getType() == null ? "" : template.getType();
                    String templateUrl = template.getUrl();
                    if (templateUrl == null || templateUrl.trim().equals("")) {
                        throw new IOException("Authentication template is empty.");
                    }
                    //FIXME - Hack
                    if (templateUrl.contains("Desktop")) {
                        continue;
                    }
                    String fileName = null;
//                        if (type.equals(Constants.LOGIN)) {
                    if (templateUrl.contains("Login")) {
                        fileName = bean.getSaasName() + Util.upperFirstChar(Constants.LOGIN);
//                        } else if (type.equals(Constants.CALLBACK)) {
                    } else if (templateUrl.contains("Callback")) {
                        fileName = bean.getSaasName() + Util.upperFirstChar(Constants.CALLBACK);
                    } else if (templateUrl.contains("Authenticator")) {
//                        } else if (type.equals(Constants.AUTH)) {
                        continue;
                    }
                    FileObject fObj = null;
                    if (fileName != null) {
                        fObj = targetFolder.getFileObject(fileName);
                        if (fObj == null) {
                            DataObject d = Util.createDataObjectFromTemplate(templateUrl, targetFolder,
                                    fileName);
                            if (d != null) {
                                fObj = d.getPrimaryFile();
                                Map<String, String> tokens = new HashMap<String, String>();
                                tokens.put("__GROUP__", targetFolder.getName());
                                replaceTokens(targetFolder.getFileObject(fileName, Constants.PHP_EXT), tokens);
                            }
                        }
                    }
                    if (fObj != null) {
                        if (type.equals(Constants.LOGIN)) {
                            loginFile = fObj;
                        } else if (type.equals(Constants.CALLBACK)) {
                            callbackFile = fObj;
                        }
                    }
                }
            }
        }
    }
    
    private static void replaceTokens(FileObject fO, Map<String, String> tokens) throws IOException {
        FileLock lock = fO.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fO)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                for(Map.Entry e:tokens.entrySet()) {
                    String key = (String) e.getKey();
                    String value = (String) e.getValue();
                    line = line.replaceAll(key, value);
                }
                sb.append(line+"\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(fO.getOutputStream(lock), StandardCharsets.UTF_8);
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
}
