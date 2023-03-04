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
import javax.swing.ImageIcon;
import org.netbeans.modules.html.custom.conf.Tag;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.openide.util.ImageUtilities;

/**
 *
 * @author marek
 */
public class CustomTagCompletionItem extends HtmlCompletionItem {

    private static final ImageIcon HTML_TAG_ICON
            = ImageUtilities.loadImageIcon("org/netbeans/modules/html/custom/resources/custom_html_element.png", false); // NOI18N

    private static final Color GRAY_COLOR = Color.GRAY;
    private static final Color DEFAULT_FG_COLOR = new Color(0, 0, 0xFF);

    //XXX defined if the tag is allowed in context
    private boolean possible = true;

    private org.netbeans.modules.html.custom.conf.Tag tag;

    public CustomTagCompletionItem(org.netbeans.modules.html.custom.conf.Tag tag, int substituteOffset) {
        super(tag.getName(), substituteOffset);
        this.tag = tag;
    }

    @Override
    protected String getSubstituteText() {
        return new StringBuilder().append("<").append(getItemText()).toString();
    }

    @Override
    protected String getLeftHtmlText() {
        StringBuilder b = new StringBuilder();
        if (possible) {
            b.append("<font color=#");
            b.append(hexColorCode(DEFAULT_FG_COLOR));
            b.append(">&lt;");
            b.append(getItemText());
            b.append("&gt;</font>");
        } else {
            b.append("<font color=#");
            b.append(hexColorCode(GRAY_COLOR));
            b.append(">&lt;");
            b.append(getItemText());
            b.append("&gt;</font>");
        }
        return b.toString();
    }

    @Override
    protected ImageIcon getIcon() {
        return HTML_TAG_ICON;
    }

    @Override
    public String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>");
        sb.append(tag.getName());
        sb.append("</h1>");
        sb.append("<h2>Custom HTML element</h2>");
        
        String description = tag.getDescription();
        if(description != null) {
            sb.append("<p>");
            sb.append(description);
            sb.append("</p>");
        }
        
        String documentation = tag.getDocumentation();
        if(documentation != null) {
            sb.append("<p>");
            sb.append(documentation);
            sb.append("</p>");
        }

        if(description != null || documentation != null) {
            sb.append("<hr/>");
        }
        
        sb.append("<p>");
        if (tag.getContexts().isEmpty()) {
            sb.append("<b>Allowed in all contexts.</b>");
        } else {
            sb.append("<b>Allowed in context(s): </b>");
            Iterator<String> i = tag.getContexts().iterator();
            while(i.hasNext()) {
                String ctx = i.next();
                sb.append(ctx);
                if(i.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append("</p>");
        
        sb.append("<p>");
        if (tag.getTagsNames().isEmpty()) {
            sb.append("<b>Defines no children elements.</b>");
        } else {
            sb.append("<b>Defined children elements: </b>");
            Iterator<String> i = tag.getTagsNames().iterator();
            while(i.hasNext()) {
                String ctx = i.next();
                sb.append(ctx);
                if(i.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append("</p>");
        
        sb.append("<p>");
        if (tag.getAttributesNames().isEmpty()) {
            sb.append("<b>Defines no attributes.</b>");
        } else {
            sb.append("<b>Defined attributes: </b>");
            Iterator<String> i = tag.getAttributesNames().iterator();
            while(i.hasNext()) {
                String ctx = i.next();
                sb.append(ctx);
                if(i.hasNext()) {
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
