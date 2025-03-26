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

package org.netbeans.modules.websvc.rest.model.api;

/**
 * JSR-311 annotation constants
 * 
 * @author nam
 */
public class RestConstants {

    public static final String PATH_ANNOTATION = "Path"; //NOI18N
    
    public static final String PATH_PARAM_ANNOTATION = "PathParam";       //NOI18N
    
    public static final String QUERY_PARAM_ANNOTATION = "QueryParam";       //NOI18N
    
    public static final String DEFAULT_VALUE_ANNOTATION = "DefaultValue";       //NOI18N
    
    public static final String GET_ANNOTATION = "GET";   //NOI18N
    
    public static final String POST_ANNOTATION = "POST";   //NOI18N
    
    public static final String PUT_ANNOTATION = "PUT";   //NOI18N
    
    public static final String DELETE_ANNOTATION = "DELETE";   //NOI18N
    
    public static final String PRODUCE_MIME_ANNOTATION = "Produces"; //NOI18N
    
    public static final String CONSUME_MIME_ANNOTATION = "Consumes"; //NOI18N
    
    public static final String SINGLETON_ANNOTATION = "Singleton";     //NOI18N
    
    public static final String CONTEXT_ANNOTATION = "Context";     //NOI18N

    public static final String STATELESS_ANNOTATION = "Stateless"; //NOI18N

    public static final String REST_API_PACKAGE = "javax.ws.rs.";       //NOI18N

    public static final String REST_API_PACKAGE_JAKARTA = "jakarta.ws.rs.";       //NOI18N

    public static final String PROVIDER_ANNOTATION = "javax.ws.rs.ext.Provider"; //NOI18N

    public static final String PROVIDER_ANNOTATION_JAKARTA = "jakarta.ws.rs.ext.Provider"; //NOI18N

    public static final String JavaEE5_EJB_PACKAGE = "javax.ejb.";       //NOI18N

    public static final String JavaEE5_EJB_PACKAGE_JAKARTA = "jakarta.ejb.";       //NOI18N
    
    public static final String REST_API_CORE_PACKAGE = REST_API_PACKAGE + "core.";      //NOI18N

    public static final String REST_API_CORE_PACKAGE_JAKARTA = REST_API_PACKAGE_JAKARTA + "core.";      //NOI18N
    
    public static final String JERSEY_PACKAGE = "com.sun.jersey.";       //NOI18N
    
    public static final String JERSEY_API_PACKAGE = JERSEY_PACKAGE + "api.";        //NOI18N
    
    public static final String JERSEY_SPI_PACKAGE = JERSEY_PACKAGE + "spi.";        //NOI18M

    public static final String PATH = REST_API_PACKAGE + PATH_ANNOTATION;

    public static final String PATH_JAKARTA = REST_API_PACKAGE_JAKARTA + PATH_ANNOTATION;
    
    public static final String GET = REST_API_PACKAGE + GET_ANNOTATION;

    public static final String GET_JAKARTA = REST_API_PACKAGE_JAKARTA + GET_ANNOTATION;

    public static final String POST = REST_API_PACKAGE + POST_ANNOTATION;

    public static final String POST_JAKARTA = REST_API_PACKAGE_JAKARTA + POST_ANNOTATION;
    
    public static final String PUT = REST_API_PACKAGE + PUT_ANNOTATION;

    public static final String PUT_JAKARTA = REST_API_PACKAGE_JAKARTA + PUT_ANNOTATION;
    
    public static final String DELETE = REST_API_PACKAGE + DELETE_ANNOTATION;

    public static final String DELETE_JAKARTA = REST_API_PACKAGE_JAKARTA + DELETE_ANNOTATION;
    
    public static final String PRODUCE_MIME = REST_API_PACKAGE + PRODUCE_MIME_ANNOTATION;

    public static final String PRODUCE_MIME_JAKARTA = REST_API_PACKAGE_JAKARTA + PRODUCE_MIME_ANNOTATION;
    
    public static final String CONSUME_MIME = REST_API_PACKAGE + CONSUME_MIME_ANNOTATION;

