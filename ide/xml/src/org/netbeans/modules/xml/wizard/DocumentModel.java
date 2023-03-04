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
package org.netbeans.modules.xml.wizard;

import org.netbeans.modules.xml.wizard.XMLContentAttributes;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;

/**
 * Holds state of new document wizard.
 *
 * @author  Petr Kuzel
 */
public final class DocumentModel {

    public static final int NONE = 0;

    public static final int DTD = 1;

    public static final int SCHEMA = 2;

    public static final int OTHER = 3;

    private String name;
    
    private String namespace;
    
    private String publicID;    
    
    private String systemID;
        
    private String root;
    
    // input property describing wizard invocation context
    private URL targetFolderURL;
    
    public static final String PROP_TYPE = "type";
    
    private int type;

    private PropertyChangeSupport support;
    
    private List schemaNodes;
  
    private String prefix;
    
    private XMLContentAttributes contentAttr;
    
    private String primarySchemaFileName;
    
    /** Creates new SchemaWizardModel */
    public DocumentModel(URL targetFolderURL) {
        type = NONE;
        this.targetFolderURL = targetFolderURL;
    }
        
    public String getName() {
        return name;
    }
    
    public void setName(String value) {
        name = value;
    }
        
    public String getNamespace() {
        return this.namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getPublicID() {
        if (publicID != null && publicID.trim().equals("")) return null;
        return this.publicID;
    }
    
    public void setPublicID(String publicID) {
        this.publicID = publicID;
    }
    
    public String getSystemID() {
        return this.systemID;
    }
    
    public void setSystemID(String systemID) {
        this.systemID = systemID;
    }
    
    public String getRoot() {
        if (root != null && root.trim().equals("")) return null;
        return this.root;
    }
    
    public void setRoot(String root) {
        this.root = root;
    }
            
    public int getType() {
        return this.type;
    }
    
    public void setType(int type) {
        int old = this.type;
        this.type = type;
        getSupport().firePropertyChange(PROP_TYPE, old, type);
    }
    
    public URL getTargetFolderURL() {
        return targetFolderURL;
    }
        
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getSupport().addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getSupport().removePropertyChangeListener(l);
    }
    
    private synchronized PropertyChangeSupport getSupport() {
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        return support;
    }
    
    public void setSchemaNodes(List schemaNodes){
        this.schemaNodes = schemaNodes;
    }
    
    public List getSchemaNodes(){
        return schemaNodes;
        
    }
    
    public void setPrefix(String prefix){
        this.prefix=prefix;
    }
    
    public String getPrefix(){
        return prefix;
    }
    
    public void setXMLContentAttributes(XMLContentAttributes attr) {
        this.contentAttr = attr;
    }
    
    public XMLContentAttributes getXMLContentAttributes() {
        return contentAttr;
    }
    
     public void setPrimarySchema(String fileName) {
        this.primarySchemaFileName = fileName;
    }
    
    public String getPrimarySchema(){
        return primarySchemaFileName;
    }
   
}
