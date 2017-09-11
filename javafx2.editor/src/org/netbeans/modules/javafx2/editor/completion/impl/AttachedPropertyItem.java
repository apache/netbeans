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
package org.netbeans.modules.javafx2.editor.completion.impl;

import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.openide.util.NbBundle;
import static org.netbeans.modules.javafx2.editor.completion.impl.Bundle.*;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
final class AttachedPropertyItem extends AbstractCompletionItem {
    private static final String ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/property-static.png"; // NOI18N

    /**
     * Class name prefix without the '.' in the case that only class name will be inserted.
     */
    private final String  classPrefix;
    
    /**
     * Name of the property
     */
    private final String  propertyName;
    
    /**
     * Example names of properties in a class (class prefix)
     */
    private final String  propertySamples;
    
    /**
     * Type of the attachment, just for specific properties; empty for class prefixes.
     */
    private final String  type;
    
    /**
     * whether a property type is primitive (different formatting)
     */
    private final boolean primitive;
    
    private boolean attribute;
    
    public AttachedPropertyItem(CompletionContext ctx, String text, String classPrefix, String propertySamples) {
        super(ctx, text);
        this.classPrefix = classPrefix;
        this.propertySamples = propertySamples;
        this.primitive = false;
        this.type = null;
        this.propertyName = null;
    }

    public AttachedPropertyItem(CompletionContext ctx, String text, String propertyName, String type, boolean primitive) {
        super(ctx, text);
        this.propertyName = propertyName;
        this.primitive = primitive;
        this.type = type;
        this.classPrefix = null;
        this.propertySamples = null;
    }

    public void setAttribute(boolean attribute) {
        this.attribute = attribute;
    }

    @Override
    protected String getSubstituteText() {
        boolean replaceExisting = ctx.isReplaceExisting();
        if (attribute) {
            if (replaceExisting || type == null) {
                return super.getSubstituteText();
            } else {
                return super.getSubstituteText() + "=\"\" "; // NOI18N
            } 
        } else {
            if (replaceExisting || type == null) {
                return "<" + super.getSubstituteText();
            } else {
                return "<" + super.getSubstituteText() + "></" + super.getSubstituteText() + ">";
            }
        }
    }
    
    @NbBundle.Messages({
        "# {0} - simple class name",
        "# {1} - sample property names",
        "FMT_attachedPropertyClassLeft=<i>{0}<font color='gray'> - {1}</font></i>",
        "# {0} - property name",
        "FMT_attachedPropertyLeft=<i>{0}</i>"
    })
    @Override
    protected String getLeftHtmlText() {
        if (classPrefix != null) {
            return FMT_attachedPropertyClassLeft(classPrefix, propertySamples);
        } else {
            return FMT_attachedPropertyLeft(propertyName);
        }
    }

    @Override
    protected int getCaretShift(Document d) {
        if (classPrefix != null) {
            return super.getCaretShift(d);
        } else if (!attribute) {
            return 2 + super.getSubstituteText().length();
        } else {
            // position the caret into apostrophes:
            return super.getCaretShift(d) - 2;
        }
    }

    @Override
    protected String getRightHtmlText() {
        if (classPrefix != null) {
            return null;
        }
        if (type == null) {
            return null;
        }
        if (primitive) {
            return NbBundle.getMessage(AttachedPropertyItem.class, "FMT_PrimitiveType", type);
        } else {
            return NbBundle.getMessage(AttachedPropertyItem.class, "FMT_DeclaredType", type);
        }
    }

    private static ImageIcon ICON = null;
    
    @Override
    protected ImageIcon getIcon() {
        if (ICON == null) {
            ICON = ImageUtilities.loadImageIcon(ICON_RESOURCE, false);
        }
        return ICON;
    }
    
    public String toString() {
        return "staticProperty[" + propertyName + "]";
    }
}
