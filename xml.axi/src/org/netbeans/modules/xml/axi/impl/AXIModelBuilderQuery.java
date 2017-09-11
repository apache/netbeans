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

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

/**
 * Helper class that exposes query-like APIs. Various queries can be made
 * on schema components such as whether or not the a component has any affect
 * on the AXI model OR whether or not a component can be viewed in the editor.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelBuilderQuery extends AbstractModelBuilder {
    
    public AXIModelBuilderQuery(AXIModelImpl model) {
        super(model);
    }
    
    /**
     * Returns true for all schema components that are viewable,
     * false otherwise. Not all schema components have corresponding AXI
     * components and not all AXI components are viewable.
     */
    public boolean canView(SchemaComponent schemaComponent) {
        canView = false;
        schemaComponent.accept(this);
        return canView;
    }
    
    /**
     * Returns true if the schema component has an impact on AXI model,
     * false otherwise. Not all schema components affects AXI model.
     */
    public boolean affectsModel(SchemaComponent schemaComponent) {
        affectsModel = false;
        schemaComponent.accept(this);
        return affectsModel;
    }
    
    public void visit(Schema schema) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(AnyElement schemaComponent) {
        affectsModel = true;
        canView = checkComponent(schemaComponent);
    }
    
    public void visit(AnyAttribute schemaComponent) {
        affectsModel = true;
        canView = checkComponent(schemaComponent);
    }
    
    public void visit(GlobalElement schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(LocalElement component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(ElementReference component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(GlobalAttribute schemaComponent) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(LocalAttribute component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(AttributeReference component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(Sequence component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(Choice component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(All component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(GlobalGroup schemaComponent) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GroupReference component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GlobalAttributeGroup schemaComponent) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(AttributeGroupReference component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GlobalComplexType schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(LocalComplexType component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(ComplexContent component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(SimpleContent component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(SimpleExtension component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(ComplexExtension component) {
        affectsModel = true;
        canView = false;
    }
    
    /**
     * If the component's top level parent is a complex type or an element
     * it'll be visible in design view, else no.
     * 
     * @param component
     * @return
     */
    private boolean checkComponent(SchemaComponent component) {
        if(component == null)
            return false;
        SchemaComponent parent = component;
        while(parent.getParent() != null && !(parent.getParent() instanceof Schema)) {
            parent = parent.getParent();
        }
        if((parent instanceof GlobalComplexType) || (parent instanceof GlobalElement))
            return true;
        
        return false;
    }
    

    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    private boolean affectsModel;
    private boolean canView;
}
