/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.html.ojet;

import javax.swing.ImageIcon;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.ojet.data.DataItem;


/**
 *
 * @author Petr Pisl
 */
public class OJETCompletionHtmlItem extends HtmlCompletionItem.Attribute {
    
    private final DataItem data;
    
    public OJETCompletionHtmlItem(final DataItem data, final int substituteOffset) {
        super(data.getName(), substituteOffset, true, "");
        this.data = data;
    }
    
    @Override
    protected ImageIcon getIcon() {
        return OJETUtils.OJET_ICON;
    }
    
    @Override
    protected String getLeftHtmlText() {
        return new StringBuilder()
                .append("<font color=#628FB5>") //NOI18N
                .append(getItemText())
                .append("</font>").toString();  //NOI18N
    }

    @Override
    public int getAnchorOffset() {
        return substitutionOffset - 3;
    }

    
    
    @Override
    protected String getSubstituteText() {
        OJETUtils.logUsage(null);
        if (data.getTemplate() != null) {
            return data.getTemplate();
        }
        return super.getSubstituteText();
    }
    
    
}
