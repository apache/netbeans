/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.php.editor.codegen;

import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;

/**
 *
 * @author Petr Pisl
 */
public class Property {

    private final String name;
    private final int modifier;
    private boolean selected;
    private final String type;

    public Property(final String name, final int modifier, final String type) {
        this.name = name;
        this.modifier = modifier;
        this.type = type;
        selected = false;
    }

    public Property(String name, int modifier) {
        this(name, modifier, ""); //NOI18N
    }

    public int getModifier() {
        return modifier;
    }

    public String getAccessor() {
        return isStatic() ? "self::" : "$$this->"; //NOI18N
    }

    public String getFluentReturnAccessor() {
        return isStatic() ? "self" : "$$this"; //NOI18N
    }

    private boolean isStatic() {
        return BodyDeclaration.Modifier.isStatic(getModifier());
    }

    public String getFunctionModifier() {
        return isStatic() ? "static" : ""; //NOI18N
    }

    public String getName() {
        return name;
    }

    public String getAccessedName() {
        return isStatic() ? "$$" + name : name; //NOI18N
    }

    public String getType() {
        return type;
    }

    public String getTypeForTemplate() {
        return getType() + " "; //NOI18N
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public PhpElementKind getKind() {
        return PhpElementKind.FIELD;
    }

    @Override
    public String toString() {
        return "Property{" + "name=" + name + ", modifier=" + modifier + ", selected=" + selected + ", type=" + type + '}'; // NOI18N
    }

}
