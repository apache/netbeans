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
package org.netbeans.modules.web.client.rest.wizard;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.web.client.rest.wizard.RestPanel.JsUi;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
class JSClientGenerator {
    
    static final String TABLESORTER_URL = 
            "http://mottie.github.com/tablesorter/";                 // NOI18N
    
    
    enum MethodType {
        GET,
        SET
    }
    
    enum HttpRequests {
        POST("create"),  // NOI18N
        PUT("update"),  // NOI18N
        DELETE("delete");  // NOI18N
        
        HttpRequests(String method){
            myBackboneMethod = method;
        }
        @Override
        public String toString(){
            return myBackboneMethod;
        }
        
        private final String myBackboneMethod;
    }
    
    private static final Logger LOG = Logger.getLogger( JSClientGenerator.class.getName()); 
    
    private static final String PATH = "javax.ws.rs.Path";           // NOI18N
    private static final String PATH_PARAM = "javax.ws.rs.PathParam";// NOI18N
    private static final String GET = "javax.ws.rs.GET";             // NOI18N
    private static final String PUT = "javax.ws.rs.PUT";             // NOI18N
    private static final String POST = "javax.ws.rs.POST";           // NOI18N
    private static final String DELETE = "javax.ws.rs.DELETE";       // NOI18N
    private static final String PRODUCES = "javax.ws.rs.Produces";   // NOI18N
    private static final String CONSUMES = "javax.ws.rs.Consumes";   // NOI18N
    private static final String JSON = "application/json";           // NOI18N
    
    private static final String XML_ROOT_ELEMENT = 
        "javax.xml.bind.annotation.XmlRootElement";                  // NOI18N
    
    private JSClientGenerator(RestServiceDescription description, JsUi ui){
        myDescription = description;
        myUi = ui;
    }

    static JSClientGenerator create( RestServiceDescription description, JsUi ui )
    {
        return new JSClientGenerator(description, ui);
    }

    public Map<String,String> generate( ) {
        FileObject restSource = myDescription.getFile();
        if ( restSource == null ){
            return Collections.emptyMap();
        }
        Map<String,String> result = new HashMap<String, String>();
        myModels = new StringBuilder();        
        myRouters = new StringBuilder();
        myContent = new StringBuilder();
        myHeader =  new StringBuilder();
        mySidebar = new StringBuilder();
        myTmplCreate = new StringBuilder();
        myTmplList = new StringBuilder();
        myTmplDetails = new StringBuilder();
        
        JavaSource javaSource = JavaSource.forFileObject( restSource);
        final String restClass = myDescription.getClassName();
        Task<CompilationController> task = new Task<CompilationController>(){

            @Override
            public void run( CompilationController controller ) throws Exception {
                controller.toPhase( Phase.ELEMENTS_RESOLVED );
                
                List<ExecutableElement> getMethods = new LinkedList<ExecutableElement>();
                List<ExecutableElement> postMethods = new LinkedList<ExecutableElement>();
                List<ExecutableElement> putMethods = new LinkedList<ExecutableElement>();
                List<ExecutableElement> deleteMethods = new LinkedList<ExecutableElement>();
                
                TypeElement restResource = controller.getElements().getTypeElement( 
                       restClass );
                List<ExecutableElement> methods = ElementFilter.methodsIn(
                        restResource.getEnclosedElements());
                for (ExecutableElement method : methods) {
                    List<? extends AnnotationMirror> annotations = 
                        method.getAnnotationMirrors();
                    if ( getAnnotation( annotations , DELETE)!= null ){
                        deleteMethods.add( method );
                        continue;
                    }
                    if ( !hasJsonMedia( annotations) ){
                        continue;
                    }
                    if ( getAnnotation( annotations, GET) != null ){
                        getMethods.add( method);
                    }
                    else if ( getAnnotation( annotations , POST)!= null ){
                        postMethods.add( method );
                    }
                    else if ( getAnnotation( annotations , PUT)!= null ){
                        putMethods.add( method );
                    }
                }
                
                try {
                    handleRestMethods(controller , getMethods, postMethods, 
                        putMethods, deleteMethods);
                }
                catch( IOException e ){
                    LOG.log(Level.WARNING , null ,e );
                }
            }

        };
        try {
            Future<Void> future = javaSource.runWhenScanFinished( task, true);
            future.get();
        }
        catch (IOException e) {
            LOG.log(Level.INFO , null ,e );
        }
        catch (InterruptedException e) {
            LOG.log(Level.INFO , null ,e );
        }
        catch (ExecutionException e) {
            LOG.log(Level.INFO , null ,e );
        }
        
        if ( !isModelGenerated ){
            myModels.append("// No JSON media type is detected in GET RESTful methods\n");
        }
        result.put("models",myModels.toString());           // NOI18N 
        result.put("routers", myRouters.toString());        // NOI18N 
        result.put("header", myHeader.toString());          // NOI18N 
        result.put("sidebar", mySidebar.toString());        // NOI18N 
        result.put("content", myContent.toString());        // NOI18N 
        result.put("tpl_create", myTmplCreate.toString());  // NOI18N 
        result.put("tpl_list_item", myTmplList.toString()); // NOI18N 
        result.put("tpl_details", myTmplDetails.toString());// NOI18N 
        if ( hasUi() ){
            result.put("ui", Boolean.TRUE.toString());      // NOI18N
        }
        
        return result;
    }
    
