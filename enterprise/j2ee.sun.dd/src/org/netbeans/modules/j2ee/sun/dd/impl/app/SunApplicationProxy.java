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
package org.netbeans.modules.j2ee.sun.dd.impl.app;

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
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
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
public class SunApplicationProxy implements SunApplication, RootInterfaceImpl {

    private SunApplication appRoot;
    private String version;
    private int ddStatus;
    private SAXParseException error;    
    private List<PropertyChangeListener> listeners; 
    private Schema2BeansUtil.ReindentationListener reindentationListener = new Schema2BeansUtil.ReindentationListener();
    

    public SunApplicationProxy(SunApplication appRoot, String version) {
        this.appRoot = appRoot;
        this.version = version;
        this.listeners = new ArrayList<PropertyChangeListener>();
        addPropertyChangeListener(reindentationListener);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        if (appRoot != null) 
            appRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping value) {
        return appRoot==null?-1:appRoot.addSecurityRoleMapping(value);
    }

    public int addWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value) {
        return appRoot==null?-1:appRoot.addWeb(value);
    }

    public String dumpBeanNode() {
        return appRoot==null?null:appRoot.dumpBeanNode();
    }

    public String getPassByReference() {
        return appRoot==null?null:appRoot.getPassByReference();
    }

    public String getRealm() {
        return appRoot==null?null:appRoot.getRealm();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping() {
        return appRoot==null?null:appRoot.getSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int index) {
        return appRoot==null?null:appRoot.getSecurityRoleMapping(index);
    }

    public String getUniqueId() {
        return appRoot==null?null:appRoot.getUniqueId();
    }

    public Object getValue(String propertyName) {
        return appRoot==null?null:appRoot.getValue(propertyName);
    }

    public java.math.BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.app.Web[] getWeb() {
        return appRoot==null?null:appRoot.getWeb();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.app.Web getWeb(int index) {
        return appRoot==null?null:appRoot.getWeb(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping newSecurityRoleMapping() {
        return appRoot==null?null:appRoot.newSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.app.Web newWeb() {
        return appRoot==null?null:appRoot.newWeb();
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        if (appRoot != null) 
            appRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping value) {
        return appRoot==null?-1:appRoot.removeSecurityRoleMapping(value);
    }

    public int removeWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value) {
        return appRoot==null?-1:appRoot.removeWeb(value);
    }

    public void setPassByReference(String value) {
        if (appRoot!=null) appRoot.setPassByReference(value);
    }

    public void setRealm(String value) {
        if (appRoot!=null) appRoot.setRealm(value);
    }

    public void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] value) {
        if (appRoot!=null) appRoot.setSecurityRoleMapping(value);
    }

    public void setSecurityRoleMapping(int index, org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping value) {
        if (appRoot!=null) appRoot.setSecurityRoleMapping(index, value);
    }

    public void setUniqueId(String value) {
        if (appRoot!=null) appRoot.setUniqueId(value);
    }

    public void setVersion(java.math.BigDecimal version) {
        String newVersion = version.toString();
        
        if (this.version.equals(newVersion))
            return;
        if (appRoot != null) {
            Document document = null;
            if(newVersion.equals(SunApplication.VERSION_6_0_1)){
                //This will always be an upgrade
                document = getDocument();
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_1.GlassFishApplication appGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_1.GlassFishApplication.createGraph(document);
                appGraph.changeDocType(DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID, DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID);
                this.appRoot = appGraph;
            }
            if(newVersion.equals(SunApplication.VERSION_6_0_0)){
                //This will always be an upgrade
                document = getDocument();
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_0.SunApplication appGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_0.SunApplication.createGraph(document);
                appGraph.changeDocType(DTDRegistry.SUN_APPLICATION_60_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_60_DTD_SYSTEM_ID);
                this.appRoot = appGraph;
            }
            if(newVersion.equals(SunApplication.VERSION_5_0_0)){
                //This will always be an upgrade
                document = getDocument();
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication appGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication.createGraph(document);
                appGraph.changeDocType(DTDRegistry.SUN_APPLICATION_50_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_50_DTD_SYSTEM_ID);
                this.appRoot = appGraph;
            }
            if(newVersion.equals(SunApplication.VERSION_1_4_0)){
                document = getDocument();
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication appGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication.createGraph(document);
                appGraph.changeDocType(DTDRegistry.SUN_APPLICATION_140_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_140_DTD_SYSTEM_ID);
                this.appRoot = appGraph;
            }
            if(newVersion.equals(SunApplication.VERSION_1_3_0)){
                appRoot.setRealm(null);
                document = getDocument();
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication appGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication.createGraph(document);
                appGraph.changeDocType(DTDRegistry.SUN_APPCLIENT_130_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_130_DTD_SYSTEM_ID);
                this.appRoot = appGraph;
            }
        }
    }

    private Document getDocument(){
        Document document = null;
        if (appRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication)appRoot).graphManager().getXmlDocument();
        }else if (appRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication)appRoot).graphManager().getXmlDocument();
        }else if (appRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication)appRoot).graphManager().getXmlDocument();
        } else if (appRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_0.SunApplication) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_0.SunApplication)appRoot).graphManager().getXmlDocument();
        } else if (appRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_1.GlassFishApplication) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_1.GlassFishApplication)appRoot).graphManager().getXmlDocument();
        }
        return document;
    }
    
    public void setWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web[] value) {
        if (appRoot!=null) appRoot.setWeb(value);
    }

    public void setWeb(int index, org.netbeans.modules.j2ee.sun.dd.api.app.Web value) {
        if (appRoot!=null) appRoot.setWeb(index, value);
    }

    public int sizeSecurityRoleMapping() {
        return appRoot==null?-1:appRoot.sizeSecurityRoleMapping();
    }

    public int sizeWeb() {
        return appRoot==null?-1:appRoot.sizeWeb();
    }

    public void write(OutputStream os) throws IOException {
        if (appRoot!=null) {
            appRoot.write(os);
        }
    }
 
    public void write(Writer w) throws IOException, DDException {
        if (appRoot!=null) appRoot.write(w);
    }
    
    public void write(FileObject fo) throws IOException {
        if(appRoot != null) {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject) {
                ((DDProviderDataObject) dataObject).writeModel(appRoot);
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
    
    public void setOriginal(SunApplication appRoot) {
        if (this.appRoot != appRoot) {
            for (int i=0;i<listeners.size();i++) {
                PropertyChangeListener pcl = listeners.get(i);
                if (this.appRoot != null) {
                    this.appRoot.removePropertyChangeListener(pcl);
                }
                if (appRoot != null) {
                    appRoot.addPropertyChangeListener(pcl);
                }
            }
            this.appRoot = appRoot;
            if (appRoot != null) {
                setProxyVersion(appRoot.getVersion().toString());
            }
        }
    }
     
    public SunApplication getOriginal() {
        return appRoot;
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
    
    public void setValue(String name, Object[] value) {
        if (appRoot!=null) appRoot.setValue(name, value);
    }

    public Object[] getValues(String name) {
        return appRoot==null?null:appRoot.getValues(name);
    }

    public void setValue(String name, int index, Object value) {
        if (appRoot!=null) appRoot.setValue(name, index, value);
    }

    public void setValue(String name, Object value) {
        if (appRoot!=null) appRoot.setValue(name, value);
    }

    public Object getValue(String name, int index) {
        return appRoot==null?null:appRoot.getValue(name, index);
    }

    public String getAttributeValue(String name) {
        return appRoot==null?null:appRoot.getAttributeValue(name);
    }

    public int size(String name) {
        return appRoot==null?-1:appRoot.size(name);
    }

    public int addValue(String name, Object value) {
        return appRoot==null?-1:appRoot.addValue(name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return appRoot==null?null:appRoot.findPropertyValue(propName, value);
    }

    public int removeValue(String name, Object value) {
        return appRoot==null?-1:appRoot.removeValue(name, value);
    }

    public void removeValue(String name, int index) {
        if (appRoot!=null) appRoot.removeValue(name, index);
    }

    public Object clone() {
        SunApplicationProxy proxy = null;
        if (appRoot==null)
            proxy = new SunApplicationProxy(null, version);
        else {
            SunApplication clonedSunApp=(SunApplication)appRoot.clone();
            proxy = new SunApplicationProxy(clonedSunApp, version);
        }
        proxy.setError(error);
        return proxy;
    }

    public String getAttributeValue(String propName, String name) {
        return appRoot==null?null:appRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return appRoot==null?null:appRoot.getAttributeValue(propName, index, name);
    }

    public void setAttributeValue(String name, String value) {
        if (appRoot!=null) appRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (appRoot!=null) appRoot.setAttributeValue(propName, name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (appRoot!=null) appRoot.setAttributeValue(propName, index, name, value);
    }

    public CommonDDBean getPropertyParent(String name) {
        return appRoot.getPropertyParent(name);
    }
    
    public void merge(CommonDDBean root, int mode) {
        if (root instanceof SunApplicationProxy) {
            root = ((SunApplicationProxy) root).getOriginal();
        }
        if (appRoot != root && root instanceof SunApplication) {
            SunApplication newAppRoot = (SunApplication) root;
            if (appRoot != null && appRoot.getVersion().equals(newAppRoot.getVersion())) {
                removePropertyChangeListener(reindentationListener);
                appRoot.merge(newAppRoot, mode);
                addPropertyChangeListener(reindentationListener);
            } else {
                setOriginal((SunApplication) newAppRoot.clone());
            }
        }
    }
    
    public CommonDDBean cloneVersion(String version) {
        return appRoot == null ? null : appRoot.cloneVersion(version);
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
        return ASDDVersion.getASDDVersionFromAppVersion(getVersion());
    }
    
    public boolean isTrivial(String nameProperty) {
        // Root nodes are non-trivial by definition.
        return false;
    }
    
    public boolean isEventSource(RootInterface rootDD) {
        return appRoot != null && appRoot == rootDD;
    }

    public GraphManager graphManager() {
        return appRoot instanceof BaseBean ? ((BaseBean) appRoot).graphManager() : null;
    }
    
}
