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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;
/**
 *
 * @author rico
 */
public class ComplexContentImpl extends SchemaComponentImpl implements ComplexContent{
    
    /** Creates a new instance of ComplexContentImpl */
    protected ComplexContentImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.COMPLEX_CONTENT, model));
    }
    
    public ComplexContentImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return ComplexContent.class;
	}
    
    public void setMixed(Boolean mixed) {
        setAttribute(MIXED_PROPERTY, SchemaAttributes.MIXED, mixed);
    }
    
    public Boolean isMixed() {
//        return Boolean.parseBoolean(getAttribute(SchemaAttributes.MIXED));
        String s = getAttribute(SchemaAttributes.MIXED);
        return s == null ? null : Boolean.valueOf(s);
    }
    
    public void setLocalDefinition(ComplexContentDefinition definition) {
        if(definition == null){
            throw new IllegalArgumentException("ComplexContentDefinition is null");
        }
        List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        setChild(ComplexContentDefinition.class, LOCAL_DEFINITION_PROPERTY, definition, list);
    }
    
    public ComplexContentDefinition getLocalDefinition() {
        Collection<ComplexContentDefinition> elements = getChildren(ComplexContentDefinition.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        //TODO should we throw exception if there is no definition?
        return null;
    }
    
    /**
     * Visitor providing
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    public boolean getMixedEffective() {
        Boolean v = isMixed();
        return v == null ? getMixedDefault() : v;
    }

    /*
     * The effective mixed be the appropriate case among the following:
     * 1.1 If the mixed [attribute] is present on <complexContent>, then its actual value
     * 1.2 If the mixed [attribute] is present on <complexType>, then its actual value
     * 1.3 otherwise false.
     */
    public boolean getMixedDefault() {
        //TODO check getParent().getMixedDefault()
        return false;
    }
}
