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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;

import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.HttpMimeType;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.PathFormat;
import org.netbeans.modules.websvc.rest.client.Wadl2JavaHelper.Pair;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.oauth.Metadata;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Representation;
import org.netbeans.modules.websvc.saas.model.wadl.Request;
import org.netbeans.modules.websvc.saas.model.wadl.Response;
import org.openide.nodes.Node;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;


/**
 * @author ads
 *
 */
abstract class ClientGenerationStrategy {
    
    private static final String SIGN_PARAMS_METHOD="signParams"; //NOI18N

    abstract ClassTree generateFields(TreeMaker maker, WorkingCopy copy,ClassTree classTree,
            String resourceURI,Security security);

    abstract MethodTree generateConstructor(TreeMaker maker, WorkingCopy copy,
            ClassTree classTree, PathFormat pf, Security security);
    
    abstract MethodTree generateSubresourceMethod(TreeMaker maker, WorkingCopy copy,
            ClassTree classTree,PathFormat pf);
    
    abstract MethodTree generateClose(TreeMaker maker, WorkingCopy copy);
    
    abstract MethodTree generateBasicAuth(TreeMaker maker, WorkingCopy copy , 
            List<VariableTree> authParams) ;
    
    abstract MethodTree generateHttpPOSTMethod( WorkingCopy copy,
            HttpMethod httpMethod, HttpMimeType mimeType,
            boolean multipleMimeTypes );
    
    abstract Collection<? extends MethodTree> generateHttpGETMethod(
            WorkingCopy copy, HttpMethod httpMethod, HttpMimeType mimeType,
            boolean multipleMimeTypes );
    
    abstract MethodTree generateHttpGETMethod( WorkingCopy copy, 
            WadlSaasMethod saasMethod, HttpMimeType mimeType, 
            boolean multipleMimeTypes, HttpParams httpParams, Security security);
    
    abstract MethodTree generateHttpPOSTMethod( WorkingCopy copy,
            WadlSaasMethod saasMethod, HttpMimeType mimeType,
            boolean multipleMimeTypes, HttpParams httpParams, Security security );
    
    abstract MethodTree generateFormMethod(TreeMaker maker, WorkingCopy copy);
    
    abstract MethodTree generateOptionalFormMethod(TreeMaker maker , WorkingCopy copy );
    
    abstract ClassTree generateOAuthMethods( String projectType,
            WorkingCopy copy, ClassTree modifiedClass, Metadata oauthMetadata );
    
    /**
     * @return true if generation requires Jersey specific classes for specified method arguments.
     */
    abstract boolean requiresJersey(Node context, Security security); 
    
    List<MethodTree> generateHttpMethods( WorkingCopy copy,
            HttpMethod httpMethod )
    {
        List<MethodTree> httpMethods = new ArrayList<MethodTree>();
        String method = httpMethod.getType();
        if (RestConstants.GET_ANNOTATION.equals(method)) { // GET
            boolean found = false;
            String produces = httpMethod.getProduceMime();
            if (produces.length() > 0) {
                boolean multipleMimeTypes = produces.contains(","); // NOI18N
                for (HttpMimeType mimeType : HttpMimeType.values()) {
                    if (produces.contains(mimeType.getMimeType())) {
                        httpMethods.addAll(generateHttpGETMethod(copy,
                                httpMethod, mimeType, multipleMimeTypes));
                        found = true;
                    }
                }
            }
            if (!found) {
                httpMethods.addAll(generateHttpGETMethod(copy, httpMethod, null,
                        false));
            }
        }
        else if (RestConstants.PUT_ANNOTATION.equals(method)
                || RestConstants.POST_ANNOTATION.equals(method)
                || RestConstants.DELETE_ANNOTATION.equals(method))
        {
            boolean found = false;
            String consumes = httpMethod.getConsumeMime();
            if (consumes.length() > 0) { // NOI18N
                boolean multipleMimeTypes = consumes.contains(","); // NOI18N
                for (HttpMimeType mimeType : HttpMimeType.values()) {
                    if (consumes.contains(mimeType.getMimeType())) {
                        httpMethods.add(generateHttpPOSTMethod(copy, httpMethod,
                                mimeType, multipleMimeTypes));
                        found = true;
                    }
                }
            }
            if (!found) {
                httpMethods.add(generateHttpPOSTMethod(copy, httpMethod, null,
                        false));
            }
        }

        return httpMethods;
    }
    
