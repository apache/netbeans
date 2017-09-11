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
