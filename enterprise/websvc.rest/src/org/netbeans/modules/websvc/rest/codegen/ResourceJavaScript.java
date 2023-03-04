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
package org.netbeans.modules.websvc.rest.codegen;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.model.Method;
import org.netbeans.modules.websvc.rest.codegen.model.Resource;
import org.netbeans.modules.websvc.rest.codegen.model.RestEntity;
import org.netbeans.modules.websvc.rest.codegen.model.RestEntity.EntityKind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * 
 * @author (refactored and changed by) ads
 *
 */
class ResourceJavaScript extends TokenReplacer {
    private static final String ENTITY_NAME_VAR = "entity_name";
    
    static final String RJSSUPPORT = "rjsSupport";         // NOI18N

    ResourceJavaScript( ClientStubsGenerator generator, Resource r, 
            FileObject jsFolder, Set<String> existingEntities) 
    {
        super(generator);
        ignoredEntities = existingEntities;
        resource = r;
        this.jsFolder = jsFolder;
        pkg = "";
        object = "";
        Map<String, String> tokens = new HashMap<String, String>();
        entities = new HashMap<String, String>();
        
        StringBuilder restMethods = new StringBuilder();
        StringBuilder stubsMethods = new StringBuilder();
        createRestMethods(resource, object, pkg, restMethods , stubsMethods);
        
        tokens.put("generic_name", resource.getName());          // NOI18N
        tokens.put("rest_methods", restMethods.toString());      // NOI18N
        tokens.put("stub_methods", stubsMethods.toString());      // NOI18N
        setTokens(tokens);
    }

    public FileObject getFolder() {
        return jsFolder;
    }

    public FileObject generate() throws IOException{
        Set<String> entityFiles = new HashSet<String>();
        for( String entityName : entities.keySet() ){
            if ( ignoredEntities.contains( entityName) ){
                continue;
            }
            Map<String,String> tokens = new HashMap<String, String>();
            tokens.put(ENTITY_NAME_VAR, entityName);
            FileObject entity = createResource(ClientStubsGenerator.JS_ENTITY_TEMPLATE, 
                    entityName ,tokens );
            entityFiles.add( entity.getName() );
        }
        StringBuilder wsName = new StringBuilder( resource.getName());
        while ( entityFiles.contains( wsName) ){
            wsName.append( "REST");             // NOI18N
        }
        resource.setEntities( entityFiles );
        FileObject fo = createResource( ClientStubsGenerator.JS_STUB_TEMPLATE , 
                wsName.toString(), null);
        
        return fo;
    }

    private FileObject createResource(  String templateName , String name , 
            Map<String,String> tokens ) throws IOException 
    {
        String fileNameExt = name + "." + ClientStubsGenerator.JS;
        FileObject fo = jsFolder.getFileObject(fileNameExt);
        if (fo != null) {
            if(getGenerator().canOverwrite()) {
                fo.delete();
            } else {
                Logger.getLogger(this.getClass().getName()).log(
                    Level.INFO, NbBundle.getMessage(ClientStubsGenerator.class,
                        "MSG_SkippingStubGeneration", jsFolder.getPath()+
                                File.separator+fileNameExt));
            }
        }
        
        if ( tokens == null ){
            fo = getGenerator().createDataObjectFromTemplate(
                    templateName , jsFolder, name, 
                    ClientStubsGenerator.JS, getGenerator().canOverwrite(), getTokens());
        }
        else {
            fo = getGenerator().createDataObjectFromTemplate(
                    templateName , jsFolder, name, 
                    ClientStubsGenerator.JS, getGenerator().canOverwrite(), tokens);
        }
        return fo;
    }

    protected void createRestMethods(Resource r, String object, String pkg, 
            StringBuilder restMethods, StringBuilder stubMethods) {
        String rjSupport =  RJSSUPPORT+".";
        Set<String> methodNames = new HashSet<String>();
        Map<String,Method> httpMethods = new HashMap<String, Method>();
        for (Method method : r.getMethods()) {
            createStubMethod(method, rjSupport , pkg, methodNames , stubMethods );
            addMethodName( method.getName(), method , httpMethods );
        }
        for( Entry<String,Method> entry : httpMethods.entrySet()){
            String name = entry.getKey();
            Method method = entry.getValue();
            createRestMethod( name , method ,restMethods);
        }
        if(stubMethods.length() > 3) {
            stubMethods.delete(stubMethods.length()-3, stubMethods.length());
        }
        
    }

