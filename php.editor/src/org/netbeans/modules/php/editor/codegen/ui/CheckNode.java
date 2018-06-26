/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
