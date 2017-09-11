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
package org.netbeans.modules.xml.schema.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.FindReferredConstraintVisitor;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
/**
 *
 * @author Vidhya Narayanan
 * @author rico
 */
public class KeyRefImpl extends ConstraintImpl implements KeyRef {
    
    public KeyRefImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.KEYREF,model));
    }
    
    /**
     * Creates a new instance of KeyRefImpl
     */
    public KeyRefImpl(SchemaModelImpl model, org.w3c.dom.Element el) {
        super(model, el);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
        return KeyRef.class;
    }
    
    /**
     *
     */
    public void setReferer(Constraint c) {
        this.setAttribute(REFERER_PROPERTY, SchemaAttributes.REFER,
                c==null?null:new ConstraintWrapper(c));
    }
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    
    /**
     *
     */
    public Constraint getReferer() {
        String referValue = this.getAttribute(SchemaAttributes.REFER);
        if(referValue == null)
             return null;
        //remove prefix, if any
        String localName = getLocalName(referValue);
        SchemaComponent parent = findOutermostParentElement();
        FindReferredConstraintVisitor visitor = 
                new FindReferredConstraintVisitor();
        
        return visitor.findReferredConstraint(parent, localName);
    }
    
    /**
     * Adapter class to enable the use of Constraint in setAttribute()
     */
    private static class ConstraintWrapper{
        private Constraint c;
        
        public ConstraintWrapper(Constraint c){
            this.c = c;
        }
        
        public String toString(){
            return c.getName();
        }
    }
    
    private String getLocalName(String uri) {
        String localName = null;
        try {
            URI u = new URI(uri);
            localName = u.getSchemeSpecificPart();
        } catch (URISyntaxException ex) {
        }
        return localName;
    }
    
    /**
     * Look for the outermost <element> that encloses this keyRef. This is 
     * required to determine the effective scope where valid keys and uniques
     * may be obtained. That is, the refer attribute may only refer to keys or
     * uniques that are contained within the same element scope.
     */
    private SchemaComponent findOutermostParentElement(){
        SchemaComponent element = null;
        //go up the tree and look for the last instance of <element>
	SchemaComponent sc = getParent();
        while(sc != null){
            if(sc instanceof Element){
                element = sc;
            }
	    sc = sc.getParent();
        }
        return element;
    }
}
