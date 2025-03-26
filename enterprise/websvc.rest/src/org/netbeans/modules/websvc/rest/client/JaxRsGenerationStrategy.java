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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.Comment.Style;
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
 * 
 * JAX-RS 2.X client generation strategy
 * ( Jersey 2.X is used in specific cases ).
 * @author ads
 *
 */
class JaxRsGenerationStrategy extends ClientGenerationStrategy {

    private final String packagePrefix;

    public JaxRsGenerationStrategy(boolean jakarta) {
        this.packagePrefix = jakarta ? "jakarta.ws.rs" : "javax.ws.rs";
    }
    
    @Override
    protected void buildQueryFormParams(StringBuilder queryString){
        queryString.append(";\n ");                     // NOI18N
        queryString.append(packagePrefix);
        queryString.append(".core.Form form =");                     // NOI18N
        queryString.append("getQueryOrFormParams(queryParamNames, queryParamValues);\n");// NOI18N
        queryString.append(packagePrefix);
        queryString.append("core.MultivaluedMap<String,String> map = form.asMap();\n");// NOI18N
        queryString.append("for(java.util.Map.Entry<String,java.util.List<String>> entry: ");   // NOI18N
        queryString.append("map.entrySet()){\n");                           // NOI18N
        queryString.append("java.util.List<String> list = entry.getValue();\n");// NOI18N
        queryString.append("String[] values = list.toArray(new String[list.size()]);\n");// NOI18N
        queryString.append("webTarget = webTarget.queryParam(entry.getKey(),(Object[])values);\n");// NOI18N
        queryString.append("}");// NOI18N
    }
    
