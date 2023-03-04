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
 * SystemImpl.java
 *
 * Created on December 6, 2006, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.jaxws.catalog.impl;

import java.net.URI;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogAttributes;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogQNames;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author girix
 */
public class SystemImpl extends CatalogComponentImpl implements
        org.netbeans.modules.websvc.jaxws.catalog.System{
    
    public SystemImpl(CatalogModelImpl model, Element e) {
        super(model, e);
    }
    
    public SystemImpl(CatalogModelImpl model) {
        this(model, createElementNS(model, CatalogQNames.SYSTEM));
    }
    
    public void accept(CatalogVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getSystemIDAttr() {
        return getAttribute(CatalogAttributes.systemId);
    }
    
    public String getURIAttr() {
        return getAttribute(CatalogAttributes.uri);
    }
    
    public String getXprojectCatalogFileLocationAttr() {
        return getAttribute(CatalogAttributes.xprojectCatalogFileLocation);
    }
    
    public String getReferencingFileAttr() {
        return getAttribute(CatalogAttributes.referencingFile);
    }
    
    public void setSystemIDAttr(URI uri) {
        super.setAttribute(SYSTEMID_ATTR_PROP, CatalogAttributes.systemId,
                uri.toString());
    }
    
    public void setURIAttr(URI uri) {
        super.setAttribute(URI_ATTR_PROP, CatalogAttributes.uri,
                uri.toString());
    }
    
    public void setXprojectCatalogFileLocationAttr(URI uri) {
        super.setAttribute(XPROJECTREF_ATTR_PROP, 
                CatalogAttributes.xprojectCatalogFileLocation, uri.toString());
    }
    
    public void setReferencingFileAttr(URI uri) {
        super.setAttribute(REFFILE_ATTR_PROP, CatalogAttributes.referencingFile,
                uri.toString());
    }

}
