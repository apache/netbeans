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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.saas.codegen;

/**
 *
 * @author PeterLiu
 */
public class Constants {
    
    public static final String AUTH = "auth"; //NOI18N

    public static final String LOGIN = "login"; //NOI18N
    
    public static final String CALLBACK = "callback"; //NOI18N

    
    public static final String JAVA_ANNOTATION_PACKAGE = "javax.annotation."; //NOI18N
    
    public static final String JAVA_ANNOTATION_RESOURCE = "Resource"; //NOI18N
    
    public static final String HTTP_SERVLET_PACKAGE = "javax.servlet.http.";       //NOI18N
    
    public static final String HTTP_SERVLET_REQUEST_CLASS = "HttpServletRequest";//NOI18N
    
    public static final String HTTP_SERVLET_RESPONSE_CLASS = "HttpServletResponse";//NOI18N
    
    public static final String HTTP_SERVLET_REQUEST_VARIABLE = "request";//NOI18N
    
    public static final String HTTP_SERVLET_RESPONSE_VARIABLE = "response";//NOI18N
    
    public static final String HTTP_RESOURCE_ANNOTATION = "Resource";     //NOI18N
    
    public static final String CONTENT_TYPE = "Content-Type";//NOI18N
    
    public static final String PUT_POST_CONTENT = "content";//NOI18N
    
    public static final String UNSUPPORTED_DROP = "WARN_UnsupportedDropTarget";//NOI18N
    
    public static final String HEADER_PARAMS = "headerParams"; // NOI18n
    
    public static final String QUERY_PARAMS = "queryParams"; // NOI18n
    
    public static final String PATH_PARAMS = "pathParams"; // NOI18n
    
    public static final String REST_CONNECTION = "RestConnection"; //NOI18N
            
    public static final String SERVICE_AUTHENTICATOR = "Authenticator"; //NOI18N
    
    public static final String SERVICE_AUTHORIZATION_FRAME = "AuthorizationFrame"; //NOI18N
    
    public static final String XML_TRANSIENT_ANNOTATION = "XmlTransient"; //NOI18N
   
    public static final String XML_ROOT_ELEMENT_ANNOTATION = "XmlRootElement";  //NOI18N
    
    public static final String XML_ELEMENT_ANNOTATION = "XmlElement";  //NOI18N
    
    public static final String XML_ATTRIBUTE_ANNOTATION = "XmlAttribute";  //NOI18N
    
    public static final String PATH_ANNOTATION = "Path"; //NOI18N
    
    public static final String URI_PARAM_ANNOTATION = "PathParam";       //NOI18N
    
    public static final String QUERY_PARAM_ANNOTATION = "QueryParam";       //NOI18N
    
    public static final String DEFAULT_VALUE_ANNOTATION = "DefaultValue";       //NOI18N
    
    public static final String GET_ANNOTATION = "GET";   //NOI18N
    
    public static final String HEAD_ANNOTATION = "HEAD";   //NOI18N

    public static final String POST_ANNOTATION = "POST";   //NOI18N
    
    public static final String PUT_ANNOTATION = "PUT";   //NOI18N
    
    public static final String DELETE_ANNOTATION = "DELETE";   //NOI18N
    
    public static final String PRODUCE_MIME_ANNOTATION = "ProduceMime"; //NOI18N
    
    public static final String CONSUME_MIME_ANNOTATION = "ConsumeMime"; //NOI18N
    
    public static final String HTTP_CONTEXT_ANNOTATION = "Context";     //NOI18N

    public static final String REST_API_PACKAGE = "javax.ws.rs.";       //NOI18N
    
    public static final String PATH = REST_API_PACKAGE + PATH_ANNOTATION;
    
    public static final String GET = REST_API_PACKAGE + GET_ANNOTATION;
    
    public static final String HEAD = REST_API_PACKAGE + HEAD_ANNOTATION;
     
    public static final String POST = REST_API_PACKAGE + POST_ANNOTATION;
    
    public static final String PUT = REST_API_PACKAGE + PUT_ANNOTATION;
    
    public static final String DELETE = REST_API_PACKAGE + DELETE_ANNOTATION;
    
    public static final String PRODUCE_MIME = REST_API_PACKAGE + PRODUCE_MIME_ANNOTATION;
    
    public static final String CONSUME_MIME = REST_API_PACKAGE + CONSUME_MIME_ANNOTATION;
    
    public static final String URI_PARAM = REST_API_PACKAGE + URI_PARAM_ANNOTATION;
    
    public static final String QUERY_PARAM = REST_API_PACKAGE + QUERY_PARAM_ANNOTATION;
    
    public static final String DEFAULT_VALUE = REST_API_PACKAGE + DEFAULT_VALUE_ANNOTATION;

    public static final String WEB_APPLICATION_EXCEPTION = REST_API_PACKAGE + "WebApplicationException"; // NOI18N
    
    public static final String HTTP_RESPONSE = REST_API_PACKAGE + "core.Response"; //NOI18N
    
