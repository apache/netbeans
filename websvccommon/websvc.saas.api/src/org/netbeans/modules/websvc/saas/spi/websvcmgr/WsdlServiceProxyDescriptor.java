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
package org.netbeans.modules.websvc.saas.spi.websvcmgr;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;

/**
 * **** NOTE **** (nam):
 * 
 * This is legacy class from webservice manager.  The main data here
 * are JarEntry attributes which should be stored in //saas-metadata/codegen.
 * #add/getConsumerData is also important and harder to translate into 
 * saas-services.xml.
 * 
 * Try to avoid additional dependencies on this, as this will eventually go
 * away.
 * 
 * Metadata descriptor that contains the information for a single web service.
 * This metadata is associated (one-to-one) with a proxy jar.
 * 
 * @author quynguyen
 */
public class WsdlServiceProxyDescriptor {
    public static final int JAX_RPC_TYPE = 0;
    public static final int JAX_WS_TYPE = 1;
    public static final String WEBSVC_HOME = SaasServicesModel.WEBSVC_HOME;
    
    private String name;
    private String packageName;
    private int wsType;
    private String wsdl;
    private String xmlDescriptor;
    private transient WSService model;
    private List<JarEntry> jars;
    private Map<String, Object> consumerData;
    
    public WsdlServiceProxyDescriptor() {
    }
    
    public WsdlServiceProxyDescriptor(String name, String packageName, int wsType, URL wsdl, File xmlDescriptor, WSService model) {
        this.name = name;
        this.packageName = packageName;
        this.wsType = wsType;
        this.wsdl = wsdl.toExternalForm();
        this.xmlDescriptor = xmlDescriptor.getAbsolutePath();
        this.model = model;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getWsType() {
        return wsType;
    }

    public void setWsType(int wsType) {
        this.wsType = wsType;
    }
    
    public String getWsdl() {
        return wsdl;
    }
    
    public URL getWsdlUrl() {
        try {
            return new java.net.URL(wsdl);
        } catch (MalformedURLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            return null;
        }
    }
    
    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }
    
    public String getXmlDescriptor() {
        return xmlDescriptor;
    }

    public File getXmlDescriptorFile() {
        return new File(xmlDescriptor);
    }
    
    public void setXmlDescriptor(String xmlDescriptor) {
        this.xmlDescriptor = xmlDescriptor;
    }

    public Map<String, Object> getConsumerData() {
        if (consumerData == null) {
            consumerData = new HashMap<String, Object>();
        }
        return consumerData;
    }
    
    public void setConsumerData(Map<String, Object> consumerData) {
        this.consumerData = consumerData;
    }
    
    public void addConsumerData(String key, Object data) {
        getConsumerData().put(key, data);
    }
    
    public void removeConsumerData(String key) {
        getConsumerData().remove(key);
    }
    
    public List<JarEntry> getJars() {
        if (jars == null) {
            jars = new LinkedList<JarEntry>();
        }
        return jars;
    }
    
    public void setJars(List<JarEntry> jars) {
        this.jars = jars;
    }

    public WSService getModel() {
        return model;
    }

    public void setModel(WSService model) {
        this.model = model;
    }
    
    public void addJar(String relativePath, String type) {
        getJars().add(new JarEntry(relativePath, type));
    }
    
    public void removeJar(String relativePath, String type) {
        getJars().remove(new JarEntry(relativePath, type));
    }
    
    public static class JarEntry {
        public static final String PROXY_JAR_TYPE = "proxy";
        public static final String SRC_JAR_TYPE = "source";
        
        private String name;
        private String type;
        
        public JarEntry() {
        }
        
        public JarEntry(String name, String type) {
            this.name = name;
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public boolean equals(Object o) { 
            try {
                JarEntry entry = (JarEntry)o;
                return entry.name.equals(name) && entry.type.equals(type);
            }catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
                return false;
            }
        }
    }
}
