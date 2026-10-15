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

package org.netbeans.modules.j2ee.dd.impl.web;

import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.AbsoluteOrdering;
import org.netbeans.modules.j2ee.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.schema2beans.Schema2BeansUtil;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileLock;

import java.beans.PropertyChangeListener;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author  mk115033
 */
public class WebAppProxy implements WebApp {
    private WebApp webApp;
    private String version;
    private final List<PropertyChangeListener> listeners;
    public boolean writing=false;
    private org.xml.sax.SAXParseException error;
    private int ddStatus;
    private Schema2BeansUtil.ReindentationListener reindentationListener = new Schema2BeansUtil.ReindentationListener();

    /** Creates a new instance of WebAppProxy */
    public WebAppProxy(WebApp webApp, String version) {
        this.webApp=webApp;
        this.version = version;
        listeners = new ArrayList<>();
        addPropertyChangeListener(reindentationListener);
    }

    public void setOriginal(WebApp webApp) {
        if (this.webApp!=webApp) {
            for (int i=0;i<listeners.size();i++) {
                java.beans.PropertyChangeListener pcl = listeners.get(i);
                if (this.webApp!=null) this.webApp.removePropertyChangeListener(pcl);
                if (webApp!=null) webApp.addPropertyChangeListener(pcl);

            }
            this.webApp=webApp;
            if (webApp!=null) setProxyVersion(webApp.getVersion());
        }
    }

    public WebApp getOriginal() {
        return webApp;
    }

    public void setProxyVersion(java.lang.String value) {
        if ((version == null && value != null)
            || (version != null && value != null && !version.equals(value))) {
            java.beans.PropertyChangeEvent evt =
                new java.beans.PropertyChangeEvent(this, PROPERTY_VERSION, version, value);
            version=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }
    /*
    public void setVersion(java.lang.String value) {
    }
    */
    public java.lang.String getVersion() {
        return version;
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
                new java.beans.PropertyChangeEvent(this, PROPERTY_STATUS, ddStatus, value);
            ddStatus=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (webApp!=null) webApp.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (webApp!=null) webApp.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public int addContextParam(org.netbeans.modules.j2ee.dd.api.common.InitParam value) {
        return webApp==null?-1:webApp.addContextParam(value);
    }

    public int addEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef value) {
        return webApp==null?-1:webApp.addEjbLocalRef(value);
    }

    public int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef value) {
        return webApp==null?-1:webApp.addEjbRef(value);
    }

