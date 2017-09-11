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