    MethodTree generateConstructorAuthBasic(TreeMaker maker) {
        
        ModifiersTree methodModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC));

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        
        Tree argTypeTree = maker.Identifier("String"); //NOI18N
        ModifiersTree fieldModifier = maker.Modifiers(
                Collections.<Modifier>emptySet());
        VariableTree argFieldTree = maker.Variable(fieldModifier, 
                "username", argTypeTree, null); //NOI18N
        paramList.add(argFieldTree);
        argFieldTree = maker.Variable(fieldModifier, 
                "password", argTypeTree, null); //NOI18N
        paramList.add(argFieldTree);
        
        
        String body =
                "{"+
                     "this();" +
                     "setUsernamePassword(username, password);" +
                "}"; //NOI18N
        return maker.Constructor (
                methodModifier,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>emptyList(),
                body);
        
    }
    
    List<MethodTree> generateHttpMethods( WorkingCopy copy,
            WadlSaasMethod saasMethod, HttpParams httpParams, Security security )
    {
        List<MethodTree> httpMethods = new ArrayList<MethodTree>();
        //String methodName = saasMethod.getName();
        Method wadlMethod = saasMethod.getWadlMethod();
        String methodType = wadlMethod.getName();
        //HeaderParamsInfo headerParamsInfo = new HeaderParamsInfo(saasMethod);
        if (RestConstants.GET_ANNOTATION.equals(methodType)) { //GET
            List<Representation> produces = new ArrayList<Representation>();
            for( Response wadlResponse : wadlMethod.getResponse() )
            if (wadlResponse != null) {
                List<Representation> representations = wadlResponse.getRepresentation();
                produces.addAll(representations);
            }
            
            boolean found = false;
            boolean multipleMimeTypes = produces.size() > 1;
            for (Representation prod : produces) {
                String mediaType = prod.getMediaType();
                if (mediaType != null) {
                    for (HttpMimeType mimeType : HttpMimeType.values()) {
                        if (mediaType.equals(mimeType.getMimeType())) {
                            MethodTree method = generateHttpGETMethod(copy, 
                                    saasMethod, mimeType, multipleMimeTypes, 
                                    httpParams, security);
                            if (method != null) {
                                httpMethods.add(method);
                            }
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                httpMethods.add(generateHttpGETMethod(copy, saasMethod, null, 
                        false, httpParams, security));
            }

        } else if ( RestConstants.PUT_ANNOTATION.equals(methodType) ||
                    RestConstants.POST_ANNOTATION.equals(methodType) ||
                    RestConstants.DELETE_ANNOTATION.equals(methodType)
                  ) {
            List<Representation> consumes = new ArrayList<Representation>();
            Request wadlRequest = wadlMethod.getRequest();
            if (wadlRequest != null) {
                List<Representation> representationTypes = wadlRequest.getRepresentation();
                for (Representation reprType : representationTypes) {
                    consumes.add(reprType);
                }
            }

            boolean found = false;
            boolean multipleMimeTypes = consumes.size() > 1;
            for (Representation cons : consumes) {
                String mediaType = cons.getMediaType();
                if (mediaType != null) {
                    for (HttpMimeType mimeType : HttpMimeType.values()) {
                        if (mediaType.equals(mimeType.getMimeType())) {
                            MethodTree method = generateHttpPOSTMethod(copy, 
                                    saasMethod, mimeType, multipleMimeTypes, 
                                    httpParams, security);
                            if (method != null) {
                                httpMethods.add(method);
                            }
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                httpMethods.add(generateHttpPOSTMethod(copy, saasMethod, 
                        null, false, httpParams, security));
            }
        }
        return httpMethods;
    }
    
    protected abstract void buildQueryFormParams(StringBuilder queryString);
    
    protected abstract void buildQParams(StringBuilder queryString);

    protected String getPathExpression(PathFormat pf) {
        String[] arguments = pf.getArguments();
        if (arguments.length == 0) {
            return "\""+pf.getPattern()+"\"";                             //NOI18N
        } else {
            return "java.text.MessageFormat.format(\""+pf.getPattern()+
                    "\", new Object[] {"+getArgumentList(arguments)+"})"; //NOI18N
        }
    }
    
    protected String getArgumentList(String[] arguments) {
        if (arguments.length == 0) {
            return "";
        } else {
            StringBuilder buf = new StringBuilder(arguments[0]);
            for (int i=1 ; i<arguments.length ; i++) {
                buf.append(","+arguments[i]);
            }
            return buf.toString();
        }
    }
    
    protected void buildQueryParams( StringBuilder body, HttpMethod httpMethod, 
            List<VariableTree> paramList , TreeMaker maker)
    {
        Map<String, String> queryParams = httpMethod.getQueryParams();
        if ( queryParams.size() == 0 ){
            return;
        }
        for (Entry<String, String> entry : queryParams.entrySet()) {
            String paramName = entry.getKey();
            // default value is not needed in the client code
            //String defaultValue = entry.getValue();
            if ( paramName == null ){
                continue;
            }
            String clientParam = getClientParamName( paramName , paramList );
            Tree typeTree = maker.Identifier("String"); //NOI18N
            ModifiersTree fieldModifier = maker.Modifiers(Collections.<Modifier>emptySet());
            VariableTree fieldTree = maker.Variable(fieldModifier, clientParam, 
                    typeTree, null); //NOI18N
            paramList.add(fieldTree);
            
            body.append("if (");                                //NOI18N
            body.append(clientParam);
            body.append("!=null){");                            //NOI18N
            body.append("resource = resource.queryParam(\"");   //NOI18N
            body.append(paramName);
            body.append("\",");                                 //NOI18N
            body.append(clientParam);
            body.append(");}");                                 //NOI18N
        }
    }

    protected String getClientParamName( String paramName,
            List<VariableTree> paramList )
    {
        return getClientParamName(paramName, paramList, 0);
    }
    
    protected String getClientParamName( String paramName,
            List<VariableTree> paramList , int index)
    {
        String result = paramName;
        if ( index !=0 ){
            result = paramName +index;
        }
        for(VariableTree var: paramList ) {
            String name = var.getName().toString();
            if ( name.equals( result)){
                return getClientParamName(paramName, paramList, index +1);
            }
        }
        return result;
    }
    
    protected void addQueryParams(TreeMaker maker, HttpParams httpParams, 
            Security security,  List<VariableTree> paramList, 
            StringBuilder queryP, StringBuilder queryParamPart, 
            StringBuilder commentBuffer) 
    {
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
        SecurityParams securityParams = security.getSecurityParams();
        // adding form params
        if (httpParams.hasFormParams()) {
            for (String formParam : httpParams.getFormParams()) {
                if (securityParams == null ||
                       (!Wadl2JavaHelper.isSecurityParam(formParam, securityParams) 
                               && !Wadl2JavaHelper.isSignatureParam(formParam, securityParams))) 
                {
                    String javaIdentifier = Wadl2JavaHelper.makeJavaIdentifier(formParam);
                    VariableTree paramTree = maker.Variable(paramModifier, 
                            javaIdentifier, maker.Identifier("String"), null); //NOI18N
                    paramList.add(paramTree);
                    commentBuffer.append("@param "+javaIdentifier+" form parameter\n"); //NOI18N
                }
            }
            Pair<String> paramPair = null;
            if (securityParams != null) {
                paramPair = Wadl2JavaHelper.getParamList(httpParams.getFormParams(),
                        httpParams.getFixedFormParams(), securityParams);
            } else {
                paramPair = Wadl2JavaHelper.getParamList(httpParams.getFormParams(), 
                        httpParams.getFixedFormParams());
            }
            queryParamPart.append("String[] formParamNames = new String[] {"+
                    paramPair.getKey()+"};"); //NOI18N
            queryParamPart.append("String[] formParamValues = new String[] {"+
                    paramPair.getValue()+"};"); //NOI18N
        }
        // add query params
        if (httpParams.hasQueryParams()) {
            if (httpParams.hasRequiredQueryParams()) {
                // adding method parameters
                for (String requiredParam : httpParams.getRequiredQueryParams()) {
                   if (securityParams == null ||
                           (!Wadl2JavaHelper.isSecurityParam(requiredParam, securityParams) && 
                                   !Wadl2JavaHelper.isSignatureParam(requiredParam, securityParams))) 
                   {
                        String javaIdentifier = 
                                Wadl2JavaHelper.makeJavaIdentifier(requiredParam);
                        VariableTree paramTree = maker.Variable(paramModifier, 
                                javaIdentifier, maker.Identifier("String"), null); //NOI18N
                        paramList.add(paramTree);
                        commentBuffer.append("@param "+javaIdentifier+
                                " query parameter[REQUIRED]\n"); //NOI18N
                    }
                }
                // adding query params calculation to metthod body
                if (httpParams.hasMultipleParamsInList()) {
                    Pair<String> paramPair = null;
                    if (securityParams != null) {
                        paramPair = Wadl2JavaHelper.getParamList(
                                httpParams.getRequiredQueryParams(), 
                                httpParams.getFixedQueryParams(), securityParams);
                    } else {
                        paramPair = Wadl2JavaHelper.getParamList(
                                httpParams.getRequiredQueryParams(), 
                                httpParams.getFixedQueryParams());
                    }
                    queryParamPart.append("String[] queryParamNames = new String[] {"+
                            paramPair.getKey()+"};"); //NOI18N
                    queryParamPart.append("String[] queryParamValues = new String[] {"+
                            paramPair.getValue()+"};"); //NOI18N
                    if (Security.Authentication.SESSION_KEY == 
                            security.getAuthentication() && securityParams != null) 
                    {
                        String optParams = ""; //NOI18N
                        if (httpParams.hasOptionalQueryParams()) {
                            optParams = ", optionalQueryParams";
                        }
                        queryParamPart.append("String signature = "+
                                SIGN_PARAMS_METHOD+
                                "(queryParamNames, queryParamValues"+optParams+
                                ");"); //NOI18N
                        String sigParam = securityParams.getSignature();
                        queryP.append(".queryParam(\""+
                                sigParam+"\", signature)"); //NOI18N
                        buildQueryFormParams(queryP);
                    } else {
                        buildQueryFormParams(queryP); 
                    }
                } else {
                    List<String> requiredParams = httpParams.getRequiredQueryParams();
                    if (requiredParams.size() > 0) {
                        String paramName = requiredParams.get(0);
                        String paramValue = Wadl2JavaHelper.makeJavaIdentifier(
                                requiredParams.get(0));
                        if (Security.Authentication.SESSION_KEY == 
                                security.getAuthentication() && 
                                securityParams != null && httpParams.hasFormParams()) 
                        {
                            String optParams = ""; //NOI18N
                            if (httpParams.hasOptionalQueryParams()) {
                                optParams = ", optionalQueryParams";
                            }
                            queryParamPart.append("String signature = "+
                                    SIGN_PARAMS_METHOD+
                                    "(formParamNames, formParamValues"+optParams+");"); //NOI18N
                            String sigParam = securityParams.getSignature();
                            queryP.append(".queryParam(\""+sigParam+"\", signature)"); //NOI18N
                        } else {
                            queryP.append(".queryParam(\""+paramName+"\","+paramValue+")"); //NOI18N
                        }
                    } else {
                        Map<String, String> fixedParams = httpParams.getFixedQueryParams();
                        for(Entry<String, String> entry: fixedParams.entrySet()) {
                            String paramName = entry.getKey();
                            String paramValue = entry.getValue();
                            queryP.append(".queryParam(\""+paramName+"\",\""+paramValue+"\")"); //NOI18N"
                        }
                    }
                }
            } else if (httpParams.hasOptionalQueryParams()) {
                // optional params should be listed also when there are no required params
                for (String optionalParam : httpParams.getOptionalQueryParams()) {
                    String javaIdentifier = Wadl2JavaHelper.makeJavaIdentifier(
                            optionalParam);
                    VariableTree paramTree = maker.Variable(paramModifier, 
                            javaIdentifier, maker.Identifier("String"), null); //NOI18N
                    paramList.add(paramTree);
                    commentBuffer.append("@param "+javaIdentifier+" query parameter\n"); //NOI18N
                }
                // there are no fixed params in this case
                Pair<String> paramPair = Wadl2JavaHelper.getParamList(
                        httpParams.getOptionalQueryParams(), 
                        Collections.<String, String>emptyMap());
                queryParamPart.append("String[] queryParamNames = new String[] {"+
                        paramPair.getKey()+"};"); //NOI18N
                queryParamPart.append("String[] queryParamValues = new String[] {"+
                        paramPair.getValue()+"};"); //NOI18N
                buildQueryFormParams(queryP);
            }

            // add optional params (only when there are also some required params)
            if ((httpParams.hasOptionalQueryParams() && httpParams.hasRequiredQueryParams()) || 
                    httpParams.hasDefaultQueryParams()) 
            {
                VariableTree paramTree = maker.Variable(paramModifier, 
                        "optionalQueryParams", maker.Identifier("String..."), null); //NOI18N
                paramList.add(paramTree);

                commentBuffer.append("@param optionalQueryParams List of optional query parameters in the form of \"param_name=param_value\",...<br>\nList of optional query parameters:\n"); //NOI18N
                for (String otherParam : httpParams.getOptionalQueryParams()) {
                    commentBuffer.append("<LI>"+otherParam+" [OPTIONAL]\n"); //NOI18N
                }
                // add default params
                Map<String,String> defaultParams = httpParams.getDefaultQueryParams();
                for (String key : defaultParams.keySet()) {
                    commentBuffer.append("<LI>"+key+" [OPTIONAL, DEFAULT VALUE: \""+
                            defaultParams.get(key)+"\"]\n"); //NOI18N
                }
                buildQParams(queryP); 
            }
        }
    }
    
    protected void addHeaderParams( TreeMaker maker, HttpParams httpParams,
            List<VariableTree> paramList, StringBuilder queryP,
            StringBuilder commentBuffer )
    {
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
        // add header params
        if (httpParams.hasHeaderParams()) {
            for (String headerParam : httpParams.getHeaderParams()) {
                String javaIdentifier = Wadl2JavaHelper.makeJavaIdentifier(headerParam);
                VariableTree paramTree = maker.Variable(paramModifier, 
                        javaIdentifier, maker.Identifier("String"), null); //NOI18N
                paramList.add(paramTree);
                commentBuffer.append("@param "+javaIdentifier+" header parameter[REQUIRED]\n"); //NOI18N
                queryP.append(".header(\""+headerParam+"\","+javaIdentifier+")"); //NOI18N
            }
            Map<String, String> fixedHeaderParams = httpParams.getFixedHeaderParams();
            for (String paramName : fixedHeaderParams.keySet()) {
                String paramValue = fixedHeaderParams.get(paramName);
                queryP.append(".header(\""+paramName+"\",\""+paramValue+"\")"); //NOI18N
            }
        }
    }
    
    
    static PathFormat getPathFormat(String path) {
        String p = normalizePath(path); //NOI18N
        PathFormat pathFormat = new PathFormat();
        StringBuilder buf = new StringBuilder();
        List<String> arguments = new ArrayList<String>();
        for (int i=0 ; i<p.length() ; i++) {
            char ch = p.charAt(i);
            if (ch == '{') { //NOI18N
                int j=i+1;
                while (j<p.length() &&  p.charAt(j) != '}') { //NOI18N
                    j++;
                }
                String arg = p.substring(i+1,j);
                int index = arg.indexOf(':');
                if ( index > -1){
                    arg = arg.substring(0, index);
                }
                buf.append("{"+arguments.size()+"}"); //NOI18N
                arguments.add(arg);
                i = j;
            } else {
                buf.append(ch);
            }
        }

        pathFormat.setPattern(buf.toString().trim());
        pathFormat.setArguments(arguments.toArray(new String[arguments.size()]));
        return pathFormat;
    }
    
    static String normalizePath(String path) {
        String s = path;
        while (s.startsWith("/")) { //NOI18N
            s = s.substring(1);
        }
        while (s.endsWith("/")) { //NOI18N
            s = s.substring(0, s.length()-1);
        }
        return s;
    }

}
