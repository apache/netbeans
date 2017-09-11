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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Ayub Khan
 */
public class GardenOfEden extends DefaultSchemaGenerator {
    
    /**
     * Creates a new instance of GardenOfEden
     */
    public GardenOfEden(SchemaGenerator.Mode mode) {
        super(mode);
    }
    
    protected SchemaGenerator.Pattern getSchemaDesignPattern() {
        return SchemaGenerator.Pattern.GARDEN_OF_EDEN;
    }
    
    public void visit(Element element) {
        prepareGlobalElement(element);
    }
    
    public void visit(Datatype d) {
        createGlobalSimpleType(d, sm, datatypeParent, id, pc);
    }
    
    protected void setPeer(final Element element,
            final org.netbeans.modules.xml.schema.model.Element e,
            final ElementReference eref) {
        if(getMode() != SchemaGenerator.Mode.TRANSFORM)
            super.setPeer(element, e, eref);
        else {
            if(element.getChildren().size() > 0) {
                GlobalComplexType gct = (GlobalComplexType) getRef(e);
                if(gct == null) {
                    GlobalComplexType foundgct = null;
                    //check type from another schema model
                    gct = SchemaGeneratorUtil.findTypeFromOtherModel(e, element, sm);
                    if(gct == null) {
                        String typeName = null;
                        if(element.getType() instanceof ContentModel) {
                            typeName = ((ContentModel)element.getType()).getName();
                            HashMap<String, SchemaComponent> map =
                                    namesMap.get(GlobalComplexType.class);
                            if(map != null && map.get(typeName) != null) {
                                foundgct = (GlobalComplexType) map.get(typeName);
                                addRef(e, foundgct);
                            }
                        }
                        if(typeName == null)
                            typeName = element.getName()+"Type";
                        gct = createGlobalComplexType(typeName);
                        if(foundgct != null)
                            SchemaGeneratorUtil.setType(e, foundgct);
                        else {
                            sgh.addComplexType(gct, -1);
                            SchemaGeneratorUtil.setType(e, gct);
                            addRef(e, gct);
                        }
                    }
                }
                assert gct != null;
                scParent = gct;
            } else
                scParent = e;
        }
    }
    
    protected SchemaComponent getParent(final AXIComponent axiparent)
    throws IllegalArgumentException {
        SchemaComponent scParent = null;
        if(axiparent instanceof Element){
            SchemaComponent e = axiparent.getPeer();
            if(e instanceof ElementReference) {
                e = getRef(e);
                assert e != null;
            }
            scParent = getRef(e);
            if(scParent == null) {
                scParent = SchemaGeneratorUtil.getLocalComplexType(e);
                if(scParent == null)
                    scParent = createPeerGlobalComplexType((Element)axiparent);
                addRef(e, (GlobalType) scParent);
            }
        } else
            scParent = super.getParent(axiparent);
        return scParent;
    }
}
