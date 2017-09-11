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
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.axi.SchemaGeneratorFactory;
import org.netbeans.modules.xml.axi.SchemaGeneratorFactory.TransformHint;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author Ayub Khan
 */
public class SchemaGeneratorFactoryImpl extends SchemaGeneratorFactory {
    
    /**
     * Creates a new instance of SchemaGeneratorFactoryImpl
     */
    public SchemaGeneratorFactoryImpl() {
        super();
    }
    
    public SchemaGenerator.Pattern inferDesignPattern(AXIModel am) {
        return SchemaGeneratorUtil.inferDesignPattern(am);
    }
    
    public void updateSchema(SchemaModel sm, SchemaGenerator.Pattern pattern)
    throws BadLocationException, IOException {
        SchemaGenerator sg = null;
        if(pattern == SchemaGenerator.Pattern.GARDEN_OF_EDEN)
            sg = new GardenOfEden(SchemaGenerator.Mode.UPDATE);
        else if(pattern == SchemaGenerator.Pattern.VENITIAN_BLIND)
            sg = new VenetianBlind(SchemaGenerator.Mode.UPDATE);
        else if(pattern == SchemaGenerator.Pattern.SALAMI_SLICE)
            sg = new SalamiSlice(SchemaGenerator.Mode.UPDATE);
        else if(pattern == SchemaGenerator.Pattern.RUSSIAN_DOLL)
            sg = new RussianDoll(SchemaGenerator.Mode.UPDATE);
        else if(pattern == SchemaGenerator.Pattern.MIXED)
            sg = new MixedPattern(SchemaGenerator.Mode.UPDATE);
        
        if(sg != null)
            sg.updateSchema(sm);
    }
    
    public List<Element> findMasterGlobalElements(AXIModel am) {
        return SchemaGeneratorUtil.findMasterGlobalElements(am);
    }
    
    public TransformHint canTransformSchema(SchemaModel sm,
            SchemaGenerator.Pattern currentPattern,
            SchemaGenerator.Pattern targetPattern) {
        return canTransformSchema(sm, currentPattern, targetPattern, 
            findMasterGlobalElements(AXIModelFactory.getDefault().getModel(sm)));
    }
    
