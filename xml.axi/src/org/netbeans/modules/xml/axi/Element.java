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
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Represents an Element in XML Schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class Element extends AbstractElement implements AXIType {
    
    /**
     * Creates a new instance of Element
     */
    public Element(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Element
     */
    public Element(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this Element.
     */
    public Element(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
        
    /**
     * Allows a visitor to visit this Element.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public abstract boolean isReference();
            
    /**
     * Returns the referent if isReference() is true.
     */
    public abstract Element getReferent();
    
    /**
     * Returns abstract property.
     */
    public abstract boolean getAbstract();
    
    /**
     * Sets the abstract property.
     */
    public abstract void setAbstract(boolean value);
    
    /**
     * Returns the block.
     */
    public abstract String getBlock();
        
    /**
     * Sets the block property.
     */
    public abstract void setBlock(String value);
    
    /**
     * Returns the final property.
     */
    public abstract String getFinal();
    
    /**
     * Sets the final property.
     */
    public abstract void setFinal(String value);
    
    /**
     * Returns the fixed value.
     */
    public abstract String getFixed();
    
    /**
     * Sets the fixed value.
     */
    public abstract void setFixed(String value);
    
    /**
     * Returns the default value.
     */
    public abstract String getDefault();
    
    /**
     * Sets the default value.
     */
    public abstract void setDefault(String value);
    
    /**
     * Returns the form.
     */
    public abstract Form getForm();
    
    /**
     * Sets the form.
     */
    public abstract void setForm(Form value);
        
    /**
     * Returns the nillable.
     */
    public abstract boolean getNillable();
    
    /**
     * Sets the nillable property.
     */
    public abstract void setNillable(boolean value);

    /**
     * used  by property editor
     */
    public Boolean isNillable() {
        return Boolean.valueOf(getNillable());
    }
	
    /**
     * used  by property editor
     */
    public void setNillable(Boolean nillable) {
        if(nillable != null)
            setNillable(nillable.booleanValue());
    }
	
    /**
     * Returns the complex type of this element, if there is one.
     * Null for element with simple type or anonymous type.
     */
    public abstract AXIType getType();
	
    /**
     * sets the type of this element.
     */
    public abstract void setType(AXIType type);	
        
    /**
     * String representation of this Element.
     */
    public String toString() {
        return getName();
    }
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    protected String finalValue;
    protected String fixedValue;
    protected String defaultValue;
    protected Form form;
    protected String block;
    protected boolean isAbstract;
    protected boolean isNillable;
    
    ////////////////////////////////////////////////////////////////////
    ////////////////// Properties for firing events ////////////////////
    ////////////////////////////////////////////////////////////////////
    public static final String PROP_FINAL         = "final"; // NOI18N
    public static final String PROP_FIXED         = "fixed"; // NOI18N
    public static final String PROP_DEFAULT       = "default"; // NOI18N
    public static final String PROP_FORM          = "form"; // NOI18N
    public static final String PROP_BLOCK         = "block"; // NOI18N
    public static final String PROP_ABSTRACT      = "abstract"; // NOI18N
    public static final String PROP_NILLABLE      = "nillable"; // NOI18N
    public static final String PROP_TYPE          = "type"; // NOI18N	
    public static final String PROP_ELEMENT_REF   = "elementRef"; // NOI18N
}
