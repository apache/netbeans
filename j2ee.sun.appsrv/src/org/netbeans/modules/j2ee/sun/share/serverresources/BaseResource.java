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
/*
 * BaseResource.java
 *
 * Created on August 10, 2005, 5:34 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

import java.beans.*;

import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;


/**
 *
 * @author Nitya Doraisamy
 */
public class BaseResource extends Object implements java.io.Serializable {

    protected String name;
    protected String description;
    protected NameValuePair[] extraParams;
    
    transient protected PropertyChangeSupport propertySupport;
    
    /** Creates a new instance of BaseResource */
    public BaseResource() {
        propertySupport = new PropertyChangeSupport(this);
    }
    
    protected void initPropertyChangeSupport(){
        if(propertySupport==null){
            propertySupport = new PropertyChangeSupport ( this );
        }

    }
    
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        initPropertyChangeSupport();
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        initPropertyChangeSupport();
        propertySupport.removePropertyChangeListener (listener);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String value) {
        String oldValue = name;
        this.name = value;
        initPropertyChangeSupport();  
        propertySupport.firePropertyChange ("name", oldValue, name);//NOI18N
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        String oldValue = description;
        this.description = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("description", oldValue, description);//NOI18N
    }
    
    public NameValuePair[] getExtraParams() {
        if(this.extraParams == null){
            this.extraParams = new NameValuePair[0];   
        }
        return this.extraParams;
    }
    public void setExtraParams(Object[] value) {
        NameValuePair[] pairs = new NameValuePair[value.length];
        for (int i = 0; i < value.length; i++) {
            NameValuePair val = (NameValuePair)value[i];
            NameValuePair pair = new NameValuePair();
            pair.setParamName(val.getParamName());
            pair.setParamValue(val.getParamValue());
            //pair.setParamDescription(val.getParamDescription());
            pairs[i] = pair;
        }
        NameValuePair[] oldValue = extraParams;
        this.extraParams = pairs;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("extraParams", oldValue, extraParams);//NOI18N
    }  
    
    public Resources getResourceGraph(){
        return DDProvider.getDefault().getResourcesGraph(Resources.VERSION_1_3);
    }
    
    public PropertyElement populatePropertyElement(PropertyElement prop, NameValuePair pair){
        prop.setName(pair.getParamName()); 
        prop.setValue(pair.getParamValue()); 
        return prop;
    }
}
