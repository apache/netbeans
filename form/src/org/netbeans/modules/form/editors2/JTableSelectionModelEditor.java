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

import java.io.IOException;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import org.netbeans.modules.form.FormCodeAwareEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADProperty;
import org.netbeans.modules.form.codestructure.CodeVariable;
import org.netbeans.modules.form.editors.EnumEditor;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Property editor for <code>JTable.selectionModel</code> property. It provides tags
 * that correspond to valid values of <code>JTable.setSelectionMode()</code>.
 *
 * @author Jan Stola
 */
public class JTableSelectionModelEditor extends EnumEditor implements FormCodeAwareEditor, XMLPropertyEditor {
    /** Property being edited. */
    private FormProperty property;

    /**
     * Creates new <code>JTableSelectionModelEditor</code>.
     */
    public JTableSelectionModelEditor() {
        super(new Object[] {
            NbBundle.getMessage(JTableSelectionModelEditor.class, "JTableSelectionModelEditor_DEFAULT"), // NOI18N
            new Object(),
            "", // NOI18N
            NbBundle.getMessage(JTableSelectionModelEditor.class, "JTableSelectionModelEditor_SINGLE_SELECTION"), // NOI18N
            createListSelectionModel(ListSelectionModel.SINGLE_SELECTION),
            "ListSelectionModel.SINGLE_SELECTION", // NOI18N
            NbBundle.getMessage(JTableSelectionModelEditor.class, "JTableSelectionModelEditor_SINGLE_INTERVAL_SELECTION"), // NOI18N
            createListSelectionModel(ListSelectionModel.SINGLE_INTERVAL_SELECTION),
            "ListSelectionModel.SINGLE_INTERVAL_SELECTION", // NOI18N
            NbBundle.getMessage(JTableSelectionModelEditor.class, "JTableSelectionModelEditor_MULTIPLE_INTERVAL_SELECTION"), // NOI18N
            createListSelectionModel(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION),
            "ListSelectionModel.MULTIPLE_INTERVAL_SELECTION", // NOI18N
        }, false);
    }

    private static ListSelectionModel createListSelectionModel(int selectionMode) {
        DefaultListSelectionModel model = new DefaultListSelectionModel();
        model.setSelectionMode(selectionMode);
        return model;
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof ListSelectionModel)) {
            value = property.getDefaultValue();
        }
        super.setValue(value);
    }

    @Override
    public String getSourceCode() {
        String code = super.getJavaInitializationString();
        if (code != null) {
            RADProperty prop = (RADProperty)this.property;
            RADComponent comp = prop.getRADComponent();
            CodeVariable var = comp.getCodeExpression().getVariable();            
            String varName = (var == null) ? null : var.getName(); // NOI18N
            varName = (varName == null) ? "" : varName + "."; // NOI18N
            code = varName + "setSelectionMode(" + code + ");\n"; // NOI18N
        }        
        return code;
    }

    /**
     * Sets context of the property editor. 
     * 
     * @param formModel form model.
     * @param property property being edited.
     */
    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        this.property = property;
    }

    /**
     * Raise form version to 6.1 - this editor is available since NB 6.1.
     */
    @Override
    public void updateFormVersionLevel() {
        property.getPropertyContext().getFormModel()
                .raiseVersionLevel(FormModel.FormVersion.NB61, FormModel.FormVersion.NB61);
    }

    private static final String XML_TABLE_SELECTION_MODEL = "JTableSelectionModel"; // NOI18N
    private static final String ATTR_SELECTION_MODE = "selectionMode"; // NOI18N

    @Override
    public void readFromXML(Node element) throws IOException {
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        Object[] values = getEnumerationValues();
        Object value;
        Node node = attributes.getNamedItem(ATTR_SELECTION_MODE);
        int selectionMode = Integer.valueOf(node.getNodeValue()).intValue();
        switch (selectionMode) {
            case ListSelectionModel.SINGLE_SELECTION: value = values[4]; break;
            case ListSelectionModel.SINGLE_INTERVAL_SELECTION: value = values[7]; break;
            case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION: value = values[10]; break;
            default: value = values[1]; break;
        }
        setValue(value);
    }

    @Override
    public Node storeToXML(Document doc) {
        Object value = getValue();
        int selectionMode = -1;
        Object[] values = getEnumerationValues();
        if (values[4].equals(value)) {
            selectionMode = ListSelectionModel.SINGLE_SELECTION;
        } else if (values[7].equals(value)) {
            selectionMode = ListSelectionModel.SINGLE_INTERVAL_SELECTION;
        } else if (values[10].equals(value)) {
            selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        }
        org.w3c.dom.Element el = null;
        el = doc.createElement(XML_TABLE_SELECTION_MODEL);
        el.setAttribute(ATTR_SELECTION_MODE, Integer.toString(selectionMode));
        return el;
    }

}
