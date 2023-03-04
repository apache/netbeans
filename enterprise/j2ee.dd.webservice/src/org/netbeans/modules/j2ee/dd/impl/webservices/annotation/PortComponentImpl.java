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
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import javax.xml.namespace.QName;

/**
 *
 * @author mkuchtiak
 */
public class PortComponentImpl implements PortComponent {
    
    private final AnnotationModelHelper helper;
    private final TypeElement typeElement;
    private final String serviceName, portName, portTypeName, endpointInterface, targetNamespace;
    private ServiceImplBean serviceImplBean;
    
    public PortComponentImpl(AnnotationModelHelper helper, TypeElement typeElement, String serviceName, String portName, String portTypeName, String endpointInterface, String targetNamespace) {
        this.helper = helper;
        this.typeElement = typeElement;
        this.serviceName=serviceName;
        this.portName=portName;
        this.portTypeName=portTypeName;
        this.endpointInterface=endpointInterface;
        this.targetNamespace=targetNamespace;
    }
    
    public String getPortComponentName() {
        return portTypeName;
    }
    
    public QName getWsdlService() {
        return new QName(targetNamespace, serviceName);
    }
    
    public QName getWsdlPort() {
        return new QName(targetNamespace, portName);
    }

    public String getServiceEndpointInterface() {
        return endpointInterface;
    }

    public ServiceImplBean getServiceImplBean() {
        if (serviceImplBean==null) {
            serviceImplBean = new ServiceImplBeanImpl(portTypeName, helper, typeElement);
        }
        return serviceImplBean;
    }

    public String getDisplayName() {
        // use display name to hold information about implementation class
        return typeElement.getQualifiedName().toString();
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

    public void setPortComponentName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPortComponentNameId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPortComponentNameId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setWsdlService(QName value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWsdlServiceId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getWsdlServiceId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setWsdlPort(QName value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWsdlPortId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getWsdlPortId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceEndpointInterface(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceImplBean(ServiceImplBean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceImplBean newServiceImplBean() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHandler(int index, PortComponentHandler value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PortComponentHandler getHandler(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHandler(PortComponentHandler[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PortComponentHandler[] getHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addHandler(PortComponentHandler value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeHandler(PortComponentHandler value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PortComponentHandler newPortComponentHandler() {
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
}