    boolean hasUi(){
        return hasUi;
    }
    
    private void handleRestMethods( CompilationController controller,  
            List<ExecutableElement> getMethods,
            List<ExecutableElement> postMethods,
            List<ExecutableElement> putMethods,
            List<ExecutableElement> deleteMethods ) throws IOException
    {
        Map<String,ExecutableElement> noParamGetMethods = 
            new HashMap<String, ExecutableElement>();
        Map<String,ExecutableElement> oneParamGetMethods = 
            new HashMap<String, ExecutableElement>();
        for(ExecutableElement method : getMethods){
            List<? extends VariableElement> parameters = method.getParameters();
            if ( parameters.size() > 1){
                // TODO : handle methods with more than one  param
                continue;
            }
            
            AnnotationMirror annotation = getAnnotation(method, PATH);
            String path = getValue( annotation );
            if ( parameters.isEmpty() ){
                if ( path == null ){
                    path = ""; // NOI18N
                }
                noParamGetMethods.put( path, method );
                continue;
            }
            
            VariableElement param = parameters.get(0);
            annotation = getAnnotation(param, PATH_PARAM);
            if ( annotation == null ){
                continue;
            }
            String pathNoParam = removeParamTemplate( path , getValue(annotation));
            oneParamGetMethods.put(pathNoParam, method );
        }
        Map<String,String> fqn2Path = new HashMap<String, String>();
        for(Entry<String,ExecutableElement> entry : noParamGetMethods.entrySet()){
            String path = entry.getKey();
            ExecutableElement method = entry.getValue();
            
            TypeMirror returnType = method.getReturnType();
            Element returnElement = controller.getTypes().asElement( returnType );
            TypeMirror entityCollectionType = getCollectionType(returnType, controller);
            if ( entityCollectionType == null && 
                    returnElement instanceof TypeElement)           // skip primitives ( consider just type element )
            {
                if ( getAnnotation( returnElement, XML_ROOT_ELEMENT) == null ){
                    /* TODO : here is only @XmlRootElement annotated elements
                     * are considered as JSON serializable ( that's true for
                     * NB generated entities ) but there could be probably
                     * other ways to serialize ( read/write REST providers )
                     * POJO classes     
                     */
                    continue;
                }
                EnumMap<HttpRequests, String> paths = 
                    new EnumMap<HttpRequests, String>(HttpRequests.class);
                paths.put(HttpRequests.POST, parseNoIdPath(postMethods, 
                        returnType, controller ));
                paths.put(HttpRequests.PUT, parseNoIdPath(putMethods , 
                        returnType , controller));
                paths.put(HttpRequests.DELETE, parseNoIdPath(deleteMethods,
                        returnType, controller ));
                generate( (TypeElement)returnElement , path , 
                        null, paths , Collections.<HttpRequests, Boolean>emptyMap(), 
                        controller );
            }
            else {
                // collection of entities
                Element entityType = controller.getTypes().asElement(entityCollectionType);
                if ( entityType instanceof TypeElement ){
                    String fqn = ((TypeElement)entityType).getQualifiedName().toString();
                    fqn2Path.put(fqn, path);
                }
            }
        }
        for(Entry<String,ExecutableElement> entry : oneParamGetMethods.entrySet()){
            String path = entry.getKey();
            ExecutableElement method = entry.getValue();
            
            TypeMirror returnType = method.getReturnType();
            Element returnElement = controller.getTypes().asElement( returnType );
            if ( returnElement instanceof TypeElement ){
                // TODO: return type could be a primitive type. How it should be handled ?
                if ( getAnnotation( returnElement, XML_ROOT_ELEMENT) == null ){
                    /* TODO : here is only @XmlRootElement annotated elements
                     * are considered as JSON serializable ( that's true for
                     * NB generated entities ) but there could be probably
                     * other ways to serialize ( read/write REST providers )
                     * POJO classes     
                     */
                    continue;
                }
                String fqn = ((TypeElement)returnElement).getQualifiedName().toString();
                String collectionPath = fqn2Path.get(fqn);
                EnumMap<HttpRequests, String> paths = 
                    new EnumMap<HttpRequests, String>(HttpRequests.class);
                EnumMap<HttpRequests, Boolean> ids = 
                    new EnumMap<HttpRequests, Boolean>(HttpRequests.class);
                parsePath(postMethods, returnType, paths, ids, 
                        HttpRequests.POST, controller );
                parsePath(putMethods, returnType, paths, ids, 
                        HttpRequests.PUT, controller );
                parsePath(deleteMethods, returnType, paths, ids, 
                        HttpRequests.DELETE, controller );
                generate( (TypeElement)returnElement , path , 
                        collectionPath, paths, ids, controller );
            }
        }
    }
    
