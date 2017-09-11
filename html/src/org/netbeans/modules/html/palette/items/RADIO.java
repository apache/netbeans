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
package org.netbeans.modules.html.palette.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.palette.HtmlPaletteUtilities;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;

/**
 *
 * @author Libor Kotouc, mfukala@netbeans.org
 */
public class RADIO implements ActiveEditorDrop {

    private static final int GROUP_DEFAULT = -1;
    private String group = "";
    private int groupIndex = GROUP_DEFAULT;
    private String value = "";
    private boolean selected = false;
    private boolean disabled = false;
    private String[] groups = new String[0];

    public RADIO() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        Document doc = targetComponent.getDocument();
        if (doc instanceof BaseDocument) {

            String oldGN = null;
            if (groupIndex >= 0) // non-empty group list from previous run =>
            {
                oldGN = groups[groupIndex]; // => save previously selected group name
            } else if (group.length() > 0) // new group was inserted in the previous run
            {
                oldGN = group;
            }

            groups = findGroups((BaseDocument) doc);
            if (groups.length == 0) // no groups found => reset index
            {
                groupIndex = GROUP_DEFAULT;
            }

            if (groups.length > 0) { // some groups found
                groupIndex = 0; // point at the beginning by default
                if (groupIndex != GROUP_DEFAULT && oldGN != null) {// non-empty group list from previous run
                    for (; groupIndex < groups.length; groupIndex++) {
                        if (oldGN.equalsIgnoreCase(groups[groupIndex])) {
                            break;
                        }
                    }
                    if (groupIndex == groups.length) // previously selected group not found
                    {
                        groupIndex = 0;
                    }
                }
            }
        }

        RADIOCustomizer c = new RADIOCustomizer(this);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                HtmlPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }

        return accept;
    }

    private String createBody() {

        String strName = " name=\"\""; // NOI18N
        if (groupIndex == GROUP_DEFAULT) {
            strName = " name=\"" + group + "\""; // NOI18N
        } else {
            strName = " name=\"" + groups[groupIndex] + "\""; // NOI18N
        }
        String strValue = " value=\"" + value + "\""; // NOI18N

        String strSelected = (selected ? " checked=\"checked\"" : ""); // NOI18N

        String strDisabled = (disabled ? " disabled=\"disabled\"" : ""); // NOI18N

        String radioBody = "<input type=\"radio\"" + strName + strValue + strSelected + strDisabled + " />"; // NOI18N

        return radioBody;
    }

    //return a list of names of all radio inputs <input type=radio name="xxx"/>
    private String[] findGroups(final BaseDocument doc) {
        final CharSequence[] content = new CharSequence[1];
        doc.render(new Runnable() {
            public void run() {
                try {
                    content[0] = doc.getText(0, doc.getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        CharSequence code = content[0];
        if (code == null) {
            return new String[]{};
        }

        List<String> names = new ArrayList<String>();
        //search for the input tags
        HtmlSource source = new HtmlSource(code);
        Iterator<Element> elements = new ElementsIterator(source);
        while(elements.hasNext()) {
            Element e = elements.next();
            if (e.type() == ElementType.OPEN_TAG) {
                OpenTag tag = (OpenTag) e;
                if (LexerUtils.equals("input", tag.name(), true, true)) { //NOI18N
                    Attribute typeAttr = tag.getAttribute("type"); //NOI18N
                    Attribute nameAttr = tag.getAttribute("name"); //NOI18N
                    if (typeAttr != null && nameAttr != null) {
                        if (LexerUtils.equals("radio", typeAttr.unquotedValue(), true, true)) { //NOI18N
                            CharSequence value = nameAttr.unquotedValue();
                            if(value != null) {
                                names.add(value.toString());
                            }
                        }
                    }
                }
            }
        }
        return names.toArray(new String[]{});
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }
}
