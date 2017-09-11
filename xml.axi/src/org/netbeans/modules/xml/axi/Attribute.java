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

import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.datatype.StringType;
import org.netbeans.modules.xml.axi.impl.DatatypeBuilder;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Attribute.Use;

/**
 * Represents an attribute in XML Schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class Attribute extends AbstractAttribute implements AXIType {
	
    /**
     * Creates a new instance of Attribute
     */
    public Attribute(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Attribute
     */
    public Attribute(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }

    /**
     * Creates a proxy for this Attribute.
     */
    public Attribute(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Attribute.
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
    public abstract Attribute getReferent();
    
    /**
     * Sets the name.
     */
    public abstract void setName(String name);
        
    /**
     * Returns the type. This is expensive, since it uses a visitor
     * to traverse to obtain the type information.
     */    
    public abstract AXIType getType();
    
    /**
     * Sets the type.
     */
    public abstract void setType(AXIType type);
    	
    /**
     * Returns the form.
     */
    public abstract Form getForm();
    
    /**
     * Sets the form.
     */
    public abstract void setForm(Form form);
    
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
     * Returns the use.
     */
    public abstract Use getUse();
    
    /**
     * Sets the use.
     */
    public abstract void setUse(Use use);
        
    /**
     * Returns the string representation of this Attribute.
     */
    public String toString() {        
        return getName();              //NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    protected String name;
    protected Form form;
    protected Use use;
    protected String defaultValue;
    protected String fixedValue;
    protected AXIType datatype;

    ////////////////////////////////////////////////////////////////////
    ////////////////// Properties for firing events ////////////////////
    ////////////////////////////////////////////////////////////////////
    public static final String PROP_NAME            = "name"; //NOI18N
    public static final String PROP_FORM            = "form"; //NOI18N
    public static final String PROP_USE             = "use"; //NOI18N
    public static final String PROP_DEFAULT         = "default"; //NOI18N
    public static final String PROP_FIXED           = "fixed"; //NOI18N
    public static final String PROP_TYPE            = "type"; //NOI18N    
    public static final String PROP_ATTRIBUTE_REF   = "attributeRef"; // NOI18N
}
