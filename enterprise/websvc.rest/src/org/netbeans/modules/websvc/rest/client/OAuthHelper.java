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

package org.netbeans.modules.websvc.rest.client;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.model.oauth.AuthorizationType;
import org.netbeans.modules.websvc.saas.model.oauth.DynamicUrlType;
import org.netbeans.modules.websvc.saas.model.oauth.FlowType;
import org.netbeans.modules.websvc.saas.model.oauth.Metadata;
import org.netbeans.modules.websvc.saas.model.oauth.MethodType;
import org.netbeans.modules.websvc.saas.model.oauth.ParamType;
import org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType;

/**
 *
 * @author mkuchtiak
 */
public class OAuthHelper {
    private static final String OAUTH_LOGIN_COMMENT =
            "You need to call this method at the beginning to authorize the application to work with user data.\n" +//NOI18N
            "The method obtains the OAuth access token string, that is appended to each API request later.";//NOI18N

    private static final String OAUTH_INIT_COMMENT =
            "The method sets the OAuth parameters for webResource.\n" + //NOI18N
            "The method needs to be called after login() method, or when the webResource path is changed"; //NOI18N
    private static final String OAUTH_INIT_WEB_COMMENT =
            "The method sets the OAuth parameters for webResource."; //NOI18N

    private static final String OAUTH_UNIQUE_COMMENT =
            "The method increases OAuth nonce and timestamp parameters to make each request unique.\n" +//NOI18N
            "The method should be called when repetitive requests are sent to service API provider:\n" +//NOI18N
            "<pre>\n" +//NOI18N
            "   client.initOauth();\n" +//NOI18N
            "   client.getXXX(...);\n" +//NOI18N
            "   client.makeOAuthRequestUnique();\n" +//NOI18N
            "   client.getYYY(...);\n" +//NOI18N
            "   client.makeOAuthRequestUnique();\n" +//NOI18N
            "   client.getZZZ(...);\n" +//NOI18N
            "</pre>";//NOI18N

    static ClassTree addOAuthMethods(String projectType, WorkingCopy copy, ClassTree originalClass, Metadata oauthMetadata, String className) {
        ClassTree modifiedClass = originalClass;
        TreeMaker maker = copy.getTreeMaker();

        // create fields
        ModifiersTree privateModif =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        ModifiersTree publicModif =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
        Set<Modifier> modifiersSet = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC);
        ModifiersTree privateStaticModif =  maker.Modifiers(modifiersSet);
        modifiersSet.add(Modifier.FINAL);
        ModifiersTree privateStaticFinalModif =  maker.Modifiers(modifiersSet);
        ExpressionTree stringType = maker.Identifier("String"); //NOI18N

        FlowType flow = oauthMetadata.getFlow();
        MethodType oauthRequestTokenMethod = flow.getRequestToken();
        MethodType oauthAccessTokenMethod = flow.getAccessToken();
        ExpressionTree uniformInterfaceEx = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N
        ExpressionTree iOEx = JavaSourceHelper.createTypeTree(copy, "java.io.IOException"); //NOI18N

