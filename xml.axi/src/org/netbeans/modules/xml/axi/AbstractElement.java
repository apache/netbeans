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
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Abstract element.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractElement extends AXIContainer {
    
    /**
     * Creates a new instance of Element
     */
    public AbstractElement(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Element
     */
    public AbstractElement(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this Element.
     */
    public AbstractElement(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Element.
     */
    public abstract void accept(AXIVisitor visitor);
            
    /**
     * Returns the MinOccurs.
     */
    public String getMinOccurs() {
        return minOccurs;
    }
    
    /**
     * Sets the MinOccurs.
     */
    public void setMinOccurs(String value) {        
        String oldValue = getMinOccurs();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.minOccurs = value;
        firePropertyChangeEvent(PROP_MINOCCURS, oldValue, value);
    }
	
    /**
     * Returns the MaxOccurs.
     */
    public String getMaxOccurs() {
        return maxOccurs;
    }
    
    /**
     * Sets the MaxOccurs.
     */
    public void setMaxOccurs(String value) {        
        String oldValue = getMaxOccurs();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.maxOccurs = value;
        firePropertyChangeEvent(PROP_MAXOCCURS, oldValue, value);
    }
	
    /**
     * true if #getMaxOccurs() and #getMinOccurs() allow multiciplity outside
     * [0,1], false otherwise. This method is only accurate after the element
     * has been inserted into the model.
     */
    public boolean allowsFullMultiplicity() {
		return !(getParent() instanceof Compositor && 
				((Compositor)getParent()).getType() == Compositor.CompositorType.ALL);
    }	
    
    protected String minOccurs = "1";
    protected String maxOccurs = "1";
    
    public static final String PROP_MINOCCURS     = "minOccurs"; // NOI18N
    public static final String PROP_MAXOCCURS     = "maxOccurs"; // NOI18N
    public static final String PROP_ELEMENT       = "element"; // NOI18N
}
