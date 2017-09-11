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

/*
 * AttributeImpl.java
 *
 * Created on May 5, 2006, 12:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.datatype.StringType;
import org.netbeans.modules.xml.axi.datatype.DatatypeFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Form;

/**
 * Base and only implementation of Attribute.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public final class AttributeImpl extends Attribute {
            
    /**
     * Creates a new instance of AttributeImpl
     */
    public AttributeImpl(AXIModel model) {
        super(model);
        setDefaultDataType();
    }
    
    /**
     * Creates a new instance of AttributeImpl
     */
    public AttributeImpl(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
        setDefaultDataType();
    }

    /**
     * Initializes the default datatype for this Attribute.
     * Do NOT call setType() here, that'll inturn call
     * getType(), which is expensive. Initialize instead.
     */
    private void setDefaultDataType() {        
        this.datatype = new StringType();        
    }
                
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public boolean isReference() {
        return false;
    }
    
    /**
     * Returns the referent if isReference() is true.
     */
    public Attribute getReferent() {
        return null;
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name.
     */
    public void setName(String name) {        
        String oldName = getName();
        if( (oldName == null && name == null) ||
            (oldName != null && oldName.equals(name)) ) {
            return;
        }
        this.name = name;
        firePropertyChangeEvent(PROP_NAME, oldName, name);
    }    
        
    /**
     * Returns the type. This is expensive, since it uses a visitor
     * to traverse to obtain the type information.
     */    
    public AXIType getType() {
        if(!datatypeInitialized && getPeer() != null) {            
            datatype = DatatypeFactory.getDefault().getDatatype(getModel(), getPeer());
            datatypeInitialized = true;
        }
        
        return datatype;
    }
    
    /**
     * Sets the type.
     */
    public void setType(AXIType value) {
        if( (this == value) ||
            (this.isGlobal() && (value instanceof Attribute)) )
            return;
        
        if(value instanceof Attribute) {
            setAttributeAsType(value);
            return;
        }
        
        AXIType oldValue = getType();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.datatype = value;
        firePropertyChangeEvent(PROP_TYPE, oldValue, value);
    }
    	
    private void setAttributeAsType(final AXIType newValue) {
        if(newValue == this)
            return;
        int index = this.getIndex();
        AXIComponent parent = getParent();
        Attribute ref = getModel().getComponentFactory().createAttributeReference((Attribute)newValue);
        parent.removeChild(this);
        parent.insertAtIndex(Attribute.PROP_ATTRIBUTE_REF, ref, index);
    }
    
    /**
     * Returns the form.
     */
    public Form getForm() {
        return form;
    }
    
    /**
     * Sets the form.
     */
    public void setForm(Form value) {        
        Form oldValue = getForm();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue == value) ) {
            return;
        }
        this.form = value;
        firePropertyChangeEvent(PROP_FORM, oldValue, value);
    }
    
    /**
     * Returns the fixed value.
     */
    public String getFixed() {
        return fixedValue;
    }
    
    /**
     * Sets the fixed value.
     */
    public void setFixed(String value) {        
        String oldValue = getFixed();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.fixedValue = value;
        firePropertyChangeEvent(PROP_FIXED, oldValue, value);
    }
    
    /**
     * Returns the default value.
     */
    public String getDefault() {
        return defaultValue;
    }
    
    /**
     * Sets the default value.
     */
    public void setDefault(String value) {        
        Object oldValue = getDefault();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.defaultValue = value;
        firePropertyChangeEvent(PROP_DEFAULT, oldValue, value);
    }
    
    /**
     * Returns the use.
     */
    public Use getUse() {
        return use;
    }
    
    /**
     * Sets the use.
     */
    public void setUse(Use value) {        
        Use oldValue = getUse();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue == value) ) {
            return;
        }
        this.use = value;
        firePropertyChangeEvent(PROP_USE, oldValue, value);
    }    
    
    
    private boolean datatypeInitialized;
}
