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
package org.netbeans.modules.javascript2.model;

import org.netbeans.modules.javascript2.model.api.ModelUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.model.api.JsArray;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class JsArrayImpl extends JsObjectImpl implements JsArray {

    private List<TypeUsage> typesInArray = new ArrayList<TypeUsage>();

    public JsArrayImpl(JsObject parent, Identifier name, OffsetRange offsetRange, String mimeType, String sourceLabel) {
        super(parent, name, offsetRange, mimeType, sourceLabel);
    }

    public JsArrayImpl(JsObject parent, String name, boolean isDeclared, OffsetRange offsetRange, Set<Modifier> modifiers, String mimeType, String sourceLabel) {
        super(parent, name, isDeclared, offsetRange, modifiers, mimeType, sourceLabel);
    }
    
    public JsArrayImpl(JsObject parent, Identifier name, OffsetRange offsetRange, boolean isDeclared, Set<Modifier> modifiers, String mimeType, String sourceLabel) {
        super(parent, name, offsetRange, isDeclared, modifiers, mimeType, sourceLabel);
    }

    public Collection<? extends TypeUsage> getTypesInArray() {
        List<TypeUsage> values;
        values = new ArrayList<TypeUsage>();
        for(TypeUsage type : typesInArray) {
            values.add(type);
        }
        return Collections.unmodifiableCollection(values);
    }
   
    public void addTypeInArray(TypeUsage type) {
        boolean isHere = false;
        for (TypeUsage typeUsage : typesInArray) {
            if (typeUsage.getType().equals(type.getType())) {
                isHere = true;
                break;
            }
        }
        if (!isHere) {
            typesInArray.add(type);
        }
    }
    
    public void addTypesInArray(Collection<TypeUsage> types) {
        for (TypeUsage type : types) {
            addTypeInArray(type);
        }
    }

    @Override
    public void resolveTypes(JsDocumentationHolder jsDocHolder) {
        super.resolveTypes(jsDocHolder); 
        HashSet<String> nameTypesInArray = new HashSet<String>();
        Collection<TypeUsage> resolved = new ArrayList();
        Collection<? extends TypeUsage> typesIA = getTypesInArray();
        for (TypeUsage type : typesIA) {
            if (!(type.getType().equals(Type.UNRESOLVED) && typesIA.size() > 1)) {
                if (!type.isResolved()) {
                    for (TypeUsage rType : ModelUtils.resolveTypeFromSemiType(this, type)) {
                        if (!nameTypesInArray.contains(rType.getType())) {
                            if ("@this;".equals(type.getType())) { // NOI18N
                                rType = new TypeUsage(rType.getType(), -1, rType.isResolved());
                            }
                            resolved.add(rType);
                            nameTypesInArray.add(rType.getType());
                        }
                    }
                } else {
                    if (!nameTypesInArray.contains(type.getType())) {
                        resolved.add(type);
                        nameTypesInArray.add(type.getType());
                    }
                }
            }
        }
        
        for (TypeUsage type : resolved) {
            if (type.getOffset() > 0) {
                JsObject jsObject = ModelUtils.findJsObjectByName(this, type.getType());
                if (jsObject == null) {
                    JsObject global = ModelUtils.getGlobalObject(this);
                    jsObject = ModelUtils.findJsObjectByName(global, type.getType());
                }
                if (jsObject != null) {
                    int index = type.getType().lastIndexOf('.');
                    int typeLength = (index > -1) ? type.getType().length() - index - 1 : type.getType().length();
                    ((JsObjectImpl)jsObject).addOccurrence(new OffsetRange(type.getOffset(), type.getOffset() + typeLength));
                }
            }
        }
        typesInArray.clear();
        typesInArray.addAll(resolved);
                
    }
    
    
    
}