    public static final String RESPONSE_BUILDER = REST_API_PACKAGE + "core.Response.Builder";       //NOI18N
    
    public static final String ENTITY_TYPE = REST_API_PACKAGE + "Entity"; // NOI18N
    
    public static final String HTTP_CONTEXT = REST_API_PACKAGE + "core.Context";    //NOI18N
    
    public static final String URI_INFO = REST_API_PACKAGE + "core.UriInfo";     //NOI18N
    
    public static final String URI_TYPE = "java.net.URI";       //NOI18N
    
    public static final String QUERY_TYPE = "javax.persistence.Query";       //NOI18N
    
    public static final String ENTITY_MANAGER_TYPE = "javax.persistence.EntityManager";       //NOI18N
    
    public static final String ENTITY_MANAGER_FACTORY = "javax.persistence.EntityManagerFactory";       //NOI18N
    
    public static final String ENTITY_TRANSACTION = "javax.persistence.EntityTransaction"; // NOI18N
    
    public static final String PERSISTENCE = "javax.persistence.Persistence"; // NOI18N
    
    public static final String NO_RESULT_EXCEPTION = "javax.persistence.NoResultException";        //NOI18N
    
    public static final String XML_ROOT_ELEMENT = "javax.xml.bind.annotation.XmlRootElement";             //NOI18N
    
    public static final String XML_ELEMENT = "javax.xml.bind.annotation.XmlElement";                 //NOI18N
    
    public static final String XML_ATTRIBUTE = "javax.xml.bind.annotation.XmlAttribute";                 //NOI18N
    
    public static final String XML_TRANSIENT = "javax.xml.bind.annotation.XmlTransient";                 //NOI18N
    
    public static final String VOID = "void";           //NOI18N
    
    public static final String COLLECTION_TYPE = "java.util.Collection"; //NOI18N
    
    public static final String COLLECTIONS_TYPE = "java.util.Collections";  //NOI18N
    
    public static final String ARRAY_LIST_TYPE = "java.util.ArrayList"; //NOI18N
   
    public static final String JAVA_EXT = "java"; //NI18N
    
    public static final String PHP_EXT = "php"; // NOI18N
        
    public enum DropFileType {
        JAVA_CLIENT("java", "System.out"), // NOI18N
        RESOURCE("resource", "System.out"), // NOI18N
        SERVLET("servlet", "out"), // NOI18N
        JSP("jsp", "out"), // NOI18N
        PHP("php", "out"); // NOI18N
        
        private final String prefix; 
        private final String printWriterType;
        
        DropFileType(String prefix, String printWriterType) {
            this.prefix = prefix;
            this.printWriterType = printWriterType;
        }
        
        public String value() {
            return name();
        }
        
        public String prefix() {
            return prefix;
        }
        
        public String getPrintWriterType() {
            return printWriterType;
        }
    }

    public enum MimeType {
        XML("application/xml", "Xml"),      //NOI18N
        JSON("application/json", "Json"),   //NOI18N
        TEXT("text/plain", "Text"),         //NOI18N
        HTML("text/html", "Html");          //NOI18N
        
        private final String value;
        private final String suffix;
        
        MimeType(String value, String suffix) {
            this.value = value;
            this.suffix = suffix;
        }
        
        public String value() {
            return value;
        }
        
        public String suffix() {
            return suffix;
        }
        
        public static MimeType find(String value) {
            for(MimeType m:values()) {
                if(m.value().equals(value)) {
                    return m;
                }
            }
            return null;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    public enum HttpMethodType {
        GET("get", Constants.GET), // NOI18N
        HEAD("head", Constants.HEAD), // NOI18N
        PUT("put", Constants.PUT), // NOI18N
        POST("post", Constants.POST), // NOI18N
        DELETE("delete", Constants.DELETE); // NOI18N
        
        private final String prefix; 
        private final String annotationType;
        
        HttpMethodType(String prefix, String annotationType) {
            this.prefix = prefix;
            this.annotationType = annotationType;
        }
        
        public String value() {
            return name();
        }
        
        public String prefix() {
            return prefix;
        }
        
        public String getAnnotationType() {
            return annotationType;
        }
    }
    
    public enum SaasAuthenticationType {
        PLAIN("Plain", "plain"), // NOI18N
        HTTP_BASIC("HttpBasic", "http-basic"), // NOI18N
        API_KEY("ApiKey", "api-key"), // NOI18N
        CUSTOM("Custom", "custom"), // NOI18N
        SIGNED_URL("SignedUrl", "signed-url"), // NOI18N
        SESSION_KEY("SessionKey", "session-key"); // NOI18N
        
        private final String classId;
        private final String value;
        
        SaasAuthenticationType(String classId, String value) {
            this.classId = classId;
            this.value = value;
        }
        
        public String getClassIdentifier() {
            return classId;
        }
        
        public String value() {
            return value;
        }
    }
   
    public static final String REST_JMAKI_DIR = "resources"; //NOI18N
    public static final String REST_STUBS_DIR = "rest"; //NOI18N
    
    public static final String PASSWORD = "password"; //NOI18N
}
