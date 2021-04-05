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

import java.math.BigDecimal;
import java.io.OutputStream;
import org.w3c.dom.Element;

/**
 *
 * @author  mk115033
 */
public class EjbJarProxy implements EjbJar {
    private EjbJar ejbJar;
    private String version;
    private java.util.List listeners;
    public boolean writing=false;
    private org.xml.sax.SAXParseException error;
    private int ddStatus;
    private Schema2BeansUtil.ReindentationListener reindentationListener = new Schema2BeansUtil.ReindentationListener();

    /** Creates a new instance of EjbJarProxy */
    public EjbJarProxy(EjbJar ejbJar, String version) {
        this.ejbJar=ejbJar;
        this.version = version;
        listeners = new java.util.ArrayList();
        addPropertyChangeListener(reindentationListener);
    }

    public void setOriginal(EjbJar ejbJar) {
        if (this.ejbJar!=ejbJar) {
            for (int i=0;i<listeners.size();i++) {
                java.beans.PropertyChangeListener pcl =
                    (java.beans.PropertyChangeListener)listeners.get(i);
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
                ((java.beans.PropertyChangeListener) listeners.get(i)).propertyChange(evt);
            }
        }
    }

    /** Setter for version property.
     * Warning : Only the upgrade from lower to higher version is supported.
     * @param version ejb-jar version value
     */
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

    public java.math.BigDecimal getVersion() {
        return version == null ? BigDecimal.valueOf(0) : new BigDecimal(version);
    }
    public org.xml.sax.SAXParseException getError() {
        return error;
    }
    public void setError(org.xml.sax.SAXParseException error) {
        this.error=error;
    }
    public int getStatus() {
        return ddStatus;
    }
    public void setStatus(int value) {
        if (ddStatus!=value) {
            java.beans.PropertyChangeEvent evt =
                new java.beans.PropertyChangeEvent(this, PROPERTY_STATUS, new Integer(ddStatus), new Integer(value));
            ddStatus=value;
            for (int i=0;i<listeners.size();i++) {
                ((java.beans.PropertyChangeListener)listeners.get(i)).propertyChange(evt);
            }
        }
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (ejbJar!=null) ejbJar.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (ejbJar!=null) ejbJar.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return ejbJar==null?null:ejbJar.createBean(beanName);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException {
        return ejbJar==null?null:ejbJar.addBean(beanName, propertyNames, propertyValues, keyProperty);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        return ejbJar==null?null:ejbJar.addBean(beanName);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return ejbJar==null?null:ejbJar.findBeanByName(beanName, propertyName, value);
    }

    public java.util.Map getAllDescriptions() {
        return ejbJar==null?new java.util.HashMap():ejbJar.getAllDescriptions();
    }

    public java.util.Map getAllDisplayNames() {
        return ejbJar==null?new java.util.HashMap():ejbJar.getAllDisplayNames();
    }

    public java.util.Map getAllIcons() {
        return ejbJar==null?new java.util.HashMap():ejbJar.getAllIcons();
    }

    public String getDefaultDescription() {
        return ejbJar==null?null:ejbJar.getDefaultDescription();
    }

    public String getDefaultDisplayName() {
        return ejbJar==null?null:ejbJar.getDefaultDisplayName();
    }

    public org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon() {
        return ejbJar==null?null:ejbJar.getDefaultIcon();
    }

    public String getDescription(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return ejbJar==null?null:ejbJar.getDescription(locale);
    }

    public String getDisplayName(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return ejbJar==null?null:ejbJar.getDisplayName(locale);
    }

    public java.lang.String getId() {
        return ejbJar==null?null:ejbJar.getId();
    }

    public String getLargeIcon() {
        return ejbJar==null?null:ejbJar.getLargeIcon();
    }

    public String getLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return ejbJar==null?null:ejbJar.getLargeIcon(locale);
    }

    public String getSmallIcon() {
        return ejbJar==null?null:ejbJar.getSmallIcon();
    }