        String oauthBaseUrl = oauthMetadata.getBaseUrl();
        VariableTree fieldTree = maker.Variable(privateStaticFinalModif, "OAUTH_BASE_URL", stringType, maker.Literal(oauthBaseUrl)); //NOI18N
        int lastFieldIndex = 3;
        modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);

        fieldTree = maker.Variable(privateStaticFinalModif, "CONSUMER_KEY", stringType, maker.Literal("")); //NOI18N
        String comment = "Please, specify the consumer_key string obtained from service API pages";
        maker.addComment(fieldTree, Comment.create(Style.JAVADOC, comment), true);
        modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);

        fieldTree = maker.Variable(privateStaticFinalModif, "CONSUMER_SECRET", stringType, maker.Literal("")); //NOI18N
        comment = "Please, specify the consumer_secret string obtained from service API pages";
        maker.addComment(fieldTree, Comment.create(Style.JAVADOC, comment), true);
        modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);

        if (isCallback(oauthRequestTokenMethod)) {
            fieldTree = maker.Variable(privateStaticFinalModif, "CALLBACK_PAGE_URL", stringType, maker.Literal("")); //NOI18N
            comment = "Please, specify the full URL of your callback page (e.g. http://www.myapplication.org/OAuthCallback)";
            maker.addComment(fieldTree, Comment.create(Style.JAVADOC, comment), true);
            modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);
        }

        ExpressionTree fieldTypeTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.oauth.signature.OAuthParameters"); //NOI18N
        fieldTree = maker.Variable(privateModif, "oauth_params", fieldTypeTree, null); //NOI18N
        modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);

        fieldTypeTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.oauth.signature.OAuthSecrets"); //NOI18N
        fieldTree = maker.Variable(privateModif, "oauth_secrets", fieldTypeTree, null); //NOI18N
        modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);

        fieldTypeTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.oauth.client.OAuthClientFilter"); //NOI18N
        fieldTree = maker.Variable(privateModif, "oauth_filter", fieldTypeTree, null); //NOI18N
        modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);

        if (Wadl2JavaHelper.PROJEC_TYPE_DESKTOP.equals(projectType)) {
            fieldTree = maker.Variable(privateModif, "oauth_access_token", stringType, null); //NOI18N
            modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);

            fieldTree = maker.Variable(privateModif, "oauth_access_token_secret", stringType, null); //NOI18N
            modifiedClass = maker.insertClassMember(modifiedClass, ++lastFieldIndex, fieldTree);
        }
        // methods
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        if (Wadl2JavaHelper.PROJEC_TYPE_DESKTOP.equals(projectType)) {
            // login method
            List<ExpressionTree> throwList = new ArrayList<ExpressionTree>();
            throwList.add(iOEx); throwList.add(uniformInterfaceEx);

            MethodTree methodTree = maker.Method (
                publicModif,
                "login", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                throwList,
                getLoginBody(oauthMetadata),
                null);

            comment = OAUTH_LOGIN_COMMENT;
            maker.addComment(methodTree, Comment.create(Style.JAVADOC, comment), true);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // getRequestToken method
            ExpressionTree requestTokenReturnTree = getResponseType(copy, oauthRequestTokenMethod);

            methodTree = maker.Method (
                privateModif,
                "getOAuthRequestToken", //NOI18N
                requestTokenReturnTree,
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>singletonList(uniformInterfaceEx),
                getRequestTokenBody(oauthMetadata),
                null);

            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // getAccessToken method
            ExpressionTree accessTokenReturnTree = getResponseType(copy, oauthAccessTokenMethod);

            List<VariableTree> paramList = new ArrayList<VariableTree>();
            VariableTree paramTree = maker.Variable(paramModifier, "requestTokenResponse", requestTokenReturnTree, null); //NOI18N
            paramList.add(paramTree);
            if (oauthAccessTokenMethod.isVerifier()) {
                paramTree = maker.Variable(paramModifier, "oauth_verifier", stringType, null); //NOI18N
                paramList.add(paramTree);
            }

            methodTree = maker.Method (
                privateModif,
                "getOAuthAccessToken", //NOI18N
                accessTokenReturnTree,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>singletonList(uniformInterfaceEx),
                getAccessTokenBody(oauthMetadata),
                null);

            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // setAccessToken method
            methodTree = maker.Method (
                publicModif,
                "initOAuth", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                getInitOAuthBody(oauthMetadata),
                null);
            comment = OAUTH_INIT_COMMENT;
            maker.addComment(methodTree, Comment.create(Style.JAVADOC, comment), true);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // makeOAuthRequestUnique
            if (isTimestamp(oauthMetadata) || isNonce(oauthMetadata)) {
                methodTree = maker.Method (
                    publicModif,
                    "makeOAuthRequestUnique", //NOI18N
                    maker.Identifier("void"), //NOI18N
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    getBodyForUniqueRequest(oauthMetadata),
                    null);
                comment = OAUTH_UNIQUE_COMMENT;
                maker.addComment(methodTree, Comment.create(Style.JAVADOC, comment), true);
                modifiedClass = maker.addClassMember(modifiedClass, methodTree);
            }

            // authorizeConsumer method
            VariableTree var = maker.Variable(paramModifier, "requestTokenResponse", requestTokenReturnTree, null); //NOI18N
            methodTree = maker.Method (
                privateModif,
                "authorizeConsumer", //NOI18N
                oauthAccessTokenMethod.isVerifier() ? maker.Identifier("java.lang.String") : maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>singletonList(var),
                Collections.<ExpressionTree>singletonList(iOEx),
                getAuthorizeConsumerBody(oauthMetadata),
                null);

            modifiedClass = maker.addClassMember(modifiedClass, methodTree);
        } else if (Wadl2JavaHelper.PROJEC_TYPE_WEB.equals(projectType)) {
            modifiersSet = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);
            ModifiersTree publicStaticModif =  maker.Modifiers(modifiersSet);

            // getRequestToken method
            ExpressionTree requestTokenReturnTree = getResponseType(copy, oauthRequestTokenMethod);
            List<VariableTree> paramList = new ArrayList<VariableTree>();
            MethodTree methodTree = maker.Method (
                privateStaticModif,
                "getOAuthRequestToken", //NOI18N
                requestTokenReturnTree,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>singletonList(uniformInterfaceEx),
                getRequestTokenBodyWeb(oauthMetadata),
                null);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // getAccessToken method
            ExpressionTree accessTokenReturnTree = getResponseType(copy, oauthAccessTokenMethod);
            ExpressionTree typeTree = JavaSourceHelper.createTypeTree(copy, "javax.servlet.http.HttpSession"); //NOI18N
            VariableTree paramTree = maker.Variable(paramModifier, "session", typeTree, null); //NOI18N
            paramList.add(paramTree);
            if (oauthAccessTokenMethod.isVerifier()) {
                paramTree = maker.Variable(paramModifier, "oauth_verifier", stringType, null); //NOI18N
                paramList.add(paramTree);
            }

            methodTree = maker.Method (
                privateStaticModif,
                "getOAuthAccessToken", //NOI18N
                accessTokenReturnTree,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>singletonList(uniformInterfaceEx),
                getAccessTokenBodyWeb(oauthMetadata),
                null);

            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // initOAuth method
            paramList = new ArrayList<VariableTree>();
            typeTree = JavaSourceHelper.createTypeTree(copy, "javax.servlet.http.HttpServletRequest"); //NOI18N
            paramTree = maker.Variable(paramModifier, "request", typeTree, null); //NOI18N
            paramList.add(paramTree);
            typeTree = JavaSourceHelper.createTypeTree(copy, "javax.servlet.http.HttpServletResponse"); //NOI18N
            paramTree = maker.Variable(paramModifier, "response", typeTree, null); //NOI18N
            paramList.add(paramTree);

            methodTree = maker.Method (
                publicModif,
                "initOAuth", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>singletonList(iOEx),
                getInitOAuthBodyWeb(oauthMetadata),
                null);
            comment = OAUTH_INIT_WEB_COMMENT;
            maker.addComment(methodTree, Comment.create(Style.JAVADOC, comment), true);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // makeOAuthRequestUnique
            if (isTimestamp(oauthMetadata) || isNonce(oauthMetadata)) {
                methodTree = maker.Method (
                    publicModif,
                    "makeOAuthRequestUnique", //NOI18N
                    maker.Identifier("void"), //NOI18N
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    getBodyForUniqueRequest(oauthMetadata),
                    null);
                comment = OAUTH_UNIQUE_COMMENT;
                maker.addComment(methodTree, Comment.create(Style.JAVADOC, comment), true);
                modifiedClass = maker.addClassMember(modifiedClass, methodTree);
            }

            // logout method
            typeTree = JavaSourceHelper.createTypeTree(copy, "javax.servlet.http.HttpServletRequest"); //NOI18N
            paramTree = maker.Variable(paramModifier, "request", typeTree, null); //NOI18N

            methodTree = maker.Method (
                publicStaticModif,
                "logout", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>singletonList(paramTree),
                Collections.<ExpressionTree>emptyList(),
                getLogoutBodyWeb(oauthMetadata),
                null);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

        } else if (Wadl2JavaHelper.PROJEC_TYPE_NB_MODULE.equals(projectType)) {
            modifiersSet = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);
            ModifiersTree publicStaticModif =  maker.Modifiers(modifiersSet);

            // login method
            MethodTree methodTree = maker.Method (
                publicStaticModif,
                "login", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>singletonList(uniformInterfaceEx),
                getLoginBodyNb(oauthMetadata, className),
                null);
            comment = OAUTH_LOGIN_COMMENT;
            maker.addComment(methodTree, Comment.create(Style.JAVADOC, comment), true);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);
            
            // logout method
            methodTree = maker.Method (
                publicStaticModif,
                "logout", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                getLogoutBodyNb(oauthMetadata, className),
                null);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // getOAuthRequestToken method
            ExpressionTree requestTokenReturnTree = getResponseType(copy, oauthRequestTokenMethod);

            methodTree = maker.Method (
                privateStaticModif,
                "getOAuthRequestToken", //NOI18N
                requestTokenReturnTree,
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>singletonList(uniformInterfaceEx),
                getRequestTokenBodyWeb(oauthMetadata),
                null);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // getOAuthAccessToken method
            ExpressionTree accessTokenReturnTree = getResponseType(copy, oauthRequestTokenMethod);

            List<VariableTree> paramList = new ArrayList<VariableTree>();
            VariableTree paramTree = maker.Variable(paramModifier, "requestTokenResponse", requestTokenReturnTree, null); //NOI18N
            paramList.add(paramTree);
            if (oauthAccessTokenMethod.isVerifier()) {
                paramTree = maker.Variable(paramModifier, "oauth_verifier", stringType, null); //NOI18N
                paramList.add(paramTree);
            }

            methodTree = maker.Method (
                privateStaticModif,
                "getOAuthAccessToken", //NOI18N
                accessTokenReturnTree,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>singletonList(uniformInterfaceEx),
                getAccessTokenBodyNb(oauthMetadata),
                null);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // initOAuth method
            methodTree = maker.Method (
                publicModif,
                "initOAuth", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                getInitOAuthBodyNb(oauthMetadata),
                null);
            comment = OAUTH_INIT_COMMENT;
            maker.addComment(methodTree, Comment.create(Style.JAVADOC, comment), true);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            // makeOAuthRequestUnique
            if (isTimestamp(oauthMetadata) || isNonce(oauthMetadata)) {
                methodTree = maker.Method (
                    publicModif,
                    "makeOAuthRequestUnique", //NOI18N
                    maker.Identifier("void"), //NOI18N
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    getBodyForUniqueRequest(oauthMetadata),
                    null);
                comment = OAUTH_UNIQUE_COMMENT;
                maker.addComment(methodTree, Comment.create(Style.JAVADOC, comment), true);
                modifiedClass = maker.addClassMember(modifiedClass, methodTree);
            }
            
            // authorizeConsumer method  
            paramTree = maker.Variable(paramModifier, "requestTokenResponse", requestTokenReturnTree, null); //NOI18N
            methodTree = maker.Method (
                privateStaticModif,
                "authorizeConsumer", //NOI18N
                maker.Identifier("java.lang.String"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>singletonList(paramTree),
                Collections.<ExpressionTree>emptyList(),
                getAuthorizeConsumerBodyNb(oauthMetadata),
                null);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

        }

        if (needXPath(oauthRequestTokenMethod)) {

            String format = oauthRequestTokenMethod.getResponseStyle();

            List<VariableTree> paramList = new ArrayList<VariableTree>();
            VariableTree paramTree = maker.Variable(paramModifier, "response", stringType, null); //NOI18N
            paramList.add(paramTree);
            String paramName = ("JSON".equals(format) ? "jsonPath" : "xPath"); //NOI18N
            paramTree = maker.Variable(paramModifier, paramName, stringType, null);
            paramList.add(paramTree);

            MethodTree methodTree = maker.Method (
                privateStaticModif,
                ("JSON".equals(format) ? "jsonSearch" : "xPathSearch"), //NOI18N
                stringType,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>emptyList(),
                getBodyForSearchMethod("JSON".equals(format) ? "JSON" : "XML"), //NOI18N
                null);
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);
        }

        return modifiedClass;
    }

    private static String getSignatureMethod(Metadata oauthMetadata, MethodType oauthMethod) {
        String signatureMethod = null;
        if (oauthMethod == null) {
            signatureMethod = oauthMetadata.getSignatureMethod().value();
        } else {
            SignatureMethodType sigMethod = oauthMethod.getSignatureMethod();
            if (sigMethod == null) {
                signatureMethod = oauthMetadata.getSignatureMethod().value();
            } else {
                signatureMethod = sigMethod.value();
            }
        }
        return "com.sun.jersey.oauth.signature."+signatureMethod+".NAME"; //NOI18N
    }

    private static String getVersion(Metadata oauthMetadata) {
        String version = oauthMetadata.getVersion();
        return version == null ? "1.0" : version; //NOI18N
    }
    private static String getVersion(MethodType oauthMethod) {
        String version = oauthMethod.getVersion();
        return version == null ? "1.0" : version; //NOI18N
    }

    private static boolean isNonce(Metadata oauthMetadata) {
        Boolean nounce = oauthMetadata.isNonce();
        return !Boolean.FALSE.equals(nounce);
    }

    private static boolean isNonce(MethodType oauthMethod) {
        Boolean nounce = oauthMethod.isNonce();
        return !Boolean.FALSE.equals(nounce);
    }

    private static boolean isTimestamp(Metadata oauthMetadata) {
        Boolean timestamp = oauthMetadata.isTimestamp();
        return !Boolean.FALSE.equals(timestamp);
    }

    private static boolean isTimestamp(MethodType oauthMethod) {
        Boolean timestamp = oauthMethod.isTimestamp();
        return !Boolean.FALSE.equals(timestamp);
    }

    private static boolean isCallback(MethodType oauthMethod) {
        Boolean callback = oauthMethod.isCallback();
        return Boolean.TRUE.equals(callback);
    }

    private static boolean isVerifier(MethodType oauthMethod) {
        Boolean verifier = oauthMethod.isVerifier();
        return Boolean.TRUE.equals(verifier);
    }

    private static String getRequestTokenBody(Metadata oauthMetadata) {
        MethodType oauthMethod = oauthMetadata.getFlow().getRequestToken();
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        String webResourceMethod=getWebResourceMethod(oauthMethod);
        bodyBuf.append("WebResource resource = client.resource(OAUTH_BASE_URL)."+webResourceMethod); //NOI18N
        bodyBuf.append("oauth_params = new OAuthParameters().consumerKey(CONSUMER_KEY)"); //NOI18N
        bodyBuf.append(".signatureMethod("+getSignatureMethod(oauthMetadata, oauthMethod)+")"); //NOI18N
        bodyBuf.append(".version(\""+getVersion(oauthMethod)+"\")"); //NOI18N
        if (isNonce(oauthMethod)) {
            bodyBuf.append(".nonce()"); //NOI18N
        }
        if (isTimestamp(oauthMethod)) {
            bodyBuf.append(".timestamp()"); //NOI18N
        }
        if (isCallback(oauthMethod)) {
            bodyBuf.append(".callback(CALLBACK_PAGE_URL)"); //NOI18N
        }
        bodyBuf.append(";"); //NOI18N
        bodyBuf.append("oauth_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET);"); //NOI18N
        bodyBuf.append("oauth_filter = new OAuthClientFilter(client.getProviders(), oauth_params, oauth_secrets);"); //NOI18N
        bodyBuf.append("resource.addFilter(oauth_filter);"); //NOI18N
        bodyBuf.append("return resource.get("+getResponseClass(oauthMethod.getResponseStyle(), true)+");"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }
    private static String getAccessTokenBody(Metadata oauthMetadata) {
        MethodType requestTokenMethod = oauthMetadata.getFlow().getRequestToken();
        MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        String webResourceMethod=getWebResourceMethod(accessTokenMethod);
        bodyBuf.append("WebResource resource = client.resource(OAUTH_BASE_URL)."+webResourceMethod); //NOI18N
        bodyBuf.append("oauth_params.token("+getParamFromResponse(oauthMetadata, requestTokenMethod.getResponseStyle(), "requestTokenResponse", "oauth_token")+")"); //NOI18N
        bodyBuf.append(".signatureMethod("+getSignatureMethod(oauthMetadata, accessTokenMethod)+")"); //NOI18N
        bodyBuf.append(".version(\""+getVersion(accessTokenMethod)+"\")"); //NOI18N
        if (isNonce(accessTokenMethod)) {
            bodyBuf.append(".nonce()"); //NOI18N
        }
        if (isTimestamp(accessTokenMethod)) {
            bodyBuf.append(".timestamp()"); //NOI18N
        }
        if (isCallback(requestTokenMethod)) {
            bodyBuf.append(".callback(null)"); //NOI18N
        }
        if (isVerifier(accessTokenMethod)) {
            bodyBuf.append(".verifier(oauth_verifier)"); //NOI18N
        }
        bodyBuf.append(";"); //NOI18N


        bodyBuf.append("oauth_secrets.tokenSecret("+getParamFromResponse(oauthMetadata, requestTokenMethod.getResponseStyle(), "requestTokenResponse", "oauth_token_secret")+");"); //NOI18N
        bodyBuf.append("resource.addFilter(oauth_filter);"); //NOI18N
        bodyBuf.append("return resource.get("+getResponseClass(accessTokenMethod.getResponseStyle(), true)+");"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getInitOAuthBody(Metadata oauthMetadata) {
       //MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("oauth_params = new OAuthParameters().consumerKey(CONSUMER_KEY).token(oauth_access_token)"); //NOI18N
        bodyBuf.append(".signatureMethod("+getSignatureMethod(oauthMetadata, null)+")"); //NOI18N
        bodyBuf.append(".version(\""+getVersion(oauthMetadata)+"\")"); //NOI18N
        if (isNonce(oauthMetadata)) {
            bodyBuf.append(".nonce()"); //NOI18N
        }
        if (isTimestamp(oauthMetadata)) {
            bodyBuf.append(".timestamp()"); //NOI18N
        }
        bodyBuf.append(";"); //NOI18N
        bodyBuf.append("oauth_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET).tokenSecret(oauth_access_token_secret);"); //NOI18N
        bodyBuf.append("oauth_filter = new OAuthClientFilter(client.getProviders(), oauth_params, oauth_secrets);"); //NOI18N
        bodyBuf.append("webResource.addFilter(oauth_filter);"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getAuthorizeConsumerBody(Metadata oauthMetadata) {
        MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("try {"); //NOI18N
        bodyBuf.append("java.awt.Desktop.getDesktop().browse(new java.net.URI("+getAuthorizationUrl(oauthMetadata)+"));"); //NOI18N
        bodyBuf.append("} catch (java.net.URISyntaxException ex) {"); //NOI18N
        bodyBuf.append("ex.printStackTrace();"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        if (accessTokenMethod.isVerifier()) {
            bodyBuf.append("java.io.BufferedReader br = null;"); //NOI18N
            bodyBuf.append("String oauth_verifier = null;"); //NOI18N
            bodyBuf.append("try {"); //NOI18N
            bodyBuf.append("br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));"); //NOI18N
            bodyBuf.append("System.out.print(\"Type oauth_verifier string (taken from callback page url):\");"); //NOI18N
            bodyBuf.append("oauth_verifier=br.readLine();"); //NOI18N
            bodyBuf.append("} finally { br.close(); }"); //NOI18N
            bodyBuf.append("return oauth_verifier;"); //NOI18N
        } else {
            bodyBuf.append("System.out.print(\"After you authorize the application press <Enter>:\");System.in.read();"); //NOI18N
        }
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getLoginBody(Metadata oauthMetadata) {
        MethodType requestTokenMethod = oauthMetadata.getFlow().getRequestToken();
        MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        String verifierPrefix = accessTokenMethod.isVerifier() ? "String oauth_verifier = " : ""; //NOI18N
        String verifierPostfix = accessTokenMethod.isVerifier() ? ", oauth_verifier" : ""; //NOI18N
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append(getResponseClass(requestTokenMethod.getResponseStyle(), false)+" requestTokenResponse = getOAuthRequestToken();"); //NOI18N
        bodyBuf.append(verifierPrefix+"authorizeConsumer(requestTokenResponse);"); //NOI18N
        bodyBuf.append(getResponseClass(accessTokenMethod.getResponseStyle(), false)+" accessTokenResponse = getOAuthAccessToken(requestTokenResponse"+verifierPostfix+");"); //NOI18N
        bodyBuf.append("oauth_access_token_secret = "+getParamFromResponse(oauthMetadata, accessTokenMethod.getResponseStyle(), "accessTokenResponse", "oauth_token_secret")+";"); //NOI18N
        bodyBuf.append("oauth_access_token  = "+getParamFromResponse(oauthMetadata, accessTokenMethod.getResponseStyle(), "accessTokenResponse", "oauth_token")+";"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getWebResourceMethod(MethodType method) {
        String requestStyle = method.getRequestStyle();
        String webResourceMethod=null;
        if ("PATH".equals(requestStyle)) { //NOI18N
            webResourceMethod = "path(\""+method.getMethodName()+"\")"; //NOI18N
        } else if ("HEADER".equals(requestStyle)) { //NOI18N
            String paramName = method.getRequestParam();
            if (paramName == null) {
                paramName = "method";
            }
            webResourceMethod = "header(\""+paramName+"\",\""+method.getMethodName()+"\")";  //NOI18N
        } else {
            String paramName = method.getRequestParam();
            if (paramName == null) {
                paramName = "method";
            }
            webResourceMethod = "queryParam(\""+paramName+"\",\""+method.getMethodName()+"\")";  //NOI18N
        }
        return webResourceMethod;
    }

    private static String getResponseClass(String responseStyle, boolean extension) {
        String responseClass="String"; //NOI18N
        if ("FORM".equals(responseStyle)) { //NOI18N
            responseClass = "Form"; //NOI18N
        }
        return (extension ? responseClass+".class" : responseClass); //NOI18N
    }

    private static String getAuthorizationUrl(Metadata oauthMetadata) {
        AuthorizationType auth = oauthMetadata.getFlow().getAuthorization();
        if (auth.getFixedUrl() != null) {
            StringBuffer buf = new StringBuffer(auth.getFixedUrl().getUrl());
            List<ParamType> params =  auth.getParam();
            if (params.size() > 0) {
                buf.append("?"); //NOI18N
            }
            int i=0;
            for (ParamType p : auth.getParam()) {
                if (i++ > 0) {
                    buf.append("+\"&"); //NOI18N
                }
                String paramName = p.getParamName();
                String oauthName = p.getOauthName();
                if (paramName == null) {
                    paramName = oauthName;
                }
                String paramValue = getParamFromResponse(oauthMetadata, oauthMetadata.getFlow().getRequestToken().getResponseStyle(), "requestTokenResponse", oauthName); //NOI18N
                buf.append(paramName+"=\"+"+paramValue);
            }
            return "\""+buf.toString()+(params.size() == 0 ? "\"" : ""); //NOI18N
        } else {
            DynamicUrlType dynamicUrl = auth.getDynamicUrl();
            return getParamFromResponse(oauthMetadata, oauthMetadata.getFlow().getRequestToken().getResponseStyle(), "requestTokenResponse", dynamicUrl.getAuthParamName()); //NOI18N
        }
    }

    private static String getParamFromResponse(Metadata oauthMetadata, String responseStyle, String responseFieldName, String oauthName) {
        if ("FORM".equals(responseStyle)) { //NOI18N
            return responseFieldName+".getFirst(\""+oauthName+"\")"; //NOI18N
        } else if ("XML".equals(responseStyle)) { //NOI18N
            String xPath = getXPathForParam(oauthMetadata, oauthName);
            if (xPath != null) {
                return "xPathSearch("+responseFieldName+",\""+xPath+"\")"; //NOI18N
            }
        } else if ("JSON".equals(responseStyle)) { //NOI18N
            String xPath = getXPathForParam(oauthMetadata, oauthName);
            if (xPath != null) {
                return "jsonSearch("+responseFieldName+",\""+xPath+"\")"; //NOI18N
            }
        }
        return ""; //NOI18N
    }

    private static ExpressionTree getResponseType(WorkingCopy copy, MethodType method) {
        String responseStyle = method.getResponseStyle();
        if ("FORM".equals(responseStyle)) { //NOI18N
            return JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.representation.Form"); //NOI18N
        } else {
            return copy.getTreeMaker().Identifier("java.lang.String"); //NOI18N
        }
    }

    private static String getRequestTokenBodyWeb(Metadata oauthMetadata) {
        MethodType oauthMethod = oauthMetadata.getFlow().getRequestToken();
        String webResourceMethod=getWebResourceMethod(oauthMethod);
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("Client reqTokenClient = new Client();"); //NOI18N
        
        bodyBuf.append("WebResource resource = reqTokenClient.resource(OAUTH_BASE_URL)."+webResourceMethod); //NOI18N
        bodyBuf.append("OAuthParameters o_params = new OAuthParameters().consumerKey(CONSUMER_KEY)"); //NOI18N
        bodyBuf.append(".signatureMethod("+getSignatureMethod(oauthMetadata, oauthMethod)+")"); //NOI18N
        bodyBuf.append(".version(\""+getVersion(oauthMethod)+"\")"); //NOI18N
        if (isNonce(oauthMethod)) {
            bodyBuf.append(".nonce()"); //NOI18N
        }
        if (isTimestamp(oauthMethod)) {
            bodyBuf.append(".timestamp()"); //NOI18N
        }
        if (isCallback(oauthMethod)) {
            bodyBuf.append(".callback(CALLBACK_PAGE_URL)"); //NOI18N
        }
        bodyBuf.append(";"); //NOI18N
        bodyBuf.append("OAuthSecrets o_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET);"); //NOI18N
        bodyBuf.append("OAuthClientFilter o_filter = new OAuthClientFilter(reqTokenClient.getProviders(), o_params, o_secrets);"); //NOI18N
        bodyBuf.append("resource.addFilter(o_filter);"); //NOI18N
        bodyBuf.append("return resource.get("+getResponseClass(oauthMethod.getResponseStyle(), true)+");"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getAccessTokenBodyWeb(Metadata oauthMetadata) {
        //MethodType requestTokenMethod = oauthMetadata.getFlow().getRequestToken();
        MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        String webResourceMethod=getWebResourceMethod(accessTokenMethod);
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("Client accessTokenClient = new Client();"); //NOI18N
        bodyBuf.append("WebResource resource = accessTokenClient.resource(OAUTH_BASE_URL)."+webResourceMethod); //NOI18N
        bodyBuf.append("OAuthParameters o_params = new OAuthParameters().consumerKey(CONSUMER_KEY)");
        bodyBuf.append(".token((String)session.getAttribute(\"oauth_token\"))"); //NOI18N
        bodyBuf.append(".signatureMethod("+getSignatureMethod(oauthMetadata, accessTokenMethod)+")"); //NOI18N
        bodyBuf.append(".version(\""+getVersion(accessTokenMethod)+"\")"); //NOI18N
        if (isNonce(accessTokenMethod)) {
            bodyBuf.append(".nonce()"); //NOI18N
        }
        if (isTimestamp(accessTokenMethod)) {
            bodyBuf.append(".timestamp()"); //NOI18N
        }
        if (isVerifier(accessTokenMethod)) {
            bodyBuf.append(".verifier(oauth_verifier)"); //NOI18N
        }
        bodyBuf.append(";"); //NOI18N


        bodyBuf.append("OAuthSecrets o_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET).tokenSecret((String)session.getAttribute(\"oauth_token_secret\"));"); //NOI18N
        bodyBuf.append("OAuthClientFilter o_filter = new OAuthClientFilter(accessTokenClient.getProviders(), o_params, o_secrets);"); //NOI18N
        bodyBuf.append("resource.addFilter(o_filter);"); //NOI18N
        bodyBuf.append("return resource.get("+getResponseClass(accessTokenMethod.getResponseStyle(), true)+");"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getAccessTokenBodyNb(Metadata oauthMetadata) {
        MethodType requestTokenMethod = oauthMetadata.getFlow().getRequestToken();
        //MethodType requestTokenMethod = oauthMetadata.getFlow().getRequestToken();
        MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        String webResourceMethod=getWebResourceMethod(accessTokenMethod);
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("Client accessTokenClient = new Client();"); //NOI18N
        bodyBuf.append("WebResource resource = accessTokenClient.resource(OAUTH_BASE_URL)."+webResourceMethod); //NOI18N
        bodyBuf.append("OAuthParameters o_params = new OAuthParameters().consumerKey(CONSUMER_KEY)");
        bodyBuf.append(".token("+getParamFromResponse(oauthMetadata, requestTokenMethod.getResponseStyle(), "requestTokenResponse", "oauth_token")+")"); //NOI18N
        bodyBuf.append(".signatureMethod("+getSignatureMethod(oauthMetadata, accessTokenMethod)+")"); //NOI18N
        bodyBuf.append(".version(\""+getVersion(accessTokenMethod)+"\")"); //NOI18N
        if (isNonce(accessTokenMethod)) {
            bodyBuf.append(".nonce()"); //NOI18N
        }
        if (isTimestamp(accessTokenMethod)) {
            bodyBuf.append(".timestamp()"); //NOI18N
        }
        if (isVerifier(accessTokenMethod)) {
            bodyBuf.append(".verifier(oauth_verifier)"); //NOI18N
        }
        bodyBuf.append(";"); //NOI18N


        bodyBuf.append("OAuthSecrets o_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET).tokenSecret("+getParamFromResponse(oauthMetadata, requestTokenMethod.getResponseStyle(), "requestTokenResponse", "oauth_token_secret")+");"); //NOI18N
        bodyBuf.append("OAuthClientFilter o_filter = new OAuthClientFilter(accessTokenClient.getProviders(), o_params, o_secrets);"); //NOI18N
        bodyBuf.append("resource.addFilter(o_filter);"); //NOI18N
        bodyBuf.append("return resource.get("+getResponseClass(accessTokenMethod.getResponseStyle(), true)+");"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getInitOAuthBodyWeb(Metadata oauthMetadata) {
        //MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("HttpSession session = request.getSession();"); //NOI18N
        bodyBuf.append("if ((String)session.getAttribute(\"oauth_token\") == null) {"); //NOI18N
        bodyBuf.append("    response.sendRedirect(request.getContextPath() + \"/OAuthLogin\");"); //NOI18N
        bodyBuf.append("} else {"); //NOI18N
        bodyBuf.append("oauth_params = new OAuthParameters().consumerKey(CONSUMER_KEY).token((String)session.getAttribute(\"oauth_token\"))"); //NOI18N
        bodyBuf.append(".signatureMethod("+getSignatureMethod(oauthMetadata, null)+")"); //NOI18N
        bodyBuf.append(".version(\""+getVersion(oauthMetadata)+"\")"); //NOI18N
        if (isNonce(oauthMetadata)) {
            bodyBuf.append(".nonce()"); //NOI18N
        }
        if (isTimestamp(oauthMetadata)) {
            bodyBuf.append(".timestamp()"); //NOI18N
        }
        bodyBuf.append(";"); //NOI18N
        bodyBuf.append("oauth_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET).tokenSecret((String)session.getAttribute(\"oauth_token_secret\"));"); //NOI18N
        bodyBuf.append("oauth_filter = new OAuthClientFilter(client.getProviders(), oauth_params, oauth_secrets);"); //NOI18N
        bodyBuf.append("webResource.addFilter(oauth_filter);"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }
    

    private static String getInitOAuthBodyNb(Metadata oauthMetadata) {
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("java.util.prefs.Preferences prefs = org.openide.util.NbPreferences.forModule(this.getClass());"); //NOI18N
        bodyBuf.append("String oauth_token = prefs.get(\"oauth_token\", null);"); //NOI18N
        bodyBuf.append("String oauth_token_secret = prefs.get(\"oauth_token_secret\", null);"); //NOI18N
        bodyBuf.append("if (oauth_token == null || oauth_token_secret == null) {"); //NOI18N
        bodyBuf.append("    org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(\"You have to call the login() method first to authorize the application to access user data.\"));"); //NOI18N
        bodyBuf.append("} else {"); //NOI18N
        bodyBuf.append("oauth_params = new OAuthParameters().consumerKey(CONSUMER_KEY).token(oauth_token)"); //NOI18N
        bodyBuf.append(".signatureMethod("+getSignatureMethod(oauthMetadata, null)+")"); //NOI18N
        bodyBuf.append(".version(\""+getVersion(oauthMetadata)+"\")"); //NOI18N
        if (isNonce(oauthMetadata)) {
            bodyBuf.append(".nonce()"); //NOI18N
        }
        if (isTimestamp(oauthMetadata)) {
            bodyBuf.append(".timestamp()"); //NOI18N
        }
        bodyBuf.append(";"); //NOI18N
        bodyBuf.append("oauth_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET).tokenSecret(oauth_token_secret);"); //NOI18N
        bodyBuf.append("oauth_filter = new OAuthClientFilter(client.getProviders(), oauth_params, oauth_secrets);"); //NOI18N
        bodyBuf.append("webResource.addFilter(oauth_filter);"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }      

    private static String getBodyForUniqueRequest(Metadata oauthMetadata) {
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("if (oauth_params != null) {"); //NOI18N
        if (isNonce(oauthMetadata)) {
            bodyBuf.append("oauth_params.nonce()"); //NOI18N
            if (isTimestamp(oauthMetadata)) {
                bodyBuf.append(".timestamp();"); //NOI18N
            }
        } else if (isTimestamp(oauthMetadata)) {
            bodyBuf.append("oauth_params.timestamp();"); //NOI18N
        }
        bodyBuf.append("}"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getLoginBodyNb(Metadata oauthMetadata, String className) {
        MethodType requestTokenMethod = oauthMetadata.getFlow().getRequestToken();
        MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        String verifierPrefix = accessTokenMethod.isVerifier() ? "String oauth_verifier = " : ""; //NOI18N
        String verifierPostfix = accessTokenMethod.isVerifier() ? ", oauth_verifier" : ""; //NOI18N

        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append(getResponseClass(requestTokenMethod.getResponseStyle(), false)+" requestTokenResponse = getOAuthRequestToken();"); //NOI18N
        bodyBuf.append(verifierPrefix+"authorizeConsumer(requestTokenResponse);"); //NOI18N
        bodyBuf.append(getResponseClass(accessTokenMethod.getResponseStyle(), false)+" accessTokenResponse = getOAuthAccessToken(requestTokenResponse"+verifierPostfix+");"); //NOI18N
        bodyBuf.append("java.util.prefs.Preferences prefs = org.openide.util.NbPreferences.forModule("+className+".class);"); //NOI18N
        bodyBuf.append("prefs.put(\"oauth_token\", "+getParamFromResponse(oauthMetadata, accessTokenMethod.getResponseStyle(), "accessTokenResponse", "oauth_token")+");"); //NOI18N
        bodyBuf.append("prefs.put(\"oauth_token_secret\", "+getParamFromResponse(oauthMetadata, accessTokenMethod.getResponseStyle(), "accessTokenResponse", "oauth_token_secret")+");"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getLogoutBodyWeb(Metadata oauthMetadata) {
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("HttpSession session = request.getSession();"); //NOI18N
        bodyBuf.append("session.removeAttribute(\"oauth_token\");"); //NOI18N
        bodyBuf.append("session.removeAttribute(\"oauth_token_secret\");"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getLogoutBodyNb(Metadata oauthMetadata, String className) {
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append("java.util.prefs.Preferences prefs = org.openide.util.NbPreferences.forModule("+className+".class);"); //NOI18N
        bodyBuf.append("prefs.remove(\"oauth_token\");"); //NOI18N
        bodyBuf.append("prefs.remove(\"oauth_token_secret\");"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    private static String getAuthorizeConsumerBodyNb(Metadata oauthMetadata) {
        String dialogPanel = Wadl2JavaHelper.getMethodBody("Templates/SaaSServices/OAuthAuthorizationPanelNb.java"); //NOI18N
        //MethodType accessTokenMethod = oauthMetadata.getFlow().getAccessToken();
        StringBuffer bodyBuf = new StringBuffer();
        bodyBuf.append("{"); //NOI18N
        bodyBuf.append(dialogPanel); //NOI18N
        bodyBuf.append("String oauth_verifier = null;"); //NOI18N
        bodyBuf.append("String loginUrl = "+getAuthorizationUrl(oauthMetadata)+";"); //NOI18N
        bodyBuf.append("DialogPanel dialogPanel = new DialogPanel(loginUrl);"); //NOI18N
        bodyBuf.append("org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor(dialogPanel, \"OAuth Authentication Dialog\");"); //NOI18N
        bodyBuf.append("org.openide.DialogDisplayer.getDefault().notify(dd);"); //NOI18N
        bodyBuf.append("if (dd.getValue() == org.openide.DialogDescriptor.OK_OPTION) {"); //NOI18N
        bodyBuf.append("    oauth_verifier = dialogPanel.getVerifier();"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        bodyBuf.append("return oauth_verifier;"); //NOI18N
        bodyBuf.append("}"); //NOI18N
        return bodyBuf.toString();
    }

    static ClassTree addOAuthServlets(WorkingCopy copy, ClassTree originalClass, Metadata oauthMetadata, String clientClassName, boolean annotateServlet) {
        ClassTree modifiedClass = originalClass;
        TreeMaker maker = copy.getTreeMaker();
        TypeElement servletAn = copy.getElements().getTypeElement("javax.servlet.annotation.WebServlet"); //NOI18N    
        Set<Modifier> classModifs = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);

        // OAuthLoginServlet
        ModifiersTree classModifiers =  maker.Modifiers(classModifs);
        String className = "OAuthLoginServlet"; //NOI18N
        if (annotateServlet && servletAn != null) {
            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
            attrs.add(
                    maker.Assignment(maker.Identifier("name"), maker.Literal(className))); //NOI18N
            attrs.add(
                    maker.Assignment(maker.Identifier("urlPatterns"), maker.Literal("/OAuthLogin"))); //NOI18N

            AnnotationTree servletAnnotation = maker.Annotation(
                    maker.QualIdent(servletAn),
                    attrs);
            classModifiers =
                maker.addModifiersAnnotation(classModifiers, servletAnnotation);
        }

        ExpressionTree extendsTree = JavaSourceHelper.createTypeTree(copy, "javax.servlet.http.HttpServlet"); //NOI18N
        ClassTree innerClass = maker.Class (
                classModifiers,
                className,
                Collections.<TypeParameterTree>emptyList(),
                extendsTree,
                Collections.<Tree>emptyList(),
                Collections.<Tree>emptyList());

        ModifiersTree methodModifiers =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PROTECTED));
        ModifiersTree paramModifier =  maker.Modifiers(Collections.<Modifier>emptySet());

        // params
        List<VariableTree> paramList = new ArrayList<VariableTree>();
        ExpressionTree typeTree = JavaSourceHelper.createTypeTree(copy, "javax.servlet.http.HttpServletRequest"); //NOI18N
        VariableTree paramTree = maker.Variable(paramModifier, "request", typeTree, null); //NOI18N
        paramList.add(paramTree);
        typeTree = JavaSourceHelper.createTypeTree(copy, "javax.servlet.http.HttpServletResponse"); //NOI18N
        paramTree = maker.Variable(paramModifier, "response", typeTree, null); //NOI18N
        paramList.add(paramTree);

        // throws
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        ExpressionTree servletEx = JavaSourceHelper.createTypeTree(copy, "javax.servlet.ServletException"); //NOI18N
        ExpressionTree ioEx = JavaSourceHelper.createTypeTree(copy, "java.io.IOException"); //NOI18N
        throwsList.add(servletEx); throwsList.add(ioEx);
        
        MethodTree methodTree = maker.Method (
                methodModifiers,
                "doGet", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                throwsList,
                getOAuthLoginBody(oauthMetadata),
                null);

        ClassTree modifiedInnerClass = maker.addClassMember(innerClass, methodTree);
        modifiedClass = maker.addClassMember(modifiedClass, modifiedInnerClass);

        // OAuthCallbackServlet
        classModifiers =  maker.Modifiers(classModifs);
        className = "OAuthCallbackServlet"; //NOI18N
        if (annotateServlet && servletAn != null) {
            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
            attrs.add(
                    maker.Assignment(maker.Identifier("name"), maker.Literal(className))); //NOI18N
            attrs.add(
                    maker.Assignment(maker.Identifier("urlPatterns"), maker.Literal("/OAuthCallback"))); //NOI18N

            AnnotationTree servletAnnotation = maker.Annotation(
                    maker.QualIdent(servletAn),
                    attrs);
            classModifiers =
                maker.addModifiersAnnotation(classModifiers, servletAnnotation);
        }

        innerClass = maker.Class (
                classModifiers,
                className,
                Collections.<TypeParameterTree>emptyList(),
                extendsTree,
                Collections.<Tree>emptyList(),
                Collections.<Tree>emptyList());

        methodTree = maker.Method (
                methodModifiers,
                "doGet", //NOI18N
                maker.Identifier("void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                throwsList,
                getOAuthCallbackBody(oauthMetadata, clientClassName),
                null);
        
        modifiedInnerClass = maker.addClassMember(innerClass, methodTree);
        modifiedClass = maker.addClassMember(modifiedClass, modifiedInnerClass);

        return modifiedClass;
    }

    private static String getOAuthLoginBody(Metadata oauthMetadata) {
        String responseStyle = oauthMetadata.getFlow().getRequestToken().getResponseStyle();
        StringBuffer buf = new StringBuffer();
        buf.append("{"); //NOI18N
        buf.append("response.setContentType(\"text/html;charset=UTF-8\");");  //NOI18N
        buf.append("java.io.PrintWriter out = response.getWriter();"); //NOI18N
        buf.append("try {");  //NOI18N
        buf.append(getResponseClass(responseStyle, false)+" requestTokenResponse = null;");  //NOI18N
        buf.append("UniformInterfaceException uiEx = null;");  //NOI18N
        buf.append("try {");  //NOI18N
        buf.append("    requestTokenResponse = getOAuthRequestToken();"); //NOI18N
        buf.append("    javax.servlet.http.HttpSession session = request.getSession(true);"); //NOI18N
        buf.append("    session.setAttribute(\"oauth_token\", "+getParamFromResponse(oauthMetadata, responseStyle, "requestTokenResponse", "oauth_token")+");"); //NOI18N
        buf.append("    session.setAttribute(\"oauth_token_secret\", "+getParamFromResponse(oauthMetadata, responseStyle, "requestTokenResponse", "oauth_token_secret")+");"); //NOI18N
        buf.append("} catch (UniformInterfaceException ex) "); //NOI18N
        buf.append("    uiEx = ex;"); //NOI18N
        buf.append("}"); //NOI18N
        buf.append("out.println(\"<html>\");"); //NOI18N
        buf.append("out.println(\"<head>\");"); //NOI18N
        buf.append("out.println(\"<title>OAuth Login Servlet</title>\");"); //NOI18N
        buf.append("out.println(\"</head>\");"); //NOI18N
        buf.append("out.println(\"<body>\");"); //NOI18N
        buf.append("out.println(\"<h1>OAuth Login Servlet at \" + request.getContextPath() + \"</h1>\");"); //NOI18N
        buf.append("if (uiEx == null) {"); //NOI18N
        buf.append("out.println(\"<a href='\"+"+getAuthorizationUrl(oauthMetadata)+"+\"'>"); //NOI18N
        buf.append("Click at this link to authorize the application to access your data</a>\");"); //NOI18N
        buf.append("} else {"); //NOI18N
        buf.append("out.println(\"Problem to get request token: \"+uiEx.getResponse()+\": \"+uiEx.getResponse().getEntity(String.class));"); //NOI18N
        buf.append("}"); //NOI18N
        buf.append("out.println(\"</body>\");"); //NOI18N
        buf.append("out.println(\"</html>\");"); //NOI18N
        buf.append("} finally {"); //NOI18N
        buf.append("    out.close();"); //NOI18N
        buf.append("}"); //NOI18N
        buf.append("}"); //NOI18N
        return buf.toString();
    }

    private static String getOAuthCallbackBody(Metadata oauthMetadata, String clientClassName) {
        String responseStyle = oauthMetadata.getFlow().getAccessToken().getResponseStyle();
        boolean isVerifier = isVerifier(oauthMetadata.getFlow().getAccessToken());
        StringBuffer buf = new StringBuffer();
        buf.append("{"); //NOI18N
        buf.append("response.setContentType(\"text/html;charset=UTF-8\");");  //NOI18N
        buf.append("java.io.PrintWriter out = response.getWriter();"); //NOI18N
        buf.append("try {");  //NOI18N
        if (isVerifier) {
            buf.append("String oauth_verifier = request.getParameter(\"oauth_verifier\");"); //NOI18N
        }
        buf.append(getResponseClass(responseStyle, false)+" accessTokenResponse = null;"); //NOI18N
        buf.append("UniformInterfaceException uiEx = null;"); //NOI18N
        buf.append("try {"); //NOI18N
        buf.append("    javax.servlet.http.HttpSession session = request.getSession(true);"); //NOI18N
        buf.append("    accessTokenResponse = getOAuthAccessToken(session"+(isVerifier?", oauth_verifier":"")+");"); //NOI18N
        buf.append("    session.setAttribute(\"oauth_token\", "+getParamFromResponse(oauthMetadata, responseStyle, "accessTokenResponse", "oauth_token")+");"); //NOI18N
        buf.append("    session.setAttribute(\"oauth_token_secret\", "+getParamFromResponse(oauthMetadata, responseStyle, "accessTokenResponse", "oauth_token_secret")+");"); //NOI18N
        buf.append("} catch (UniformInterfaceException ex) {"); //NOI18N
        buf.append("    uiEx = ex;"); //NOI18N
        buf.append("}"); //NOI18N
        buf.append("out.println(\"<html>\");"); //NOI18N
        buf.append("out.println(\"<head>\");"); //NOI18N
        buf.append("out.println(\"<title>OAuth Callback Servlet</title>\");"); //NOI18N
        buf.append("out.println(\"</head>\");"); //NOI18N
        buf.append("out.println(\"<body>\");"); //NOI18N
        buf.append("out.println(\"<h1>OAuth Callback Servlet at \" + request.getContextPath() + \"</h1>\");"); //NOI18N
        buf.append("if (uiEx == null) {"); //NOI18N
        buf.append("    out.println(\"Now, you have successfully authorized this application to access your data.<br><br>\");"); //NOI18N
        buf.append("    out.println(\"Usage: <p><pre>\");"); //NOI18N
        buf.append("    out.println(\"   "+clientClassName+" client = new "+clientClassName+"(...);\");"); //NOI18N
        buf.append("    out.println(\"   client.initOAuth(httpServletRequest, httpServletResponse);\");"); //NOI18N
        buf.append("    out.println(\"   // call any method\");"); //NOI18N
        buf.append("    out.println(\"   client.close();\");"); //NOI18N
        buf.append("    out.println(\"</pre></p>\");"); //NOI18N
        buf.append("} else {"); //NOI18N
        buf.append("    out.println(\"Problem to get access token: \"+uiEx.getResponse()+\": \"+uiEx.getResponse().getEntity(String.class));"); //NOI18N
        buf.append("}"); //NOI18N
        buf.append("out.println(\"</body>\");"); //NOI18N
        buf.append("out.println(\"</html>\");"); //NOI18N
        buf.append("} finally {"); //NOI18N
        buf.append("    out.close();"); //NOI18N
        buf.append("}"); //NOI18N
        buf.append("}"); //NOI18N
        return buf.toString();
    }

    private static boolean needXPath(MethodType method) {
        return !"FORM".equals(method.getResponseStyle());
    }

    private static String getXPathForParam(Metadata oauthMetadata, String oathParamName) {
        for (ParamType p : oauthMetadata.getParam()) {
            if (oathParamName.equals(p.getOauthName())) {
                return p.getXpath();
            }
        }
        return null;
    }

    private static String getBodyForSearchMethod(String format) {
        return Wadl2JavaHelper.getMethodBody("Templates/SaaSServices/OAuthSearch"+format+".method");
    }
}
