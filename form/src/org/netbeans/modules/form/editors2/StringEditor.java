/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.form.editors2;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.ResourceSupport;
import org.netbeans.modules.form.ResourceWrapperEditor;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * Property editor wrapping StringEditor in a resource editor, allowing to
 * produce resource values (ResourceValue) from strings.
 * 
 * @author Tomas Pavek
 */
public class StringEditor extends ResourceWrapperEditor implements NamedPropertyEditor {

    private JCheckBox noI18nCheckbox;

    public StringEditor() {
        super(new org.netbeans.modules.form.editors.StringEditor());
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(StringEditor.class, "StringEditor_DisplayName"); // NOI18N
    }

    @Override
    public String getJavaInitializationString() {
        String javaStr = super.getJavaInitializationString();
        if (getValue() instanceof String
            && ResourceSupport.isResourceableProperty(property)
            && ResourceSupport.isExcludedProperty(property))
        {   // intentionally excluded from resourcing/internationalization - add NOI18N comment
            javaStr = "*/\n\\1NOI18N*/\n\\0" + javaStr; // NOI18N
            // */\n\\1 is a special code mark for line comment
            // */\n\\0 is a special code mark to indicate that a real code follows
        }
        return javaStr;
    }

    @Override
    public Component getCustomEditor() {
        Component customEd = super.getCustomEditor();
        if (noI18nCheckbox != null) {
            noI18nCheckbox.setSelected(ResourceSupport.isExcludedProperty(property));
        }
        return customEd;
    }

    @Override
    protected Component createCustomEditorGUI(Component resourcePanelGUI) {
        if (resourcePanelGUI == null && ResourceSupport.isResourceableProperty(property)) {
            // not usable for full resourcing, only for internationalization
            // add a NOI18N checkbox so the user can mark the property as not to be internationalized
            Component customEd = delegateEditor.getCustomEditor();
            JPanel panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            noI18nCheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(noI18nCheckbox, NbBundle.getMessage(StringEditor.class, "CTL_NOI18NCheckBox")); // NOI18N
            noI18nCheckbox.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getBundle(
                        StringEditor.class).getString("ACD_NOI18NCheckBox")); //NOI18N
            
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(customEd)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap().addComponent(noI18nCheckbox).addContainerGap()));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(customEd).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(noI18nCheckbox));
            return panel;
        }
        else {
            noI18nCheckbox = null;
            return super.createCustomEditorGUI(resourcePanelGUI);
        }
    }

    // called when OK button is pressed in the custom editor dialog
    @Override
    public void vetoableChange(PropertyChangeEvent ev) throws PropertyVetoException {
        super.vetoableChange(ev);
        if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())
            && resourcePanel == null && noI18nCheckbox  != null)
        {   // no resourcing, just internationalizing
            // mark the property excluded if the NOI18N checkbox is checked
            ResourceSupport.setExcludedProperty(property, noI18nCheckbox.isSelected());
        }
    }
}
