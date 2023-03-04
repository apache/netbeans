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
/*
 * ResourcesProxy.java
 *
 * Created on August 17, 2005, 3:35 PM
 *
 */

package org.netbeans.modules.j2ee.sun.dd.impl.serverresources;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.netbeans.modules.j2ee.sun.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.GraphManager;
import org.netbeans.modules.schema2beans.Schema2BeansUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Nitya Doraisamy
 */
public class ResourcesProxy implements Resources, RootInterfaceImpl {
    
    private Resources resourcesRoot;
    private String version;
    private int ddStatus;
    private SAXParseException error;    
    private List<PropertyChangeListener> listeners; 
    private Schema2BeansUtil.ReindentationListener reindentationListener = new Schema2BeansUtil.ReindentationListener();
    
    /** Creates a new instance of ResourcesProxy */
    public ResourcesProxy(Resources resourcesRoot) {
        this(resourcesRoot, Resources.VERSION_1_3);
    }

    public ResourcesProxy(Resources resourcesRoot, String version) {
        this.resourcesRoot = resourcesRoot;
        this.version = version;
        this.listeners = new ArrayList<PropertyChangeListener>();
        addPropertyChangeListener(reindentationListener);
    }
    
    public int addAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource value) {
        return resourcesRoot==null?-1:resourcesRoot.addAdminObjectResource(value);
    }

    public int removeAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource value) {
        return resourcesRoot==null?-1:resourcesRoot.removeAdminObjectResource(value);
    }

    public void setPersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource[] value) {
        if (resourcesRoot!=null) resourcesRoot.setPersistenceManagerFactoryResource(value);
    }

    public void setPersistenceManagerFactoryResource(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource value) {
        if (resourcesRoot!=null) resourcesRoot.setPersistenceManagerFactoryResource(index, value);
    }

    public void setAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource[] value) {
        if (resourcesRoot!=null) resourcesRoot.setAdminObjectResource(value);
    }

    public void setJdbcResource(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource value) {
        if (resourcesRoot!=null) resourcesRoot.setJdbcResource(index, value);
    }

    public int addConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource value) {
        return resourcesRoot==null?-1:resourcesRoot.addConnectorResource(value);
    }

    public int removeConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource value) {
        return resourcesRoot==null?-1:resourcesRoot.removeConnectorResource(value);
    }

    public void setJdbcConnectionPool(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool value) {
        if (resourcesRoot!=null) resourcesRoot.setJdbcConnectionPool(index, value);
    }

    public void setConnectorConnectionPool(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool value) {
        if (resourcesRoot!=null) resourcesRoot.setConnectorConnectionPool(index, value);
    }

    public int addExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource value) {
        return resourcesRoot==null?-1:resourcesRoot.addExternalJndiResource(value);
    }

    public int removeExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource value) {
        return resourcesRoot==null?-1:resourcesRoot.removeExternalJndiResource(value);
    }

    public void setAdminObjectResource(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource value) {
        if (resourcesRoot!=null) resourcesRoot.setAdminObjectResource(index, value);
    }

    public int size(String name) {
        return resourcesRoot==null?-1:resourcesRoot.size(name);
    }

    public Object[] getValues(String name) {
        return resourcesRoot==null?null:resourcesRoot.getValues(name);
    }

    public Object getValue(String propertyName) {
        return resourcesRoot==null?null:resourcesRoot.getValue(propertyName);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean getPropertyParent(String name) {
        return resourcesRoot==null?null:resourcesRoot.getPropertyParent(name);
    }

    public String getAttributeValue(String name) {
        return resourcesRoot==null?null:resourcesRoot.getAttributeValue(name);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig getResourceAdapterConfig(int index) {
        return resourcesRoot==null?null:resourcesRoot.getResourceAdapterConfig(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource getPersistenceManagerFactoryResource(int index) {
        return resourcesRoot==null?null:resourcesRoot.getPersistenceManagerFactoryResource(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource getMailResource(int index) {
        return resourcesRoot==null?null:resourcesRoot.getMailResource(index);
    }

//    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource getJmsResource(int index) {
//        return resourcesRoot==null?null:resourcesRoot.getJmsResource(index);
//    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource getJdbcResource(int index) {
        return resourcesRoot==null?null:resourcesRoot.getJdbcResource(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool getJdbcConnectionPool(int index) {
        return resourcesRoot==null?null:resourcesRoot.getJdbcConnectionPool(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource getExternalJndiResource(int index) {
        return resourcesRoot==null?null:resourcesRoot.getExternalJndiResource(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource getCustomResource(int index) {
        return resourcesRoot==null?null:resourcesRoot.getCustomResource(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource getAdminObjectResource(int index) {
        return resourcesRoot==null?null:resourcesRoot.getAdminObjectResource(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool getConnectorConnectionPool(int index) {
        return resourcesRoot==null?null:resourcesRoot.getConnectorConnectionPool(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource getConnectorResource(int index) {
        return resourcesRoot==null?null:resourcesRoot.getConnectorResource(index);
    }

    public void setValue(String name, int index, Object value) {
        if (resourcesRoot!=null) resourcesRoot.setValue(name, index, value);
    }

    public int addValue(String name, Object value) {
        return resourcesRoot==null?-1:resourcesRoot.addValue(name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return resourcesRoot==null?null:resourcesRoot.findPropertyValue(propName, value);
    }

    public int removeValue(String name, Object value) {
        return resourcesRoot==null?-1:resourcesRoot.removeValue(name, value);
    }

    public void setValue(String name, Object value) {
        if (resourcesRoot!=null) resourcesRoot.setValue(name, value);
    }

//    public void setJmsResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource[] value) {
//        if (resourcesRoot!=null) resourcesRoot.setJmsResource(value);
//    }

    public void setConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool[] value) {
        if (resourcesRoot!=null) resourcesRoot.setConnectorConnectionPool(value);
    }

//    public void setJmsResource(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource value) {
//        if (resourcesRoot!=null) resourcesRoot.setJmsResource(index, value);
//    }

    public void setResourceAdapterConfig(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig value) {
        if (resourcesRoot!=null) resourcesRoot.setResourceAdapterConfig(index, value);
    }

    public void setCustomResource(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource value) {
        if (resourcesRoot!=null) resourcesRoot.setCustomResource(index, value);
    }

    public int addJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool value) {
        return resourcesRoot==null?-1:resourcesRoot.addJdbcConnectionPool(value);
    }

    public int removeJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool value) {
        return resourcesRoot==null?-1:resourcesRoot.removeJdbcConnectionPool(value);
    }

    public void setValue(String name, Object[] value) {
        if (resourcesRoot!=null) resourcesRoot.setValue(name, value);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (resourcesRoot != null) 
            resourcesRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (resourcesRoot != null) 
            resourcesRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public void setMailResource(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource value) {
        if (resourcesRoot!=null) resourcesRoot.setMailResource(index, value);
    }

    public int addMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource value) {
        return resourcesRoot==null?-1:resourcesRoot.addMailResource(value);
    }

    public int removeMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource value) {
        return resourcesRoot==null?-1:resourcesRoot.removeMailResource(value);
    }

    public void setExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource[] value) {
        if (resourcesRoot!=null) resourcesRoot.setExternalJndiResource(value);
    }

    public void setConnectorResource(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource value) {
        if (resourcesRoot!=null) resourcesRoot.setConnectorResource(index, value);
    }

    public int addCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource value) {
        return resourcesRoot==null?-1:resourcesRoot.addCustomResource(value);
    }

    public int removeCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource value) {
        return resourcesRoot==null?-1:resourcesRoot.removeCustomResource(value);
    }

    public int addJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource value) {
        return resourcesRoot==null?-1:resourcesRoot.addJdbcResource(value);
    }

    public int removeJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource value) {
        return resourcesRoot==null?-1:resourcesRoot.removeJdbcResource(value);
    }

//    public int addJmsResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource value) {
//        return resourcesRoot==null?-1:resourcesRoot.addJmsResource(value);
//    }

//    public int removeJmsResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource value) {
//        return resourcesRoot==null?-1:resourcesRoot.removeJmsResource(value);
//    }

    public void setExternalJndiResource(int index, org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource value) {
        if (resourcesRoot!=null) resourcesRoot.setExternalJndiResource(index, value);
    }

    public int addPersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource value) {
        return resourcesRoot==null?-1:resourcesRoot.addPersistenceManagerFactoryResource(value);
    }

    public int removePersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource value) {
        return resourcesRoot==null?-1:resourcesRoot.removePersistenceManagerFactoryResource(value);
    }

    public void setCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource[] value) {
        if (resourcesRoot!=null) resourcesRoot.setCustomResource(value);
    }

    public void setJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool[] value) {
        if (resourcesRoot!=null) resourcesRoot.setJdbcConnectionPool(value);
    }

    public int addConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool value) {
        return resourcesRoot==null?-1:resourcesRoot.addConnectorConnectionPool(value);
    }

    public int removeConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool value) {
        return resourcesRoot==null?-1:resourcesRoot.removeConnectorConnectionPool(value);
    }

    public void setJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource[] value) {
        if (resourcesRoot!=null) resourcesRoot.setJdbcResource(value);
    }

    public int addResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig value) {
        return resourcesRoot==null?-1:resourcesRoot.addResourceAdapterConfig(value);
    }

    public int removeResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig value) {
        return resourcesRoot==null?-1:resourcesRoot.removeResourceAdapterConfig(value);
    }

    public void setResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig[] value) {
        if (resourcesRoot!=null) resourcesRoot.setResourceAdapterConfig(value);
    }

    public void setMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource[] value) {
        if (resourcesRoot!=null) resourcesRoot.setMailResource(value);
    }

    public void setConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource[] value) {
        if (resourcesRoot!=null) resourcesRoot.setConnectorResource(value);
    }

    public void merge(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean root, int mode) {
        if (root != null) {
            if (root instanceof ResourcesProxy)
                resourcesRoot.merge(((ResourcesProxy)root).getOriginal(), mode);
            else resourcesRoot.merge(root, mode);
        }
    }

    public Resources getOriginal() {
        return resourcesRoot;
    }
    
    public Object getValue(String name, int index) {
        return resourcesRoot==null?null:resourcesRoot.getValues(name);
    }

    public void removeValue(String name, int index) {
        if (resourcesRoot!=null) resourcesRoot.removeValue(name, index);
    }

    public int sizeResourceAdapterConfig() {
        return resourcesRoot==null?-1:resourcesRoot.sizeResourceAdapterConfig();
    }

    public int sizePersistenceManagerFactoryResource() {
        return resourcesRoot==null?-1:resourcesRoot.sizePersistenceManagerFactoryResource();
    }

    public int sizeMailResource() {
        return resourcesRoot==null?-1:resourcesRoot.sizeMailResource();
    }

