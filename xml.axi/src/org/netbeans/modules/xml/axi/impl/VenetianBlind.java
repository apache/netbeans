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
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.schema.model.*;

/**
 *
 * @author Ayub Khan
 */
public class VenetianBlind extends GardenOfEden {
    
    /**
     * Creates a new instance of VenetianBlind
     */
    public VenetianBlind(SchemaGenerator.Mode mode) {
        super(mode);
    }
    
    protected SchemaGenerator.Pattern getSchemaDesignPattern() {
        return SchemaGenerator.Pattern.VENITIAN_BLIND;
    }
    
    public void visit(Element element) {
        prepareLocalElement(element);
    }
    
    protected void setPeer(final Element element,
            final org.netbeans.modules.xml.schema.model.Element e,
            final ElementReference eref) {
        if(element.getPeer() != null && element.getChildren().size() > 0 &&
                SchemaGeneratorUtil.isGlobalElement(element)) {
            LocalType lct = SchemaGeneratorUtil.getLocalComplexType(e);
            if(lct == null)
                lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
            assert lct != null;
            scParent = lct;
        } else
            super.setPeer(element, e, eref);
    }
    
    protected SchemaComponent getParent(
            AXIComponent axiparent) throws IllegalArgumentException {
        SchemaComponent scParent = null;
        if(axiparent instanceof Element &&
                (SchemaGeneratorUtil.isGlobalElement(axiparent) /*||
                                        isSimpleElementStructure((Element)axiparent)*/)) {
            SchemaComponent e = axiparent.getPeer();
            if(e instanceof ElementReference)
                e = ((ElementReference)e).getRef().get();
            assert e != null;
            SchemaComponent lct = SchemaGeneratorUtil.getLocalComplexType(e);
            if(lct == null) {
                lct = SchemaGeneratorUtil.getGlobalComplexType(e);
                if(lct == null)
                    lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
            }
            assert lct != null;
            scParent = lct;
        } else {
            scParent = super.getParent(axiparent);
        }
        return scParent;
    }
}
