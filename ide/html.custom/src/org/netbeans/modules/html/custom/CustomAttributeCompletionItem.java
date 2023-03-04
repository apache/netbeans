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
