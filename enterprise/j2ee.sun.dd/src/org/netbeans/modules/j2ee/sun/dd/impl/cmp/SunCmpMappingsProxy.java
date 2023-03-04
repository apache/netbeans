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
package org.netbeans.modules.j2ee.sun.dd.impl.cmp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;
import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;
import org.netbeans.modules.j2ee.sun.dd.impl.DDTreeWalker;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.netbeans.modules.j2ee.sun.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.GraphManager;
import org.netbeans.modules.schema2beans.Schema2BeansUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;


/**
 *
 * @author Peter Williams
 */
public class SunCmpMappingsProxy implements SunCmpMappings, RootInterfaceImpl {
    
    private SunCmpMappings cmpMappingsRoot;
    private String version;
    private int ddStatus;
    private org.xml.sax.SAXParseException error;    
    private List<PropertyChangeListener> listeners; 
    private Schema2BeansUtil.ReindentationListener reindentationListener = new Schema2BeansUtil.ReindentationListener();
    

    public SunCmpMappingsProxy(SunCmpMappings cmpMappingsRoot, String version) {
        this.cmpMappingsRoot = cmpMappingsRoot;
        this.version = version;
        this.listeners = new ArrayList<PropertyChangeListener>();
        addPropertyChangeListener(reindentationListener);
    }
    
    public void setSunCmpMapping(int index, SunCmpMapping value) {
        if(cmpMappingsRoot != null) cmpMappingsRoot.setSunCmpMapping(index, value);
    }
    
    public SunCmpMapping getSunCmpMapping(int index) {
        return (cmpMappingsRoot != null) ? cmpMappingsRoot.getSunCmpMapping(index) : null;
    }
    
    public int sizeSunCmpMapping() {
        return (cmpMappingsRoot != null) ? cmpMappingsRoot.sizeSunCmpMapping() : -1;
    }
    
    public void setSunCmpMapping(SunCmpMapping[] value) {
        if(cmpMappingsRoot != null) cmpMappingsRoot.setSunCmpMapping(value);
    }
    
    public SunCmpMapping[] getSunCmpMapping() {
        return (cmpMappingsRoot != null) ? cmpMappingsRoot.getSunCmpMapping() : null;
    }
    
    public int addSunCmpMapping(SunCmpMapping value) {
        return (cmpMappingsRoot != null) ? cmpMappingsRoot.addSunCmpMapping(value) : -1;
    }
    
    public int removeSunCmpMapping(SunCmpMapping value) {
        return (cmpMappingsRoot != null) ? cmpMappingsRoot.removeSunCmpMapping(value) : -1;
    }
    
    public SunCmpMapping newSunCmpMapping() {
        return (cmpMappingsRoot != null) ? cmpMappingsRoot.newSunCmpMapping() : null;
    }

