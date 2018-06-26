/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsReference;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class JsObjectReference extends JsObjectImpl implements JsReference {
 
    private final JsObject original;
    
    private final Set<Modifier> modifiers;

    public JsObjectReference(JsObject parent, Identifier declarationName,
            @NonNull JsObject original, boolean isDeclared, Set<Modifier> modifiers) {
        super(parent, declarationName, declarationName.getOffsetRange(), isDeclared,
                modifiers == null ? EnumSet.noneOf(Modifier.class) : modifiers, original.getMimeType(), original.getSourceLabel());
        this.original = original;
        this.modifiers = modifiers;
    }

    @Override
    public Map<String, ? extends JsObject> getProperties() {
        return original.getProperties();
    }

    @Override
    public void addProperty(String name, JsObject property) {
        original.addProperty(name, property);
    }

    @Override
    public JsObject getProperty(String name) {
        return original.getProperty(name);
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public Kind getJSKind() {
        Kind kind = original.getJSKind();
        if (kind == JsElement.Kind.ANONYMOUS_OBJECT) {
            kind = JsElement.Kind.OBJECT_LITERAL;
        }
        return kind;
    }

    @Override
    public ElementKind getKind() {
        return original.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (modifiers != null) {
            return modifiers;
        }
        return original.getModifiers();
    }
    
    public JsObject getOriginal() {
        return original;
    }

    @Override
    public Collection<? extends TypeUsage> getAssignmentForOffset(int offset) {
        return original.getAssignmentForOffset(offset);
    }

    @Override
    public Collection<? extends TypeUsage> getAssignments() {
        return original.getAssignments();
    }

    @Override
    public void resolveTypes(JsDocumentationHolder docHolder) {
        // do nothing
    }

    @Override
    public Documentation getDocumentation() {
        return original.getDocumentation(); 
    }

    @Override
    protected void correctTypes(String fromType, String toType) {
        //Do nothing
    }
    
}
