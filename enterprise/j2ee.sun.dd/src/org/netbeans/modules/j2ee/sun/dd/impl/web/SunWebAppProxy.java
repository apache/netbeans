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
package org.netbeans.modules.j2ee.sun.dd.impl.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.Valve;
import org.netbeans.modules.j2ee.sun.dd.impl.DDTreeWalker;
import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.netbeans.modules.j2ee.sun.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.GraphManager;
import org.netbeans.modules.schema2beans.Schema2BeansUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;


/**
 *
 * @author Nitya Doraisamy
 * @author Peter Williams
 */
public class SunWebAppProxy implements SunWebApp, RootInterfaceImpl {
    
    private SunWebApp webRoot;
    private String version;
    private int ddStatus;
    private SAXParseException error;    
    private List<PropertyChangeListener> listeners; 
    private Schema2BeansUtil.ReindentationListener reindentationListener = new Schema2BeansUtil.ReindentationListener();
        
    
    public SunWebAppProxy(SunWebApp webRoot, String version) {
        this.webRoot = webRoot;
        this.version = version;
        this.listeners = new ArrayList<PropertyChangeListener>();
        addPropertyChangeListener(reindentationListener);
    }

    public int addEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ejbRef) {
        return webRoot==null?-1:webRoot.addEjbRef(ejbRef);
    }

    public int addIdempotentUrlPattern(boolean param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.addIdempotentUrlPattern(param);
    }

    public int addMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination messageDestination) {
        return webRoot==null?-1:webRoot.addMessageDestination(messageDestination);
    }

    public int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty webProperty) {
        return webRoot==null?-1:webRoot.addWebProperty(webProperty);
    }

    public int addResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef resourceEnvRef) {
        return webRoot==null?-1:webRoot.addResourceEnvRef(resourceEnvRef);
    }

    public int addResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef resourceRef) {
        return webRoot==null?-1:webRoot.addResourceRef(resourceRef);
    }

    public int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        return webRoot==null?-1:webRoot.addSecurityRoleMapping(securityRoleMapping);
    }

    public int addServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef serviceRef) {
        return webRoot==null?-1:webRoot.addServiceRef(serviceRef);
    }

    public int addServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet servlet) {
        return webRoot==null?-1:webRoot.addServlet(servlet);
    }

    public int addWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription webserviceDescription) {
        return webRoot==null?-1:webRoot.addWebserviceDescription(webserviceDescription);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Cache getCache() {
        return webRoot==null?null:webRoot.getCache();
    }

    public String getContextRoot() {
        return webRoot==null?null:webRoot.getContextRoot();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[] getEjbRef() {
        return webRoot==null?null:webRoot.getEjbRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef getEjbRef(int param) {
        return webRoot==null?null:webRoot.getEjbRef(param);
    }

    public String getErrorUrl() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getErrorUrl();
    }

    public String getHttpservletSecurityProvider() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getHttpservletSecurityProvider();
    }
    
    public boolean[] getIdempotentUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getIdempotentUrlPattern();
    }

    public String getIdempotentUrlPatternNumOfRetries(int param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getIdempotentUrlPatternNumOfRetries(param);
    }

    public String getIdempotentUrlPatternUrlPattern(int param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getIdempotentUrlPatternUrlPattern(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig getJspConfig() {
        return webRoot==null?null:webRoot.getJspConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo getLocaleCharsetInfo() {
        return webRoot==null?null:webRoot.getLocaleCharsetInfo();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[] getMessageDestination() {
        return webRoot==null?null:webRoot.getMessageDestination();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination getMessageDestination(int param) {
        return webRoot==null?null:webRoot.getMessageDestination(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader getMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getMyClassLoader();
    }
    
	public java.lang.String getMyClassLoaderExtraClassPath() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getMyClassLoaderExtraClassPath();
    }
    
	public java.lang.String getMyClassLoaderDelegate() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getMyClassLoaderDelegate();
    }

    public String getParameterEncodingDefaultCharset() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getParameterEncodingDefaultCharset();
    }

    public String getParameterEncodingFormHintField() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getParameterEncodingFormHintField();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty() {
        return webRoot==null?null:webRoot.getWebProperty();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int param) {
        return webRoot==null?null:webRoot.getWebProperty(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[] getResourceEnvRef() {
        return webRoot==null?null:webRoot.getResourceEnvRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef getResourceEnvRef(int param) {
        return webRoot==null?null:webRoot.getResourceEnvRef(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[] getResourceRef() {
        return webRoot==null?null:webRoot.getResourceRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef getResourceRef(int param) {
        return webRoot==null?null:webRoot.getResourceRef(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping() {
        return webRoot==null?null:webRoot.getSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int param) {
        return webRoot==null?null:webRoot.getSecurityRoleMapping(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[] getServiceRef() {
        return webRoot==null?null:webRoot.getServiceRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef getServiceRef(int param) {
        return webRoot==null?null:webRoot.getServiceRef(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Servlet[] getServlet() {
        return webRoot==null?null:webRoot.getServlet();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Servlet getServlet(int param) {
        return webRoot==null?null:webRoot.getServlet(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig getSessionConfig() {
        return webRoot==null?null:webRoot.getSessionConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription[] getWebserviceDescription() {
        return webRoot==null?null:webRoot.getWebserviceDescription();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription getWebserviceDescription(int param) {
        return webRoot==null?null:webRoot.getWebserviceDescription(param);
    }

    public boolean isIdempotentUrlPattern(int param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?false:webRoot.isIdempotentUrlPattern(param);
    }

    public boolean isMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?false:webRoot.isMyClassLoader();
    }

    public boolean isParameterEncoding() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?false:webRoot.isParameterEncoding();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Cache newCache() {
        return webRoot==null?null:webRoot.newCache();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef newEjbRef() {
        return webRoot==null?null:webRoot.newEjbRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig newJspConfig() {
        return webRoot==null?null:webRoot.newJspConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo newLocaleCharsetInfo() {
        return webRoot==null?null:webRoot.newLocaleCharsetInfo();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination newMessageDestination() {
        return webRoot==null?null:webRoot.newMessageDestination();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader newMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.newMyClassLoader();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty() {
        return webRoot==null?null:webRoot.newWebProperty();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef newResourceEnvRef() {
        return webRoot==null?null:webRoot.newResourceEnvRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef newResourceRef() {
        return webRoot==null?null:webRoot.newResourceRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping newSecurityRoleMapping() {
        return webRoot==null?null:webRoot.newSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef newServiceRef() {
        return webRoot==null?null:webRoot.newServiceRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Servlet newServlet() {
        return webRoot==null?null:webRoot.newServlet();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig newSessionConfig() {
        return webRoot==null?null:webRoot.newSessionConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription newWebserviceDescription() {
        return webRoot==null?null:webRoot.newWebserviceDescription();
    }

    public int removeEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ejbRef) {
        return webRoot==null?-1:webRoot.removeEjbRef(ejbRef);
    }

    public int removeIdempotentUrlPattern(boolean param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.removeIdempotentUrlPattern(param);
    }

    public void removeIdempotentUrlPattern(int param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.removeIdempotentUrlPattern(param);
    }

    public int removeMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination messageDestination) {
        return webRoot==null?-1:webRoot.removeMessageDestination(messageDestination);
    }

    public int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty webProperty) {
        return webRoot==null?-1:webRoot.removeWebProperty(webProperty);
    }

    public int removeResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef resourceEnvRef) {
        return webRoot==null?-1:webRoot.removeResourceEnvRef(resourceEnvRef);
    }

    public int removeResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef resourceRef) {
        return webRoot==null?-1:webRoot.removeResourceRef(resourceRef);
    }

    public int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        return webRoot==null?-1:webRoot.removeSecurityRoleMapping(securityRoleMapping);
    }

    public int removeServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef serviceRef) {
        return webRoot==null?-1:webRoot.removeServiceRef(serviceRef);
    }

    public int removeServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet servlet) {
        return webRoot==null?-1:webRoot.removeServlet(servlet);
    }

    public int removeWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription webserviceDescription) {
        return webRoot==null?-1:webRoot.removeWebserviceDescription(webserviceDescription);
    }

    public void setCache(org.netbeans.modules.j2ee.sun.dd.api.web.Cache cache) {
        if (webRoot!=null) webRoot.setCache(cache);
    }

    public void setContextRoot(String str) {
        if (webRoot!=null) webRoot.setContextRoot(str);
    }

    public void setEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[] ejbRef) {
        if (webRoot!=null) webRoot.setEjbRef(ejbRef);
    }

    public void setEjbRef(int param, org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ejbRef) {
        if (webRoot!=null) webRoot.setEjbRef(param, ejbRef);
    }

    public void setErrorUrl(String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setErrorUrl(str);
    }

    public void setHttpservletSecurityProvider(String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setHttpservletSecurityProvider(str);
    }
    
    public void setIdempotentUrlPattern(boolean[] values) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setIdempotentUrlPattern(values);
    }

    public void setIdempotentUrlPattern(int param, boolean param1) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setIdempotentUrlPattern(param, param1);
    }

    public void setIdempotentUrlPatternNumOfRetries(int param, String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setIdempotentUrlPatternNumOfRetries(param, str);
    }

    public void setIdempotentUrlPatternUrlPattern(int param, String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setIdempotentUrlPatternUrlPattern(param, str);
    }

    public void setJspConfig(org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig jspConfig) {
        if (webRoot!=null) webRoot.setJspConfig(jspConfig);
    }

    public void setLocaleCharsetInfo(org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo localeCharsetInfo) {
        if (webRoot!=null) webRoot.setLocaleCharsetInfo(localeCharsetInfo);
    }

    public void setMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[] messageDestination) {
        if (webRoot!=null) webRoot.setMessageDestination(messageDestination);
    }

    public void setMessageDestination(int param, org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination messageDestination) {
        if (webRoot!=null) webRoot.setMessageDestination(param, messageDestination);
    }

    public void setMyClassLoader(boolean param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMyClassLoader(param);
    }

    public void setMyClassLoader(org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader myClassLoader) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMyClassLoader(myClassLoader);
    }
    
	public void setMyClassLoaderExtraClassPath(java.lang.String value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMyClassLoaderExtraClassPath(value);
    }
    
	public void setMyClassLoaderDelegate(java.lang.String value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMyClassLoaderDelegate(value);
    }
    
    public void setParameterEncoding(boolean param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setParameterEncoding(param);
    }

    public void setParameterEncodingDefaultCharset(String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setParameterEncodingDefaultCharset(str);
    }

    public void setParameterEncodingFormHintField(String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setParameterEncodingFormHintField(str);
    }

    public void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] webProperty) {
        if (webRoot!=null) webRoot.setWebProperty(webProperty);
    }

    public void setWebProperty(int param, org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty webProperty) {
        if (webRoot!=null) webRoot.setWebProperty(param, webProperty);
    }

    public void setResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[] resourceEnvRef) {
        if (webRoot!=null) webRoot.setResourceEnvRef(resourceEnvRef);
    }

    public void setResourceEnvRef(int param, org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef resourceEnvRef) {
        if (webRoot!=null) webRoot.setResourceEnvRef(param, resourceEnvRef);
    }

    public void setResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[] resourceRef) {
        if (webRoot!=null) webRoot.setResourceRef(resourceRef);
    }

    public void setResourceRef(int param, org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef resourceRef) {
        if (webRoot!=null) webRoot.setResourceRef(param, resourceRef);
    }

    public void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] securityRoleMapping) {
        if (webRoot!=null) webRoot.setSecurityRoleMapping(securityRoleMapping);
    }

    public void setSecurityRoleMapping(int param, org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        if (webRoot!=null) webRoot.setSecurityRoleMapping(param, securityRoleMapping);
    }

    public void setServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[] serviceRef) {
        if (webRoot!=null) webRoot.setServiceRef(serviceRef);
    }

    public void setServiceRef(int param, org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef serviceRef) {
        if (webRoot!=null) webRoot.setServiceRef(param, serviceRef);
    }

    public void setServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet[] servlet) {
        if (webRoot!=null) webRoot.setServlet(servlet);
    }

    public void setServlet(int param, org.netbeans.modules.j2ee.sun.dd.api.web.Servlet servlet) {
        if (webRoot!=null) webRoot.setServlet(param, servlet);
    }

    public void setSessionConfig(org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig sessionConfig) {
        if (webRoot!=null) webRoot.setSessionConfig(sessionConfig);
    }

    public void setWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription[] webserviceDescription) {
        if (webRoot!=null) webRoot.setWebserviceDescription(webserviceDescription);
    }

    public void setWebserviceDescription(int param, org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription webserviceDescription) {
        if (webRoot!=null) webRoot.setWebserviceDescription(param, webserviceDescription);
    }

    public int sizeEjbRef() {
        return webRoot==null?-1:webRoot.sizeEjbRef();
    }

    public int sizeIdempotentUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeIdempotentUrlPattern();
    }

    public int sizeIdempotentUrlPatternNumOfRetries() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeIdempotentUrlPatternNumOfRetries();
    }

    public int sizeIdempotentUrlPatternUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeIdempotentUrlPatternUrlPattern();
    }

    public int sizeMessageDestination() {
        return webRoot==null?-1:webRoot.sizeMessageDestination();
    }

    public int sizeWebProperty() {
        return webRoot==null?-1:webRoot.sizeWebProperty();
    }

    public int sizeResourceEnvRef() {
        return webRoot==null?-1:webRoot. sizeResourceEnvRef();
    }

    public int sizeResourceRef() {
        return webRoot==null?-1:webRoot.sizeResourceRef();
    }

    public int sizeSecurityRoleMapping() {
        return webRoot==null?-1:webRoot.sizeSecurityRoleMapping();
    }

    public int sizeServiceRef() {
        return webRoot==null?-1:webRoot.sizeServiceRef();
    }

    public int sizeServlet() {
        return webRoot==null?-1:webRoot.sizeServlet();
    }

    public int sizeWebserviceDescription() {
        return webRoot==null?-1:webRoot.sizeWebserviceDescription();
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        if (webRoot != null) 
            webRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
         if (webRoot != null) 
            webRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void setOriginal(SunWebApp webRoot) {
        if (this.webRoot != webRoot) {
            for (int i=0;i<listeners.size();i++) {
                PropertyChangeListener pcl = listeners.get(i);
                if (this.webRoot != null) {
                    this.webRoot.removePropertyChangeListener(pcl);
                }
                if (webRoot != null) {
                    webRoot.addPropertyChangeListener(pcl);
                }
            }
            this.webRoot = webRoot;
            if (webRoot != null) {
                setProxyVersion(webRoot.getVersion().toString());
            }
        }
    }
     
    public SunWebApp getOriginal() {
        return webRoot;
    }
    
    public SAXParseException getError() {
        return error;
    }
    
    public void setError(SAXParseException error) {
        this.error = error;
    }  
    
    public void setProxyVersion(java.lang.String value) {
        if ((version==null && value!=null) || (version != null && !version.equals(value))) {
            PropertyChangeEvent evt = new PropertyChangeEvent(
                    this, PROPERTY_VERSION, version, value); 
            version=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }
    
    public void setVersion(java.math.BigDecimal version) {
        String newVersion = version.toString();
        String currentVersion = null;
        if (this.version.equals(newVersion)) 
            return;
        if (webRoot != null) {
            Document document = null;
            if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp)webRoot).graphManager().getXmlDocument();
                currentVersion = SunWebApp.VERSION_2_3_0;
            }else if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp)webRoot).graphManager().getXmlDocument();
                currentVersion = SunWebApp.VERSION_2_4_0;
            }else if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp)webRoot).graphManager().getXmlDocument();
                currentVersion = SunWebApp.VERSION_2_4_1;
            }else if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp)webRoot).graphManager().getXmlDocument();
                currentVersion = SunWebApp.VERSION_2_5_0;
            }else if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_0.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_0.SunWebApp)webRoot).graphManager().getXmlDocument();
                currentVersion = SunWebApp.VERSION_3_0_0;
            } else if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_1.GlassFishWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_1.GlassFishWebApp)webRoot).graphManager().getXmlDocument();
                currentVersion = SunWebApp.VERSION_3_0_1;
            }
            
            //remove the doctype
            document = removeDocType(document);
            
            if(newVersion.equals(SunWebApp.VERSION_3_0_1)){
                //This will always be an upgrade
                generate3_01Graph(document);
            } else if(newVersion.equals(SunWebApp.VERSION_3_0_0)){
                if(currentVersion.equals(SunWebApp.VERSION_2_5_0) || currentVersion.equals(SunWebApp.VERSION_2_4_1) || currentVersion.equals(SunWebApp.VERSION_2_4_0) ||
                        currentVersion.equals(SunWebApp.VERSION_2_3_0))
                    generate3_00Graph(document);
                else
                    downgradeWebGraph(document, newVersion, currentVersion);
            } else if(newVersion.equals(SunWebApp.VERSION_2_5_0)){
                if(currentVersion.equals(SunWebApp.VERSION_2_4_1) || currentVersion.equals(SunWebApp.VERSION_2_4_0) ||
                        currentVersion.equals(SunWebApp.VERSION_2_3_0))
                    generate2_50Graph(document);
                else
                    downgradeWebGraph(document, newVersion, currentVersion);
            } else if(newVersion.equals(SunWebApp.VERSION_2_4_1)){
                if(currentVersion.equals(SunWebApp.VERSION_2_4_0) || currentVersion.equals(SunWebApp.VERSION_2_3_0))
                    generate2_41Graph(document);
                else
                    downgradeWebGraph(document, newVersion, currentVersion);
            } else if(newVersion.equals(SunWebApp.VERSION_2_4_0)){
                if(currentVersion.equals(SunWebApp.VERSION_2_3_0))
                    generate2_40Graph(document);
                else
                    downgradeWebGraph(document, newVersion, currentVersion);
            } else if(newVersion.equals(SunWebApp.VERSION_2_3_0)){
                //This will always be a downgrade
                downgradeWebGraph(document, newVersion, currentVersion);
            }
        }
    }

    private void downgradeWebGraph(Document document, String downgradeVersion, String currentVersion){
            DDTreeWalker downgradeScanner = new DDTreeWalker(document, downgradeVersion, currentVersion);
            downgradeScanner.downgradeSunWebAppDocument();
            if(downgradeVersion.equals(SunWebApp.VERSION_2_5_0)){
                generate2_50Graph(document);
            }else if(downgradeVersion.equals(SunWebApp.VERSION_2_4_1)){
                generate2_41Graph(document);
            }else if(downgradeVersion.equals(SunWebApp.VERSION_2_4_0)){
                generate2_40Graph(document);
            }else if(downgradeVersion.equals(SunWebApp.VERSION_2_3_0)){
                generate2_30Graph(document);
            }
    }
        
    private Document removeDocType(Document document){
        if (document != null) {
            org.w3c.dom.Element docElement = document.getDocumentElement();
            if (docElement != null) {
                org.w3c.dom.DocumentType docType = document.getDoctype();
                if (docType != null) {
                    document.removeChild(docType); //NOI18N
                }
            }
        }
        return document;
    } 
    
    private void generate3_01Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_1.GlassFishWebApp webGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_1.GlassFishWebApp.createGraph(document);
        webGraph.changeDocType(DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID, DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID);
        this.webRoot = webGraph;
    }

    private void generate3_00Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_0.SunWebApp webGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_0.SunWebApp.createGraph(document);
        webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_300_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_300_DTD_SYSTEM_ID);
        this.webRoot = webGraph;
    }

    private void generate2_50Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp webGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp.createGraph(document);
        webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_250_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_250_DTD_SYSTEM_ID);
        this.webRoot = webGraph;
    }
    
    private void generate2_41Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp webGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp.createGraph(document);
        webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_241_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_241_DTD_SYSTEM_ID);
        this.webRoot = webGraph;
    }
    
    private void generate2_40Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp webGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp.createGraph(document);
        webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_240_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_240_DTD_SYSTEM_ID);
        this.webRoot = webGraph;
    }
    
    private void generate2_30Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp webGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp.createGraph(document);
        webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_230_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_230_DTD_SYSTEM_ID);
        this.webRoot = webGraph;
    }
    
    public java.math.BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }
    
    public Object getValue(String name) {
        return webRoot==null?null:webRoot.getValue(name);
    }
    
    public void write(OutputStream os) throws IOException {
        if (webRoot != null) {
            webRoot.write(os);
        }
    }
    
    public void write(Writer w) throws IOException, DDException {
        if (webRoot!=null) webRoot.write(w);
    }

    public void write(FileObject fo) throws IOException {
        if(webRoot != null) {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject) {
                ((DDProviderDataObject) dataObject).writeModel(webRoot);
            } else {
                FileLock lock = fo.lock();
                try {
                    OutputStream os = fo.getOutputStream(lock);
                    try {
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
    
    public String dumpBeanNode() {
        if (webRoot != null) 
            return webRoot.dumpBeanNode();
        else
            return null;
    }

    public void setValue(String name, Object[] value) {
        if (webRoot!=null) webRoot.setValue(name, value);
    }

    public Object[] getValues(String name) {
        return webRoot==null?null:webRoot.getValues(name);
    }

    public void setValue(String name, int index, Object value) {
        if (webRoot!=null) webRoot.setValue(name, index, value);
    }

    public void setValue(String name, Object value) {
        if (webRoot!=null) webRoot.setValue(name, value);
    }

    public Object getValue(String name, int index) {
        return webRoot==null?null:webRoot.getValue(name, index);
    }

    public String getAttributeValue(String name) {
         return webRoot==null?null:webRoot.getAttributeValue(name);
    }

    public int size(String name) {
         return webRoot==null?-1:webRoot.size(name);
    }

    public int addValue(String name, Object value) {
        return webRoot==null?-1:webRoot.addValue(name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return webRoot==null?null:webRoot.findPropertyValue(propName, value);
    }

    public int removeValue(String name, Object value) {
        return webRoot==null?-1:webRoot.removeValue(name, value);
    }

    public void removeValue(String name, int index) {
        if (webRoot!=null) webRoot.removeValue(name, index);
    }

    public Object clone() {
        SunWebAppProxy proxy = null;
        if (webRoot==null)
            proxy = new SunWebAppProxy(null, version);
        else {
            SunWebApp clonedSunWeb=(SunWebApp)webRoot.clone();
            proxy = new SunWebAppProxy(clonedSunWeb, version);
        }
        proxy.setError(error);
        return proxy;
    }

    public String getAttributeValue(String propName, String name) {
        return webRoot==null?null:webRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return webRoot==null?null:webRoot.getAttributeValue(propName, index, name);
    }

    public void setAttributeValue(String name, String value) {
        if (webRoot!=null) webRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (webRoot!=null) webRoot.setAttributeValue(propName, name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (webRoot!=null) webRoot.setAttributeValue(propName, index, name, value);
    }    
    
    public CommonDDBean getPropertyParent(String name) {
        return webRoot.getPropertyParent(name);
    }

    public void merge(CommonDDBean root, int mode) {
        if (root instanceof SunWebAppProxy) {
            root = ((SunWebAppProxy) root).getOriginal();
        }
        if (webRoot != root && root instanceof SunWebApp) {
            SunWebApp newWebRoot = (SunWebApp) root;
            if (webRoot != null && webRoot.getVersion().equals(newWebRoot.getVersion())) {
                removePropertyChangeListener(reindentationListener);
                webRoot.merge(newWebRoot, mode);
                addPropertyChangeListener(reindentationListener);
            } else {
                setOriginal((SunWebApp) newWebRoot.clone());
            }
        }
    }

    public CommonDDBean cloneVersion(String version) {
        return webRoot == null ? null : webRoot.cloneVersion(version);
    }
   
    public int removeMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.removeMessageDestinationRef(value);
    }

    public int addMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.addMessageDestinationRef(value);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef getMessageDestinationRef(int index) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getMessageDestinationRef(index);
    }

    public void setMessageDestinationRef(int index, org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMessageDestinationRef(index, value);
    }

    public void setMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[] value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMessageDestinationRef(value);
    }

    public int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeMessageDestinationRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.newMessageDestinationRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getMessageDestinationRef();
    }

    public void setValve(int index, Valve value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setValve(index, value);
    }

    public Valve getValve(int index) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getValve(index);
    }

    public int sizeValve() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeValve();
    }

    public void setValve(Valve[] value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setValve(value);
    }

    public Valve[] getValve() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getValve();
    }

    public int addValve(Valve value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.addValve(value);
    }

    public int removeValve(Valve value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.removeValve(value);
    }

    public Valve newValve() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.newValve();
    }

    public int getStatus() {
        return ddStatus;
    }
    
    public void setStatus(int value) {
        if (ddStatus!=value) {
            PropertyChangeEvent evt = new PropertyChangeEvent(
                    this, PROPERTY_STATUS, ddStatus, value);
            ddStatus=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }
    
    public RootInterface getRootInterface() {
        return this;
    }

    public boolean hasOriginal() {
        return getOriginal() != null;
    }
    
    public ASDDVersion getASDDVersion() {
        return ASDDVersion.getASDDVersionFromServletVersion(getVersion());
    }
    
    public boolean isTrivial(String nameProperty) {
        // Root nodes are non-trivial by definition.
        return false;
    }
    
    public boolean isEventSource(RootInterface rootDD) {
        return webRoot != null && webRoot == rootDD;
    }
    
    public GraphManager graphManager() {
        return webRoot instanceof BaseBean ? ((BaseBean) webRoot).graphManager() : null;
    }
    
}