    public void setVersion(BigDecimal version) {
        String newVersion = version.toString();
        String currentVersion = null;
        if (this.version.equals(newVersion))
            return;
        if (cmpMappingsRoot != null) {
            Document document = null;
            if (cmpMappingsRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_0.SunCmpMappings) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_0.SunCmpMappings)cmpMappingsRoot).graphManager().getXmlDocument();
                currentVersion = SunCmpMappings.VERSION_1_0;
            } else if (cmpMappingsRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_1.SunCmpMappings) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_1.SunCmpMappings)cmpMappingsRoot).graphManager().getXmlDocument();
                currentVersion = SunCmpMappings.VERSION_1_1;
            } else if (cmpMappingsRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_2.SunCmpMappings) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_2.SunCmpMappings)cmpMappingsRoot).graphManager().getXmlDocument();
                currentVersion = SunCmpMappings.VERSION_1_2;
            }
            
            //remove the doctype
            document = removeDocType(document);
            
            if(newVersion.equals(SunCmpMappings.VERSION_1_2)) {
                if(currentVersion.equals(SunCmpMappings.VERSION_1_1) || currentVersion.equals(SunCmpMappings.VERSION_1_0))
                    generate1_2Graph(document);
                else
                    downgradeCmpMappingsGraph(document, newVersion, currentVersion);
            }
            if(newVersion.equals(SunCmpMappings.VERSION_1_1)) {
                if(currentVersion.equals(SunCmpMappings.VERSION_1_0))
                    generate1_1Graph(document);
                else
                    downgradeCmpMappingsGraph(document, newVersion, currentVersion);
            }
            if(newVersion.equals(SunCmpMappings.VERSION_1_0)) {
                //This will always be a downgrade             
                downgradeCmpMappingsGraph(document, newVersion, currentVersion);
            }
        }
    }

    private void downgradeCmpMappingsGraph(Document document, String downgradeVersion, String currentVersion) {
            DDTreeWalker downgradeScanner = new DDTreeWalker(document, downgradeVersion, currentVersion);
            downgradeScanner.downgradeSunCmpMappingsDocument();
            if(downgradeVersion.equals(SunCmpMappings.VERSION_1_2)) {
                generate1_2Graph(document);
            } else if(downgradeVersion.equals(SunCmpMappings.VERSION_1_1)) {
                generate1_1Graph(document);
            } else if(downgradeVersion.equals(SunCmpMappings.VERSION_1_0)) {
                generate1_0Graph(document);
            }
    }
    
    private void generate1_2Graph(Document document) {
        org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_2.SunCmpMappings cmpMappingsGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_2.SunCmpMappings.createGraph(document);
        cmpMappingsGraph.changeDocType(DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID, DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID);
        this.cmpMappingsRoot = cmpMappingsGraph;
    }
    
    private void generate1_1Graph(Document document) {
        org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_1.SunCmpMappings cmpMappingsGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_1.SunCmpMappings.createGraph(document);
        cmpMappingsGraph.changeDocType(DTDRegistry.SUN_CMP_MAPPING_800_DTD_PUBLIC_ID, DTDRegistry.SUN_CMP_MAPPING_800_DTD_SYSTEM_ID);
        this.cmpMappingsRoot = cmpMappingsGraph;
    }
    
    private void generate1_0Graph(Document document) {
        org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_0.SunCmpMappings cmpMappingsGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_0.SunCmpMappings.createGraph(document);
        cmpMappingsGraph.changeDocType(DTDRegistry.SUN_CMP_MAPPING_700_DTD_PUBLIC_ID, DTDRegistry.SUN_CMP_MAPPING_700_DTD_SYSTEM_ID);
        this.cmpMappingsRoot = cmpMappingsGraph;
    }
    
    public BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }

    private Document getDocument() {
        Document document = null;
        if (cmpMappingsRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_0.SunCmpMappings) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_0.SunCmpMappings)cmpMappingsRoot).graphManager().getXmlDocument();
        } else if (cmpMappingsRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_1.SunCmpMappings) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_1.SunCmpMappings)cmpMappingsRoot).graphManager().getXmlDocument();
        } else if (cmpMappingsRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_2.SunCmpMappings) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_2.SunCmpMappings)cmpMappingsRoot).graphManager().getXmlDocument();
        }
        return document;
    }
    
    private Document removeDocType(Document document) {
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
    public void merge(CommonDDBean root, int mode) {
        if (root instanceof SunCmpMappingsProxy) {
            root = ((SunCmpMappingsProxy) root).getOriginal();
        }
        if (cmpMappingsRoot != root && root instanceof SunCmpMappings) {
            SunCmpMappings newCmpMappingsRoot = (SunCmpMappings) root;
            if (cmpMappingsRoot != null && cmpMappingsRoot.getVersion().equals(newCmpMappingsRoot.getVersion())) {
                removePropertyChangeListener(reindentationListener);
                cmpMappingsRoot.merge(newCmpMappingsRoot, mode);
                addPropertyChangeListener(reindentationListener);
            } else {
                setOriginal((SunCmpMappings) newCmpMappingsRoot.clone());
            }
        }
    }

    public CommonDDBean cloneVersion(String version) {
        return cmpMappingsRoot == null ? null : cmpMappingsRoot.cloneVersion(version);
    }
   
    public void setOriginal(SunCmpMappings cmpMappingsRoot) {
        if (this.cmpMappingsRoot != cmpMappingsRoot) {
            for (int i=0;i<listeners.size();i++) {
                PropertyChangeListener pcl = listeners.get(i);
                if (this.cmpMappingsRoot != null) {
                    this.cmpMappingsRoot.removePropertyChangeListener(pcl);
                }
                if (cmpMappingsRoot != null) {
                    cmpMappingsRoot.addPropertyChangeListener(pcl);
                }
            }
            this.cmpMappingsRoot = cmpMappingsRoot;
            if (cmpMappingsRoot != null) {
                setProxyVersion(cmpMappingsRoot.getVersion().toString());
            }
        }
    }
    
    public SunCmpMappings getOriginal() {
        return cmpMappingsRoot;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        if (cmpMappingsRoot != null) 
            cmpMappingsRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        if (cmpMappingsRoot != null) 
            cmpMappingsRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public Object getValue(String propertyName) {
        return cmpMappingsRoot == null?null:cmpMappingsRoot.getValue(propertyName);
    }

    public Object[] getValues(String name) {
        return cmpMappingsRoot == null?null:cmpMappingsRoot.getValues(name);
    }

    public Object getValue(String name, int index) {
        return cmpMappingsRoot == null?null:cmpMappingsRoot.getValue(name, index);
    }

    public void setValue(String name, Object value) {
        if (cmpMappingsRoot != null) cmpMappingsRoot.setValue(name, value);
    }

    public void setValue(String name, Object[] value) {
        if (cmpMappingsRoot != null) cmpMappingsRoot.setValue(name, value);
    }

    public void setValue(String name, int index, Object value) {
        if (cmpMappingsRoot != null) cmpMappingsRoot.setValue(name, index, value);
    }

    public String getAttributeValue(String name) {
        return cmpMappingsRoot == null?null:cmpMappingsRoot.getAttributeValue(name);
    }

    public String getAttributeValue(String propName, String name) {
        return cmpMappingsRoot == null?null:cmpMappingsRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return cmpMappingsRoot == null?null:cmpMappingsRoot.getAttributeValue(propName, index, name);
    }

    public void setAttributeValue(String name, String value) {
        if (cmpMappingsRoot != null) cmpMappingsRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (cmpMappingsRoot != null) cmpMappingsRoot.setAttributeValue(propName, index, name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (cmpMappingsRoot != null) cmpMappingsRoot.setAttributeValue(propName, name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return cmpMappingsRoot == null?null:cmpMappingsRoot.findPropertyValue(propName, value);
    }

    public int addValue(String name, Object value) {
        return cmpMappingsRoot == null?-1:cmpMappingsRoot.addValue(name, value);
    }

    public int removeValue(String name, Object value) {
        return cmpMappingsRoot == null?-1:cmpMappingsRoot.removeValue(name, value);
    }

    public void removeValue(String name, int index) {
        if (cmpMappingsRoot != null) cmpMappingsRoot.removeValue(name, index);
    }

    public void write(OutputStream os) throws IOException {
        if (cmpMappingsRoot != null) {
            cmpMappingsRoot.write(os);
        }
    }

    public void write(Writer w) throws IOException, DDException {
        if (cmpMappingsRoot != null) cmpMappingsRoot.write(w);
    }
    
    public void write(FileObject fo) throws IOException {
        if(cmpMappingsRoot != null) {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject) {
                ((DDProviderDataObject) dataObject).writeModel(cmpMappingsRoot);
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
        if (cmpMappingsRoot != null) 
            return cmpMappingsRoot.dumpBeanNode();
        else
            return null;
    }

    public CommonDDBean getPropertyParent(String name) {
        return cmpMappingsRoot.getPropertyParent(name);
    }
    
    public Object clone() {
        SunCmpMappingsProxy proxy = null;
        if (cmpMappingsRoot == null)
            proxy = new SunCmpMappingsProxy(null, version);
        else {
            SunCmpMappings clonedSunCmpMappings=(SunCmpMappings)cmpMappingsRoot.clone();
            proxy = new SunCmpMappingsProxy(clonedSunCmpMappings, version);
        }
        proxy.setError(error);
        return proxy;
    }
    
    public org.xml.sax.SAXParseException getError() {
        return error;
    }
    
    public void setError(org.xml.sax.SAXParseException error) {
        this.error=error;
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
    
    public int size(String name) {
        return cmpMappingsRoot == null?-1:cmpMappingsRoot.size(name);
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
        return ASDDVersion.getASDDVersionFromCmpMappingsVersion(getVersion());
    }
    
    public boolean isTrivial(String nameProperty) {
        // Root nodes are non-trivial by definition.
        return false;
    }
    
    public boolean isEventSource(RootInterface rootDD) {
        return cmpMappingsRoot != null && cmpMappingsRoot == rootDD;
    }
    
    public GraphManager graphManager() {
        return cmpMappingsRoot instanceof BaseBean ? ((BaseBean) cmpMappingsRoot).graphManager() : null;
    }
    
}
