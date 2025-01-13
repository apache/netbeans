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

package org.netbeans.modules.j2ee.dd.impl.client;

/**
 *
 * @author  Nitya Doraisamy
 */
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;

public class AppClientProxy implements AppClient {
    
    private AppClient app;
    private String version;
    private List<PropertyChangeListener> listeners;
    public boolean writing=false;
    private OutputProvider outputProvider;
    private org.xml.sax.SAXParseException error;
    private int ddStatus;
    
    private static final Logger LOGGER = Logger.getLogger(AppClientProxy.class.getName());
    
    /**
     * Creates a new instance of AppClientProxy
     *
     */
    public AppClientProxy(AppClient app, String version) {
        this.app = app;
        this.version = version;
        listeners = new ArrayList<>();
    }
    
    public void setOriginal(AppClient app) {
        if (this.app != app) {
            for (Iterator<PropertyChangeListener> i = listeners.iterator(); i.hasNext();) {
                PropertyChangeListener pcl = i.next();
                if (this.app != null) this.app.removePropertyChangeListener(pcl);
                if (app != null) app.addPropertyChangeListener(pcl);
                
            }
            this.app = app;
            if (app != null){
                String version = app.getVersion() != null ? app.getVersion().toString() : "";
                setProxyVersion(version);
            }
        }
    }
    
    public AppClient getOriginal() {
        return app;
    }
    
    public void setProxyVersion(java.lang.String value) {
        if ((version == null && value != null) || (version != null && !version.equals(value))) {
            java.beans.PropertyChangeEvent evt =
                    new java.beans.PropertyChangeEvent(this, PROPERTY_VERSION, version, value);
            version=value;
            for (Iterator<PropertyChangeListener> i = listeners.iterator(); i.hasNext();) {
                i.next().propertyChange(evt);
            }
        }
    }
    
    
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        return app==null?null:app.addBean(beanName);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException {
        return app==null?null:app.addBean(beanName, propertyNames, propertyValues, keyProperty);
    }
    
