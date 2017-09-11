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

import java.util.Set;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalElement.Final;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 */
public class GlobalElementImpl extends ElementImpl implements GlobalElement {

    public GlobalElementImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.ELEMENT,model));
    }
    
    /**
     * Creates a new instance of GlobalElementImpl
     */
    public GlobalElementImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalElement.class;
	}
    
    /**
     *
     */
    public void setSubstitutionGroup(NamedComponentReference<GlobalElement> element) {
	setAttribute(SUBSTITUTION_GROUP_PROPERTY, SchemaAttributes.SUBSTITUTION_GROUP, element);
    }
    
    /**
     *
     */
    public NamedComponentReference<GlobalElement> getSubstitutionGroup() {
        return resolveGlobalReference(GlobalElement.class, SchemaAttributes.SUBSTITUTION_GROUP);
    }
    
    /**
     *
     */
    public void setAbstract(Boolean abstr) {
        setAttribute(ABSTRACT_PROPERTY, SchemaAttributes.ABSTRACT, abstr);
    }
    
    /**
     *
     */
    public Boolean isAbstract() {
        String s = getAttribute(SchemaAttributes.ABSTRACT);
        return s == null ? null : Boolean.valueOf(s);
    }
    

    public boolean getAbstractEffective() {
        Boolean v = isAbstract();
        return v == null ? getAbstractDefault() : v;
    }

    public boolean getAbstractDefault() {
        return false;
    }
    
    public void setFinal(Set<Final> finalValue) {
        setAttribute(FINAL_PROPERTY, SchemaAttributes.FINAL, 
                finalValue == null ? null : 
                    Util.convertEnumSet(Final.class, finalValue));
    }
    
    public Set<Final> getFinal() {
        String s = getAttribute(SchemaAttributes.FINAL);
        return s == null ? null : Util.valuesOf(Final.class, s);
    }
    
    public Set<Final> getFinalEffective() {
        Set<Final> v = getFinal();
        return v == null ? getFinalDefault() : v;
    }

    public Set<Final> getFinalDefault() {
        return Util.convertEnumSet(Final.class, getModel().getSchema().getFinalDefaultDefault());
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
 
    protected Class getAttributeMemberType(SchemaAttributes attr) {
        switch(attr) {
            case FINAL:
                return Final.class;
            default:
                return super.getAttributeMemberType(attr);
        }
    }
}