    private String parseNoIdPath( List<ExecutableElement> methods , 
            TypeMirror type , CompilationController controller) 
    {
        for (ExecutableElement method : methods) {
            List<? extends VariableElement> parameters = method.getParameters();
            boolean matches = false;
            if (parameters.isEmpty()) {
                matches = true;
            }
            else if ( parameters.size() == 1){
                VariableElement param = parameters.get(0);
                if ( controller.getTypes().isSameType(param.asType(),type)){
                    matches = true;
                }
            }
            else {
                continue;
            }
            if ( matches ){
                AnnotationMirror annotation = getAnnotation(method, PATH);
                if ( annotation == null ){
                    return ""; // NOI18N
                }
                else {
                    return getValue(annotation);
                }
            }
        }
        return null;
    }
    
    private void parsePath( List<ExecutableElement> methods , TypeMirror type ,
            EnumMap<HttpRequests, String> paths, 
            EnumMap<HttpRequests, Boolean> ids, HttpRequests request,
            CompilationController controller) 
    {
        for (ExecutableElement method : methods) {
            List<? extends VariableElement> parameters = method.getParameters();
            boolean matches = false;
            String pathParam = null;
            if ( parameters.size() == 1){
                VariableElement param = parameters.get(0);
                if ( controller.getTypes().isSameType(param.asType(),type)){
                    matches = true;
                }
                else if ( getAnnotation(param, PATH_PARAM) != null ){
                    pathParam = getValue(getAnnotation(param, PATH_PARAM));
                    matches = true;
                    ids.put(request, Boolean.TRUE);
                }
            }
            else if (parameters.size() == 2) {
                VariableElement param1 = parameters.get(0);
                VariableElement param2 = parameters.get(1);
                if ( getAnnotation(param1, PATH_PARAM) != null ){
                    pathParam = getValue(getAnnotation(param1, PATH_PARAM));
                    if ( controller.getTypes().isSameType(param2.asType(),type)){
                        matches = true;
                    }
                }
                else if ( controller.getTypes().isSameType(param1.asType(),type)){
                    if ( getAnnotation(param2, PATH_PARAM) != null ){
                        pathParam = getValue(getAnnotation(param2, PATH_PARAM));
                        matches = true;
                    }
                }
                if ( matches ){
                    ids.put(request, Boolean.TRUE);
                }
            }
            else {
                continue;
            }
            if ( matches ){
                AnnotationMirror annotation = getAnnotation(method, PATH);
                if ( annotation == null ){
                    paths.put(request, ""); // NOI18N
                }
                else {
                    String path = getValue(annotation);
                    if ( pathParam != null ){
                        path = removeParamTemplate(path, pathParam);
                    }
                    paths.put(request,path);
                }
                break;
            }
        }
    }
    
