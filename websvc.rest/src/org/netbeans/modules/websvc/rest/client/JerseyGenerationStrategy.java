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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.rest.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.HttpMimeType;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.PathFormat;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.oauth.Metadata;
import org.netbeans.modules.websvc.saas.model.wadl.Response;
import org.openide.nodes.Node;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.EnumSet;


/**
 * Jersey 1.X generation strategy
 * 
 * @author ads
 *
 */
class JerseyGenerationStrategy extends ClientGenerationStrategy {

    @Override
    public ClassTree generateFields(TreeMaker maker, WorkingCopy copy,
            ClassTree classTree,String resourceURI,Security security) 
    {
        // add 3 fields
        ModifiersTree fieldModif =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        Tree typeTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.WebResource"); //NOI18N
        VariableTree fieldTree = maker.Variable(fieldModif, "webResource", typeTree, null); //NOI18N
        ClassTree modifiedClass = maker.addClassMember(classTree, fieldTree);

        fieldModif =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        typeTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.Client"); //NOI18N
        fieldTree = maker.Variable(fieldModif, "client", typeTree, null); //NOI18N
        modifiedClass = maker.addClassMember(modifiedClass, fieldTree);

        Set<Modifier> modifiersSet = new HashSet<Modifier>();
        modifiersSet.add(Modifier.PRIVATE);
        modifiersSet.add(Modifier.STATIC);
        modifiersSet.add(Modifier.FINAL);
        fieldModif =  maker.Modifiers(modifiersSet);
        typeTree = maker.Identifier("String"); //NOI18N

        String baseUri = resourceURI;
        if (security.isSSL() && resourceURI.startsWith("http:")) { //NOI18N
            baseUri = "https:"+resourceURI.substring(5); //NOI18N
        }
        fieldTree = maker.Variable(fieldModif, "BASE_URI", typeTree, maker.Literal(baseUri)); //NOI18N
        modifiedClass = maker.addClassMember(modifiedClass, fieldTree);      
        return modifiedClass;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.client.ClientGenerationStrategy#buildQueryFormParams(java.lang.StringBuilder)
     */
    @Override
    protected void buildQueryFormParams( StringBuilder queryString ) {
        queryString.append(".queryParams(getQueryOrFormParams(queryParamNames, queryParamValues))");//NOI18N
    }
    
    @Override
    protected void buildQParams(StringBuilder queryString){
        queryString.append( ".queryParams(getQParams(optionalQueryParams))");//NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.client.ClientGenerationStrategy#requiresJersey(org.openide.nodes.Node, org.netbeans.modules.websvc.rest.client.Security)
     */
    @Override
    boolean requiresJersey( Node context, Security security ) {
        // Jersey client generation strategy is completely based on Jersey and requires it in the classpath  
        return true;
    }
    
    @Override
    MethodTree generateConstructor(TreeMaker maker, WorkingCopy copy,
            ClassTree classTree, PathFormat pf, Security security)
    {
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));
        TypeElement clientEl = copy.getElements().getTypeElement(
                "com.sun.jersey.api.client.Client"); // NOI18N
        boolean isSubresource = (pf.getArguments().length>0);

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (isSubresource) {
            for (String arg : pf.getArguments()) {
                Tree argTypeTree = maker.Identifier("String"); //NOI18N
                ModifiersTree fieldModifier = maker.Modifiers(
                        Collections.<Modifier>emptySet());
                VariableTree argFieldTree = maker.Variable(fieldModifier, 
                        arg, argTypeTree, null); //NOI18N
                paramList.add(argFieldTree);
            }
        }

        String resURI = null; //NOI18N
        String subresourceExpr = ""; //NOI18N
        if (isSubresource) {
            subresourceExpr = "    String resourcePath = "+
                    getPathExpression(pf)+";";                      //NOI18N
            resURI = "resourcePath";                                //NOI18N
        } else {
            resURI = getPathExpression(pf); //NOI18N
        }

        String SSLExpr = security.isSSL() ?
            "// SSL configuration\n" + //NOI18N
            "config.getProperties().put(" +
            "com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, " + //NOI18N
            "new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), getSSLContext()));": //NOI18N
            ""; //NOI18N

        String body =
                "{"+                                                           //NOI18N
                "   com.sun.jersey.api.client.config.ClientConfig config = " + //NOI18N
                "new com.sun.jersey.api.client.config.DefaultClientConfig();"+ //NOI18N
                SSLExpr+
                "   client = "+(clientEl == null ? "com.sun.jersey.api.client.":"")+"Client.create(config);"+ //NOI18N
                subresourceExpr +
                ("\"\"".equals(resURI) ?
                "   webResource = client.resource(BASE_URI);" : //NOI18N
                "   webResource = client.resource(BASE_URI).path("+resURI+");") + //NOI18N
                "}"; //NOI18N
        return maker.Constructor (
                methodModifier,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>emptyList(),
                body);
    }

