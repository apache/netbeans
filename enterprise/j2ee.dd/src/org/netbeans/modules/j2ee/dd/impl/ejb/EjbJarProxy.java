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

package org.netbeans.modules.j2ee.dd.impl.ejb;

import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Interceptors;
import org.netbeans.modules.j2ee.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.schema2beans.Schema2BeansUtil;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 *
 * @author  mk115033
 */
public class EjbJarProxy implements EjbJar {
    private EjbJar ejbJar;
    private String version;
    private List<PropertyChangeListener> listeners;
    public boolean writing=false;
    private org.xml.sax.SAXParseException error;
    private int ddStatus;
    private Schema2BeansUtil.ReindentationListener reindentationListener = new Schema2BeansUtil.ReindentationListener();

    /** Creates a new instance of EjbJarProxy */
    public EjbJarProxy(EjbJar ejbJar, String version) {
        this.ejbJar=ejbJar;
        this.version = version;
        listeners = new ArrayList<>();
        addPropertyChangeListener(reindentationListener);
    }

    public void setOriginal(EjbJar ejbJar) {
        if (this.ejbJar!=ejbJar) {
            for (int i=0;i<listeners.size();i++) {
                java.beans.PropertyChangeListener pcl =
                    listeners.get(i);
                if (this.ejbJar!=null) this.ejbJar.removePropertyChangeListener(pcl);
                if (ejbJar!=null) ejbJar.addPropertyChangeListener(pcl);

            }
            this.ejbJar=ejbJar;
            if (ejbJar!=null) setProxyVersion(ejbJar.getVersion().toString());
        }
    }

    public EjbJar getOriginal() {
        return ejbJar;
    }

