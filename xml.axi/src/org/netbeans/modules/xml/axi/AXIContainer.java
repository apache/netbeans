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
package org.netbeans.modules.xml.axi;

import java.util.List;
import org.netbeans.modules.xml.axi.ContentModel.ContentModelType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.util.NbBundle;

/**
 * Represents a named component that can contain attributes,
 * for example an Element or a ContentModel. 
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AXIContainer extends AXIComponent {
    
    /**
     * Creates a new instance of AXIContainer.
     */
    public AXIContainer(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of AXIContainer.
     */
    public AXIContainer(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this AXIContainer.
     */
    public AXIContainer(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        if(name != null)
            return name;
        
        if(this instanceof Element)
            return NbBundle.getMessage(AXIContainer.class, "Unnamed-Element");
        
        if(this instanceof ContentModel) {
            ContentModelType type = ((ContentModel)this).getType();            
            switch (type) {
                case COMPLEX_TYPE:
                    return NbBundle.getMessage(AXIContainer.class, "Unnamed-ComplexType");
                case GROUP:
                    return NbBundle.getMessage(AXIContainer.class, "Unnamed-Group");
                case ATTRIBUTE_GROUP:
                    return NbBundle.getMessage(AXIContainer.class, "Unnamed-AttributeGroup");
            }
        }
        
        return NbBundle.getMessage(AXIContainer.class, "Unnamed-Component");
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
     * Adds a Compositor as its child.
     * Compositor must always be at the 0th index.
     */
    public void addCompositor(Compositor compositor) {
        insertAtIndex(Compositor.PROP_COMPOSITOR, compositor, 0);
    }
    
    /**
     * Removes a Compositor.
     */
    public void removeCompositor(Compositor compositor) {
        removeChild(Compositor.PROP_COMPOSITOR, compositor);
    }
    
    /**
     * Adds an Element as its child.
     * If attributes exist, add the new child before all attributes.
     * Attributes must always be added at the end of the list.
     */
    public void addElement(AbstractElement child) {
        if(this instanceof Element) {
            AXIType type = ((Element)this).getType();
            if(type != null && type instanceof ContentModel) {
                ((ContentModel)type).addElement(child);
                return;
            }
        }
        
        //if compositor does not exist, add one.
        Compositor c = getCompositor();
        if(c == null) {
            c = getModel().getComponentFactory().createSequence();
            addCompositor(c);
        }
        //add element to the compositor
        c.appendChild(AbstractElement.PROP_ELEMENT, child);
    }
    
    /**
     * Removes an Element.
     */
    public void removeElement(AbstractElement element) {
        removeChild(AbstractElement.PROP_ELEMENT, element);
    }
    
    /**
     * Adds an attribute.
     */
    public void addAttribute(AbstractAttribute attribute) {
        appendChild(AbstractAttribute.PROP_ATTRIBUTE, attribute);
    }
    
    /**
     * Removes an attribute.
     */
    public void removeAttribute(AbstractAttribute attribute) {
        removeChild(AbstractAttribute.PROP_ATTRIBUTE, attribute);
    }
    
    /**
     * Returns the compositor.
     */
    public Compositor getCompositor() {
        for(AXIComponent child: getChildren()) {
            if(Compositor.class.isAssignableFrom(child.getClass()))
                return (Compositor)child;
        }        
        return null;
    }
    
    /**
     * Returns the list of attributes.
     */
    public final List<AbstractAttribute> getAttributes() {
        return getChildren(AbstractAttribute.class);
    }
    
    protected String name;
	
    public static final String PROP_NAME          = "name"; // NOI18N
}