    @Override
    protected void buildQParams(StringBuilder queryString){
        queryString.append(packagePrefix);
        queryString.append(".core.MultivaluedMap<String,String> mapOptionalParams = "); // NOI18N
        queryString.append("getQParams(optionalQueryParams);\n");           // NOI18N
        queryString.append("for(java.util.Map.Entry<String,java.util.List<String>> entry: ");   // NOI18N
        queryString.append("mapOptionalParams.entrySet()){\n");                           // NOI18N
        queryString.append("java.util.List<String> list = entry.getValue();\n");// NOI18N
        queryString.append("String[] values = list.toArray(new String[list.size()]);\n");// NOI18N
        queryString.append("webTarget = webTarget.queryParam(entry.getKey(),(Object[])values);\n");// NOI18N
        queryString.append("}");// NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.client.ClientGenerationStrategy#generateFields(org.netbeans.api.java.source.TreeMaker, org.netbeans.api.java.source.WorkingCopy, com.sun.source.tree.ClassTree, java.lang.String, org.netbeans.modules.websvc.rest.client.Security)
     */
    @Override
    ClassTree generateFields( TreeMaker maker, WorkingCopy copy,
            ClassTree classTree, String resourceURI, Security security )
    {
     // add 3 fields
        ModifiersTree fieldModif =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        Tree typeTree = JavaSourceHelper.createTypeTree(copy, packagePrefix + ".client.WebTarget"); //NOI18N
        VariableTree fieldTree = maker.Variable(fieldModif, "webTarget", typeTree, null); //NOI18N
        ClassTree modifiedClass = maker.addClassMember(classTree, fieldTree);

        fieldModif =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        typeTree = JavaSourceHelper.createTypeTree(copy, packagePrefix + ".client.Client"); //NOI18N
        fieldTree = maker.Variable(fieldModif, "client", typeTree, null); //NOI18N
        modifiedClass = maker.addClassMember(modifiedClass, fieldTree);

        Set<Modifier> modifiersSet = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
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
     * @see org.netbeans.modules.websvc.rest.client.ClientGenerationStrategy#requiresJersey(org.openide.nodes.Node, org.netbeans.modules.websvc.rest.client.Security)
     */
    @Override
    boolean requiresJersey( Node context, Security security ) {
        /*
         *  There are only a couple of cases when JAX-RS 2.0 is not sufficient 
         *  for client code generation.
         */
        return security.isSSL()||Security.Authentication.BASIC.equals(security.getAuthentication());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.client.ClientGenerationStrategy#generateConstructor(org.netbeans.api.java.source.TreeMaker, org.netbeans.api.java.source.WorkingCopy, com.sun.source.tree.ClassTree, org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.PathFormat, org.netbeans.modules.websvc.rest.client.Security)
     */
    @Override
    MethodTree generateConstructor( TreeMaker maker, WorkingCopy copy,
            ClassTree classTree, PathFormat pf, Security security )
    {
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));
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

        String clientCreation = "   client = " + packagePrefix + ".client.ClientBuilder.newClient();";
        if (security.isSSL()) {
            clientCreation = "client = " + packagePrefix + ".client.ClientBuilder.newBuilder().sslContext(getSSLContext()).build();"; // NOI18N
        }

        String body =
                "{"+                                                            //NOI18N
                clientCreation+
                subresourceExpr +
                ("\"\"".equals(resURI) ?
                "   webTarget = client.target(BASE_URI);" : //NOI18N
                "   webTarget = client.target(BASE_URI).path("+resURI+");") + //NOI18N
                "}"; //NOI18N
        return maker.Constructor (
                methodModifier,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>emptyList(),
                body);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.client.ClientGenerationStrategy#generateSubresourceMethod(org.netbeans.api.java.source.TreeMaker, org.netbeans.api.java.source.WorkingCopy, com.sun.source.tree.ClassTree, org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.PathFormat)
     */
    @Override
    MethodTree generateSubresourceMethod( TreeMaker maker, WorkingCopy copy,
            ClassTree classTree, PathFormat pf )
    {
        String body =
                "{"+ //NOI18N
                "   String resourcePath = "+getPathExpression(pf)+";"+ //NOI18N
                "   webTarget = client.target(BASE_URI).path(resourcePath);"+ //NOI18N
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

    

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.client.ClientGenerationStrategy#generateClose(org.netbeans.api.java.source.TreeMaker, org.netbeans.api.java.source.WorkingCopy)
     */
    @Override
    MethodTree generateClose( TreeMaker maker, WorkingCopy copy ) {
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
                "   client.close();"+ //NOI18N
                "}", //NOI18N
                null); 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.client.ClientGenerationStrategy#generateBasicAuth(org.netbeans.api.java.source.TreeMaker, org.netbeans.api.java.source.WorkingCopy, java.util.List)
     */
    @Override
    MethodTree generateBasicAuth( TreeMaker maker, WorkingCopy copy,
            List<VariableTree> authParams )
    {
        ModifiersTree methodModifier = maker.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        
        String body =
                "{"+ //NOI18N
                "   webTarget.register(new org.glassfish.jersey.client.filter.HttpBasicAuthFilter(username, password));"+ //NOI18N
                "}"; //NOI18N
        return maker.Method(methodModifier,
                "setUsernamePassword", // NOI18N
                JavaSourceHelper.createTypeTree(copy, "void"), // NOI18N
                Collections.<TypeParameterTree> emptyList(), authParams,
                Collections.<ExpressionTree> emptyList(), body, null); // NOI18N
    }
    
    @Override
    MethodTree generateHttpPOSTMethod( WorkingCopy copy, HttpMethod httpMethod,
            HttpMimeType requestMimeType, boolean multipleMimeTypes )
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
        
        // create param list

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        String bodyParam = "null";
        if (methodPrefix.equals("delete")) { // NOI18N
            bodyParam = "";
        }
        if (requestMimeType != null) {
            if (requestMimeType == HttpMimeType.FORM) {
                // PENDING
            } else {
                VariableTree objectParam = maker.Variable(paramModifier, 
                        "requestEntity", maker.Identifier("Object"), null); //NOI18N
                paramList.add(objectParam);
                bodyParam= packagePrefix + ".client.Entity.entity(requestEntity,"+
                        requestMimeType.getMediaType()+")"; //NOI18N
            }
        }
        
        if ((packagePrefix + ".core.Response").equals(responseType)) {         //NOI18N
            TypeElement clientResponseEl = copy.getElements()
                    .getTypeElement(packagePrefix + ".core.Response"); //NOI18N
            ret = "return "; //NOI18N
            responseTree = (clientResponseEl == null ?
                copy.getTreeMaker().Identifier(packagePrefix + ".core.Response") : // NOI18N
                copy.getTreeMaker().QualIdent(clientResponseEl));
            bodyParam1 = (clientResponseEl == null ?
                (packagePrefix + ".core.Response.class") :      //NOI18N
                "Response.class");                                //NOI18N
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

        if (classParam != null) {
            paramList.add(classParam);
       
        }
        
        if ( bodyParam.length() >0 && bodyParam1.length() >0 ){
            bodyParam +=",";
        }

        // throws
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, packagePrefix + ".ClientErrorException");     //NOI18N

        if (path.length() == 0) {
            // body
            String body =
                "{"+ 
                    (requestMimeType == null ?
                        "   "+ret+"webTarget.request()."+methodPrefix+"("+
                        bodyParam +bodyParam1+");" :                    //NOI18N
                        "   "+ret+"webTarget.request("+
                            requestMimeType.getMediaType()+")."+methodPrefix+
                            "("+bodyParam+bodyParam1+");") +               //NOI18N
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
                            "   "+ret+"webTarget.path("+getPathExpression(pf)+
                            ").request()."+methodPrefix+"("+
                                bodyParam +bodyParam1+");" :  //NOI18N
                            "   "+ret+"webTarget.path("+getPathExpression(pf)+
                            ").request("+requestMimeType.getMediaType()+")."+
                            methodPrefix+"("+bodyParam+bodyParam1+");") +  //NOI18N
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

    @Override
    Collection<? extends MethodTree> generateHttpGETMethod( WorkingCopy copy,
            HttpMethod httpMethod, HttpMimeType mimeType,
            boolean multipleMimeTypes )
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

        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, packagePrefix + ".ClientErrorException"); //NOI18N

        StringBuilder body = new StringBuilder(
                "{ WebTarget resource = webTarget;");           // NOI18N
        StringBuilder resourceBuilder = new StringBuilder();
        if (path.length() == 0) {
            resourceBuilder.append(".request(");  // NOI18N
            if ( mimeType != null ){
                resourceBuilder.append(mimeType.getMediaType());
            }
            resourceBuilder.append(')');
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
                resourceBuilder.append(".request(");                   // NOI18N
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
    MethodTree generateHttpGETMethod( WorkingCopy copy,
            WadlSaasMethod saasMethod, HttpMimeType mimeType,
            boolean multipleMimeTypes, HttpParams httpParams, Security security )
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

        if (httpParams.hasQueryParams()) {
            addQueryParams(maker, httpParams, security, paramList, queryP,
                    queryParamPart, commentBuffer);
            if ( queryP.length()>0 && queryP.charAt(0)=='.'){
                queryP.insert(0, "webTarget = webTarget");
            }
        }
        
        queryP.append("return webTarget");

        commentBuffer.append("@return response object (instance of responseType class)"); //NOI18N

        if ( mimeType== null){
            queryP.append(".request()");
        }
        else {
            queryP.append(".request(");
            queryP.append(mimeType.getMediaType());
            queryP.append(')');
        }
        if(httpParams.hasHeaderParams()){
            addHeaderParams(maker, httpParams, paramList, queryP, commentBuffer);
        }
        String body =
                "{"+queryParamPart+ queryP+                                        //NOI18N
                    ".get("+bodyParam+");"+  //NOI18N
                "}";                                                        //NOI18N
        
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, packagePrefix + ".ClientErrorException"); //NOI18N
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
    MethodTree generateHttpPOSTMethod( WorkingCopy copy,
            WadlSaasMethod saasMethod, HttpMimeType requestMimeType,
            boolean multipleMimeTypes, HttpParams httpParams, Security security )
    {
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
        String bodyParam = "null";              //NOI18N
        if (methodPrefix.equals("delete")) { // NOI18N
            bodyParam = "";
        }
        String ret = ""; 

        StringBuilder commentBuffer = new StringBuilder();
        if (response != null && !response.isEmpty()) {
            VariableTree classParam = maker.Variable(paramModifier, 
                    "responseType", maker.Identifier("Class<T>"), null); //NOI18N
            responseTree = maker.Identifier("T");
            bodyParam1 = ", responseType"; // NOI18N
            typeParams =   Collections.<TypeParameterTree>singletonList(
                    maker.TypeParameter("T", Collections.<ExpressionTree>emptyList()));
            if (classParam != null) {
                paramList.add(classParam);
                commentBuffer.append("@param responseType Class representing the response\n"); // NOI18N
            }
            ret = "return "; //NOI18N
        } else {
            responseTree = maker.Identifier("void");
            typeParams = Collections.<TypeParameterTree>emptyList();
        }

        StringBuilder queryP = new StringBuilder();
        StringBuilder queryParamPart = new StringBuilder();

        if (httpParams.hasFormParams() || httpParams.hasQueryParams() ) 
        {
            addQueryParams(maker, httpParams, security, paramList, 
                    queryP, queryParamPart, commentBuffer);
            if ( queryP.length()>0 && queryP.charAt(0)=='.'){
                queryP.insert(0, "webTarget = webTarget");
            }
        }
        
        queryP.append(ret);
        queryP.append(" webTarget");
        
        if (requestMimeType != null) {
            if (requestMimeType == HttpMimeType.FORM && httpParams.hasFormParams()) {
                bodyParam = packagePrefix + ".client.Entity.form("
                        + "getQueryOrFormParams(formParamNames, formParamValues))"; //NOI18N
            } 
            else {
                VariableTree objectParam = maker.Variable(paramModifier, 
                        "requestEntity", maker.Identifier("Object"), null); //NOI18N
                paramList.add(0, objectParam);
                bodyParam = packagePrefix + ".client.Entity.entity(requestEntity, " + requestMimeType.getMediaType() + ")"; //NOI18N
                commentBuffer.append("@param requestEntity request data");
            }
        }

        commentBuffer.append("@return response object (instance of responseType class)"); //NOI18N

        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, 
                packagePrefix + ".ClientErrorException"); //NOI18N
        throwsList.add(throwsTree);

        if (Security.Authentication.SESSION_KEY == security.getAuthentication()) {
            ExpressionTree ioExceptionTree = JavaSourceHelper.createTypeTree(copy, 
                    "java.io.IOException"); //NOI18N
            throwsList.add(ioExceptionTree);
        }

        if ( requestMimeType== null){
            queryP.append(".request()");
        }
        else {
            queryP.append(".request(");
            queryP.append(requestMimeType.getMediaType());
            queryP.append(')');
        }
        
        if(httpParams.hasHeaderParams()){
            addHeaderParams(maker, httpParams, paramList, queryP, commentBuffer);
        }

        String body =
            "{"+queryParamPart + queryP+
                    "."+methodPrefix+"("+
                    bodyParam+bodyParam1+");" +  //NOI18N
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
    MethodTree generateFormMethod( TreeMaker maker, WorkingCopy copy ) {
        String form = packagePrefix + ".core.Form"; //NOI18N
        TypeElement mvMapEl = copy.getElements().getTypeElement(form);
        String mvType = mvMapEl==null? (packagePrefix + ".Form"):"Form"; //NOI18N

        String body =
        "{"+ //NOI18N
            mvType+" form = new " + packagePrefix + ".core.Form();"+ //NOI18N
            "for (int i=0;i< paramNames.length;i++) {" + //NOI18N
            "    if (paramValues[i] != null) {"+ //NOI18N
            "        form = form.param(paramNames[i], paramValues[i]);"+ //NOI18N
            "    }"+ //NOI18N
            "}"+ //NOI18N
            "return form;"+ //NOI18N
        "}"; //NOI18N
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PRIVATE));
        ExpressionTree returnTree =
                mvMapEl ==null ? 
                    copy.getTreeMaker().Identifier(packagePrefix + ".core.Form"):    //NOI18N
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
    MethodTree generateOptionalFormMethod( TreeMaker maker, WorkingCopy copy ) {
        String mvMapClass = packagePrefix + ".core.MultivaluedMap"; //NOI18N
        TypeElement mvMapEl = copy.getElements().getTypeElement(mvMapClass);
        String mvType = mvMapEl == null ? mvMapClass : "MultivaluedMap"; //NOI18N

        String body =
        "{"+ //NOI18N
            mvType+"<String,String> qParams = new " + packagePrefix + ".core.MultivaluedHashMap<String,String>();"+ //NOI18N
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
    ClassTree generateOAuthMethods( String projectType, WorkingCopy copy,
            ClassTree modifiedClass, Metadata oauthMetadata )
    {
        /*
         *  There is no OAuth functionality available in Jersey 2.X
         *  So this strategy should not be used when OAuth is required. 
         */
        assert false;
        return modifiedClass;
    }
}
