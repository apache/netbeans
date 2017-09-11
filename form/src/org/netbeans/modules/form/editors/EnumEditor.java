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

package org.netbeans.modules.form.editors;

import java.beans.*;
import java.io.IOException;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A property editor class handling enumeration values provided for some
 * properties of Swing components.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class EnumEditor extends PropertyEditorSupport
                        implements NamedPropertyEditor, XMLPropertyEditor
{
    /** array of object triplets describing the enumeration
     * 0 - displayed label
     * 1 - value
     * 2 - code string
     */
    private Object[] enumerationValues;

    private boolean unknownToString;

    public EnumEditor(Object[] enumerationValues) {
        this(enumerationValues, true, false);
    }

    public EnumEditor(Object[] enumerationValues, boolean translate) {
        this(enumerationValues, translate, false);
    }

    /**
     * @param unknownToString false if only the enumerated values can be entered,
     *        true if also other values can be entered - then they are transformed
     *        to String representation via toString(), for creating values from
     *        String the subclass needs to override setAsText method
     */
    protected EnumEditor(Object[] enumerationValues, boolean translate, boolean unknownToString) {
        if (translate) {
            translateEnumLabels(enumerationValues);
        }
        this.enumerationValues = enumerationValues;
        this.unknownToString = unknownToString;
    }

    protected Object[] getEnumerationValues() {
        return enumerationValues;
    }

    // --------

    @Override
    public String[] getTags() {
        int n = enumerationValues.length / 3;
        String[] tags = new String[n];
        for (int i=0; i < n; i++)
            tags[i] = (String) enumerationValues[i*3];

        return tags;
    }

    @Override
    public void setAsText(String str) {
        setValueFromString(str);
    }

    protected final boolean setValueFromString(String str) {
        int n = enumerationValues.length / 3;
        for (int i=0; i < n; i++) {
            if (enumerationValues[i*3].toString().equals(str)) {
                setValue(enumerationValues[i*3 + 1]);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        int n = enumerationValues.length / 3;
        for (int i=0; i < n; i++) {
            Object eVal = enumerationValues[i*3 + 1];
            if ((eVal == null && value == null) || (eVal != null && eVal.equals(value)))
                return enumerationValues[i*3].toString();
        }
        if (unknownToString && value != null) {
            return value.toString();
        }
        return enumerationValues.length > 0 ?
                 enumerationValues[0].toString() : null;
    }

    @Override
    public String getJavaInitializationString() {
        String initString = null;

        Object value = getValue();
        int n = enumerationValues.length / 3;
        for (int i=0; i < n; i++) {
            Object eVal = enumerationValues[i*3 + 1];
            if ((eVal == null && value == null) || (eVal != null && eVal.equals(value))) {
                initString = (String) enumerationValues[i*3 + 2];
                break;
            }
        }

        if (initString == null) {
            if (unknownToString && value != null) {
                initString = value.toString();
            } else {
                initString = enumerationValues.length > 2 ?
                             (String) enumerationValues[2] : null;
            }
        }
        if (initString == null)
            return null;

        for (int i=0; i < swingClassNames.length; i++)
            if (initString.startsWith(swingClassNames[i])) {
                initString = "javax.swing." + initString; // NOI18N
                break;
            }

        return initString;
    }

    // -------
    // NamedPropertyEditor

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getBundle(EnumEditor.class)
                                            .getString("CTL_EnumEditorName"); // NOI18N
    }

    // -------

    /* We arrange some constants description to better fit in narrow space
     * in property sheet, e.g. instead of HORIZONTAL_SCROLLBAR_AS_NEEDED we
     * show only AS_NEEDED. We keep the uppercase letters to preserve the
     * feeling that the value is a constant.
     */
    private static void translateEnumLabels(Object[] enumerationValues) {
        int n1 = enumerationValues.length / 3;
        int n2 = arrangedEnumLabels.length / 2;

        for (int i=0; i < n1; i++) {
            String code = (String) enumerationValues[i*3 + 2];
            for (int j=0; j < n2; j++)
                if (code.endsWith(arrangedEnumLabels[j*2])) {
                    enumerationValues[i*3] = arrangedEnumLabels[j*2 + 1];
                    break;
                }
        }
    }

    private static String[] arrangedEnumLabels = {
        "WindowConstants.DISPOSE_ON_CLOSE", "DISPOSE", // NOI18N
        "WindowConstants.DO_NOTHING_ON_CLOSE", "DO_NOTHING", // NOI18N
        "WindowConstants.HIDE_ON_CLOSE", "HIDE", // NOI18N
        "JFrame.EXIT_ON_CLOSE", "EXIT", // NOI18N
        "ListSelectionModel.MULTIPLE_INTERVAL_SELECTION", "MULTIPLE_INTERVAL", // NOI18N
        "ListSelectionModel.SINGLE_INTERVAL_SELECTION", "SINGLE_INTERVAL", // NOI18N
        "ListSelectionModel.SINGLE_SELECTION", "SINGLE", // NOI18N
        "JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED", "AS_NEEDED", // NOI18N
        "JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS", "ALWAYS", // NOI18N
        "JScrollPane.HORIZONTAL_SCROLLBAR_NEVER", "NEVER", // NOI18N
        "JScrollPane.VERTICAL_SCROLLBAR_ALWAYS", "ALWAYS", // NOI18N
        "JScrollPane.VERTICAL_SCROLLBAR_NEVER", "NEVER", // NOI18N
        "JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED", "AS_NEEDED", // NOI18N
        "ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED", "AS_NEEDED", // NOI18N
        "ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS", "ALWAYS", // NOI18N
        "ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER", "NEVER", // NOI18N
        "ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS", "ALWAYS", // NOI18N
        "ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER", "NEVER", // NOI18N
        "ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED", "AS_NEEDED", // NOI18N
        "JTable.AUTO_RESIZE_NEXT_COLUMN", "NEXT_COLUMN", // NOI18N
        "JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS", "SUBSEQUENT_COLUMNS", // NOI18N
        "JTable.AUTO_RESIZE_OFF", "OFF", // NOI18N
        "JTable.AUTO_RESIZE_ALL_COLUMNS", "ALL_COLUMNS", // NOI18N
        "JTable.AUTO_RESIZE_LAST_COLUMN", "LAST_COLUMN" // NOI18N
    };

    private static String[] swingClassNames = {
        "SwingConstants.", // NOI18N
        "DebugGraphics.", // NOI18N
        "JDesktopPane.", // NOI18N
        "JFileChooser.", // NOI18N
        "WindowConstants.", // NOI18N
        "ListSelectionModel.", // NOI18N
        "JScrollBar.", // NOI18N
        "JScrollPane.", // NOI18N
        "ScrollPaneConstants.", // NOI18N
        "JSlider.", // NOI18N
        "JSplitPane.", // NOI18N
        "JTabbedPane.", // NOI18N
        "JTable.", // NOI18N
        "JTextField.", // NOI18N
        "JViewport.", // NOI18N
        "JFrame.", // NOI18N        
        "JList.", // NOI18N
        "JFormattedTextField." // NOI18N
    };

    private static final String XML_VALUE = "Value"; // NOI18N
    private static final String ATTR_ID = "id"; // NOI18N

    @Override
    public void readFromXML(Node element) throws IOException {
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        Node node = attributes.getNamedItem(ATTR_ID);
        String id = node.getNodeValue();
        int n = enumerationValues.length / 3;
        for (int i=0; i < n; i++) {
            if (enumerationValues[i*3].toString().equals(id)
                || ((enumerationValues[i*3+1] instanceof Enum) && (((Enum)enumerationValues[i*3+1]).name().equals(id)))) {
                setValue(enumerationValues[i*3 + 1]);
                break;
            }
        }
    }

    @Override
    public Node storeToXML(Document doc) {
        Object value = getValue();
        if (value instanceof Integer || value instanceof Short
                || value instanceof Byte || value instanceof Long
                || value instanceof Float || value instanceof Double
                || value instanceof Boolean || value instanceof Character
                || value instanceof String || value instanceof Class) {
            // These types are stored using GandalfPersistenceManager.encodePrimitiveValue()
            return null;
        }
        String id = (value instanceof Enum) ? ((Enum)value).name() : getAsText();
        org.w3c.dom.Element el = doc.createElement(XML_VALUE);
        el.setAttribute(ATTR_ID, id);
        return el;
    }
}
