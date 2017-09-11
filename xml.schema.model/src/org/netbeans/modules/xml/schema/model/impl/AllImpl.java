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

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Occur;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This class implements the xml schema all type. The all
 * type describes an unordered group of elements.
 *
 * @author nn136682
 */
public class AllImpl extends SchemaComponentImpl implements All {
    
    public AllImpl(SchemaModelImpl model) {
	this(model,createNewComponent(SchemaElements.ALL, model));
    }
    
    /** Creates a new instance of AllImpl */
    public AllImpl(SchemaModelImpl model, Element e) {
	super(model, e);
    }
    
    public Class<? extends SchemaComponent> getComponentType() {
	return All.class;
    }
    
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
    }
    
    protected Class getAttributeType(SchemaAttributes attr) {
	switch(attr) {
	    case MIN_OCCURS:
		return Occur.ZeroOne.class;
	    default:
		return super.getAttributeType(attr);
	}
    }
    
    
    /**
     * @return minimum occurrences, must be 0 <= x <= 1
     */
    public Occur.ZeroOne getMinOccurs() {
	String s = super.getAttribute(SchemaAttributes.MIN_OCCURS);
	return s == null ? null : Util.parse(Occur.ZeroOne.class, s);
    }
    
    /**
     * set the minimum number of occurs.
     * @param occurs must satisfy 0 <= occurs <= 1
     */
    public void setMinOccurs(Occur.ZeroOne occurs) {
	setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, occurs);
    }
    
    public Occur.ZeroOne getMinOccursDefault() {
	return Occur.ZeroOne.ONE;
    }
    
    public Occur.ZeroOne getMinOccursEffective() {
	Occur.ZeroOne v = getMinOccurs();
	return v == null ? getMinOccursDefault() : v;
    }
    
    public Collection<LocalElement> getElements() {
	return super.getChildren(LocalElement.class);
    }
    
    public void addElement(LocalElement e) {
	appendChild(ELEMENT_PROPERTY, e);
    }
    
    public void removeElement(LocalElement e) {
	removeChild(ELEMENT_PROPERTY, e);
    }
    
    public Collection<ElementReference> getElementReferences() {
	return super.getChildren(ElementReference.class);
    }
    
    public void addElementReference(ElementReference e) {
	appendChild(ELEMENT_REFERENCE_PROPERTY, e);
    }
    
    public void removeElementReference(ElementReference e) {
	removeChild(ELEMENT_REFERENCE_PROPERTY, e);
    }
    
    public boolean allowsFullMultiplicity() {
	return !(getParent() instanceof GlobalGroup);
    }
}
