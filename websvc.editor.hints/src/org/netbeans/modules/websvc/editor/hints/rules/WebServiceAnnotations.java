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