    public String getSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return ejbJar==null?null:ejbJar.getSmallIcon(locale);
    }

    public Object getValue(String name) {
        return ejbJar==null?null:ejbJar.getValue(name);
    }

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

    public void removeAllDescriptions() {

        if (ejbJar!=null) ejbJar.removeAllDescriptions();
    }

    public void removeAllDisplayNames() {
        if (ejbJar!=null) ejbJar.removeAllDisplayNames();
    }

    public void removeAllIcons() {
        if (ejbJar!=null) ejbJar.removeAllIcons();
    }

    public void removeDescription() {
        if (ejbJar!=null) ejbJar.removeDescription();
    }

    public void removeDescriptionForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeDescriptionForLocale(locale);
    }

    public void removeDisplayName() {
        if (ejbJar!=null) ejbJar.removeDisplayName();
    }

    public void removeDisplayNameForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeDisplayNameForLocale(locale);
    }

    public void removeIcon() {
        if (ejbJar!=null) ejbJar.removeIcon();
    }

    public void removeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeIcon(locale);
    }

    public void removeLargeIcon() {
        if (ejbJar!=null) ejbJar.removeLargeIcon();
    }

    public void removeLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeLargeIcon(locale);
    }

    public void removeSmallIcon() {
        if (ejbJar!=null) ejbJar.removeSmallIcon();
    }

    public void removeSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.removeSmallIcon(locale);
    }

    public void setAllDescriptions(java.util.Map descriptions) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setAllDescriptions(descriptions);
    }

    public void setAllDisplayNames(java.util.Map displayNames) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setAllDisplayNames(displayNames);
    }

    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setAllIcons(locales, smallIcons, largeIcons);
    }

    public void setDescription(String description) {
        if (ejbJar!=null) ejbJar.setDescription(description);
    }

    public void setDescription(String locale, String description) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setDescription(locale, description);
    }

    public void setDisplayName(String displayName) {
        if (ejbJar!=null) ejbJar.setDisplayName(displayName);
    }

    public void setDisplayName(String locale, String displayName) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setDisplayName(locale, displayName);
    }

    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon) {
        if (ejbJar!=null) ejbJar.setIcon(icon);
    }

    public void setId(java.lang.String value) {
        if (ejbJar!=null) ejbJar.setId(value);
    }

    public void setLargeIcon(String icon) {
        if (ejbJar!=null) ejbJar.setLargeIcon(icon);
    }

    public void setLargeIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setLargeIcon(locale, icon);
    }

    public void setSmallIcon(String icon) {
        if (ejbJar!=null) ejbJar.setSmallIcon(icon);
    }

    public void setSmallIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setSmallIcon(locale, icon);
    }

    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (ejbJar!=null) {
            writing=true;
            Schema2BeansUtil.write((BaseBean) ejbJar, os);
        }
    }

    public void write(FileObject fo) throws java.io.IOException {
        if (ejbJar != null) {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject instanceof DDProviderDataObject) {
                ((DDProviderDataObject) dataObject).writeModel(ejbJar);
            } else {
                FileLock lock = fo.lock();
                try {
                    OutputStream os = fo.getOutputStream(lock);
                    try {
                        writing = true;
                        write(os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
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
            if (EjbJar.VERSION_2_1.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.ejb.model_2_1.EjbJar)clonedEjbJar)._setSchemaLocation
                    ("http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd");
                clonedEjbJar.setVersion(new java.math.BigDecimal(EjbJar.VERSION_2_1));
            }
            else if (EjbJar.VERSION_3_0.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.ejb.model_3_0.EjbJar)clonedEjbJar)._setSchemaLocation
                    ("http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/ejb-jar_3_0.xsd");
                clonedEjbJar.setVersion(new java.math.BigDecimal(EjbJar.VERSION_3_0));
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

    public org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans getEnterpriseBeans() {
        return ejbJar==null?null:ejbJar.getEnterpriseBeans();
    }

    public org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor getSingleAssemblyDescriptor() {
        return ejbJar==null?null:ejbJar.getSingleAssemblyDescriptor();
    }

    public String getSingleEjbClientJar() {
        return ejbJar==null?null:ejbJar.getSingleEjbClientJar();
    }

    public org.netbeans.modules.j2ee.dd.api.ejb.Relationships getSingleRelationships() {
        return ejbJar==null?null:ejbJar.getSingleRelationships();
    }

    public void setAssemblyDescriptor(org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor value) {
        if (ejbJar!=null) ejbJar.setAssemblyDescriptor(value);
    }

    public void setEjbClientJar(String value) {
        if (ejbJar!=null) ejbJar.setEjbClientJar(value);
    }

    public void setEnterpriseBeans(org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans value) {
        if (ejbJar!=null) ejbJar.setEnterpriseBeans(value);
    }

    public void setRelationships(org.netbeans.modules.j2ee.dd.api.ejb.Relationships value) {
        if (ejbJar!=null) ejbJar.setRelationships(value);
    }

    public org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor newAssemblyDescriptor() {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.newAssemblyDescriptor();
    }

    public org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans newEnterpriseBeans() {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.newEnterpriseBeans();
    }

    public org.netbeans.modules.j2ee.dd.api.ejb.Relationships newRelationships() {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.newRelationships();
    }

    public void setInterceptors(Interceptors valueInterface) throws VersionNotSupportedException {
        if (ejbJar!=null) ejbJar.setInterceptors(valueInterface);
    }

    public Interceptors newInterceptors() throws VersionNotSupportedException {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.newInterceptors();
    }

    public Interceptors getInterceptors() throws VersionNotSupportedException {
        if(ejbJar == null)
            return null;
        else
            return ejbJar.getInterceptors();
    }
}