    private void addMethodName( String name, Method method, Map<String, Method> methods )
    {
        Method stored = methods.get(name);
        if ( stored ==null){
            methods.put(name, method);
        }
        else {
            String storedPath = stored.getPath();
            String methodPath = method.getPath();
            
            String[] storedSplit = split( storedPath );
            String[] methodSplit = split( methodPath );
            int storedLength = storedSplit.length;
            int methodLength = methodSplit.length;
            if ( storedLength == methodLength ){
                name = name +method.getType().toString();
                methods.put(name, method);
            }
            else if ( storedLength > methodLength ){
                methods.put( name , method );
                name = name + getPostfix( storedSplit );
                addMethodName(name, stored, methods);
            }
            else {
                name = name + getPostfix( methodSplit );
                addMethodName(name, method, methods);
            }
        }
    }
    
    private String[] split(String path ){
        String splitter = "/";              // NOI18N
        String result = path;
        if ( result == null ){
            return new String[0];
        }
        if ( result.startsWith(splitter)){
            result = result.substring(1);
        }
        if ( result.endsWith(splitter)){
            result = result.substring(0, result.length()-1);
        }
        return result.split(splitter);
    }
    
    private String getPostfix( String[] path ) {
        for (int i=1; i<=path.length; i++) {
            String part = path[path.length -i];
            String postfix = getPostfix( part );
            if ( postfix != null ){
                return capitalize(postfix);
            }
        }
        return "1";                                 // NOI18N
    }
    
    private String capitalize(String str ){
        if ( str == null || str.length() ==0 || str.length() == 1){
            return str;
        }
        char first = str.charAt(0);
        return Character.toUpperCase(first) + str.substring(1);
    }

    private String getPostfix( String path ) {
        String result = path.trim();
        if ( result.length() == 0 ){
            return null;
        }
        int index = result.indexOf('{');
        if ( index ==0 ){
            result = result.substring(1);
        }
        else if ( index >0 ){
            result = result.substring(0, index);
        }
        index = result.indexOf(':');
        if ( index != -1 ){
            result = result.substring(0 , index );
        }
        else {
            index = result.indexOf('}');
            if ( index != -1 ){
                result = result.substring(0 , index );
            }
        }
        if ( result.length() != 0 && Utilities.isJavaIdentifier( result)){
            return result;
        }
        return null;
    }