    public TransformHint canTransformSchema(SchemaModel sm,
            SchemaGenerator.Pattern currentPattern,
            SchemaGenerator.Pattern targetPattern, List<Element> ges) {
        if(sm == null) return TransformHint.INVALID_SCHEMA;
        AXIModel am = AXIModelFactory.getDefault().getModel(sm);
        
        if(am == null || am.getState() != Model.State.VALID ||
                am.getState() == Model.State.NOT_WELL_FORMED)
            return TransformHint.INVALID_SCHEMA;
        
        if(currentPattern == targetPattern)
            return TransformHint.SAME_DESIGN_PATTERN;

        if(currentPattern != null && targetPattern != null) {
            if(ges.size() == 0)
                return TransformHint.NO_GLOBAL_ELEMENTS;
            else if(ges.size() > 1 &&
                    (targetPattern == SchemaGenerator.Pattern.RUSSIAN_DOLL ||
                    targetPattern == SchemaGenerator.Pattern.VENITIAN_BLIND))
                return TransformHint.CANNOT_REMOVE_GLOBAL_ELEMENTS;
            
            //single global element
            int childElementEmptyCount = 0;
            int childAttributesEmptyCount = 0;
            boolean grandChildrenAvailable = false;
            int size = am.getRoot().getElements().size();
            for(Element ge:am.getRoot().getElements()) {
                List<AbstractElement> les =
                        new ArrayList<AbstractElement>(ge.getChildElements());
                filterOtherModelComponents(les, am);
                List<AbstractAttribute> attrs =
                        new ArrayList<AbstractAttribute>(ge.getAttributes());
                filterOtherModelComponents(attrs, am);
                if(les.size() == 0)
                    childElementEmptyCount++;
                if(attrs.size() == 0)
                    childAttributesEmptyCount++;
                if(!grandChildrenAvailable) {
                    for(AbstractElement le:les) {
                        List<AbstractElement> gles =
                                new ArrayList<AbstractElement>(le.getChildElements());
                        filterOtherModelComponents(gles, am);
                        List<AbstractAttribute> gattrs =
                                new ArrayList<AbstractAttribute>(le.getAttributes());
                        filterOtherModelComponents(gattrs, am);
                        if(gles.size() > 0 || gattrs.size() > 0) {
                            grandChildrenAvailable = true;
                            break;
                        }
                    }
                }
            }
            boolean noChildElements = childElementEmptyCount > 0 &&
                    childElementEmptyCount == size;
            boolean noChildAttributes = childAttributesEmptyCount > 0 &&
                    childAttributesEmptyCount == size;
            if(noChildElements && targetPattern == SchemaGenerator.Pattern.SALAMI_SLICE)
                return TransformHint.GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS;
            if(noChildAttributes && noChildElements &&
                    (targetPattern == SchemaGenerator.Pattern.GARDEN_OF_EDEN ||
                    targetPattern == SchemaGenerator.Pattern.RUSSIAN_DOLL))
                return TransformHint.GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS_AND_ATTRIBUTES;
            
            if(!grandChildrenAvailable &&
                    targetPattern == SchemaGenerator.Pattern.VENITIAN_BLIND)
                return TransformHint.GLOBAL_ELEMENTS_HAVE_NO_GRAND_CHILDREN;
            
            if(currentPattern == SchemaGenerator.Pattern.SALAMI_SLICE ||
                    currentPattern == SchemaGenerator.Pattern.RUSSIAN_DOLL)
                return TransformHint.WILL_REMOVE_GLOBAL_ELEMENTS;
            else
                return TransformHint.WILL_REMOVE_GLOBAL_ELEMENTS_AND_TYPES;
//            if((currentPattern == SchemaGenerator.Pattern.VENITIAN_BLIND ||
//                    currentPattern == SchemaGenerator.Pattern.GARDEN_OF_EDEN) &&
//                    (targetPattern == SchemaGenerator.Pattern.RUSSIAN_DOLL ||
//                    targetPattern == SchemaGenerator.Pattern.SALAMI_SLICE)) {
//                if(currentPattern == SchemaGenerator.Pattern.GARDEN_OF_EDEN)
//                    return TransformHint.WILL_REMOVE_GLOBAL_ELEMENTS_AND_TYPES;
//                else
//                    return TransformHint.WILL_REMOVE_TYPES;
//            } else if((currentPattern == SchemaGenerator.Pattern.SALAMI_SLICE ||
//                    currentPattern == SchemaGenerator.Pattern.GARDEN_OF_EDEN) &&
//                    (targetPattern == SchemaGenerator.Pattern.RUSSIAN_DOLL ||
//                    targetPattern == SchemaGenerator.Pattern.VENITIAN_BLIND)) {
//                if(currentPattern == SchemaGenerator.Pattern.GARDEN_OF_EDEN)
//                    return TransformHint.WILL_REMOVE_GLOBAL_ELEMENTS_AND_TYPES;
//                else
//                    return TransformHint.WILL_REMOVE_GLOBAL_ELEMENTS;
//            }
        }
        return TransformHint.OK;
    }
    
    private void filterOtherModelComponents(List cs, AXIModel am) {
        List<Integer> removeList = new ArrayList<Integer>();
        for(int i=0;i<cs.size();i++) {
            AXIComponent e = (AXIComponent) cs.get(i);
            if(!SchemaGeneratorUtil.fromSameSchemaModel(
                    e.getPeer(), am.getSchemaModel()))
                removeList.add(new Integer(i));
        }
        //finally remove components from other model
        for(int i=removeList.size()-1;i>=0;i--) {
            cs.remove(removeList.get(i).intValue());
        }
    }
    
    public void transformSchema(SchemaModel sm, SchemaGenerator.Pattern targetPattern)
    throws IOException {
        SchemaGenerator sg = null;
        if(targetPattern == SchemaGenerator.Pattern.GARDEN_OF_EDEN)
            sg = new GardenOfEden(SchemaGenerator.Mode.TRANSFORM);
        else if(targetPattern == SchemaGenerator.Pattern.VENITIAN_BLIND)
            sg = new VenetianBlind(SchemaGenerator.Mode.TRANSFORM);
        else if(targetPattern == SchemaGenerator.Pattern.SALAMI_SLICE)
            sg = new SalamiSlice(SchemaGenerator.Mode.TRANSFORM);
        else if(targetPattern == SchemaGenerator.Pattern.RUSSIAN_DOLL)
            sg = new RussianDoll(SchemaGenerator.Mode.TRANSFORM);
        else if(targetPattern == SchemaGenerator.Pattern.MIXED)
            sg = new MixedPattern(SchemaGenerator.Mode.TRANSFORM);
        
        if(sg != null)
            sg.transformSchema(sm);
    }
}
