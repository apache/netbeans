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
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

/**
 * An AXI model can be created with few schema componnets. This class must
 * define a set of visit methods for those components and all the builder
 * implementation must implement those.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractModelBuilder extends DeepSchemaVisitor {
    
    public AbstractModelBuilder(AXIModelImpl model) {
        this.model = model;
    }
    
    public AXIModelImpl getModel() {
        return model;
    }    
    
    public abstract void visit(Schema schema);
    
    public abstract void visit(AnyElement schemaComponent);
    
    public abstract void visit(AnyAttribute schemaComponent);
    
    public abstract void visit(GlobalElement schemaComponent);
    
    public abstract void visit(LocalElement component);
    
    public abstract void visit(ElementReference component);
    
    public abstract void visit(GlobalAttribute schemaComponent);
    
    public abstract void visit(LocalAttribute component);
    
    public abstract void visit(AttributeReference component);
    
    public abstract void visit(Sequence component);
    
    public abstract void visit(Choice component);
    
    public abstract void visit(All component);
    
    public abstract void visit(GlobalGroup schemaComponent);
    
    public abstract void visit(GroupReference component);
    
    public abstract void visit(GlobalAttributeGroup schemaComponent);
    
    public abstract void visit(AttributeGroupReference component);
    
    public abstract void visit(GlobalComplexType schemaComponent);
    
    public abstract void visit(LocalComplexType component);
    
    public abstract void visit(ComplexContent component);
    
    public abstract void visit(SimpleContent component);
    
    public abstract void visit(SimpleExtension component);
    
    public abstract void visit(ComplexExtension component);

    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    protected AXIModelImpl model;    
}