//    public int sizeJmsResource() {
//        return resourcesRoot==null?-1:resourcesRoot.sizeJmsResource();
//    }

    public int sizeJdbcResource() {
        return resourcesRoot==null?-1:resourcesRoot.sizeJdbcResource();
    }

    public int sizeJdbcConnectionPool() {
        return resourcesRoot==null?-1:resourcesRoot.sizeJdbcConnectionPool();
    }

    public int sizeExternalJndiResource() {
        return resourcesRoot==null?-1:resourcesRoot.sizeExternalJndiResource();
    }

    public int sizeCustomResource() {
        return resourcesRoot==null?-1:resourcesRoot.sizeCustomResource();
    }

    public int sizeConnectorResource() {
        return resourcesRoot==null?-1:resourcesRoot.sizeConnectorResource();
    }

    public int sizeConnectorConnectionPool() {
        return resourcesRoot==null?-1:resourcesRoot.sizeConnectorConnectionPool();
    }

    public int sizeAdminObjectResource() {
        return resourcesRoot==null?-1:resourcesRoot.sizeAdminObjectResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource newCustomResource() {
        return resourcesRoot==null?null:resourcesRoot.newCustomResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource newConnectorResource() {
        return resourcesRoot==null?null:resourcesRoot.newConnectorResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool newConnectorConnectionPool() {
        return resourcesRoot==null?null:resourcesRoot.newConnectorConnectionPool();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource newAdminObjectResource() {
        return resourcesRoot==null?null:resourcesRoot.newAdminObjectResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig[] getResourceAdapterConfig() {
        return resourcesRoot==null?null:resourcesRoot.getResourceAdapterConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource[] getPersistenceManagerFactoryResource() {
        return resourcesRoot==null?null:resourcesRoot.getPersistenceManagerFactoryResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource[] getMailResource() {
        return resourcesRoot==null?null:resourcesRoot.getMailResource();
    }

//    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource[] getJmsResource() {
//        return resourcesRoot==null?null:resourcesRoot.getJmsResource();
//    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource[] getJdbcResource() {
        return resourcesRoot==null?null:resourcesRoot.getJdbcResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool[] getJdbcConnectionPool() {
        return resourcesRoot==null?null:resourcesRoot.getJdbcConnectionPool();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource[] getExternalJndiResource() {
        return resourcesRoot==null?null:resourcesRoot.getExternalJndiResource();
    }

    public String dumpBeanNode() {
        return resourcesRoot==null?null:resourcesRoot.dumpBeanNode();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource[] getAdminObjectResource() {
        return resourcesRoot==null?null:resourcesRoot.getAdminObjectResource();
    }

    public String getAttributeValue(String propName, String name) {
        return resourcesRoot==null?null:resourcesRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return resourcesRoot==null?null:resourcesRoot.getAttributeValue(propName, index, name);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool[] getConnectorConnectionPool() {
        return resourcesRoot==null?null:resourcesRoot.getConnectorConnectionPool();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource[] getConnectorResource() {
        return resourcesRoot==null?null:resourcesRoot.getConnectorResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource[] getCustomResource() {
        return resourcesRoot==null?null:resourcesRoot.getCustomResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource newExternalJndiResource() {
        return resourcesRoot==null?null:resourcesRoot.newExternalJndiResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool newJdbcConnectionPool() {
        return resourcesRoot==null?null:resourcesRoot.newJdbcConnectionPool();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource newJdbcResource() {
        return resourcesRoot==null?null:resourcesRoot.newJdbcResource();
    }

//    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource newJmsResource() {
//        return resourcesRoot==null?null:resourcesRoot.newJmsResource();
//    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource newMailResource() {
        return resourcesRoot==null?null:resourcesRoot.newMailResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource newPersistenceManagerFactoryResource() {
        return resourcesRoot==null?null:resourcesRoot.newPersistenceManagerFactoryResource();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig newResourceAdapterConfig() {
        return resourcesRoot==null?null:resourcesRoot.newResourceAdapterConfig();
    }

    public void setAttributeValue(String name, String value) {
        if (resourcesRoot!=null) resourcesRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (resourcesRoot!=null) resourcesRoot.setAttributeValue(propName, name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (resourcesRoot!=null) resourcesRoot.setAttributeValue(propName, index, name, value);
    }

    public Object clone() {
       ResourcesProxy proxy = null;
        if (resourcesRoot==null)
            proxy = new ResourcesProxy(null);
        else {
            Resources clonedResources=(Resources)resourcesRoot.clone();
            proxy = new ResourcesProxy(clonedResources);
        }
        return proxy;
    }

    /** Resources have no version (at present) so we could throw UnsupportedOperationException
     *  but it's probably just better to clone it since that is what this method does anyway.
     */
    public CommonDDBean cloneVersion(String version) {
        return (CommonDDBean) clone();
    }
   
    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (resourcesRoot != null) {
            resourcesRoot.write(os);
        }
    }

    public void write(java.io.Writer w) throws java.io.IOException, org.netbeans.modules.j2ee.sun.dd.api.DDException {
        if (resourcesRoot!=null) resourcesRoot.write(w);
    }

    public void write(java.io.File f) throws java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansRuntimeException {
        if (resourcesRoot!=null) resourcesRoot.write(f);
    }

    public void write(FileObject fo) throws IOException {
        if(resourcesRoot != null) {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject) {
                ((DDProviderDataObject) dataObject).writeModel(resourcesRoot);
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
    
    public void setVersion(BigDecimal version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }

    public RootInterface getRootInterface() {
        return this;
    }

    public SAXParseException getError() {
        return error;
    }

    public void setError(SAXParseException error) {
        this.error = error;
    }

    public boolean hasOriginal() {
        return getOriginal() != null;
    }

    public ASDDVersion getASDDVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    
    public boolean isTrivial(String nameProperty) {
        // Root nodes are non-trivial by definition.
        return false;
    }
    
    public boolean isEventSource(RootInterface rootDD) {
        return resourcesRoot != null && resourcesRoot == rootDD;
    }
    
    public GraphManager graphManager() {
        return resourcesRoot instanceof BaseBean ? ((BaseBean) resourcesRoot).graphManager() : null;
    }
    
}
