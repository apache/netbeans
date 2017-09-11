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
package org.netbeans.modules.xml.axi.util;

import java.io.IOException;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
//import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 *
 * @author Samaresh
 */
public class SimulationHelper {
    
    private AXIModel model;
    
    /**
     * Creates a new instance of SimulationHelper
     */
    public SimulationHelper(AXIModel model) {
        this.model = model;
    }
        
    public Element dropGlobalElement(String name) {
        AXIDocument doc = model.getRoot();
        model.startTransaction();
        Element e = model.getComponentFactory().createElement();
        e.setName(name);
        doc.addElement(e);
        model.endTransaction();
        return e;
    }
    
    public ContentModel dropGlobalComplexType(String name) {
        AXIDocument doc = model.getRoot();
        model.startTransaction();
        ContentModel cm = model.getComponentFactory().createComplexType();
        cm.setName(name);
        doc.addContentModel(cm);
        model.endTransaction();
        return cm;
    }
    
    public Element dropElement(AXIContainer parent, String name) {
        model.startTransaction();
        Element element = model.getComponentFactory().createElement();
        element.setName(name);
        parent.addElement(element);
        model.endTransaction();
        return element;
    }
    
    public void setElementType(Element e, AXIType type) {
        model.startTransaction();
        e.setType(type);
        model.endTransaction();
    }
    
    public void delete(AXIComponent child) {
        model.startTransaction();
        child.getParent().removeChild(child);
        model.endTransaction();
    }    
    
    public void clearAll() {
        model.startTransaction();
        model.getRoot().removeAllChildren();
        model.endTransaction();
    }    
        
    public Compositor dropCompositor(AXIContainer parent, CompositorType type) {
        model.startTransaction();
        Compositor c = getCompositor(type);
        parent.addCompositor(c);
        model.endTransaction();
        return c;
    }
    
    public Attribute dropAttribute(AXIContainer parent, String name) {
        model.startTransaction();
        Attribute a = model.getComponentFactory().createAttribute();
        a.setName(name);
        parent.addAttribute(a);
        model.endTransaction();
        return a;
    }
     
    public Element dropElementOnCompositor(Compositor c, String name) {
        model.startTransaction();
        Element e = model.getComponentFactory().createElement();
        e.setName(name);
        c.addElement(e);
        model.endTransaction();
        return e;
    }

    public void dropChildAtIndex(AXIComponent parent, AXIComponent child, int i) {
        model.startTransaction();
        parent.addChildAtIndex(child, i);
        model.endTransaction();
    }

    public void setCompositorType(Compositor c, CompositorType type) {
        //model.startTransaction();
        c.setType(type);
        //model.endTransaction();
    }
    
    private Compositor getCompositor(CompositorType type) {
        Compositor c = null;
        switch(type) {
            case SEQUENCE:
                c = model.getComponentFactory().createSequence();
                break;
            case CHOICE:
                c = model.getComponentFactory().createChoice();
                break;
            case ALL:
                c = model.getComponentFactory().createAll();
                break;
        }
        
        return c;
    }
    
    /**
     * Checks if a component is part of an AXI model.
     * Returns true if it has a valid parent and model, false otherwise.
     */
    public boolean inModel(AXIComponent c) {
	//for everything else, both parent and model should be valid
        return ( (c.getParent() != null) && (c.getModel() != null) && (c.getPeer() != null));
    }
    
//    public boolean refactorRename(AXIContainer container, String name) {
//        assert(container.getParent() instanceof AXIDocument);
//        NamedReferenceable ref = null;
//        SchemaComponent comp = container.getPeer();
//        if (comp instanceof NamedReferenceable) {
//            ref = NamedReferenceable.class.cast(comp);
//        }
//        
//        
//        try {
//            SchemaModel sm = model.getSchemaModel();
//            SharedUtils.locallyRenameRefactor((Nameable)ref, name);
//            model.sync();
//        } catch (IOException ex) {
//            return false;
//        }
//        return true;
//    }    

}
