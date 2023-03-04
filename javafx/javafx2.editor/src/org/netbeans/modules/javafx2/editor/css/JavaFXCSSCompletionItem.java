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
package org.netbeans.modules.javafx2.editor.css;

import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.*;

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
public class JavaFXCSSCompletionItem  implements CompletionProposal {

    private String prefix, resource;
    private int anchor;

    public JavaFXCSSCompletionItem(String prefix, String resourceIdentifier, int anchorOffset) {
        this.anchor = anchorOffset;
        this.prefix = prefix;
        this.resource = resourceIdentifier;
    }

    @Override
    public ElementKind getKind() {
        return JavaFXCSSModule.JFX_CSS_ELEMENT_KIND;
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
        formatter.appendText(prefix);
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
        return prefix;
    }

    @Override
    public String getInsertPrefix() {
        return prefix;
    }

    @Override
    public String getSortText() {
        return prefix;
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
