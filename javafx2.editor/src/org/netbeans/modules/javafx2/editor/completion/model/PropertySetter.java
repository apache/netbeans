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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sdedic
 */
public class PropertySetter extends PropertyValue implements HasContent {
    /**
     * Character content of the property value, if the value is simple
     */
    private Object  valueContent;
    
    /**
     * If true, this PE corresponds to the default property and its
     * Element is not present in the source.
     */
    private boolean implicit;
    
    /**
     * Nested instances
     */
    private List<FxObjectBase>    valueBeans = Collections.emptyList();

    PropertySetter(String propertyName) {
        super(propertyName);
    }
    
    PropertySetter asImplicitDefault() {
        this.implicit = true;
        return this;
    }

    public boolean isImplicit() {
        return implicit;
    }
    
    public List<FxObjectBase> getValues() {
        return Collections.unmodifiableList(valueBeans);
    }
    
    /**
     * Returns the content of the property value.
     * @return 
     */
    public CharSequence getContent() {
        CharSequence c = getValContent(valueContent);
        if (c != valueContent) {
            valueContent = c;
        }
        return c;
    }
    
    void addValue(FxObjectBase instance) {
        if (valueBeans.isEmpty()) {
            valueBeans = new ArrayList<FxObjectBase>();
        }
        valueBeans.add(instance);
    }
    
    @Override
    public void accept(FxNodeVisitor v) {
        v.visitPropertySetter(this);
    }
    
    static Object addCharContent(Object valueContent, CharSequence content) {
        if (valueContent == null) {
            valueContent = content;
        } else if (valueContent instanceof CharSequence) {
            List<CharSequence> parts = new ArrayList<CharSequence>();
            parts.add((CharSequence)valueContent);
            parts.add(content);
            valueContent = parts;
        } else if (valueContent instanceof List) {
            ((List<CharSequence>)(List)valueContent).add(content);
        }
        return valueContent;
    }
    
    static CharSequence getValContent(Object valueContent) {
        if (valueContent == null) {
            return null;
        }
        if (valueContent instanceof CharSequence) {
            return (CharSequence)valueContent;
        } else if (valueContent instanceof List) {
            return new CompoundCharSequence(0, (List<CharSequence>)(List)valueContent, -1);
        }
        throw new IllegalStateException();
    }
    
    void addContent(CharSequence content) {
        valueContent = addCharContent(valueContent, content);
    }

    @Override
    void addChild(FxNode child) {
        super.addChild(child);
        if (child instanceof FxObjectBase) {
            addValue((FxObjectBase)child);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    
}
