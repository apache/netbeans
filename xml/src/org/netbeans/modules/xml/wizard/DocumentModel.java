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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
