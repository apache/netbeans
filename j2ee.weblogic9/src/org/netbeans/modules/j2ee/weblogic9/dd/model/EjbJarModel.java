/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
public final class EjbJarModel extends BaseDescriptorModel {

    private static final Pattern SCHEMA_1031 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-ejb-jar/1\\.[0-2]/weblogic-ejb-jar\\.xsd"); // NOI18N

    private static final Pattern SCHEMA_1211 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-ejb-jar/1\\.[3-5]/weblogic-ejb-jar\\.xsd"); // NOI18N

    private static final Pattern SCHEMA_1221 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-ejb-jar/1\\.[6]/weblogic-ejb-jar\\.xsd"); // NOI18N

    private final WeblogicEjbJar bean;

    private EjbJarModel(WeblogicEjbJar bean) {
        super(bean);
        this.bean = bean;
    }
    
    public static EjbJarModel forFile(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            return forInputStream(is);
        } finally {
            is.close();
        }
    }
    
    public static EjbJarModel forInputStream(InputStream is) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        
        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(NullEntityResolver.newInstance());
            doc = builder.parse(is);
        } catch (SAXException ex) {
            throw new RuntimeException(NbBundle.getMessage(EjbJarModel.class, "MSG_CantCreateXMLDOMDocument"), ex);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(EjbJarModel.class, "MSG_CantCreateXMLDOMDocument"), ex);
        }        

        String ns = doc.getDocumentElement().getNamespaceURI();
        if ("http://xmlns.oracle.com/weblogic/weblogic-ejb-jar".equals(ns)) { // NOI18N
            String value = doc.getDocumentElement().getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"); // NOI18N
            if (SCHEMA_1031.matcher(value).matches()) {
                return new EjbJarModel(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WeblogicEjbJar.createGraph(doc));
            } else if (SCHEMA_1211.matcher(value).matches()) {
                return new EjbJarModel(org.netbeans.modules.j2ee.weblogic9.dd.ejb1211.WeblogicEjbJar.createGraph(doc));
            } else if (SCHEMA_1221.matcher(value).matches()) {
                return new EjbJarModel(org.netbeans.modules.j2ee.weblogic9.dd.ejb1221.WeblogicEjbJar.createGraph(doc));
            } else {
                return new EjbJarModel(org.netbeans.modules.j2ee.weblogic9.dd.ejb1221.WeblogicEjbJar.createGraph(doc));
            }
        } else if ("http://www.bea.com/ns/weblogic/weblogic-ejb-jar".equals(ns)) { // NOI18N
            return new EjbJarModel(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.WeblogicEjbJar.createGraph(doc));
        }
        return new EjbJarModel(org.netbeans.modules.j2ee.weblogic9.dd.ejb90.WeblogicEjbJar.createGraph(doc));  
    }
    
    public static EjbJarModel generate(@NullAllowed Version serverVersion) {
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

    private static EjbJarModel generate90() {
        org.netbeans.modules.j2ee.weblogic9.dd.ejb90.WeblogicEjbJar webLogicEjbJar = new org.netbeans.modules.j2ee.weblogic9.dd.ejb90.WeblogicEjbJar();
        webLogicEjbJar.createAttribute("xmlns:j2ee", "xmlns:j2ee", AttrProp.CDATA | AttrProp.IMPLIED, null, null); // NOI18N
        webLogicEjbJar.setAttributeValue("xmlns:j2ee", "http://java.sun.com/xml/ns/j2ee"); // NOI18N
        webLogicEjbJar.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicEjbJar.setAttributeValue("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/90 http://www.bea.com/ns/weblogic/90/weblogic-ejb-jar.xsd"); // NOI18N
        return new EjbJarModel(webLogicEjbJar);
    }
    
    private static EjbJarModel generate1030() {
        org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.WeblogicEjbJar webLogicEjbJar = new org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.WeblogicEjbJar();
        webLogicEjbJar.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicEjbJar.setAttributeValue("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/weblogic-ejb-jar http://www.bea.com/ns/weblogic/weblogic-ejb-jar/1.0/weblogic-ejb-jar.xsd"); // NOI18N
        return new EjbJarModel(webLogicEjbJar);
    }

    private static EjbJarModel generate1031() {
        org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WeblogicEjbJar webLogicEjbJar = new org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WeblogicEjbJar();
        webLogicEjbJar.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicEjbJar.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd http://xmlns.oracle.com/weblogic/weblogic-ejb-jar http://xmlns.oracle.com/weblogic/weblogic-ejb-jar/1.0/weblogic-ejb-jar.xsd"); // NOI18N
        return new EjbJarModel(webLogicEjbJar);
    }

    private static EjbJarModel generate1211() {
        org.netbeans.modules.j2ee.weblogic9.dd.ejb1211.WeblogicEjbJar webLogicEjbJar = new org.netbeans.modules.j2ee.weblogic9.dd.ejb1211.WeblogicEjbJar();
        webLogicEjbJar.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicEjbJar.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd http://xmlns.oracle.com/weblogic/weblogic-ejb-jar http://xmlns.oracle.com/weblogic/weblogic-ejb-jar/1.3/weblogic-ejb-jar.xsd"); // NOI18N
        return new EjbJarModel(webLogicEjbJar);
    }

    private static EjbJarModel generate1221() {
        org.netbeans.modules.j2ee.weblogic9.dd.ejb1221.WeblogicEjbJar webLogicEjbJar = new org.netbeans.modules.j2ee.weblogic9.dd.ejb1221.WeblogicEjbJar();
        webLogicEjbJar.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicEjbJar.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd http://xmlns.oracle.com/weblogic/weblogic-ejb-jar http://xmlns.oracle.com/weblogic/weblogic-ejb-jar/1.6/weblogic-ejb-jar.xsd"); // NOI18N
        return new EjbJarModel(webLogicEjbJar);
    }
    
    public void setReference(String ejbName, String ejbType, String referenceName, String jndiName) {
        WeblogicEnterpriseBeanType enterpriseBean = getWeblogicEnterpriseBean(ejbName);
        for (ResourceDescriptionType type : enterpriseBean.getResourceDescription()) {
            String refName = type.getResRefName();
            if (referenceName.equals(refName)) {
                type.setJndiName(jndiName);
                return;
            }
        }
        
        ResourceDescriptionType type = enterpriseBean.addResourceDescription();
        type.setResRefName(referenceName);
        type.setJndiName(jndiName);
    }

    public String getReferenceJndiName(String ejbName, String referenceName) {
        WeblogicEnterpriseBeanType enterpriseBean = null;
        for (WeblogicEnterpriseBeanType ejb : bean.getWeblogicEnterpriseBean()) {
            if (ejbName.equals(ejb.getEjbName())) {
                enterpriseBean = ejb;
                break;
            }
        }
        
        if (enterpriseBean == null) {
            return null;
        }

        for (ResourceDescriptionType type : enterpriseBean.getResourceDescription()) {
            String refName = type.getResRefName();
            if (referenceName.equals(refName)) {
                return type.getJndiName();
            }
        }
        return null;
    }

    public String getDestinationJndiName(String mdbName) {
        // TODO logical JMS destinations support ?
        // http://download.oracle.com/docs/cd/E12840_01/wls/docs103/ejb/message_beans.html#wp1164140
        WeblogicEnterpriseBeanType enterpriseBean = null;
        for (WeblogicEnterpriseBeanType ejb : bean.getWeblogicEnterpriseBean()) {
            if (mdbName.equals(ejb.getEjbName())) {
                enterpriseBean = ejb;
                break;
            }
        }
        
        if (enterpriseBean == null) {
            return null;
        }

        MessageDrivenDescriptorType descriptor = enterpriseBean.getMessageDrivenDescriptor();
        if (descriptor != null) {
            return descriptor.getDestinationJndiName();
        }
        return null;
    }

    private WeblogicEnterpriseBeanType getWeblogicEnterpriseBean(String name) {
        for (WeblogicEnterpriseBeanType enterpriseBean : bean.getWeblogicEnterpriseBean()) {
            if (name.equals(enterpriseBean.getEjbName())) {
                return enterpriseBean;
            }
        }
        
        WeblogicEnterpriseBeanType type = bean.addWeblogicEnterpriseBean();
        type.setEjbName(name);
        return type;
    }
}
