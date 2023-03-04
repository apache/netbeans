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