    private void createRestMethod( String name , Method method, 
            StringBuilder restMethods)
    {
        HttpMethodType type = method.getType();
        String path = method.getPath();
        RestEntity returnType = method.getReturnType();
        RestEntity parameterType = method.getParameterType();
        List<String> requestMimes = method.getRequestMimes();
        List<String> responseMimes = method.getResponseMimes();
        
        // this is (default) method which will be called on access to WS URI
        boolean isDefault = false;
        if ( type == HttpMethodType.GET && path == null && 
                returnType.getKind()!= EntityKind.VOID)
        {
            // default method produces JSON mime type
            isDefault = true;
            createEntitiesMethod(restMethods , name , returnType , responseMimes );
        }
        boolean jsonAware = responseMimes == null && requestMimes == null;
        if ( ! jsonAware ){
            if ( responseMimes != null && responseMimes.contains(
                    Constants.MimeType.JSON.value()))
            {
                jsonAware = true;
            }
            if ( !jsonAware && requestMimes!= null && requestMimes.contains(
                    Constants.MimeType.JSON.value()))
            {
                jsonAware = true;
            }
        }
        if ( jsonAware) {
            String returnEntity = addEntity( returnType , isDefault );
            String paramEntity = addEntity( parameterType , false  );
            
            addComment(method, restMethods, type, path, returnType.getKind(),
                    returnEntity, parameterType.getKind(), paramEntity );
            
            restMethods.append("   " );
            restMethods.append( name );
            // Even with no @Path it is possible to provide query parameters
            restMethods.append( " : function(uri_" );
            if ( parameterType.getKind() != EntityKind.VOID ){
                restMethods.append(", param ");
            }
            restMethods.append( " ) {\n" );
            restMethods.append("    var url = \"\";\n");
            restMethods.append("    if ( uri_ != null && uri_ !=undefined ){\n");
            restMethods.append("        url = uri_;\n");
            restMethods.append("    }\n");
            restMethods.append("    var  remote = new ");
            restMethods.append(resource.getName());
            restMethods.append("Remote(this.uri);\n");
            restMethods.append("    var  c= remote.");
            restMethods.append( method.getType().prefix());
            restMethods.append(Constants.MimeType.JSON.suffix());
            restMethods.append("_(url");
            if ( parameterType.getKind() == EntityKind.PRIMITIVE ){
                restMethods.append(", param ");
            }
            else if ( parameterType.getKind() == EntityKind.ENTITY ||
                    parameterType.getKind() == EntityKind.COLLECTION)
            {
                restMethods.append(", this.asString(param)");
            }
            restMethods.append(");\n");
            restMethods.append("    if ( c== -1 ){\n" );
            restMethods.append("        return -1;\n");
            restMethods.append("    }\n");            
            if ( returnType.getKind() == EntityKind.VOID ){
                restMethods.append( "   },\n\n" );
                return;
            }
            if ( returnType.getKind() == EntityKind.PRIMITIVE ){
                if ( isDefault ){
                    restMethods.append( "    return new ");
                    restMethods.append(returnEntity);
                    restMethods.append("( c );\n");
                }
                else {
                    restMethods.append( "    return c;\n");
                }
                restMethods.append( "   },\n\n" );
                return;
            }
            restMethods.append("    var  myObj = eval('('+c+')');\n");
            if ( returnType.getKind() == EntityKind.ENTITY ){
                restMethods.append( "    if ( myObj['@uri'] != null && myObj['@uri']!= undefined ){\n");
                restMethods.append( "       return new ");
                restMethods.append( returnEntity );
                restMethods.append("( myObj, myObj['@uri']);\n");
                restMethods.append( "     }\n");
                restMethods.append( "     else {\n");
                restMethods.append( "       return new ");
                restMethods.append( returnEntity );
                restMethods.append("( myObj );\n");
                restMethods.append( "     }\n");
            }
            else if ( returnType.getKind() == EntityKind.COLLECTION ){
                restMethods.append( "    var result = new Array();\n" );
                restMethods.append("    for ( var prop in myObj ){\n");
                restMethods.append( "       var ref= myObj[prop];\n");
                restMethods.append( "       var j=0;\n");
                restMethods.append( "       for( j=0; j<ref.length; j++){\n");
                restMethods.append( "           if ( ref[j]['@uri'] != null && ref[j]['@uri']!= undefined ){\n");
                restMethods.append( "               result[j] = new ");
                restMethods.append( returnEntity );
                restMethods.append("( ref[j], ref[j]['@uri']);\n");
                restMethods.append( "           }\n");
                restMethods.append( "           else {\n");
                restMethods.append( "           result[j] = new ");
                restMethods.append( returnEntity );
                restMethods.append("( ref[j] );\n");
                restMethods.append( "           }\n");
                restMethods.append( "       }\n");
                restMethods.append( "    }\n");
                restMethods.append( "    return result;\n");
            }
            restMethods.append( "   },\n\n" );
        }
    }

    private String addEntity( RestEntity entity , boolean wrapPrimitive ) {
        EntityKind kind = entity.getKind();
        if ( kind == EntityKind.VOID || ( !wrapPrimitive && 
                kind == EntityKind.PRIMITIVE) ) 
        {
            return null;
        }
        String fqn = entity.getFqn();
        if ( kind == EntityKind.PRIMITIVE ){
            fqn = String.class.getName();
        }
        int index = fqn.lastIndexOf('.');
        String simpleName = fqn.substring(index +1);
        String storedFqn = entities.get(simpleName);
        if ( storedFqn!= null && !storedFqn.equals( fqn ) ){
            simpleName = fqn.replace('.', '_');
        }
        entities.put( simpleName, fqn );
        return simpleName;
    }

