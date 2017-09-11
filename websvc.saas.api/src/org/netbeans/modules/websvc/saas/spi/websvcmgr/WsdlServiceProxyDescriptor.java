/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    transient private WSService model;
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