    private void generate( TypeElement entity, String path,
            String collectionPath, Map<HttpRequests, String> httpPaths ,
            Map<HttpRequests, Boolean> useIds,
            CompilationController controller ) throws IOException
    {
        isModelGenerated = true;
        ModelGenerator generator = new ModelGenerator(myDescription,
                myModels, myEntities, myUi);
        generator.generateModel(entity, path, 
                collectionPath, httpPaths, useIds, controller);
        generateRouter(entity, path, collectionPath, httpPaths, controller, 
                generator);
    }

    private void generateRouter( TypeElement entity, String path,
            String collectionPath, Map<HttpRequests, String> httpPaths,
            CompilationController controller, ModelGenerator modelGenerator )
    {
        if ( myModelsCount >0 ){
            myRouters.append("/*");                                 // NOI18N
        }
        String name = "AppRouter";                                  // NOI18N
        if ( myModelsCount >0 ){
            name += myModelsCount;                              
        }
        RouterGenerator generator = new RouterGenerator(myRouters, name, 
                modelGenerator);
        generator.generateRouter(entity, path, collectionPath, httpPaths,controller );
        
        if ( myModelsCount == 0 ){
            // Create HTML "view" for header identifier 
            myHeader.append("<div id='");                           // NOI18N
            myHeader.append(generator.getHeaderId());
            myHeader.append("'></div>\n");                          // NOI18N
            
            if ( generator.getSideBarId()!= null) {
                // Create HTML "view" for sidebar identifier
                generateCollection(generator);
            }
            
            // Create HTML "view" for content identifier
            generateContent(generator);
            
            if ( generator.getCreateTemplate()!= null){
                // Create HTML "view" for "create new item" template
                myTmplCreate.append("<script type='text/template' id='");               // NOI18N
                myTmplCreate.append(generator.getCreateTemplate());
                myTmplCreate.append("'>\n");                                            // NOI18N
                myTmplCreate.append("<!--\n");                                          // NOI18N
                myTmplCreate.append("\tPut your controls to create new entity here.\n\n");// NOI18N
                myTmplCreate.append("\tClass 'new' is used to listen on events in JS code.\n");// NOI18N
                myTmplCreate.append("-->\n");                                           // NOI18N
                myTmplCreate.append("<button class='new'>Create</button>\n");           // NOI18N
                myTmplCreate.append("</script>\n");                                     // NOI18N
            }
            
            if ( generator.getListItemTemplate()!= null){
                // Create HTML "view" for list item
                if ( generator.useUi() ){
                    generateHeadTemplate(generator);
                }
                myTmplList.append("<script type='text/template' id='");                 // NOI18N
                myTmplList.append(generator.getListItemTemplate());
                myTmplList.append("'>\n");                                              // NOI18N
                generateItemContent(generator);
                myTmplList.append("</script>\n");                                       // NOI18N
            }
            
            if ( generator.getDetailsTemplate()!= null){
                // Create HTML "view" for details of chosen item
                myTmplDetails.append("<script type='text/template' id='");              // NOI18N
                myTmplDetails.append(generator.getDetailsTemplate());
                myTmplDetails.append("'>\n");                                           // NOI18N
                myTmplDetails.append("<div>\n");                                        // NOI18N
                String idAttribute = null; 
                if ( generator.useUi()){
                    myTmplDetails.append("<table>\n<tbody>\n"); // NOI18N
                }
                if ( modelGenerator.getIdAttribute()!= null ){
                    idAttribute = modelGenerator.getIdAttribute().getName();
                    if ( generator.useUi()){
                        myTmplDetails.append("<tr><td>Id</td>\n<td>\n");                // NOI18N
                    }
                    else {
                        myTmplDetails.append("<label>Id:</label>\n");                   // NOI18N
                    }
                    myTmplDetails.append("<input type='text' id='");                    // NOI18N
                    myTmplDetails.append(idAttribute);
                    myTmplDetails.append("' name='id' value='<%= typeof(");             // NOI18N
                    myTmplDetails.append(idAttribute);
                    myTmplDetails.append(")!== \"undefined\" ? ");                      // NOI18N
                    myTmplDetails.append(idAttribute);
                    myTmplDetails.append(" : \"\" %>'  />\n");                          // NOI18N
                    if ( generator.useUi()){
                        myTmplDetails.append("</td>\n</tr>\n");                         // NOI18N
                    }
                }
                String nameAttribute = modelGenerator.getDisplayNameAlias();
                if ( !generator.useUi()){
                    if ( nameAttribute != null && !nameAttribute.equals( idAttribute )){
                        myTmplDetails.append("<label>Name:</label>\n");                 // NOI18N
                        myTmplDetails.append("<input type='text' id='");                // NOI18N
                        myTmplDetails.append(nameAttribute);
                        myTmplDetails.append("' name='");                               // NOI18N
                        myTmplDetails.append(nameAttribute);
                        myTmplDetails.append("' value='<%= ");                          // NOI18N
                        myTmplDetails.append(nameAttribute);
                        myTmplDetails.append(" %>'/>\n");                               // NOI18N
                    }
                    myTmplDetails.append("<!--\n");                                     // NOI18N
                    myTmplDetails.append("\tPut your editing controls for model\n");    // NOI18N
                    myTmplDetails.append("attribute data (text fields, ...) here\n");   // NOI18N
                }
                
                Set<ModelAttribute> attributes = modelGenerator.getAttributes();
                for (ModelAttribute attribute : attributes) {
                    String attrName = attribute.getName();
                    if ( !generator.useUi() && attrName.equals( nameAttribute)||
                            attrName.equals(idAttribute))
                    {
                        continue;
                    }
                    if ( generator.useUi() ){
                        myTmplDetails.append("<tr>\n<td>");                             // NOI18N
                    }
                    else {
                        myTmplDetails.append("<label>");                                // NOI18N
                    }
                    myTmplDetails.append(attrName);
                    if ( generator.useUi() ){
                        myTmplDetails.append("</td><td>");                              // NOI18N   
                    }
                    else {
                        myTmplDetails.append(":</label>\n");                            // NOI18N    
                    }
                    myTmplDetails.append("<input type='text' id='");                    // NOI18N
                    myTmplDetails.append(attrName);
                    myTmplDetails.append("' name='");                                   // NOI18N
                    myTmplDetails.append(attrName);
                    myTmplDetails.append("' value='<%= ");                              // NOI18N
                    myTmplDetails.append(attrName);
                    myTmplDetails.append(" %>'/>");                                     // NOI18N
                    if ( generator.useUi() ){
                        myTmplDetails.append("</td></tr>\n");                           // NOI18N
                    }
                }
                if ( generator.useUi()){
                    myTmplDetails.append("</tbody>\n</table>\n");                        // NOI18N
                }
                else {
                    myTmplDetails.append("-->\n");                                      // NOI18N    
                }
                myTmplDetails.append("<!--\n");
                myTmplDetails.append("\tPut your controls to create new entity here.\n");// NOI18N
                myTmplDetails.append("\tClasses 'save' and 'delete' are used ");        // NOI18N
                myTmplDetails.append("to listen on events in JS code.\n");              // NOI18N
                myTmplDetails.append("-->\n");                                          // NOI18N
                myTmplDetails.append("<button  class='save'>Save</button>\n");          // NOI18N
                myTmplDetails.append("<button  class='delete'>Delete</button>\n");      // NOI18N
                myTmplDetails.append("</div>\n");                                       // NOI18N
                myTmplDetails.append("</script>\n");                                    // NOI18N
            }
        }
        else {
            myRouters.append("*/");                                         // NOI18N
        }
        myModelsCount++;
    }

