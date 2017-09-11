/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.custom;

import java.awt.Color;
import java.util.Iterator;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;

/**
 *
 * @author marek
 */
public class CustomAttributeCompletionItem extends HtmlCompletionItem {

    private final org.netbeans.modules.html.custom.conf.Attribute attr;

    private final boolean autocompleteQuotes;

    public CustomAttributeCompletionItem(org.netbeans.modules.html.custom.conf.Attribute attr, int substituteOffset) {
        super(attr.getName(), substituteOffset);
        this.attr = attr;
        this.autocompleteQuotes = !"boolean".equals(attr.getType()); //NOI18N
    }

    protected Color getAttributeColor() {
        return Color.green.darker();
    }

    @Override
    protected String getSubstituteText() {
        StringBuilder sb = new StringBuilder();
        sb.append(getItemText());
        if (autocompleteQuotes) {
            sb.append("=\"\""); //NOI18N
        }
        return sb.toString();
    }

    @Override
    protected int getMoveBackLength() {
        return autocompleteQuotes ? 1 : 0; //last quotation
    }

    @Override
    public int getSortPriority() {
        //required attributes are higher in the cc list
        //context attributes (defined parent) are higher in the cc list
        return super.getSortPriority() - (attr.isRequired() ? 1 : 0) - (attr.getParent() != null ? 1 : 0);
    }

    @Override
    protected String getLeftHtmlText() {
        StringBuilder sb = new StringBuilder();
        if (attr.isRequired()) {
            sb.append("<b>"); //NOI18N
        }
        sb.append("<font color=#"); //NOI18N
        sb.append(hexColorCode(getAttributeColor()));
        sb.append(">"); //NOI18N
        sb.append(getItemText());
        sb.append("</font>"); //NOI18N
        if (attr.isRequired()) {
            sb.append("</b>"); //NOI18N
        }

        return sb.toString();
    }

    //TODO needs localization
    //TODO parts of the doc generation needs to be shared w/ the CustomTagCompletionItem - copied
    @Override
    public String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>");
        sb.append(attr.getName());
        sb.append("</h1>");
        sb.append("<h2>Custom HTML attribute</h2>");

        String description = attr.getDescription();
        if(description != null) {
            sb.append("<p>");
            sb.append(description);
            sb.append("</p>");
        }
        
        String documentation = attr.getDocumentation();
        if(documentation != null) {
            sb.append("<p>");
            sb.append(documentation);
            sb.append("</p>");
        }

        if(description != null || documentation != null) {
            sb.append("<hr/>");
        }
        
        sb.append("<p><b>");
        sb.append(attr.isRequired() ? "Required" : "Optional");
        sb.append("</b></p>");
        
        sb.append("<p><b>Type: </b>");
        sb.append(attr.getType() != null ? attr.getType() : "undefined");
        
        sb.append("<p>");
        if (attr.getContexts().isEmpty()) {
            sb.append("<b>Allowed in all contexts.</b>");
        } else {
            sb.append("<b>Allowed in context(s): </b>");
            Iterator<String> i = attr.getContexts().iterator();
            while (i.hasNext()) {
                String ctx = i.next();
                sb.append(ctx);
                if (i.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append("</p>");

        return sb.toString();
    }

    @Override
    public boolean hasHelp() {
        return true;
    }

}