    private void addComment( Method method, StringBuilder restMethods,
            HttpMethodType type, String path , EntityKind returnKind, 
            String returnType, EntityKind paramKind, String paramType )
    {
        restMethods.append("   /* Method " );
        restMethods.append( method.getName() );
        restMethods.append(" with HTTP request metod " );
        restMethods.append( type );
        if ( path != null ){
            restMethods.append(" and path : " );
            restMethods.append( path  );
        }
        if ( returnType!= null ){
            restMethods.append(", its return type is " );
            if ( returnKind == EntityKind.COLLECTION ){
                restMethods.append("array of " );
            }
            restMethods.append(returnType );
        }
        if ( paramType!= null ){
            restMethods.append(", its parameter type is " );
            if ( paramKind == EntityKind.COLLECTION ){
                restMethods.append("array of " );
            }
            restMethods.append(paramType );
        }
        restMethods.append("  */\n" );
    }
    
    private void createEntitiesMethod( StringBuilder buffer , String restMethodName, 
            RestEntity entity, List<String> mimes )
    {
        EntityKind kind = entity.getKind();
        if ( mimes !=null &&  !mimes.contains( Constants.MimeType.JSON.value()))
        {
            return;
        }
        buffer.append("   getEntities : function() {\n" );
        if ( kind == EntityKind.COLLECTION ){
            buffer.append("     return this.");
            buffer.append( restMethodName );
            buffer.append("(null);\n");
        }
        else {
            buffer.append("     var result = new Array();\n");
            buffer.append("     result[0] = this.");
            buffer.append( restMethodName );
            buffer.append("(null);\n");
            buffer.append("     return result;\n");
        }
        buffer.append("   },\n\n");
        resource.setDefaultGet();
    }

    private void createStubMethod(Method method, final String object, String pkg,
            Set<String> names , StringBuilder stubMethods ) 
    {
        HttpMethodType type = method.getType();
        if ( type == HttpMethodType.GET ){
            List<String> response = method.getResponseMimes();
            if ( response == null ){
                response = Collections.singletonList( Constants.MimeType.XML.value());
            }
            for (String mime : response) {
                mime = mime.replace("\"", "").trim();       // NOI18N
                String stubMethodName = createMethodName(method, mime, 
                        response.size());
                if ( names.contains(stubMethodName)){
                    continue;
                }
                names.add( stubMethodName );
                stubMethods.append("   " );
                stubMethods.append( stubMethodName );
                stubMethods.append( " : function(uri_) {\n" );
                stubMethods.append("   return " );
                stubMethods.append(object);
                stubMethods.append("get(this.uri+uri_, '");
                stubMethods.append( mime );
                stubMethods.append( "');\n   },\n\n" );
            }
        }
        else {
            String prefix = type.prefix();
            List<String> request = method.getRequestMimes();
            if ( request == null ){
                request = Collections.singletonList( Constants.MimeType.XML.value());
            }
            for (String mime : request) {
                mime = mime.replace("\"", "").trim();       // NOI18N
                String stubMethodName = createMethodName(method, mime, 
                        request.size());
                if ( names.contains(stubMethodName)){
                    continue;
                }
                names.add( stubMethodName );
                stubMethods.append("   " );
                stubMethods.append( stubMethodName );
                stubMethods.append( " : function(uri_, content) {\n" );
                stubMethods.append("   return " );
                stubMethods.append(object);
                stubMethods.append(prefix);
                stubMethods.append("(this.uri+uri_, '");
                stubMethods.append( mime );
                stubMethods.append( "', content);\n   },\n\n" );
            }
        }
    }

    private String createMethodName(Method method, String mimeType, int length) {
        if(length > 1) {
            for(Constants.MimeType mime:Constants.MimeType.values())
                if(mime.value().equals(mimeType)) {
                    return method.getType().prefix() + mime.suffix();
                }
        }
        return method.getType().prefix();
    }
    
    private Resource resource;
    private FileObject jsFolder;
    private String pkg;
    private String object;
    private Map<String,String> entities;
    private Set<String> ignoredEntities;
}