    private void generateHeadTemplate( RouterGenerator generator ) {
        myTmplList.append("<script type='text/template' id='");                 // NOI18N
        myTmplList.append(generator.getTableHeadId());
        myTmplList.append("'>\n<thead>\n<tr>\n");                               // NOI18N
         
        if (generator.getModelGenerator().getIdAttribute() != null) {
            String id = generator.getModelGenerator().getIdAttribute()
                    .getName();
            myTmplList.append("<th>");                                          // NOI18N
            myTmplList.append(id);
            myTmplList.append("</th>\n");                                       // NOI18N
        }
        Set<ModelAttribute> attributes = generator.getModelGenerator()
                .getAttributes();
        for (ModelAttribute attribute : attributes) {
            myTmplList.append("<th>");                                          // NOI18N
            myTmplList.append(attribute.getName());
            myTmplList.append("</th>\n");                                       // NOI18N
        }
        
        myTmplList.append("</tr>\n</thead>\n</script>\n");                      // NOI18N
    }

    private void generateItemContent( RouterGenerator generator ) {
        if ( generator.useUi()){
            if ( generator.getModelGenerator().getIdAttribute() != null ){
                String id = generator.getModelGenerator().getIdAttribute().getName();
                myTmplList.append("<td><a href='#<%= ");                // NOI18N
                myTmplList.append(id);
                myTmplList.append(" %>'><%= ");                         // NOI18N
                myTmplList.append(id);
                myTmplList.append(" %></a></td>\n");                    // NOI18N
            }
            Set<ModelAttribute> attributes = generator.getModelGenerator().getAttributes();
            for( ModelAttribute attribute : attributes ){
                myTmplList.append("<td><%= ");                          // NOI18N
                myTmplList.append(attribute.getName());
                myTmplList.append(" %></td>\n");                      // NOI18N
            }
        }
        else {
            myTmplList.append("<!-- modify output display name for item here");     // NOI18N
            myTmplList.append(" or change displayName in the JS model code -->\n"); // NOI18N
            if (generator.getModelGenerator().getIdAttribute()!= null){
                myTmplList.append("<a href='#<%= ");                                // NOI18N
                myTmplList.append(generator.getModelGenerator().getIdAttribute().getName());
                myTmplList.append(" %>'><%= displayName %></a>\n");                 // NOI18N
            }
            else {
                myTmplList.append("<%= displayName %>\n");                          // NOI18N
            }
        }
    }

