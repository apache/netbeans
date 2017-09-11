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
import javax.swing.*;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.RADProperty;
import org.netbeans.modules.form.editors.StringArrayCustomEditor;
import org.netbeans.modules.form.editors.StringArrayEditor;
import org.openide.util.NbBundle;

/** A simple property editor for ComboBoxModel.
 *
 * @author Tomas Pavek
 */

public class ComboBoxModelEditor extends StringArrayEditor 
       implements NamedPropertyEditor, FormAwareEditor {

    private ComboBoxModel comboModel;
    private RADProperty property;

    @Override
    public void setValue(Object val) {
        if (val instanceof ComboBoxModel) {
            comboModel = (ComboBoxModel) val;
            super.setValue(getDataFromModel(comboModel));
        }
        else if (val instanceof String[]) {
            comboModel = getModelForData((String[])val);
            super.setValue(val);
        }
        else {
            comboModel = getModelForData(new String[0]);
            super.setValue(null);
        }
    }

    @Override
    public Object getValue() {
        return comboModel;
    }

    @Override
    public void setStringArray(String[] value) {
        comboModel = getModelForData(value);
        super.setValue(value);
    }

    @Override
    public String[] getStringArray () {
        return (String[])super.getValue ();
    }

    @Override
    public String getJavaInitializationString() {
        if (getStrings(true).equals("")) { // NOI18N
            return null;
        }
        StringBuilder buf = new StringBuilder();
        buf.append("new javax.swing.DefaultComboBoxModel"); // NOI18N
        buf.append(FormUtils.getTypeParametersCode(property, false));
        buf.append("(new String[] { "); // NOI18N
        buf.append(getStrings(true));
        buf.append(" })"); // NOI18N

        return buf.toString();
    }

    static String[] getDataFromModel(ComboBoxModel model) {
        return ListModelEditor.getDataFromModel(model);
    }

    static ComboBoxModel getModelForData(String[] data) {
        return new DefaultComboBoxModel(data);
    }
    
    /**
     * Returns human-readable name of this property editor.
     * 
     * @return human-readable name of this property editor.
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(ComboBoxModelEditor.class).getString("CTL_ComboBoxModelEditor_DisplayName"); // NOI18N
    }

    @Override
    public Component getCustomEditor() {
        return new StringArrayCustomEditor(
                this,
                NbBundle.getMessage(
                    ComboBoxModelEditor.class, 
                    "ComboBoxModelEditor.label.text")
                ); // NOI18N
    }

    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        if (property instanceof RADProperty) {
            this.property = (RADProperty) property;
        }
    }

    @Override
    public void updateFormVersionLevel() {
    }
}
