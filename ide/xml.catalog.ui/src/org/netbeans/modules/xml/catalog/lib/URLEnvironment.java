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
package org.netbeans.modules.xml.catalog.lib;

import java.io.*;
import java.net.*;
import java.util.*;

import org.openide.text.*;
import org.openide.ErrorManager;


/**
 * Defines numb read-only URL environment but finding CloneableOpenSupport (outerclass).
 * It hardcodes <code>text/xml</code> MIME type.
 *
 * @author  Petr Kuzel
 * @version
 */
public abstract class URLEnvironment implements CloneableEditorSupport.Env {

    /** Serial Version UID */
    private static final long serialVersionUID =9098933339895727443L;
    private final String publicId;
    private final String systemId;    
    private transient Date modified;
        
    /** Creates new StreamEnvironment */
    public URLEnvironment(String publicId, String systemId) {
        if (systemId == null) throw new NullPointerException();
        this.publicId = publicId;
        this.systemId = systemId;
        modified = new Date();
    }
        
    public void markModified() throws java.io.IOException {
        throw new IOException("r/o"); // NOI18N
    }    
    
    public void unmarkModified() {
    }    

    public void removePropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener) {
    }
    
    public boolean isModified() {
        return false;
    }
    
    public java.util.Date getTime() {
        return modified;
    }
    
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener vetoableChangeListener) {
    }
    
    public boolean isValid() {
        return true;
    }
    
    public java.io.OutputStream outputStream() throws java.io.IOException {
        throw new IOException("r/o"); // NOI18N
    }

    /**
     * @return "text/xml" for XML Schemas
     * @return "application/xml-dtd" for DTDs
     */
    public java.lang.String getMimeType() {
        //fair assumption I guess
        if (publicId != null &&
           (publicId.toLowerCase().startsWith("schema:") || // NOI18N
            publicId.toLowerCase().endsWith(".xsd:")) ) // NOI18N
            return "text/xml"; // NOI18N
        
        return "application/xml-dtd";  // NOI18N        
    }

    /**
     * Always return fresh stream.
     */
    public java.io.InputStream inputStream() throws java.io.IOException {
        try {
            URL peer = new URL(systemId);
            return peer.openStream();
        } catch (IOException ex) {
            // #21556
            // annotate exception as USER error, he provided wrong URL
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(ex, ErrorManager.USER, null, null, null, null);
            throw ex;
        }
    }
    
    public void addVetoableChangeListener(java.beans.VetoableChangeListener vetoableChangeListener) {
    }
    
    public void addPropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener) {
    }
            
}
