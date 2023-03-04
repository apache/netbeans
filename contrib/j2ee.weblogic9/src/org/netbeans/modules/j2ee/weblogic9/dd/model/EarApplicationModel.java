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

package org.netbeans.modules.j2ee.weblogic9.dd.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.NullEntityResolver;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Hejl
 */
public final class EarApplicationModel extends BaseDescriptorModel {

    private static final Pattern SCHEMA_1031 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-application/1\\.[0-3]/weblogic-application\\.xsd"); // NOI18N

    private static final Pattern SCHEMA_1211 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-application/1\\.[4-6]/weblogic-application\\.xsd"); // NOI18N

    private static final Pattern SCHEMA_1221 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-application/1\\.[7]/weblogic-application\\.xsd"); // NOI18N

    private final WeblogicApplication bean;

    private EarApplicationModel(WeblogicApplication bean) {
        super(bean);
        this.bean = bean;
    }
    
    public static EarApplicationModel forFile(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            return forInputStream(is);
        } finally {
            is.close();
        }
    }
    
    public static EarApplicationModel forInputStream(InputStream is) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        
        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(NullEntityResolver.newInstance());
            doc = builder.parse(is);
        } catch (SAXException ex) {
            throw new RuntimeException(NbBundle.getMessage(EarApplicationModel.class, "MSG_CantCreateXMLDOMDocument"), ex);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(EarApplicationModel.class, "MSG_CantCreateXMLDOMDocument"), ex);
        }        

        String ns = doc.getDocumentElement().getNamespaceURI();
        if ("http://xmlns.oracle.com/weblogic/weblogic-application".equals(ns)) { // NOI18N
            String value = doc.getDocumentElement().getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"); // NOI18N
            if (SCHEMA_1031.matcher(value).matches()) {
                return new EarApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.ear1031.WeblogicApplication.createGraph(doc));
            } else if (SCHEMA_1211.matcher(value).matches()) {
                return new EarApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WeblogicApplication.createGraph(doc));
            } else if (SCHEMA_1221.matcher(value).matches()) {
                return new EarApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.ear1221.WeblogicApplication.createGraph(doc));
            } else {
                return new EarApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.ear1221.WeblogicApplication.createGraph(doc));
            }
        } else if ("http://www.bea.com/ns/weblogic/weblogic-application".equals(ns)) { // NOI18N
            return new EarApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.ear1030.WeblogicApplication.createGraph(doc));
        }
        return new EarApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.ear90.WeblogicApplication.createGraph(doc));  
    }
    
    public static EarApplicationModel generate(@NullAllowed Version serverVersion) {
        if (serverVersion != null) {
            if (serverVersion.isAboveOrEqual(VERSION_12_2_1)) {
                return generate1221();
            } else if (serverVersion.isAboveOrEqual(VERSION_12_1_1)) {
                return generate1211();
            } else if (serverVersion.isAboveOrEqual(VERSION_10_3_1)) {
                return generate1031();
            } else if (serverVersion.isAboveOrEqual(VERSION_10_3_0)) {
                return generate1030();
            }
        }
        return generate90();
    }

    private static EarApplicationModel generate90() {
        org.netbeans.modules.j2ee.weblogic9.dd.ear90.WeblogicApplication webLogicApplication = new org.netbeans.modules.j2ee.weblogic9.dd.ear90.WeblogicApplication();
        webLogicApplication.createAttribute("xmlns:j2ee", "xmlns:j2ee", AttrProp.CDATA | AttrProp.IMPLIED, null, null); // NOI18N
        webLogicApplication.setAttributeValue("xmlns:j2ee", "http://java.sun.com/xml/ns/j2ee"); // NOI18N
        webLogicApplication.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicApplication.setAttributeValue("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/90 http://www.bea.com/ns/weblogic/90/weblogic-application.xsd"); // NOI18N
        return new EarApplicationModel(webLogicApplication);
    }
    
    private static EarApplicationModel generate1030() {
        org.netbeans.modules.j2ee.weblogic9.dd.ear1030.WeblogicApplication webLogicApplication = new org.netbeans.modules.j2ee.weblogic9.dd.ear1030.WeblogicApplication();
        webLogicApplication.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicApplication.setAttributeValue("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/weblogic-application http://www.bea.com/ns/weblogic/weblogic-application/1.0/weblogic-application.xsd"); // NOI18N
        return new EarApplicationModel(webLogicApplication);
    }

    private static EarApplicationModel generate1031() {
        org.netbeans.modules.j2ee.weblogic9.dd.ear1031.WeblogicApplication webLogicApplication = new org.netbeans.modules.j2ee.weblogic9.dd.ear1031.WeblogicApplication();
        webLogicApplication.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicApplication.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/javaee_5.xsd http://xmlns.oracle.com/weblogic/weblogic-application http://xmlns.oracle.com/weblogic/weblogic-application/1.0/weblogic-application.xsd"); // NOI18N
        return new EarApplicationModel(webLogicApplication);
    }

    private static EarApplicationModel generate1211() {
        org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WeblogicApplication webLogicApplication = new org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WeblogicApplication();
        webLogicApplication.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicApplication.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/javaee_5.xsd http://xmlns.oracle.com/weblogic/weblogic-application http://xmlns.oracle.com/weblogic/weblogic-application/1.4/weblogic-application.xsd"); // NOI18N
        return new EarApplicationModel(webLogicApplication);
    }

    private static EarApplicationModel generate1221() {
        org.netbeans.modules.j2ee.weblogic9.dd.ear1221.WeblogicApplication webLogicApplication = new org.netbeans.modules.j2ee.weblogic9.dd.ear1221.WeblogicApplication();
        webLogicApplication.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicApplication.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/javaee_5.xsd http://xmlns.oracle.com/weblogic/weblogic-application http://xmlns.oracle.com/weblogic/weblogic-application/1.7/weblogic-application.xsd"); // NOI18N
        return new EarApplicationModel(webLogicApplication);
    }
}
