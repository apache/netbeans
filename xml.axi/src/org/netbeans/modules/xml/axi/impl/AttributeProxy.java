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

package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Form;

/**
 * Proxy attribute, acts on behalf of an Attribute.
 * Delegates all calls to the original attribute.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AttributeProxy extends Attribute implements AXIComponentProxy {
   
    /**
     * Creates a new instance of AttributeProxy
     */
    public AttributeProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    private Attribute getShared() {
        return (Attribute)getSharedComponent();
    }
    
    /**
     * Returns the type of this component,
     * may be local, shared, proxy or reference.
     * @see ComponentType.
     */
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
    
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public boolean isReference() {
        return getShared().isReference();
    }
    
    /**
     * Returns the referent if isReference() is true.
     */
    public Attribute getReferent() {
        return getShared().getReferent();
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        return getShared().getName();
    }
    
    /**
     * Sets the name.
     */
    public void setName(String name) {
        getShared().setName(name);
    }    
        
    /**
     * Returns the type. This is expensive, since it uses a visitor
     * to traverse to obtain the type information.
     */    
    public AXIType getType() {
        return getShared().getType();
    }
    
    /**
     * Sets the type.
     */
    public void setType(AXIType datatype) {
        getShared().setType(datatype);
    }
    	
    /**
     * Returns the form.
     */
    public Form getForm() {
        return getShared().getForm();
    }
    
    /**
     * Sets the form.
     */
    public void setForm(Form form) {
        getShared().setForm(form);
    }
    
    /**
     * Returns the fixed value.
     */
    public String getFixed() {
        return getShared().getFixed();
    }
    
    /**
     * Sets the fixed value.
     */
    public void setFixed(String value) {
        getShared().setFixed(value);        
    }
    
    /**
     * Returns the default value.
     */
    public String getDefault() {
        return getShared().getDefault();
    }
    
    /**
     * Sets the default value.
     */
    public void setDefault(String value) {
        getShared().setDefault(value);
    }
    
    /**
     * Returns the use.
     */
    public Use getUse() {
        return getShared().getUse();
    }
    
    /**
     * Sets the use.
     */
    public void setUse(Use value) {
        getShared().setUse(value);
    }    
    
    /**
     * Proxy doesn't get refreshed in the UI. We must notify.
     */
    void forceFireEvent() {
        firePropertyChangeEvent(Attribute.PROP_NAME, null, getName());
    }
}