    private void generateCollection( RouterGenerator generator ) {
        if ( generator.useUi()){
            hasUi = true;
            mySidebar.append("<table id='");                                 // NOI18N
            mySidebar.append(generator.getSideBarId());
            mySidebar.append("' class='tablesorter-blue'>\n</table>\n");         // NOI18N
            mySidebar.append("<div class='pager' id='pager'>\n");            // NOI18N
            mySidebar.append("<img src='");                                  // NOI18N
            mySidebar.append(TABLESORTER_URL);
            mySidebar.append("addons/pager/icons/first.png' class='first' ");// NOI18N
            mySidebar.append("alt='First'/>\n");                             // NOI18N
            mySidebar.append("<img src='");                                  // NOI18N
            mySidebar.append(TABLESORTER_URL);
            mySidebar.append("addons/pager/icons/prev.png' class='prev' ");  // NOI18N
            mySidebar.append("alt='Prev'/>\n");                              // NOI18N
            mySidebar.append("<span class='pagedisplay'></span>");           // NOI18N   
            mySidebar.append(" <!-- this can be any element, including an input -->\n");// NOI18N  
            mySidebar.append("<img src='");                                  // NOI18N
            mySidebar.append(TABLESORTER_URL);
            mySidebar.append("addons/pager/icons/next.png' class='next' ");  // NOI18N
            mySidebar.append("alt='Next'/>\n");                              // NOI18N
            mySidebar.append("<img src='");                                  // NOI18N
            mySidebar.append(TABLESORTER_URL);
            mySidebar.append("addons/pager/icons/last.png' class='last' ");  // NOI18N
            mySidebar.append("alt='Last'/>\n");                              // NOI18N                        
            mySidebar.append("<select class='pagesize'>\n");                 // NOI18N
            mySidebar.append("<option selected='selected' value='10'>");     // NOI18N
            mySidebar.append("10</option>\n");                               // NOI18N
            mySidebar.append("<option value='20'>20</option>\n");            // NOI18N
            mySidebar.append("<option value='30'>30</option>\n");            // NOI18N
            mySidebar.append("<option value='40'>40</option>\n");            // NOI18N
            mySidebar.append("</select>\n</div>\n<br>\n");                   // NOI18N   
        }
        else {
            mySidebar.append("<div id='");                                  // NOI18N
            mySidebar.append(generator.getSideBarId());
            mySidebar.append("'></div>\n");                                 // NOI18N
        }
    }

    private void generateContent( RouterGenerator generator ) {
        myContent.append("<div id='");                          // NOI18N
        myContent.append(generator.getContentId());
        myContent.append("'></div>\n");                         // NOI18N
    }

