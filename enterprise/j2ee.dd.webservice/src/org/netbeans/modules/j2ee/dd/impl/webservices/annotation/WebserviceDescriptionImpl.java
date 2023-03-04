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
package org.netbeans.modules.j2ee.dd.impl.webservices.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.JavaContextListener;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;

/**
 *
 * @author mkuchtiak
 */
public class WebserviceDescriptionImpl extends PersistentObject implements WebserviceDescription, JavaContextListener {

    // persistent
    private String serviceName;
    private String wsdlLocation;
    private boolean isProvider;
    
    // transient: set to null in javaContextLeft()
    private PortComponent[] portComponents;
    
    public WebserviceDescriptionImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        helper.addJavaContextListener(this);
        boolean valid = refresh(typeElement);
        assert valid;
    }
    
    boolean refresh(TypeElement typeElement) {
        if (typeElement.getKind() == ElementKind.INTERFACE) {
            // don't consider interfaces (SEI classes)
            return false;
        }
        AnnotationModelHelper helper = getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror webServiceAnn = annByType.get("javax.jws.WebService"); // NOI18N 
        if (webServiceAnn == null) {
            webServiceAnn = annByType.get("javax.xml.ws.WebServiceProvider"); // NOI18N
            isProvider = true;
        }
        if (webServiceAnn == null) {
            return false;
        }
        
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("serviceName", AnnotationParser.defaultValue(typeElement.getSimpleName().toString() + "Service")); // NOI18N
        parser.expectString("wsdlLocation", AnnotationParser.defaultValue("")); // NOI18N
        ParseResult parseResult = parser.parse(webServiceAnn); //NOI18N
        serviceName = parseResult.get("serviceName", String.class); // NOI18N
        wsdlLocation = parseResult.get("wsdlLocation", String.class); // NOI18N
        return true;
    }
    
    public void javaContextLeft() {
        portComponents = null;
    }
    
    public String getWebserviceDescriptionName() {
        return serviceName;
    }

    public String getWsdlFile() {
        return wsdlLocation;
    }
    
    public String getDisplayName() {
        return isProvider ? "javax.xml.ws.WebServiceProvider" : serviceName; //NOI18N
    }

    public PortComponent[] getPortComponent() {
        initPortComonents();
        return portComponents;
    }
    
    public int sizePortComponent() {
        initPortComonents();
        return portComponents.length;
    }

    public PortComponent getPortComponent(int index) {
        initPortComonents();
        return portComponents[index];
    }
    
    private void initPortComonents() {
        
        if (portComponents != null) {
            return;
        }
        
        AnnotationModelHelper helper = getHelper();
        TypeElement typeElement = getTypeElement();
        if (typeElement == null) {
            // the element was removed, should get an event soon
            portComponents = new PortComponent[0];
            return;
        }
        
        // javax.ejb.EJBs is array of javax.ejb.EJB and is applicable to class
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("portName", AnnotationParser.defaultValue(typeElement.getSimpleName().toString()+"Port")); // NOI18N
        parser.expectString("name", AnnotationParser.defaultValue(typeElement.getSimpleName().toString())); // NOI18N
        parser.expectString("endpointInterface", AnnotationParser.defaultValue(typeElement.getQualifiedName().toString())); // NOI18N
        parser.expectString("targetNamespace", AnnotationParser.defaultValue("http://"+getPackageNameFromTypeElement(typeElement)+"/")); // NOI18N
        ParseResult parseResult = parser.parse(annByType.get("javax.jws.WebService")); //NOI18N
        String portName = parseResult.get("portName", String.class); // NOI18N
        String portTypeName = parseResult.get("name", String.class); // NOI18N
        String endpointInterface = parseResult.get("endpointInterface", String.class); // NOI18N
        String targetNamespace = parseResult.get("targetNamespace", String.class); // NOI18N
        
        portComponents = new PortComponent[]{
            new PortComponentImpl(helper, typeElement, serviceName, portName, portTypeName, endpointInterface, targetNamespace)
        };
    }
    

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescriptionId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescriptionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescriptionXmlLang(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescriptionXmlLang() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayNameId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDisplayNameId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayNameXmlLang(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDisplayNameXmlLang() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setIcon(Icon value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Icon newIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWebserviceDescriptionName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWebserviceDescriptionNameId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getWebserviceDescriptionNameId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWsdlFile(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setJaxrpcMappingFile(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getJaxrpcMappingFile() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPortComponent(int index, PortComponent value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPortComponent(PortComponent[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPortComponent(PortComponent value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePortComponent(PortComponent value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PortComponent newPortComponent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // </editor-fold>

    private String getPackageNameFromTypeElement(TypeElement typeElement) {
        Element el = typeElement;
        while (ElementKind.PACKAGE != el.getKind()) {
            el = el.getEnclosingElement();
        }
        return ((PackageElement)el).getQualifiedName().toString();
    }
}
