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

import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.model.Form;

/**
 * Proxy element, acts on behalf of an Element.
 * Delegates all calls to the original element.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ElementProxy extends Element implements AXIComponentProxy {
                
    /**
     * Creates a new instance of ElementProxy
     */
    public ElementProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
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
    public Element getReferent() {
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
     * Returns the MinOccurs.
     */
    public String getMinOccurs() {
        return getShared().getMinOccurs();
    }
    
    /**
     * Sets the MinOccurs.
     */
    public void setMinOccurs(String value) {        
        getShared().setMinOccurs(value);
    }
	
    /**
     * Returns the MaxOccurs.
     */
    public String getMaxOccurs() {
        return getShared().getMaxOccurs();
    }
    
    /**
     * Sets the MaxOccurs.
     */
    public void setMaxOccurs(String value) {        
        getShared().setMaxOccurs(value);
    }
    
    /**
     * Returns abstract property.
     */
    public boolean getAbstract() {
        return getShared().getAbstract();
    }
    
    /**
     * Sets the abstract property.
     */
    public void setAbstract(boolean value) {
        getShared().setAbstract(value);
    }
    
    /**
     * Returns the block.
     */
    public String getBlock() {
        return getShared().getBlock();
    }
        
    /**
     * Sets the block property.
     */
    public void setBlock(String value) {
        getShared().setBlock(value);
    }
    
    /**
     * Returns the final property.
     */
    public String getFinal() {
        return getShared().getFinal();
    }
    
    /**
     * Sets the final property.
     */
    public void setFinal(String value) {
        getShared().setFinal(value);
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
     * Returns the form.
     */
    public Form getForm() {
        return getShared().getForm();
    }
    
    /**
     * Sets the form.
     */
    public void setForm(Form value) {
        getShared().setForm(value);
    }
        
    /**
     * Returns the nillable.
     */
    public boolean getNillable() {
        return getShared().getNillable();
    }
    
    /**
     * Sets the nillable property.
     */
    public void setNillable(boolean value) {
        getShared().setNillable(value);
    }
    
    /**
     * Adds a Compositor as its child.
     * Compositor must always be at the 0th index.
     */
    public void addCompositor(Compositor compositor) {
        getShared().addCompositor(compositor);
    }
    
    /**
     * Removes a Compositor.
     */
    public void removeCompositor(Compositor compositor) {
        getShared().removeCompositor(compositor);
    }
    
    /**
     * Adds an Element as its child.
     * If attributes exist, add the new child before all attributes.
     * Attributes must always be added at the end of the list.
     */
    public void addElement(AbstractElement child) {
        getShared().addElement(child);
    }
    
    /**
     * Removes an Element.
     */
    public void removeElement(AbstractElement element) {
        getShared().removeElement(element);
    }
    
    /**
     * Adds an attribute.
     */
    public void addAttribute(AbstractAttribute attribute) {
        getShared().addAttribute(attribute);
    }
    
    /**
     * Removes an attribute.
     */
    public void removeAttribute(AbstractAttribute attribute) {
        getShared().removeAttribute(attribute);
    }
    
    /**
     * gets the type of this element.
     */	
	public AXIType getType() {
		return getShared().getType();
	}
	
    /**
     * sets the type of this element.
     */
    public void setType(AXIType type) {
        getShared().setType(type);
    }
	
    /**
     * Returns the compositor.
     */
    public Compositor getCompositor() {
        return getShared().getCompositor();
    }    
    
    Element getShared() {
        return (Element)getSharedComponent();
    }
    
    /**
     * Proxy doesn't get refreshed in the UI. We must notify.
     */
    void forceFireEvent() {
        firePropertyChangeEvent(Element.PROP_NAME, null, getName());
    }
}