    public void setProxyVersion(java.lang.String value) {
        if ((version == null && value != null) || (version != null && !version.equals(value))) {
            java.beans.PropertyChangeEvent evt =
                    new java.beans.PropertyChangeEvent(this, PROPERTY_VERSION, version, value);
            version = value;
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }

    /** Setter for version property.
     * Warning : Only the upgrade from lower to higher version is supported.
     * @param version ejb-jar version value
     */
    @Override
    public void setVersion(java.math.BigDecimal version) {
       String newVersion = version.toString();
        if (this.version.equals(newVersion)){
             return;
        }
        if (new BigDecimal(this.version).compareTo(version) > 0){
            throw new RuntimeException("Only the upgrade from lower to upper version is supported"); //NOI18N
        }
        if (!EjbJar.VERSION_2_1.equals(newVersion) || EjbJar.VERSION_3_0.equals(newVersion)){
            throw new RuntimeException("Unsupported version: " + newVersion 
                        + ". Only " + EjbJar.VERSION_2_1 + " and " + EjbJar.VERSION_3_0 + " are supported."); //NOI18N
        }
        if (ejbJar!=null) {
            org.w3c.dom.Document document = null;
            if (ejbJar instanceof org.netbeans.modules.j2ee.dd.impl.ejb.model_2_1.EjbJar){
                document =
                    ((org.netbeans.modules.j2ee.dd.impl.ejb.model_2_1.EjbJar)ejbJar).graphManager().getXmlDocument();
                
            }
            if (document!=null) {
                org.w3c.dom.Element docElement = document.getDocumentElement();
                if (docElement!=null) {
                    org.w3c.dom.DocumentType docType = document.getDoctype();
                    if (docType!=null) {
                        document.removeChild(docType); //NOI18N
                    }
                    boolean setTo30 = EjbJar.VERSION_3_0.equals(newVersion);
                    if (setTo30){
                        setVersionTo30(docElement);
                    } else {
                        setVersionTo21(docElement);
                    }
                }
            }
        }
    }

    private void setVersionTo21(Element docElement){
        docElement.setAttribute("version","2.1"); //NOI18N
        docElement.setAttribute("xmlns","http://java.sun.com/xml/ns/j2ee"); //NOI18N
        docElement.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance"); //NOI18N
        docElement.setAttribute("xsi:schemaLocation", //NOI18N
                        "http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd"); //NOI18N
    }

    private void setVersionTo30(Element docElement){
        docElement.setAttribute("version","3.0"); //NOI18N
        docElement.setAttribute("xmlns","http://java.sun.com/xml/ns/javaee"); //NOI18N
        docElement.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance"); //NOI18N
        docElement.setAttribute("xsi:schemaLocation", //NOI18N
                        "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd"); //NOI18N
    }

    @Override
    public java.math.BigDecimal getVersion() {
        return version == null ? BigDecimal.valueOf(0) : new BigDecimal(version);
    }
    @Override
    public org.xml.sax.SAXParseException getError() {
        return error;
    }
    public void setError(org.xml.sax.SAXParseException error) {
        this.error=error;
    }
    @Override
    public int getStatus() {
        return ddStatus;
    }
    public void setStatus(int value) {
        if (ddStatus!=value) {
            java.beans.PropertyChangeEvent evt =
                new java.beans.PropertyChangeEvent(this, PROPERTY_STATUS, ddStatus, value);
            ddStatus=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }

    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (ejbJar!=null) ejbJar.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (ejbJar!=null) ejbJar.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return ejbJar==null?null:ejbJar.createBean(beanName);
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException {
        return ejbJar==null?null:ejbJar.addBean(beanName, propertyNames, propertyValues, keyProperty);
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        return ejbJar==null?null:ejbJar.addBean(beanName);
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return ejbJar==null?null:ejbJar.findBeanByName(beanName, propertyName, value);
    }

    @Override
    public Map<String, String> getAllDescriptions() {
        return ejbJar == null ? new HashMap<>() : ejbJar.getAllDescriptions();
    }

    @Override
    public Map<String, String> getAllDisplayNames() {
        return ejbJar == null ? new HashMap<>() : ejbJar.getAllDisplayNames();
    }

    @Override
    public Map<String, String[]> getAllIcons() {
        return ejbJar == null ? new HashMap<>() : ejbJar.getAllIcons();
    }

    @Override
    public String getDefaultDescription() {
        return ejbJar==null?null:ejbJar.getDefaultDescription();
    }

    @Override
    public String getDefaultDisplayName() {
        return ejbJar==null?null:ejbJar.getDefaultDisplayName();
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon() {
        return ejbJar==null?null:ejbJar.getDefaultIcon();
    }

    @Override
    public String getDescription(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return ejbJar==null?null:ejbJar.getDescription(locale);
    }

    @Override
    public String getDisplayName(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return ejbJar==null?null:ejbJar.getDisplayName(locale);
    }

    @Override
    public java.lang.String getId() {
        return ejbJar==null?null:ejbJar.getId();
    }

    @Override
    public String getLargeIcon() {
        return ejbJar==null?null:ejbJar.getLargeIcon();
    }

    @Override
    public String getLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return ejbJar==null?null:ejbJar.getLargeIcon(locale);
    }

    @Override
    public String getSmallIcon() {
        return ejbJar==null?null:ejbJar.getSmallIcon();
    }

    @Override
    public String getSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return ejbJar==null?null:ejbJar.getSmallIcon(locale);
    }

    @Override
    public Object getValue(String name) {
        return ejbJar==null?null:ejbJar.getValue(name);
    }

    @Override
    public void merge(org.netbeans.modules.j2ee.dd.api.common.RootInterface bean, int mode) {
        if (bean instanceof EjbJarProxy) {
            bean = ((EjbJarProxy) bean).getOriginal();
        }
        if (ejbJar != bean && bean instanceof EjbJar) {
            EjbJar newEjbJar = (EjbJar) bean;
            if (ejbJar != null && ejbJar.getVersion().equals(newEjbJar.getVersion())) {
                removePropertyChangeListener(reindentationListener);
                ejbJar.merge(newEjbJar, mode);
                addPropertyChangeListener(reindentationListener);
            } else {
                setOriginal((EjbJar) newEjbJar.clone());
            }
        }
    }

    @Override
    public void removeAllDescriptions() {

        if (ejbJar!=null) ejbJar.removeAllDescriptions();
    }

    @Override
    public void removeAllDisplayNames() {
        if (ejbJar!=null) ejbJar.removeAllDisplayNames();
    }

    @Override
    public void removeAllIcons() {
        if (ejbJar!=null) ejbJar.removeAllIcons();
    }

    @Override
    public void removeDescription() {
        if (ejbJar!=null) ejbJar.removeDescription();
    }

    @Override
    public void removeDescriptionForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeDescriptionForLocale(locale);
    }

    @Override
    public void removeDisplayName() {
        if (ejbJar!=null) ejbJar.removeDisplayName();
    }