    public int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?-1:app.addIcon(value);
    }
    
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (app != null) app.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return app==null?null:app.createBean(beanName);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return app==null?null:app.findBeanByName(beanName, propertyName, value);
    }
    
    @Override
    public Map getAllDescriptions() {
        return app == null ? new HashMap<>() : app.getAllDescriptions();
    }
    
    @Override
    public Map<String, String> getAllDisplayNames() {
        return app == null ? new HashMap<>() :app.getAllDisplayNames();
    }
    
    @Override
    public Map<String, String[]>  getAllIcons() {
        return app == null ? new HashMap<>() : app.getAllIcons();
    }
    
    public String getDefaultDescription() {
        return app==null?null:app.getDefaultDescription();
    }
    
    public String getDefaultDisplayName() {
        return app==null?null:app.getDefaultDisplayName();
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon() {
        return app==null?null:app.getDefaultIcon();
    }
    
    public String getDescription(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getDescription(locale);
    }
    
    public String getDisplayName(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getDisplayName(locale);
    }
    
    public org.xml.sax.SAXParseException getError() {
        return error;
    }
    
    public void setError(org.xml.sax.SAXParseException error) {
        this.error=error;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int index) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getIcon(index);
    }
    
    public String getId() {
        return app==null?null:app.getId();
    }
    
    public String getLargeIcon() {
        return app==null?null:app.getLargeIcon();
    }
    
    public String getLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getLargeIcon(locale);
    }
    
    public String getSmallIcon() {
        return app==null?null:app.getSmallIcon();
    }
    
    public String getSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getSmallIcon(locale);
    }
    
    public int getStatus() {
        return ddStatus;
    }
    
    public void setStatus(int value) {
        if (ddStatus!=value) {
            java.beans.PropertyChangeEvent evt =
                    new java.beans.PropertyChangeEvent(this, PROPERTY_STATUS, ddStatus, value);
            ddStatus=value;
            for (Iterator<PropertyChangeListener> i = listeners.iterator(); i.hasNext();) {
                i.next().propertyChange(evt);
            }
        }
    }
    
    public Object getValue(String propertyName) {
        return app==null?null:app.getValue(propertyName);
    }
    
    /**
     * @return the dd version or null the version is not specified correctly,
     * i.e. it is null or not a number.
     */ 
    public BigDecimal getVersion() {
        if (version == null){
            return null;
        }
        try{
            return new BigDecimal(version);
        } catch (NumberFormatException nfe){
            LOGGER.log(Level.INFO, "Not a valid version: " + version, nfe);//NO18N
            return null;
        }
    }
    
    public void merge(org.netbeans.modules.j2ee.dd.api.common.RootInterface root, int mode) {
        if (app!=null) {
            if (root instanceof AppClientProxy)
                app.merge(((AppClientProxy)root).getOriginal(), mode);
            else app.merge(root, mode);
        }
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.Icon newIcon() {
        try {
            return app==null?null:app.newIcon();
        } catch (VersionNotSupportedException vnse) {
            return null;
        }
    }
    
    public void removeAllDescriptions() {
        if (app!=null) app.removeAllDescriptions();
    }
    
    public void removeAllDisplayNames() {
        if (app!=null) app.removeAllDisplayNames();
    }
    
    public void removeAllIcons() {
        if (app!=null) app.removeAllIcons();
    }
    
    public void removeDescription() {
        if (app!=null) app.removeDescription();
    }
    
    public void removeDescriptionForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.removeDescriptionForLocale(locale);
    }
    
    public void removeDisplayName() {
        if (app!=null) app.removeDisplayName();
    }
    
    public void removeDisplayNameForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.removeDisplayNameForLocale(locale);
    }
    
    public void removeIcon() {
        if (app!=null) app.removeIcon();
    }
    
    public int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?-1:app.removeIcon(value);
    }
    
    public void removeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.removeIcon(locale);
    }
    
    public void removeLargeIcon() {
        if (app!=null) app.removeLargeIcon();
    }
    
    public void removeLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.removeLargeIcon(locale);
    }
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (app != null) app.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }
    
    public void removeSmallIcon() {
        if (app != null) app.removeSmallIcon();
    }
    
    public void removeSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.removeSmallIcon(locale);
    }
    
    public void setAllDescriptions(java.util.Map descriptions) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setAllDescriptions(descriptions);
    }
    
    public void setAllDisplayNames(java.util.Map displayNames) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setAllDisplayNames(displayNames);
    }
    
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setAllIcons(locales, smallIcons, largeIcons);
    }
    
    public void setDescription(String description) {
        if (app != null) app.setDescription(description);
    }
    
    public void setDescription(String locale, String description) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setDescription(locale, description);
    }
    
    public void setDisplayName(String displayName) {
        if (app != null) app.setDisplayName(displayName);
    }
    
    public void setDisplayName(String locale, String displayName) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setDisplayName(locale, displayName);
    }
    
    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[] value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setIcon(value);
    }
    
    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon) {
        if (app != null) app.setIcon(icon);
    }
    
    public void setIcon(int index, org.netbeans.modules.j2ee.dd.api.common.Icon value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setIcon(index, value);
    }
    
    public void setId(String value) {
        if (app != null) app.setId(value);
    }
    
    public void setLargeIcon(String icon) {
        if (app != null) app.setLargeIcon(icon);
    }
    
    public void setLargeIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setLargeIcon(locale, icon);
    }
    
    public void setSmallIcon(String icon) {
        if (app != null) app.setSmallIcon(icon);
    }
    
    public void setSmallIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setSmallIcon(locale, icon);
    }
    
    public int sizeIcon() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?0:app.sizeIcon();
    }
    
    public void write(FileObject fo) throws java.io.IOException {
        if (app != null) {
            try (org.openide.filesystems.FileLock lock = fo.lock();
                    java.io.OutputStream os = fo.getOutputStream(lock)) {
                writing=true;
                write(os);
            } catch (FileAlreadyLockedException ex) {
                // trying to use OutputProvider for writing changes
                org.openide.loaders.DataObject dobj = org.openide.loaders.DataObject.find(fo);
                if (dobj instanceof OutputProvider)
                    ((AppClientProxy.OutputProvider)dobj).write(this);
                else throw ex;
            }
        }
    }
    
    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (app != null) {
            writing=true;
            app.write(os);
        }
    }
    
    @Override
    public Object clone() {
        AppClientProxy proxy = null;
        if (app == null)
            proxy = new AppClientProxy(null,version);
        else {
            AppClientProxy clonedApp=(AppClientProxy)app.clone();
            proxy = new AppClientProxy(clonedApp, version);
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
    
    public void setOutputProvider(OutputProvider iop) {
        this.outputProvider=iop;
    }
    
    public int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef value) {
        return app==null?-1:app.addEjbRef(value);
    }
    
    public int addEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry value) {
        return app==null?-1:app.addEnvEntry(value);
    }
    
    public int addMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?-1:app.addMessageDestination(value);
    }
    
    public int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef value) throws VersionNotSupportedException {
        return app==null?-1:app.addMessageDestinationRef(value);
    }
    
    public int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef value) {
        return app==null?-1:app.addResourceEnvRef(value);
    }
    
    public int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef value) {
        return app==null?-1:app.addResourceRef(value);
    }
    
    public int addServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?-1:app.addServiceRef(value);
    }
    
    public String getCallbackHandler() {
        return (app != null) ? app.getCallbackHandler() : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef() {
        return (app != null) ? app.getEjbRef() : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int index) {
        return (app != null) ? app.getEjbRef(index) : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] getEnvEntry() {
        return app==null?new org.netbeans.modules.j2ee.dd.api.common.EnvEntry[0]:app.getEnvEntry();
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.EnvEntry getEnvEntry(int index) {
        return app==null?null:app.getEnvEntry(index);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] getMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?new org.netbeans.modules.j2ee.dd.api.common.MessageDestination[0]:app.getMessageDestination();
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.MessageDestination getMessageDestination(int index) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getMessageDestination(index);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?new org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[0]:app.getMessageDestinationRef();
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef getMessageDestinationRef(int index) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getMessageDestinationRef(index);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] getResourceEnvRef() {
        return app==null?null:app==null?new org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[0]:app.getResourceEnvRef();
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef getResourceEnvRef(int index) {
        return app==null?null:app.getResourceEnvRef(index);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef() {
        return app==null?new org.netbeans.modules.j2ee.dd.api.common.ResourceRef[0]:app.getResourceRef();
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int index) {
        return app==null?null:app.getResourceRef(index);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] getServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?new org.netbeans.modules.j2ee.dd.api.common.ServiceRef[0]:app.getServiceRef();
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ServiceRef getServiceRef(int index) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getServiceRef(index);
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.EjbRef newEjbRef() {
        return (app != null) ? app.newEjbRef() : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.EnvEntry newEnvEntry() {
        return (app != null) ? app.newEnvEntry() : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.MessageDestination newMessageDestination() throws VersionNotSupportedException {
        return (app != null) ? app.newMessageDestination() : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException {
        return (app != null) ? app.newMessageDestinationRef() : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef newResourceEnvRef() {
        return (app != null) ? app.newResourceEnvRef() : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ResourceRef newResourceRef() {
        return (app != null) ? app.newResourceRef() : null;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.ServiceRef newServiceRef() throws VersionNotSupportedException {
        return (app != null) ? app.newServiceRef() : null;
    }
    
    public int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef value) {
        return app==null?-1:app.removeEjbRef(value);
    }
    
    public int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry value) {
        return app==null?-1:app.removeEnvEntry(value);
    }
    
    public int removeMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination value) throws VersionNotSupportedException {
        return app==null?-1:app.removeMessageDestination(value);
    }
    
    public int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef value) throws VersionNotSupportedException {
        return app==null?-1:app.removeMessageDestinationRef(value);
    }
    
    public int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef value) {
        return app==null?-1:app.removeResourceEnvRef(value);
    }
    
    public int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef value) {
        return app==null?-1:app.removeResourceRef(value);
    }
    
    public int removeServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef value) throws VersionNotSupportedException {
        return app==null?-1:app.removeServiceRef(value);
    }
    
    public void setVersion(java.math.BigDecimal version) {
        if (app != null) app.setVersion(version);
    }
    
    public void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[] value) {
        if (app != null) { app.setEjbRef(value); }
    }
    
    public void setResourceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceRef value) {
        if (app!=null) app.setResourceRef(index, value);
    }
    
    public void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] value) {
        if (app!=null) app.setResourceEnvRef(value);
    }
    
    public void setServiceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ServiceRef value) throws VersionNotSupportedException {
        if (app!=null) app.setServiceRef(index, value);
    }
    
    public void setMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] value) throws VersionNotSupportedException {
        if (app!=null) app.setMessageDestination(value);
    }
    
    public void setResourceEnvRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef value) {
        if (app!=null) app.setResourceEnvRef(index, value);
    }
    
    public void setEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] value) {
        if (app!=null) app.setEnvEntry(value);
    }
    
    public void setMessageDestination(int index, org.netbeans.modules.j2ee.dd.api.common.MessageDestination value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.setMessageDestination(index, value);
    }
    
    public void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] value) throws VersionNotSupportedException {
        if (app!=null) app.setMessageDestinationRef(value);
    }
    
    public void setMessageDestinationRef(int index, org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef value) throws VersionNotSupportedException {
        if (app!=null) app.setMessageDestinationRef(index, value);
    }
    
    public void setEnvEntry(int index, org.netbeans.modules.j2ee.dd.api.common.EnvEntry value) {
        if (app!=null) app.setEnvEntry(index, value);
    }
    
    public void setEjbRef(int index, org.netbeans.modules.j2ee.dd.api.common.EjbRef value) {
        if (app != null) app.setEjbRef(index, value);
    }
    
    public void setCallbackHandler(String value) {
        if (app!=null) app.setCallbackHandler(value);
    }
    
    public void setServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] value) throws VersionNotSupportedException {
        if (app!=null) app.setServiceRef(value);
    }
    
    public void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] value) {
        if (app!=null) app.setResourceRef(value);
    }
    
    public int sizeEjbRef() {
        return (app != null) ? app.sizeEjbRef() : 0;
    }
    
    public int sizeEnvEntry() {
        return app==null?0:app.sizeEnvEntry();
    }
    
    public int sizeMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?0:app.sizeMessageDestination();
    }
    
    public int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?0:app.sizeMessageDestinationRef();
    }
    
    public int sizeResourceEnvRef() {
        return app==null?0:app.sizeResourceEnvRef();
    }
    
    public int sizeResourceRef() {
        return app==null?0:app.sizeResourceRef();
    }
    
    public int sizeServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?0:app.sizeServiceRef();
    }
    
    /** Contract between friend modules that enables
     * a specific handling of write(FileObject) method for targeted FileObject
     */
    public static interface OutputProvider {
        public void write(AppClient app) throws java.io.IOException;
        public FileObject getTarget();
    }
}
