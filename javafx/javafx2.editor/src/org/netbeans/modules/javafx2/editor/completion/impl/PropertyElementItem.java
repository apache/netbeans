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

import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.completion.impl.Bundle.*;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
final class PropertyElementItem extends AbstractCompletionItem {
    /**
     * type in a printable form
     */
    private String  propertyType;
    
    /**
     * true, if the type is primitive - different markup
     */
    private boolean primitive;
    
    private boolean attribute;
    
    private boolean inherited;
    
    private boolean system;
    
    private boolean map;
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/property.png"; // NOI18N
    private static final String MAP_ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/map-property.png"; // NOI18N
    private static final String SYSTEM_ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/system-property.png"; // NOI18N
    
    private static ImageIcon ICON;
    private static ImageIcon MAP_ICON;
    private static ImageIcon SYSTEM_ICON;

    private Callable<String> namespaceCreator;
    
    public PropertyElementItem(CompletionContext ctx, String text, boolean attribute) {
        super(ctx, text);
        this.attribute = attribute;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
    
    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public void setMap(boolean map) {
        this.map = map;
    }
    
    public void setNamespaceCreator(Callable<String> namespaceCreator) {
        this.namespaceCreator = namespaceCreator;
    }
    
    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }
    
    @NbBundle.Messages({
        "# {0} - property name",
        "FMT_ownProperty=<b>{0}</b>"
    })
    @Override
    protected String getLeftHtmlText() {
        if (!inherited) {
            return FMT_ownProperty(super.getLeftHtmlText());
        } else {
            return super.getLeftHtmlText();
        }
    }

    @Override
    protected void doSubstituteText(JTextComponent c, BaseDocument d, String text) throws BadLocationException {
        if (namespaceCreator != null) {
            try {
                String s = namespaceCreator.call();
                if (!"fx".equals(s)) {
                    text = text.replace("fx:", s + ":");
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        super.doSubstituteText(c, d, text);
    }
    
    @Override
    protected String getSubstituteText() {
        boolean replaceExisting = ctx.isReplaceExisting();
        if (attribute) {
            if (replaceExisting) {
                return super.getSubstituteText();
            } else {
                return super.getSubstituteText() + "=\"\" ";
            }
        } else if (map) {
            if (replaceExisting) {
                return "<" + super.getSubstituteText(); // NOI18N
            } else {
                return "<" + super.getSubstituteText() + " />";
            }
        } else {
            if (replaceExisting) {
                // leave the matching end tag unchanged
                return "<" + super.getSubstituteText();
            } else {
                return "<" + super.getSubstituteText() + "></" + super.getSubstituteText() + ">";
            }
        }
    }
    
    @Override
    protected int getCaretShift(Document d) {
        // incidentally, for all 3 cases:
        return 2 + super.getSubstituteText().length();
    }

    @Override
    protected String getRightHtmlText() {
        if (propertyType == null) {
            return null;
        }
        return NbBundle.getMessage(PropertyElementItem.class, 
                primitive ? "FMT_PrimitiveType" : "FMT_DeclaredType", propertyType);
    }
    
    @Override
    protected ImageIcon getIcon() {
        if (system) {
            if (SYSTEM_ICON == null) {
                SYSTEM_ICON = ImageUtilities.loadImageIcon(SYSTEM_ICON_RESOURCE, false);
            }
            return SYSTEM_ICON;
        } else if (map) {
            if (MAP_ICON == null) {
                MAP_ICON = ImageUtilities.loadImageIcon(MAP_ICON_RESOURCE, false);
            }
            return MAP_ICON;
        } else {
            if (ICON == null) {
                ICON = ImageUtilities.loadImageIcon(ICON_RESOURCE, false);
            }
            return ICON;
        }
    }

    public String toString() {
        return "property[" + super.getSubstituteText() + "]";
    }
}