    @Override
    MethodTree generateSubresourceMethod(TreeMaker maker, WorkingCopy copy,
            ClassTree classTree,PathFormat pf)
    {
        String body =
                "{"+ //NOI18N
                "   String resourcePath = "+getPathExpression(pf)+";"+ //NOI18N
                "   webResource = client.resource(BASE_URI).path(resourcePath);"+ //NOI18N
                "}"; //NOI18N
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));
        
        List<VariableTree> paramList = new ArrayList<VariableTree>();
        for (String arg : pf.getArguments()) {
            Tree argTypeTree = maker.Identifier("String"); // NOI18N
            ModifiersTree fieldModifier = maker.Modifiers(Collections
                    .<Modifier> emptySet());
            VariableTree argFieldTree = maker.Variable(fieldModifier, arg,
                    argTypeTree, null); // NOI18N
            paramList.add(argFieldTree);
        }
        return maker.Method(methodModifier,
                "setResourcePath", // NOI18N
                JavaSourceHelper.createTypeTree(copy, "void"), // NOI18N
                Collections.<TypeParameterTree> emptyList(), paramList,
                Collections.<ExpressionTree> emptyList(), body, null); // NOI18N
    }
    
    @Override
    MethodTree generateBasicAuth(TreeMaker maker, WorkingCopy copy,
            List<VariableTree> authParams) 
    {
        ModifiersTree methodModifier = maker.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        
        String body =
                "{"+ //NOI18N
                "   client.addFilter(new com.sun.jersey.api.client.filter.HTTPBasicAuthFilter(username, password));"+ //NOI18N
                "}"; //NOI18N
        return maker.Method(methodModifier,
                "setUsernamePassword", // NOI18N
                JavaSourceHelper.createTypeTree(copy, "void"), // NOI18N
                Collections.<TypeParameterTree> emptyList(), authParams,
                Collections.<ExpressionTree> emptyList(), body, null); // NOI18N
    }
    
    @Override
    MethodTree generateClose(TreeMaker maker, WorkingCopy copy){
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));
        return maker.Method (
                methodModifier,
                "close", //NOI18N
                JavaSourceHelper.createTypeTree(copy, "void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                "{"+ //NOI18N
                "   client.destroy();"+ //NOI18N
                "}", //NOI18N
                null); 
    }
    
    @Override
    MethodTree generateHttpGETMethod(WorkingCopy copy, 
            WadlSaasMethod saasMethod, HttpMimeType mimeType, 
            boolean multipleMimeTypes, HttpParams httpParams, Security security) 
    {
        String methodName = Wadl2JavaHelper.makeJavaIdentifier(saasMethod.getName()) + 
                (multipleMimeTypes ? "_"+mimeType.name() : ""); //NOI18N

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        VariableTree classParam = maker.Variable(paramModifier, 
                "responseType", maker.Identifier("Class<T>"), null); //NOI18N
        ExpressionTree responseTree = maker.Identifier("T");
        String bodyParam = "responseType"; //NOI18N
        List<TypeParameterTree> typeParams = Collections.<TypeParameterTree>
            singletonList(maker.TypeParameter("T", Collections.<ExpressionTree>emptyList()));

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (classParam != null) {
            paramList.add(classParam);
        }

        StringBuilder queryP = new StringBuilder();
        StringBuilder queryParamPart = new StringBuilder();
        StringBuilder commentBuffer = new StringBuilder("@param responseType Class representing the response\n"); //NOI18N

        if (httpParams.hasQueryParams() || httpParams.hasHeaderParams()) {
            addQueryParams(maker, httpParams, security, paramList, queryP, queryParamPart, commentBuffer);
            addHeaderParams(maker, httpParams, paramList, queryP, commentBuffer);
        }

        commentBuffer.append("@return response object (instance of responseType class)"); //NOI18N
        String body =
            "{"+queryParamPart+ //NOI18N

                (mimeType == null ?
                    "   return webResource"+queryP+".get("+bodyParam+");" :  //NOI18N
                    "   return webResource"+queryP+".accept("+mimeType.getMediaType()+").get("+bodyParam+");") +  //NOI18N
            "}"; //NOI18N
        
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, 
                "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N
        throwsList.add(throwsTree);
        if (Security.Authentication.SESSION_KEY == security.getAuthentication()) 
        {
            ExpressionTree ioExceptionTree = JavaSourceHelper.createTypeTree(copy, 
                    "java.io.IOException"); //NOI18N
            throwsList.add(ioExceptionTree);
        }

        MethodTree method = maker.Method (
                            methodModifier,
                            methodName,
                            responseTree,
                            typeParams,
                            paramList,
                            throwsList,
                            body,
                            null);
        if (method != null) {
            Comment comment = Comment.create(Style.JAVADOC, commentBuffer.toString());
            maker.addComment(method, comment, true);
        }
        return method;
    }

    @Override
    MethodTree generateHttpPOSTMethod(WorkingCopy copy, 
            WadlSaasMethod saasMethod, HttpMimeType requestMimeType, 
            boolean multipleMimeTypes, HttpParams httpParams, Security security) {
        String methodName = saasMethod.getName() + 
                (multipleMimeTypes ? "_"+requestMimeType.name() : ""); //NOI18N
        String methodPrefix = saasMethod.getWadlMethod().getName().toLowerCase();

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        List<Response> response = saasMethod.getWadlMethod().getResponse();

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        ExpressionTree responseTree = null;
        List<TypeParameterTree> typeParams = null;
        String bodyParam1 = "";
        String bodyParam2 = "";
        String ret = ""; //NOI18N

        if (response != null && !response.isEmpty()) {
            VariableTree classParam = maker.Variable(paramModifier, 
                    "responseType", maker.Identifier("Class<T>"), null); //NOI18N
            responseTree = maker.Identifier("T");
            bodyParam1 = "responseType"; //NOI18N
            typeParams =   Collections.<TypeParameterTree>singletonList(
                    maker.TypeParameter("T", Collections.<ExpressionTree>emptyList()));
            if (classParam != null) {
                paramList.add(classParam);
            }
            ret = "return "; //NOI18N
        } else {
            responseTree = maker.Identifier("void");
            typeParams = Collections.<TypeParameterTree>emptyList();
        }

        StringBuilder queryP = new StringBuilder();
        StringBuilder queryParamPart = new StringBuilder();
        StringBuilder commentBuffer = new StringBuilder("@param responseType Class representing the response\n"); //NOI18N

        if (httpParams.hasFormParams() || httpParams.hasQueryParams() || 
                httpParams.hasHeaderParams()) 
        {
            addQueryParams(maker, httpParams, security, paramList, 
                    queryP, queryParamPart, commentBuffer);
            addHeaderParams(maker, httpParams, paramList, queryP, commentBuffer);
        }
        

        if (requestMimeType != null) {
            if (requestMimeType == HttpMimeType.FORM && httpParams.hasFormParams()) {
                bodyParam2=(bodyParam1.length() > 0 ? ", " : "") + 
                        "getQueryOrFormParams(formParamNames, formParamValues)"; //NOI18N
            } 
            else {
                VariableTree objectParam = maker.Variable(paramModifier, 
                        "requestEntity", maker.Identifier("Object"), null); //NOI18N
                paramList.add(0, objectParam);
                bodyParam2=(bodyParam1.length() > 0 ? ", " : "") + "requestEntity"; //NOI18N
                commentBuffer.append("@param requestEntity request data");
            }
        }

        commentBuffer.append("@return response object (instance of responseType class)"); //NOI18N

        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, 
                "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N
        throwsList.add(throwsTree);

        if (Security.Authentication.SESSION_KEY == security.getAuthentication()) {
            ExpressionTree ioExceptionTree = JavaSourceHelper.createTypeTree(copy, "java.io.IOException"); //NOI18N
            throwsList.add(ioExceptionTree);
        }

        String body =
            "{"+queryParamPart + //NOI18N
                (requestMimeType == null ?
                    "   "+ret+"webResource"+queryP+"."+methodPrefix+"("+bodyParam1+bodyParam2+");" :  //NOI18N
                    "   "+ret+"webResource"+queryP+".type("+requestMimeType.getMediaType()+
                    ")."+methodPrefix+"("+bodyParam1+bodyParam2+");") +  //NOI18N
            "}"; //NOI18N

        MethodTree method = maker.Method (
                methodModifier,
                methodName,
                responseTree,
                typeParams,
                paramList,
                throwsList,
                body,
                null); //NOI18N
        if (method != null) {
            Comment comment = Comment.create(Style.JAVADOC, commentBuffer.toString());
            maker.addComment(method, comment, true);
        }
        return method;
    }
    
    @Override
    MethodTree generateFormMethod(TreeMaker maker, WorkingCopy copy){
        String mvMapClass = "javax.ws.rs.core.MultivaluedMap"; //NOI18N
        TypeElement mvMapEl = copy.getElements().getTypeElement(mvMapClass);
        String mvType = mvMapEl == null ? mvMapClass : "MultivaluedMap"; //NOI18N

        String body =
        "{"+ //NOI18N
            mvType+"<String,String> qParams = new com.sun.jersey.api.representation.Form();"+ //NOI18N
            "for (int i=0;i< paramNames.length;i++) {" + //NOI18N
            "    if (paramValues[i] != null) {"+ //NOI18N
            "        qParams.add(paramNames[i], paramValues[i]);"+ //NOI18N
            "    }"+ //NOI18N
            "}"+ //NOI18N
            "return qParams;"+ //NOI18N
        "}"; //NOI18N
        ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        ExpressionTree returnTree =
                mvMapEl == null ?
                    copy.getTreeMaker().Identifier(mvMapClass) :
                    copy.getTreeMaker().QualIdent(mvMapEl);
        List<VariableTree> paramList = new ArrayList<VariableTree>();
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
        paramList.add(maker.Variable(paramModifier, "paramNames", maker.Identifier("String[]"), null)); //NOI18N
        paramList.add(maker.Variable(paramModifier, "paramValues", maker.Identifier("String[]"), null)); //NOI18N
        return maker.Method (
                methodModifier,
                "getQueryOrFormParams", //NOI18N
                returnTree,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>emptyList(),
                body,
                null); //NOI18N
        
    }
    
    @Override
    MethodTree generateOptionalFormMethod(TreeMaker maker , WorkingCopy copy ){
        String mvMapClass = "javax.ws.rs.core.MultivaluedMap"; //NOI18N
        TypeElement mvMapEl = copy.getElements().getTypeElement(mvMapClass);
        String mvType = mvMapEl == null ? mvMapClass : "MultivaluedMap"; //NOI18N

        String body =
        "{"+ //NOI18N
            mvType+"<String,String> qParams = new com.sun.jersey.api.representation.Form();"+ //NOI18N
           "for (String qParam : optionalParams) {" + //NOI18N
            "    String[] qPar = qParam.split(\"=\");"+ //NOI18N
            "    if (qPar.length > 1) qParams.add(qPar[0], qPar[1])"+ //NOI18N
            "}"+ //NOI18N
            "return qParams;"+ //NOI18N
        "}"; //NOI18N
        ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        ExpressionTree returnTree =
                mvMapEl == null ?
                    copy.getTreeMaker().Identifier(mvMapClass) :
                    copy.getTreeMaker().QualIdent(mvMapEl);
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
        VariableTree param = maker.Variable(paramModifier, "optionalParams", maker.Identifier("String..."), null); //NOI18N
        return maker.Method (
                methodModifier,
                "getQParams", //NOI18N
                returnTree,
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>singletonList(param),
                Collections.<ExpressionTree>emptyList(),
                body,
                null); //NOI18N
    }
    
    @Override
    ClassTree generateOAuthMethods( String projectType,
            WorkingCopy copy, ClassTree classTree, Metadata oauthMetadata)
    {
        return OAuthHelper.addOAuthMethods( projectType, copy, classTree, 
                oauthMetadata, classTree.getSimpleName().toString());
    }
    
    @Override
    Collection<MethodTree> generateHttpGETMethod(WorkingCopy copy, 
            HttpMethod httpMethod, HttpMimeType mimeType, boolean multipleMimeTypes) 
    {
        Collection<MethodTree> result = new ArrayList<MethodTree>(2);
        String responseType = httpMethod.getReturnType();
        String path = httpMethod.getPath();
        String methodName = httpMethod.getName() + (multipleMimeTypes ? 
                "_"+mimeType.name() : ""); //NOI18N

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(
                Collections.<Modifier>emptySet());

        VariableTree classParam = null;
        ExpressionTree responseTree = null;
        String bodyParam = ""; //NOI18N
        List<TypeParameterTree> typeParams =  null;

        if (String.class.getName().equals(responseType)) { 
            responseTree = maker.Identifier("String"); //NOI18N
            bodyParam="String.class"; //NOI18N
            typeParams =  Collections.<TypeParameterTree>emptyList();
        } else {
            responseTree = maker.Identifier("T"); //NOI18N
            bodyParam="responseType"; //NOI18N
            classParam = maker.Variable(paramModifier, "responseType", 
                    maker.Identifier("Class<T>"), null); //NOI18N
            typeParams = Collections.<TypeParameterTree>singletonList(
                    maker.TypeParameter("T", 
                            Collections.<ExpressionTree>emptyList())); //NOI18N
        }

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (classParam != null) {
            paramList.add(classParam);
        }

        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, 
                "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N

        StringBuilder body = new StringBuilder(
                "{ WebResource resource = webResource;");           // NOI18N
        StringBuilder resourceBuilder = new StringBuilder();
        if (path.length() == 0) {
            if ( mimeType != null ){
                resourceBuilder.append(".accept(");  // NOI18N
                resourceBuilder.append(mimeType.getMediaType());
                resourceBuilder.append(')');
            }
            buildQueryParams( body , httpMethod, paramList , maker );
        } 
        else {
            PathFormat pf = getPathFormat(path);
            for (String arg : pf.getArguments()) {
                Tree typeTree = maker.Identifier("String"); //NOI18N
                ModifiersTree fieldModifier = maker.Modifiers(Collections.<Modifier>emptySet());
                VariableTree fieldTree = maker.Variable(fieldModifier, arg, typeTree, null); //NOI18N
                paramList.add(fieldTree);
            }
            buildQueryParams( body , httpMethod, paramList , maker );
            
            body.append("resource=resource.path(");     // NOI18N
            body.append(getPathExpression(pf));
            body.append(')');
            if ( mimeType != null ){
                resourceBuilder.append(".accept(");                   // NOI18N
                resourceBuilder.append(mimeType.getMediaType());
                resourceBuilder.append(')');
            }

        }
        body.append( "return resource");                   // NOI18N
        body.append(resourceBuilder);
        body.append(".get(");                              // NOI18N
        body.append(bodyParam);
        body.append(");");                                 // NOI18N
        body.append('}');                                  // NOI18N
        MethodTree method = maker.Method (
                methodModifier,
                methodName,
                responseTree,
                typeParams,
                paramList,
                Collections.<ExpressionTree>singletonList(throwsTree), 
                body.toString(),
                null); 
        result.add( method );
        return result;
    }
    
    @Override
    MethodTree generateHttpPOSTMethod(WorkingCopy copy, HttpMethod httpMethod, 
            HttpMimeType requestMimeType, boolean multipleMimeTypes) 
    {
        String methodPrefix = httpMethod.getType().toLowerCase();
        String responseType = httpMethod.getReturnType();
        String path = httpMethod.getPath();
        String methodName = httpMethod.getName() + 
                (multipleMimeTypes ? "_"+requestMimeType.name() : ""); //NOI18N

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        VariableTree classParam = null;
        ExpressionTree responseTree = null;
        String bodyParam1 = ""; //NOI18N
        String ret = ""; //NOI18N
        List<TypeParameterTree> typeParams =  
                Collections.<TypeParameterTree>emptyList();
        if ("javax.ws.rs.core.Response".equals(responseType)) {         //NOI18N
            TypeElement clientResponseEl = copy.getElements().getTypeElement(
                    "com.sun.jersey.api.client.ClientResponse");        //NOI18N
            ret = "return "; //NOI18N
            responseTree = (clientResponseEl == null ?
                copy.getTreeMaker().Identifier("com.sun.jersey.api.client.ClientResponse") : // NOI18N
                copy.getTreeMaker().QualIdent(clientResponseEl));
            bodyParam1 = (clientResponseEl == null ?
                "com.sun.jersey.api.client.ClientResponse.class" :      //NOI18N
                "ClientResponse.class");                                //NOI18N
        } 
        else if ("void".equals(responseType)) {                         //NOI18N
            responseTree = maker.Identifier("void");                    //NOI18N
        } 
        else if (String.class.getName().equals(responseType)) {            
            responseTree = maker.Identifier("String");                  //NOI18N
            ret = "return "; //NOI18N
            bodyParam1="String.class";                                  //NOI18N
        } 
        else {
            responseTree = maker.Identifier("T");                       //NOI18N
            ret = "return ";                                            //NOI18N
            bodyParam1="responseType";                                  //NOI18N
            classParam = maker.Variable(paramModifier, "responseType", 
                    maker.Identifier("Class<T>"), null);                //NOI18N
            typeParams = Collections.<TypeParameterTree>singletonList(
                    maker.TypeParameter("T", Collections.<ExpressionTree>emptyList()));
        }

        // create param list

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (classParam != null) {
            paramList.add(classParam);
       
        }
        String bodyParam2 = "";
        if (requestMimeType != null) {
            if (requestMimeType == HttpMimeType.FORM) {
                // PENDING
            } else {
                VariableTree objectParam = maker.Variable(paramModifier, 
                        "requestEntity", maker.Identifier("Object"), null); //NOI18N
                paramList.add(objectParam);
                bodyParam2=(bodyParam1.length() > 0 ? ", " : "") + "requestEntity"; //NOI18N
            }
        }
        // throws
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, 
                "com.sun.jersey.api.client.UniformInterfaceException");     //NOI18N

        if (path.length() == 0) {
            // body
            String body =
                "{"+ 
                    (requestMimeType == null ?
                        "   "+ret+"webResource."+methodPrefix+"("+
                            bodyParam1+bodyParam2+");" :                    //NOI18N
                        "   "+ret+"webResource.type("+
                            requestMimeType.getMediaType()+")."+methodPrefix+
                            "("+bodyParam1+bodyParam2+");") +               //NOI18N
                "}";                                                        //NOI18N
            return maker.Method (
                    methodModifier,
                    methodName,
                    responseTree,
                    typeParams,
                    paramList,
                    Collections.singletonList(throwsTree),
                    body,
                    null); 
        } else {
            // add path params to param list
            PathFormat pf = getPathFormat(path);
            for (String arg : pf.getArguments()) {
                Tree typeTree = maker.Identifier("String");                 //NOI18N
                ModifiersTree fieldModifier = maker.Modifiers(
                        Collections.<Modifier>emptySet());
                VariableTree fieldTree = maker.Variable(fieldModifier, arg, 
                        typeTree, null); 
                paramList.add(fieldTree);
            }
            // body
            String body =
                    "{"+ //NOI18N
                        (requestMimeType == null ?
                            "   "+ret+"webResource.path("+getPathExpression(pf)+
                            ")."+methodPrefix+"("+bodyParam1+bodyParam2+");" :  //NOI18N
                            "   "+ret+"webResource.path("+getPathExpression(pf)+
                            ").type("+requestMimeType.getMediaType()+")."+
                            methodPrefix+"("+bodyParam1+bodyParam2+");") +  //NOI18N
                    "}"; //NOI18N
            return maker.Method (
                    methodModifier,
                    methodName,
                    responseTree,
                    typeParams,
                    paramList,
                    Collections.<ExpressionTree>singletonList(throwsTree),
                    body,
                    null); //NOI18N
        }
    }
    
}
