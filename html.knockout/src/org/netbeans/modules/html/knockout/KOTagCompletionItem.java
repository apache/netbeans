/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
