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
package org.netbeans.modules.websvc.editor.hints.rules;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public interface WebServiceAnnotations {

    public static final String ANNOTATION_WEBSERVICE = "javax.jws.WebService";
    public static final String ANNOTATION_WEBMETHOD = "javax.jws.WebMethod";
    public static final String ANNOTATION_WEBPARAM = "javax.jws.WebParam";
    public static final String ANNOTATION_WEBRESULT = "javax.jws.WebResult";
    public static final String ANNOTATION_ONEWAY = "javax.jws.Oneway";
    public static final String ANNOTATION_HANDLERCHAIN = "javax.jws.HandlerChain";
    public static final String ANNOTATION_SOAPMESSAGEHANDLERS = "javax.jws.soap.SOAPMessageHandlers";
    public static final String ANNOTATION_INITPARAM = "javax.jws.soap.InitParam";
    public static final String ANNOTATION_SOAPBINDING = "javax.jws.soap.SOAPBinding";
    public static final String ANNOTATION_SOAPMESSAGEHANDLER = "javax.jws.soap.SOAPMessageHandler";

    public static final String ANNOTATION_ATTRIBUTE_SERVICE_NAME = "serviceName";
    public static final String ANNOTATION_ATTRIBUTE_SEI = "endpointInterface";
    public static final String ANNOTATION_ATTRIBUTE_PORTNAME = "portName";
    public static final String ANNOTATION_ATTRIBUTE_OPERATIONNAME = "operationName";
    public static final String ANNOTATION_ATTRIBUTE_ACTION = "action";
    public static final String ANNOTATION_ATTRIBUTE_WSDLLOCATION = "wsdlLocation";
    public static final String ANNOTATION_ATTRIBUTE_MODE = "mode";
    public static final String ANNOTATION_ATTRIBUTE_NAME = "name";
    public static final String ANNOTATION_ATTRIBUTE_TARGETNAMESPACE = "targetNamespace";
    public static final String ANNOTATION_ATTRIBUTE_EXCLUDE = "exclude";
    public static final String ANNOTATION_ATTRIBUTE_STYLE = "style";
    public static final String ANNOTATION_ATTRIBUTE_USE = "use";
    public static final String ANNOTATION_ATTRIBUTE_PARAMETERSTYLE = "parameterStyle";
}
