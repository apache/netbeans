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

import org.netbeans.modules.websvc.saas.codegen.*;
import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.j2ee.support.J2eeUtil;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.HttpBasicAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Login;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Method;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Token;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Token.Prompt;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseTemplates;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseTemplates.Template;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SignedUrlAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaUtil;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class SaasClientJ2eeAuthenticationGenerator extends SaasClientAuthenticationGenerator {
    
    private JavaSource loginJS;
    private JavaSource callbackJS;
    private JavaSource saasAuthJS;
    private FileObject saasAuthFile;
    private FileObject loginFile;
    private FileObject callbackFile;
    
    public SaasClientJ2eeAuthenticationGenerator(SaasBean bean,
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
            methodBody += "        String apiKey = " + getBean().getAuthenticatorClassName() + ".getApiKey();";
        } else if (authType == SaasAuthenticationType.SESSION_KEY) {
            SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
            methodBody += "        " + getBean().getAuthenticatorClassName() + ".login(" + getLoginArguments() + ");\n";
            List<ParameterInfo> signParams = sessionKey.getParameters();
            String paramStr = "";

            if (signParams != null && signParams.size() > 0) {
                paramStr = Util.getSignParamDeclaration(getBean(), signParams, Collections.<ParameterInfo>emptyList());
            }

            String sigName = sessionKey.getSigKeyName();
            paramStr += "        String " +
                    Util.getVariableName(sigName) + " = " +
                    getBean().getAuthenticatorClassName() + ".sign(\n";//sig
            paramStr += "                new String[][] {";
            for (ParameterInfo p : getBean().getInputParameters()) {
                if (p.getName().equals(sigName)) continue;
                
                paramStr += "                    {\"" + p.getName() + "\", " +
                        Util.getVariableName(p.getName()) + "}, ";
            }
            if(getBean().getInputParameters().size() > 0)
                paramStr = paramStr.substring(0, paramStr.length()-2);
            paramStr += "});\n";
            methodBody += paramStr;

        } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
            HttpBasicAuthentication httpBasic = (HttpBasicAuthentication) getBean().getAuthentication();
            methodBody += "        " + getBean().getAuthenticatorClassName() + ".login(" + getLoginArguments() + ");\n";
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
                String paramStr = Util.getSignParamDeclaration(getBean(), signParams, getBean().getInputParameters());
                paramStr += "        String " +
                        Util.getVariableName(signedUrl.getSigKeyName()) + " = " +
                        getBean().getAuthenticatorClassName() + ".sign(\n";
                paramStr += "                new String[][] {";
                for (ParameterInfo p : signParams) {
                    paramStr += "                    {\"" + p.getName() + "\", " +
                            Util.getVariableName(p.getName()) + "}, ";
                }
                if(getBean().getInputParameters().size() > 0)
                    paramStr = paramStr.substring(0, paramStr.length()-2);
                paramStr += "});\n";
                methodBody += paramStr;
            }
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
                if (authType == SaasAuthenticationType.API_KEY) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.SIGNED_URL) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.SESSION_KEY) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                }
                if (authTemplate != null) {
                    saasAuthJS = JavaSourceHelper.createJavaSource(
                            authTemplate + Constants.SERVICE_AUTHENTICATOR + "."+Constants.JAVA_EXT,
                            targetFolder, getBean().getSaasServicePackageName(), authFileName);// NOI18n
                    Set<FileObject> files = new HashSet<FileObject>(saasAuthJS.getFileObjects());
                    if (files != null && files.size() > 0) {
                        saasAuthFile = files.iterator().next();
                    }
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
                for (Template template : useTemplates.getTemplates()) {
                    if(!template.getDropTypeList().contains(getDropFileType().prefix()))
                        continue;
                    String id = template.getId();
                    String type = template.getType();
                    String templateUrl = template.getUrl();

                    String fileName = null;
                    //FIXME - Hack
                    if(templateUrl.contains("Desktop"))
                        continue;
//                    if (type.equals(Constants.AUTH)) {
                    if(templateUrl.contains("Authenticator")) {
                        fileName = getBean().getAuthenticatorClassName();
                    } else
                        continue;

                    if(templateUrl.endsWith("."+Constants.JAVA_EXT)) {
                        JavaSourceHelper.createJavaSource(templateUrl, targetFolder, 
                                getBean().getSaasServicePackageName(), fileName);
                    } else {
                        if (templateUrl.indexOf("/") != -1) {
                            fileName = getBean().getSaasName() +
                                    templateUrl.substring(templateUrl.lastIndexOf("/") + 1);
                        }
                        if (fileName != null) {
                            FileObject fobj = targetFolder.getFileObject(fileName);
                            if (fobj == null) {
                                Util.createDataObjectFromTemplate(templateUrl, targetFolder,
                                        fileName);
                            }
                        }
                    }
                }
            }
        }

        //Also copy profile.properties
        if(getBean().getAuthenticationType() != SaasAuthenticationType.PLAIN) {
            String profileName = getBean().getAuthenticatorClassName().toLowerCase();
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
            J2eeUtil.createSessionKeyAuthorizationClassesForWeb(
                getBean(), getProject(),
                getBean().getSaasName(), getBean().getSaasServicePackageName(), 
                getSaasServiceFolder(), 
                loginJS, loginFile, 
                callbackJS, callbackFile,
                parameters, paramTypes, getBean().isUseTemplates(), getDropFileType()
            );
        }
    }

    /**
     *  Return target and generated file objects
     */
    public void modifyAuthenticationClass() throws IOException {
        if (getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
            return;
        }
        Modifier[] modifiers = JavaUtil.PUBLIC_STATIC;
        Object[] throwList = null;
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
        if (sessionKey.getUseGenerator() != null) {
            UseGenerator useGenerator = sessionKey.getUseGenerator();
            //create getSessionKey() method
            String methodName = "getSessionKey";
            String comment = "";
            String bodyText = "";
            Object returnType = null;
            if (sessionKey.getSessionKeyName() != null) {
                String name = Util.getParameterName(sessionKey.getSessionKeyName(), true, true);
                List<ParameterInfo> fields = new ArrayList<ParameterInfo>();
                fields.add(new ParameterInfo(name, String.class));
                Modifier[] modifier = JavaUtil.PRIVATE_STATIC;
                JavaUtil.addInputParamFields(saasAuthJS, fields, modifier);//add sessionKey field. apiKey, secret fields already in template
                methodName = Util.getSessionKeyMethodName(name);
                comment = methodName + "\n";
                returnType = "String";
                bodyText = "return " + name + ";\n";
                if (bodyText != null) {
                    modifyAuthenticationClass(comment, modifiers, returnType, methodName,
                            null, null, throwList, bodyText);
                }
            }

            //create login() method
            returnType = Constants.VOID;
            methodName = Constants.LOGIN;
            comment = methodName + "\n";
            List<ParameterInfo> filterParams = getAuthenticatorMethodParameters();
            final String[] parameters = Util.getGetParamNames(filterParams);
            final Object[] paramTypes = Util.getGetParamTypes(filterParams);
            bodyText = getLoginBody(getBean(), getBean().getDisplayName(), Constants.QUERY_PARAMS);
            if (bodyText != null) {
                modifyAuthenticationClass(comment, modifiers, returnType, methodName,
                        parameters, paramTypes, throwList, bodyText);
            }

            //create getToken() method
            methodName = Util.getTokenMethodName(useGenerator);
            comment = methodName + "\n";
            returnType = "String";
            bodyText = getTokenBody(getBean(), getBean().getDisplayName(), Constants.QUERY_PARAMS,
                    getBean().getSaasServicePackageName());
            if (bodyText != null) {
                modifyAuthenticationClass(comment, modifiers, returnType, methodName,
                        parameters, paramTypes, throwList, bodyText);
            }

            //create logout() method
            methodName = "logout";
            comment = methodName + "\n";
            returnType = Constants.VOID;
            bodyText = getLogoutBody();
            if (bodyText != null) {
                modifyAuthenticationClass(comment, modifiers, returnType, methodName,
                        parameters, paramTypes, throwList, bodyText);
            }
        }
    }

    /**
     *  Return target and generated file objects
     */
    public void modifyAuthenticationClass(final String comment, final Object[] modifiers,
            final Object returnType, final String name, final String[] parameters, final Object[] paramTypes,
            final Object[] throwList, final String bodyText)
            throws IOException {
        if (JavaSourceHelper.isContainsMethod(saasAuthJS, name, parameters, paramTypes)) {
            return;
        }
        ModificationResult result = saasAuthJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree tree = JavaSourceHelper.addMethod(copy, initial,
                        (Modifier[])modifiers, null, null,
                        name, returnType, parameters, paramTypes,
                        null, null,
                        throwList, "{ \n" + bodyText + "\n }", comment);
                copy.rewrite(initial, tree);
            }
        });
        result.commit();
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
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Login login = useGenerator.getLogin();
            if (login != null) {
                String tokenName = Util.getTokenName(useGenerator);
                String tokenMethodName = Util.getTokenMethodName(useGenerator);
                methodBody += "        if (" + Util.getVariableName(sessionKey.getSessionKeyName()) + " == null) {\n";
                methodBody += "            String " + tokenName + " = " + tokenMethodName + "(" +
                        Util.getHeaderOrParameterUsage(getAuthenticatorMethodParameters()) + ");\n\n";

                methodBody += "            if (" + tokenName + " != null) {\n";
                methodBody += "                try {\n";
                Map<String, String> tokenMap = new HashMap<String, String>();
                methodBody += Util.getLoginBody(login, getBean(), groupName, tokenMap);
                methodBody += "                } catch (IOException ex) {\n";
                methodBody += "                    Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "                }\n\n";

                methodBody += "            }\n";
                methodBody += "        }\n";
            }
        }
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
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Token token = useGenerator.getToken();
            if (token != null) {
                String tokenName = Util.getTokenName(useGenerator);
                String sigId = "sig";
                if (token.getSignId() != null) {
                    sigId = token.getSignId();
                }
                String methodName = null;
                Method method = token.getMethod();
                if (method != null) {
                    methodName = method.getHref();
                    if (methodName == null) {
                        return methodBody;
                    } else {
                        methodName = methodName.startsWith("#") ? methodName.substring(1) : methodName;
                    }
                }
                methodBody += "       String " + tokenName + " = null;\n";
                methodBody += "       try {\n";
                methodBody += "            String method = \"" + methodName + "\";\n";
                methodBody += "            String v = \"1.0\";\n\n";

                List<ParameterInfo> signParams = token.getParameters();
                if (signParams != null && signParams.size() > 0) {
                    String paramStr = "";
                    paramStr += "        String " + sigId + " = sign(secret, \n";
                    paramStr += getSignParamUsage(signParams, groupName);
                    paramStr += ");\n\n";
                    methodBody += paramStr;
                }

                String queryParamsCode = "";
                Map<String, String> tokenMap = new HashMap<String, String>();
                if (method != null) {
                    String id = method.getId();
                    if (id != null) {
                        String[] tokens = id.split(",");
                        for (String tk : tokens) {
                            String[] tokenElem = tk.split("=");
                            if (tokenElem.length == 2) {
                                tokenMap.put(tokenElem[0], tokenElem[1]);
                            }
                        }
                    }
                    String href = method.getHref();
                    if (href != null && bean instanceof RestClientSaasBean) {
                        org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod =
                                SaasUtil.wadlMethodFromIdRef(
                                ((RestClientSaasBean)bean).getMethod().getSaas().getWadlModel(), href);
                        if (wadlMethod != null) {
                            ArrayList<ParameterInfo> params = ((RestClientSaasBean)bean).findWadlParams(wadlMethod);
                            if (params != null &&
                                    params.size() > 0) {
                                queryParamsCode = Util.getHeaderOrParameterDefinition(params, paramVariableName, false);
                            }
                        }
                    }
                }

                //Insert parameter declaration
                methodBody += "        " + queryParamsCode;

                String url = "";
                if (bean instanceof RestClientSaasBean) {
                    url = ((RestClientSaasBean) bean).getUrl();
                }
                methodBody += "             " + Constants.REST_CONNECTION + " conn = new " + Constants.REST_CONNECTION + "(\"" + url + "\"";
                if (!queryParamsCode.trim().equals("")) {
                    methodBody += ", " + paramVariableName;
                }
                methodBody += ");\n";

                methodBody += "            String result = conn.get();\n";

                for (Entry e : tokenMap.entrySet()) {
                    String name = Util.getVariableName((String) e.getKey());
                    String val = (String) e.getValue();
                    if (val.startsWith("{")) {
                        val = val.substring(1);
                    }
                    if (val.endsWith("}")) {
                        val = val.substring(0, val.length() - 1);
                    }
                    methodBody += "            " + name + " = result.substring(result.indexOf(\"<" + val + "\"),\n";
                    methodBody += "                            result.indexOf(\"</" + val + ">\"));\n\n";
                    methodBody += "            " + name + " = " + name + ".substring(" + name + ".indexOf(\">\") + 1);\n\n";
                }


                if (token.getPrompt() != null) {
                    Prompt prompt = token.getPrompt();
                    signParams = prompt.getParameters();
                    if (signParams != null && signParams.size() > 0) {
                        methodBody += "            String perms = \"write\";";
                        String paramStr = "";
                        paramStr += "        " + sigId + " = sign(\n";
                        paramStr += "                new String[][] {\n";
                        for (ParameterInfo p : signParams) {
                            paramStr += "                    {\"" + p.getName() + "\", " +
                                    Util.getParameterName(p, true, true) + "},\n";
                        }
                        paramStr += "        });\n\n";
                        methodBody += paramStr;
                    }
                    url = prompt.getDesktopUrl();
                    methodBody += "            String loginUrl = \"" + Util.getTokenPromptUrl(token, url) + "\";\n";
                }
                methodBody += "            " + authFileName + " frame = new " + authFileName + "(loginUrl);\n";
                methodBody += "            synchronized (frame) {\n";
                methodBody += "                try {\n";
                methodBody += "                    frame.wait();\n";
                methodBody += "                } catch (InterruptedException ex) {\n";
                methodBody += "                    Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "                }\n";
                methodBody += "            }\n";
                methodBody += "       } catch (IOException ex) {\n";
                methodBody += "            Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "       }\n\n";
                methodBody += "       return " + tokenName + ";\n";
            }
        }
        return methodBody;
    }

    public String getSignParamUsage(List<ParameterInfo> signParams, String groupName) {
        return Util.getSignParamUsage(signParams, groupName, 
                getBean().isDropTargetWeb());
    }

}
