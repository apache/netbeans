package org.netbeans.modules.javascript2.model;

import org.netbeans.modules.javascript2.model.api.ModelUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsWith;
import org.netbeans.modules.javascript2.model.api.Occurrence;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

/**
 *
 * @author Petr Pisl
 */
public class JsWithObjectImpl extends JsObjectImpl implements JsWith {

    private final Collection<TypeUsage> withTypes;
    private final JsWith outerWith;
    private final Collection<JsWith> innerWith = new ArrayList<JsWith>();
    private final OffsetRange expressionRange;
    private final Set<JsObject> assignedIn = new HashSet<JsObject>();
    
    public JsWithObjectImpl(JsObject parent, String name, Collection<TypeUsage> withTypes,
            OffsetRange offsetRange, OffsetRange expressionRange, JsWith outer, String mimeType, String sourceLabel) {
        super(parent, name, false, offsetRange, EnumSet.of(Modifier.PUBLIC), mimeType, sourceLabel);
        this.withTypes = withTypes;
//        while (parent != null && !(parent instanceof JsWithObjectImpl)) {
//            parent = parent.getParent();
//        }
        this.outerWith = outer;
        if (this.outerWith != null) {
            ((JsWithObjectImpl)outerWith).addInnerWith(this);
        }
        this.expressionRange = expressionRange;
    }

    @Override
    public Collection<TypeUsage> getTypes() {
        return withTypes;
    }
    
    protected void addInnerWith(JsWith inner) {
        innerWith.add(inner);
    }
    
    
    @Override
    public Collection<JsWith> getInnerWiths() {
        Collection<JsWith> result = innerWith.isEmpty() ? Collections.EMPTY_LIST : new ArrayList<JsWith>(innerWith);
        return result;
    }
    
    @Override
    public JsWith getOuterWith() {
        return outerWith;
    }
    
    @Override
    public void resolveTypes(JsDocumentationHolder jsDocHolder) {
        Collection<JsObject> withProperties = new ArrayList<JsObject>(getProperties().values());
        for (JsObject withProperty: withProperties) {
            if (resolveWith(this, withProperty)) {
                properties.remove(withProperty.getName());
            }
        }
    }

    public Collection<JsObject> getObjectWithAssignment() {
        return this.assignedIn;
    }
    
    public void addObjectWithAssignment(JsObject object) {
        this.assignedIn.add(object);
    }
    
    private boolean resolveWith(JsWithObjectImpl withObject, JsObject property) {
        JsObject global = ModelUtils.getGlobalObject(withObject.getParent());
        for (TypeUsage typeUsage : withObject.getTypes()) {
            for (TypeUsage rType : ModelUtils.resolveTypeFromSemiType(withObject, typeUsage)) {
                JsObject fromType = ModelUtils.findJsObjectByName(global, rType.getType());
                if (fromType != null) {
                    JsObject propertyFromType = fromType.getProperty(property.getName());
                    if (propertyFromType == null && withObject.getOuterWith() == null) {
                        propertyFromType = global.getProperty(property.getName());
                    }
                    if (propertyFromType != null) {
                        moveOccurrenceOfObject((JsObjectImpl)propertyFromType, (JsObjectImpl)property);
                        moveFromWith((JsObjectImpl)propertyFromType, (JsObjectImpl)property);
                        return true;
                    } else {
                        JsWith outer = withObject.getOuterWith();
                        if (outer != null) {
                            return resolveWith((JsWithObjectImpl)outer, property);
                        }
                    }
                } else {
                    JsWith outer = withObject.getOuterWith();
                    if (outer != null) {
                        return resolveWith((JsWithObjectImpl)outer, property);
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public Kind getJSKind() {
        return JsElement.Kind.WITH_OBJECT;
    }
    
    @Override
    public int getOffset() {
        return getOffsetRange().getStart();
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    public OffsetRange getExpressionRange() {
        return expressionRange;
    }

    private void moveOccurrenceOfObject(JsObjectImpl original, JsObjectImpl copy) {
        for (Occurrence occurrence: copy.getOccurrences()) {
            original.addOccurrence(occurrence.getOffsetRange());
        }
        copy.clearOccurrences();
    }
    
    protected void moveFromWith(JsObjectImpl original, JsObjectImpl inWith) {
        if (original.equals(inWith)) {
            return;
        }
        if (!original.isDeclared() && inWith.isDeclared()) {
            moveOccurrenceOfObject(inWith, original);
            moveOccurrenceOfProperties(inWith, original);
            inWith.setParent(original.getParent());
            original.getParent().addProperty(original.getName(), inWith);
            return;
        }
        
        Collection<JsObject> prototypeChains = findPrototypeChain(original);
        Collection<JsObject> propertiesCopy = new ArrayList<JsObject>(inWith.getProperties().values());
        for (JsObject withProperty : propertiesCopy) {
            if (withProperty.isDeclared()) {
                boolean accessible = false;
                for (JsObject jsObject : prototypeChains) {
                    JsObject originalProperty = jsObject.getProperty(withProperty.getName());
                    if (originalProperty != null) {
                        accessible = true;
                        break;
                    }
                }
                if (!accessible) {
                    ((JsObjectImpl)withProperty).setParent(original);
                    original.addProperty(withProperty.getName(), withProperty);
                    inWith.properties.remove(withProperty.getName());
                }
            }
        }
        
        for (JsObject jsObject : prototypeChains) {
            for (JsObject origProperty : jsObject.getProperties().values()) {
                if(origProperty.getModifiers().contains(Modifier.PUBLIC)
                        || origProperty.getModifiers().contains(Modifier.PROTECTED)) {
                    JsObjectImpl usedProperty = (JsObjectImpl)inWith.getProperty(origProperty.getName());
                    if (usedProperty != null) {
                        ((JsObjectImpl)origProperty).addOccurrence(usedProperty.getDeclarationName().getOffsetRange());
                        for(Occurrence occur : usedProperty.getOccurrences()) {
                            ((JsObjectImpl)origProperty).addOccurrence(occur.getOffsetRange());
                        }
                        usedProperty.clearOccurrences();
                        if (origProperty.isDeclared() && usedProperty.isDeclared()){
                            usedProperty.setDeclared(false); // the property is not declared here
                        }
                        moveFromWith((JsObjectImpl)origProperty, usedProperty);
                    }
                }
            }
            JsObject prototype = jsObject.getProperty(ModelUtils.PROTOTYPE);
            if (prototype != null) {
                moveFromWith((JsObjectImpl)prototype, inWith);
            }
        }
        
        
    }
    
    
}