    private String removeParamTemplate( String path, String param ) {
        int index = path.indexOf('{');
        String template;
        if ( index == -1 ){
            return path;
        }
        else {
            template = path.substring(index+1).trim();
            int lastIndex = template.lastIndexOf('}');
            if ( lastIndex == -1 ){
                return path;
            }
            template = template.substring( 0, lastIndex ).trim();
        }
        if ( !template.startsWith(param) ){
            return path;
        }
        template = template.substring(param.length()).trim();
        if ( template.length() == 0 || template.charAt(0)==':'){
            return path.substring( 0, index );
        }
        return path;
    }

    private TypeMirror getCollectionType (TypeMirror type , 
            CompilationController controller)
    {
        TypeElement collectionElement = controller.getElements().getTypeElement(
                Collection.class.getName());
        TypeMirror collectionType = controller.getTypes().erasure(
                collectionElement.asType());
        TypeMirror erasure = controller.getTypes().erasure(type);
        if (!controller.getTypes().isSubtype(erasure, collectionType)) {
            return null;
        }
        List<? extends TypeMirror> supers = controller.getTypes().directSupertypes( type );
        for (TypeMirror superType : supers) {
            erasure = controller.getTypes().erasure(superType);
            if ( controller.getTypes().isSameType(erasure, collectionType)){
                return getParameterType( superType);
            }
            TypeMirror found = getCollectionType(superType, controller);
            if ( found != null ){
                return found;
            }
        }
        return null;
    }
    
    private TypeMirror getParameterType( TypeMirror type ){
        if ( type instanceof DeclaredType ){
            List<? extends TypeMirror> typeArguments = ((DeclaredType)type).getTypeArguments();
            if (typeArguments.isEmpty()) {
                return null;
            }
            return typeArguments.get(0);
        }
        return null;
    }
    
    private String getValue( AnnotationMirror annotation ){
        if ( annotation == null ){
            return null;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = 
            annotation.getElementValues();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
            elementValues.entrySet()) 
        {
            ExecutableElement annotationMethod = entry.getKey();
            AnnotationValue value = entry.getValue();
            if (annotationMethod.getSimpleName().contentEquals("value")) { // NOI18N
                Object val = value.getValue();
                if ( val != null ){
                    return val.toString();
                }
            }
        }
        return null;
    }
    
    private boolean hasJsonMedia( List<? extends AnnotationMirror> annotations) {
        AnnotationMirror consumes = getAnnotation(annotations, CONSUMES);
        AnnotationMirror produces = getAnnotation(annotations, PRODUCES);
        AnnotationMirror mimeTypeDecl = consumes==null ? produces: consumes;
        if ( mimeTypeDecl == null ){
            return false;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = 
            mimeTypeDecl.getElementValues();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
            elementValues.entrySet()) 
        {
            ExecutableElement annotationMethod = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( annotationMethod.getSimpleName().contentEquals("value")){      // NOI18N
                Object mediaType = value.getValue();
                if ( mediaType instanceof List<?>){
                    List<?> types = (List<?>)mediaType;
                    for (Object type : types) {
                        if ( type instanceof AnnotationValue ){
                            mediaType = ((AnnotationValue)type).getValue();
                            if ( JSON.equals( mediaType )){
                                return true;
                            }
                        }
                    }
                }
                else if ( JSON.equals( mediaType )){
                    return true;
                }
            }
        }
        return false;
    }
    
    static AnnotationMirror getAnnotation( List<? extends AnnotationMirror> annotations, 
            String annotation )
    {
        for (AnnotationMirror annotationMirror : annotations) {
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            if ( annotationElement instanceof TypeElement){
                TypeElement annotationDecl = (TypeElement) annotationElement;
                if ( annotationDecl.getQualifiedName().contentEquals( annotation)){
                    return annotationMirror;
                }
            }
        }
        return null;
    }
    
    static AnnotationMirror getAnnotation( Element element, String annotation )
    {
        List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
        return getAnnotation(annotations, annotation);
    }
    
    private final RestServiceDescription myDescription;
    private final JsUi myUi;
    private StringBuilder myModels;
    private StringBuilder myRouters;
    private StringBuilder myHeader;
    private StringBuilder myContent;
    private StringBuilder mySidebar;
    private StringBuilder myTmplCreate;
    private StringBuilder myTmplList;
    private StringBuilder myTmplDetails;
    private final Set<String> myEntities = new HashSet<String>();
    private boolean isModelGenerated;
    private int myModelsCount;
    private boolean hasUi;

}
