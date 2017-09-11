/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;

/**
 *
 * @author mfukala@netbeans.org
 */
public class NamespaceCompletionItem implements CompletionProposal {

    private String namespacePrefix, resource;
    private int anchor;

    public NamespaceCompletionItem(String namespacePrefix, String resourceIdentifier, int anchorOffset) {
        this.anchor = anchorOffset;
        this.namespacePrefix = namespacePrefix;
        this.resource = resourceIdentifier;
    }

    @Override
    public ElementKind getKind() {
        return NamespacesModule.NAMESPACE_ELEMENT_KIND;
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        formatter.appendHtml("<font color=999999>"); //NOI18N
        formatter.appendText(resource);
        formatter.appendHtml("</font>"); //NOI18N
        return formatter.getText();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.appendHtml("<b>"); //NOI18N
        formatter.appendText(namespacePrefix);
        formatter.appendHtml("</b>"); //NOI18N
        return formatter.getText();
    }

    @Override
    public int getAnchorOffset() {
        return anchor;
    }

    @Override
    public ElementHandle getElement() {
        return null;
    }

    @Override
    public String getName() {
        return namespacePrefix;
    }

    @Override
    public String getInsertPrefix() {
        return namespacePrefix;
    }

    @Override
    public String getSortText() {
        return namespacePrefix;
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return 0;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }
}