    public int addEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry value) {
        return webApp==null?-1:webApp.addEnvEntry(value);
    }

    public int addErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage value) {
        return webApp==null?-1:webApp.addErrorPage(value);
    }

    public int addFilter(org.netbeans.modules.j2ee.dd.api.web.Filter value) {
        return webApp==null?-1:webApp.addFilter(value);
    }

    public int addFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping value) {
        return webApp==null?-1:webApp.addFilterMapping(value);
    }

    public int addListener(org.netbeans.modules.j2ee.dd.api.web.Listener value) {
        return webApp==null?-1:webApp.addListener(value);
    }

    public int addMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?-1:webApp.addMessageDestination(value);
    }

    public int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?-1:webApp.addMessageDestinationRef(value);
    }

    public int addMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping value) {
        return webApp==null?-1:webApp.addMimeMapping(value);
    }

    public int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef value) {
        return webApp==null?-1:webApp.addResourceEnvRef(value);
    }

    public int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef value) {
        return webApp==null?-1:webApp.addResourceRef(value);
    }

    public int addSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint value) {
        return webApp==null?-1:webApp.addSecurityConstraint(value);
    }

    public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        return webApp==null?-1:webApp.addSecurityRole(value);
    }

    public int addServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?-1:webApp.addServiceRef(value);
    }

    public int addServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet value) {
        return webApp==null?-1:webApp.addServlet(value);
    }

    public int addServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping value) {
        return webApp==null?-1:webApp.addServletMapping(value);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return webApp==null?null:webApp.createBean(beanName);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException {
        return webApp==null?null:webApp.addBean(beanName, propertyNames, propertyValues, keyProperty);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        return webApp==null?null:webApp.addBean(beanName);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return webApp==null?null:webApp.findBeanByName(beanName, propertyName, value);
    }

    public java.util.Map getAllDescriptions() {
        return webApp==null?new java.util.HashMap():webApp.getAllDescriptions();
    }

    public java.util.Map getAllDisplayNames() {
        return webApp==null?new java.util.HashMap():webApp.getAllDisplayNames();
    }

    public java.util.Map getAllIcons() {
        return webApp==null?new java.util.HashMap():webApp.getAllIcons();
    }

    public org.netbeans.modules.j2ee.dd.api.common.InitParam[] getContextParam() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.InitParam[0]:webApp.getContextParam();
    }

    public org.netbeans.modules.j2ee.dd.api.common.InitParam getContextParam(int index) {
        return webApp==null?null:webApp.getContextParam(index);
    }

    public String getDefaultDescription() {
        return webApp==null?null:webApp.getDefaultDescription();
    }

    public String getDefaultDisplayName() {
        return webApp==null?null:webApp.getDefaultDisplayName();
    }

    public org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon() {
        return webApp==null?null:webApp.getDefaultIcon();
    }

    public String getDescription(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getDescription(locale);
    }

    public String getDisplayName(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getDisplayName(locale);
    }

    public org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[] getEjbLocalRef() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[0]:webApp.getEjbLocalRef();
    }

    public org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef getEjbLocalRef(int index) {
        return webApp==null?null:webApp.getEjbLocalRef(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.EjbRef[0]:webApp.getEjbRef();
    }

    public org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int index) {
        return webApp==null?null:webApp.getEjbRef(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] getEnvEntry() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.EnvEntry[0]:webApp.getEnvEntry();
    }

    public org.netbeans.modules.j2ee.dd.api.common.EnvEntry getEnvEntry(int index) {
        return webApp==null?null:webApp.getEnvEntry(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.ErrorPage[] getErrorPage() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.web.ErrorPage[0]:webApp.getErrorPage();
    }

    public org.netbeans.modules.j2ee.dd.api.web.ErrorPage getErrorPage(int index) {
        return webApp==null?null:webApp.getErrorPage(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.Filter[] getFilter() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.web.Filter[0]:webApp.getFilter();
    }

    public org.netbeans.modules.j2ee.dd.api.web.Filter getFilter(int index) {
        return webApp==null?null:webApp.getFilter(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.FilterMapping[] getFilterMapping() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.web.FilterMapping[0]:webApp.getFilterMapping();
    }

    public org.netbeans.modules.j2ee.dd.api.web.FilterMapping getFilterMapping(int index) {
        return webApp==null?null:webApp.getFilterMapping(index);
    }

    public java.lang.String getId() {
        return webApp==null?null:webApp.getId();
    }

    public String getLargeIcon() {
        return webApp==null?null:webApp.getLargeIcon();
    }

    public String getLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getLargeIcon(locale);
    }

    public org.netbeans.modules.j2ee.dd.api.web.Listener[] getListener() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.web.Listener[0]:webApp.getListener();
    }

    public org.netbeans.modules.j2ee.dd.api.web.Listener getListener(int index) {
        return webApp==null?null:webApp.getListener(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMappingList getSingleLocaleEncodingMappingList() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getSingleLocaleEncodingMappingList();
    }

    public org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] getMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.MessageDestination[0]:webApp.getMessageDestination();
    }

    public org.netbeans.modules.j2ee.dd.api.common.MessageDestination getMessageDestination(int index) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getMessageDestination(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[0]:webApp.getMessageDestinationRef();
    }

    public org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef getMessageDestinationRef(int index) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getMessageDestinationRef(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.MimeMapping[] getMimeMapping() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.web.MimeMapping[0]:webApp.getMimeMapping();
    }

    public org.netbeans.modules.j2ee.dd.api.web.MimeMapping getMimeMapping(int index) {
        return webApp==null?null:webApp.getMimeMapping(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] getResourceEnvRef() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[0]:webApp.getResourceEnvRef();
    }

    public org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef getResourceEnvRef(int index) {
        return webApp==null?null:webApp.getResourceEnvRef(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.ResourceRef[0]:webApp.getResourceRef();
    }

    public org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int index) {
        return webApp==null?null:webApp.getResourceRef(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint[] getSecurityConstraint() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint[0]:webApp.getSecurityConstraint();
    }

    public org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint getSecurityConstraint(int index) {
        return webApp==null?null:webApp.getSecurityConstraint(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.SecurityRole[0]:webApp.getSecurityRole();
    }

    public org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int index) {
        return webApp==null?null:webApp.getSecurityRole(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] getServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.common.ServiceRef[0]:webApp.getServiceRef();
    }

    public org.netbeans.modules.j2ee.dd.api.common.ServiceRef getServiceRef(int index) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getServiceRef(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.Servlet[] getServlet() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.web.Servlet[0]:webApp.getServlet();
    }

    public org.netbeans.modules.j2ee.dd.api.web.Servlet getServlet(int index) {
        return webApp==null?null:webApp.getServlet(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.ServletMapping[] getServletMapping() {
        return webApp==null?new org.netbeans.modules.j2ee.dd.api.web.ServletMapping[0]:webApp.getServletMapping();
    }

    public org.netbeans.modules.j2ee.dd.api.web.ServletMapping getServletMapping(int index) {
        return webApp==null?null:webApp.getServletMapping(index);
    }

    public org.netbeans.modules.j2ee.dd.api.web.JspConfig getSingleJspConfig() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getSingleJspConfig();
    }

    public org.netbeans.modules.j2ee.dd.api.web.LoginConfig getSingleLoginConfig() {
        return webApp==null?null:webApp.getSingleLoginConfig();
    }

    public org.netbeans.modules.j2ee.dd.api.web.SessionConfig getSingleSessionConfig() {
        return webApp==null?null:webApp.getSingleSessionConfig();
    }

    public org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList getSingleWelcomeFileList() {
        return webApp==null?null:webApp.getSingleWelcomeFileList();
    }

    public String getSmallIcon() {
        return webApp==null?null:webApp.getSmallIcon();
    }

    public String getSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?null:webApp.getSmallIcon(locale);
    }

    public Object getValue(String name) {
        return webApp==null?null:webApp.getValue(name);
    }

    public boolean isDistributable() {
        return webApp==null?false:webApp.isDistributable();
    }

    public void merge(org.netbeans.modules.j2ee.dd.api.common.RootInterface bean, int mode) {
        if (bean instanceof WebAppProxy) {
            bean = ((WebAppProxy) bean).getOriginal();
        }
        if (webApp != bean && bean instanceof WebApp) {
            WebApp newWebApp = (WebApp) bean;
            if (webApp != null && webApp.getVersion().equals(newWebApp.getVersion())) {
                removePropertyChangeListener(reindentationListener);
                webApp.merge(newWebApp, mode);
                addPropertyChangeListener(reindentationListener);
            } else if (webApp == null) {
                removePropertyChangeListener(reindentationListener);
                setOriginal((WebApp) newWebApp.clone());
                addPropertyChangeListener(reindentationListener);
            } else {
                setOriginal((WebApp) newWebApp.clone());
            }
        }
    }

    public void removeAllDescriptions() {

        if (webApp!=null) webApp.removeAllDescriptions();
    }

    public void removeAllDisplayNames() {
        if (webApp!=null) webApp.removeAllDisplayNames();
    }

    public void removeAllIcons() {
        if (webApp!=null) webApp.removeAllIcons();
    }

    public int removeContextParam(org.netbeans.modules.j2ee.dd.api.common.InitParam value) {
        return webApp==null?-1:webApp.removeContextParam(value);
    }

    public void removeDescription() {
        if (webApp!=null) webApp.removeDescription();
    }

    public void removeDescriptionForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.removeDescriptionForLocale(locale);
    }

    public void removeDisplayName() {
        if (webApp!=null) webApp.removeDisplayName();
    }

    public void removeDisplayNameForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.removeDisplayNameForLocale(locale);
    }

    public int removeEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef value) {
        return webApp==null?-1:webApp.removeEjbLocalRef(value);
    }

    public int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef value) {
        return webApp==null?-1:webApp.removeEjbRef(value);
    }

    public int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry value) {
        return webApp==null?-1:webApp.removeEnvEntry(value);
    }

    public int removeErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage value) {
        return webApp==null?-1:webApp.removeErrorPage(value);
    }

    public int removeFilter(org.netbeans.modules.j2ee.dd.api.web.Filter value) {
        return webApp==null?-1:webApp.removeFilter(value);
    }

    public int removeFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping value) {
        return webApp==null?-1:webApp.removeFilterMapping(value);
    }

    public void removeIcon() {
        if (webApp!=null) webApp.removeIcon();
    }

    public void removeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.removeIcon(locale);
    }

    public void removeLargeIcon() {
        if (webApp!=null) webApp.removeLargeIcon();
    }

    public void removeLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.removeLargeIcon(locale);
    }

    public int removeListener(org.netbeans.modules.j2ee.dd.api.web.Listener value) {
        return webApp==null?-1:webApp.removeListener(value);
    }

    public int removeMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?-1:webApp.removeMessageDestination(value);
    }

    public int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?-1:webApp.removeMessageDestinationRef(value);
    }

    public int removeMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping value) {
        return webApp==null?-1:webApp.removeMimeMapping(value);
    }

    public int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef value) {
        return webApp==null?-1:webApp.removeResourceEnvRef(value);
    }

    public int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef value) {
        return webApp==null?-1:webApp.removeResourceRef(value);
    }

    public int removeSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint value) {
        return webApp==null?-1:webApp.removeSecurityConstraint(value);
    }

    public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        return webApp==null?-1:webApp.removeSecurityRole(value);
    }

    public int removeServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?-1:webApp.removeServiceRef(value);
    }

    public int removeServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet value) {
        return webApp==null?-1:webApp.removeServlet(value);
    }

    public int removeServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping value) {
        return webApp==null?-1:webApp.removeServletMapping(value);
    }

    public void removeSmallIcon() {
        if (webApp!=null) webApp.removeSmallIcon();
    }

    public void removeSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.removeSmallIcon(locale);
    }

    public void setAllDescriptions(java.util.Map descriptions) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setAllDescriptions(descriptions);
    }

    public void setAllDisplayNames(java.util.Map displayNames) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setAllDisplayNames(displayNames);
    }

    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setAllIcons(locales, smallIcons, largeIcons);
    }

    public void setContextParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[] value) {
        if (webApp!=null) webApp.setContextParam(value);
    }

    public void setContextParam(int index, org.netbeans.modules.j2ee.dd.api.common.InitParam value) {
        if (webApp!=null) webApp.setContextParam(index, value);
    }

    public void setDescription(String description) {
        if (webApp!=null) webApp.setDescription(description);
    }

    public void setDescription(String locale, String description) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setDescription(locale, description);
    }

    public void setDisplayName(String displayName) {
        if (webApp!=null) webApp.setDisplayName(displayName);
    }

    public void setDisplayName(String locale, String displayName) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setDisplayName(locale, displayName);
    }

    public void setDistributable(boolean value) {
        if (webApp!=null) webApp.setDistributable(value);
    }

    public void setEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[] value) {
        if (webApp!=null) webApp.setEjbLocalRef(value);
    }

    public void setEjbLocalRef(int index, org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef value) {
        if (webApp!=null) webApp.setEjbLocalRef(index, value);
    }

    public void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[] value) {
        if (webApp!=null) webApp.setEjbRef(value);
    }

    public void setEjbRef(int index, org.netbeans.modules.j2ee.dd.api.common.EjbRef value) {
        if (webApp!=null) webApp.setEjbRef(index, value);
    }

    public void setEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] value) {
        if (webApp!=null) webApp.setEnvEntry(value);
    }

    public void setEnvEntry(int index, org.netbeans.modules.j2ee.dd.api.common.EnvEntry value) {
        if (webApp!=null) webApp.setEnvEntry(index, value);
    }

    public void setErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage[] value) {
        if (webApp!=null) webApp.setErrorPage(value);
    }

    public void setErrorPage(int index, org.netbeans.modules.j2ee.dd.api.web.ErrorPage value) {
        if (webApp!=null) webApp.setErrorPage(index, value);
    }

    public void setFilter(org.netbeans.modules.j2ee.dd.api.web.Filter[] value) {
        if (webApp!=null) webApp.setFilter(value);
    }

    public void setFilter(int index, org.netbeans.modules.j2ee.dd.api.web.Filter value) {
        if (webApp!=null) webApp.setFilter(index, value);
    }

    public void setFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping[] value) {
        if (webApp!=null) {
        org.netbeans.modules.j2ee.dd.api.web.FilterMapping[] oldMappings = getFilterMapping();
        int lenOld = oldMappings.length;
        int lenNew = (value==null?0:value.length);
        if (lenOld<=lenNew) {
            for (int i=0;i<lenOld;i++) {
                webApp.setFilterMapping(i,value[i]);
            }
            for (int i=lenOld;i<lenNew;i++) {
                webApp.addFilterMapping(value[i]);
            }
        } else {
            for (int i=0;i<lenNew;i++) {
                webApp.setFilterMapping(i,value[i]);
            }
            for (int i=lenOld-1;i>=lenNew;i--) {
                webApp.removeFilterMapping(oldMappings[i]);
            }
        }
        }
    }

    public void setFilterMapping(int index, org.netbeans.modules.j2ee.dd.api.web.FilterMapping value) {
        if (webApp!=null) webApp.setFilterMapping(index, value);
    }

    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon) {
        if (webApp!=null) webApp.setIcon(icon);
    }

    public void setId(java.lang.String value) {
        if (webApp!=null) webApp.setId(value);
    }

    public void setJspConfig(org.netbeans.modules.j2ee.dd.api.web.JspConfig value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setJspConfig(value);
    }

    public void setLargeIcon(String icon) {
        if (webApp!=null) webApp.setLargeIcon(icon);
    }

    public void setLargeIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setLargeIcon(locale, icon);
    }

    public void setListener(org.netbeans.modules.j2ee.dd.api.web.Listener[] value) {
        if (webApp!=null) {
        org.netbeans.modules.j2ee.dd.api.web.Listener[] oldListeners = getListener();
        int lenOld = oldListeners.length;
        int lenNew = (value==null?0:value.length);
        if (lenOld<=lenNew) {
            for (int i=0;i<lenOld;i++) {
                webApp.setListener(i,value[i]);
            }
            for (int i=lenOld;i<lenNew;i++) {
                webApp.addListener(value[i]);
            }
        } else {
            for (int i=0;i<lenNew;i++) {
                webApp.setListener(i,value[i]);
            }
            for (int i=lenOld-1;i>=lenNew;i--) {
                webApp.removeListener(oldListeners[i]);
            }
        }
        }
    }

    public void setListener(int index, org.netbeans.modules.j2ee.dd.api.web.Listener value) {
        if (webApp!=null) webApp.setListener(index, value);
    }

    public void setLocaleEncodingMappingList(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMappingList value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setLocaleEncodingMappingList(value);
    }

    public void setLoginConfig(org.netbeans.modules.j2ee.dd.api.web.LoginConfig value) {
        if (webApp!=null) webApp.setLoginConfig(value);
    }

    public void setMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setMessageDestination(value);
    }

    public void setMessageDestination(int index, org.netbeans.modules.j2ee.dd.api.common.MessageDestination value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setMessageDestination(index, value);
    }

    public void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setMessageDestinationRef(value);
    }

    public void setMessageDestinationRef(int index, org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setMessageDestinationRef(index, value);
    }

    public void setMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping[] value) {
        if (webApp!=null) webApp.setMimeMapping(value);
    }

    public void setMimeMapping(int index, org.netbeans.modules.j2ee.dd.api.web.MimeMapping value) {
        if (webApp!=null) webApp.setMimeMapping(index, value);
    }

    public void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] value) {
        if (webApp!=null) webApp.setResourceEnvRef(value);
    }

    public void setResourceEnvRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef value) {
        if (webApp!=null) webApp.setResourceEnvRef(index, value);
    }

    public void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] value) {
        if (webApp!=null) webApp.setResourceRef(value);
    }

    public void setResourceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceRef value) {
        if (webApp!=null) webApp.setResourceRef(index, value);
    }

    public void setSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint[] value) {
        if (webApp!=null) webApp.setSecurityConstraint(value);
    }

    public void setSecurityConstraint(int index, org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint value) {
        if (webApp!=null) webApp.setSecurityConstraint(index, value);
    }

    public void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] value) {
        if (webApp!=null) webApp.setSecurityRole(value);
    }

    public void setSecurityRole(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        if (webApp!=null) webApp.setSecurityRole(index, value);
    }

    public void setServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setServiceRef(value);
    }

    public void setServiceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ServiceRef value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setServiceRef(index, value);
    }

    public void setServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet[] value) {
        if (webApp!=null) webApp.setServlet(value);
    }

    public void setServlet(int index, org.netbeans.modules.j2ee.dd.api.web.Servlet value) {
        if (webApp!=null) webApp.setServlet(index, value);
    }

    public void setServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping[] value) {
        if (webApp!=null) webApp.setServletMapping(value);
    }

    public void setServletMapping(int index, org.netbeans.modules.j2ee.dd.api.web.ServletMapping value) {
        if (webApp!=null) webApp.setServletMapping(index, value);
    }

    public void setSessionConfig(org.netbeans.modules.j2ee.dd.api.web.SessionConfig value) {
        if (webApp!=null) webApp.setSessionConfig(value);
    }

    public void setSmallIcon(String icon) {
        if (webApp!=null) webApp.setSmallIcon(icon);
    }

    public void setSmallIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setSmallIcon(locale, icon);
    }

    public void setWelcomeFileList(org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList value) {
        if (webApp!=null) webApp.setWelcomeFileList(value);
    }

    public int sizeContextParam() {
        return webApp==null?0:webApp.sizeContextParam();
    }

    public int sizeEjbLocalRef() {
        return webApp==null?0:webApp.sizeEjbLocalRef();
    }

    public int sizeEjbRef() {
        return webApp==null?0:webApp.sizeEjbRef();
    }

    public int sizeEnvEntry() {
        return webApp==null?0:webApp.sizeEnvEntry();
    }

    public int sizeErrorPage() {
        return webApp==null?0:webApp.sizeErrorPage();
    }

    public int sizeFilter() {
        return webApp==null?0:webApp.sizeFilter();
    }

    public int sizeFilterMapping() {
        return webApp==null?0:webApp.sizeFilterMapping();
    }

    public int sizeListener() {
        return webApp==null?0:webApp.sizeListener();
    }

    public int sizeMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?0:webApp.sizeMessageDestination();
    }

    public int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?0:webApp.sizeMessageDestinationRef();
    }

    public int sizeMimeMapping() {
        return webApp==null?0:webApp.sizeMimeMapping();
    }

    public int sizeResourceEnvRef() {
        return webApp==null?0:webApp.sizeResourceEnvRef();
    }

    public int sizeResourceRef() {
        return webApp==null?0:webApp.sizeResourceRef();
    }

    public int sizeSecurityConstraint() {
        return webApp==null?0:webApp.sizeSecurityConstraint();
    }

    public int sizeSecurityRole() {
        return webApp==null?0:webApp.sizeSecurityRole();
    }

    public int sizeServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return webApp==null?0:webApp.sizeServiceRef();
    }

    public int sizeServlet() {
        return webApp==null?0:webApp.sizeServlet();
    }

    public int sizeServletMapping() {
        return webApp==null?0:webApp.sizeServletMapping();
    }

    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (webApp!=null) {
            writing=true;
            BaseBean webAppl = (BaseBean)webApp;
            webAppl.reindent("    ");
            Schema2BeansUtil.write(webAppl, os);
        }
    }

    public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException {
        if (webApp!=null) {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject instanceof DDProviderDataObject) {
                ((DDProviderDataObject) dataObject).writeModel(webApp);
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
        WebAppProxy proxy = null;
        if (webApp==null)
            proxy = new WebAppProxy(null,version);
        else {
            WebApp clonedWebApp=(WebApp)webApp.clone();
            proxy = new WebAppProxy(clonedWebApp,version);
            if (WebApp.VERSION_2_4.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.web.model_2_4.WebApp)clonedWebApp)._setSchemaLocation
                    ("http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd");
            } else if (WebApp.VERSION_2_5.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.web.model_2_5.WebApp)clonedWebApp)._setSchemaLocation
                    ("http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd");
            } else if (WebApp.VERSION_3_0.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.web.model_3_0.WebApp)clonedWebApp)._setSchemaLocation
                    ("http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd");
            } else if (WebApp.VERSION_3_1.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.web.model_3_1.WebApp)clonedWebApp)._setSchemaLocation
                    ("http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd");
            } else if (WebApp.VERSION_4_0.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.web.model_4_0.WebApp)clonedWebApp)._setSchemaLocation
                    ("http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd");
            } else if (WebApp.VERSION_5_0.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.web.model_5_0.WebApp)clonedWebApp)._setSchemaLocation
                    ("https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd");
            } else if (WebApp.VERSION_6_0.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.web.model_6_0.WebApp)clonedWebApp)._setSchemaLocation
                    ("https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd");
            } else if (WebApp.VERSION_6_1.equals(version)) {
                ((org.netbeans.modules.j2ee.dd.impl.web.model_6_1.WebApp)clonedWebApp)._setSchemaLocation
                    ("https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_1.xsd");
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

    public void setMetadataComplete(boolean value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (webApp!=null) webApp.setMetadataComplete(value);
    }

    public boolean isMetadataComplete() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException{
        return webApp==null?false:webApp.isMetadataComplete();
    }

    public int addJspConfig(JspConfig valueInterface) throws VersionNotSupportedException {
        return webApp == null ? 0 : webApp.addJspConfig(valueInterface);
    }

    public int removeJspConfig(JspConfig valueInterface) throws VersionNotSupportedException {
        return webApp == null ? 0 : webApp.removeJspConfig(valueInterface);
    }

    public void setName(String[] value) throws VersionNotSupportedException {
        if (webApp != null) webApp.setName(value);
    }

    public String[] getName() throws VersionNotSupportedException {
        return webApp != null ? webApp.getName() : null;
    }

    public AbsoluteOrdering newAbsoluteOrdering() throws VersionNotSupportedException {
        return webApp != null ? webApp.newAbsoluteOrdering() : null;
    }

    public void setAbsoluteOrdering(AbsoluteOrdering[] value) throws VersionNotSupportedException {
        if (webApp != null) webApp.setAbsoluteOrdering(value);
    }

    public AbsoluteOrdering[] getAbsoluteOrdering() throws VersionNotSupportedException {
        return webApp != null ? webApp.getAbsoluteOrdering() : null;
    }

}