    public static final String CONSUME_MIME_JAKARTA = REST_API_PACKAGE_JAKARTA + CONSUME_MIME_ANNOTATION;
    
    public static final String PATH_PARAM = REST_API_PACKAGE + PATH_PARAM_ANNOTATION;

    public static final String PATH_PARAM_JAKARTA = REST_API_PACKAGE_JAKARTA + PATH_PARAM_ANNOTATION;
    
    public static final String QUERY_PARAM = REST_API_PACKAGE + QUERY_PARAM_ANNOTATION;

    public static final String QUERY_PARAM_JAKARTA = REST_API_PACKAGE_JAKARTA + QUERY_PARAM_ANNOTATION;
    
    public static final String DEFAULT_VALUE = REST_API_PACKAGE + DEFAULT_VALUE_ANNOTATION;

    public static final String DEFAULT_VALUE_JAKARTA = REST_API_PACKAGE_JAKARTA + DEFAULT_VALUE_ANNOTATION;
    
    public static final String WEB_APPLICATION_EXCEPTION = REST_API_PACKAGE + "WebApplicationException";

    public static final String WEB_APPLICATION_EXCEPTION_JAKARTA = REST_API_PACKAGE_JAKARTA + "WebApplicationException";
    
    public static final String HTTP_RESPONSE = REST_API_CORE_PACKAGE + "Response"; //NOI18N

    public static final String HTTP_RESPONSE_JAKARTA = REST_API_CORE_PACKAGE_JAKARTA + "Response"; //NOI18N
    
    public static final String RESPONSE_BUILDER = REST_API_CORE_PACKAGE + "Response.Builder";       //NOI18N

    public static final String RESPONSE_BUILDER_JAKARTA = REST_API_CORE_PACKAGE_JAKARTA + "Response.Builder";       //NOI18N
    
    public static final String ENTITY_TYPE = REST_API_PACKAGE + "Entity";

    public static final String ENTITY_TYPE_JAKARTA = REST_API_PACKAGE_JAKARTA + "Entity";
    
    public static final String CONTEXT = REST_API_CORE_PACKAGE + "Context";    //NOI18N

    public static final String CONTEXT_JAKARTA = REST_API_CORE_PACKAGE_JAKARTA + "Context";    //NOI18N

    public static final String URI_INFO = REST_API_CORE_PACKAGE + "UriInfo";     //NOI18N
    
    public static final String URI_INFO_JAKARTA = REST_API_CORE_PACKAGE_JAKARTA + "UriInfo";     //NOI18N
    
    public static final String URI_BUILDER = REST_API_CORE_PACKAGE + "UriBuilder"; //NOI18N

    public static final String URI_BUILDER_JAKARTA = REST_API_CORE_PACKAGE_JAKARTA + "UriBuilder"; //NOI18N

    public static final String SINGLETON = JERSEY_SPI_PACKAGE + "resource." + SINGLETON_ANNOTATION;     //NOI18N
    
    public static final String RESOURCE_CONTEXT = JERSEY_API_PACKAGE + "core.ResourceContext";          //NOI18N
    
    public static final String STATELESS = JavaEE5_EJB_PACKAGE + "Stateless";    //NOI18N

    public static final String STATELESS_JAKARTA = JavaEE5_EJB_PACKAGE_JAKARTA + "Stateless";    //NOI18N

    public static final String EJB = JavaEE5_EJB_PACKAGE + "EJB";    //NOI18N

    public static final String EJB_JAKARTA = JavaEE5_EJB_PACKAGE_JAKARTA + "EJB";    //NOI18N

    public static final String APPLICATION_PATH = REST_API_PACKAGE + "ApplicationPath"; //NOI18N

    public static final String APPLICATION_PATH_JAKARTA = REST_API_PACKAGE_JAKARTA + "ApplicationPath"; //NOI18N
    
    public static final String GET_CLASSES = "getClasses"; //NOI18N
    
    public static final String GET_REST_RESOURCE_CLASSES2 = "addRestResourceClasses";//NOI18N
}
