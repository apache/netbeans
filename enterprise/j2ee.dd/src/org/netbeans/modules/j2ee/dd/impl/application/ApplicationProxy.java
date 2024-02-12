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

package org.netbeans.modules.j2ee.dd.impl.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import org.netbeans.modules.j2ee.dd.api.application.Application;

import java.beans.PropertyChangeListener;

/**
 * @author  Nitya Doraisamy
 */
public class ApplicationProxy implements Application {
    private Application app;
    private String version;
    private List<PropertyChangeListener> listeners;
    public boolean writing=false;
    private OutputProvider outputProvider;
    private org.xml.sax.SAXParseException error;
    private int ddStatus;

//    private static CommonDDAccess cDDA = new

    /** Creates a new instance of ApplicationProxy */
    public ApplicationProxy(Application app, String version) {
        this.app = app;
        this.version = version;
        listeners = new ArrayList<>();
    }

    public void setOriginal(Application app) {
        if (this.app != app) {
            for (int i=0;i<listeners.size();i++) {
                java.beans.PropertyChangeListener pcl = listeners.get(i);
                if (this.app != null) this.app.removePropertyChangeListener(pcl);
                if (app != null) app.addPropertyChangeListener(pcl);
                
            }
            this.app = app;
            if (app != null) setProxyVersion(app.getVersion().toString());
        }
    }
    
    public Application getOriginal() {
        return app;
    }
    
    public void setProxyVersion(java.lang.String value) {
        if ((version==null && value!=null) || (version != null && !version.equals(value))) {
            java.beans.PropertyChangeEvent evt = 
                new java.beans.PropertyChangeEvent(this, PROPERTY_VERSION, version, value);
            version=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
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

    public int addModule(org.netbeans.modules.j2ee.dd.api.application.Module value) {
        return app==null?-1:app.addModule(value);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (app != null) app.addPropertyChangeListener(pcl);
        listeners.add(pcl); 
    }

    public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        return app==null?-1:app.addSecurityRole(value);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return app==null?null:app.createBean(beanName);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return app==null?null:app.findBeanByName(beanName, propertyName, value);
    }

    @Override
    public Map<String, String> getAllDescriptions() {
        return app == null ? new HashMap<>() : app.getAllDescriptions();
    }

    public Map<String, String> getAllDisplayNames() {
        return app == null ? new HashMap<>() : app.getAllDisplayNames();
    }

    public Map<String, String[]> getAllIcons() {
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

    public org.netbeans.modules.j2ee.dd.api.application.Module[] getModule() {
        return app==null?null:app.getModule();
    }

    public org.netbeans.modules.j2ee.dd.api.application.Module getModule(int index) {
        return app==null?null:app.getModule(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole() {
        return app==null?null:app.getSecurityRole();
    }

    public org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int index) {
        return app==null?null:app.getSecurityRole(index);
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
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }
    
    public Object getValue(String propertyName) {
        return app==null?null:app.getValue(propertyName);
    }

    public BigDecimal getVersion() {
        return version == null ? BigDecimal.ZERO : new BigDecimal(version);
    }

    public void merge(org.netbeans.modules.j2ee.dd.api.common.RootInterface root, int mode) {
        if (app!=null) {
            if (root instanceof ApplicationProxy)
                app.merge(((ApplicationProxy)root).getOriginal(), mode);
            else app.merge(root, mode);
        }
    }

    public org.netbeans.modules.j2ee.dd.api.common.Icon newIcon() {
        return app==null?null:app.newIcon();
    }

    public org.netbeans.modules.j2ee.dd.api.application.Module newModule() {
        return app==null?null:app.newModule();
    }

    public org.netbeans.modules.j2ee.dd.api.common.SecurityRole newSecurityRole() {
        return app==null?null:app.newSecurityRole();
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

    public int removeModule(org.netbeans.modules.j2ee.dd.api.application.Module value) {
        return app==null?-1:app.removeModule(value);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (app != null) app.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        return app==null?-1:app.removeSecurityRole(value);
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

    public void setModule(org.netbeans.modules.j2ee.dd.api.application.Module[] value) {
        if (app != null) app.setModule(value);
    }

    public void setModule(int index, org.netbeans.modules.j2ee.dd.api.application.Module value) {
        if (app != null) app.setModule(index, value);
    }

    public void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] value) {
        if (app != null) app.setSecurityRole(value);
    }

    public void setSecurityRole(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        if (app != null) app.setSecurityRole(index, value);
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

    public int sizeModule() {
        return app==null?0:app.sizeModule();
    }

    public int sizeSecurityRole() {
        return app==null?0:app.sizeSecurityRole();
    }

    public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException {
        if (app != null) {
            try (org.openide.filesystems.FileLock lock = fo.lock();
                    java.io.OutputStream os = fo.getOutputStream(lock)) {
                writing=true;
                write(os);
            } catch (org.openide.filesystems.FileAlreadyLockedException ex) {
                // trying to use OutputProvider for writing changes
                org.openide.loaders.DataObject dobj = org.openide.loaders.DataObject.find(fo);
                if (dobj instanceof OutputProvider)
                    ((ApplicationProxy.OutputProvider)dobj).write(this);
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
        ApplicationProxy proxy = null;
        if (app == null)
            proxy = new ApplicationProxy(null,version);
        else {
            ApplicationProxy clonedApp=(ApplicationProxy)app.clone();
            proxy = new ApplicationProxy(clonedApp, version);
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
    
    /** Contract between friend modules that enables 
    * a specific handling of write(FileObject) method for targeted FileObject
    */
    public static interface OutputProvider {
        public void write(Application app) throws java.io.IOException;
        public org.openide.filesystems.FileObject getTarget();
    }
}
