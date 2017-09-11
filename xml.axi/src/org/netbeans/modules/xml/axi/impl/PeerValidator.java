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

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.visitor.DefaultVisitor;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;

/**
 * PeerValidator validates the peer in an AXIComponent.
 * It is possible that the code generator, sets arbitrary peer values
 * for various AXIComponent. AXI sync should treat those components as
 * invalid. For example if there was an ElementImpl but the peer was found
 * as an ElementReference then that ElementImpl is bad.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class PeerValidator extends DefaultVisitor {

    private boolean result = true;
        
    /**
     * Creates a new instance of PeerValidator
     */
    public PeerValidator() {
    }
    
    public boolean validate(AXIComponent component) {
        result = true;
        component.accept(this);
        return result;
    }
    
    public void visit(AXIDocument root) {
        if(! (root.getPeer() instanceof Schema) )
            result = false;
    }
    
    public void visit(Element element) {
        SchemaComponent peer = element.getPeer();        
        if(element instanceof ElementImpl) {
            if( !(peer instanceof GlobalElement) &&
                !(peer instanceof LocalElement) )
                result = false;
        }
        if(element instanceof ElementRef) {
            if( !(peer instanceof ElementReference) )
                result = false;
        }        
    }
    
    public void visit(AnyElement element) {
        if(! (element.getPeer() instanceof org.netbeans.modules.xml.schema.model.AnyElement) )
            result = false;
    }
    
    public void visit(Attribute attribute) {        
        SchemaComponent peer = attribute.getPeer();        
        if(attribute instanceof AttributeImpl) {
            if( !(peer instanceof GlobalAttribute) &&
                !(peer instanceof LocalAttribute) )
                result = false;
        }
        if(attribute instanceof AttributeRef) {
            if( !(peer instanceof AttributeReference) )
                result = false;
        }        
    }
        
    public void visit(AnyAttribute attribute) {        
        if(! (attribute.getPeer() instanceof org.netbeans.modules.xml.schema.model.AnyAttribute) )
            result = false;
    }
    
    public void visit(Compositor compositor) {
        SchemaComponent peer = compositor.getPeer();
        if( !(peer instanceof Sequence) &&
            !(peer instanceof Choice) &&
            !(peer instanceof All) )
            result = false;
    }
    
    public void visit(ContentModel contentModel) {
        SchemaComponent peer = contentModel.getPeer();
        if( !(peer instanceof GlobalComplexType) &&
            !(peer instanceof GlobalGroup) &&
            !(peer instanceof GlobalAttributeGroup) )
            result = false;
    }    
}
