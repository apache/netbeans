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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.client.rest.wizard.JSClientGenerator.HttpRequests;
import org.netbeans.modules.web.client.rest.wizard.RestPanel.JsUi;


/**
 * @author ads
 *
 */
class RouterGenerator {
    
    RouterGenerator(StringBuilder routers, String name , ModelGenerator generator){
        myRouters = routers;
        myRouterName = name;
        myModelGenerator = generator;
    }

    void generateRouter( TypeElement entity, String path,
            String collectionPath, Map<HttpRequests, String> httpPaths,
            CompilationController controller)
    {
        myRouters.append("var ");                                         // NOI18N
        myRouters.append(myRouterName);
        myRouters.append(" = Backbone.Router.extend({\n");                // NOI18N
        
        boolean hasCollection = collectionPath != null; 
        String modelVar = getModelGenerator().getModelName().toLowerCase(Locale.ENGLISH);
        /*
         *  Fill routes
         */
        // default route used on page loading 
        myRouters.append("routes:{\n");                                   // NOI18N
        if ( hasCollection ){
            myRouters.append("'':'list'");                                // NOI18N
        }
        else {
            myRouters.append("'':'details'");                             // NOI18N
        }
        // #new route if there is a corresponding POST request in the REST
        if ( httpPaths.get( HttpRequests.POST) != null ){
            myRouters.append(",\n'new':'create'\n");                      // NOI18N
        }
        // #id route if REST has a method for collection
        if ( hasCollection ){
            myRouters.append(",\n':id':'details'\n");                     // NOI18N
        }
        myRouters.append("},\n");                                         // NOI18N
        
        // CTOR ( initialize ) function assign CreateView for "tpl-create" template
        myRouters.append("initialize:function(){\n");                     // NOI18N
        myRouters.append("var self = this;\n");                           // NOI18N
        myRouters.append("$('#");                                         // NOI18N
        myRouters.append(getHeaderId());
        myRouters.append("').html(new views.CreateView({\n");             // NOI18N
        myRouters.append(" // tpl-create is template identifier for 'create' block\n");// NOI18N
        myRouters.append("templateName :'#");                             // NOI18N
        myRouters.append(getCreateTemplate());
        myRouters.append("',\n");                                         // NOI18N
        myRouters.append("navigate: function(){\n");                      // NOI18N
        myRouters.append("self.navigate('new', true);\n}\n");             // NOI18N
        myRouters.append("}).render().el);\n},\n");                       // NOI18N
        
        if ( hasCollection ){
            if ( useUi() ){
                mySideBarId = "datatable";                                // NOI18N
            }
            else {
                mySideBarId = "sidebar";                                  // NOI18N
            }
            myRouters.append("list:function () {\n");                     // NOI18N
            myRouters.append("this.collection = new models.");            // NOI18N
            myRouters.append(getModelGenerator().getCollectionModelName());
            myRouters.append("();\nvar self = this;\n");                  // NOI18N
            myRouters.append("this.collection.fetch({\n");                // NOI18N
            myRouters.append("success:function () {\n");                  // NOI18N
            myRouters.append("self.listView = new views.ListView({\n");   // NOI18N
            myRouters.append("model:self.collection,\n");
            StringBuilder builder = new StringBuilder("tpl-");            // NOI18N
            builder.append(modelVar);
            builder.append("-list-item");                                 // NOI18N
            myListItemTemplate = builder.toString();
            myRouters.append(" // ");                                      // NOI18N
            myRouters.append(myListItemTemplate);
            myRouters.append("is template identifier for item\n");        // NOI18N
            myRouters.append("templateName : '#");                        // NOI18N
            myRouters.append(myListItemTemplate);
            myRouters.append("'\n});\n");                                 // NOI18N
            myRouters.append("$('#");                                     // NOI18N
            myRouters.append(getSideBarId());
            myRouters.append("').html(self.listView.render().el)");       // NOI18N
            if ( useUi() ){
                myRouters.append(".append(_.template($('#");              // NOI18N
                myRouters.append(getTableHeadId());
                myRouters.append("').html())())");
            }
            myRouters.append(";\nif (self.requestedId) {\n");             // NOI18N
            myRouters.append("self.details(self.requestedId);\n}\n");     // NOI18N
            if ( useUi() ){
                myRouters.append("var pagerOptions = {\n");                   // NOI18N
                myRouters.append(" // target the pager markup \n");            // NOI18N
                myRouters.append("container: $('.pager'),\n");                // NOI18N
                myRouters.append(" // output string - default is ");           // NOI18N
                myRouters.append("'{page}/{totalPages}'; possible");          // NOI18N
                myRouters.append("variables: {page}, {totalPages},");         // NOI18N
                myRouters.append("{startRow}, {endRow} and {totalRows}\n");   // NOI18N
                myRouters.append("output: '{startRow} to");                   // NOI18N
                myRouters.append(" {endRow} ({totalRows})',\n");              // NOI18N
                myRouters.append(" // starting page of the pager (zero based index)\n");// NOI18N
                myRouters.append("page: 0,\n");                               // NOI18N
                myRouters.append(" // Number of visible rows - default is 10\n");// NOI18N
                myRouters.append("size: 10\n};\n$('#");                       // NOI18N
                myRouters.append(getSideBarId());
                myRouters.append("').tablesorter({widthFixed: true, \n");     // NOI18N
                myRouters.append("widgets: ['zebra']}).\n");                  // NOI18N
                myRouters.append("tablesorterPager(pagerOptions);\n");        // NOI18N
            }
            myRouters.append("}\n});\n},\n");                             // NOI18N
        }
        
        StringBuilder builder = new StringBuilder("tpl-");                // NOI18N
        builder.append(modelVar);
        builder.append("-details");                                       // NOI18N
        myDetailsTemplateName = builder.toString();
        
        // details function
        myRouters.append("details:function (");                           // NOI18N
        if ( hasCollection ){
            myRouters.append("id");                                       // NOI18N
        }
        myRouters.append("){\n");                                         // NOI18N
        if ( hasCollection ){
            myRouters.append("if (this.collection) {\n");                 // NOI18N
            myRouters.append("this.");                                    // NOI18N
            myRouters.append(modelVar);
            myRouters.append("= this.collection.get(id);\n");             // NOI18N
            myRouters.append("if (this.view) {\n");                       // NOI18N
            myRouters.append("this.view.close();\n}\n");                  // NOI18N
            myRouters.append("var self = this;\n");                       // NOI18N  
            myRouters.append("this.view = new views.ModelView({\n");      // NOI18N
            myRouters.append("model:this.");                              // NOI18N
            myRouters.append(modelVar);
            myRouters.append(",\n // ");                                   // NOI18N
            myRouters.append( myDetailsTemplateName );
            myRouters.append(" is template identifier for chosen model element\n");// NOI18N
            myRouters.append("templateName: '#");                         // NOI18N
            myRouters.append( myDetailsTemplateName );
            myRouters.append("',\ngetHashObject: function(){\n");         // NOI18N
            myRouters.append("return self.getData();\n}\n});\n");         // NOI18N
            myRouters.append("$('#");                                      // NOI18N
            myRouters.append(getContentId());
            myRouters.append("').html(this.view.render().el);");          // NOI18N
            myRouters.append("} else {\n");                               // NOI18N
            myRouters.append("this.requestedId = id;\n");                 // NOI18N          
            myRouters.append("this.list();\n}\n},\n");                    // NOI18N
        }
        else {
            myRouters.append("if (this.view) {\n");                       // NOI18N
            myRouters.append("this.view.close();\n}\n");                  // NOI18N
            myRouters.append("var self = this;\n");                       // NOI18N  
            myRouters.append("this.");                                    // NOI18N 
            myRouters.append(modelVar);
            myRouters.append(" = models.");                               // NOI18N 
            myRouters.append(getModelGenerator().getModelName());
            myRouters.append("();\nthis.");                               // NOI18N
            myRouters.append(modelVar);
            myRouters.append(".fetch({\n");                               // NOI18N
            myRouters.append("success:function(){\n");                    // NOI18N
            myRouters.append("self.view = new views.ModelView({\n");      // NOI18N
            myRouters.append("model: self.newclass,\n // ");               // NOI18N
            myRouters.append(myDetailsTemplateName);
            myRouters.append(" is template identifier for chosen model element\n");// NOI18N
            myRouters.append("templateName : '#");                        // NOI18N
            myRouters.append(myDetailsTemplateName);
            myRouters.append("'\n});\n");                                 // NOI18N
            myRouters.append("$('#");                                     // NOI18N
            myRouters.append(getContentId());
            myRouters.append("').html(self.view.render().el);}\n});\n},\n");// NOI18N
        }
        
        if ( httpPaths.get( HttpRequests.POST) != null){
            myRouters.append("create:function () {\n");                   // NOI18N
            myRouters.append("if (this.view) {\n");                       // NOI18N
            myRouters.append("this.view.close();\n}\n");                  // NOI18N
            myRouters.append("var self = this;\n");                       // NOI18N
            
            myRouters.append("var dataModel = new models.");
            myRouters.append( getModelGenerator().getModelName());
            myRouters.append("();\n");                                    // NOI18N
            myRouters.append(" // see isNew() method implementation in the model\n");// NOI18N
            myRouters.append("dataModel.notSynced = true;\n");            // NOI18N
            
            myRouters.append("this.view = new views.ModelView({\n");      // NOI18N
            myRouters.append("model: dataModel,\n");
            if ( hasCollection ){
                myRouters.append("collection: this.collection,\n");       // NOI18N
            }
            myRouters.append(" // ");                                      // NOI18N
            myRouters.append(myDetailsTemplateName);
            myRouters.append(" is a template identifier for chosen model element\n");// NOI18N
            myRouters.append("templateName: '#");                          // NOI18N
            myRouters.append(myDetailsTemplateName);
            myRouters.append("',\n");                                      // NOI18N
            myRouters.append("navigate: function( id ){\n");               // NOI18N
            myRouters.append("self.navigate(id, false);\n},\n\n");         // NOI18N
            myRouters.append("getHashObject: function(){\n");              // NOI18N
            myRouters.append("return self.getData();\n}\n");               // NOI18N
            myRouters.append("});\n");                                     // NOI18N
            myRouters.append("$('#");                                      // NOI18N
            myRouters.append(getContentId());                               
            myRouters.append("').html(this.view.render().el);\n},\n");     // NOI18N
        }
        
        // add method getData which returns composite object data got from HTML controls 
        myRouters.append("getData: function(){\n");                       // NOI18N
        myRouters.append("return {\n");                                   // NOI18N
        if ( useUi() ){
            ModelAttribute id = getModelGenerator().getIdAttribute();
            if ( id!= null ){
                myRouters.append(id.getName());
                myRouters.append(":$('#");                                // NOI18N
                myRouters.append(id.getName());
                myRouters.append("').val(),\n");                          // NOI18N
            }
            Set<ModelAttribute> attributes = getModelGenerator().getAttributes();
            int size = attributes.size();
            int i=0;
            for (ModelAttribute attribute : attributes) {
                myRouters.append(attribute.getName());
                myRouters.append(":$('#");                                // NOI18N
                myRouters.append(attribute.getName());
                myRouters.append("').val()");                             // NOI18N
                i++;
                if ( i <size ){
                    myRouters.append(',');
                }
                myRouters.append("\n");                                   // NOI18N
            }
        }
        else {
            String mainModelAttribute = getModelGenerator()
                    .getDisplayNameAlias();
            myRouters.append("/*\n * get values from the HTML controls and"); // NOI18N
            myRouters.append(" put them here as a hash of attributes\n"); // NOI18N
            if ( mainModelAttribute!= null ){
                myRouters.append(" * f.e.\n * ");                         // NOI18N
                myRouters.append(mainModelAttribute);
                myRouters.append(":$('#");                                // NOI18N
                myRouters.append(mainModelAttribute);
                myRouters.append("').val(),\n * ....\n");                 // NOI18N
            }
            myRouters.append(" */\n");                                // NOI18N
        }
        myRouters.append("};\n}\n");                                      // NOI18N
        
        myRouters.append("});\n");                                        // NOI18N
        myRouters.append("new ");                                         // NOI18N
        myRouters.append(myRouterName);                              
        myRouters.append("();\n");                                        // NOI18N
    }
    
    ModelGenerator getModelGenerator(){
        return myModelGenerator;
    }
    
    String getDetailsTemplate(){
        return myDetailsTemplateName;
    }
    
    String getListItemTemplate(){
        return myListItemTemplate;
    }
    
    String getCreateTemplate(){
        return "tpl-create";                                               // NOI18N
    }
    
    String getTableHeadId(){
        return "thead";                                                    // NOI18N
    }
    
    String getHeaderId(){
        if ( useUi() ){
            return "create";                                               // NOI18N
        }
        return "header";                                                   // NOI18N
    }
    
    boolean useUi(){
        return getModelGenerator().hasCollection() && 
                getModelGenerator().getUi() == JsUi.TABLESORTER;
    }
    
    String getContentId(){
        if ( useUi() ){
            return "details";                                              // NOI18N
        }
        return "content";                                                  // NOI18N
    }
    
    String getSideBarId(){
        return mySideBarId;
    }
    
    private ModelGenerator myModelGenerator;
    private StringBuilder myRouters;
    private String myRouterName;
    private String myDetailsTemplateName;
    private String mySideBarId;
    private String myListItemTemplate;
}
