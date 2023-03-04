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
package org.netbeans.modules.html.knockout;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.javascript2.knockout.index.KnockoutCustomElement;

/**
 *
 * @author Roman Svitanic
 */
public class KOTagCompletionItem extends HtmlCompletionItem.Tag {

    private final KnockoutCustomElement element;
    private final List<String> alternativeLocations = new ArrayList<>();

    public KOTagCompletionItem(KnockoutCustomElement element, int substitutionOffset) {
        super(element.getName(), substitutionOffset, null, true);
        this.element = element;
    }

    @Override
    protected ImageIcon getIcon() {
        return KOUtils.KO_ICON;
    }

    @Override
    public String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>"); //NOI18N
        sb.append(element.getName());
        sb.append("</h1>"); //NOI18N
        sb.append("<h2>Custom Knockout element</h2>"); //NOI18N
        File file = new File(element.getDeclarationFile().toString());
        sb.append("<p>"); //NOI18N
        sb.append("Registered in "); //NOI18N
        sb.append(file.getName());
        for (String loc : alternativeLocations) {
            sb.append(", "); //NOI18N
            sb.append(loc);
        }
        sb.append("</p>"); //NOI18N
        return sb.toString();
    }

    @Override
    public boolean hasHelp() {
        return true;
    }

    public String getCustomElementName() {
        return element.getName();
    }

    public void addAlternativeLocation(URL url) {
        File file = new File(url.toString());
        if (!element.getDeclarationFile().equals(url)
                && !alternativeLocations.contains(file.getName())) {
            alternativeLocations.add(file.getName());
        }
    }
}
