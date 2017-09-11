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

import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Any.ProcessContents;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Represents anyAttribute in XML Schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AnyAttribute extends AbstractAttribute {
    
    /**
     * Creates a new instance of AnyAttribute
     */
    public AnyAttribute(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of AnyAttribute
     */
    public AnyAttribute(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a new instance of AnyAttributeProxy
     */
    public AnyAttribute(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }    
    
    /**
     * Allows a visitor to visit this Attribute.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        return "anyAttribute"; //NOI18N
    }    

    /**
     * Returns the processContents.
     */
    public ProcessContents getProcessContents() {
        return processContents;
    }
    
    /**
     * Sets the processContents.
     */
    public void setProcessContents(ProcessContents value) {
        ProcessContents oldValue = getProcessContents();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue == value) ) {
            return;
        }
        this.processContents = value;
        firePropertyChangeEvent(PROP_PROCESSCONTENTS, oldValue, value);
    }
    
    /**
     * Returns the target namespace.
     */
    public String getTargetNamespace() {
        return namespace;
    }

    /**
     * Sets the target namespace.
     */
    public void setTargetNamespace(String value) {
        String oldValue = getTargetNamespace();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.namespace = value;
        firePropertyChangeEvent(PROP_NAMESPACE, oldValue, value);
    }
    
    /**
     * String representation of this Element.
     */
    public String toString() {        
        return getName();
    }	
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    private String namespace;
    private ProcessContents processContents;
    
    ////////////////////////////////////////////////////////////////////
    ////////////////// Properties for firing events ////////////////////
    ////////////////////////////////////////////////////////////////////
    public static final String PROP_NAMESPACE         = "namespace"; // NOI18N
    public static final String PROP_PROCESSCONTENTS   = "processContents"; // NOI18N
}
