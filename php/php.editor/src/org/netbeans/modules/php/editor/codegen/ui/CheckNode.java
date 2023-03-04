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
package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.Image;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.codegen.Property;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public abstract class CheckNode extends DefaultMutableTreeNode {

    private static final String ICON_BASE = "org/netbeans/modules/php/editor/resources/"; //NOI18N
    private static final String ICON_EXTENSION = ".png"; //NOI18N
    public static final int SINGLE_SELECTION = 0;
    public static final int DIG_IN_SELECTION = 4;
    protected int selectionMode;
    protected boolean isSelected;

    public CheckNode() {
        this(null);
    }

    public CheckNode(Object userObject) {
        this(userObject, true, false);
    }

    public CheckNode(Object userObject, boolean allowsChildren, boolean isSelected) {
        super(userObject, allowsChildren);
        this.isSelected = isSelected;
        setSelectionMode(DIG_IN_SELECTION);
    }

    private void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;

        if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
            Enumeration en = children.elements();
            while (en.hasMoreElements()) {
                CheckNode node = (CheckNode) en.nextElement();
                node.setSelected(isSelected);
            }
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public abstract Image getIcon();

    public static class CGSClassNode extends CheckNode {

        public CGSClassNode(String className) {
            super(className, true, false);
        }

        @Override
        public Image getIcon() {
            return ImageUtilities.loadImage(ICON_BASE + "class" + ICON_EXTENSION); //NOI18N
        }
    }

    public static class CGSPropertyNode extends CheckNode {

        protected final Property property;

        public CGSPropertyNode(Property property) {
            super(property.getName(), false, property.isSelected());
            this.property = property;
        }

        @Override
        public void setSelected(boolean isSelected) {
            super.setSelected(isSelected);
            property.setSelected(isSelected);
        }

        @Override
        public Image getIcon() {
            final int modifier = property.getModifier();
            final boolean isPublic = BodyDeclaration.Modifier.isPublic(modifier);
            final boolean isProtected = isPublic ? false : BodyDeclaration.Modifier.isProtected(modifier);
            final boolean isStatic = BodyDeclaration.Modifier.isStatic(modifier);
            return ImageUtilities.loadImage(ICON_BASE + getName(isPublic, isProtected, isStatic) + ICON_EXTENSION);
        }

        protected String getName(boolean isPublic, boolean isProtected, boolean isStatic) {
            String name = "fieldPrivate"; // NOI18N
            if (isPublic) {
                name = "fieldPublic"; // NOI18N
            } else if (isProtected) {
                name = "fieldProtected"; // NOI18N
            }
            return name;
        }
    }

    public static class MethodPropertyNode extends CGSPropertyNode {

        public MethodPropertyNode(Property property) {
            super(property);
            assert PhpElementKind.METHOD.equals(property.getKind()) : property.getKind();
        }

        @Override
        protected String getName(boolean isPublic, boolean isProtected, boolean isStatic) {
            StringBuilder sb = new StringBuilder();
            sb.append(isStatic ? "methodStatic" : "method"); // NOI18N
            sb.append(isPublic ? "Public" : "Protected"); // NOI18N
            return sb.toString();
        }
    }
}