    @Override
    public void removeDisplayNameForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeDisplayNameForLocale(locale);
    }

    @Override
    public void removeIcon() {
        if (ejbJar!=null) ejbJar.removeIcon();
    }

    @Override
    public void removeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeIcon(locale);
    }

    @Override
    public void removeLargeIcon() {
        if (ejbJar!=null) ejbJar.removeLargeIcon();
    }

    @Override
    public void removeLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeLargeIcon(locale);
    }

    @Override
    public void removeSmallIcon() {
        if (ejbJar!=null) ejbJar.removeSmallIcon();
    }

    @Override
    public void removeSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeSmallIcon(locale);
    }

    @Override
    public void setAllDescriptions(Map descriptions) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setAllDescriptions(descriptions);
    }

    @Override
    public void setAllDisplayNames(Map displayNames) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setAllDisplayNames(displayNames);
    }

    @Override
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setAllIcons(locales, smallIcons, largeIcons);
    }

    @Override
    public void setDescription(String description) {
        if (ejbJar!=null) ejbJar.setDescription(description);
    }

    @Override
    public void setDescription(String locale, String description) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setDescription(locale, description);
    }

    @Override
    public void setDisplayName(String displayName) {
        if (ejbJar!=null) ejbJar.setDisplayName(displayName);
    }

    @Override
    public void setDisplayName(String locale, String displayName) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setDisplayName(locale, displayName);
    }

    @Override
    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon) {
        if (ejbJar!=null) ejbJar.setIcon(icon);
    }

    @Override
    public void setId(java.lang.String value) {
        if (ejbJar!=null) ejbJar.setId(value);
    }

    @Override
    public void setLargeIcon(String icon) {
        if (ejbJar!=null) ejbJar.setLargeIcon(icon);
    }

    @Override
    public void setLargeIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setLargeIcon(locale, icon);
    }

    @Override
    public void setSmallIcon(String icon) {
        if (ejbJar!=null) ejbJar.setSmallIcon(icon);
    }

    @Override
    public void setSmallIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setSmallIcon(locale, icon);
    }

    @Override
    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (ejbJar!=null) {
            writing=true;
            Schema2BeansUtil.write((BaseBean) ejbJar, os);
        }
    }

    @Override
    public void write(FileObject fo) throws java.io.IOException {
        if (ejbJar != null) {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject instanceof DDProviderDataObject) {
                ((DDProviderDataObject) dataObject).writeModel(ejbJar);
            } else {
                try (FileLock lock = fo.lock();
                        OutputStream os = fo.getOutputStream(lock)) {
                    writing = true;
                    write(os);
                }
            }
        }
    }

    @Override
    public Object clone() {
        EjbJarProxy proxy;
        if (ejbJar==null)
            proxy = new EjbJarProxy(null,version);
        else {
            EjbJar clonedEjbJar=(EjbJar)ejbJar.clone();
            proxy = new EjbJarProxy(clonedEjbJar,version);
            if (EjbJar.VERSION_4_0.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.ejb.model_4_0.EjbJar)clonedEjbJar)._setSchemaLocation
                    ("https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/ejb-jar_4_0.xsd");
                clonedEjbJar.setVersion(new java.math.BigDecimal(EjbJar.VERSION_3_2));
            } else if (EjbJar.VERSION_3_2.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.ejb.model_3_2.EjbJar)clonedEjbJar)._setSchemaLocation
                    ("http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/ejb-jar_3_2.xsd");
                clonedEjbJar.setVersion(new java.math.BigDecimal(EjbJar.VERSION_3_2));
            } else if (EjbJar.VERSION_3_1.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.ejb.model_3_1.EjbJar)clonedEjbJar)._setSchemaLocation
                    ("http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/j2ee/ejb-jar_3_1.xsd");
                clonedEjbJar.setVersion(new java.math.BigDecimal(EjbJar.VERSION_3_1));
            } else if (EjbJar.VERSION_3_0.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.ejb.model_3_0.EjbJar)clonedEjbJar)._setSchemaLocation
                    ("http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/j2ee/ejb-jar_3_0.xsd");
                clonedEjbJar.setVersion(new java.math.BigDecimal(EjbJar.VERSION_3_0));
            } else if (EjbJar.VERSION_2_1.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.ejb.model_2_1.EjbJar)clonedEjbJar)._setSchemaLocation
                    ("http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd");
                clonedEjbJar.setVersion(new java.math.BigDecimal(EjbJar.VERSION_2_1));
            }
        }
        proxy.setError(error);
        proxy.setStatus(ddStatus);
        return proxy;
    }

    public boolean isWriting() {
        return writing;
    }

    public void setWriting(boolean writing) {
        this.writing=writing;
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans getEnterpriseBeans() {
        return ejbJar==null?null:ejbJar.getEnterpriseBeans();
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor getSingleAssemblyDescriptor() {
        return ejbJar==null?null:ejbJar.getSingleAssemblyDescriptor();
    }

    @Override
    public String getSingleEjbClientJar() {
        return ejbJar==null?null:ejbJar.getSingleEjbClientJar();
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.ejb.Relationships getSingleRelationships() {
        return ejbJar==null?null:ejbJar.getSingleRelationships();
    }

    @Override
    public void setAssemblyDescriptor(org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor value) {
        if (ejbJar!=null) ejbJar.setAssemblyDescriptor(value);
    }

    @Override
    public void setEjbClientJar(String value) {
        if (ejbJar!=null) ejbJar.setEjbClientJar(value);
    }

    @Override
    public void setEnterpriseBeans(org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans value) {
        if (ejbJar!=null) ejbJar.setEnterpriseBeans(value);
    }

    @Override
    public void setRelationships(org.netbeans.modules.j2ee.dd.api.ejb.Relationships value) {
        if (ejbJar!=null) ejbJar.setRelationships(value);
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor newAssemblyDescriptor() {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.newAssemblyDescriptor();
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans newEnterpriseBeans() {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.newEnterpriseBeans();
    }

    @Override
    public org.netbeans.modules.j2ee.dd.api.ejb.Relationships newRelationships() {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.newRelationships();
    }

    @Override
    public void setInterceptors(Interceptors valueInterface) throws VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setInterceptors(valueInterface);
    }

    @Override
    public Interceptors newInterceptors() throws VersionNotSupportedException {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.newInterceptors();
    }

    @Override
    public Interceptors getInterceptors() throws VersionNotSupportedException {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.getInterceptors();
    }
}
